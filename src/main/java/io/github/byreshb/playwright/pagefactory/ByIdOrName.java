package io.github.byreshb.playwright.pagefactory;

import com.microsoft.playwright.Locator;

/**
 * Locates elements whose {@code id} or {@code name} attribute equals the given value, mirroring
 * Selenium's {@code org.openqa.selenium.support.ByIdOrName}.
 *
 * <p>This is the strategy {@link PageFactory} applies to {@link Locator} fields that carry no
 * {@link FindBy} annotation at all: the field's name is used as the id or name to look for.
 */
public class ByIdOrName extends By {
  private final String idOrName;
  private final String selector;

  public ByIdOrName(String idOrName) {
    requireNonEmpty(idOrName, "idOrName");
    this.idOrName = idOrName;
    String quoted = cssString(idOrName);
    this.selector = "css=[id=" + quoted + "], [name=" + quoted + "]";
  }

  @Override
  public Locator locate(SearchContext context) {
    return context.locator(selector);
  }

  @Override
  public String toString() {
    return "By.idOrName: " + idOrName;
  }
}
