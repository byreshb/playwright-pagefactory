package io.github.byreshb.playwright.pagefactory.examples;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.github.byreshb.playwright.pagefactory.FindBy;
import io.github.byreshb.playwright.pagefactory.How;

/** The login page, showing every {@code @FindBy} flavour side by side. */
public class LoginPage extends BasePage {

  // Selenium short form
  @FindBy(id = "username")
  private Locator username;

  // Selenium long form
  @FindBy(how = How.NAME, using = "password")
  private Locator password;

  @FindBy(name = "remember")
  private Locator rememberMe;

  // Playwright getByRole with accessible name, the locator Playwright recommends first
  @FindBy(role = "button", roleName = "Sign in")
  private Locator signIn;

  @FindBy(partialLinkText = "Forgot")
  private Locator forgotPassword;

  private Locator message; // no annotation: id or name == "message"

  public LoginPage(Page page) {
    super(page);
  }

  public static LoginPage open(Page page, String url) {
    page.navigate(url);
    return new LoginPage(page);
  }

  public LoginPage loginAs(String user, String pass) {
    username.fill(user);
    password.fill(pass);
    signIn.click();
    return this;
  }

  public LoginPage rememberMe() {
    rememberMe.check();
    return this;
  }

  public boolean isRememberMeChecked() {
    return rememberMe.isChecked();
  }

  public String message() {
    return message.textContent();
  }

  public boolean isMessageError() {
    return "error".equals(message.getAttribute("class"));
  }

  public String forgotPasswordHref() {
    return forgotPassword.getAttribute("href");
  }
}
