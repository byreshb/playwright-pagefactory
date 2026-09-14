package io.github.byreshb.playwright.pagefactory;

import io.github.byreshb.playwright.pagefactory.support.AbstractFindByBuilder;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

/**
 * Matches elements found by <em>any</em> of the given {@link FindBy}s (their union). Equivalent to
 * Selenium's {@code @FindAll}, backed by {@link ByAll}.
 *
 * <pre><code>
 * &#64;FindAll({&#64;FindBy(className = "error"), &#64;FindBy(className = "warning")})
 * List&lt;Locator&gt; messages;
 * </code></pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
@PageFactoryFinder(FindAll.FindByBuilder.class)
public @interface FindAll {
  /** The alternatives. */
  FindBy[] value();

  /** Converts a {@link FindAll} into a {@link ByAll}. */
  class FindByBuilder extends AbstractFindByBuilder {
    @Override
    public By buildIt(Annotation annotation, Field field) {
      FindBy[] findBys = ((FindAll) annotation).value();
      if (findBys.length == 0) {
        throw new IllegalArgumentException(
            "@FindAll on '" + field.getName() + "' must contain at least one @FindBy");
      }
      By[] bys = new By[findBys.length];
      for (int i = 0; i < findBys.length; i++) {
        bys[i] = new FindBy.FindByBuilder().buildIt(findBys[i], field);
      }
      return new ByAll(bys);
    }
  }
}
