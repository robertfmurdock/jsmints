# Agent Guidance

When writing or reviewing Testmints tests, use `$testmints` if installed. The
[upstream Testmints skill](https://github.com/robertfmurdock/testmints/tree/master/skills/testmints)
can be installed with `$skill-installer`.
For repository-specific test boundaries and conventions, read [TESTING.md](TESTING.md).

Before changing Gradle build logic, check the [Gradle maintenance backlog](GRADLE-MAINTENANCE.md)
for related work. When addressing an item, update the checklist in the same
change with what you verified and any work that remains.

For the follow-up Gradle refactors, see the [refactoring initiative](GRADLE-REFACTORING.md)
and log decisions, verification, and changes of direction alongside the work.

Run `git add` and `git commit` as separate, standalone commands so Codex
can match their saved approvals. Run file edits and verification separately.
