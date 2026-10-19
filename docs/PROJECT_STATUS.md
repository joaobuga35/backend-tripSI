# TripSi — Project Status

> **Last updated**: 2026-10-16 (Sprint 1, Day 2 — in progress on `feature/project-setup`)
> **Current Sprint**: Sprint 1 (Oct 13–19) — Foundation + User + Auth

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

---

## What Needs To Be Done Next

> Order below follows `docs/SPRINT_PLAN.md` task numbering exactly (not reshuffled) per explicit instruction.

### Sprint 1 — remaining tasks (branch: `feature/project-setup` until Sprint 1 ends)
- [ ] **#9** `UserRepository` (secondary port)
- [ ] **#10** `UserService` with TDD — registration, update, soft delete
- [ ] **#11** User DTOs (Java Records) + `UserMapper` (MapStruct)
- [ ] **#12** JWT — `JwtTokenProvider`, `JwtAuthenticationFilter`
- [ ] **#13** `SecurityConfig` — JWT filter chain, endpoint security, CORS. Also needs new (not in original SPRINT_PLAN numbering): `UserPrincipal` + `UserDetailsServiceImpl` in `user/adapter/in/security/` (see `CLASS_DIAGRAM.md`)
- [ ] **#14** `AuthController` — POST `/api/v1/auth/login`, POST `/api/v1/auth/register`
- [ ] **#15** `UserController` — GET/PUT `/api/v1/users/me`
- [ ] **#16** `UserRepository` integration tests (Testcontainers)
- [ ] **#17** Auth controller tests (MockMvc) — also covers the `@Valid` validation branch deferred above

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
