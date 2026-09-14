package io.github.byreshb.playwright.pagefactory.support;

import com.microsoft.playwright.Locator;
import io.github.byreshb.playwright.pagefactory.By;
import io.github.byreshb.playwright.pagefactory.SearchContext;
import java.lang.reflect.Field;
import java.util.List;

/**
 * The default {@link ElementLocator}: resolves the field's {@link By} against a {@link
 * SearchContext}, optionally caching the result. Mirrors Selenium's {@code DefaultElementLocator}.
 */
public class DefaultElementLocator implements ElementLocator {
  private final SearchContext searchContext;
  private final boolean shouldCache;
  private final By by;

  private Locator cachedLocator;
  private List<Locator> cachedLocatorList;

  /**
   * @param searchContext the context to resolve the field against
   * @param field the page-object field that will hold the located value
   */
  public DefaultElementLocator(SearchContext searchContext, Field field) {
    this(searchContext, new Annotations(field));
  }

  /**
   * Use this constructor to process custom annotations.
   *
   * @param searchContext the context to resolve the field against
   * @param annotations an {@link AbstractAnnotations} implementation
   */
  public DefaultElementLocator(SearchContext searchContext, AbstractAnnotations annotations) {
    this.searchContext = searchContext;
    this.shouldCache = annotations.isLookupCached();
    this.by = annotations.buildBy();
  }

  @Override
  public Locator findLocator() {
    if (cachedLocator != null && shouldCache()) {
      return cachedLocator;
    }
    Locator locator = by.locate(searchContext);
    if (shouldCache()) {
      cachedLocator = locator;
    }
    return locator;
  }

  @Override
  public List<Locator> findLocators() {
    if (cachedLocatorList != null && shouldCache()) {
      return cachedLocatorList;
    }
    List<Locator> locators = findLocator().all();
    if (shouldCache()) {
      cachedLocatorList = locators;
    }
    return locators;
  }

  /** Whether results are cached; override to change the policy. */
  protected boolean shouldCache() {
    return shouldCache;
  }

  /** The {@link By} this locator resolves. */
  public By getBy() {
    return by;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " '" + by + "'";
  }
}
