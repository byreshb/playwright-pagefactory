package io.github.byreshb.playwright.pagefactory;

import com.microsoft.playwright.Locator;
import java.util.Arrays;
import java.util.Objects;

/**
 * Chains several {@link By}s: each is resolved <em>inside</em> the elements matched by the previous
 * one. This is the strategy behind {@link FindBys} and mirrors Selenium's {@code
 * org.openqa.selenium.support.pagefactory.ByChained}.
 *
 * <p>For example {@code new ByChained(By.id("form"), By.tagName("input"))} is equivalent to {@code
 * page.locator("css=[id=\"form\"]").locator("css=input")}.
 */
public class ByChained extends By {
  private final By[] bys;

  public ByChained(By... bys) {
    Objects.requireNonNull(bys, "bys");
    if (bys.length == 0) {
      throw new IllegalArgumentException("ByChained requires at least one By");
    }
    for (By by : bys) {
      Objects.requireNonNull(by, "ByChained does not accept null By elements");
    }
    this.bys = bys.clone();
  }

  @Override
  public Locator locate(SearchContext context) {
    Locator current = bys[0].locate(context);
    for (int i = 1; i < bys.length; i++) {
      current = bys[i].locate(SearchContext.of(current));
    }
    return current;
  }

  @Override
  public String toString() {
    return "By.chained(" + Arrays.toString(bys) + ")";
  }
}
