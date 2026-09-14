package io.github.byreshb.playwright.pagefactory.examples;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.byreshb.playwright.pagefactory.BrowserTestBase;
import org.junit.jupiter.api.Test;

/** Drives the example page objects against the HTML fixtures in src/test/resources/pages. */
class ExamplesTest extends BrowserTestBase {

  @Test
  void successfulLogin() {
    LoginPage login = LoginPage.open(page, fixtureUrl("login.html"));

    assertThat(login.title()).isEqualTo("Acme - Sign in");
    assertThat(login.navBar().activeLinkText()).isEqualTo("Home");
    assertThat(login.navBar().linkTexts()).containsExactly("Home", "Products", "About");

    login.rememberMe().loginAs("admin", "secret");

    assertThat(login.isRememberMeChecked()).isTrue();
    assertThat(login.message()).isEqualTo("Welcome, admin!");
    assertThat(login.isMessageError()).isFalse();
    assertThat(login.forgotPasswordHref()).isEqualTo("#forgot");
  }

  @Test
  void failedLoginShowsError() {
    LoginPage login = LoginPage.open(page, fixtureUrl("login.html"));
    login.loginAs("admin", "wrong");

    assertThat(login.message()).isEqualTo("Invalid credentials");
    assertThat(login.isMessageError()).isTrue();
  }

  @Test
  void productsPageListsAndComponents() {
    ProductsPage products = ProductsPage.open(page, fixtureUrl("products.html"));

    assertThat(products.heading()).isEqualTo("Products");
    assertThat(products.navBar().activeLinkText()).isEqualTo("Products");
    assertThat(products.products()).hasSize(3);
    assertThat(products.addButtonCount()).isEqualTo(3);
    assertThat(products.alertMessages())
        .containsExactly("Some items are out of stock", "Prices shown exclude tax");

    ProductCard monitor = products.product("Monitor");
    assertThat(monitor.price()).isEqualTo(199);

    monitor.addToCart();
    products.product("Mouse").addToCart();
    assertThat(products.cartCount()).isEqualTo(2);
  }

  @Test
  void searchFiltersTheListLive() {
    ProductsPage products = ProductsPage.open(page, fixtureUrl("products.html"));

    products.search("mo");
    assertThat(products.visibleProductNames()).containsExactly("Mouse", "Monitor");

    products.search("");
    assertThat(products.visibleProductNames()).containsExactly("Keyboard", "Mouse", "Monitor");
  }

  @Test
  void navigatingBetweenPages() {
    LoginPage login = LoginPage.open(page, fixtureUrl("login.html"));
    login.navBar().clickLink("Products");

    ProductsPage products = new ProductsPage(page);
    assertThat(products.title()).isEqualTo("Acme - Products");
    assertThat(products.products()).hasSize(3);
  }
}
