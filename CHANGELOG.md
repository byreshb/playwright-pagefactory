# Changelog

All notable changes to this project are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and the project uses
[Semantic Versioning](https://semver.org/).

## [Unreleased]

### Added
- `@FindBy(role = "button", roleName = "Sign in")`, `By.role(...)` and `How.ROLE` for
  Playwright's `getByRole`, the locator Playwright recommends first.
- Releasing guide covering GitHub Releases and the planned Maven Central setup.

## [1.0.0] - 2026-09-13

### Added
- Selenium-style PageFactory for Playwright Java: `@FindBy`, `@FindBys`, `@FindAll`,
  `@CacheLookup`, `How`, `By`, `ByChained`, `ByAll`, `ByIdOrName` and
  `PageFactory.initElements` for `Locator` and `List<Locator>` fields.
- Playwright strategies as `@FindBy` attributes: `selector`, `testId`, `text`, `label`,
  `placeholder`, `altText`, `title`.
- `SearchContext` adapters so page objects can be scoped to a `Page`, `Frame`, `Locator`
  or `FrameLocator`.
- Extension points mirroring Selenium: `ElementLocatorFactory`, `FieldDecorator`,
  `@PageFactoryFinder` with `AbstractFindByBuilder`.
- Example page objects and components under `src/test/java/.../examples`.
- Documentation: README, design notes and a Selenium migration guide.
- Tooling: automatic formatting (Spotless, google-java-format), JaCoCo coverage gate at 85%,
  Playwright trace and screenshot capture for failing tests, GitHub Actions CI and release
  workflows.

[Unreleased]: https://github.com/byreshb/playwright-pagefactory/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/byreshb/playwright-pagefactory/releases/tag/v1.0.0
