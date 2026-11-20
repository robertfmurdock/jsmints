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

`BrowserType`, `Browser`, `BrowserContext`, `Page`, and `Locator` expose a `native` property that returns the underlying Playwright object. The public `Js*` external interfaces describe the members JSMints already uses. Use `asDynamic()` on the handle for other members, or `unsafeCast<YourExternalInterface>()` to supply local types. For example, `page.native.asDynamic().title()` returns the native JavaScript promise. Use these handles inside the owning scenario so that its normal cleanup and trace capture still apply.

Optional arguments on the native bindings use Playwright's defaults. `JsPage.evaluate` accepts synchronous callbacks or JavaScript source strings. `JsPage.evaluateAsync` accepts promise-returning callbacks and maps to the same JavaScript `evaluate` method, with the resolved result reflected in the Kotlin type.

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

## First slice

The `libraries/playwright` module uses handwritten bindings to Playwright 1.65.1, pinned in the shared JavaScript dependency manifest and Yarn lockfile. It uses a dedicated Kotlin/JS Node test run with the standard Mocha runner. Chromium tests run during normal `check`. The test run provisions the browser from the locked Playwright package. GitHub Actions installs the Linux system dependencies once before the aggregate check. For a focused run:

```sh
./gradlew :libraries:playwright:playwrightRun
```

The slice uses a typed, browser-serializable function and an explicit serialized argument for `evaluate`. This gives page code an explicit browser-side boundary while Kotlin awaits the result in Node.

The initial Karakum 1.0.0-alpha.133 run against the published `playwright-core` declarations reached conversion, then stopped on case-insensitive filename collisions such as `request.kt` and `Request.kt`. A curated declaration probe emitted 24 Kotlin files. It split role types between `Page` and `Locator`, left `PageFunction` unresolved in the probe output, and emitted a top-level `chromium` binding. In the handwritten runtime check, that import form resolved to the module default, so the working binding reads `chromium` from the default export. Keep the handwritten slice while generation still needs curation and a verified module mapping. Generated bindings have yet to pass the browser tests or the version-bump check.

## Fixture parity and consumer integration

The Playwright checks cover the 64 WDIO fixture scenarios. Fifteen query tests each exercise one lookup behavior across role, text, label, and scoped text. The checks use the same HTML fixture as WDIO. They cover immediate resolution, bounded waiting, absence, ambiguity, and ordered collections. Playwright label queries use substring matching for this fixture because the wrapping labels also contain the button text.

The runner checks cover evaluation of a page promise, relative navigation against a Node HTTP server on an ephemeral port, packaged resource loading, and public HTTPS from the Node worker. The CommonJS consumer fixture resolves the Playwright library from the included libraries build. Its Playwright run and the existing WDIO run both participate in the same normal `check`.

Apply `com.zegreatrob.jsmints.plugins.playwrighttest` in the local composite and put the tests in `src/jsPlaywrightTest/kotlin`. Add the Playwright library to `jsPlaywrightTestImplementation` when the project consumes it from another module. The plugin exposes `baseUrl`, `browser`, `headless`, `timeoutMillis`, and `reuseBrowser` through `playwrightTest`. The Gradle properties `playwrightBaseUrl`, `playwrightBrowser`, `playwrightHeadless`, and `playwrightReuseBrowser` override those settings for a run. Chromium and Firefox use the same test logic. The plugin wires the dedicated compilation’s linked executable into Kotlin’s standard Mocha task. Selecting the custom compilation alone leaves Kotlin’s task pointing at the ordinary unit-test executable.

```sh
./gradlew :wdio-testing-library-test:playwrightRun
./gradlew :libraries:playwright:playwrightRun -PplaywrightBrowser=firefox
```

The fixture helper `withPlaywrightPage` applies the configured browser settings, gives each scenario an isolated context, and retains a trace under `build/reports/playwright/traces` for failures inside its block. Put assertions that need browser traces inside that block. Gradle generates its standard HTML and JUnit reports, and Playwright traces carry screenshots and DOM snapshots. WDIO's optional Allure and timeline reporters still use the WDIO run.

### Browser profiles and native configuration

Declare profiles in the Gradle build to run the same compiled Kotlin suite
against multiple browsers. With configuration caching enabled, Gradle can
run the independent profiles concurrently. Reports are separated by task
and traces live under `build/reports/playwright/traces/<profile>`.

```kotlin
playwrightTest {
    profiles {
        register("chromium")
        register("firefox")
        register("chrome") {
            browser.set("chromium")
        }
    }
}
```

`playwrightRun` and `check` run the declared profiles. Use
`-PplaywrightProfile=firefox` to select one. Projects without profiles still
use the existing single-browser configuration. `-PplaywrightBrowser=firefox`
selects an ad hoc single-browser run.

The plugin loads top-level `.mjs`, `.cjs`, and `.js` files from
`playwright.conf.d` in filename order. Set `configDirectory` to use a
different directory. Each fragment exports a configuration object or a
factory receiving `{ profile, browser }`. Factories and hooks can be
asynchronous.

```js
// playwright.conf.d/20-browser-options.mjs
export default ({ profile }) => ({
    launchOptions: profile === "chrome" ? { channel: "chrome" } : {},
    contextOptions: { viewport: { width: 1280, height: 720 } },
    async afterContext(context) {
        await context.setExtraHTTPHeaders({ "x-test-profile": profile });
    },
});
```

Launch and context options are passed through to the native Playwright
library. Later fragments replace earlier values at the option-key level.
For example, a later `viewport` replaces the whole viewport object.
Fragment launch options override launch defaults. A base URL supplied by
Gradle or by a scenario takes precedence over fragment context options.
The optional `afterLaunch`, `afterContext`, and `afterPage` hooks receive
the native Playwright objects and run in fragment order. With browser reuse,
`afterLaunch` runs once per browser process. The context and page hooks
run for every scenario. Errors identify the fragment and, for hooks, the
stage. JSMints installs the bundled engine builds and manages scenario isolation
and artifact retention. A fragment that selects an installed browser channel
needs that channel available on the machine.

### Testmints setup and failure traces

Use the library’s `playwrightSetup` for Testmints browser scenarios. It keeps
the phases visible at the call site. Setup receives the live page and
returns the context used by exercise and verification.

```kotlin
@Test
fun savedStatusAppearsAfterSubmitting() = playwrightSetup { page ->
    page.goto("/settings")
    page
} exercise {
    getByRole("button", name = "Save").click()
} verify {
    getByText("Saved", exact = true).waitForVisible(timeoutMillis = 1500)
}
```

The helper delegates the phases to Testmints and runs the completed result
through `playwrightTest`. It supports `verifyAnd { … } teardown { … }` and
`playwrightSetup(timeoutMs = …)`. The page remains live until teardown
finishes, and teardown failures can retain traces. Use
`playwrightSetupFrom { page -> existingTemplate.with({ … }) }` to adapt an
existing Testmints setup with its own shared setup, teardown, or composition.
Each scenario gets a fresh page and context. By default, the Gradle plugin
reuses the browser process for the Mocha run and closes it in the run teardown.
Use `playwrightTest { reuseBrowser.set(false) }` or
`-PplaywrightReuseBrowser=false` for a fresh browser process per scenario.
Standalone `BrowserType.withPage` calls own their browser. Any `beforeAll`
fixture should have a lifetime independent of an individual scenario’s page. The lower-level
`playwrightTest` runner accepts any Kotlin coroutine-test result.

Keep fixture actions in setup and the action under test in exercise.
The adapter retains a trace when the completed result rejects, including
exercise or verification failures reported after Testmints teardown. Passing
scenarios discard their traces. Both the library and consumer browser tests
exercise the fluent setup API. Node-only resource and HTTPS checks use
plain Testmints setup because they have no browser interaction.

The Gradle integration and library are currently used through the repository's composite build. Publishing them for external consumers is a separate release step.

The Chromium and Firefox runs exercise the same query and runner tests. A configuration probe verified that removing a base URL override restores the consumer fixture’s default. Temporarily removing the duplicate-match wait caused the targeted scenario to fail with a strictness error. The runner printed the trace location and the archive contained browser operations, network data, and screenshots.
