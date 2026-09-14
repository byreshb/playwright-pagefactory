package io.github.byreshb.playwright.pagefactory.examples;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.github.byreshb.playwright.pagefactory.FindBy;
import io.github.byreshb.playwright.pagefactory.PageFactory;

/**
 * Common base for page objects. Fields declared here are initialised too, because {@link
 * PageFactory} walks the whole class hierarchy, exactly like Selenium.
 */
public abstract class BasePage {
  protected final Page page;

  @FindBy(id = "header")
  protected Locator header;

  /** The navigation bar is a reusable component scoped to its own root element. */
  protected final NavBar navBar;

  protected BasePage(Page page) {
    this.page = page;
    PageFactory.initElements(page, this); // the classic Selenium idiom
    this.navBar = new NavBar(page.locator("#main-nav"));
  }

  public String title() {
    return page.title();
  }

  public NavBar navBar() {
    return navBar;
  }
}
