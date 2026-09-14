package io.github.byreshb.playwright.pagefactory.examples;

import com.microsoft.playwright.Locator;
import io.github.byreshb.playwright.pagefactory.FindBy;
import io.github.byreshb.playwright.pagefactory.PageFactory;

/** One product row. Built from the row's locator, so all fields are scoped to that row. */
public class ProductCard {
  @FindBy(className = "name")
  private Locator name;

  @FindBy(className = "price")
  private Locator price;

  @FindBy(text = "Add to cart")
  private Locator addToCart;

  public ProductCard(Locator root) {
    PageFactory.initElements(root, this);
  }

  public String name() {
    return name.textContent();
  }

  public int price() {
    return Integer.parseInt(price.textContent());
  }

  public void addToCart() {
    addToCart.click();
  }
}
