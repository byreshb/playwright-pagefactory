package io.github.byreshb.playwright.pagefactory;

import com.microsoft.playwright.Frame;
import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.Objects;
import java.util.function.Function;

/**
 * The Playwright counterpart of Selenium's {@code SearchContext}: something that can produce a
 * {@link Locator} from a selector.
 *
 * <p>Playwright has four unrelated types that can locate elements ({@link Page}, {@link Frame},
 * {@link Locator} and {@link FrameLocator}) but no shared interface between them. This interface
 * papers over that so a {@link By} can be resolved against any of them, which is what makes
 * scoping a page object to a component (via {@code PageFactory.initElements(locator, this)})
 * possible.
 *
 * <p>Obtain an instance with one of the {@code of(...)} factory methods.
 */
public interface SearchContext {

  /** Equivalent of {@code page.locator(selector)} on the wrapped object. */
  Locator locator(String selector);

  /** Equivalent of {@code page.getByTestId(testId)} on the wrapped object. */
  Locator getByTestId(String testId);

  /** Equivalent of {@code page.getByText(text)} on the wrapped object. */
  Locator getByText(String text);

  /** Equivalent of {@code page.getByLabel(text)} on the wrapped object. */
  Locator getByLabel(String text);

  /** Equivalent of {@code page.getByPlaceholder(text)} on the wrapped object. */
  Locator getByPlaceholder(String text);

  /** Equivalent of {@code page.getByAltText(text)} on the wrapped object. */
  Locator getByAltText(String text);

  /** Equivalent of {@code page.getByTitle(text)} on the wrapped object. */
  Locator getByTitle(String text);

  /**
   * The underlying Playwright object this context delegates to: a {@link Page}, {@link Frame},
   * {@link Locator} or {@link FrameLocator}.
   */
  Object unwrap();

  /** Wraps a {@link Page}. Selectors resolve against the whole main frame. */
  static SearchContext of(Page page) {
    Objects.requireNonNull(page, "page");
    return new Adapter(page, page::locator, page::getByTestId, page::getByText, page::getByLabel,
        page::getByPlaceholder, page::getByAltText, page::getByTitle);
  }

  /** Wraps a {@link Frame}. */
  static SearchContext of(Frame frame) {
    Objects.requireNonNull(frame, "frame");
    return new Adapter(frame, frame::locator, frame::getByTestId, frame::getByText,
        frame::getByLabel, frame::getByPlaceholder, frame::getByAltText, frame::getByTitle);
  }

  /** Wraps a {@link Locator}. Selectors resolve relative to the element(s) it matches. */
  static SearchContext of(Locator locator) {
    Objects.requireNonNull(locator, "locator");
    return new Adapter(locator, locator::locator, locator::getByTestId, locator::getByText,
        locator::getByLabel, locator::getByPlaceholder, locator::getByAltText,
        locator::getByTitle);
  }

  /** Wraps a {@link FrameLocator}. Selectors resolve inside the targeted iframe. */
  static SearchContext of(FrameLocator frameLocator) {
    Objects.requireNonNull(frameLocator, "frameLocator");
    return new Adapter(frameLocator, frameLocator::locator, frameLocator::getByTestId,
        frameLocator::getByText, frameLocator::getByLabel, frameLocator::getByPlaceholder,
        frameLocator::getByAltText, frameLocator::getByTitle);
  }

  /**
   * Wraps any supported Playwright object, or returns the argument unchanged if it already is a
   * {@link SearchContext}.
   *
   * @throws IllegalArgumentException if the object is not a Page, Frame, Locator, FrameLocator or
   *     SearchContext
   */
  static SearchContext of(Object context) {
    Objects.requireNonNull(context, "context");
    if (context instanceof SearchContext) {
      return (SearchContext) context;
    }
    if (context instanceof Page) {
      return of((Page) context);
    }
    if (context instanceof Frame) {
      return of((Frame) context);
    }
    if (context instanceof Locator) {
      return of((Locator) context);
    }
    if (context instanceof FrameLocator) {
      return of((FrameLocator) context);
    }
    throw new IllegalArgumentException(
        "Cannot create a SearchContext from " + context.getClass().getName()
            + "; expected Page, Frame, Locator or FrameLocator");
  }

  /** Delegating implementation shared by all four Playwright types. */
  final class Adapter implements SearchContext {
    private final Object target;
    private final Function<String, Locator> locator;
    private final Function<String, Locator> byTestId;
    private final Function<String, Locator> byText;
    private final Function<String, Locator> byLabel;
    private final Function<String, Locator> byPlaceholder;
    private final Function<String, Locator> byAltText;
    private final Function<String, Locator> byTitle;

    private Adapter(Object target,
                    Function<String, Locator> locator,
                    Function<String, Locator> byTestId,
                    Function<String, Locator> byText,
                    Function<String, Locator> byLabel,
                    Function<String, Locator> byPlaceholder,
                    Function<String, Locator> byAltText,
                    Function<String, Locator> byTitle) {
      this.target = target;
      this.locator = locator;
      this.byTestId = byTestId;
      this.byText = byText;
      this.byLabel = byLabel;
      this.byPlaceholder = byPlaceholder;
      this.byAltText = byAltText;
      this.byTitle = byTitle;
    }

    @Override public Locator locator(String selector) { return locator.apply(selector); }
    @Override public Locator getByTestId(String testId) { return byTestId.apply(testId); }
    @Override public Locator getByText(String text) { return byText.apply(text); }
    @Override public Locator getByLabel(String text) { return byLabel.apply(text); }
    @Override public Locator getByPlaceholder(String text) { return byPlaceholder.apply(text); }
    @Override public Locator getByAltText(String text) { return byAltText.apply(text); }
    @Override public Locator getByTitle(String text) { return byTitle.apply(text); }
    @Override public Object unwrap() { return target; }

    @Override
    public String toString() {
      return "SearchContext(" + target + ")";
    }
  }
}
