package io.github.byreshb.playwright.pagefactory.support;

import com.microsoft.playwright.Locator;

import java.util.List;

/**
 * Knows how to locate the value of a single page-object field. Mirrors Selenium's
 * {@code ElementLocator}, with {@link Locator} in place of {@code WebElement}.
 */
public interface ElementLocator {

  /** Returns a locator for the field. The locator is lazy; nothing is queried yet. */
  Locator findLocator();

  /**
   * Returns one locator per currently matching element, i.e. {@code findLocator().all()}. Unlike
   * {@link #findLocator()} this <em>does</em> query the page.
   */
  List<Locator> findLocators();
}
