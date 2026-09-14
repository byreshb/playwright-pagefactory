package io.github.byreshb.playwright.pagefactory.support;

import com.microsoft.playwright.Locator;
import io.github.byreshb.playwright.pagefactory.FindAll;
import io.github.byreshb.playwright.pagefactory.FindBy;
import io.github.byreshb.playwright.pagefactory.FindBys;
import io.github.byreshb.playwright.pagefactory.PageFactoryFinder;
import io.github.byreshb.playwright.pagefactory.support.internal.LocatingLocatorListHandler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

/**
 * The default {@link FieldDecorator}: decorates {@link Locator} fields and annotated
 * {@code List<Locator>} fields, leaving everything else alone. Mirrors Selenium's
 * {@code DefaultFieldDecorator}.
 *
 * <p><b>Locator fields</b> receive a real Playwright {@link Locator} rather than a dynamic proxy.
 * A Playwright locator is already lazy (it re-resolves on every action), so a proxy would add
 * nothing, and a proxy would break Playwright methods that cast to the internal implementation
 * ({@code Locator.or}, {@code Locator.and}, {@code setHas(...)} etc.).
 *
 * <p><b>List&lt;Locator&gt; fields</b> receive a proxy that re-queries the page on every method
 * call (so the list always reflects the current DOM) unless the field is annotated with
 * {@link io.github.byreshb.playwright.pagefactory.CacheLookup}. As in Selenium, list fields are
 * only decorated when they carry a locating annotation.
 */
public class DefaultFieldDecorator implements FieldDecorator {

  /** The factory used to build a locator for each field. */
  protected final ElementLocatorFactory factory;

  public DefaultFieldDecorator(ElementLocatorFactory factory) {
    this.factory = Objects.requireNonNull(factory, "factory");
  }

  @Override
  public Object decorate(ClassLoader loader, Field field) {
    boolean isLocator = Locator.class.isAssignableFrom(field.getType());
    if (!(isLocator || isDecoratableList(field))) {
      return null;
    }

    ElementLocator locator = factory.createLocator(field);
    if (locator == null) {
      return null;
    }

    if (isLocator) {
      return proxyForLocator(loader, locator);
    }
    return proxyForListLocator(loader, locator);
  }

  /** True for {@code List<Locator>} fields that carry a locating annotation. */
  protected boolean isDecoratableList(Field field) {
    if (!List.class.isAssignableFrom(field.getType())) {
      return false;
    }
    Type genericType = field.getGenericType();
    if (!(genericType instanceof ParameterizedType)) {
      return false;
    }
    Type listType = ((ParameterizedType) genericType).getActualTypeArguments()[0];
    if (!Locator.class.equals(listType)) {
      return false;
    }
    return hasLocatingAnnotation(field);
  }

  /** True when the field carries FindBy/FindBys/FindAll or any custom finder annotation. */
  protected boolean hasLocatingAnnotation(Field field) {
    if (field.getAnnotation(FindBy.class) != null
        || field.getAnnotation(FindBys.class) != null
        || field.getAnnotation(FindAll.class) != null) {
      return true;
    }
    for (Annotation annotation : field.getDeclaredAnnotations()) {
      if (annotation.annotationType().isAnnotationPresent(PageFactoryFinder.class)) {
        return true;
      }
    }
    return false;
  }

  /** Value for a {@link Locator} field. The default returns the real locator, un-proxied. */
  protected Locator proxyForLocator(ClassLoader loader, ElementLocator locator) {
    return locator.findLocator();
  }

  /** Value for a {@code List<Locator>} field: a proxy that re-queries on each call. */
  @SuppressWarnings("unchecked")
  protected List<Locator> proxyForListLocator(ClassLoader loader, ElementLocator locator) {
    InvocationHandler handler = new LocatingLocatorListHandler(locator);
    return (List<Locator>) Proxy.newProxyInstance(loader, new Class<?>[] {List.class}, handler);
  }
}
