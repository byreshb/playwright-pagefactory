package io.github.byreshb.playwright.pagefactory.support;

import java.lang.reflect.Field;

/** Creates an {@link ElementLocator} per field. Mirrors Selenium's {@code ElementLocatorFactory}. */
public interface ElementLocatorFactory {

  /**
   * Returns the locator for the given field, or {@code null} if the field should be left alone.
   */
  ElementLocator createLocator(Field field);
}
