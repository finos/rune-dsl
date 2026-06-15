# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

Rune DSL is a domain-specific language (DSL) for modelling data and business logic in the financial markets' industry (used heavily for regulatory reporting). The repo defines the language grammar **and** a code generator that turns a Rune model into executable Java. It is an [Xtext](https://www.eclipse.org/Xtext/) 2.38 project built with EMF/Xcore. The DSL is historically named "Rosetta" — the Java package root is `com.regnosys.rosetta` and the grammar is `Rosetta.xtext`.

## Build & test

Requires **Java 21** strictly (enforced as `[21,22)`; Xtend cannot compile on later JDKs). Verify with `mvn -v`.

```bash
mvn clean install              # full build of all modules (runs grammar gen + codegen + tests)
mvn -pl rune-lang -am install  # build one module and its dependencies
mvn -o ...                     # offline, once dependencies are cached
```

Run tests:

```bash
mvn test -pl rune-integration-tests                       # one module's tests (most tests live here)
mvn test -pl rune-integration-tests -Dtest=ClassNameTest  # a single test class
mvn test -pl rune-integration-tests -Dtest=ClassNameTest#methodName  # a single method
```

Checkstyle is enforced during the build (`checkstyle.xml`); a violation fails the build.

### Generated sources — do not edit

Several source roots are **generated and git-ignored**; never edit files under them, edit the grammar/generator inputs instead and rebuild:

- `**/emf-gen/` — EMF model classes generated from the Xcore/`.xtext` model
- `**/src-gen/` — Xtext infrastructure (parser, lexer, scoping stubs)
- `**/xtend-gen/` — Java generated from `.xtend` sources

The grammar lives at `rune-lang/src/main/java/com/regnosys/rosetta/Rosetta.xtext`. It is processed by the MWE2 workflow `GenerateRosetta.mwe2`, which runs automatically in Maven's `generate-sources` phase. After changing the grammar you must rebuild (`mvn -pl rune-lang generate-sources` or a full build) to regenerate the parser and EMF classes.

## Module layout

- **rune-lang** (artifact `rune-lang`, "Rune DSL SDK") — the core. Grammar, EMF model, plus the meat of the language under `com.regnosys.rosetta`: `validation` (model validators), `scoping` (name resolution), `types` (type system / type inference), `generator/java` (the Java code generator — `object`, `function`, `expression`, `condition`, `enums`, `reports`, etc.), `formatting2` (the auto-formatter), `parsing`, `serialization`, `derivedstate`, `interpreter`.
- **rune-runtime** — runtime library the generated Java depends on at execution time.
- **rune-testing** — shared test utilities/fixtures for model and generator tests.
- **rune-integration-tests** — where the bulk of tests live (grammar parsing, validation, code generation regression, formatting). Test models/expectations sit under `src/test/resources/` (e.g. `generation-regression-tests/`).
- **rune-ide** — Language Server (LSP) implementation for editor integration.
- **rune-maven-plugin** — Maven plugin that runs Rune code generation in downstream model builds.
- **rune-tools** — standalone scripts.
- **rune-profiling** — profiling harness.
- **rune-xcore-plugin-dependencies** — packaging module supplying Xcore plugin dependencies to the build.

## Xtend → Java migration (active)

The codebase is mid-migration from Xtend to plain Java (~64 `.xtend` files remain). New code should be written in Java. The process and rules are documented in `.junie/xtend-migration-plan.md` — key points when migrating a file:

- Treat the `xtend-gen/` output as *inspiration only*; write idiomatic Java (meaningful names, Java streams/lambdas, Java text blocks instead of `StringBuilder` patterns, no `@Extension`/Xtend libs).
- Delete the generated Java stub before adding the hand-written Java class so there is no duplicate type, then delete the original `.xtend` source.
- Pay special care to Xtend triple-quote string templates — these are used throughout the code generator and are easy to break.
- Run tests before and after and confirm the same number of tests execute.

## Notes

- IntelliJ has limited Xtext support: it can't edit `.xtend`/`.xtext` or run `GenerateRosetta.mwe2` — let Maven handle those and edit/run regular Java there. Eclipse "IDE for Java and DSL Developers" (2025-06) is the supported full IDE. See `README.md` for IDE setup and troubleshooting.
- This is a FINOS project; contributions require a CLA and go through PRs (see `CONTRIBUTING.md`).
