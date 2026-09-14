package io.github.byreshb.playwright.pagefactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.github.byreshb.playwright.pagefactory.support.DefaultElementLocatorFactory;
import io.github.byreshb.playwright.pagefactory.support.DefaultFieldDecorator;
import io.github.byreshb.playwright.pagefactory.support.ElementLocatorFactory;
import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/** End-to-end tests of {@link PageFactory} against a real (headless) browser. */
class PageFactoryTest extends BrowserTestBase {

  private static final String HTML =
      String.join(
          "\n",
          "<h1>Title</h1>",
          "<form id='form'>",
          "  <input id='username' name='user' placeholder='Your name'>",
          "  <label for='pw'>Password</label><input id='pw' type='password'>",
          "  <button type='submit' data-testid='submit'>Log in</button>",
          "</form>",
          "<ul id='items'><li class='item'>a</li><li class='item'>b</li><li"
              + " class='item'>c</li></ul>",
          "<div class='error'>e1</div><div class='warning'>w1</div><div class='error'>e2</div>",
          "<a href='#x'>Forgot your password?</a>",
          "<div class='item'>outside the list</div>",
          "<iframe id='frame' srcdoc=\"<button id='inner'>Inside</button>\"></iframe>");

  @SuppressWarnings("unused")
  static class BasePage {
    @FindBy(tagName = "h1")
    Locator heading;
  }

  @SuppressWarnings("unused")
  static class TestPage extends BasePage {
    static Locator staticField; // must be ignored
    final Locator finalField = null; // must be ignored (constructor-owned)
    String notALocator = "untouched"; // must be ignored
    List<Locator> unannotatedList; // must stay null
    List<String> stringList; // must stay null

    @FindBy(id = "username")
    Locator username;

    @FindBy(how = How.NAME, using = "user")
    Locator usernameByName;

    @FindBy(placeholder = "Your name")
    Locator usernameByPlaceholder;

    @FindBy(label = "Password")
    Locator password;

    @FindBy(testId = "submit")
    Locator submit;

    @FindBy(text = "Log in")
    Locator submitByText;

    @FindBy(partialLinkText = "Forgot")
    Locator forgot;

    @FindBy(linkText = "Forgot your password?")
    Locator forgotExact;

    @FindBy(selector = "css=#items >> nth=0")
    Locator rawSelector;

    Locator pw; // no annotation -> id or name "pw"

    @FindBy(className = "item")
    List<Locator> allItems;

    @FindBys({@FindBy(id = "items"), @FindBy(className = "item")})
    List<Locator> listItems;

    @FindAll({@FindBy(className = "error"), @FindBy(className = "warning")})
    List<Locator> alerts;

    @CacheLookup
    @FindBy(className = "item")
    List<Locator> cachedItems;

    @CacheLookup
    @FindBy(id = "username")
    Locator cachedUsername;
  }

  private TestPage init() {
    page.setContent(HTML);
    TestPage p = new TestPage();
    PageFactory.initElements(page, p);
    return p;
  }

  @Test
  void locatorFieldsResolveAgainstThePage() {
    TestPage p = init();

    p.username.fill("alice");
    assertThat(p.usernameByName.inputValue()).isEqualTo("alice");
    assertThat(p.usernameByPlaceholder.inputValue()).isEqualTo("alice");
    assertThat(p.password.getAttribute("type")).isEqualTo("password");
    assertThat(p.pw.getAttribute("type")).isEqualTo("password");
    assertThat(p.submit.textContent()).isEqualTo("Log in");
    assertThat(p.submitByText.getAttribute("data-testid")).isEqualTo("submit");
    assertThat(p.forgot.getAttribute("href")).isEqualTo("#x");
    assertThat(p.forgotExact.getAttribute("href")).isEqualTo("#x");
    assertThat(p.rawSelector.getAttribute("id")).isEqualTo("items");
  }

  @Test
  void superclassFieldsAreInitialised() {
    TestPage p = init();
    assertThat(p.heading.textContent()).isEqualTo("Title");
  }

  @Test
  void nonLocatorAndUnannotatedListFieldsAreLeftAlone() {
    TestPage p = init();
    assertThat(TestPage.staticField).isNull();
    assertThat(p.finalField).isNull();
    assertThat(p.notALocator).isEqualTo("untouched");
    assertThat(p.unannotatedList).isNull();
    assertThat(p.stringList).isNull();
  }

  @Test
  void locatorFieldsAreRealPlaywrightLocatorsNotProxies() {
    TestPage p = init();
    // Locator.or/and cast to the internal implementation, which a Proxy would break.
    assertThat(p.username.or(p.password).count()).isEqualTo(2);
    assertThat(p.heading.and(page.locator("h1")).count()).isEqualTo(1);
  }

  @Test
  void locatorFieldsAreLazyAndSurviveNavigation() {
    TestPage p = init();
    page.setContent("<input id='username' value='after reload'>");
    assertThat(p.username.inputValue()).isEqualTo("after reload");
  }

  @Test
  void listFieldsReflectTheCurrentDomOnEveryAccess() {
    TestPage p = init();
    assertThat(p.allItems).hasSize(4);

    page.evaluate(
        "document.getElementById('items').insertAdjacentHTML('beforeend', \"<li"
            + " class='item'>d</li>\")");
    assertThat(p.allItems).hasSize(5);
    assertThat(p.allItems.get(3).textContent()).isEqualTo("d");
  }

  @Test
  void cacheLookupFreezesListSnapshot() {
    TestPage p = init();
    assertThat(p.cachedItems).hasSize(4);
    page.evaluate(
        "document.getElementById('items').insertAdjacentHTML('beforeend', \"<li"
            + " class='item'>d</li>\")");
    assertThat(p.cachedItems).hasSize(4);
    assertThat(p.allItems).hasSize(5);
  }

  @Test
  void cacheLookupOnLocatorFieldStillWorks() {
    TestPage p = init();
    p.cachedUsername.fill("bob");
    assertThat(p.username.inputValue()).isEqualTo("bob");
  }

  @Test
  void listProxyToStringDoesNotQueryThePage() {
    TestPage p = init();
    page.close(); // any query would now throw
    assertThat(p.allItems.toString()).startsWith("Proxy list for: DefaultElementLocator");
  }

  @Test
  void findBysChainsInsideThePreviousMatch() {
    TestPage p = init();
    // 4 elements have class 'item' but only 3 are inside #items.
    assertThat(p.listItems).hasSize(3);
    assertThat(p.listItems.stream().map(Locator::textContent).collect(Collectors.toList()))
        .containsExactly("a", "b", "c");
  }

  @Test
  void findAllUnionsInDocumentOrder() {
    TestPage p = init();
    assertThat(p.alerts.stream().map(Locator::textContent).collect(Collectors.toList()))
        .containsExactly("e1", "w1", "e2");
  }

  @Test
  void listElementsAreUsableLocators() {
    TestPage p = init();
    p.listItems.get(1).click();
    assertThat(p.listItems.get(1).textContent()).isEqualTo("b");
  }

  // ---- instantiation -------------------------------------------------------------------------

  static class PageCtorPage {
    final Page page;

    @FindBy(id = "username")
    Locator username;

    PageCtorPage(Page page) {
      this.page = page;
    }
  }

  static class NoArgPage {
    @FindBy(id = "username")
    Locator username;
  }

  static class Component {
    final Locator root;

    @FindBy(className = "item")
    List<Locator> items;

    Component(Locator root) {
      this.root = root;
    }
  }

  static class ContextPage {
    final SearchContext ctx;

    @FindBy(tagName = "button")
    Locator button;

    ContextPage(SearchContext ctx) {
      this.ctx = ctx;
    }
  }

  static class Uninstantiable {
    Uninstantiable(String needsAString) {}
  }

  @Test
  void initElementsWithClassUsesPageConstructor() {
    page.setContent(HTML);
    PageCtorPage p = PageFactory.initElements(page, PageCtorPage.class);
    assertThat(p.page).isSameAs(page);
    assertThat(p.username.getAttribute("name")).isEqualTo("user");
  }

  @Test
  void initElementsWithClassFallsBackToNoArgConstructor() {
    page.setContent(HTML);
    NoArgPage p = PageFactory.initElements(page, NoArgPage.class);
    assertThat(p.username.getAttribute("name")).isEqualTo("user");
  }

  @Test
  void initElementsScopedToALocatorUsesLocatorConstructor() {
    page.setContent(HTML);
    Component c = PageFactory.initElements(page.locator("#items"), Component.class);
    assertThat(c.root).isNotNull();
    assertThat(c.items).hasSize(3);
  }

  @Test
  void initElementsWithSearchContextConstructor() {
    page.setContent(HTML);
    FrameLocator frame = page.frameLocator("#frame");
    ContextPage p = PageFactory.initElements(SearchContext.of(frame), ContextPage.class);
    assertThat(p.ctx.unwrap()).isSameAs(frame);
    assertThat(p.button.textContent()).isEqualTo("Inside");
  }

  @Test
  void initElementsInsideAFrameLocator() {
    page.setContent(HTML);
    ContextPage p = new ContextPage(null);
    PageFactory.initElements(page.frameLocator("#frame"), p);
    assertThat(p.button.getAttribute("id")).isEqualTo("inner");
  }

  @Test
  void initElementsAgainstAFrame() {
    page.setContent(HTML);
    ContextPage p = new ContextPage(null);
    PageFactory.initElements(page.frames().get(1), p);
    assertThat(p.button.getAttribute("id")).isEqualTo("inner");
  }

  @Test
  void unsupportedConstructorIsReportedClearly() {
    assertThatThrownBy(() -> PageFactory.initElements(page, Uninstantiable.class))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Uninstantiable")
        .hasMessageContaining("constructor");
  }

  // ---- validation ----------------------------------------------------------------------------

  @SuppressWarnings("unused")
  static class BadPage {
    @FindBy(id = "a", css = "b")
    Locator conflicting;
  }

  @Test
  void invalidAnnotationsFailFastAtInitTime() {
    assertThatThrownBy(() -> PageFactory.initElements(page, new BadPage()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("at most one location strategy");
  }

  // ---- extension points ----------------------------------------------------------------------

  @Test
  void customElementLocatorFactoryCanSkipFields() {
    page.setContent(HTML);
    ElementLocatorFactory defaultFactory = new DefaultElementLocatorFactory(SearchContext.of(page));
    ElementLocatorFactory onlyUsername =
        (Field f) -> f.getName().equals("username") ? defaultFactory.createLocator(f) : null;

    TestPage p = new TestPage();
    PageFactory.initElements(onlyUsername, p);
    assertThat(p.username).isNotNull();
    assertThat(p.password).isNull();
    assertThat(p.allItems).isNull();
  }

  @Test
  void customFieldDecoratorCanOverrideLocatorValues() {
    page.setContent(HTML);
    DefaultFieldDecorator decorator =
        new DefaultFieldDecorator(new DefaultElementLocatorFactory(SearchContext.of(page))) {
          @Override
          protected Locator proxyForLocator(
              ClassLoader loader,
              io.github.byreshb.playwright.pagefactory.support.ElementLocator locator) {
            return locator.findLocator().first();
          }
        };
    TestPage p = new TestPage();
    PageFactory.initElements(decorator, p);
    assertThat(p.heading.textContent()).isEqualTo("Title");
  }
}
