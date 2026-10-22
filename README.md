# Karate API Automation Framework

[![API Tests](https://github.com/RealOrji/karate-framework/actions/workflows/api-tests.yml/badge.svg)](https://github.com/RealOrji/karate-framework/actions/workflows/api-tests.yml)

An API test automation framework built on [Karate](https://karatelabs.io). Every automated scenario maps to a test case in a manual test case register, and a build-time check keeps the two in sync.

The sample suites target two public APIs:

| API | What it exercises |
|---|---|
| [Restful-Booker](https://restful-booker.herokuapp.com/apidoc/index.html) | Token and Basic auth, full CRUD, filtering, negative and security cases, an end-to-end journey with real persisted data |
| [JSONPlaceholder](https://jsonplaceholder.typicode.com) | Contract and schema validation, data-driven reads, fake writes |

**Stack:** Java 17 · Karate 1.5.2 · JUnit 5 · Maven (wrapper included) · Cucumber HTML reports · GitHub Actions

---

## Quick start

```bash
git clone https://github.com/RealOrji/karate-framework.git
cd karate-framework
./mvnw test                      # full regression against the qa environment
```

You only need JDK 17 or newer. The Maven wrapper downloads Maven itself.

Reports are written to:

- `target/karate-reports/karate-summary.html`: Karate report with every request and response
- `target/cucumber-html-reports/overview-features.html`: Cucumber dashboard (features, tags, trends)
- `target/traceability-report.md`: manual test case ↔ automation matrix

## Running tests

| Goal | Command |
|---|---|
| Full regression | `./mvnw test` |
| Smoke only | `./mvnw test -Dkarate.options="--tags @smoke"` |
| One manual test case | `./mvnw test -Dkarate.options="--tags @TC-BKG-001"` |
| Several test cases (OR) | `./mvnw test -Dkarate.options="--tags @TC-BKG-001,@TC-AUTH-002"` |
| Positive booking tests (AND) | `./mvnw test -Dkarate.options="--tags @booking --tags @positive"` |
| Everything except negatives | `./mvnw test -Dkarate.options="--tags ~@negative"` |
| A single feature file | `./mvnw test -Dkarate.options="classpath:features/booker/auth.feature"` |
| Another environment | `./mvnw test -Dkarate.env=staging` |
| More parallelism | `./mvnw test -Dthreads=10` |
| Quieter console | `./mvnw test -Dkarate.log.level=INFO` |
| Traceability check only (offline) | `./mvnw test -Dtest=TraceabilityTest` |

In an IDE, run `BookerRunner` or `PlaceholderRunner` to execute one API's features. `ApiTestSuite` is the parallel runner that CI uses.

## Project structure

```
karate-framework/
├── pom.xml
├── mvnw, mvnw.cmd, .mvn/                  # Maven wrapper
├── test-cases/
│   └── manual-test-cases.csv              # manual test case register (source of truth)
├── .github/
│   ├── workflows/api-tests.yml            # CI/CD pipeline
│   └── dependabot.yml
└── src/test/java/
    ├── karate-config.js                   # environments, credentials, global helpers, timeouts
    ├── logback-test.xml
    ├── com/realorji/api/
    │   ├── ApiTestSuite.java              # parallel runner + report generation (CI entry point)
    │   ├── support/
    │   │   ├── DataFactory.java           # thread-safe test data generators
    │   │   ├── AuthHeaders.java           # Basic auth header builder
    │   │   ├── SensitiveDataMasker.java   # masks passwords, tokens, cookies in logs and reports
    │   │   └── CucumberReport.java        # Cucumber HTML report generation
    │   └── traceability/
    │       ├── TraceabilityTest.java      # enforces register ↔ feature mapping
    │       ├── ManualTestCase.java
    │       └── FeatureScenario.java
    ├── features/                          # executable test suites, one folder per API
    │   ├── booker/
    │   │   ├── health.feature
    │   │   ├── auth.feature
    │   │   ├── booking-lifecycle.feature  # end-to-end journey
    │   │   └── booking/
    │   │       ├── create-booking.feature
    │   │       ├── get-booking.feature
    │   │       ├── update-booking.feature
    │   │       └── delete-booking.feature
    │   └── placeholder/
    │       ├── users.feature
    │       └── posts.feature
    ├── common/                            # reusable building blocks (@ignore, called by features)
    │   ├── booker/
    │   │   ├── get-token.feature
    │   │   ├── create-booking.feature
    │   │   ├── delete-booking.feature
    │   │   ├── build-booking.js           # payload builder with overrides
    │   │   └── cleanup-booking.js         # afterScenario test data cleanup
    │   └── placeholder/build-post.js
    ├── schemas/                           # Karate fuzzy-match schemas (contract tests)
    └── data/                              # CSV / JSON data for Scenario Outlines
```

## Manual test case mapping

`test-cases/manual-test-cases.csv` is the test case register. It opens in Excel or Google Sheets and can be imported into TestRail, Xray or Zephyr.

| Column | Example |
|---|---|
| `test_case_id` | `TC-BKG-001` |
| `module` | `Booking - Create` |
| `title` | `Create a booking with a valid payload and verify it is persisted` |
| `priority` | `P1` … `P3` |
| `type` | `Positive`, `Negative`, `Security`, `Contract`, `Data-driven`, `End-to-end`, `Performance` |
| `preconditions`, `steps`, `expected_result` | Written for a manual tester |
| `automation_status` | `Automated` or `Manual` |
| `automated_in` | `features/booker/booking/create-booking.feature` |

Each automated scenario carries its test case ID as a tag:

```gherkin
@TC-BKG-001 @smoke @positive @contract
Scenario: Create a booking with a valid payload and verify it is persisted
```

`TraceabilityTest` runs offline in under a second and fails the build when:

- a runnable scenario has no `@TC-*` tag, or has more than one
- a `@TC-*` tag is not in the register
- two scenarios automate the same test case
- a case marked `Automated` has no scenario, or lives in a different file than `automated_in` says
- a case marked `Manual` is in fact automated, so its status is stale
- the register contains duplicate IDs

It also writes a coverage matrix to `target/traceability-report.md`. CI adds that matrix to the job summary.

### ID prefixes

| Prefix | Area |
|---|---|
| `TC-HLT` | Health checks |
| `TC-AUTH` | Authentication |
| `TC-BKG` | Bookings (create, read, update, delete) |
| `TC-E2E` | End-to-end journeys |
| `TC-SEC` / `TC-PERF` | Security and performance (currently manual) |
| `TC-USR` / `TC-PST` | JSONPlaceholder users and posts |

## Tag taxonomy

| Tag | Meaning |
|---|---|
| `@TC-xxx-nnn` | Link to the manual test case (exactly one per scenario) |
| `@smoke` | Fast critical-path checks, run on every pull request |
| `@regression` | Set at feature level, so every runnable scenario has it |
| `@positive` / `@negative` | Happy path or error handling |
| `@security` | Authorization and access-control checks |
| `@contract` | Response schema validation |
| `@data-driven` | Scenario Outline fed from `data/` |
| `@e2e` | Multi-step business journey |
| `@booker`, `@placeholder`, `@booking`, `@auth`, … | Set at feature level to slice by API or module |
| `@ignore` | Reusable feature that is only called, never run directly |
| `@wip` | Work in progress, excluded from runs by default |

## Writing a new test

1. Add a row to `test-cases/manual-test-cases.csv` with `automation_status = Automated`.
2. Add the scenario with the matching `@TC-*` tag, and reuse the building blocks:

```gherkin
@TC-BKG-020 @positive
Scenario: Booking without a deposit can be paid later
  * def created = call read('classpath:common/booker/create-booking.feature') { payload: '#(buildBooking({ depositpaid: false }))' }
  * def bookingId = created.bookingId

  Given path 'booking', bookingId
  And cookie token = token
  And request { depositpaid: true }
  When method patch
  Then status 200
  And match response == karate.merge(created.booking, { depositpaid: true })
```

3. Run `./mvnw test -Dkarate.options="--tags @TC-BKG-020"`.

### Reusable building blocks

| Block | Use it for |
|---|---|
| `buildBooking(overrides)` | A valid, unique booking payload; override any field |
| `buildPost(overrides)` | A JSONPlaceholder post payload |
| `DataFactory.*` | `uniqueName('Guest')`, `dateFromToday(7)`, `price(50, 500)`, `randomLetters(8)` |
| `AuthHeaders.basic(user, pass)` | Basic auth header value |
| `common/booker/get-token.feature` | Auth token, shared by the whole run through `karate.callSingle` |
| `common/booker/create-booking.feature` | Create a booking and get its `bookingId` back |
| `common/booker/cleanup-booking.js` | `afterScenario` hook that deletes the scenario's booking, even when the scenario fails |
| `schemas/**` | Fuzzy-match contracts, e.g. `match response == read('classpath:schemas/booker/booking.json')` |

### Conventions

- **Independent scenarios.** Every scenario creates its own data and cleans it up afterwards, so scenarios can run in parallel and in any order.
- **Unique data.** Shared environments contain other people's data, so names come from `DataFactory.uniqueName` and searches only ever find our own records.
- **Asserting full bodies.** Prefer `match response == expected` over checking fields one by one, and assert schema and values separately.
- **Secrets stay out of reports.** Credentials come from `karate-config.js` and are never passed as call arguments, because Karate writes call arguments into the reports. `SensitiveDataMasker` masks `Authorization`, `Cookie`, `password` and `token` in HTTP logs.

## Configuration

Every setting is resolved in this order: `-D` system property, then environment variable, then the default in `karate-config.js`.

| Setting | System property | Env variable | Default |
|---|---|---|---|
| Environment | `karate.env` | – | `qa` |
| Restful-Booker URL | `booker.url` | `BOOKER_URL` | `https://restful-booker.herokuapp.com` |
| JSONPlaceholder URL | `placeholder.url` | `PLACEHOLDER_URL` | `https://jsonplaceholder.typicode.com` |
| Username / password | `booker.username` / `booker.password` | `BOOKER_USERNAME` / `BOOKER_PASSWORD` | Restful-Booker's public demo credentials |
| Response time budget (ms) | `max.response.time` | `MAX_RESPONSE_TIME` | `5000` |
| Parallel threads | `threads` | – | `5` |

To add an environment, add a block to the `environments` map in `karate-config.js` and an option to the workflow's `environment` input.

## CI/CD

`.github/workflows/api-tests.yml` runs these jobs:

| Job | What it does |
|---|---|
| **Test case traceability** | Runs `TraceabilityTest` and adds the coverage matrix to the job summary. It is offline and fast, and it gates the API tests. |
| **Karate API tests** | Runs the suite in parallel, publishes JUnit results as a check, writes a results summary and uploads the HTML reports as an artifact. |
| **Publish reports to GitHub Pages** | Deploys the latest `main` report to GitHub Pages. This job is opt-in (see below). |

| Trigger | Scope |
|---|---|
| Pull request | `@smoke` |
| Push to `main` | Full regression |
| Nightly (06:00 UTC, weekdays) | Full regression |
| Manual (**Run workflow**) | Choose the environment, tags (for example `@TC-BKG-001`) and threads |

**Optional setup**

- Repository secrets `BOOKER_USERNAME` / `BOOKER_PASSWORD`: when they are unset, the demo defaults are used.
- Report publishing: under **Settings → Pages**, set **Source** to **GitHub Actions**. Then add the repository variable `PUBLISH_REPORTS=true` under **Settings → Secrets and variables → Actions → Variables**.

Dependabot opens weekly, grouped update PRs for Maven dependencies and GitHub Actions.
