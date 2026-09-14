package io.github.byreshb.playwright.pagefactory;

import com.microsoft.playwright.Locator;
import io.github.byreshb.playwright.pagefactory.support.AbstractFindByBuilder;
import io.github.byreshb.playwright.pagefactory.support.Annotations;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Tests annotation parsing ({@link Annotations}) without a browser. */
class AnnotationsTest {

  // A custom annotation, to prove the @PageFactoryFinder extension point works.
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.FIELD)
  @PageFactoryFinder(ByRole.Builder.class)
  @interface ByRole {
    String value();

    class Builder extends AbstractFindByBuilder {
      @Override
      public By buildIt(Annotation annotation, Field field) {
        return By.css("[role=" + ((ByRole) annotation).value() + "]");
      }
    }
  }

  @SuppressWarnings("unused")
  static class Holder {
    @FindBy(id = "user") Locator shortForm;
    @FindBy(how = How.CSS, using = ".btn") Locator longForm;
    @FindBy(how = How.XPATH) Locator howWithoutUsing;
    @FindBy(id = "a", css = "b") Locator twoStrategies;
    @FindBy(placeholder = "Search") Locator playwrightShortForm;
    Locator noAnnotation;
    @CacheLookup @FindBy(id = "cached") Locator cached;
    @FindBys({@FindBy(id = "form"), @FindBy(tagName = "input")}) Locator chained;
    @FindAll({@FindBy(className = "a"), @FindBy(className = "b")}) Locator union;
    @FindBy(id = "x") @FindBys({@FindBy(id = "y")}) Locator findByAndFindBys;
    @FindBy(id = "x") @FindAll({@FindBy(id = "y")}) Locator findByAndFindAll;
    @FindBys({@FindBy(id = "x")}) @FindAll({@FindBy(id = "y")}) Locator findBysAndFindAll;
    @ByRole("dialog") Locator custom;
  }

  private static Annotations annotationsOf(String fieldName) throws NoSuchFieldException {
    return new Annotations(Holder.class.getDeclaredField(fieldName));
  }

  @Test
  void shortForm() throws Exception {
    assertThat(annotationsOf("shortForm").buildBy()).isEqualTo(By.id("user"));
  }

  @Test
  void longForm() throws Exception {
    assertThat(annotationsOf("longForm").buildBy()).isEqualTo(By.css(".btn"));
  }

  @Test
  void playwrightShortForm() throws Exception {
    assertThat(annotationsOf("playwrightShortForm").buildBy()).isEqualTo(By.placeholder("Search"));
  }

  @Test
  void howRequiresUsing() throws Exception {
    assertThatThrownBy(() -> annotationsOf("howWithoutUsing").buildBy())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("'using'");
  }

  @Test
  void onlyOneStrategyAllowed() throws Exception {
    assertThatThrownBy(() -> annotationsOf("twoStrategies").buildBy())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("at most one location strategy")
        .hasMessageContaining("id, css");
  }

  @Test
  void noAnnotationFallsBackToIdOrNameOfFieldName() throws Exception {
    assertThat(annotationsOf("noAnnotation").buildBy()).isEqualTo(By.idOrName("noAnnotation"));
  }

  @Test
  void cacheLookupIsDetected() throws Exception {
    assertThat(annotationsOf("cached").isLookupCached()).isTrue();
    assertThat(annotationsOf("shortForm").isLookupCached()).isFalse();
  }

  @Test
  void findBysBuildsByChained() throws Exception {
    assertThat(annotationsOf("chained").buildBy())
        .isEqualTo(By.chained(By.id("form"), By.tagName("input")));
  }

  @Test
  void findAllBuildsByAll() throws Exception {
    assertThat(annotationsOf("union").buildBy())
        .isEqualTo(By.all(By.className("a"), By.className("b")));
  }

  @Test
  void conflictingAnnotationsAreRejected() throws Exception {
    assertThatThrownBy(() -> annotationsOf("findByAndFindBys").buildBy())
        .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("@FindBys");
    assertThatThrownBy(() -> annotationsOf("findByAndFindAll").buildBy())
        .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("@FindAll");
    assertThatThrownBy(() -> annotationsOf("findBysAndFindAll").buildBy())
        .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("@FindAll");
  }

  @Test
  void customAnnotationsViaPageFactoryFinder() throws Exception {
    assertThat(annotationsOf("custom").buildBy()).isEqualTo(By.css("[role=dialog]"));
  }
}
