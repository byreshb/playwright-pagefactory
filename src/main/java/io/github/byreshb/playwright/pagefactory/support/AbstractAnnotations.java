package io.github.byreshb.playwright.pagefactory.support;

import io.github.byreshb.playwright.pagefactory.By;

/**
 * Abstraction over how a page-object field is annotated, so {@link DefaultElementLocator} does not
 * depend on the concrete annotation set. Mirrors Selenium's {@code AbstractAnnotations}.
 */
public abstract class AbstractAnnotations {

  /** Builds the {@link By} described by the field's annotations (or a default). */
  public abstract By buildBy();

  /** Whether lookup results for the field should be cached after the first call. */
  public abstract boolean isLookupCached();
}
