---
name: seedu-java-coding-standard
description: Apply the SE-EDU basic and intermediate Java coding standard when creating, editing, or reviewing Java source and test code in this project.
---

# Seedu Java Coding Standard

Apply this skill to every Java file in this project, including production code and
tests. Use the [SE-EDU Java coding standard](https://se-education.org/guides/conventions/java/intermediate.html)
as the source of truth; use the [Google Java style guide](https://google.github.io/styleguide/javaguide.html)
for topics not covered there.

## Naming

- Keep package names lowercase; use the project's existing `degreeprogress` root.
- Name classes and enums as nouns in `PascalCase`.
- Name methods as verbs in `camelCase`.
- Name variables in `camelCase` and constants in `SCREAMING_SNAKE_CASE`.
- Do not use all-uppercase abbreviations inside names; use forms such as `Json`,
  `Html`, and `Id`.
- Name boolean variables and methods so they read as booleans, preferably with
  prefixes such as `is`, `has`, `was`, `can`, or `should`. Boolean setters use
  `setName(boolean name)` form.
- Use plural names for collections.
- Name JUnit tests with
  `featureUnderTest_testScenario_expectedBehavior()`. The scenario and expected
  behavior parts may be omitted when the remaining name is unambiguous. Keep
  test names concise.

## Layout and statements

- Use four spaces for indentation and K&R braces.
- Keep lines at or below 120 characters, with a soft target below 110. Wrap
  continuation lines with eight spaces relative to the parent line.
- Keep a method or constructor attached to its opening parenthesis when wrapping
  a declaration or call.
- Put one blank line between logical units in a block.
- Surround operators with spaces, put spaces after commas, and put a space after
  Java reserved words such as `if`, `for`, and `while`.
- Put every class in a package and keep imports explicit and consistently ordered.
- Attach array specifiers to the type, such as `int[] values`.
- Initialize variables where declared and keep them in the smallest possible scope.
- Always use braces for loop and conditional bodies, including single statements.
- Put each conditional on its own line.
- Add an explicit `// Fallthrough` comment for intentional switch fallthrough.

## Comments

- Write comments and Javadocs in English using American spelling.
- Add descriptive header comments for public classes and public methods. Getters,
  setters, overrides with applicable inherited Javadoc, and test code are exempt.
- Follow the guide's Javadoc structure: a short summary first, then any relevant
  details, blank line, and correctly punctuated `@param`, `@return`, and `@throws`
  descriptions.

## Review checklist

Before finishing a Java change, inspect both production and test code for naming,
line length, imports, braces, initialization/scope, and public API comments.
Run the project's test suite after making style-only changes.
