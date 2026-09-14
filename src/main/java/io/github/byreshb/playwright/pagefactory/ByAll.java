package io.github.byreshb.playwright.pagefactory;

import com.microsoft.playwright.Locator;
import java.util.Arrays;
import java.util.Objects;

/**
 * Matches elements found by <em>any</em> of the given {@link By}s, i.e. their union. This is the
 * strategy behind {@link FindAll} and mirrors Selenium's {@code
 * org.openqa.selenium.support.pagefactory.ByAll}.
 *
 * <p>Implemented with {@link Locator#or(Locator)}, so the resulting locator matches every element
 * matched by at least one of the constituent locators, in document order.
 */
public class ByAll extends By {
  private final By[] bys;

  public ByAll(By... bys) {
    Objects.requireNonNull(bys, "bys");
    if (bys.length == 0) {
      throw new IllegalArgumentException("ByAll requires at least one By");
    }
    for (By by : bys) {
      Objects.requireNonNull(by, "ByAll does not accept null By elements");
    }
    this.bys = bys.clone();
  }

  @Override
  public Locator locate(SearchContext context) {
    Locator union = bys[0].locate(context);
    for (int i = 1; i < bys.length; i++) {
      union = union.or(bys[i].locate(context));
    }
    return union;
  }

  @Override
  public String toString() {
    return "By.all(" + Arrays.toString(bys) + ")";
  }
}
