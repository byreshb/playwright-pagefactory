package io.github.byreshb.playwright.pagefactory;

import com.microsoft.playwright.Frame;
import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.github.byreshb.playwright.pagefactory.support.DefaultElementLocatorFactory;
import io.github.byreshb.playwright.pagefactory.support.DefaultFieldDecorator;
import io.github.byreshb.playwright.pagefactory.support.ElementLocatorFactory;
import io.github.byreshb.playwright.pagefactory.support.FieldDecorator;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Objects;

/**
 * Factory that populates the {@link Locator} and {@code List<Locator>} fields of a page object, the
 * same way Selenium's {@code org.openqa.selenium.support.PageFactory} populates {@code WebElement}
 * fields.
 *
 * <pre><code>
 * public class LoginPage {
 *   &#64;FindBy(id = "username")            Locator username;
 *   &#64;FindBy(css = "input[type=password]") Locator password;
 *   &#64;FindBy(text = "Sign in")            Locator signIn;
 *
 *   public LoginPage(Page page) {
 *     PageFactory.initElements(page, this);
 *   }
 * }
 * </code></pre>
 *
 * <p>Fields are resolved for the page object's class and all of its superclasses. Static and final
 * fields are ignored (a final field was assigned by the constructor, typically the component's root
 * locator, and must not be clobbered by the default id-or-name lookup). Which fields are decorated,
 * and with what, is decided by a {@link FieldDecorator}; the default one is described in {@link
 * DefaultFieldDecorator}.
 *
 * @see FindBy
 * @see FindBys
 * @see FindAll
 * @see CacheLookup
 */
public final class PageFactory {

  private PageFactory() {}

  // ---------------------------------------------------------------------------------------------
  // Instantiate + initialise
  // ---------------------------------------------------------------------------------------------

  /**
   * Instantiates {@code pageClassToProxy} and initialises its fields against {@code page}.
   *
   * <p>The class is instantiated through a constructor that accepts a {@link Page} if there is one,
   * falling back to a no-arg constructor.
   *
   * @param page the page to resolve fields against
   * @param pageClassToProxy the page-object class
   * @param <T> the page-object type
   * @return the initialised page object
   */
  public static <T> T initElements(Page page, Class<T> pageClassToProxy) {
    return initElements(SearchContext.of(page), pageClassToProxy);
  }

  /**
   * Instantiates {@code pageClassToProxy} and initialises its fields against {@code scope}, i.e.
   * relative to the element(s) the locator matches. Useful for reusable components.
   *
   * <p>The class is instantiated through a constructor that accepts a {@link Locator} if there is
   * one, falling back to a no-arg constructor.
   */
  public static <T> T initElements(Locator scope, Class<T> pageClassToProxy) {
    return initElements(SearchContext.of(scope), pageClassToProxy);
  }

  /**
   * Instantiates {@code pageClassToProxy} and initialises its fields against {@code searchContext}.
   *
   * <p>Constructor resolution, in order: a single-argument constructor accepting the object the
   * context wraps ({@link Page}, {@link Frame}, {@link Locator} or {@link FrameLocator}); a
   * single-argument constructor accepting {@link SearchContext}; a no-arg constructor.
   */
  public static <T> T initElements(SearchContext searchContext, Class<T> pageClassToProxy) {
    T page = instantiatePage(searchContext, pageClassToProxy);
    initElements(searchContext, page);
    return page;
  }

  // ---------------------------------------------------------------------------------------------
  // Initialise an existing instance
  // ---------------------------------------------------------------------------------------------

  /** Initialises the fields of an existing page object against {@code page}. */
  public static void initElements(Page page, Object pageObject) {
    initElements(SearchContext.of(page), pageObject);
  }

  /** Initialises the fields of an existing page object against a {@link Frame}. */
  public static void initElements(Frame frame, Object pageObject) {
    initElements(SearchContext.of(frame), pageObject);
  }

  /** Initialises the fields of an existing page object relative to a {@link Locator}. */
  public static void initElements(Locator scope, Object pageObject) {
    initElements(SearchContext.of(scope), pageObject);
  }

  /** Initialises the fields of an existing page object inside a {@link FrameLocator}. */
  public static void initElements(FrameLocator frameLocator, Object pageObject) {
    initElements(SearchContext.of(frameLocator), pageObject);
  }

  /** Initialises the fields of an existing page object against any {@link SearchContext}. */
  public static void initElements(SearchContext searchContext, Object pageObject) {
    initElements(new DefaultElementLocatorFactory(searchContext), pageObject);
  }

  /**
   * As above, but with a custom {@link ElementLocatorFactory}. If the factory returns {@code null}
   * for a field, that field is not decorated.
   */
  public static void initElements(ElementLocatorFactory factory, Object pageObject) {
    initElements(new DefaultFieldDecorator(factory), pageObject);
  }

  /** As above, but with a custom {@link FieldDecorator} deciding every field's value. */
  public static void initElements(FieldDecorator decorator, Object pageObject) {
    Objects.requireNonNull(decorator, "decorator");
    Objects.requireNonNull(pageObject, "pageObject");

    Class<?> proxyIn = pageObject.getClass();
    while (proxyIn != null && proxyIn != Object.class) {
      proxyFields(decorator, pageObject, proxyIn);
      proxyIn = proxyIn.getSuperclass();
    }
  }

  // ---------------------------------------------------------------------------------------------
  // Internals
  // ---------------------------------------------------------------------------------------------

  private static void proxyFields(FieldDecorator decorator, Object pageObject, Class<?> proxyIn) {
    ClassLoader loader = pageObject.getClass().getClassLoader();
    for (Field field : proxyIn.getDeclaredFields()) {
      int mods = field.getModifiers();
      if (Modifier.isStatic(mods) || Modifier.isFinal(mods)) {
        // A final field was necessarily assigned by the constructor; never clobber it.
        continue;
      }
      Object value = decorator.decorate(loader, field);
      if (value == null) {
        continue;
      }
      try {
        field.setAccessible(true);
        field.set(pageObject, value);
      } catch (IllegalAccessException e) {
        throw new IllegalStateException(
            "Cannot set field '" + field.getName() + "' on " + proxyIn.getName(), e);
      }
    }
  }

  private static <T> T instantiatePage(SearchContext searchContext, Class<T> pageClass) {
    Objects.requireNonNull(pageClass, "pageClassToProxy");
    Object unwrapped = searchContext.unwrap();
    try {
      Constructor<T> ctor = findSingleArgConstructor(pageClass, unwrapped);
      if (ctor != null) {
        return ctor.newInstance(unwrapped);
      }
      ctor = findSingleArgConstructor(pageClass, searchContext);
      if (ctor != null) {
        return ctor.newInstance(searchContext);
      }
      Constructor<T> noArg = pageClass.getDeclaredConstructor();
      noArg.setAccessible(true);
      return noArg.newInstance();
    } catch (NoSuchMethodException e) {
      throw new IllegalArgumentException(
          pageClass.getName()
              + " needs a constructor taking "
              + unwrapped.getClass().getSimpleName()
              + " (or Page/Locator/Frame/FrameLocator), SearchContext, or no arguments",
          e);
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      if (cause instanceof RuntimeException) {
        throw (RuntimeException) cause;
      }
      throw new IllegalStateException("Constructor of " + pageClass.getName() + " failed", cause);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Cannot instantiate " + pageClass.getName(), e);
    }
  }

  @SuppressWarnings("unchecked")
  private static <T> Constructor<T> findSingleArgConstructor(Class<T> pageClass, Object arg) {
    for (Constructor<?> ctor : pageClass.getDeclaredConstructors()) {
      Class<?>[] params = ctor.getParameterTypes();
      if (params.length == 1 && params[0].isInstance(arg)) {
        ctor.setAccessible(true);
        return (Constructor<T>) ctor;
      }
    }
    return null;
  }
}
