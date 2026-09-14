# Design

This library is a port, not a re-imagining: every type maps onto a Selenium type of the same name
and role, so anyone who has read Selenium's `org.openqa.selenium.support` package will feel at
home. This document explains the mapping, the one place where the port deliberately diverges, and
where to plug in your own behaviour.

## The pipeline

`PageFactory.initElements(page, pageObject)` runs the following for each field of the page object
and its superclasses:

```mermaid
flowchart LR
  PF[PageFactory] -->|for each field| FD[FieldDecorator]
  FD -->|is Locator or annotated List&lt;Locator&gt;?| ELF[ElementLocatorFactory]
  ELF -->|createLocator field| EL[ElementLocator]
  EL -->|reads| AN[Annotations]
  AN -->|@FindBy / @FindBys / @FindAll / custom| BY[By]
  BY -->|locate| SC[SearchContext]
  SC -->|page.locator, getByTestId, ...| LOC[Playwright Locator]
  EL -->|Locator field| LOC
  EL -->|List field| PROXY[List proxy: re-queries via findLocators]
```

| Role                                     | Selenium                            | This library                             |
|------------------------------------------|-------------------------------------|------------------------------------------|
| Entry point                              | `PageFactory`                       | `PageFactory`                            |
| Thing that finds elements                | `SearchContext` (WebDriver, WebElement) | `SearchContext` (Page, Frame, Locator, FrameLocator) |
| Located value                            | `WebElement`                        | `Locator`                                |
| Locating strategy                        | `By`, `ByIdOrName`, `ByChained`, `ByAll` | same names                          |
| Field annotations                        | `@FindBy`, `@FindBys`, `@FindAll`, `@CacheLookup` | same names                  |
| Strategy enum                            | `How`                               | `How` (+ Playwright constants)           |
| Custom annotation hook                   | `@PageFactoryFinder` + `AbstractFindByBuilder` | same names                    |
| Annotation reader                        | `AbstractAnnotations`, `Annotations` | same names                              |
| Per-field locator                        | `ElementLocator`, `DefaultElementLocator`, `ElementLocatorFactory`, `DefaultElementLocatorFactory` | same names |
| Per-field value decision                 | `FieldDecorator`, `DefaultFieldDecorator` | same names                          |
| Lazy list                                | `LocatingElementListHandler`        | `LocatingLocatorListHandler`             |
| Lazy element                             | `LocatingElementHandler`            | not needed (see below)                   |

## `SearchContext`

Selenium has a single `SearchContext` interface implemented by both `WebDriver` and
`WebElement`, which is what lets a `By` be resolved against either. Playwright Java has four
unrelated classes that can locate (`Page`, `Frame`, `Locator`, `FrameLocator`) and no interface
in common. `SearchContext` restores that abstraction: a tiny interface with `locator(String)` and
the `getBy*` methods, plus `SearchContext.of(...)` adapters for each Playwright type. `By.locate`
takes a `SearchContext`, so chaining (`ByChained`) and component scoping fall out for free.

## `By` produces a `Locator`, not an element

In Selenium, `By.findElement(context)` performs a DOM query and returns a handle. In Playwright
the equivalent (`page.locator(selector)`) is a pure client-side operation: it builds a description
of how to find the element and nothing is queried until an action runs. So `By.locate(context)`
returns a `Locator`, and building all of a page object's locators at `initElements` time costs no
round trips.

Selenium strategies are translated to Playwright selector strings (see the table in the README).
Two details worth knowing:

- `id`, `name` and `className` use quoted attribute selectors (`[id="..."]`) rather than
  `#...`/`....` so that values with dots, colons or other CSS-special characters work without
  escaping rules.
- `linkText` / `partialLinkText` use XPath with `normalize-space` so they keep Selenium's exact,
  case-sensitive semantics. XPath has no string escaping, so `By.xpathString` builds a `concat()`
  when a value contains both quote characters. Playwright evaluates an XPath that starts with `//`
  relative to the scope when chained, which is what `ByChained` relies on.

## Why `Locator` fields are not proxies

Selenium wraps every `WebElement` field in a `java.lang.reflect.Proxy` so the lookup can be
deferred until the first method call, and repeated on every call unless `@CacheLookup` is present.
That machinery exists because a `WebElement` is an eager, stale-able handle.

A Playwright `Locator` is already what Selenium's proxy was simulating: it is lazy, it re-resolves
on every action, and it auto-waits. Wrapping it would add nothing, and it would break real
Playwright code: `Locator.or`, `Locator.and`, `filter(new FilterOptions().setHas(...))` and
friends cast their argument to Playwright's internal `LocatorImpl`, which a proxy is not. The
`PageFactoryTest.locatorFieldsAreRealPlaywrightLocatorsNotProxies` test guards this.

`List<Locator>` fields are different. `Locator.all()` takes a snapshot of the currently matching
elements, so to give the list Selenium's "fresh on every access" behaviour it must be re-queried
on each call. Those fields therefore do get a proxy (`LocatingLocatorListHandler`), and
`@CacheLookup` freezes the first snapshot, mirroring Selenium's behaviour for `List<WebElement>`.

## Deliberate small deviations

- **Static and final fields are skipped.** Selenium writes to them. A final `Locator` field can
  only have been assigned by the constructor (typically a component's root), and the default
  id-or-name fallback would silently overwrite it with a locator for `[id="root"]`. Skipping is
  the safer default; the `NavBar` example shows the pattern.
- **Constructor resolution is broader.** Selenium looks for a `WebDriver` constructor and then a
  no-arg one. Here `initElements(context, Class)` looks for a single-argument constructor that
  accepts whatever the context wraps (`Page`, `Locator`, `Frame`, `FrameLocator`), then one that
  accepts `SearchContext`, then a no-arg constructor.
- **Validation happens at init time.** Invalid annotation combinations throw
  `IllegalArgumentException` from `initElements`, the same moment Selenium reports them.

## Extension points

All three Selenium hooks are preserved and work the same way.

**Custom annotation.** Meta-annotate it with `@PageFactoryFinder(YourBuilder.class)` and have the
builder extend `AbstractFindByBuilder`:

```java
@Retention(RUNTIME) @Target(FIELD)
@PageFactoryFinder(ByRole.Builder.class)
public @interface ByRole {
  String value();
  class Builder extends AbstractFindByBuilder {
    @Override
    public By buildIt(Annotation a, Field f) {
      return By.css("[role=" + ((ByRole) a).value() + "]");
    }
  }
}
```

`Annotations` picks it up automatically, and `DefaultFieldDecorator` treats a `List<Locator>`
carrying it as decoratable. See `AnnotationsTest.customAnnotationsViaPageFactoryFinder`.

**Custom `ElementLocatorFactory`.** Return `null` for fields you want left alone, or return an
`ElementLocator` that resolves differently (from configuration, with a timeout, with logging):

```java
PageFactory.initElements((Field f) -> f.getName().startsWith("skip") ? null : new DefaultElementLocator(ctx, f), pageObject);
```

**Custom `FieldDecorator`.** Subclass `DefaultFieldDecorator` and override `proxyForLocator` /
`proxyForListLocator`, or implement `FieldDecorator` from scratch to support additional field
types (for example, injecting your own component classes).

## Threading

`PageFactory` holds no state. `DefaultElementLocator` holds the cached values for
`@CacheLookup` and is not synchronised, the same as Selenium; page objects are expected to be used
from the thread that owns the `Page`, which is also Playwright's own rule.
