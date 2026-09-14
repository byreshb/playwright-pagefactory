package io.github.byreshb.playwright.pagefactory;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.nio.file.Path;
import java.nio.file.Paths;

/** Shares one headless Chromium per test class and gives each test a fresh {@link Page}. */
public abstract class BrowserTestBase {
  private static Playwright playwright;
  private static Browser browser;
  protected Page page;

  @BeforeAll
  static void launchBrowser() {
    playwright = Playwright.create();
    browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
  }

  @AfterAll
  static void closeBrowser() {
    if (browser != null) browser.close();
    if (playwright != null) playwright.close();
  }

  @BeforeEach
  void newPage() {
    page = browser.newPage();
  }

  @AfterEach
  void closePage() {
    if (page != null) page.close();
  }

  /** file:// URL of an HTML fixture under src/test/resources/pages. */
  protected static String fixtureUrl(String name) {
    Path path = Paths.get("src", "test", "resources", "pages", name).toAbsolutePath();
    return path.toUri().toString();
  }
}
