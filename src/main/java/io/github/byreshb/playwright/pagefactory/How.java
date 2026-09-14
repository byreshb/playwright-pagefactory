package io.github.byreshb.playwright.pagefactory;

/**
 * The lookup strategies available to {@link FindBy#how()}, mirroring Selenium's {@code How}.
 * Each constant knows how to turn a raw value into a {@link By}.
 */
public enum How {
  /** {@link By#className(String)} */
  CLASS_NAME {
    @Override
    public By buildBy(String value) {
      return By.className(value);
    }
  },
  /** {@link By#css(String)} */
  CSS {
    @Override
    public By buildBy(String value) {
      return By.css(value);
    }
  },
  /** {@link By#id(String)} */
  ID {
    @Override
    public By buildBy(String value) {
      return By.id(value);
    }
  },
  /** {@link By#idOrName(String)} */
  ID_OR_NAME {
    @Override
    public By buildBy(String value) {
      return By.idOrName(value);
    }
  },
  /** {@link By#linkText(String)} */
  LINK_TEXT {
    @Override
    public By buildBy(String value) {
      return By.linkText(value);
    }
  },
  /** {@link By#name(String)} */
  NAME {
    @Override
    public By buildBy(String value) {
      return By.name(value);
    }
  },
  /** {@link By#partialLinkText(String)} */
  PARTIAL_LINK_TEXT {
    @Override
    public By buildBy(String value) {
      return By.partialLinkText(value);
    }
  },
  /** {@link By#tagName(String)} */
  TAG_NAME {
    @Override
    public By buildBy(String value) {
      return By.tagName(value);
    }
  },
  /** {@link By#xpath(String)} */
  XPATH {
    @Override
    public By buildBy(String value) {
      return By.xpath(value);
    }
  },

  // ---- Playwright-specific strategies -------------------------------------------------------

  /** {@link By#selector(String)}: a raw Playwright selector such as {@code "text=Sign in"}. */
  SELECTOR {
    @Override
    public By buildBy(String value) {
      return By.selector(value);
    }
  },
  /** {@link By#testId(String)} */
  TEST_ID {
    @Override
    public By buildBy(String value) {
      return By.testId(value);
    }
  },
  /** {@link By#text(String)} */
  TEXT {
    @Override
    public By buildBy(String value) {
      return By.text(value);
    }
  },
  /** {@link By#label(String)} */
  LABEL {
    @Override
    public By buildBy(String value) {
      return By.label(value);
    }
  },
  /** {@link By#placeholder(String)} */
  PLACEHOLDER {
    @Override
    public By buildBy(String value) {
      return By.placeholder(value);
    }
  },
  /** {@link By#altText(String)} */
  ALT_TEXT {
    @Override
    public By buildBy(String value) {
      return By.altText(value);
    }
  },
  /** {@link By#title(String)} */
  TITLE {
    @Override
    public By buildBy(String value) {
      return By.title(value);
    }
  },

  /** The default when {@code how} is not specified; behaves like {@link #ID}. */
  UNSET {
    @Override
    public By buildBy(String value) {
      return ID.buildBy(value);
    }
  };

  /** Builds the {@link By} for this strategy and the given value. */
  public abstract By buildBy(String value);
}
