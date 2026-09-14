package io.github.byreshb.playwright.pagefactory;

import io.github.byreshb.playwright.pagefactory.support.AbstractFindByBuilder;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-annotation that makes a custom annotation usable by {@link PageFactory}. Annotate your
 * annotation with {@code @PageFactoryFinder(MyBuilder.class)}, where {@code MyBuilder} extends
 * {@link AbstractFindByBuilder} and knows how to turn your annotation into a {@link By}.
 *
 * <p>{@link FindBy}, {@link FindBys} and {@link FindAll} are all implemented this way, so custom
 * annotations are first-class citizens.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface PageFactoryFinder {
  /** The builder that converts the annotated annotation into a {@link By}. */
  Class<? extends AbstractFindByBuilder> value();
}
