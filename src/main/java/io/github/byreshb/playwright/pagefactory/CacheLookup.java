package io.github.byreshb.playwright.pagefactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker used to indicate that a lookup result should be cached after the first call.
 *
 * <p>A Playwright {@link com.microsoft.playwright.Locator} is already lazy and re-resolves the
 * element on every action, so for {@code Locator} fields this annotation only avoids re-creating
 * the locator object. It matters for {@code List<Locator>} fields: without it the list is
 * re-queried ({@code locator.all()}) on <em>every</em> access so it always reflects the current
 * DOM; with it the snapshot taken on first access is reused, which is what Selenium's
 * {@code @CacheLookup} does for {@code List<WebElement>}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface CacheLookup {}
