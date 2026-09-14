package io.github.byreshb.playwright.pagefactory.support;

import io.github.byreshb.playwright.pagefactory.SearchContext;
import java.lang.reflect.Field;
import java.util.Objects;

/** Creates a {@link DefaultElementLocator} for every field. */
public class DefaultElementLocatorFactory implements ElementLocatorFactory {
  private final SearchContext searchContext;

  public DefaultElementLocatorFactory(SearchContext searchContext) {
    this.searchContext = Objects.requireNonNull(searchContext, "searchContext");
  }

  @Override
  public ElementLocator createLocator(Field field) {
    return new DefaultElementLocator(searchContext, field);
  }
}
