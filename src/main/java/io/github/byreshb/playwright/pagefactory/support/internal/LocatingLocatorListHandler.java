package io.github.byreshb.playwright.pagefactory.support.internal;

import com.microsoft.playwright.Locator;
import io.github.byreshb.playwright.pagefactory.support.ElementLocator;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Backs the {@code List<Locator>} proxy: every call re-queries via
 * {@link ElementLocator#findLocators()} and forwards to the resulting list. Mirrors Selenium's
 * {@code LocatingElementListHandler}.
 */
public class LocatingLocatorListHandler implements InvocationHandler {
  private final ElementLocator locator;

  public LocatingLocatorListHandler(ElementLocator locator) {
    this.locator = locator;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    // Object methods that must not trigger a page query.
    if (method.getDeclaringClass() == Object.class) {
      switch (method.getName()) {
        case "toString":
          return "Proxy list for: " + locator;
        case "hashCode":
          return System.identityHashCode(proxy);
        case "equals":
          return proxy == args[0];
        default:
          break;
      }
    }

    List<Locator> locators = locator.findLocators();
    try {
      return method.invoke(locators, args);
    } catch (InvocationTargetException e) {
      throw e.getCause();
    }
  }
}
