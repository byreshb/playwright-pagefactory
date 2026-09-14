# Playwright PageFactory (Java)

[![CI](https://github.com/byreshb/playwright-pagefactory/actions/workflows/ci.yml/badge.svg)](https://github.com/byreshb/playwright-pagefactory/actions/workflows/ci.yml)

A faithful port of Selenium's [`PageFactory`](https://github.com/SeleniumHQ/selenium/wiki/PageFactory)
to [Playwright for Java](https://playwright.dev/java/). Annotate `Locator` fields with `@FindBy`,
call `PageFactory.initElements(page, this)`, and write page objects exactly the way you did with
Selenium, with Playwright's lazy, auto-waiting `Locator` doing the work underneath.

```java
public class LoginPage {
  @FindBy(id = "username")
  Locator username;

  @FindBy(how = How.NAME, using = "password")
  Locator password;

  @FindBy(role = "button", roleName = "Sign in")
  Locator signIn;

  @FindBy(className = "error")
  List<Locator> errors;

  public LoginPage(Page page) {
    PageFactory.initElements(page, this);
  }

  public void loginAs(String user, String pass) {
    username.fill(user);
    password.fill(pass);
    signIn.click();
  }
}
```

Everything from Selenium is here with the same names and semantics: `@FindBy` (short and
`how`/`using` forms), `@FindBys` (chain), `@FindAll` (union), `@CacheLookup`, `How`, `By`,
`ByChained`, `ByAll`, `ByIdOrName`, un-annotated fields falling back to the field name as id or
name, superclass fields, and the `ElementLocatorFactory` / `FieldDecorator` /
`@PageFactoryFinder` extension points.

## Requirements

- Java 17 or newer
- Maven 3.6 or newer
- Playwright for Java 1.52 (pulled in as the only runtime dependency)

## Install and use it locally

The library is not on Maven Central yet (planned, see [docs/releasing.md](docs/releasing.md)).
Build it once on your machine and install it into your local
Maven repository (`~/.m2`), after which any project on that machine can depend on it.

```bash
git clone https://github.com/byreshb/playwright-pagefactory.git
cd playwright-pagefactory
mvn install -DskipTests      # drop -DskipTests to also run the browser tests (downloads Chromium)
```

Then add the dependency to your own project's `pom.xml`:

```xml
<dependency>
  <groupId>io.github.byreshb</groupId>
  <artifactId>playwright-pagefactory</artifactId>
  <version>1.0.0</version>
</dependency>
<dependency>
  <groupId>com.microsoft.playwright</groupId>
  <artifactId>playwright</artifactId>
  <version>1.52.0</version>
</dependency>
```

Gradle users:

```groovy
repositories { mavenLocal(); mavenCentral() }
dependencies {
  implementation 'io.github.byreshb:playwright-pagefactory:1.0.0'
  implementation 'com.microsoft.playwright:playwright:1.52.0'
}
```

Prefer a download? Every [GitHub Release](https://github.com/byreshb/playwright-pagefactory/releases)
has the main, sources and Javadoc jars attached.

No Maven or Gradle in the consuming project? `mvn package -DskipTests` produces
`target/playwright-pagefactory-1.0.0.jar` (plus `-sources.jar` and `-javadoc.jar`).
Put the main jar on your classpath next to the Playwright jars.

To share the build with teammates without a Maven repository, either commit the jar to their
project under `lib/` and use a `system` scoped dependency, or have everyone run the
`mvn install` step above.

## Quick start

1. Create a page object with `Locator` and `List<Locator>` fields and annotate them.
2. Initialise it, in either of the two Selenium styles:

```java
// Style A: the page object initialises itself
public LoginPage(Page page) {
  PageFactory.initElements(page, this);
}

// Style B: let the factory instantiate it (needs a Page constructor or a no-arg constructor)
LoginPage login = PageFactory.initElements(page, LoginPage.class);
```

3. Use the fields. They are ordinary Playwright `Locator`s: they auto-wait, re-resolve on every
   action and support the whole Playwright API (`click`, `fill`, `filter`, `or`, `nth`,
   `assertThat(locator)`, ...).

Full runnable examples live in
[`src/test/java/.../examples`](src/test/java/io/github/byreshb/playwright/pagefactory/examples):
a `BasePage`, a `LoginPage`, a `ProductsPage` with lists, chains and unions, and two
locator-scoped components (`NavBar`, `ProductCard`). `ExamplesTest` drives them against the HTML
in `src/test/resources/pages`.

## Annotation reference

### `@FindBy`

Exactly one strategy per annotation, in either the short or the long form.

| Short form               | Long form                             | Playwright selector produced                         |
|--------------------------|---------------------------------------|------------------------------------------------------|
| `id = "x"`               | `how = How.ID, using = "x"`           | `css=[id="x"]`                                       |
| `name = "x"`             | `How.NAME`                            | `css=[name="x"]`                                     |
| `className = "x"`        | `How.CLASS_NAME`                      | `css=[class~="x"]` (compound names rejected)         |
| `css = "x"`              | `How.CSS`                             | `css=x`                                              |
| `tagName = "x"`          | `How.TAG_NAME`                        | `css=x`                                              |
| `linkText = "x"`         | `How.LINK_TEXT`                       | `xpath=//a[normalize-space(.)='x']`                  |
| `partialLinkText = "x"`  | `How.PARTIAL_LINK_TEXT`               | `xpath=//a[contains(normalize-space(.), 'x')]`       |
| `xpath = "x"`            | `How.XPATH`                           | `xpath=x`                                            |
| (none)                   | `How.ID_OR_NAME`                      | `css=[id="x"], [name="x"]`                           |
| `selector = "x"`         | `How.SELECTOR`                        | `x`, verbatim (e.g. `text=Sign in >> nth=0`)         |
| `testId = "x"`           | `How.TEST_ID`                         | `getByTestId("x")`                                   |
| `text = "x"`             | `How.TEXT`                            | `getByText("x")`                                     |
| `label = "x"`            | `How.LABEL`                           | `getByLabel("x")`                                    |
| `placeholder = "x"`      | `How.PLACEHOLDER`                     | `getByPlaceholder("x")`                              |
| `altText = "x"`          | `How.ALT_TEXT`                        | `getByAltText("x")`                                  |
| `title = "x"`            | `How.TITLE`                           | `getByTitle("x")`                                    |
| `role = "button"`        | `How.ROLE`                            | `getByRole(AriaRole.BUTTON)`                         |
| `role = "button", roleName = "Save"` | (short form only)         | `getByRole(BUTTON, setName("Save"))`                 |

The first nine rows are the Selenium strategies; the rest are Playwright's own. `roleName` is the
accessible name for `role` (it is not called `name` because `name` is the HTML attribute, as in
Selenium). Role names are case-insensitive and accept dashes: `"menu-item"` is `AriaRole.MENUITEM`.

### `@FindBys` (chain)

Each `@FindBy` is resolved inside the previous one, like Selenium's `ByChained`:

```java
@FindBys({@FindBy(id = "checkout"), @FindBy(css = "button[type=submit]")})
Locator placeOrder;   // page.locator("[id=\"checkout\"]").locator("button[type=submit]")
```

### `@FindAll` (union)

Matches elements found by any of the `@FindBy`s, in document order, like Selenium's `ByAll`.
Implemented with `Locator.or`.

```java
@FindAll({@FindBy(className = "error"), @FindBy(className = "warning")})
List<Locator> alerts;
```

### `@CacheLookup`

For a `List<Locator>` field: take the list once, on first access, and keep reusing it. Without
the annotation the list is re-queried on every method call so it always mirrors the current DOM.
For a plain `Locator` field the annotation is accepted but changes nothing observable, because a
Playwright locator already re-resolves itself.

### No annotation

An un-annotated `Locator` field is looked up by its field name as an id or name, exactly like
Selenium (`Locator message;` means `[id="message"], [name="message"]`). An un-annotated
`List<Locator>` field is left alone. Static and final fields are never touched.

## Scoping to a component, frame or iframe

`initElements` accepts anything Playwright can locate from:

```java
PageFactory.initElements(page, this);                    // whole page
PageFactory.initElements(page.locator("#main-nav"), this); // relative to an element
PageFactory.initElements(page.frameLocator("#checkout"), this); // inside an iframe
PageFactory.initElements(page.frames().get(1), this);     // a Frame
```

Passing a `Locator` is how you build reusable components:

```java
public class NavBar {
  private final Locator root;              // final: PageFactory leaves it alone
  @FindBy(css = "a.active")
  Locator activeLink;

  @FindBy(tagName = "a")
  List<Locator> links;

  public NavBar(Locator root) {
    this.root = root;
    PageFactory.initElements(root, this);
  }
}

NavBar nav = new NavBar(page.locator("#main-nav"));
// or: PageFactory.initElements(page.locator("#main-nav"), NavBar.class)
```

## Using `By` directly

`By` works without annotations too, and is handy for dynamic lookups:

```java
Locator row = By.chained(By.id("orders"), By.text("#1042")).locate(page);
Locator any = By.all(By.testId("ok"), By.text("Confirm")).locate(dialog);
```

## Differences from Selenium worth knowing

- Fields are `com.microsoft.playwright.Locator`, not `WebElement`. A locator is lazy and
  auto-waiting; there is no `NoSuchElementException` at lookup time. Use `count()`,
  `isVisible()` or Playwright assertions to check presence.
- `Locator` fields hold real Playwright locators, not dynamic proxies. Proxies would break
  `Locator.or`, `Locator.and` and `setHas(...)`, which cast to Playwright's implementation class.
- `List<Locator>` fields are proxies (like Selenium's `List<WebElement>`) so they can re-query on
  each access. Calling any method on the list queries the page; `toString` does not.
- `By.text`, `By.label` and the other `getBy*` strategies follow Playwright's matching rules
  (case-insensitive substring by default). `linkText` and `partialLinkText` keep Selenium's exact,
  case-sensitive semantics via XPath.
- Static and final fields are skipped.

See [docs/migrating-from-selenium.md](docs/migrating-from-selenium.md) for a side-by-side guide
and [docs/design.md](docs/design.md) for how the pieces fit together and how to extend them.

## Building and testing

```bash
mvn test                # unit tests + browser tests (first run downloads Chromium)
mvn javadoc:javadoc     # API docs in target/site/apidocs
mvn install             # build, test, install into ~/.m2
mvn spotless:check      # verify formatting without changing anything (for CI)
mvn verify              # tests + coverage report in target/site/jacoco (fails under 85% lines)
```

### Debugging a failing browser test

Every browser test runs with Playwright tracing on. When a test fails, its trace and a full-page
screenshot are written to `target/playwright-artifacts/<TestClass>/<testMethod>/`; passing tests
leave nothing behind. Open a trace in the Playwright trace viewer:

```bash
mvn exec:java -Dexec.mainClass=com.microsoft.playwright.CLI \
  -Dexec.args="show-trace target/playwright-artifacts/ExamplesTest/successfulLogin/trace.zip"
```

or drag the zip onto https://trace.playwright.dev. To watch the browser instead of running
headless, add `-Dplaywright.headless=false`. The lifecycle lives in
`src/test/java/.../PlaywrightExtension.java`, a small JUnit 5 extension you can copy into your
own project.

### Continuous integration

Every push and pull request runs the GitHub Actions workflow in `.github/workflows/ci.yml`:
formatting check, full test suite in headless Chromium, and upload of the Surefire reports.
When a test fails, its Playwright traces and screenshots are attached to the run as an artifact.

### Releasing

1. Move the `Unreleased` notes in `CHANGELOG.md` under a new version heading and set that
   version in `pom.xml`.
2. Commit, then tag and push: `git tag v1.2.3 && git push origin v1.2.3`.
3. The release workflow in `.github/workflows/release.yml` checks the tag matches the pom,
   builds the jars, and publishes a GitHub Release with the changelog section as its notes.

Full steps, including the planned but not yet configured Maven Central publishing, are in
[docs/releasing.md](docs/releasing.md).

Formatting is automatic: every build runs [Spotless](https://github.com/diffplug/spotless) with
google-java-format (Google style, annotations on their own line) over `src/` before compiling, so
you never need to format by hand.

The browser tests launch headless Chromium through Playwright. On the first run Playwright
downloads the browsers it needs into `~/Library/Caches/ms-playwright` (macOS),
`~/.cache/ms-playwright` (Linux) or `%USERPROFILE%\AppData\Local\ms-playwright` (Windows).

## License

Apache License 2.0. The design, class names and documentation follow Selenium's
`org.openqa.selenium.support` package, which is also Apache 2.0 licensed; see [NOTICE](NOTICE).
