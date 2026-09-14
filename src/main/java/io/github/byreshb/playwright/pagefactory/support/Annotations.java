package io.github.byreshb.playwright.pagefactory.support;

import io.github.byreshb.playwright.pagefactory.By;
import io.github.byreshb.playwright.pagefactory.CacheLookup;
import io.github.byreshb.playwright.pagefactory.FindAll;
import io.github.byreshb.playwright.pagefactory.FindBy;
import io.github.byreshb.playwright.pagefactory.FindBys;
import io.github.byreshb.playwright.pagefactory.PageFactoryFinder;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * Reads the {@link FindBy}, {@link FindBys}, {@link FindAll}, {@link CacheLookup} and any custom
 * {@link PageFactoryFinder}-annotated annotations from a field. Mirrors Selenium's {@code
 * Annotations}.
 */
public class Annotations extends AbstractAnnotations {
  private final Field field;

  /**
   * @param field a field on a page object
   */
  public Annotations(Field field) {
    this.field = field;
  }

  /** {@inheritDoc} @return true when the field carries {@link CacheLookup} */
  @Override
  public boolean isLookupCached() {
    return field.getAnnotation(CacheLookup.class) != null;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Looks for an annotation meta-annotated with {@link PageFactoryFinder} ({@link FindBy},
   * {@link FindBys}, {@link FindAll} or a custom one). If none is present the field name is used as
   * an id or name, exactly like Selenium.
   *
   * @throws IllegalArgumentException when conflicting annotations are present on the field
   */
  @Override
  public By buildBy() {
    assertValidAnnotations();

    for (Annotation annotation : field.getDeclaredAnnotations()) {
      PageFactoryFinder finder = annotation.annotationType().getAnnotation(PageFactoryFinder.class);
      if (finder == null) {
        continue;
      }
      AbstractFindByBuilder builder;
      try {
        builder = finder.value().getDeclaredConstructor().newInstance();
      } catch (ReflectiveOperationException e) {
        throw new IllegalStateException(
            "Cannot instantiate "
                + finder.value().getName()
                + " for @"
                + annotation.annotationType().getSimpleName()
                + " on field '"
                + field.getName()
                + "'; it needs an accessible no-arg constructor",
            e);
      }
      return builder.buildIt(annotation, field);
    }
    return buildByFromDefault();
  }

  /** The annotated field. */
  protected Field getField() {
    return field;
  }

  /** The strategy used when no locating annotation is present: the field name as id or name. */
  protected By buildByFromDefault() {
    return By.idOrName(field.getName());
  }

  /** Rejects the combinations Selenium rejects: at most one of FindBy / FindBys / FindAll. */
  protected void assertValidAnnotations() {
    FindBys findBys = field.getAnnotation(FindBys.class);
    FindAll findAll = field.getAnnotation(FindAll.class);
    FindBy findBy = field.getAnnotation(FindBy.class);

    if (findBys != null && findBy != null) {
      throw new IllegalArgumentException(
          "If you use a '@FindBys' annotation, you must not also use a '@FindBy' annotation");
    }
    if (findAll != null && findBy != null) {
      throw new IllegalArgumentException(
          "If you use a '@FindAll' annotation, you must not also use a '@FindBy' annotation");
    }
    if (findAll != null && findBys != null) {
      throw new IllegalArgumentException(
          "If you use a '@FindAll' annotation, you must not also use a '@FindBys' annotation");
    }
  }
}
