package io.github.byreshb.playwright.pagefactory.support;

import java.lang.reflect.Field;

/**
 * Decides what value, if any, a page-object field should receive. Mirrors Selenium's {@code
 * FieldDecorator}.
 */
public interface FieldDecorator {

  /**
   * Returns the value to assign to {@code field}, or {@code null} to leave the field untouched.
   *
   * @param loader class loader to use when creating proxies
   * @param field the field being decorated
   */
  Object decorate(ClassLoader loader, Field field);
}
