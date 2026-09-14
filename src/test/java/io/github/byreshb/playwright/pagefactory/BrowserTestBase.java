package io.github.byreshb.playwright.pagefactory;

import com.microsoft.playwright.Page;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * Base class for browser tests. {@link PlaywrightExtension} provides one headless Chromium per
 * class, a fresh {@link Page} per test, and a trace plus screenshot for every failing test.
 */
public abstract class BrowserTestBase {

  @RegisterExtension static final PlaywrightExtension playwright = new PlaywrightExtension();

  protected Page page;

  @BeforeEach
  void takePage() {
    page = playwright.page();
  }

  /** file:// URL of an HTML fixture under src/test/resources/pages. */
  protected static String fixtureUrl(String name) {
    Path path = Paths.get("src", "test", "resources", "pages", name).toAbsolutePath();
    return path.toUri().toString();
  }
}
