package io.github.byreshb.playwright.pagefactory.support;

import io.github.byreshb.playwright.pagefactory.By;
import io.github.byreshb.playwright.pagefactory.FindBy;
import io.github.byreshb.playwright.pagefactory.How;
import io.github.byreshb.playwright.pagefactory.PageFactoryFinder;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for the builders referenced by {@link PageFactoryFinder}. Subclass this to add support
 * for your own locating annotation; the helper methods here are the same ones Selenium's {@code
 * AbstractFindByBuilder} offers.
 */
public abstract class AbstractFindByBuilder {

  /** Turns the annotation found on {@code field} into a {@link By}. Never returns null. */
  public abstract By buildIt(Annotation annotation, Field field);

  /**
   * Validates a {@link FindBy}: {@code how} requires {@code using}, and at most one strategy may be
   * specified.
   *
   * @throws IllegalArgumentException when the annotation is inconsistent
   */
  protected void assertValidFindBy(FindBy findBy) {
    if (findBy.how() != How.UNSET && findBy.using().isEmpty()) {
      throw new IllegalArgumentException(
          "If you set the 'how' property, you must also set 'using'");
    }

    List<String> finders = new ArrayList<>();
    if (!findBy.using().isEmpty()) finders.add("how/using");
    if (!findBy.id().isEmpty()) finders.add("id");
    if (!findBy.name().isEmpty()) finders.add("name");
    if (!findBy.className().isEmpty()) finders.add("className");
    if (!findBy.css().isEmpty()) finders.add("css");
    if (!findBy.tagName().isEmpty()) finders.add("tagName");
    if (!findBy.linkText().isEmpty()) finders.add("linkText");
    if (!findBy.partialLinkText().isEmpty()) finders.add("partialLinkText");
    if (!findBy.xpath().isEmpty()) finders.add("xpath");
    if (!findBy.selector().isEmpty()) finders.add("selector");
    if (!findBy.testId().isEmpty()) finders.add("testId");
    if (!findBy.text().isEmpty()) finders.add("text");
    if (!findBy.label().isEmpty()) finders.add("label");
    if (!findBy.placeholder().isEmpty()) finders.add("placeholder");
    if (!findBy.altText().isEmpty()) finders.add("altText");
    if (!findBy.title().isEmpty()) finders.add("title");

    if (finders.size() > 1) {
      throw new IllegalArgumentException(
          "You must specify at most one location strategy. Number found: "
              + finders.size()
              + " ("
              + String.join(", ", finders)
              + ")");
    }
  }

  /** Builds a {@link By} from the "short" attributes ({@code id}, {@code css}, ...). */
  protected By buildByFromShortFindBy(FindBy findBy) {
    if (!findBy.id().isEmpty()) return By.id(findBy.id());
    if (!findBy.name().isEmpty()) return By.name(findBy.name());
    if (!findBy.className().isEmpty()) return By.className(findBy.className());
    if (!findBy.css().isEmpty()) return By.css(findBy.css());
    if (!findBy.tagName().isEmpty()) return By.tagName(findBy.tagName());
    if (!findBy.linkText().isEmpty()) return By.linkText(findBy.linkText());
    if (!findBy.partialLinkText().isEmpty()) return By.partialLinkText(findBy.partialLinkText());
    if (!findBy.xpath().isEmpty()) return By.xpath(findBy.xpath());
    if (!findBy.selector().isEmpty()) return By.selector(findBy.selector());
    if (!findBy.testId().isEmpty()) return By.testId(findBy.testId());
    if (!findBy.text().isEmpty()) return By.text(findBy.text());
    if (!findBy.label().isEmpty()) return By.label(findBy.label());
    if (!findBy.placeholder().isEmpty()) return By.placeholder(findBy.placeholder());
    if (!findBy.altText().isEmpty()) return By.altText(findBy.altText());
    if (!findBy.title().isEmpty()) return By.title(findBy.title());
    return null;
  }

  /** Builds a {@link By} from the "long" attributes ({@code how} + {@code using}). */
  protected By buildByFromLongFindBy(FindBy findBy) {
    return findBy.how().buildBy(findBy.using());
  }
}
