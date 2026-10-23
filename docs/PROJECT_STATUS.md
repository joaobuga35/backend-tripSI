# TripSi — Project Status

> **Last updated**: 2026-10-23 (Sprint 1 — COMPLETE, zero known issues, on `feature/project-setup`, not yet merged to `dev`)
> **Current Sprint**: Sprint 1 (Oct 13–19) — Foundation + User + Auth

---

## Resolved Issues

- **`ModularityTests` — FIXED.** Root cause was never really "2.1.1 is broken" — the entire `spring-modulith` **2.x line targets Spring Boot 4.x** (same generation-pairing convention as Spring Security 6.x/Boot 3.x vs Security 7.x/Boot 4.x). `2.x`'s `ApplicationModuleDetectionStrategyLookup` calls `ConfigurableBootstrapContext` from package `org.springframework.boot.bootstrap` — a package that only exists in Boot 4.x; Boot 3.5.16 still has that class in `org.springframework.boot` (no `.bootstrap`), hence `NoSuchMethodError`. Downgrading to `2.0.9` reproduced the identical error (confirming it's a generation mismatch, not a patch-level bug) — the correct fix was the **1.x line**, whose latest stable is `1.4.14`. Switching to `1.4.14` surfaced two more real, previously-hidden issues, both fixed:
  1. **Named interface violation**: once `shared.security` and `shared.exception` are explicitly marked with `@NamedInterface` (needed for `JwtPrincipal`/`JwtTokenProvider`/`ResourceNotFoundException`/`BusinessRuleException` to be visible outside `shared`), the bare `"shared"` entry in `user`'s `allowedDependencies` stopped covering named interfaces. Fixed by listing them explicitly: `allowedDependencies = {"shared", "shared :: security", "shared :: exception"}` in `user/package-info.java`.
  2. **Missing `event_publication` table**: `spring-modulith-starter-jpa` (present since Day 1 scaffolding, for ADR-005) registers its own JPA entity for tracking published domain events — Hibernate's `validate` mode failed because no Flyway migration created it. Added `V3__create_event_publication_table.sql` using Spring Modulith's official PostgreSQL reference schema (legacy variant — matches the columns present in 1.4.14's `JpaEventPublication`: `id, listener_id, event_type, serialized_event, publication_date, completion_date`).
  - Also fixed a **flaky test** found while re-running the suite repeatedly: `JwtTokenProviderTest.should_return_false_when_token_is_tampered` flipped the *last* character of the token, which sometimes lands on an unused padding bit in base64url and silently fails to change the decoded bytes. Now flips a character in the middle of the token instead (deterministic across 15+ consecutive runs).
- **Full suite: 63/63 tests passing, `mvn test` → `BUILD SUCCESS`.**

---

## What Has Been Done

### Architecture & Planning (Pre-Sprint)
- [x] Architecture decisions documented as 10 ADRs in CLAUDE.md
- [x] C4 Model diagrams created (docs/C4_MODEL.md)
- [x] Class diagrams created (docs/CLASS_DIAGRAM.md)
- [x] Sprint plan with 4 sprints, story points, and ceremonies (docs/SPRINT_PLAN.md)
- [x] Hexagonal architecture fully defined with primary ports (port/in/) and secondary ports (port/out/)
- [x] Module structure with 5 modules: user, suggestion, flight, recommendation, planner
- [x] Event flow defined: suggestion → flight → recommendation → planner

### Project Scaffolding (Sprint 1, Day 1 — SPRINT_PLAN #1–3)
- [x] pom.xml with all dependencies (Spring Boot 3.5.16, Spring AI 1.1.8, Spring Modulith 2.1.1, Java 21)
- [x] Full hexagonal package structure for all 5 modules
- [x] package-info.java for each module with Spring Modulith allowedDependencies
- [x] TripSiApplication.java with @Modulithic annotation
- [x] application.yml + profiles (dev, test, prod)
- [x] logback-spring.xml (text dev, JSON prod with correlationId)
- [x] docker-compose.yml with PostgreSQL 16 Alpine
- [x] .env.example with all required variables
- [x] .gitignore
- [x] V1 Flyway migration (uuid-ossp extension)
- [x] ModularityTests.java (Spring Modulith boundary verification)
- [x] TripSiApplicationTests.java (context load test)
- [x] GitHub repository created (`origin` → joaobuga35/backend-tripSI), `main` + `dev` branches
- [x] Setup commits landed on `dev` (`chore: initial config`, `chore: env and docker compose config`)

### Shared / Cross-Cutting (Sprint 1, Day 2 — SPRINT_PLAN #4, branch `feature/project-setup`)
- [x] `GlobalExceptionHandler` (`shared/exception/`) — `@RestControllerAdvice` returning RFC 9457 `ProblemDetail` for `ResourceNotFoundException` (404), `BusinessRuleException` (422), `BadCredentialsException` (401), `AccessDeniedException` (403), bean validation errors (400, field-level), and a catch-all (500) that logs full detail server-side only
- [x] `ResourceNotFoundException`, `BusinessRuleException` (`shared/exception/`)
- [x] Unit tests for `GlobalExceptionHandler` (5/5 passing) — `@Valid` validation branch deliberately deferred to the AuthController MockMvc tests (SPRINT_PLAN #17), where it can be tested through real Spring validation instead of hand-built `MethodArgumentNotValidException`
- [x] `CorrelationIdFilter` (`shared/logging/`) — `OncePerRequestFilter` reusing/generating `X-Correlation-Id`, stored in MDC key `correlationId` (matches `logback-spring.xml`), echoed in the response header, cleared in `finally`
- [x] Unit tests for `CorrelationIdFilter` (3/3 passing)
- [x] `OpenApiConfig` (`shared/config/`) — Swagger metadata + global `bearerAuth` (HTTP Bearer, JWT) security scheme
- [x] Unit tests for `OpenApiConfig` (2/2 passing)

### User Module — domain data layer (SPRINT_PLAN #7, branch `feature/project-setup`)
- [x] `V2__create_users_table.sql` — `users` table + `user_roles` join table (for `Set<Role>` element collection). Verified with `mvn flyway:migrate` against the local `tripsi-postgres` Docker container (schema now at v2)
- [x] `Role`, `Status` enums + `User` entity (`user/domain/`) — TDD (4/4 tests, Red→Green). Pure domain, no Spring Security dependency: exposes `getRoles(): Set<Role>` instead of the diagram's original `getAuthorities()`. See `CLASS_DIAGRAM.md` note on `UserPrincipal` (adapter, still pending in SPRINT_PLAN #9/#10) for the `UserDetails` translation
- [x] `UserRepository` (`user/port/out/`) — `JpaRepository<User, UUID>` + `findByEmail`, `existsByEmail`. No unit test written (nothing to unit-test in an interface Spring Data implements); verified by integration test in SPRINT_PLAN #16
- [x] Minimal User DTOs (Java Records, `user/dto/`): `RegisterRequestDTO`, `UpdateUserDTO`, `UserResponseDTO`, `LoginRequestDTO` — created ahead of SPRINT_PLAN #11 only because `UserServicePort`/`UserService` (#10) need the types to compile. `UserMapper` (MapStruct) still pending — #11 now means "formalize the mapper", not "create the DTOs"
- [x] `UserServicePort` (`user/port/in/`) + `UserService` (`user/application/`) — TDD (10/10 tests, Red→Green): register (with duplicate-email business rule), findById, findByEmail, update, softDelete
- [x] `UserMapper` (MapStruct, `user/dto/`) — `User → UserResponseDTO`. `UserService` refactored to use it (manual mapping method removed); 10/10 tests still green

### Shared Security (SPRINT_PLAN #12, branch `feature/project-setup`)
- [x] `JwtTokenProvider` (`shared/security/`) — TDD (4/4 tests). Deliberately generic: takes `(UUID userId, String email, Set<String> roles)`, never touches `User`/`UserPrincipal` — keeps `shared` free of any module dependency (Spring Modulith rule: feature modules depend on `shared`, never the reverse)
- [x] `JwtAuthenticationFilter` (`shared/security/`) — reads `Authorization: Bearer`, populates `SecurityContextHolder` directly from JWT claims (no DB lookup per request — stateless). 4/4 tests passing

### SecurityConfig (SPRINT_PLAN #13, branch `feature/project-setup`)
- [x] `UserPrincipal` (`user/adapter/in/security/`) — implements `UserDetails`, wraps `User`, translates `Set<Role>` → `Collection<GrantedAuthority>`. 4/4 tests
- [x] `UserDetailsServiceImpl` (`user/adapter/in/security/`) — implements Spring Security's `UserDetailsService`; used only by the login `AuthenticationManager`/`DaoAuthenticationProvider` to verify passwords, NOT by the per-request JWT filter. 2/2 tests
- [x] `RestAuthenticationEntryPoint` / `RestAccessDeniedHandler` (`shared/security/`) — Spring Security rejects unauthenticated/forbidden requests *before* they reach `GlobalExceptionHandler` (it only catches exceptions thrown inside the `DispatcherServlet`/controller layer), so without these two the 401/403 bodies would not be RFC 9457 `ProblemDetail` like every other error in the API. Both just write a `ProblemDetail` JSON body directly to the response
- [x] `SecurityConfig` (`shared/config/`) — stateless JWT filter chain (`SessionCreationPolicy.STATELESS`), CSRF disabled (justified: stateless bearer-token API, not cookie/session-based, so CSRF doesn't apply — standard OWASP guidance), CORS from `tripsi.cors.allowed-origins`, `/api/v1/auth/**` + Swagger/OpenAPI paths public, everything else requires auth. `BCryptPasswordEncoder` bean lives here (consumed by `UserService`, built back in #10/SPRINT_PLAN numbering)
- **Two subtle fixes applied while wiring this** (documented because they're easy to reintroduce by accident):
  1. `JwtAuthenticationFilter` deliberately has **no** `@Component`. It's manually `new`'d inside `SecurityConfig` and added via `addFilterBefore`. If it were a `@Component`, Spring Boot's generic servlet-filter auto-registration would *also* register it outside Spring Security's control — at an unpredictable position, risking the `SecurityContext` it sets being wiped by Security's own context-reset filter. (Documented pattern — see Spring Security's own sample apps and the well-known Baeldung JWT tutorial, which never annotate a manually-wired filter with `@Component`.)
  2. `CorrelationIdFilter` got `@Order(Ordered.HIGHEST_PRECEDENCE)`. Generic `@Component` filters are registered by Spring Boot at default/lowest precedence, which would put it *after* Spring Security's filter chain — meaning logs produced during JWT authentication wouldn't carry a `correlationId`. Now it always runs first.

### AuthController (SPRINT_PLAN #14, branch `feature/project-setup`)
- [x] `AuthResponseDTO` (`user/dto/`) — `accessToken`, `tokenType` ("Bearer"), `expiresIn` (seconds; RFC 6749 §5.1 shape). No `refreshToken` — out of scope until a real refresh-token flow is built (ADR-009 OAuth2 extension is a later stretch goal); adding the field without the mechanism behind it would be misleading
- [x] `JwtTokenProvider.getExpirationSeconds()` added so `AuthService` can report `expiresIn` without duplicating the config value
- [x] `AuthServicePort` (`user/port/in/`) + `AuthService` (`user/application/`) — TDD (2/2 tests). Deliberately separate from `UserServicePort`: login is an authentication concern (`AuthenticationManager` + JWT issuance), not user CRUD, and isn't in the original `CLASS_DIAGRAM.md` port list
- [x] `AuthController` (`user/adapter/in/rest/`) — thin, delegates `register` to `UserServicePort` and `login` to `AuthServicePort`; Springdoc `@Operation`/`@ApiResponse` annotations present. `POST /api/v1/auth/register` (201), `POST /api/v1/auth/login` (200, 401 on bad credentials via existing `GlobalExceptionHandler`)

### UserController (SPRINT_PLAN #15, branch `feature/project-setup`)
- [x] `JwtPrincipal(UUID userId, String email)` record (`shared/security/`) — replaces the raw email string as the `Authentication` principal set by `JwtAuthenticationFilter`, so controllers get the user's ID without an extra DB lookup. Still framework-generic, no `user` module dependency
- [x] `UserController` (`user/adapter/in/rest/`) — `GET/PUT/DELETE /api/v1/users/me`, as originally sketched. Briefly switched to `/api/v1/users/{id}` + `@PreAuthorize` ownership check, then reverted: putting the id in the URL at all is an unneeded IDOR attack surface (OWASP A01) for no benefit, since the server already knows who's calling from the token — `/me` has no id for an attacker to tamper with. `@EnableMethodSecurity`/`@PreAuthorize` removed from `SecurityConfig` accordingly (unused). The `JwtPrincipal(UUID userId, String email)` change (see #13 above) was kept — it's what lets `/me` read the id straight off the token with no extra DB lookup

### UserRepository integration tests (SPRINT_PLAN #16, branch `feature/project-setup`)
- [x] Added `spring-boot-testcontainers` dependency to `pom.xml`. Fixed a scaffolding inconsistency found in `application-test.yml`: it used the legacy `jdbc:tc:postgresql:...` magic-URL driver, which needs the `org.testcontainers:jdbc` artifact — not present in `pom.xml`, so it would've failed the first time anyone actually ran a test needing the `test` profile with a real DB. Replaced with the current Spring Boot-recommended approach: `@ServiceConnection` on a `@Container`-managed `PostgreSQLContainer` declared directly in the test class — no manual datasource URL/driver config needed at all, so removed that from `application-test.yml`
- [x] `UserRepositoryIntegrationTest` (`user/port/out/`, `@DataJpaTest` + `@Testcontainers`) — 6/6 tests: save/findById, findByEmail (hit/miss), existsByEmail (hit/miss), roles correctly persisted in the `user_roles` join table. Runs Flyway migrations (V1+V2) against a real disposable PostgreSQL container, not a stub

### Controller tests + hardening pass (SPRINT_PLAN #17, branch `feature/project-setup`) — LAST Sprint 1 task
- [x] **Bug found and fixed**: `GlobalExceptionHandler` only caught `BadCredentialsException`, not the broader `AuthenticationException`. A soft-deleted user attempting login (`UserPrincipal.isEnabled()=false`) throws `DisabledException` — a sibling type, not a subclass of `BadCredentialsException` — which would have fallen through to the generic 500 handler. Widened to `@ExceptionHandler(AuthenticationException.class)`, and **deliberately kept the same generic "Invalid credentials" detail for every cause** (bad password, disabled, locked, etc.) — revealing *why* auth failed would let an attacker enumerate which emails have an account (OWASP user-enumeration guidance)
- [x] `AuthAndUserControllerIntegrationTest` (`user/adapter/in/rest/`, full `@SpringBootTest` + `@AutoConfigureMockMvc` + `@Testcontainers`) — 12/12 tests covering the real end-to-end stack (real Postgres, real Security filter chain, real JWT):
  - register: 201 + no password in response, 422 duplicate email, 400 field errors (this is the `@Valid` branch deferred all the way back when `GlobalExceptionHandler` was first built)
  - login: 200 + token/tokenType/expiresIn, 401 wrong password, 401 unknown email
  - protected endpoint without token → 401 (`RestAuthenticationEntryPoint`), malformed token → 401, **expired token → 401** (generates a token with a negative-expiration `JwtTokenProvider` instance — this case was initially missed and added after being asked for explicitly)
  - `/me` GET/PUT/DELETE happy paths, and soft-delete → subsequent login now fails (exercises the `DisabledException` fix above)
- [x] **Found and fixed two more pre-existing scaffolding bugs while running the full suite for the first time ever in this project** (none of the targeted `-Dtest=X` runs so far had exercised these):
  1. `application-test.yml`'s `tripsi.jwt.secret` (`test-secret-key-that-is-at-least-256-bits-long-for-hmac-sha`) contained hyphens — **not valid Base64** — which crashed `JwtTokenProvider`'s bean creation (`DecodingException: Illegal base64 character: '-'`) the moment any test loaded the full Spring context. Replaced with a real generated Base64 string (fixed, test-only — not a real credential, fine to commit)
  2. `TripSiApplicationTests` (context-load smoke test, present since Day 1, never actually run before) had no Testcontainers setup, so after removing the legacy `jdbc:tc:` URL it had no datasource at all. Added the same `@Testcontainers` + `@ServiceConnection PostgreSQLContainer` pattern used everywhere else
- [x] **Known issue, left for Sprint 2 (not blocking Sprint 1)**: `ModularityTests` (`ApplicationModules.of(...)` / `.verify()`) fails with `NoSuchMethodError` on `ConfigDataEnvironmentPostProcessor.applyTo(...)` — a binary incompatibility between `spring-modulith` 2.1.1 and `spring-boot` 3.5.16. No stable Modulith release fixes this yet (only pre-release `2.2.0-M2`). Formal module-boundary verification is SPRINT_PLAN task #32 (Sprint 2), so this doesn't block Sprint 1 — revisit then
- [x] **Full suite result**: 61/61 tests passing (`mvn test -Dtest='!ModularityTests'`), `BUILD SUCCESS`

---

## What Needs To Be Done Next

> Order below follows `docs/SPRINT_PLAN.md` task numbering exactly (not reshuffled) per explicit instruction.

### Sprint 1 — COMPLETE ✅ (all 17 tasks done, 61/61 tests passing, one known issue tracked for Sprint 2 — see above)

### Sprint 2 (Oct 20–26): Suggestion + Spring AI
### Sprint 3 (Oct 27–Nov 2): Flight + Duffel API
### Sprint 4 (Nov 3–9): Recommendation + Planner + E2E

---

## Local Dev Environment Notes

- SDKMAN's default Java on this machine is 17.0.2, but the project requires Java 21 (`pom.xml`). The machine has a JDK 23 installed system-wide at `/Library/Java/JavaVirtualMachines/jdk-23.jdk` (not registered in SDKMAN — no internet access to validate the candidate at time of setup). Until registered properly, run Maven with:
  `export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-23.jdk/Contents/Home && mvn ...`

---

## Key Decisions Made

| Decision | Choice | Reason |
|----------|--------|--------|
| Lombok | **NO** | Explicit code, Java Records for DTOs |
| Java version | **21 LTS** | Modern features, long-term support |
| Spring Boot | **3.5.6** | 3.4.x OSS ended Dec 2025 |
| DTO mapping | **MapStruct** in all modules | Compile-time, zero reflection |
| .env support | **spring-dotenv** (me.paulschwarz) | .env file support in Java |
| Primary ports | **Yes** — service interfaces | Controllers depend on interface, not concrete |
| Event listeners | **Driving adapters** in adapter/in/event/ | Decoupling, not performance |
| JPA repos | **port/out/** | Simultaneously port and adapter |
| Cross-module references | **UUID value + DB-level FK** | No JPA relation (Modulith boundary), integrity enforced by PostgreSQL |

---

## Repository Structure

```
tripsi-backend/
├── CLAUDE.md                    # Agent configuration (mandatory rules)
├── pom.xml                      # Maven with all dependencies
├── docker-compose.yml           # PostgreSQL 16 for local dev
├── .env.example                 # Environment variable template
├── .gitignore
├── docs/
│   ├── SPRINT_PLAN.md           # 4 sprints with story points
│   └── PROJECT_STATUS.md        # THIS FILE — agent memory
└── src/
    ├── main/
    │   ├── java/com/tripsi/api/
    │   │   ├── TripSiApplication.java
    │   │   ├── shared/{config,exception,security,logging}/
    │   │   ├── user/{domain,application,port/{in,out},adapter/in/rest,dto}/
    │   │   ├── suggestion/{domain,application,port/{in,out},adapter/{in/rest,out/ai},dto,event}/
    │   │   ├── flight/{domain,application,port/{in,out},adapter/{in/{rest,event},out/{duffel,mock}},dto,event}/
    │   │   ├── recommendation/{...same pattern...}/
    │   │   └── planner/{...same pattern...}/
    │   └── resources/
    │       ├── application.yml
    │       ├── application-{dev,test,prod}.yml
    │       ├── logback-spring.xml
    │       └── db/migration/V1__create_uuid_extension.sql
    └── test/
        └── java/com/tripsi/api/
            ├── TripSiApplicationTests.java
            ├── ModularityTests.java
            └── {user,suggestion,flight,recommendation,planner}/{application,adapter/in/rest}/
```

---

## How To Resume Work

1. Read `CLAUDE.md` — all mandatory rules and architecture
2. Read this file (`docs/PROJECT_STATUS.md`) — current state and next steps
3. Read `docs/SPRINT_PLAN.md` — full sprint breakdown with story points
4. Check which sprint we're in based on the date
5. Pick up from "What Needs To Be Done Next"
