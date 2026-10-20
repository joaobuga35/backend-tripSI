# TripSi — Backend Sprint Plan

> **Sprint Duration**: 1 week each (Tuesday → Monday)
> **Total Sprints**: 4 (backend) + 4 (frontend, deployment, polish)
> **Board**: GitHub Projects Kanban (Backlog → To Do → In Progress → In Review → Done)
> **Sprint Planning**: Every Monday
> **Definition of Done**: See bottom of this document

---

## Sprint 1 — Foundation & User Module

**Period**: 13 Oct (Tue) – 19 Oct (Sun)
**Sprint Goal**: Project bootstrapped with working user registration, JWT authentication, and Swagger UI accessible. PostgreSQL running via Docker. All cross-cutting concerns in place.

| # | Task | Type | Story Points | Labels |
|---|------|------|:---:|--------|
| 1 | Initialise Spring Boot 3.4.x project with all dependencies (Spring Modulith, Spring AI, Spring Security, Spring Data JPA, Flyway, Springdoc, MapStruct, JJWT, spring-dotenv) | feat | 3 | `setup` |
| 2 | Configure `application.yml` for dev, test, and prod profiles + `.env.example` | feat | 2 | `setup`, `config` |
| 3 | Set up PostgreSQL + pgAdmin via Docker Compose (dev environment) | feat | 2 | `setup`, `infra` |
| 4 | Implement GlobalExceptionHandler with `@RestControllerAdvice` + `ProblemDetail` (RFC 9457) | feat | 2 | `shared`, `error-handling` |
| 5 | Configure CorrelationIdFilter for MDC-based request correlation IDs | feat | 1 | `shared`, `logging` |
| 6 | Configure Springdoc OpenAPI — SwaggerConfig, global Bearer JWT security scheme | feat | 1 | `shared`, `docs` |
| 7 | Create Flyway migration `V1__create_users_table.sql` | feat | 1 | `user-module`, `database` |
| 8 | Implement User entity with UUID PK, Role enum, Status enum | feat | 2 | `user-module`, `domain` |
| 9 | Implement UserRepository (JPA port) | feat | 1 | `user-module`, `port` |
| 10 | Write UserService with TDD — registration, update, soft delete | feat | 3 | `user-module`, `service`, `tdd` |
| 11 | Create User DTOs (Java Records) + UserMapper (MapStruct) | feat | 2 | `user-module`, `dto` |
| 12 | Implement JWT — JwtTokenProvider, JwtAuthenticationFilter | feat | 3 | `security` |
| 13 | Configure SecurityConfig — JWT filter chain, endpoint security, CORS | feat | 2 | `security`, `config` |
| 14 | Implement AuthController — POST `/api/v1/auth/login`, POST `/api/v1/auth/register` | feat | 2 | `user-module`, `controller` |
| 15 | Implement UserController — GET `/api/v1/users/me`, PUT `/api/v1/users/me` | feat | 1 | `user-module`, `controller` |
| 16 | Write integration tests for UserRepository with Testcontainers | test | 2 | `user-module`, `test` |
| 17 | Write controller tests for auth endpoints (MockMvc) | test | 2 | `security`, `test` |

**Total**: 17 tasks, ~32 story points
**Sprint Velocity Target**: 32 SP

**Key branches**:
- `feature/project-setup`
- `feature/user-domain`
- `feature/user-auth`

---

## Sprint 2 — Suggestion Module & Spring AI

**Period**: 20 Oct (Tue) – 26 Oct (Sun)
**Sprint Goal**: User can submit a travel prompt via chat and receive AI-generated destination suggestions. Event pipeline initiated. Spring Modulith boundaries verified.

| # | Task | Type | Story Points | Labels |
|---|------|------|:---:|--------|
| 18 | Create Flyway migration `V2__create_prompts_and_suggestions.sql` | feat | 1 | `suggestion-module`, `database` |
| 19 | Implement Prompt entity (aggregate root) with Location embeddable, RequestStatus enum | feat | 2 | `suggestion-module`, `domain` |
| 20 | Implement SuggestionOption entity with `@ManyToOne` to Prompt | feat | 1 | `suggestion-module`, `domain` |
| 21 | Implement PromptRepository and SuggestionOptionRepository | feat | 1 | `suggestion-module`, `port` |
| 22 | Define `LlmSuggestionPort` interface (driven port) | feat | 1 | `suggestion-module`, `port` |
| 23 | Implement `SpringAiSuggestionAdapter` — prompt engineering for destination suggestions | feat | 3 | `suggestion-module`, `adapter`, `spring-ai` |
| 24 | Write SuggestionService with TDD — create prompt, trigger LLM, save suggestions | feat | 3 | `suggestion-module`, `service`, `tdd` |
| 25 | Implement `SuggestionCompletedEvent` and publish from SuggestionService | feat | 1 | `suggestion-module`, `event` |
| 26 | Create Suggestion DTOs (Java Records) + `SuggestionMapper` (MapStruct) | feat | 2 | `suggestion-module`, `dto` |
| 27 | Implement SuggestionController — POST `/api/v1/prompts`, GET `/api/v1/prompts/{id}`, GET `/api/v1/prompts/{id}/suggestions` | feat | 2 | `suggestion-module`, `controller` |
| 28 | Add Springdoc `@Operation` / `@ApiResponse` annotations to SuggestionController | feat | 1 | `suggestion-module`, `docs` |
| 29 | Write unit tests for SuggestionService (TDD — these are written FIRST) | test | 3 | `suggestion-module`, `test`, `tdd` |
| 30 | Write integration tests for Prompt/SuggestionOption repositories (Testcontainers) | test | 2 | `suggestion-module`, `test` |
| 31 | Write controller tests for suggestion endpoints (MockMvc) | test | 2 | `suggestion-module`, `test` |
| 32 | Run Spring Modulith `ApplicationModulesTest` — verify module boundaries | test | 1 | `architecture`, `test` |

**Total**: 15 tasks, ~26 story points

**Key branches**:
- `feature/suggestion-domain`
- `feature/suggestion-spring-ai`
- `feature/suggestion-controller`

---

## Sprint 3 — Flight Module & Duffel API

**Period**: 27 Oct (Tue) – 2 Nov (Sun)
**Sprint Goal**: End-to-end flow from prompt → suggestions → flight offers. Duffel API integration working. Mock fallback adapter operational for demo/test.

| # | Task | Type | Story Points | Labels |
|---|------|------|:---:|--------|
| 33 | Create Flyway migration `V3__create_flight_requests_and_offers.sql` | feat | 2 | `flight-module`, `database` |
| 34 | Implement FlightRequest entity with SliceFlight, Passenger embeddables, CabinClass enum | feat | 2 | `flight-module`, `domain` |
| 35 | Implement Offer entity with FlightSegment, Carrier, BookingConditions embeddables | feat | 3 | `flight-module`, `domain` |
| 36 | Implement FlightRequestRepository and OfferRepository | feat | 1 | `flight-module`, `port` |
| 37 | Define `FlightSearchPort` interface (driven port) | feat | 1 | `flight-module`, `port` |
| 38 | Implement `DuffelFlightAdapter` — RestClient to Duffel API, response mapping to domain | feat | 5 | `flight-module`, `adapter`, `duffel` |
| 39 | Implement `MockFlightAdapter` — cached real data for demo/test (`@Profile("dev")`) | feat | 2 | `flight-module`, `adapter`, `mock` |
| 40 | Implement `FlightOffersEventListener` — listen to `SuggestionCompletedEvent`, trigger flight search | feat | 2 | `flight-module`, `event` |
| 41 | Write FlightService with TDD — create request, search via port, save offers, publish event | feat | 3 | `flight-module`, `service`, `tdd` |
| 42 | Implement `FlightOffersReceivedEvent` and publish from FlightService | feat | 1 | `flight-module`, `event` |
| 43 | Create Flight DTOs (Java Records) + `FlightMapper` (MapStruct — nested embeddable mappings) | feat | 3 | `flight-module`, `dto` |
| 44 | Implement FlightController — POST `/api/v1/flights/search`, GET `/api/v1/flights/offers/{promptId}` | feat | 2 | `flight-module`, `controller` |
| 45 | Add Springdoc annotations to FlightController | feat | 1 | `flight-module`, `docs` |
| 46 | Write unit tests for FlightService (TDD) | test | 3 | `flight-module`, `test`, `tdd` |
| 47 | Write integration tests for FlightRequest/Offer repositories (Testcontainers) | test | 2 | `flight-module`, `test` |
| 48 | Write integration test: full flow prompt → suggestion → flight search | test | 3 | `flight-module`, `integration`, `test` |

**Total**: 16 tasks, ~36 story points

**Key branches**:
- `feature/flight-domain`
- `feature/flight-duffel-adapter`
- `feature/flight-mock-adapter`
- `feature/flight-controller`

**Risk**: DuffelFlightAdapter (task 38) is the most complex adapter — involves external API contract, response deserialization, error handling, and rate limiting. Allocate extra time.

---

## Sprint 4 — Recommendation & Planner Modules

**Period**: 3 Nov (Tue) – 9 Nov (Sun)
**Sprint Goal**: Complete backend API — all 5 modules operational, full event pipeline working end-to-end, all tests green, Swagger documenting every endpoint.

| # | Task | Type | Story Points | Labels |
|---|------|------|:---:|--------|

### Recommendation Module

| 49 | Create Flyway migration `V4__create_recommendations.sql` | feat | 1 | `recommendation-module`, `database` |
| 50 | Implement Recommendation entity with ScoreBreakdown, RecommendationAlternative embeddables | feat | 2 | `recommendation-module`, `domain` |
| 51 | Implement RecommendationRepository | feat | 1 | `recommendation-module`, `port` |
| 52 | Define `LlmAnalysisPort` interface | feat | 1 | `recommendation-module`, `port` |
| 53 | Implement `SpringAiAnalysisAdapter` — prompt engineering for offer analysis & ranking | feat | 3 | `recommendation-module`, `adapter`, `spring-ai` |
| 54 | Implement `RecommendationEventListener` — listen to `FlightOffersReceivedEvent` | feat | 1 | `recommendation-module`, `event` |
| 55 | Write RecommendationService with TDD — analyse, rank, save recommendation, publish event | feat | 3 | `recommendation-module`, `service`, `tdd` |
| 56 | Implement `RecommendationReadyEvent` and publish from RecommendationService | feat | 1 | `recommendation-module`, `event` |
| 57 | Create Recommendation DTOs + `RecommendationMapper` (MapStruct) | feat | 1 | `recommendation-module`, `dto` |
| 58 | Implement RecommendationController — GET `/api/v1/recommendations/{promptId}` | feat | 1 | `recommendation-module`, `controller` |
| 59 | Write unit tests for RecommendationService (TDD) | test | 2 | `recommendation-module`, `test`, `tdd` |

### Planner Module

| 60 | Create Flyway migration `V5__create_planner.sql` | feat | 1 | `planner-module`, `database` |
| 61 | Implement Planner, PlannerDay, PlannerBlock entities with DateRange embeddable | feat | 3 | `planner-module`, `domain` |
| 62 | Implement PlannerRepository | feat | 1 | `planner-module`, `port` |
| 63 | Define `LlmPlannerPort` interface | feat | 1 | `planner-module`, `port` |
| 64 | Implement `SpringAiPlannerAdapter` — prompt engineering for itinerary generation | feat | 3 | `planner-module`, `adapter`, `spring-ai` |
| 65 | Implement `PlannerEventListener` — listen to `RecommendationReadyEvent` | feat | 1 | `planner-module`, `event` |
| 66 | Write PlannerService with TDD — generate itinerary, save plan | feat | 3 | `planner-module`, `service`, `tdd` |
| 67 | Create Planner DTOs (PlannerResponseDTO, PlannerDayDTO, PlannerBlockDTO) + `PlannerMapper` | feat | 2 | `planner-module`, `dto` |
| 68 | Implement PlannerController — GET `/api/v1/planner/{promptId}` | feat | 1 | `planner-module`, `controller` |
| 69 | Add Springdoc annotations to Recommendation + Planner controllers | feat | 1 | `docs` |
| 70 | Write unit tests for PlannerService (TDD) | test | 2 | `planner-module`, `test`, `tdd` |

### Integration & Architecture

| 71 | Write integration tests for Recommendation + Planner repositories | test | 2 | `test` |
| 72 | Write full E2E integration test: prompt → suggestion → flight → recommendation → planner | test | 3 | `integration`, `test` |
| 73 | Run Spring Modulith `ApplicationModulesTest` — final architecture verification | test | 1 | `architecture`, `test` |

**Total**: 25 tasks, ~42 story points

**Key branches**:
- `feature/recommendation-domain`
- `feature/recommendation-spring-ai`
- `feature/planner-domain`
- `feature/planner-spring-ai`
- `feature/e2e-integration-tests`

**Note**: Heaviest sprint, but by Sprint 4 the hexagonal pattern is muscle memory. Both modules follow the exact same structure as suggestion (port → adapter → service → controller → mapper). The velocity gain from pattern repetition compensates the task count.

---

## Post-Backend Sprints (10 Nov – 2 Dec)

| Sprint | Period | Focus |
|--------|--------|-------|
| **Sprint 5** | 10–16 Nov | Angular frontend — project setup, auth screens, chat UI, suggestion flow |
| **Sprint 6** | 17–23 Nov | Angular — flight display, recommendation view, planner itinerary display |
| **Sprint 7** | 24–28 Nov | Cloud deployment (Docker + VM), OWASP security scan, polish |
| **Sprint 8** | 29 Nov–2 Dec | OAuth2 stretch goal, final testing, demo preparation, code freeze |
| **Buffer** | 3–5 Dec | Bug fixes, presentation rehearsal |
| **Presentation** | 9 Dec | Final presentation |

---

## Sprint Ceremonies

| Ceremony | When | Duration | What |
|----------|------|----------|------|
| **Sprint Planning** | Monday | 30 min | Review backlog, commit to sprint tasks, define sprint goal |
| **Daily Check** | Daily (async) | 5 min | Quick status — what did I do, what's next, any blockers? (personal log) |
| **Sprint Review** | Sunday evening | 15 min | Demo working increment, verify sprint goal met |
| **Sprint Retro** | Sunday evening | 10 min | What went well, what to improve, action items for next sprint |

> Solo project — ceremonies are lightweight and personal. The value is in the discipline of reflecting and adjusting, not the formality.

---

## Velocity Tracking

| Sprint | Planned SP | Completed SP | Notes |
|--------|:---:|:---:|-------|
| Sprint 1 | 32 | 30 | Tasks #1–15 done. UserController uses /api/v1/users/{id} (not /me, developer decision) with @PreAuthorize ownership check; JwtAuthenticationFilter principal changed to JwtPrincipal(userId, email) to support it. Next: #16 (UserRepository Testcontainers) on `feature/project-setup` |
| Sprint 2 | 26 | — | |
| Sprint 3 | 36 | — | |
| Sprint 4 | 42 | — | |

---

## GitHub Issues Labels

| Label | Color | Description |
|-------|-------|-------------|
| `sprint-1` | `#0E8A16` | Sprint 1 tasks |
| `sprint-2` | `#1D76DB` | Sprint 2 tasks |
| `sprint-3` | `#D93F0B` | Sprint 3 tasks |
| `sprint-4` | `#5319E7` | Sprint 4 tasks |
| `setup` | `#FBCA04` | Project setup |
| `user-module` | `#B60205` | User module |
| `suggestion-module` | `#0075CA` | Suggestion module |
| `flight-module` | `#E4E669` | Flight module |
| `recommendation-module` | `#D876E3` | Recommendation module |
| `planner-module` | `#C2E0C6` | Planner module |
| `shared` | `#BFD4F2` | Shared/cross-cutting |
| `domain` | `#EDEDED` | Domain layer |
| `service` | `#FEF2C0` | Application/service layer |
| `controller` | `#F9D0C4` | Controller/adapter-in |
| `adapter` | `#D4C5F9` | Adapter-out |
| `port` | `#C5DEF5` | Port interface |
| `event` | `#BFDADC` | Spring Events |
| `dto` | `#E6E6E6` | DTOs + MapStruct |
| `database` | `#0052CC` | Database/Flyway |
| `security` | `#B60205` | Security |
| `config` | `#FBCA04` | Configuration |
| `docs` | `#0075CA` | Documentation/OpenAPI |
| `test` | `#0E8A16` | Tests |
| `tdd` | `#006B75` | TDD tasks |
| `integration` | `#1D76DB` | Integration tests |
| `spring-ai` | `#5319E7` | Spring AI |
| `duffel` | `#D93F0B` | Duffel API |
| `mock` | `#E4E669` | Mock adapters |
| `infra` | `#BFD4F2` | Infrastructure |
| `architecture` | `#D876E3` | Architecture verification |
| `error-handling` | `#C2E0C6` | Error handling |
| `logging` | `#BFD4F2` | Logging |

---

## Definition of Done (per task)

- [ ] Code in the correct hexagonal package (`domain/`, `application/`, `port/`, `adapter/`, `dto/`)
- [ ] TDD applied for service/domain tasks (test written first, Red → Green → Refactor)
- [ ] All tests pass (`mvn verify`)
- [ ] MapStruct mapper created for Entity ↔ DTO conversions
- [ ] Springdoc annotations present on controller endpoints (`@Operation`, `@ApiResponse`)
- [ ] Flyway migration runs cleanly on fresh database
- [ ] No hardcoded secrets or credentials
- [ ] Conventional commit message in English (`feat:`, `fix:`, `test:`, etc.)
- [ ] PR created from feature branch to `dev`

## Definition of Done (per sprint)

- [ ] All P0 tasks completed
- [ ] `mvn verify` passes with zero failures
- [ ] Swagger UI shows all new endpoints correctly
- [ ] Sprint deliverable is demonstrable (working increment)
- [ ] Sprint retro completed with action items
