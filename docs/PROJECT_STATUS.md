# TripSi — Project Status

> **Last updated**: 2026-10-13 (Sprint 1, Day 1 — Planning)
> **Current Sprint**: Sprint 1 (Oct 13–19) — Foundation + User + Auth

---

## What Has Been Done

### Architecture & Planning (Pre-Sprint)
- [x] Architecture decisions documented as 10 ADRs in CLAUDE.md
- [x] C4 Model diagrams created (TripSi_C4_Model.md)
- [x] Class diagrams created (TripSi_Class_Diagram.md)
- [x] Sprint plan with 4 sprints, story points, and ceremonies (docs/SPRINT_PLAN.md)
- [x] Hexagonal architecture fully defined with primary ports (port/in/) and secondary ports (port/out/)
- [x] Module structure with 5 modules: user, suggestion, flight, recommendation, planner
- [x] Event flow defined: suggestion → flight → recommendation → planner

### Project Scaffolding (Sprint 1, Day 1)
- [x] pom.xml with all 22 dependencies (Spring Boot 3.5.6, Spring AI 1.1.8, Spring Modulith 2.1.1)
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

---

## What Needs To Be Done Next

### Immediate (Sprint 1 — remaining tasks)
1. **Create GitHub repository** — init git, push scaffolding to `dev` branch
2. **User domain entities** — User entity, Role enum, Status enum
3. **V2 Flyway migration** — users table DDL
4. **UserServicePort** (primary port) — interface with register, findById, update, softDelete
5. **UserRepository** (secondary port) — extends JpaRepository<User, UUID>
6. **DTOs** — RegisterRequestDTO, LoginRequestDTO, UserResponseDTO (Java Records)
7. **UserMapper** — MapStruct interface
8. **UserService** — implements UserServicePort (TDD: write tests first)
9. **AuthController + UserController** — thin REST layer
10. **SecurityConfig** — JWT filter chain, BCrypt password encoder
11. **JwtTokenProvider** — token generation/validation
12. **GlobalExceptionHandler** — ProblemDetail responses
13. **CorrelationIdFilter** — MDC propagation
14. **Controller tests** — after implementation

### Sprint 2 (Oct 20–26): Suggestion + Spring AI
### Sprint 3 (Oct 27–Nov 2): Flight + Duffel API
### Sprint 4 (Nov 3–9): Recommendation + Planner + E2E

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
