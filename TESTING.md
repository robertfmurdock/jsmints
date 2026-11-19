# Testing Guidance

Use this guide with the Testmints skill when writing or reviewing tests in this
repository. Keep tests in the module and source set that own the behavior.

Before changing behavior, state the expected observable contract, its owning
boundary, and the failure the test should reveal. For meaningful behavior
changes, first confirm the focused test fails for that behavior, then implement
the smallest change and refactor with the test passing. Keep the test and
behavior change together. Do not add tests for documentation or purely
mechanical edits.

## Keep tests in normal checks

Include every committed test in the repository's normal `check` lifecycle.
Provision dependencies through the build so local and CI checks exercise the
same tests. Keep the suite fast by testing each contract at the narrowest
boundary that can observe it, reusing fixture setup and existing scenarios where
practical, and sharing test logic across local and CI runs. Give each added test
a distinct regression to catch.

## Choose the behavior boundary

- Test Kotlin rules in the owning library's existing `commonTest` or `jsTest`
  source set.
- Test React component rendering, user interactions, and React Testing Library
  wrappers in their owning `jsTest` source sets. These suites use Node and jsdom.
- Use the WDIO fixture in `wdio-testing-library-test` when the behavior depends
  on a real browser, the WDIO worker, or the assembled fixture. Do not move DOM
  or wrapper cases there just because a browser runner is available.
- Test Gradle plugin behavior in the plugin's existing `functionalTest` suite
  with Gradle TestKit when the contract depends on applying or running the plugin.
- Add broader-level evidence when behavior crosses module or runtime boundaries.
  Keep detailed variations at the narrowest boundary that fully observes them.

## Use Testmints phases to show intent

- Use `setup` for synchronous scenarios and `asyncSetup` for scenarios that
  suspend or depend on coroutine work. Return the Testmints chain from the test
  function, especially in Kotlin/JS.
- In setup, define fresh inputs, collaborators, and prerequisites. Keep the
  behavior under test out of setup.
- In exercise, visibly call the subject or perform the meaningful user action.
  Await completion handles there when available, and return the outcome needed
  by verification.
- In verify, assert on observable output, state, or collaborator interactions.
  Use bounded waits for live asynchronous conditions; keep the subject call
  outside retrying assertions.
- Use `ScopeMint.exerciseScope` when the subject launches work in an injected
  coroutine scope. Await work launched elsewhere or wait on an observable
  condition. Do not treat a thrown exception as cancellation.
- Use `verifyAnd`/`teardown` when a scenario owns resources that need explicit
  cleanup. Keep shared fixture setup and teardown in a Testmints template when
  that makes multiple scenarios clearer.

## Make the scenario and evidence clear

- Name the distinguishing input or starting state, then the observable outcome
  when that helps readers compare cases. Read the name and body both ways; they
  should describe the same behavior.
- Carry meaningful inputs through the subject call into verification. Prefer
  `assertIsEqualTo` with an expected value that states the contract.
- For expected failures, capture the outcome of the specific call and assert the
  expected failure evidence. A generic `isFailure` check can let unrelated
  exceptions pass.
- Test behavior at the public boundary, not implementation text, source
  structure, or type declarations. Use the simplest fixture that exposes the
  behavior.
- Add a scenario when it protects a meaningful contract and would catch a
  plausible regression. Avoid exhaustive permutations and tests added only to
  increase coverage.
- Every behavioral test should fail when the behavior in its name is absent.
  During review, identify a plausible defect and trace which assertion would
  expose it. For a new or materially changed test, confirm that failure using
  the prior behavior or a reversible, isolated fault when practical. Run the
  scenario alone when supported. The failure should identify the behavior under
  test, not broken syntax, fixtures, or unrelated setup.

## Keep tests easy to read and maintain

- Keep the subject call and scenario visible at each test call site. Use small
  helpers for a reusable fixture or assertion when they clarify intent.
- Prefer some duplication over a generic test framework that hides which
  behavior is exercised. Preserve separate suites when they test distinct
  public interfaces or runtime boundaries.
- Keep related observations of one action together. Split scenarios when they
  exercise different transitions or different behavior owners.
- When extending query or wrapper tests, cover meaningful outcomes and the
  synchronous/asynchronous paths that have distinct behavior; do not copy a
  full matrix mechanically when the cases add no new evidence.

## Run the relevant suite

Run the smallest task that exercises the changed boundary. Examples:

```sh
./gradlew -p libraries :react-testing-library:jsNodeTest
./gradlew -p plugins :plugins:functionalTest
./gradlew :wdio-testing-library-test:e2eRun --no-configuration-cache
```

The WDIO fixture uses a real browser; its required ports and configuration are
documented in the README under “Running the repository’s WDIO fixture.” Run
`./gradlew check` for the repository completion gate when the change crosses
boundaries or requires broad validation.
