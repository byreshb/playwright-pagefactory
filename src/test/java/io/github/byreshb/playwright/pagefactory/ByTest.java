package io.github.byreshb.playwright.pagefactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Test;

/** Pure unit tests for {@link By}: no browser, selectors are captured with a fake context. */
class ByTest {

  /** Records which SearchContext method a By used and with what argument. */
  static final class RecordingContext implements SearchContext {
    String method;
    String arg;

    private Locator record(String method, String arg) {
      this.method = method;
      this.arg = arg;
      return null;
    }

    @Override
    public Locator locator(String selector) {
      return record("locator", selector);
    }

    @Override
    public Locator getByTestId(String v) {
      return record("getByTestId", v);
    }

    @Override
    public Locator getByText(String v) {
      return record("getByText", v);
    }

    @Override
    public Locator getByLabel(String v) {
      return record("getByLabel", v);
    }

    @Override
    public Locator getByPlaceholder(String v) {
      return record("getByPlaceholder", v);
    }

    @Override
    public Locator getByAltText(String v) {
      return record("getByAltText", v);
    }

    @Override
    public Locator getByTitle(String v) {
      return record("getByTitle", v);
    }

    @Override
    public Locator getByRole(AriaRole role, String name) {
      return record("getByRole", role + (name == null ? "" : "," + name));
    }

    @Override
    public Object unwrap() {
      return this;
    }
  }

  private static String selectorOf(By by) {
    RecordingContext ctx = new RecordingContext();
    by.locate(ctx);
    assertThat(ctx.method).isEqualTo("locator");
    return ctx.arg;
  }

  private static String methodOf(By by) {
    RecordingContext ctx = new RecordingContext();
    by.locate(ctx);
    return ctx.method + "(" + ctx.arg + ")";
  }

  // ---- Selenium strategies -------------------------------------------------------------------

  @Test
  void idBecomesAttributeSelector() {
    assertThat(selectorOf(By.id("login"))).isEqualTo("css=[id=\"login\"]");
  }

  @Test
  void idWithSpecialCharactersIsQuotedNotEscapedAsCssIdentifier() {
    assertThat(selectorOf(By.id("form.user:name"))).isEqualTo("css=[id=\"form.user:name\"]");
  }

  @Test
  void attributeValuesEscapeQuotesAndBackslashes() {
    assertThat(selectorOf(By.name("say \"hi\" \\ bye")))
        .isEqualTo("css=[name=\"say \\\"hi\\\" \\\\ bye\"]");
  }

  @Test
  void nameBecomesAttributeSelector() {
    assertThat(selectorOf(By.name("q"))).isEqualTo("css=[name=\"q\"]");
  }

  @Test
  void classNameMatchesWholeClassToken() {
    assertThat(selectorOf(By.className("btn-primary"))).isEqualTo("css=[class~=\"btn-primary\"]");
  }

  @Test
  void compoundClassNamesAreRejectedLikeSelenium() {
    assertThatThrownBy(() -> By.className("btn primary"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Compound class names");
  }

  @Test
  void cssIsPrefixed() {
    assertThat(selectorOf(By.css("form > input[type=text]")))
        .isEqualTo("css=form > input[type=text]");
  }

  @Test
  void tagName() {
    assertThat(selectorOf(By.tagName("button"))).isEqualTo("css=button");
  }

  @Test
  void xpathIsPrefixed() {
    assertThat(selectorOf(By.xpath("//a[@href='/x']"))).isEqualTo("xpath=//a[@href='/x']");
  }

  @Test
  void linkTextIsExactAndCaseSensitive() {
    assertThat(selectorOf(By.linkText("Sign in")))
        .isEqualTo("xpath=//a[normalize-space(.)='Sign in']");
  }

  @Test
  void partialLinkTextUsesContains() {
    assertThat(selectorOf(By.partialLinkText("Forgot")))
        .isEqualTo("xpath=//a[contains(normalize-space(.), 'Forgot')]");
  }

  @Test
  void xpathLiteralWithSingleQuoteUsesDoubleQuotes() {
    assertThat(selectorOf(By.linkText("Don't click")))
        .isEqualTo("xpath=//a[normalize-space(.)=\"Don't click\"]");
  }

  @Test
  void xpathLiteralWithBothQuotesUsesConcat() {
    assertThat(selectorOf(By.linkText("Say \"don't\"")))
        .isEqualTo("xpath=//a[normalize-space(.)=concat('Say \"don', \"'\", 't\"')]");
  }

  @Test
  void idOrNameMatchesEither() {
    assertThat(selectorOf(By.idOrName("email"))).isEqualTo("css=[id=\"email\"], [name=\"email\"]");
  }

  // ---- Playwright strategies -----------------------------------------------------------------

  @Test
  void rawSelectorIsPassedThroughVerbatim() {
    assertThat(selectorOf(By.selector("text=Sign in >> nth=0"))).isEqualTo("text=Sign in >> nth=0");
  }

  @Test
  void getByStrategiesCallTheMatchingContextMethod() {
    assertThat(methodOf(By.testId("submit"))).isEqualTo("getByTestId(submit)");
    assertThat(methodOf(By.text("Sign in"))).isEqualTo("getByText(Sign in)");
    assertThat(methodOf(By.label("Email"))).isEqualTo("getByLabel(Email)");
    assertThat(methodOf(By.placeholder("Search"))).isEqualTo("getByPlaceholder(Search)");
    assertThat(methodOf(By.altText("Logo"))).isEqualTo("getByAltText(Logo)");
    assertThat(methodOf(By.title("Help"))).isEqualTo("getByTitle(Help)");
  }

  @Test
  void roleMapsToAriaRoleWithOptionalName() {
    assertThat(methodOf(By.role("button"))).isEqualTo("getByRole(BUTTON)");
    assertThat(methodOf(By.role("Button", "Sign in"))).isEqualTo("getByRole(BUTTON,Sign in)");
    assertThat(methodOf(By.role("menu-item"))).isEqualTo("getByRole(MENUITEM)");
    assertThat(methodOf(By.role("menu_item_checkbox"))).isEqualTo("getByRole(MENUITEMCHECKBOX)");
    assertThat(methodOf(By.role("link", ""))).isEqualTo("getByRole(LINK)");
  }

  @Test
  void unknownRoleIsRejectedWithTheValidRolesListed() {
    assertThatThrownBy(() -> By.role("clickable"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("clickable")
        .hasMessageContaining("BUTTON");
  }

  @Test
  void roleToStringAndEquality() {
    assertThat(By.role("button", "Save")).hasToString("By.role: button \"Save\"");
    assertThat(By.role("button")).hasToString("By.role: button");
    assertThat(By.role("button", "Save")).isEqualTo(By.role("button", "Save"));
    assertThat(By.role("button", "Save")).isNotEqualTo(By.role("button"));
  }

  // ---- How -----------------------------------------------------------------------------------

  @Test
  void howBuildsTheSameByAsTheStaticFactories() {
    assertThat(How.ID.buildBy("a")).isEqualTo(By.id("a"));
    assertThat(How.NAME.buildBy("a")).isEqualTo(By.name("a"));
    assertThat(How.CLASS_NAME.buildBy("a")).isEqualTo(By.className("a"));
    assertThat(How.CSS.buildBy("a")).isEqualTo(By.css("a"));
    assertThat(How.TAG_NAME.buildBy("a")).isEqualTo(By.tagName("a"));
    assertThat(How.LINK_TEXT.buildBy("a")).isEqualTo(By.linkText("a"));
    assertThat(How.PARTIAL_LINK_TEXT.buildBy("a")).isEqualTo(By.partialLinkText("a"));
    assertThat(How.XPATH.buildBy("a")).isEqualTo(By.xpath("a"));
    assertThat(How.ID_OR_NAME.buildBy("a")).isEqualTo(By.idOrName("a"));
    assertThat(How.SELECTOR.buildBy("a")).isEqualTo(By.selector("a"));
    assertThat(How.TEST_ID.buildBy("a")).isEqualTo(By.testId("a"));
    assertThat(How.TEXT.buildBy("a")).isEqualTo(By.text("a"));
    assertThat(How.LABEL.buildBy("a")).isEqualTo(By.label("a"));
    assertThat(How.PLACEHOLDER.buildBy("a")).isEqualTo(By.placeholder("a"));
    assertThat(How.ALT_TEXT.buildBy("a")).isEqualTo(By.altText("a"));
    assertThat(How.TITLE.buildBy("a")).isEqualTo(By.title("a"));
    assertThat(How.ROLE.buildBy("link")).isEqualTo(By.role("link"));
  }

  @Test
  void unsetBehavesLikeId() {
    assertThat(How.UNSET.buildBy("x")).isEqualTo(By.id("x"));
  }

  // ---- Object contract -----------------------------------------------------------------------

  @Test
  void equalityIsByStrategyAndValue() {
    assertThat(By.id("a")).isEqualTo(By.id("a")).hasSameHashCodeAs(By.id("a"));
    assertThat(By.id("a")).isNotEqualTo(By.name("a"));
    assertThat(By.id("a")).isNotEqualTo(By.id("b"));
    assertThat(By.chained(By.id("a"), By.css("b"))).isEqualTo(By.chained(By.id("a"), By.css("b")));
    assertThat(By.all(By.id("a"))).isNotEqualTo(By.chained(By.id("a")));
  }

  @Test
  void toStringLooksLikeSelenium() {
    assertThat(By.id("login")).hasToString("By.id: login");
    assertThat(By.css("a")).hasToString("By.cssSelector: a");
    assertThat(By.chained(By.id("a"), By.css("b")))
        .hasToString("By.chained([By.id: a, By.cssSelector: b])");
  }

  @Test
  void emptyAndNullValuesAreRejected() {
    assertThatThrownBy(() -> By.id("")).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> By.css(null)).isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> By.chained()).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> By.all((By) null)).isInstanceOf(NullPointerException.class);
  }
}
