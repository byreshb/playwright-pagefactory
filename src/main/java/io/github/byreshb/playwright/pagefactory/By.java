package io.github.byreshb.playwright.pagefactory;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.Objects;
import java.util.function.Function;

/**
 * A mechanism for locating elements, modelled on Selenium's {@code org.openqa.selenium.By}.
 *
 * <p>Where Selenium's {@code By} finds {@code WebElement}s in a {@code SearchContext}, this one
 * produces a lazy Playwright {@link Locator} from a {@link SearchContext}. Every Selenium strategy
 * ({@link #id}, {@link #name}, {@link #className}, {@link #css}, {@link #tagName},
 * {@link #linkText}, {@link #partialLinkText}, {@link #xpath}) is translated into an equivalent
 * Playwright selector, and Playwright's own {@code getBy*} strategies are exposed alongside them.
 *
 * <p>Instances are immutable, and two {@code By}s are equal when they describe the same strategy
 * and value.
 */
public abstract class By {

  /** Resolves this strategy against the given context and returns a (lazy) locator. */
  public abstract Locator locate(SearchContext context);

  /** Convenience for {@code locate(SearchContext.of(page))}. */
  public Locator locate(Page page) {
    return locate(SearchContext.of(page));
  }

  /** Convenience for {@code locate(SearchContext.of(scope))}: resolves relative to a locator. */
  public Locator locate(Locator scope) {
    return locate(SearchContext.of(scope));
  }

  // ---------------------------------------------------------------------------------------------
  // Selenium strategies
  // ---------------------------------------------------------------------------------------------

  /** Matches elements whose {@code id} attribute equals the given value. */
  public static By id(String id) {
    requireNonEmpty(id, "id");
    return new SelectorBy("By.id", id, "css=[id=" + cssString(id) + "]");
  }

  /** Matches elements whose {@code name} attribute equals the given value. */
  public static By name(String name) {
    requireNonEmpty(name, "name");
    return new SelectorBy("By.name", name, "css=[name=" + cssString(name) + "]");
  }

  /**
   * Matches elements carrying the given class. Like Selenium, compound class names ("a b") are
   * rejected; use {@link #css} for those.
   */
  public static By className(String className) {
    requireNonEmpty(className, "className");
    if (className.matches(".*\\s.*")) {
      throw new IllegalArgumentException(
          "Compound class names not permitted: '" + className + "'. Use By.css(\".a.b\")");
    }
    return new SelectorBy("By.className", className, "css=[class~=" + cssString(className) + "]");
  }

  /** Matches elements by CSS selector. */
  public static By css(String cssSelector) {
    requireNonEmpty(cssSelector, "cssSelector");
    return new SelectorBy("By.cssSelector", cssSelector, "css=" + cssSelector);
  }

  /** Matches elements by tag name. */
  public static By tagName(String tagName) {
    requireNonEmpty(tagName, "tagName");
    return new SelectorBy("By.tagName", tagName, "css=" + tagName);
  }

  /**
   * Matches {@code <a>} elements whose whitespace-normalised text equals the given value exactly,
   * case-sensitively, like Selenium's {@code By.linkText}.
   */
  public static By linkText(String linkText) {
    requireNonEmpty(linkText, "linkText");
    return new SelectorBy("By.linkText", linkText,
        "xpath=//a[normalize-space(.)=" + xpathString(linkText) + "]");
  }

  /**
   * Matches {@code <a>} elements whose whitespace-normalised text contains the given value,
   * case-sensitively, like Selenium's {@code By.partialLinkText}.
   */
  public static By partialLinkText(String partialLinkText) {
    requireNonEmpty(partialLinkText, "partialLinkText");
    return new SelectorBy("By.partialLinkText", partialLinkText,
        "xpath=//a[contains(normalize-space(.), " + xpathString(partialLinkText) + ")]");
  }

  /** Matches elements by XPath expression. */
  public static By xpath(String xpathExpression) {
    requireNonEmpty(xpathExpression, "xpathExpression");
    return new SelectorBy("By.xpath", xpathExpression, "xpath=" + xpathExpression);
  }

  /**
   * Matches elements whose {@code id} <em>or</em> {@code name} equals the given value. This is the
   * default strategy used for un-annotated {@code Locator} fields, mirroring Selenium's
   * {@code ByIdOrName}.
   */
  public static By idOrName(String idOrName) {
    return new ByIdOrName(idOrName);
  }

  // ---------------------------------------------------------------------------------------------
  // Playwright strategies
  // ---------------------------------------------------------------------------------------------

  /**
   * Uses a raw Playwright selector verbatim, e.g. {@code "text=Sign in"},
   * {@code "css=nav >> a.active"} or {@code "//button[@type='submit']"}.
   */
  public static By selector(String selector) {
    requireNonEmpty(selector, "selector");
    return new SelectorBy("By.selector", selector, selector);
  }

  /** Playwright's {@code getByTestId(testId)}. */
  public static By testId(String testId) {
    requireNonEmpty(testId, "testId");
    return new ContextBy("By.testId", testId, ctx -> ctx.getByTestId(testId));
  }

  /** Playwright's {@code getByText(text)} (case-insensitive substring match). */
  public static By text(String text) {
    requireNonEmpty(text, "text");
    return new ContextBy("By.text", text, ctx -> ctx.getByText(text));
  }

  /** Playwright's {@code getByLabel(text)}. */
  public static By label(String label) {
    requireNonEmpty(label, "label");
    return new ContextBy("By.label", label, ctx -> ctx.getByLabel(label));
  }

  /** Playwright's {@code getByPlaceholder(text)}. */
  public static By placeholder(String placeholder) {
    requireNonEmpty(placeholder, "placeholder");
    return new ContextBy("By.placeholder", placeholder, ctx -> ctx.getByPlaceholder(placeholder));
  }

  /** Playwright's {@code getByAltText(text)}. */
  public static By altText(String altText) {
    requireNonEmpty(altText, "altText");
    return new ContextBy("By.altText", altText, ctx -> ctx.getByAltText(altText));
  }

  /** Playwright's {@code getByTitle(text)}. */
  public static By title(String title) {
    requireNonEmpty(title, "title");
    return new ContextBy("By.title", title, ctx -> ctx.getByTitle(title));
  }

  // ---------------------------------------------------------------------------------------------
  // Combinators
  // ---------------------------------------------------------------------------------------------

  /** Each {@code By} is resolved inside the result of the previous one. See {@link ByChained}. */
  public static By chained(By... bys) {
    return new ByChained(bys);
  }

  /** Matches elements found by <em>any</em> of the given {@code By}s. See {@link ByAll}. */
  public static By all(By... bys) {
    return new ByAll(bys);
  }

  // ---------------------------------------------------------------------------------------------
  // Object contract: equality by description, like Selenium
  // ---------------------------------------------------------------------------------------------

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof By)) {
      return false;
    }
    return toString().equals(o.toString());
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  @Override
  public abstract String toString();

  // ---------------------------------------------------------------------------------------------
  // Implementations
  // ---------------------------------------------------------------------------------------------

  /**
   * A {@code By} backed by a Playwright selector string. Exposed so custom
   * {@link io.github.byreshb.playwright.pagefactory.support.ElementLocator}s can inspect the
   * selector that will be passed to {@code locator(...)}.
   */
  public static final class SelectorBy extends By {
    private final String strategy;
    private final String value;
    private final String selector;

    SelectorBy(String strategy, String value, String selector) {
      this.strategy = strategy;
      this.value = value;
      this.selector = selector;
    }

    /** The Playwright selector string this {@code By} passes to {@code locator(...)}. */
    public String getSelector() {
      return selector;
    }

    @Override
    public Locator locate(SearchContext context) {
      return context.locator(selector);
    }

    @Override
    public String toString() {
      return strategy + ": " + value;
    }
  }

  /** A {@code By} that calls one of the {@code getBy*} methods on the context. */
  private static final class ContextBy extends By {
    private final String strategy;
    private final String value;
    private final Function<SearchContext, Locator> finder;

    ContextBy(String strategy, String value, Function<SearchContext, Locator> finder) {
      this.strategy = strategy;
      this.value = value;
      this.finder = finder;
    }

    @Override
    public Locator locate(SearchContext context) {
      return finder.apply(context);
    }

    @Override
    public String toString() {
      return strategy + ": " + value;
    }
  }

  // ---------------------------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------------------------

  static void requireNonEmpty(String value, String what) {
    Objects.requireNonNull(value, what + " must not be null");
    if (value.isEmpty()) {
      throw new IllegalArgumentException(what + " must not be empty");
    }
  }

  /** Quotes a value for use inside a CSS attribute selector: {@code [attr="..."]}. */
  static String cssString(String value) {
    return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
  }

  /**
   * Quotes a value as an XPath 1.0 string literal. XPath has no escape sequences, so a value that
   * contains both quote characters is assembled with {@code concat()}.
   */
  static String xpathString(String value) {
    if (!value.contains("'")) {
      return "'" + value + "'";
    }
    if (!value.contains("\"")) {
      return "\"" + value + "\"";
    }
    StringBuilder sb = new StringBuilder("concat(");
    String[] parts = value.split("'", -1);
    for (int i = 0; i < parts.length; i++) {
      if (i > 0) {
        sb.append(", \"'\", ");
      }
      sb.append("'").append(parts[i]).append("'");
    }
    return sb.append(")").toString();
  }
}
