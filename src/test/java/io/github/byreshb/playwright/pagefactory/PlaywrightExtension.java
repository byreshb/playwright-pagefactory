package io.github.byreshb.playwright.pagefactory;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit 5 extension that owns the Playwright lifecycle for a test class and captures diagnostics
 * when a test fails.
 *
 * <ul>
 *   <li>One headless Chromium per test class (set {@code -Dplaywright.headless=false} to watch).
 *   <li>A fresh {@link BrowserContext} and {@link Page} per test, with tracing (screenshots, DOM
 *       snapshots, sources) recording from the start.
 *   <li>On failure, a full-page screenshot and the trace zip are written to {@code
 *       target/playwright-artifacts/<TestClass>/<testMethod>/}. On success the trace is discarded.
 * </ul>
 *
 * Open a trace with {@code mvn exec:java -Dexec.mainClass=com.microsoft.playwright.CLI
 * -Dexec.args="show-trace target/playwright-artifacts/.../trace.zip"} or at
 * https://trace.playwright.dev.
 */
public final class PlaywrightExtension
    implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback {

  /** Where failure artifacts are written. */
  public static final Path ARTIFACT_ROOT = Paths.get("target", "playwright-artifacts");

  private Playwright playwright;
  private Browser browser;
  private BrowserContext context;
  private Page page;

  @Override
  public void beforeAll(ExtensionContext ctx) {
    playwright = Playwright.create();
    boolean headless = !"false".equalsIgnoreCase(System.getProperty("playwright.headless"));
    browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(headless));
  }

  @Override
  public void afterAll(ExtensionContext ctx) {
    if (browser != null) {
      browser.close();
    }
    if (playwright != null) {
      playwright.close();
    }
  }

  @Override
  public void beforeEach(ExtensionContext ctx) {
    context = browser.newContext();
    context
        .tracing()
        .start(
            new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(true)
                .setTitle(ctx.getDisplayName()));
    page = context.newPage();
  }

  @Override
  public void afterEach(ExtensionContext ctx) throws IOException {
    boolean failed = ctx.getExecutionException().isPresent();
    try {
      if (failed) {
        Path dir = artifactDir(ctx);
        Files.createDirectories(dir);
        if (!page.isClosed()) {
          page.screenshot(
              new Page.ScreenshotOptions()
                  .setPath(dir.resolve("screenshot.png"))
                  .setFullPage(true));
        }
        context.tracing().stop(new Tracing.StopOptions().setPath(dir.resolve("trace.zip")));
        System.err.println("Playwright artifacts for failed test saved to " + dir.toAbsolutePath());
      } else {
        context.tracing().stop();
      }
    } finally {
      context.close();
      context = null;
      page = null;
    }
  }

  /** The page for the currently running test. */
  public Page page() {
    return page;
  }

  /** The browser context for the currently running test. */
  public BrowserContext context() {
    return context;
  }

  private static Path artifactDir(ExtensionContext ctx) {
    String testClass = ctx.getRequiredTestClass().getSimpleName();
    String testMethod = ctx.getRequiredTestMethod().getName().replaceAll("[^A-Za-z0-9_.-]", "_");
    return ARTIFACT_ROOT.resolve(testClass).resolve(testMethod);
  }
}
