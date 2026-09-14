# Migrating a Selenium PageFactory page object

Most page objects migrate by changing imports and two type names. This guide goes through what
changes and what does not.

## 1. Imports

| Selenium                                              | Playwright PageFactory                                   |
|-------------------------------------------------------|----------------------------------------------------------|
| `org.openqa.selenium.WebDriver`                       | `com.microsoft.playwright.Page`                          |
| `org.openqa.selenium.WebElement`                      | `com.microsoft.playwright.Locator`                       |
| `org.openqa.selenium.By`                              | `io.github.byreshb.playwright.pagefactory.By`            |
| `org.openqa.selenium.support.PageFactory`             | `io.github.byreshb.playwright.pagefactory.PageFactory`   |
| `org.openqa.selenium.support.FindBy` / `FindBys` / `FindAll` / `CacheLookup` / `How` | same simple names under `io.github.byreshb.playwright.pagefactory` |
| `org.openqa.selenium.support.pagefactory.*`           | `io.github.byreshb.playwright.pagefactory.support.*`     |

## 2. Types

Replace `WebDriver` with `Page` and `WebElement` with `Locator`. `List<WebElement>` becomes
`List<Locator>`. The annotations stay exactly as they are.

Before:

```java
public class LoginPage {
  private final WebDriver driver;
  @FindBy(id = "username") private WebElement username;
  @FindBy(how = How.NAME, using = "password") private WebElement password;
  @FindBy(css = "button[type=submit]") private WebElement submit;
  @FindBy(className = "error") private List<WebElement> errors;

  public LoginPage(WebDriver driver) {
    this.driver = driver;
    PageFactory.initElements(driver, this);
  }

  public void loginAs(String user, String pass) {
    username.sendKeys(user);
    password.sendKeys(pass);
    submit.click();
  }
}
```

After:

```java
public class LoginPage {
  private final Page page;
  @FindBy(id = "username") private Locator username;
  @FindBy(how = How.NAME, using = "password") private Locator password;
  @FindBy(css = "button[type=submit]") private Locator submit;
  @FindBy(className = "error") private List<Locator> errors;

  public LoginPage(Page page) {
    this.page = page;
    PageFactory.initElements(page, this);
  }

  public void loginAs(String user, String pass) {
    username.fill(user);
    password.fill(pass);
    submit.click();
  }
}
```

## 3. Method calls on elements

| `WebElement`                     | `Locator`                                             |
|----------------------------------|-------------------------------------------------------|
| `sendKeys(text)`                 | `fill(text)` (replaces) or `pressSequentially(text)`  |
| `clear()`                        | `clear()`                                             |
| `click()`                        | `click()`                                             |
| `getText()`                      | `textContent()` or `innerText()`                      |
| `getAttribute("value")`          | `inputValue()`                                        |
| `getAttribute(name)`             | `getAttribute(name)`                                  |
| `isDisplayed()`                  | `isVisible()`                                         |
| `isEnabled()`                    | `isEnabled()`                                         |
| `isSelected()`                   | `isChecked()`                                         |
| `findElement(By)`                | `locator(selector)` or `By.x(...).locate(locator)`    |
| `findElements(By)`               | `locator(selector).all()`                             |
| `new Select(el).selectByVisibleText(t)` | `selectOption(t)`                              |

## 4. Waiting

Delete your `WebDriverWait` / `ExpectedConditions` code around actions. Every Playwright action
auto-waits for the element to be attached, visible, stable and enabled. For assertions use
Playwright's `assertThat(locator).isVisible()` and friends, which retry until they pass.

## 5. Things that behave differently

- **No `NoSuchElementException` at lookup.** Accessing a field never fails; an action on a
  locator that matches nothing waits and then throws `TimeoutError`. Check presence with
  `locator.count()` or `locator.isVisible()`.
- **Strict mode.** A locator that matches several elements throws when you act on it. Selenium
  quietly used the first match. If you relied on that, add `.first()` in the page object or make
  the selector more specific.
- **`@CacheLookup` on a `Locator` field does nothing observable**, because locators already
  re-resolve. It still freezes `List<Locator>` fields like it froze `List<WebElement>`.
- **Static and final fields are skipped.** Selenium would overwrite them.
- **`By.className` rejects compound names**, same as Selenium; use `css = ".a.b"`.

## 6. New things you can use

Playwright's user-facing locators are available as annotation attributes:

```java
@FindBy(testId = "sign-in")          Locator signIn;
@FindBy(text = "Sign in")            Locator signInByText;
@FindBy(label = "Password")          Locator password;
@FindBy(placeholder = "Search")      Locator search;
@FindBy(selector = "text=OK >> nth=0") Locator raw;   // any Playwright selector
```

And page objects can be scoped to a component, frame or iframe:

```java
PageFactory.initElements(page.locator("#cart"), this);
PageFactory.initElements(page.frameLocator("#payment"), this);
```
