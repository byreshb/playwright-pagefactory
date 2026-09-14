package io.github.byreshb.playwright.pagefactory;

import io.github.byreshb.playwright.pagefactory.support.AbstractFindByBuilder;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

/**
 * Declares how a {@link com.microsoft.playwright.Locator} (or {@code List<Locator>}) field on a
 * page object should be located. This is the Playwright equivalent of Selenium's
 * {@code org.openqa.selenium.support.FindBy}.
 *
 * <p>Two forms are supported, exactly like Selenium:
 *
 * <pre><code>
 * // "short" form: one strategy attribute
 * &#64;FindBy(id = "username")      Locator username;
 * &#64;FindBy(css = "button.primary") Locator submit;
 *
 * // "long" form: how + using
 * &#64;FindBy(how = How.XPATH, using = "//a[@href='/logout']") Locator logout;
 * </code></pre>
 *
 * <p>Exactly one strategy may be given per annotation. In addition to the Selenium strategies,
 * Playwright's {@code getBy*} strategies are available: {@link #selector()}, {@link #testId()},
 * {@link #text()}, {@link #label()}, {@link #placeholder()}, {@link #altText()} and
 * {@link #title()}.
 *
 * @see FindBys
 * @see FindAll
 * @see CacheLookup
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
@PageFactoryFinder(FindBy.FindByBuilder.class)
public @interface FindBy {

  /** Long form: the strategy. Requires {@link #using()}. */
  How how() default How.UNSET;

  /** Long form: the value for {@link #how()}. */
  String using() default "";

  // ---- Selenium short forms ------------------------------------------------------------------

  /** {@link By#id(String)} */
  String id() default "";

  /** {@link By#name(String)} */
  String name() default "";

  /** {@link By#className(String)} */
  String className() default "";

  /** {@link By#css(String)} */
  String css() default "";

  /** {@link By#tagName(String)} */
  String tagName() default "";

  /** {@link By#linkText(String)} */
  String linkText() default "";

  /** {@link By#partialLinkText(String)} */
  String partialLinkText() default "";

  /** {@link By#xpath(String)} */
  String xpath() default "";

  // ---- Playwright short forms ----------------------------------------------------------------

  /** {@link By#selector(String)}: raw Playwright selector, e.g. {@code "text=Sign in"}. */
  String selector() default "";

  /** {@link By#testId(String)} */
  String testId() default "";

  /** {@link By#text(String)} */
  String text() default "";

  /** {@link By#label(String)} */
  String label() default "";

  /** {@link By#placeholder(String)} */
  String placeholder() default "";

  /** {@link By#altText(String)} */
  String altText() default "";

  /** {@link By#title(String)} */
  String title() default "";

  /** Converts a {@link FindBy} into a {@link By}. */
  class FindByBuilder extends AbstractFindByBuilder {
    @Override
    public By buildIt(Annotation annotation, Field field) {
      FindBy findBy = (FindBy) annotation;
      assertValidFindBy(findBy);
      By by = buildByFromShortFindBy(findBy);
      if (by == null) {
        by = buildByFromLongFindBy(findBy);
      }
      return by;
    }
  }
}
