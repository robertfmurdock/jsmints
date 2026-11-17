# Gradle maintenance backlog

## Problem

The Gradle audit found deferred configuration, eager task configuration, and wrapper and repository maintenance work across the JSMints composite. Each change can be reviewed, verified, and released independently.

## Expected changes

- [ ] **High — remove six `afterEvaluate` callbacks.** Migrate the dependency BOM, plugin publishing build, reports and publishing conventions, and MinReact and wrapper KSP plugins one at a time. Use plugin or configuration callbacks and providers. Check publication metadata, report wiring, and local and published processor resolution as applicable. [Gradle guidance](https://docs.gradle.org/current/userguide/best_practices_general.html#avoid_after_evaluate)
  - Migrated Gradle plugin and convention POM metadata to publication callbacks. The regenerated plugin and BOM POMs matched byte-for-byte on Gradle 9.9.1. Publishing task ordering and the KSP callbacks remain.
  - Removed the reports callback and switched the convention and plugin project to a single `collectResults` copy task per project. All 1,848 files under the aggregate CI artifact path matched byte-for-byte before and after.
- [ ] **High — keep WDIO configuration lazy.** Model environment values, paths, arguments, and reporter flags through task properties. Resolve concrete values when the task needs them and preserve input tracking for values that affect results. [Gradle guidance](https://docs.gradle.org/current/userguide/best_practices_tasks.html#avoid_provider_get_outside_task_action)
- [x] **High — move WDIO log directory creation to execution.** The Node task creates the parent of its declared log output immediately before opening the stream. A fixture `e2eRun --dry-run` with a fresh build directory left the log directory absent. [Gradle guidance](https://docs.gradle.org/current/userguide/best_practices_performance.html#avoid_computations_in_configuration_phase)
- [x] **Medium — declare the WDIO fixture root name.** The standalone `projects` task confirmed `wdio-testing-library-test` and all three included builds on Gradle 9.9.1. [Gradle guidance](https://docs.gradle.org/current/userguide/best_practices_general.html#name_your_root_project)
- [x] **Medium — centralize dependency repositories.** Moved repository declarations in the convention build, libraries (including the BOM and processors), plugin implementation build, and WDIO fixture to their respective settings. Kept the published WDIO plugin’s Maven Central default for consumers. Java 21 `assemble check` passed, including Chrome and Firefox. [Gradle guidance](https://docs.gradle.org/current/userguide/best_practices_dependencies.html#set_up_repositories_in_settings)
- [ ] **Medium — declare WDIO task cacheability on the type.** Replace `outputs.cacheIf { true }` with `@CacheableTask` only once all result-affecting inputs are modeled and reuse is verified. Otherwise use `@DisableCachingByDefault` with the reason. [Gradle guidance](https://docs.gradle.org/current/userguide/best_practices_tasks.html#use_cacheability_annotations)
- [x] **Medium — align the checked-in wrappers.** Regenerated the libraries wrapper at Gradle 9.9.1 with the published binary distribution checksum. Standalone `tasks` passed before and after. The root `:libraries:tasks` passed after, and the new JAR matched Gradle’s published hash. [Version guidance](https://docs.gradle.org/current/userguide/best_practices_general.html#use_latest_minor_versions) · [Wrapper validation](https://docs.gradle.org/current/userguide/best_practices_security.html#validate_wrapper_checksum)
- [x] **Recommendation — switch the root wrapper to `bin`.** Regenerated the Gradle 9.9.1 wrapper and updated the scheduled workflow to request `bin`. Pinned the published distribution checksum, verified the wrapper JAR against Gradle’s published hash, and ran `tasks` before and after. [Gradle guidance](https://docs.gradle.org/current/userguide/best_practices_performance.html#prefer_bin_distribution)
- [ ] **Recommendation — reduce reliance on generated Kotlin task names in WDIO.** Use public Kotlin compilation APIs or task providers where available. Verify that invoking `e2eRun` directly still schedules every prerequisite. [Gradle guidance](https://docs.gradle.org/current/userguide/best_practices_tasks.html#dont_hardcode_task_names)

## Verification and release

- Keep commits focused on one independently verifiable change. Update this checklist in the same change as the fix. Check off an item after verification and record the check that passed. For partial work, record what remains under the open item. Split the six callbacks across commits where possible.
- Run focused checks first, then the repository’s normal `assemble check` CI path on Java 21. For WDIO task changes, exercise the existing TestKit suite and fixture, and check configuration-cache behavior separately since the main build and release steps pass `--no-configuration-cache`. For publishing changes, compare generated metadata and verify consumer resolution before merging.
- Follow the existing commit and version conventions (`[none]` for non-release work and `[patch]` for backward-compatible fixes where appropriate). Let the existing fingerprint, version calculation, and release workflow determine and publish releases. Keep publishing on the existing isolated CI path with scoped credentials.

The audit was static. Runtime behavior and wrapper binary integrity are verification work for the relevant changes.

## Verification checkpoints

- November 17, 2026 — Java 21 and Gradle 9.9.1 passed `assemble check` and the two WDIO TestKit cases. The WDIO fixture passed in Chrome and Firefox with Gradle running outside the restricted environment. After switching from Java 22, rerunning convention-plugin validation with `--rerun-tasks --no-build-cache -Pkotlin.compiler.execution.strategy=in-process` rebuilt the generated classes for Java 21. Run `clean` separately from `assemble check` to avoid the composite task scheduling failure observed when combining them.
