# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Manatoko is a self-contained UI test suite for the [HAL management console](https://hal.github.io) (WildFly/JBoss EAP). Tests use Testcontainers to spin up WildFly + HAL containers, Arquillian Graphene/Drone for browser automation via Selenium, and JUnit 5.

Java 17+, Maven 3.9.9+. Uses the Maven wrapper (`./mvnw`).

## Build & Test Commands

```shell
# Build without tests (CI verification)
./mvnw -P all-tests -DskipTests verify

# Run ALL tests (remote browser in container, default)
./mvnw test -P all-tests

# Run ALL tests with local browser
./mvnw test -P all-tests,local

# Run tests for a single module
./mvnw test -P all-tests --projects test-configuration-systemproperty --also-make

# Run a single test class
./mvnw test -P all-tests --projects test-configuration-systemproperty --also-make \
    -Dtest=org.jboss.hal.testsuite.configuration.systemproperty.SystemPropertyTest

# Run a single test method
./mvnw test -P all-tests --projects test-configuration-systemproperty --also-make \
    -Dtest=org.jboss.hal.testsuite.configuration.systemproperty.SystemPropertyTest#create

# Debug a test (attach debugger to port 5005)
./mvnw test -P all-tests --projects <module> --also-make -Dmaven.surefire.debug
```

## Code Formatting & Validation

```shell
# Format code (license headers, code style, import sorting)
./format.sh

# Validate code (enforcer, checkstyle, license, formatter, import sort)
./validate.sh
```

Formatting config lives in `code-parent/pom.xml` using resources from `build-config/src/main/resources/manatoko/`. Import sort order: `java., javax., org., io., com.` with static imports after.

## Container Images

Default images (override with `-D` properties):
- HAL: `quay.io/halconsole/hal:latest` (`-Dhal.image=...`)
- WildFly standalone: `quay.io/halconsole/wildfly:latest` (`-Dwildfly.standalone.image=...`)
- WildFly domain: `quay.io/halconsole/wildfly-domain:latest` (`-Dwildfly.domain.image=...`)

## Architecture

### Module Dependency Chain

`environment` → `management-model` → `container` → `junit` → `ui` → `arquillian` / `fixture` / `command` → `test-parent` → `test-*` modules

- **environment**: Singleton managing test mode (local browser vs remote Testcontainers browser)
- **management-model**: DMR/Creaper utilities for WildFly management operations (`ResourceVerifier`, `ModelNodeGenerator`, `Operations`, address builders)
- **container**: `WildFlyContainer` (extends Testcontainers `GenericContainer`), `HalContainer`, `Browser` setup, `WildFlyConfiguration` enum (DEFAULT, FULL, FULL_HA, HA, etc.)
- **junit**: `@Manatoko` composite annotation (activates `SystemSetupExtension` + `ArquillianExtension` + VNC recording), custom JUnit 5 extensions
- **ui**: Arquillian page objects (`*Page`) and fragments (`FormFragment`, `TableFragment`, `FinderFragment`, etc.), `CrudOperations` helper, `Console` utilities
- **fixture**: Test constants and addresses per subsystem (`*Fixtures.java`), names and DMR addresses for create/read/update/delete test data
- **command**: Creaper commands to set up management resources before tests (e.g., `AddKeyStore`, `AddMessagingServer`, `AddSocketBinding`)
- **arquillian**: Testcontainers-Arquillian bridge (`ManatokoExtension`, `TestcontainersWebDriverFactory`)
- **test-parent**: Parent POM for all test modules, pulls in all framework dependencies
- **test-configuration-\***: Tests for WildFly subsystem configuration (one module per subsystem)
- **test-runtime-\***: Tests for runtime views

### Test Modules Are NOT Built by Default

Test modules (`test-*`) are only activated with `-P all-tests`. The root POM's default `<modules>` only includes framework modules.

### Writing a Test

Every test class needs two annotations in order:
1. `@Manatoko` — activates JUnit extensions for Testcontainers + Arquillian
2. `@Testcontainers` — manages `@Container`-annotated fields

Inject a `WildFlyContainer` as a static `@Container` field using either `WildFlyContainer.standalone(WildFlyConfiguration)` or `WildFlyContainer.domain()`. Inject page objects with `@Page` and helpers with `@Inject`.

### Key Testing Patterns

- `CrudOperations` provides create/read/update/delete helpers that interact with UI forms/tables and verify the management model
- `ResourceVerifier` checks that DMR operations against the management model return expected values
- `*Fixtures` classes define test resource names, addresses, and values as constants
- Tests navigate to pages, interact with UI fragments, and verify both UI state and management model changes

## CI Workflows

- `verify.yml` — runs on push/PR, builds with `-DskipTests verify`
- `test-all.yml` — triggered by `gh-test-all.sh`, runs all test modules in parallel
- `test-single.yml` — triggered by `gh-test-single.sh`, runs one test module
- `test-scheduled.yml` — nightly run of all tests
