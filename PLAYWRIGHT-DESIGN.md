# Playwright for Kotlin/JS end-to-end tests

Status — Proposed. The API and task names are illustrative.

## Goal

Give consumers of the JSMints WDIO plugin an incremental migration to Playwright. Their test code still runs in Node, so it can keep using `kotlin.test`, Testmints, Kotlin/JS helpers, npm packages, and Node APIs. Every scenario currently covered by the repository’s WDIO fixture must have an explicit Playwright counterpart. Use that inventory to set the initial Kotlin API scope.

The design has two pieces. A Kotlin/JS library provides typed access to the npm `playwright` API. An opt-in Gradle integration compiles and runs Kotlin/JS E2E tests with Mocha and manages browsers, configuration, and failure artifacts. The library should also be usable from a consumer’s own Node test setup.

JetBrains’ experimental browser DSL can coexist in the same build for tests that execute inside the browser. This proposal uses Playwright from Node, where the existing WDIO tests and their helpers already run. Playwright Test is a separate runner with its own fixtures, assertions, and reports. Consumers whose priority is that runner experience can evaluate TypeScript specs that call exported Kotlin/JS helpers. [Kotlin browser testing](https://kotlinlang.org/docs/js-running-tests.html) · [Playwright Library](https://playwright.dev/docs/library)

## Scenario parity

The [WDIO browser fixture](wdio-testing-library-test/src/jsE2eTest/kotlin/com/zegreatrob/wrapper/wdio/testing/library) contains 64 tests. Each row below describes the behavior to carry over. Reuse the served fixture where practical. Keep the current WDIO tests as independent coverage of their own APIs.

| Existing coverage | Playwright counterpart |
| --- | --- |
| `ByRoleTest` (15) | `getByRole` with accessible name options. Verify the matching element, visibility, and identifying attributes. Exercise every case in the query matrix below. |
| `ByTextTest` (15) | `getByText` with explicit matching options where needed. Exercise the same matrix. |
| `ByLabelTextTest` (15) | `getByLabel` against the existing labeled controls. Exercise the same matrix. |
| `WithinTest` (15) | Locate a section and chain `getByText` from that locator. Exercise the same matrix and prove that an identical match outside the section is excluded. |
| `BrowserTest` (2) | Await a promise returned by page evaluation with an explicit serialized argument. Resolve a relative navigation against the fixture base URL and verify the final absolute URL. |
| `ResourceLoadingTest` (1) | Read the packaged test resource from Node and verify its contents. The runner must provide a predictable resource location compatible with its module format. |
| `PublicHttpsFetchTest` (1) | Make a public HTTPS request with Node’s `fetch` inside the actual runner process and assert the response status. Preserve normal TLS verification. This covers Node runtime access and uses no browser API. |

Each query family has nine single-result cases and six collection cases. “Immediate” below means a current observation at the time the query is resolved. “Wait” means a bounded wait for the required result.

| Query intent | Zero matches | One match | Multiple matches |
| --- | --- | --- | --- |
| Require one immediately (`getBy`) | Fail with missing-match evidence | Verify the match | Fail with ambiguity evidence |
| Wait for one (`findBy`) | Fail at the deadline with missing-match evidence | Verify the match | Fail with ambiguity evidence if still ambiguous at the deadline |
| Check for one optionally (`queryBy`) | Return absence | Verify the match | Fail with ambiguity evidence |
| Require all immediately, wait for a nonempty collection, or query all | The current WDIO fixture has no zero-match collection cases | Verify the singleton for all three intents | Verify membership and order for all three intents |

Playwright locators are lazy. Creating a locator succeeds before the element exists, and operations that require one match can fail with a strictness error. Use explicit resolution where a migrated helper needs immediate absence, uniqueness, or a bounded wait for a nonempty result. Keep that policy in the calling helper or a narrowly scoped compatibility helper if the pilot shows repeated need. Use a delayed insertion to prove the waiting path actually waits. Verify the failure category and relevant query details. Playwright and Testing Library have distinct selector, matching, and error conventions, so check the fixture’s actual results and set options explicitly where needed. `isVisible()` is an immediate observation and `all()` captures the matches currently available. Stabilize the collection before enumerating when the page is loading. [Playwright locators](https://playwright.dev/docs/locators) · [Locator API](https://playwright.dev/docs/api/class-locator)

The three [WDIO plugin functional tests](plugins/plugins/src/functionalTest/kotlin/com/zegreatrob/jsmints/plugins/WdioTestPluginFunctionalTest.kt) also inform the integration contract. Verify root npm installation in a multi-project build, runner distribution and dependency resolution, and rerunning when an environment override is removed and the default becomes effective. If Playwright uses a different runner distribution mechanism, verify the equivalent consumer outcome through that mechanism.

## Kotlin API

Follow the official Node API for names and semantics. Use the JVM binding as a reference for shared vocabulary such as `Page`, `BrowserContext`, `Locator`, and ARIA roles. Keep locator construction synchronous. Use suspending calls for operations that return Node promises. Offer Kotlin named arguments or typed options where they improve call sites. Pass JSON-compatible values explicitly into `evaluate`, which executes in the browser. Verify the Kotlin/JS callback and promise bridge before committing to its public signature. [Node Page API](https://playwright.dev/docs/api/class-page) · [Java Page API](https://playwright.dev/java/docs/api/class-page) · [Evaluating JavaScript](https://playwright.dev/docs/evaluating)

Start with the types needed for browser launch, context and page lifecycle, evaluation and navigation, role/text/label queries, locator scoping, visibility and attributes, match enumeration, and trace capture. Include a minimal locator action such as `click` in the consumer pilot. Add public bindings when an explicit scenario needs them. New capabilities such as routing, popups, or downloads can be added independently when a consumer workflow calls for them.

## Gradle integration and migration

- Give Playwright its own opt-in suite and task so a consumer can run both during migration. Use the existing Kotlin Node test machinery where it can provide discovery, async completion, resources, filters, and test reports. Verify those behaviors before choosing how much runner code JSMints needs to own.
- Resolve the Playwright npm dependency with the Kotlin build and provision matching browser binaries from that version. Start with Chromium for the pilot and verify the existing fixture’s Chromium and Firefox profiles before claiming parity. Pass base URL, headless mode, and timeouts as explicit configuration. Document the HTTPS fixture’s trust setup and scope any local exception to the fixture.
- Let the runner own browser launch and shutdown. Provide a fresh context per test and a page fixture. Close contexts even when setup or the test fails, and preserve the original test failure if cleanup also fails. Make additional contexts possible through the library.
- Capture context traces and retain them on failure before closing the context. Surface unique artifact paths in test output. Library tracing captures browser operations and network activity. Mocha and Gradle report test assertions. Review existing screenshot and reporter workflows with the migrating consumer and document the equivalent outputs or gaps. [Playwright tracing](https://playwright.dev/docs/api/class-tracing)
- Model effective environment values and test inputs in Gradle. Follow the existing E2E convention of explicit opt-in for result caching when a consumer can declare the application inputs. Review [Gradle maintenance](GRADLE-MAINTENANCE.md) and the [refactoring initiative](GRADLE-REFACTORING.md) when implementing build changes.

Start migration with a shared browser helper and one Kotlin scenario that crosses the browser boundary and calls a real Node-side helper. Run that slice beside WDIO against the same app. Then move the rest of the browser helpers and scenarios incrementally. Domain setup, API clients, and Testmints phases can carry over when their contracts still fit.

## Handwritten bindings or Karakum

Start with a small handwritten slice. In parallel with evaluating that slice, try **Karakum**, a TypeScript-to-Kotlin converter used for some JetBrains Kotlin Wrappers modules. The `kotlin-actions/karakum` and `kotlin-cesium/karakum` directories are examples to inspect. Playwright publishes its own TypeScript declarations. Use the declarations from the pinned npm release as input and verify that generated imports target the runtime module that actually exports the symbols. [JetBrains Kotlin Wrappers](https://github.com/JetBrains/kotlin-wrappers) · [Karakum](https://github.com/karakum-team/karakum) · [Playwright Core declarations](https://github.com/microsoft/playwright/blob/main/packages/playwright-core/types/types.d.ts)

Compare the two approaches on the same slice. Include `launch`, `newContext`, `newPage`, role options, locator chaining, and promise-returning `evaluate`. Those exercise optional properties, literal unions, generic callbacks, and promise handling. Try generating only the required declarations and their transitive types, using a curated input or converter configuration if necessary. Keep coroutine ergonomics in handwritten code over either set of externals.

Adopt generation if pinned inputs regenerate deterministically, the Kotlin compiles and runs the same scenario, and a trial Playwright version bump produces a reviewable diff without manual edits to generated files. Compare the generator configuration and patches with the size and upkeep of the handwritten slice. If generation requires a large public surface or more maintenance than the bindings it replaces, ship the handwritten slice and revisit when the scenario inventory grows.

## Acceptance for the first consumer release

1. Every one of the 64 WDIO fixture scenarios has a mapped, passing Playwright counterpart. The four query families explicitly cover the matrix above.
2. Gradle functional coverage verifies the consumer-facing install, resource, and configuration contracts. A mixed project can execute both suites independently.
3. A deliberate assertion failure produces a readable Gradle failure and a usable trace. A subsequent test starts in a fresh context and repeat runs shut down cleanly.
4. The consumer pilot preserves its Node helpers and Kotlin test structure, and exercises one Playwright feature that motivated the migration. Record which feature justified any additional bindings.
5. Document the chosen binding approach, browser and Node setup, and how to move a WDIO scenario. Record any reporter or browser-profile differences that affect the consumer.
