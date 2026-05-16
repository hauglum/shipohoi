# Project Rules

# AI Engineering Rules — Uncle Bob Style

## Core Principles

* Prefer clarity over cleverness.
* Write code for humans first, machines second.
* Every class, method, and variable must have one clear responsibility.
* Small functions are better than large functions.
* Eliminate duplication aggressively.
* Make illegal states impossible when practical.
* Favor composition over inheritance.
* Avoid premature optimization.
* Simplicity is a feature.

---

# Architecture Rules

* Separate business logic from frameworks and infrastructure.
* Keep domain logic independent of Spring, databases, HTTP, and cloud providers.
* Dependencies must point inward toward the domain.
* Use interfaces at architectural boundaries.
* Infrastructure must be replaceable without changing core business rules.
* Prefer explicit architecture over "magic."

---

# Clean Code Rules

## Naming

* Use intention-revealing names.
* Avoid abbreviations unless universally understood.
* Classes should be nouns.
* Methods should be verbs.
* Boolean variables must read naturally:

  * `isActive`
  * `hasAccess`
  * `canRetry`

---

## Functions

* Functions should do one thing only.
* Prefer functions under 20 lines.
* Avoid more than 3 parameters.
* Avoid boolean flag parameters.
* Return early to reduce nesting.
* Replace complex conditionals with expressive methods.

Bad:

```java
if (user != null && user.isActive() && !user.isLocked())
```

Good:

```java
if (user.canLogin())
```

---

## Classes

* Keep classes small and cohesive.
* A class should have only one reason to change.
* Avoid "God objects."
* Avoid utility classes containing unrelated methods.

---

# SOLID Rules

## S — Single Responsibility Principle

One class = one responsibility.

## O — Open/Closed Principle

Extend behavior without modifying stable code.

## L — Liskov Substitution Principle

Subtypes must behave correctly when substituted.

## I — Interface Segregation Principle

Prefer small focused interfaces.

## D — Dependency Inversion Principle

Depend on abstractions, not concretions.

---

# Error Handling Rules

* Fail fast.
* Never swallow exceptions silently.
* Use meaningful exception names.
* Prefer domain exceptions over generic runtime exceptions.
* Logging is not error handling.

---

# Testing Rules

* Write testable code by design.
* Prefer pure functions when possible.
* Avoid hidden dependencies.
* Unit tests must be fast and deterministic.
* Mock only architectural boundaries.
* Do not mock value objects or domain logic.
* If code is difficult to test, simplify the design.

---

# API Rules

* APIs must be predictable and explicit.
* Avoid leaking infrastructure details into API contracts.
* Use stable URLs.
* Avoid exposing internal IDs when unnecessary.
* Prefer readability over compactness.

Example:
Bad:

```text
/report?id=123&sig=abc&tmp=true
```

Good:

```text
/reports/monthly-sales
```

---

# Cloud & Infrastructure Rules

* Infrastructure is a detail, not the architecture.
* Avoid vendor lock-in in core logic.
* Keep cloud SDK usage isolated.
* Never expose signed/internal storage URLs to end users unless absolutely necessary.
* Prefer stable application URLs that redirect internally when needed.

---

# Refactoring Rules

* Leave code cleaner than you found it.
* Refactor before adding complexity.
* Remove dead code immediately.
* Replace conditionals with polymorphism when appropriate.
* Continuously improve naming.

---

# AI Output Rules

When generating code:

* Produce production-quality code.
* Prefer readability over brevity.
* Explain tradeoffs when architecture decisions are involved.
* Include tests for non-trivial logic.
* Avoid unnecessary frameworks or abstractions.
* Do not introduce patterns unless they solve a real problem.
* Avoid speculative generality.
* Prefer immutable objects where practical.

---

# Definition of Done

Code is done when:

* It is readable.
* It is tested.
* It is maintainable.
* It has clear boundaries.
* It hides implementation details.
* Another senior engineer can understand it quickly.
* Future changes will be safe and inexpensive.
