package io.github.byreshb.playwright.pagefactory;

import io.github.byreshb.playwright.pagefactory.support.AbstractFindByBuilder;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

/**
 * Chains several {@link FindBy}s: each one is resolved inside the elements matched by the previous
 * one. Equivalent to Selenium's {@code @FindBys}, backed by {@link ByChained}.
 *
 * <pre><code>
 * &#64;FindBys({&#64;FindBy(id = "checkout"), &#64;FindBy(css = "button[type=submit]")})
 * Locator placeOrder;   // == page.locator("[id=checkout]").locator("button[type=submit]")
 * </code></pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
@PageFactoryFinder(FindBys.FindByBuilder.class)
public @interface FindBys {
  /** The chain, outermost first. */
  FindBy[] value();

  /** Converts a {@link FindBys} into a {@link ByChained}. */
  class FindByBuilder extends AbstractFindByBuilder {
    @Override
    public By buildIt(Annotation annotation, Field field) {
      FindBy[] findBys = ((FindBys) annotation).value();
      if (findBys.length == 0) {
        throw new IllegalArgumentException(
            "@FindBys on '" + field.getName() + "' must contain at least one @FindBy");
      }
      By[] bys = new By[findBys.length];
      for (int i = 0; i < findBys.length; i++) {
        bys[i] = new FindBy.FindByBuilder().buildIt(findBys[i], field);
      }
      return new ByChained(bys);
    }
  }
}
