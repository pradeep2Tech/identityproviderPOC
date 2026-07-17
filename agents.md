# Repository Instructions

## Rule

- Do not write, generate, or add test cases to this project.

## Efficient repository navigation

Read `docs/CODEBASE_GUIDE.md` before scanning source files. It contains the project architecture, request flow, configuration map, and the authoritative file for each concern.

When making a change:

1. Use the guide to identify the smallest relevant file set.
2. Read only those files and their direct dependencies.
3. Update the guide when architecture, routes, configuration, security behavior, or deployment instructions change.
4. Verify production code with `mvn clean package`; do not introduce tests as part of verification.
