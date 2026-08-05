# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A library (published to Maven Central as `org.bonitasoft.web:ui-designer-artifact-builder`) that builds artifacts designed with the Bonita UI Designer: it imports/migrates page, fragment and widget JSON models and generates the runnable HTML/zip output. Java 17, multi-module Maven, gitflow branching (default branch: `develop`, PRs target `develop`).

## Build & test commands

```bash
./mvnw clean verify                 # full build + unit tests + the IT
./mvnw spotless:apply               # fix formatting (build FAILS on violations otherwise)
./mvnw test -pl artifact-builder    # tests for one module
./mvnw test -pl artifact-builder -Dtest=DefaultArtifactBuilderTest                 # single class
./mvnw test -pl artifact-builder -Dtest='DefaultArtifactBuilderTest#should_*'      # single method(s)
./mvnw verify -pl artifact-builder -Dit.test=ArtifactBuilderIT                     # the integration test
```

Gotchas:
- **PhantomJS** (JS tests in `generator-angularjs`): on Ubuntu 24 set `OPENSSL_CONF=/dev/null` or the build fails at phantomjs startup.
- **The maven-enforcer-plugin fails the build at `validate`** on JDK < 17 and on any javax-generation Bean Validation / EL dependency (see the `bannedDependencies` rule in the root pom).
- **Spotless runs at `process-sources` and fails the build**: Eclipse formatter (`formatter.xml`), import order `java/javax/jakarta/org/com` (`eclipse.importorder`), license header (`header.txt`), sorted poms. `-Poffline` skips the check.
- `-PkeepNodeModules` avoids wiping `node_modules` on `clean` (yarn install is slow).
- Building a module alone requires its siblings installed or `-am` (e.g. `./mvnw test -pl common -am`).

## Module dependency graph

```
model ──> common ──> generator-angularjs ──┐
  │         │                              ├──> artifact-builder ──> coverage-report
  └─────────┴──────────── migrationReport ─┘
artifact-builder-dependencies (BOM only, no code)
```

- **model** — POJOs (`Page`, `Fragment`, `Widget`, `Element` hierarchy), `JsonHandler`/`JacksonJsonHandler`, and the `ElementVisitor<T>` interface everything else implements. No Spring.
- **common** — file-based repositories (`PageRepository`, `WidgetRepository`, `JsonFileBasedLoader/Persister`), live build (`Watcher`), export infrastructure (`ExportStep`, `Zipper`), `migration.Version`, `GeneratorStrategy` abstraction.
- **generator-angularjs** — the AngularJS rendering backend: `AngularJsGeneratorStrategy`, `HtmlBuilderVisitor`, Handlebars templates (`src/main/resources/templates/*.hbs.*`), 27 provided `pb*` widgets, plus a gulp-built JS runtime (see below).
- **artifact-builder** — public API and wiring: services, importers/exporters, migration step definitions.
- **migrationReport** — 4 DTO classes (`MigrationReport`, `MigrationStepReport`…) kept as a separate module so consumers can depend on the report types alone.
- **coverage-report** — JaCoCo aggregation for Sonar; not published.

## Architecture

**Entry point**: `ArtifactBuilder` interface → `DefaultArtifactBuilder`, created via `new ArtifactBuilderFactory(UiDesignerProperties).create()`. All wiring is done by hand in `ArtifactBuilderFactory` and `UiDesignerCoreFactory` (no Spring context — spring-core is only used for classpath resource scanning). Configuration via `UiDesignerPropertiesBuilder`; use `.disableLiveBuild()` for library/CI use, otherwise a file watcher thread starts.

**Build flow (page → zip)**: `DefaultArtifactBuilder.build()` → `PageExporter` → `DefaultPageService.get(id)` (loads JSON, applies migrations) → ordered `ExportStep`s write into a `Zipper` (`PagePropertiesExportStep`, `AssetExportStep`, `FragmentsExportStep`, `HtmlExportStep`, `WidgetsExportStep`).

**Rendering (model → HTML)**: `HtmlBuilderVisitor implements ElementVisitor<String>` walks the element tree; each `visit()` renders a Handlebars template via `TemplateEngine`. The visitor pattern is the core idiom — cross-cutting concerns are separate visitors (`WidgetIdVisitor`, `AssetVisitor`, `PropertyValuesVisitor`, …).

**Migrations**: a `Migration<A>(targetVersion, MigrationStep<A>...)` applies steps when the artifact's version is older. Steps live in `artifact-builder`'s `org.bonitasoft.web.designer.migration` (+ `.page`). **The ordered migration lists are hardcoded in `UiDesignerCoreFactory.create()`**, keyed by version strings up to the current model version — adding a model version means adding entries there and bumping `<uidModelVersion>` in the root pom. Whole-workspace migration happens at `Workspace.initialize()` via `LiveRepositoryUpdate`.

**JSON**: `JacksonJsonHandler` built by `JsonHandlerFactory`. `DEFAULT_VIEW_INCLUSION` is disabled, so **Jackson views are mandatory** — a field without `@JsonView(JsonViewPersistence.class)` is silently not persisted. `Element` polymorphism uses `@JsonTypeInfo(property = "type")` with subtypes registered explicitly in `JsonHandlerFactory` — new element types must be registered there.

**Runtime resources travel via the classpath**: generator-angularjs jars its gulp-built JS runtime under `META-INF/resources/runtime/**` and widgets under `widgets/**`; `Workspace.initialize()` extracts them into `<workspaceUid>/extract/angularjs` (deleted and re-created each time) and copies `pb*` widgets into the user widget repository.

## Non-obvious build mechanics

- **Generated sources**: `artifact-builder` and `generator-angularjs` filter `src/main/java-templates/**` (e.g. `Version.java` with `${project.version}`) into `target/generated-sources` — the IDE won't compile until Maven has run once. `generator-angularjs` also runs jsonschema2pojo on `target/widget-schema/`.
- **generator-angularjs needs node**: frontend-maven-plugin installs node/yarn at the repo root and runs gulp (`yarn run build` outputs directly into `target/classes`; `yarn test` runs karma/jasmine). A GitHub dependency (`bonitasoft/widget-builder`) supplies a Handlebars template copied out of `node_modules`.
- Lombok is used throughout (`provided` scope) — IDE needs annotation processing enabled.

## Conventions

- **Commit messages** follow `type(category): description` (types: `feat`, `fix`, `chore`, `docs`, `test`, `refactor`, `breaking`, …) — the changelog is generated from them.
- **Tests**: JUnit 5 + AssertJ + Mockito. Surefire runs `*Test`; failsafe runs `*IT` (currently only `ArtifactBuilderIT`, a full import→build→unzip round trip). Use the fluent test-data builders in `model`'s `org.bonitasoft.web.designer.builder` (`PageBuilder`, `WidgetBuilder`, `aPage()`, …) — they're shared between modules via test-jars. Any update must be tested, at least with unit tests.
- JS tests for the AngularJS runtime live in `generator-angularjs/src/test/javascript` (karma).
