package io.github.byreshb.playwright.pagefactory.examples;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.github.byreshb.playwright.pagefactory.CacheLookup;
import io.github.byreshb.playwright.pagefactory.FindAll;
import io.github.byreshb.playwright.pagefactory.FindBy;
import io.github.byreshb.playwright.pagefactory.FindBys;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/** The products page: lists, chained and union lookups, caching, and sub-components. */
public class ProductsPage extends BasePage {

  @CacheLookup
  @FindBy(tagName = "h1")
  private Locator heading;

  @FindBy(placeholder = "Search products")
  private Locator search;

  /** Re-queried on every access, so it always reflects what is on the page. */
  @FindBy(testId = "product")
  private List<Locator> products;

  /**
   * @FindBys == chain: the buttons *inside* #product-list.
   */
  @FindBys({@FindBy(id = "product-list"), @FindBy(className = "add")})
  private List<Locator> addButtons;

  /**
   * @FindAll == union: every error *or* warning alert.
   */
  @FindAll({@FindBy(className = "error"), @FindBy(className = "warning")})
  private List<Locator> alerts;

  @FindBy(id = "cart-count")
  private Locator cartCount;

  public ProductsPage(Page page) {
    super(page);
  }

  public static ProductsPage open(Page page, String url) {
    page.navigate(url);
    return new ProductsPage(page);
  }

  public String heading() {
    return heading.textContent();
  }

  public List<ProductCard> products() {
    return products.stream().map(ProductCard::new).collect(Collectors.toList());
  }

  public List<String> visibleProductNames() {
    return products.stream()
        .filter(Locator::isVisible)
        .map(p -> p.locator(".name").textContent())
        .collect(Collectors.toList());
  }

  public ProductCard product(String name) {
    return products().stream()
        .filter(p -> p.name().equals(name))
        .findFirst()
        .orElseThrow(() -> new NoSuchElementException("No product named " + name));
  }

  public ProductsPage search(String term) {
    search.fill(term);
    return this;
  }

  public int addButtonCount() {
    return addButtons.size();
  }

  public List<String> alertMessages() {
    return alerts.stream().map(Locator::textContent).collect(Collectors.toList());
  }

  public int cartCount() {
    return Integer.parseInt(cartCount.textContent());
  }
}
