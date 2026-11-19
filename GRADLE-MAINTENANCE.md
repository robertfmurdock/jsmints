# Gradle maintenance backlog

## Problem

The Gradle audit found deferred configuration, eager task configuration, and wrapper and repository maintenance work across the JSMints composite. Each change can be reviewed, verified, and released independently.

## Expected changes

- [x] **High — remove six `afterEvaluate` callbacks.** Migrate the dependency BOM, plugin publishing build, reports and publishing conventions, and MinReact and wrapper KSP plugins one at a time. Use plugin or configuration callbacks and providers. Check publication metadata, report wiring, and local and published processor resolution as applicable. [Gradle guidance](https://docs.gradle.org/current/userguide/best_practices_general.html#avoid_after_evaluate)
  - Migrated Gradle plugin and convention POM metadata to publication callbacks. The regenerated plugin and BOM POMs matched byte-for-byte on Gradle 9.9.1.
  - Removed the reports callback and switched the convention and plugin project to a single `collectResults` copy task per project. All 1,848 files under the aggregate CI artifact path matched byte-for-byte before and after.
  - Migrated the MinReact and wrapper KSP dependencies to lazy configuration callbacks. Both local JS fixtures passed. A consumer-mode probe confirmed that both KSP configurations declare the expected external processor coordinates when local selection is disabled.
  - Replaced the remaining publication callbacks with task configuration rules. The combined BOM and wrapper processor publication dry run scheduled the same 50 tasks in the same order.
- [x] **High — keep WDIO configuration lazy.** Model environment values, paths, arguments, and reporter flags through task properties. Resolve concrete values when the task needs them and preserve input tracking for values that affect results. [Gradle guidance](https://docs.gradle.org/current/userguide/best_practices_tasks.html#avoid_provider_get_outside_task_action)
  - Wired the WDIO working directory, runner arguments, log output, and diagnostic message through task properties. Existing Node task setters still work.
  - Moved WDIO template token replacement into the copy action and declared the replacement values as task inputs. All rendered fixture configuration files matched the rebased baseline byte-for-byte. Plugin validation, the WDIO TestKit suite, and both browsers passed with the configuration cache enabled.
  - Wired WDIO environment defaults through providers and declared the effective environment as a task input. Chrome and Firefox wrote reports to a task-level override with configuration caching enabled. Removing the override reran WDIO and regenerated reports in the default location during the successful Java 21 `assemble check` and TestKit run.
- [x] **High — move WDIO log directory creation to execution.** The Node task creates the parent of its declared log output immediately before opening the stream. A fixture `e2eRun --dry-run` with a fresh build directory left the log directory absent. [Gradle guidance](https://docs.gradle.org/current/userguide/best_practices_performance.html#avoid_computations_in_configuration_phase)
- [x] **Medium — declare the WDIO fixture root name.** The standalone `projects` task confirmed `wdio-testing-library-test` and all three included builds on Gradle 9.9.1. [Gradle guidance](https://docs.gradle.org/current/userguide/best_practices_general.html#name_your_root_project)
- [x] **Medium — centralize dependency repositories.** Moved repository declarations in the convention build, libraries (including the BOM and processors), plugin implementation build, and WDIO fixture to their respective settings. Kept the published WDIO plugin’s Maven Central default for consumers. Java 21 `assemble check` passed, including Chrome and Firefox. [Gradle guidance](https://docs.gradle.org/current/userguide/best_practices_dependencies.html#set_up_repositories_in_settings)
- [x] **Medium — configure WDIO task cacheability.** Consumers choose caching through Gradle's task outputs API. This build opts in with `outputs.cacheIf { true }` and declares the controlled app's resources, Caddy configuration, and Compose definition as inputs. The plugin also tracks the installed runner. The prior decision to disable caching for this build was too broad. Reports restored `FROM-CACHE` after removing declared outputs. A temporary Caddy configuration change invalidated the entry and reran Chrome and Firefox successfully.
- [x] **Medium — align the checked-in wrappers.** Regenerated the libraries wrapper at Gradle 9.9.1 with the published binary distribution checksum. Standalone `tasks` passed before and after. The root `:libraries:tasks` passed after, and the new JAR matched Gradle’s published hash. [Version guidance](https://docs.gradle.org/current/userguide/best_practices_general.html#use_latest_minor_versions) · [Wrapper validation](https://docs.gradle.org/current/userguide/best_practices_security.html#validate_wrapper_checksum)
- [x] **Recommendation — switch the root wrapper to `bin`.** Regenerated the Gradle 9.9.1 wrapper and updated the scheduled workflow to request `bin`. Pinned the published distribution checksum, verified the wrapper JAR against Gradle’s published hash, and ran `tasks` before and after. [Gradle guidance](https://docs.gradle.org/current/userguide/best_practices_performance.html#prefer_bin_distribution)
- [x] **Recommendation — reduce reliance on generated Kotlin task names in WDIO.** Use the compilation and development binary task providers. Direct fixture `e2eRun` scheduled the link and resource tasks and passed in Chrome and Firefox. Both WDIO TestKit cases passed. [Gradle guidance](https://docs.gradle.org/current/userguide/best_practices_tasks.html#dont_hardcode_task_names)

## Verification and release

- Keep commits focused on one independently verifiable change. Update this checklist in the same change as the fix. Check off an item after verification and record the check that passed. For partial work, record what remains under the open item. Split the six callbacks across commits where possible.
- Run focused checks first, then the repository’s normal `assemble check` CI path on Java 21. For WDIO task changes, exercise the existing TestKit suite and fixture and verify configuration-cache behavior. The main CI build uses the configuration cache; the release step retains its explicit opt-out. For publishing changes, compare generated metadata and verify consumer resolution before merging.
- Follow the existing commit and version conventions (`[none]` for non-release work and `[patch]` for backward-compatible fixes where appropriate). Use `[none]` when a commit affects only unpublished libraries. Let the existing fingerprint, version calculation, and release workflow determine and publish releases. Keep publishing on the existing isolated CI path with scoped credentials.

The audit was static. Runtime behavior and wrapper binary integrity are verification work for the relevant changes.

## Verification checkpoints

- [x] Include Playwright browser tests in normal checks. The library check installed Chromium from the locked npm package and passed all seven tests with configuration caching enabled. The root `assemble check` dry run included both browser installation and execution. The libraries build-scan callback now follows the root build's CI predicate so the configuration cache can store it. Linux CI provisioning is wired and remains to be verified on a hosted runner.

- November 17, 2026 — Java 21 and Gradle 9.9.1 passed `assemble check` and the two WDIO TestKit cases. The WDIO fixture passed in Chrome and Firefox with Gradle running outside the restricted environment. After switching from Java 22, rerunning convention-plugin validation with `--rerun-tasks --no-build-cache -Pkotlin.compiler.execution.strategy=in-process` rebuilt the generated classes for Java 21. Run `clean` separately from `assemble check` to avoid the composite task scheduling failure observed when combining them.

- November 18, 2026 — Java 21 `assemble check` and both WDIO TestKit cases passed after the repository, publication, KSP, and WDIO changes. The browser fixture passed with `JSMINTS_WDIO_HTTP_PORT=28080` and `JSMINTS_WDIO_HTTPS_PORT=28443` while another local stack held 80 and 443. Two separate WDIO dry runs stored and reused the configuration cache.

## Follow-up investigations

- [x] Pin the convention-plugin Java 21 toolchain. Rebuilding with Java 22 as the Gradle launcher generated 254 classes, all at Java 21 class-file version 65. Plugin validation then passed on Java 21.
- [x] Give generated WDIO fragments a single directory owner. Changed the copy task to `Sync` and removed its explicit clean-task dependency. Disabling the HTML reporter removed its fragment while preserving fixture and Allure fragments. Restoring it passed in Chrome and Firefox.
- [x] Enable configuration caching for the CI build and check. Three local Java 21 runs used the same version input. The second invalidated the cache after an included plugin compiler input changed, and the third reused it. The dashboard summaries matched byte-for-byte with 224 passing tests. The release dry run reported incompatible Sonatype publishing tasks, so the release step keeps its explicit opt-out.
- [x] Investigate publication-specific signing outputs. The shared Javadoc signature reproduced Gradle's implicit-dependency error when publishing both wrapper processor variants to a disposable Maven repository. A probe with one Javadoc archive path per publication published the BOM and both processor variants locally. GPG verified all 14 detached signatures. Production publication wiring was subsequently simplified with separate Javadoc and signature paths in the [refactoring initiative](GRADLE-REFACTORING.md).
  - The Nexus plugin attached its staging initializer to the disposable repository tasks. Sonatype rejected that initial unauthenticated request with HTTP 401. Subsequent runs excluded the initializer and the BOM's disabled Sonatype publication, and a dry run verified the local graph. The probe replaced publication artifacts after Kotlin had finished configuring them.

## Additional build cleanup

- [x] Pin both KSP processor toolchains to Java 21, matching the existing class target. Launching Gradle with Java 22 still produced Java 21 class files for both processors. Both consumer fixtures passed.
- [x] Filter dependency repositories and remove the duplicate Plugin Portal declaration. Google is scoped to Android coordinates. The convention build uses the Plugin Portal for its three plugin implementation groups and Maven Central for other dependencies. Compile and runtime dependency coordinates matched the baseline in both builds.
- [x] Remove the redundant convention-build Kotlin stdlib declaration. Both final compile and runtime dependency graphs matched the baseline. Retained and documented the published plugin build declaration because removing it downgraded runtime stdlib from 2.4.21 to 2.4.0.
- [x] Describe and group NCU and WDIO preparation tasks. The disposable NCU consumer showed its description and dependency-management group in task help. All three WDIO preparation tasks appeared in the verification task listing. Final Java 21 `assemble check` and the three TestKit cases passed with configuration caching enabled; Chrome and Firefox passed.

## Release regression follow-up

- [x] Isolate the WDIO runner executable archive per Maven publication. CI run 44533591837 exposed overlapping executable signature outputs when publishing both variants. Restoring the shared archive in a disposable local publication reproduced the exact implicit-dependency failure. Separate archive directories preserved the executable classifier and identical archive contents; both signed publications and runner lint passed locally, and GPG verified all 13 detached signatures. Remote release remains to be verified by CI.

## Remaining provider and library cleanup

These items are investigations from the static follow-up audit. Confirm the
supported Kotlin Multiplatform APIs and required dependency metadata before
settling on an implementation.

- [x] Defer Node/npm directory resolution in `NodeExec.setup` where the Kotlin plugin supports it. Inspect the pinned Kotlin plugin's public Node and npm APIs and preserve the published setters and both NCU and WDIO entry points. Verify task configuration, actual executable paths, and configuration-cache reuse. If a value must be queried during configuration, record the specific API and lifecycle constraint.
- [x] Track and reuse parsed package metadata. Review `JsConstraintExtension`, `JsPackageExtension`, and template version generation for repeated configuration-time file reads. Use tracked file-content providers and reuse parsed data where possible. Dependency declarations can need metadata during configuration, so preserve dependency selection and compare resolved coordinates, generated templates, and package outputs.
- [x] Wire the WDIO runner archive from its compiler output with `flatMap`. Check the public compilation and binary APIs before changing the existing named lookup. Remove the explicit prerequisite only after verifying that the output provider schedules compilation on a fresh build. Compare the executable archive contents and run direct WDIO execution.
- [x] Extract focused conventions for repeated library test setup and generated-source lint handling. Compare the repeated Mocha/jsdom blocks first. Check whether lint and format still need KSP prerequisites when generated sources are excluded. Verify generated-source exclusions, test behavior, and generated package configuration.
- [x] Review React dependencies declared by `react-data-loader` that `minreact` exports through its API. Inspect source usage and public API ownership before removing declarations. Compare resolved dependencies, generated POMs, and Gradle module metadata, then run the affected tests.

### Testmints precedent and investigation notes

- The sibling checkout's `GRADLE_BUILD_IMPROVEMENTS.md` checkpoints 10 and 12
  describe moving Mint Logs hooks through Kotlin/JS test resources. Commit
  `9197667` uses the compilation's `processResourcesTaskName` to supply those
  files and removes direct configuration of `DefaultIncrementalSyncTask`.
  The same commit removes explicit KSP prerequisites from fixture lint and
  format tasks that exclude generated sources. Use those public integration
  points as precedent when reviewing this build's runner and lint wiring.
- Testmints checkpoint 16 retains a direct `:report` API dependency in
  `:async` because its public types use that API, despite transitive exposure
  through `:standard`. Apply that reasoning when assessing the React
  declarations here. A matching runtime classpath alone does not establish
  that removing a direct dependency preserves the intended publication contract.
- Checkpoint 15 keeps dashboard and lifecycle setup local to avoid making the
  convention build apply its own build logic. Consider that build boundary
  before extracting additional conventions.
- The reviewed Testmints tracker and commits did not document a deferral for
  Node/npm directory providers equivalent to `NodeExec.setup`. Investigate
  the actual pinned KMP API before deciding whether to defer that item.

- Runner wiring uses the public production binary link-task provider. After clearing the compiler and archive outputs, invoking only `executableJar` scheduled compilation through its output provider. Archive entries and runner code matched the baseline. The fresh link reordered three interface metadata arrays in generated Kotlin stdlib code; normalizing that order made the entire archive content comparison pass. Direct browser execution passed in the combined gate.

- Node executable and npm paths now use the public executable and directory providers, retaining existing setters. The pinned KMP binary makes `NodeJsEnvSpec.env` internal, so populating the legacy `nodeBinDir` field still uses `requireConfigured()`; that field is retained for compatibility. Chrome, Firefox, the three TestKit cases, and the disposable NCU task passed. Plugin checks passed after a lint spacing correction. Configuration-cache entries were stored for WDIO and NCU.

- Package metadata now uses tracked file-content providers. Constraint lookups reuse the parsed JSON while contents are unchanged; dependency-selection reads remain in configuration because the Kotlin npm DSL needs those values there. Template token parsing runs during copying and declares package.json as an input. All 26 generated package/template files and the representative React dependency graph matched the baseline. The new TestKit case verifies cache reuse followed by dependency-version invalidation when package.json changes. Four TestKit cases and plugin/convention checks passed. The temporary coordinate probe was run separately because it captures a project; its initial cache check and a test fixture script capture were corrected during verification.

- Added shared jsdom/Mocha test setup and generated-source lint conventions. After clearing two consumer outputs, lint and format passed with generated sources excluded. Removed the redundant explicit KSP edges; the lint tasks still infer KSP prerequisites from their source inputs, as the dry-run graph confirms. All 26 generated package/template files matched the baseline, and convention checks passed.

- Retained direct React because `DataLoaderProps` and `DataLoader` expose React types. Moved the direct React DOM declaration from main to test, where its imports occur. Compile/test dependency graphs matched the baseline. The POM and module metadata changed only by removing the direct runtime React DOM edge; `minreact` still exports it transitively. DataLoader tests passed.

- Combined gate for the five remaining cleanup items passed on Java 21 and Gradle 9.9.1 with two workers and configuration caching enabled. `assemble check`, four TestKit cases, and fresh Chrome and Firefox runs passed. The working tree contains no temporary probes or generated-source formatting changes.
