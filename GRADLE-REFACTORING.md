# Gradle refactoring initiative

Make the WDIO, KSP, and publishing build logic easier to change in small steps while preserving published plugin APIs, browser behavior, and release ordering. The [maintenance backlog](GRADLE-MAINTENANCE.md) records the preceding work.

## Planned slices

- [ ] Share KSP processor selection between MinReact and Wrapper. Verify local processor execution and published processor coordinates. Keep MinReact's runtime dependency visible in its own script.
- [ ] Add a WDIO regression test for a caller's environment override and the transition back to defaults. Exercise the task through its normal entry point and verify that a changed effective value causes a new run.
- [ ] Move WDIO spec selection, environment assembly, result cleanup, and failure diagnostics into the task. Keep provider wiring in the plugin script. Verify Chrome, Firefox, and configuration-cache reuse with the existing report paths.
- [ ] Migrate internal Node task callers to typed properties and isolate Kotlin's Node/npm setup. Retain legacy setters for published consumers and verify both the npm upgrade and WDIO entry points.
- [ ] Explain the Sonatype signing and publishing exceptions next to their declarations. Investigate whether artifact relationships can express the same ordering. Compare the BOM and processor dry-run graphs before any ordering change.

## Working agreement

Keep each slice independently reviewable. Run focused verification before the Java 21 aggregate build, using configurable fixture ports and Gradle outside the restricted environment for browser runs. Follow the existing release markers for local commits. Record decisions, friction, and any change of direction here with the slice that prompted them.

## Decision log

- Start with KSP because the duplicated behavior is small and both selection paths have existing verification. Establish durable WDIO override coverage before moving launch behavior.
