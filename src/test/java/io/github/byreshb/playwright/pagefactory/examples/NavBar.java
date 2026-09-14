package io.github.byreshb.playwright.pagefactory.examples;

import com.microsoft.playwright.Locator;
import io.github.byreshb.playwright.pagefactory.FindBy;
import io.github.byreshb.playwright.pagefactory.PageFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A component: its fields are resolved <em>relative to</em> the root locator passed to the
 * constructor, so the same class works for every nav bar on the site.
 */
public class NavBar {
  private final Locator root;

  @FindBy(css = "a.active")
  private Locator activeLink;

  @FindBy(tagName = "a")
  private List<Locator> links;

  public NavBar(Locator root) {
    this.root = root;
    PageFactory.initElements(root, this);
  }

  public String activeLinkText() {
    return activeLink.textContent().trim();
  }

  public List<String> linkTexts() {
    return links.stream().map(l -> l.textContent().trim()).collect(Collectors.toList());
  }

  public void clickLink(String text) {
    root.getByText(text).click();
  }
}
