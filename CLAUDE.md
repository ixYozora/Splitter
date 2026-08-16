# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Splitter — a university (ProPra) Spring Boot MVP that tracks shared expenses in groups and computes
the minimal set of settlement transfers. `aufgabe.adoc` is the original assignment spec (German) and
is the authority on required behaviour; `README.adoc` is the user-facing setup guide.

**Everything except the docs lives in `splitter/`** — that subdirectory is the Gradle project root.
Run all build commands from `/home/yozora/Splitter/splitter`.

## Build and test

```bash
cd splitter
./gradlew test                                         # unit + integration + ArchUnit tests
./gradlew check                                        # what CI runs (= test)
./gradlew test --tests 'propra2.splitter.domain.DomainTests'
./gradlew test --tests '*DomainTests.test_01'          # single test method
./gradlew build                                        # compile + test + jar
```

**Toolchain:** Java 25, Gradle 9.7.0, Spring Boot 3.5.16. `build.gradle` declares a Java 25
toolchain, so Gradle must be able to discover a JDK 25 (on this machine
`~/.sdkman/candidates/java/25.0.3-graal`); it fails rather than silently falling back to another JDK.
Gradle 9.1 was the first release able to *run* on Java 25 — older wrappers die with
`Unsupported class file major version 69`, so do not downgrade the wrapper. The pinned distribution
is checksum-verified via `distributionSha256Sum` in `gradle-wrapper.properties`; regenerate both with
`./gradlew wrapper --gradle-version <v> --gradle-distribution-sha256-sum <sum>` rather than by hand.

Tests need Docker but no environment variables: `TestcontainersKonfiguration` starts a
`postgres:15-alpine` container and Spring wires it in via `@ServiceConnection`, so every run
applies the Flyway migrations. Spring caches the context, so one container serves the whole
suite.

## Running the app

Needs a Postgres container and four env vars. `docker-compose.yml` reads all three Postgres values
from the environment (the README's claim that they are written in the compose file is stale).

The datasource URL is assembled in `application.yaml` from `POSTGRES_HOST` (default `localhost`),
`POSTGRES_PORT` (default `5432`) and `POSTGRES_DB` (default `splitter`), so the database name is
free and `bootRun` works without them. Compose sets `POSTGRES_HOST=database`, since inside the
network Postgres answers to its service name.

```bash
cd splitter
POSTGRES_DB=splitter POSTGRES_USER=user POSTGRES_PASSWORD=password docker compose up -d
CLIENT_SECRET=… CLIENT_ID=… POSTGRES_USER=user POSTGRES_PASSWORD=password ./gradlew bootRun
```

The app serves on **port 9000**; GitHub OAuth callback and homepage URL must both be
`http://localhost:9000`. Swagger UI comes from springdoc.

`splitter/run_tests.jar` is the course-provided acceptance checker: start the app, then
`java -jar run_tests.jar` — it drives `http://localhost:9000/api/` with real HTTP calls.

## Architecture

Onion architecture, enforced automatically by `src/test/java/propra2/splitter/ArchTests.java`
(ArchUnit runs as part of `test` via the `archunit-junit5-engine`). Layers under `propra2.splitter`:

- `domain` — domain model, no Spring dependencies.
- `service` — application services (`GruppenService`, `RestGruppenService`) plus the port interface
  `GruppenRepository` and the record/bean types the adapters exchange (`GruppenDetails`,
  `GruppeEntity`, `AusgabeEntity`, `TransaktionEntity`, …).
- `web`, `database` — the two adapters. `web` holds both `WebController` (Thymeleaf UI) and
  `RestController` (`/api/**`); `database` holds the Spring Data JDBC implementation.
- `config` — every `@Configuration` class must live here.
- `stereotypes` — the DDD marker annotations `@AggregateRoot`, `@Entity`, `@Wertobjekt`.

Rules that will break the build if violated: no `@Autowired`/`@Value` **fields** (constructor
injection only); `@Configuration`/`@Service`/`@Controller`/`@Repository` classes must sit in
`config`/`service`/`web`/`database` respectively; controllers must not depend on `..database..`;
nothing may be `@Deprecated`; exactly one `@AggregateRoot` per domain slice; every `domain` class
needs a DDD stereotype annotation; and **only the aggregate root may be public in `domain`**
(explicit exceptions: `AusgabenDetails`, `TransaktionDetails`).

ArchUnit 1.x fails any rule whose selector matches nothing (`failOnEmptyShould`), so a mistyped
package pattern surfaces as a failing test instead of a silently vacuous rule — note that slice
patterns only produce slices via a `(*)`/`(**)` capturing group.

That last rule shapes the domain API: `Gruppe` is the only public domain type, `Person`, `Ausgabe`,
`Transaktion`, `Aktivitaet` and `PersonComparator` are package-private, and data leaves the aggregate
only as the public records `AusgabenDetails` / `TransaktionDetails` (`getAusgabenDetails()`,
`getTransaktionDetails()`). Adding a public class to `domain` fails `ArchTests`.

### Domain

`Gruppe` is the aggregate root and owns members, expenses and computed transactions. Money is
JavaMoney/Moneta `Money`, always `"EUR"`. `berechneTransaktionen()` computes each member's net
balance (expenses paid minus share owed), then recursively settles the largest creditor against the
largest debtor to keep transaction count low; callers clear first (`clearTransaktionen()`) so
recomputation is idempotent — `GruppenService.transaktionBerechnen` does exactly this.

Business rules live in the aggregate: `addPerson` refuses once `ausgabeGetaetigt` is true or the
group is closed, and `addAusgabeToPerson` refuses on a closed group. `addPersonAlways`,
`addAusgabe` and `addTransaktion` deliberately bypass those checks — they exist **only** so the
persistence adapter can rehydrate a stored group, and must not be called from services.

### Persistence

Spring Data JDBC (not JPA). `database/*DTO` records mirror the tables one-to-one and are mapped to
the aggregate by hand in `GruppenRepositoryImpl` (`toGruppe` / `fromGruppe`); the domain model itself
carries no persistence annotations. IDs are `UUID` and are generated by the database (a new group is
saved with `id == null`).

Flyway is on version 11, where per-database support is a separate artifact — `flyway-core` alone
cannot migrate Postgres, hence the `flyway-database-postgresql` runtime dependency. Tests never
exercise this too, since the tests now run the migrations against Postgres.

The schema lives only in `src/main/resources/db/migration/` and Flyway applies it in tests as
well as at runtime. `src/test/resources/database/gruppe_insert.sql` supplies fixtures on top of
it via `@Sql`.

### Web and REST

The Thymeleaf UI is fully authenticated via GitHub OAuth2; the logged-in user is read from
`OAuth2AuthenticationToken` as the `login` attribute, and group membership is matched on that string.
`WebSecurityKonfiguration` **excludes `/api/**` from the security filter chain entirely**, so the
REST adapter is unauthenticated by design.

The REST side is a parallel stack (`RestController` → `RestGruppenService` → same `GruppenRepository`)
and speaks a different money unit: JSON uses integer **cents** (`AusgabeEntity.cent`,
`TransaktionEntity.cents`) while the domain uses EUR `Money`; conversion happens in
`RestGruppenService`. Endpoints: `GET /api/user/{githublogin}/gruppen`, `POST /api/gruppen`,
`GET /api/gruppen/{id}`, `POST /api/gruppen/{id}/schliessen`, `POST /api/gruppen/{id}/auslagen`,
`GET /api/gruppen/{id}/ausgleich`.

## Testing conventions

- German `@DisplayName` on every test, methods named `test_01`, `test_02`, … Follow this.
- Controller tests: `@WebMvcTest(controllers = X.class)` + `@Import(WebSecurityKonfiguration.class)`,
  service `@MockitoBean` (not the removed `@MockBean`). Authentication comes from the local
  `@WithMockOAuth2User(login = "MaxHub")` annotation in `propra2.splitter.helper`; POSTs need
  `.with(csrf())`.
- Several UI tests assert on **exact HTML substrings** (e.g. `GruppenAnzeigeTest` matches the literal
  `<form method="post" action="/add">` and the full `<input …>` tag). Editing `index.html` or
  `gruppe.html` markup — including attribute order or spacing — will break them.
- Repository tests: `@SpringBootTest` with `@Import(TestcontainersKonfiguration.class)` and
  `@Transactional`, so each test rolls back. `GruppeRepositoryImplTest` needs no Spring at all —
  it mocks `SpringDataGruppeRepository` to test the DTO mapping.
- Test fixtures use GitHub-style names (`MaxHub`, `GitLisa`, `ErixHub`) and group `Reisegruppe`.

## Style

Production code follows the Google Java Style Guide (2-space indent, 100-col) — required by the
assignment; JavaDoc is not required. Two tasks enforce this and both run as part of `check`:
Spotless (`spotlessApply` reformats, `spotlessCheck` verifies) and Checkstyle. They divide the work
— google-java-format owns layout, Checkstyle owns what a formatter cannot see, above all wildcard
imports. The Checkstyle config is deliberately **not** `google_checks.xml`: that reports 325
violations here, of which the test naming (`test_01`), the absent JavaDoc and the digit in package
`propra2` are all project decisions. `splitter/config/checkstyle/checkstyle.xml` keeps only the
rules that survive that, and `maxWarnings = 0` makes any violation fail the build.

Identifiers and comments are German; keep new code in the same vocabulary:

Gruppe = group · Person/Teilnehmer = member/participant · Ausgabe = expense · Ausleger/Gläubiger =
payer who fronted the cost · Schuldner = debtor · Zahler/Zahlungsempfänger = sender/recipient of a
transfer · Transaktion = settlement transfer · Ausgleich = settlement · Kosten/Betrag = cost/amount ·
geschlossen = closed · ausgabeGetaetigt = an expense has been recorded.
