# CLAUDE.md — TripSi Development Agent Rules

> This file configures the AI development agent for the TripSi project.
> Every instruction here is mandatory. Do not deviate without explicit approval.

---

## Project Overview

**TripSi** is an AI-driven travel planning application built as a modular monolith with hexagonal architecture. It uses Spring Boot 3.5.x + Spring AI + Angular 17+ to deliver personalised trip recommendations through a conversational chat interface.

**Solo developer project** — NCI Higher Diploma final project.
**Timeline**: October 13 – December 2, 2026 (buffer to Dec 5, presentation Dec 9).

---

## Agent Rules

### Commits & Version Control

- **NEVER make automatic commits.** All commits are manual, triggered only by the developer.
- **NEVER use** `--no-verify` **or skip pre-commit hooks.**
- **Conventional Commits** in English only:
  - `feat: add flight search endpoint`
  - `fix: correct null handling in offer mapping`
  - `refactor: extract booking conditions to embeddable`
  - `test: add unit tests for RecommendationService`
  - `docs: update OpenAPI schema for planner endpoints`
  - `chore: update Spring Boot to 3.4.x`
- **Branch strategy**:
  - `main` — production-ready, receives PRs from `dev` only at the end
  - `dev` — integration branch, receives PRs from feature branches
  - `feature/<module>-<description>` — e.g., `feature/user-auth`, `feature/flight-duffel-adapter`
- **PR flow**: `feature/`\* → `dev` (during development), `dev` → `main` (final validation only)

### Code Quality

- **Language**: All code, comments, commit messages, and documentation in **English**.
- **Java conventions**: Follow standard Java naming (camelCase for methods/fields, PascalCase for classes, SCREAMING_SNAKE for constants).
- **Angular Style Guide**: Follow the official Angular style guide.
- **No hardcoded secrets**: All sensitive values via environment variables or Spring profiles.
- **No** `System.out.println`: Use SLF4J + Logback structured logging exclusively.
- **No** `ddl-auto=update` **or** `ddl-auto=create`: All schema changes via **Flyway** migrations only.

### Testing Strategy (TDD)

- **TDD is mandatory for domain logic**: Services, ports (interfaces), business rules, DTOs validation.
  - Write the test FIRST, see it fail (Red), implement the minimum code to pass (Green), then refactor (Refactor).
- **Controllers and adapters**: Tested AFTER implementation (not TDD), but tests are still required.
- **Integration tests**: Use **Testcontainers** with PostgreSQL for repository and end-to-end flow testing.
- **Test naming**: `should_<expected>_when_<condition>` — e.g., `should_throw_exception_when_prompt_not_found`.
- **No test is optional.** Tests are part of the deliverable.

### Architecture Enforcement

- **Hexagonal architecture (ports & adapters)** in every module. No shortcuts.
- **Module boundaries enforced by Spring Modulith** — verified at compile time.
- **Domain logic lives in services**, never in controllers or adapters.
- **Controllers** are thin — validate input, delegate to service, return response.
- **Ports** are interfaces in the domain layer. Adapters implement them in the infrastructure layer.
- **Inter-module communication** via Spring Application Events only. No direct cross-module service calls.
- **JPA relationships within module boundaries (e.g., Prompt ↔ FlightRequest). UUID references across distant modules (e.g., Prompt → userId, Recommendation → promptId) are stored as plain UUID values with no JPA relation and no import of the other module's entity. Referential integrity is enforced by a database-level foreign key in the Flyway migration. Queries use derived methods on the value (e.g.,** `findByUserIdOrderByCreatedAtDesc`**).**

### API Design

- **API versioning**: All endpoints prefixed with `/api/v1/`
- **OpenAPI documentation**: Every endpoint documented via Springdoc annotations (`@Operation`, `@ApiResponse`, `@Schema`).
- **Error responses**: Use `ProblemDetail` (RFC 9457) via `@RestControllerAdvice`. Never expose stack traces or internal class names.
- **DTOs**: Always use DTOs for request/response. Never expose entities directly.
- **Validation**: Use `@Valid` + Bean Validation annotations on DTOs.

### Security (OWASP Top 10 Baseline)

- **A01 Broken Access Control**: Role-based access (`ROLE_USER`, `ROLE_ADMIN`) enforced at endpoint level via Spring Security.
- **A02 Cryptographic Failures**: Passwords hashed with BCrypt. JWT secrets via environment variables. HTTPS in production.
- **A03 Injection**: Use parameterised queries (JPA/Hibernate). Validate all input with Bean Validation.
- **A04 Insecure Design**: Hexagonal architecture isolates domain from infrastructure. Rate limiting on auth endpoints.
- **A05 Security Misconfiguration**: Spring Profiles (dev/test/prod) with environment-specific configs. No default credentials.
- **A06 Vulnerable Components**: Keep dependencies updated. Use `mvn dependency:tree` to audit.
- **A07 Authentication Failures**: JWT with proper expiration. Account lockout after failed attempts (stretch goal).
- **A08 Data Integrity Failures**: Validate all deserialized data. Sign JWTs with strong keys.
- **A09 Logging & Monitoring**: Structured logging with correlation IDs. Log security events (login, failed auth, access denied).
- **A10 SSRF**: Validate and sanitize all external URLs before HTTP calls (Duffel API base URL is configurable, not user-provided).

### Logging

- **SLF4J + Logback** exclusively.
- **Structured logging** with JSON format in production.
- **Correlation IDs** via MDC (Mapped Diagnostic Context) propagated through the request lifecycle.
- **Log levels**: ERROR for failures, WARN for recoverable issues, INFO for business events, DEBUG for development.
- **Security events**: Always log at INFO level — login success/failure, token validation, access denied.

### Spring Profiles

- `dev` — Local development. H2 or local PostgreSQL. Debug logging. Mock adapters enabled.
- `test` — Testcontainers PostgreSQL. Minimal logging. Mock external services.
- `prod` — Cloud PostgreSQL. JSON structured logging. Real adapters (Duffel, LLM). HTTPS enforced.

---

## Architecture Decision Records (ADRs)

### ADR-001: Modular Monolith over Microservices

**Status**: Accepted
**Context**: Solo developer project with 8-week timeline. Microservices add operational complexity (service discovery, distributed tracing, independent deployments) without proportional benefit at this scale.
**Decision**: Build as a modular monolith using Spring Modulith. Module boundaries enforced at compile time. Can be decomposed into microservices later if needed.
**Consequences**: Single deployable unit. Simpler CI/CD. Module boundaries must be respected — no cross-module JPA relationships for distant modules.

### ADR-002: Hexagonal Architecture (Ports & Adapters)

**Status**: Accepted
**Context**: External dependencies (LLM providers, Duffel API) may change. Domain logic must be testable in isolation.
**Decision**: Each module follows the hexagonal pattern. Domain logic in services, external access through port interfaces with concrete adapters. Adapters are swappable (e.g., DuffelFlightAdapter vs MockFlightAdapter).
**Consequences**: More interfaces and classes upfront. Domain logic is fully testable with mocks. Provider switching requires only a new adapter, no domain changes.

### ADR-003: PostgreSQL with Flyway Migrations

**Status**: Accepted
**Context**: MySQL was used in the original prototype. PostgreSQL offers native UUID type, better JSON support, and is the industry standard for Spring Boot applications.
**Decision**: PostgreSQL 16+ as the relational database. All schema changes managed by Flyway migrations. Never use Hibernate `ddl-auto` for schema management.
**Consequences**: Schema is version-controlled and reproducible. Every migration is reviewed. Rollback requires a new migration (Flyway community edition).

### ADR-004: Spring AI for LLM Abstraction

**Status**: Accepted
**Context**: The LLM provider (OpenAI, Anthropic, Google) is TBD. The application must not be coupled to a specific provider.
**Decision**: Use Spring AI as the abstraction layer. Each module that needs LLM interaction defines a port interface (e.g., `LlmSuggestionPort`), with a Spring AI adapter implementing it.
**Consequences**: Provider can be switched via configuration. Prompt engineering is centralised in adapters. Spring AI handles serialization, retry, and streaming.

### ADR-005: Event-Driven Module Communication

**Status**: Accepted
**Context**: Modules must remain loosely coupled. The flow is sequential: suggestion → flight → recommendation → planner.
**Decision**: Use Spring Application Events for inter-module communication. Each module publishes a domain event when its work completes (e.g., `SuggestionCompletedEvent`). The next module listens and triggers its workflow.
**Consequences**: Modules are decoupled at compile time. Event ordering is guaranteed within a single JVM. No need for an external message broker at this stage.

### ADR-006: OWASP Top 10 Security Baseline

**Status**: Accepted
**Context**: Academic project, but security practices are part of the evaluation criteria and professional standard.
**Decision**: Address all OWASP Top 10 categories from day 1 through architectural decisions, Spring Security configuration, and coding practices. Final security scan after API completion.
**Consequences**: Security is baked in, not bolted on. Some items (rate limiting, account lockout) are stretch goals but the architecture supports them.

### ADR-007: API Versioning with URI Prefix

**Status**: Accepted
**Context**: API may evolve. Frontend and backend are separate repositories.
**Decision**: Version via URI prefix (`/api/v1/...`). Simple, explicit, visible in OpenAPI docs.
**Consequences**: Breaking changes require a new version prefix. Non-breaking changes (adding optional fields) stay in v1.

### ADR-008: Global Error Handling with Problem Details

**Status**: Accepted
**Context**: Consistent error responses are needed for the Angular frontend. OWASP requires no internal details in error responses.
**Decision**: Use `@RestControllerAdvice` with `ProblemDetail` (RFC 9457). All exceptions are caught centrally and returned in the standard Problem Details JSON format.
**Consequences**: Angular has a single error format to parse. Stack traces and class names are never exposed. Custom exception types map to specific HTTP status codes.

### ADR-009: JWT Authentication with OAuth2 Extension Point

**Status**: Accepted
**Context**: Basic authentication is needed from week 1. Third-party OAuth2 (Google/GitHub login) is a stretch goal for the final week.
**Decision**: Implement JWT-based authentication first (registration + login). Design the `SecurityConfig` to accept an OAuth2 provider later without restructuring. The third-party authenticator is implemented in the last week if time permits.
**Consequences**: Core auth works from day 1. OAuth2 extension requires adding Spring Security OAuth2 Client dependency and a new `AuthenticationProvider`, not a rewrite.

### ADR-010: Mock Fallback for External APIs

**Status**: Accepted
**Context**: Demo reliability is critical — external APIs (Duffel, LLM) may have downtime during the presentation on Dec 9.
**Decision**: Every external API adapter has a mock counterpart implementing the same port interface. Switched via Spring Profiles (`@Profile("dev")` for mock, `@Profile("prod")` for real). Mock data uses cached real responses for authenticity.
**Consequences**: Demo never fails due to external dependencies. Tests use mocks by default. Real adapters are tested separately with integration tests.

---

## Module Structure

Each module follows the hexagonal package convention below:

- **Primary ports** (`port/in/`) are interfaces that define WHAT the module can do (driving side).
  Controllers and event listeners depend on these interfaces, never on the concrete service.
- **Secondary ports** (`port/out/`) are interfaces that define what the module NEEDS from the outside world.
  Spring Data JPA repositories are simultaneously port and adapter (Spring generates the implementation).
- **Services** (`application/`) implement the primary port interface and orchestrate domain logic.
- **Event listeners** are driving adapters (they receive an external stimulus, same as a controller).
- DTOs are Java Records. MapStruct mappers live alongside their DTOs.

```
com.tripsi.api/
│
├── shared/                                    # Cross-cutting (NOT a Spring Modulith module)
│   ├── config/                                #   SecurityConfig, CorsConfig, OpenApiConfig
│   ├── exception/                             #   GlobalExceptionHandler, ResourceNotFoundException, BusinessRuleException
│   ├── security/                              #   JwtTokenProvider, JwtAuthenticationFilter
│   └── logging/                               #   CorrelationIdFilter
│
├── user/                                      # ── MODULE ──
│   ├── domain/                                #   User, Role (enum), Status (enum)
│   ├── application/                           #   UserService implements UserServicePort
│   ├── port/
│   │   ├── in/                                #   UserServicePort (primary port — driving interface)
│   │   └── out/                               #   UserRepository (extends JpaRepository)
│   ├── adapter/in/rest/                       #   UserController, AuthController → depend on UserServicePort
│   └── dto/                                   #   RegisterRequestDTO, LoginRequestDTO, UserResponseDTO, UserMapper
│
├── suggestion/                                # ── MODULE ──
│   ├── domain/                                #   Prompt (aggregate root), SuggestionOption, Location, RequestStatus
│   ├── application/                           #   SuggestionService implements SuggestionServicePort
│   ├── port/
│   │   ├── in/                                #   SuggestionServicePort (primary port)
│   │   └── out/                               #   LlmSuggestionPort, PromptRepository, SuggestionOptionRepository
│   ├── adapter/
│   │   ├── in/rest/                           #   SuggestionController → depends on SuggestionServicePort
│   │   └── out/ai/                            #   SpringAiSuggestionAdapter implements LlmSuggestionPort
│   ├── dto/                                   #   CreatePromptRequestDTO, PromptResponseDTO, SuggestionOptionDTO, SuggestionMapper
│   └── event/                                 #   SuggestionCompletedEvent
│
├── flight/                                    # ── MODULE ──
│   ├── domain/                                #   FlightRequest, Offer, SliceFlight, Passenger, FlightSegment, Carrier,
│   │                                          #   BookingConditions, BookingCondition, CabinClass
│   ├── application/                           #   FlightService implements FlightServicePort
│   ├── port/
│   │   ├── in/                                #   FlightServicePort (primary port)
│   │   └── out/                               #   FlightSearchPort, FlightRequestRepository, OfferRepository
│   ├── adapter/
│   │   ├── in/
│   │   │   ├── rest/                          #   FlightController → depends on FlightServicePort
│   │   │   └── event/                         #   FlightOffersEventListener (driving adapter) → depends on FlightServicePort
│   │   └── out/
│   │       ├── duffel/                        #   DuffelFlightAdapter implements FlightSearchPort (@Profile("prod"))
│   │       └── mock/                          #   MockFlightAdapter implements FlightSearchPort (@Profile("dev"))
│   ├── dto/                                   #   FlightSearchRequestDTO, OfferResponseDTO, FlightSegmentDTO, FlightMapper
│   └── event/                                 #   FlightOffersReceivedEvent
│
├── recommendation/                            # ── MODULE ──
│   ├── domain/                                #   Recommendation, ScoreBreakdown, RecommendationAlternative
│   ├── application/                           #   RecommendationService implements RecommendationServicePort
│   ├── port/
│   │   ├── in/                                #   RecommendationServicePort (primary port)
│   │   └── out/                               #   LlmAnalysisPort, RecommendationRepository
│   ├── adapter/
│   │   ├── in/
│   │   │   ├── rest/                          #   RecommendationController → depends on RecommendationServicePort
│   │   │   └── event/                         #   RecommendationEventListener (driving adapter) → depends on RecommendationServicePort
│   │   └── out/ai/                            #   SpringAiAnalysisAdapter implements LlmAnalysisPort
│   ├── dto/                                   #   RecommendationResponseDTO, RecommendationMapper
│   └── event/                                 #   RecommendationReadyEvent
│
└── planner/                                   # ── MODULE ──
    ├── domain/                                #   Planner, PlannerDay, PlannerBlock, DateRange
    ├── application/                           #   PlannerService implements PlannerServicePort
    ├── port/
    │   ├── in/                                #   PlannerServicePort (primary port)
    │   └── out/                               #   LlmPlannerPort, PlannerRepository
    ├── adapter/
    │   ├── in/
    │   │   ├── rest/                          #   PlannerController → depends on PlannerServicePort
    │   │   └── event/                         #   PlannerEventListener (driving adapter) → depends on PlannerServicePort
    │   └── out/ai/                            #   SpringAiPlannerAdapter implements LlmPlannerPort
    ├── dto/                                   #   PlannerResponseDTO, PlannerDayDTO, PlannerBlockDTO, PlannerMapper
    └── event/                                 #   PlannerCompletedEvent
```

### DTO Mapping with MapStruct

- Every module has a `@Mapper(componentModel = "spring")` interface in its `dto/` package.
- DTOs are Java Records (immutable, no Lombok needed).
- MapStruct generates the implementation at compile time — zero reflection overhead.
- Mappers are injected via constructor injection into controllers or services.
- For nested embeddables (e.g., Offer → FlightSegment → Carrier), MapStruct handles the mapping
  automatically if field names match, or via `@Mapping(source, target)` when they differ.

---

## Event Flow

```
User submits prompt
       │
       ▼
[suggestion module] ──SuggestionCompletedEvent──▶ [flight module]
                                                        │
                                          FlightOffersReceivedEvent
                                                        │
                                                        ▼
                                               [recommendation module]
                                                        │
                                            RecommendationReadyEvent
                                                        │
                                                        ▼
                                                [planner module]
                                                        │
                                            PlannerCompletedEvent
```

---

## Key Technologies

| Technology        | Version       | Purpose                        |
| ----------------- | ------------- | ------------------------------ |
| Java              | 17+           | Language                       |
| Spring Boot       | 3.5.x         | Framework                      |
| Spring Modulith   | Latest stable | Module boundaries              |
| Spring AI         | Latest stable | LLM abstraction                |
| Spring Security   | (via Boot)    | JWT auth + RBAC                |
| Spring Data JPA   | (via Boot)    | ORM                            |
| PostgreSQL        | 16+           | Database                       |
| Flyway            | (via Boot)    | Migrations                     |
| Springdoc OpenAPI | 2.x           | API documentation (Swagger UI) |
| Testcontainers    | Latest stable | Integration testing            |
| JUnit 5           | (via Boot)    | Unit testing                   |
| Mockito           | (via Boot)    | Mocking                        |
| Docker            | Latest        | Containerisation               |
| Angular           | 17+           | Frontend                       |

---

## Quick Reference

- **Base package**: `com.tripsi.api`
- **API prefix**: `/api/v1/`
- **Database**: PostgreSQL with native UUID
- **Migrations**: `src/main/resources/db/migration/V{version}__{description}.sql`
- **Profiles**: `dev`, `test`, `prod`
- **Error format**: Problem Details (RFC 9457) — `application/problem+json`
- **Logging**: SLF4J + Logback, JSON in prod, correlation IDs via MDC
- **Swagger UI**: Available at `/swagger-ui.html` (dev/test profiles)
