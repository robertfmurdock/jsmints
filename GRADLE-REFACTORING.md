# Gradle refactoring initiative

Make the WDIO, KSP, and publishing build logic easier to change in small steps while preserving published plugin APIs, browser behavior, and release ordering. The [maintenance backlog](GRADLE-MAINTENANCE.md) records the preceding work.

## Planned slices

- [x] Share KSP processor selection between MinReact and Wrapper. Verify local processor execution and published processor coordinates. Keep MinReact's runtime dependency visible in its own script.
- [x] Add a WDIO regression test for a caller's environment override and the transition back to defaults. Invoke the registered `WdioTest` task through Gradle and verify that a changed effective value causes a new run.
- [x] Move WDIO spec selection, environment assembly, result cleanup, and failure diagnostics into the task. Keep provider wiring in the plugin script. Verify Chrome, Firefox, and configuration-cache reuse with the existing report paths.
- [x] Migrate internal Node task callers to typed properties and isolate Kotlin's Node/npm setup. Retain legacy setters for published consumers and verify both the npm upgrade and WDIO entry points.
- [x] Explain the Sonatype signing and publishing exceptions next to their declarations. Investigate whether artifact relationships can express the same ordering. Compare the BOM and processor dry-run graphs before any ordering change.

## Working agreement

Keep each slice independently reviewable. Run focused verification before the Java 21 aggregate build, using configurable fixture ports and Gradle outside the restricted environment for browser runs. Follow the existing release markers for local commits. Record decisions, friction, and any change of direction here with the slice that prompted them.

## Decision log

- Start with KSP because the duplicated behavior is small and both selection paths have existing verification. Establish durable WDIO override coverage before moving launch behavior.

- Shared the configuration names and lazy local-versus-published selection. The helper accepts a callback so it reads the consumer's current `includedBuild` property when Gradle needs the dependency. Both local fixture checks and the external-coordinate probe passed.

- The override regression launches a small Java program through the real WDIO task action and reads the subprocess environment from its log. This isolates the Gradle contract from browser and Docker setup. Removing the override reran the task, and repeating the default run reused the configuration cache and left the task up-to-date. All three TestKit tests and functional-test lint passed.

- Moved spec selection, test-result cleanup, and report diagnostics into `WdioTest`. Its report directories are typed outputs. Gradle treated the first attempt to feed a task output provider back into its own diagnostic input as a dependency on the task itself. Using the same layout providers for both sides removed the cycle. Chrome, Firefox, and TestKit passed. The repeat run reused the configuration cache and generated reports in the existing locations.

- Moved internal Node execution and npm upgrade to the typed properties. Kept the existing `setup` entry point and legacy setters for published callers, and consolidated Node/npm lookup there. The standalone npm upgrade probe needed Maven Central in its settings, matching the plugin's consumer-owned repository setup. It then ran against a disposable package without modifying it. Java 21 `assemble check`, all three TestKit cases, Chrome, and Firefox passed together.

- The signing probe showed one Javadoc JAR shared by the BOM and its two Kotlin publications, and another shared by the wrapper processor's JVM and multiplatform publications. The BOM's disabled Kotlin publish task still schedules its signing prerequisites. Documented the existing edges and kept them. Each publication would need its own signing output path before simplifying the ordering. The Sonatype dry runs selected the same 48 tasks in the same order before and after the comments. Signing and publishing execution were outside this slice.

## Consolidation slices

- [x] Extract a shared processor convention for publishing, the Java 21 toolchain, JVM target, and processor dependencies. Compare publication metadata and run both generated-code fixtures.
- [x] Define the Plugin Portal routing groups once in convention settings. Compare compile and runtime dependency resolution.
- [ ] Give every Maven publication its own Javadoc archive and signature path. Remove cross-publication signing dependencies, verify local Maven artifacts and detached signatures, and inspect the Sonatype dry-run graph before and after.

- The processor convention reads the consuming libraries build’s KSP API catalog entry. Both generated-code fixtures and convention-plugin checks passed. The two JVM processor POMs and dependency BOM POM matched the baseline byte-for-byte.

- Maven Central exclusions and Plugin Portal inclusions now use one local group list. Both plugin builds’ compile and runtime dependency graphs matched the previous baseline.
