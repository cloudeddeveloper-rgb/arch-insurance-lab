# Project Brief for Claude Code

Read this file, then generate a `CLAUDE.md` for this repo and act as my tutor while I build. This brief is the source of truth for what the project is, how it is built, and how I want you to help me.

## How to bootstrap (what I will run first)

1. From the repo root: `claude`
2. Then: `/init` (you scan the repo and draft a CLAUDE.md).
3. Then tell you: "Refine CLAUDE.md using CLAUDE_CODE_PROJECT_BRIEF.md - fold in the conventions, the build/run/test commands, and the Tutor Mode rules. Keep CLAUDE.md under 150 lines; move any long detail into this brief and reference it with @CLAUDE_CODE_PROJECT_BRIEF.md."

---

## Who I am and why this project exists

I am a VP-level engineering leader rebuilding hands-on Java/Spring fluency for a technical interview. This lab is deliberate practice. I care about understanding the WHY and the tradeoffs, not just getting code to compile. When I ask a question, teach me the concept and connect it to how I would explain it in an interview. I already think in systems and architecture; the gap is current, hands-on Java/Spring idiom. Calibrate to a strong engineer returning to the keyboard, not a beginner.

I am on Windows using IntelliJ. In PowerShell I use `curl.exe`, not the `curl` alias.

---

## What the project is (current state = "Day 1")

A small Property and Casualty insurance backend that models a quote-to-bind flow across three Spring Boot microservices, one database each, talking over REST.

- **customer-service** (port 8081, db `customerdb`) - owns customers.
- **rating-service** (port 8082, db `ratingdb`) - prices a quote for a product and coverage; a quote in `QUOTED` status is bindable.
- **policy-service** (port 8083, db `policydb`) - the centerpiece. Binds a quote into a policy. It calls customer-service and rating-service, and it owns the invariant that premium and product come from the quote, never from the caller.

Flow: create a customer -> get a quote from rating -> policy-service binds the quote into a policy after validating the customer exists, the quote is `QUOTED`, and the quote belongs to that customer.

## Tech stack (use these exact versions; do not upgrade or add libraries without asking)

- Java 25 (LTS), Spring Boot 4.1.0, Spring Framework 7, Jakarta EE 11, Jackson 3 (package `tools.jackson`, not `com.fasterxml`).
- Maven multi-module (parent `pom.xml` aggregates the three services).
- PostgreSQL 17 in Docker (`docker-compose.yml` creates the three databases).
- springdoc-openapi 3.0.3 for Swagger UI.
- No Lombok. Records for DTOs, plain classes for JPA entities.

## Repository layout (Day 1)

```
arch-insurance-lab/
  pom.xml                 parent, modules: customer-service, rating-service, policy-service
  docker-compose.yml      postgres:17, creates customerdb / ratingdb / policydb
  README.md, SETUP_WINDOWS.md, .gitignore
  http/smoke-test.http    IntelliJ HTTP Client requests (create -> quote -> bind)

  customer-service/  com.arch.customer
    CustomerServiceApplication, domain/Customer, repo/CustomerRepository,
    service/CustomerService, web/CustomerController, web/dto/CustomerDtos,
    web/GlobalExceptionHandler, exception/{NotFoundException, DuplicateEmailException}

  rating-service/    com.arch.rating
    RatingServiceApplication, domain/{Quote, ProductType, QuoteStatus},
    repo/QuoteRepository, service/{RatingEngine, QuoteService},
    web/QuoteController, web/dto/QuoteDtos, web/GlobalExceptionHandler,
    exception/NotFoundException
    - RatingEngine is a pure, deterministic premium calculation (rate * coverage + fee, BigDecimal, scale 2).

  policy-service/    com.arch.policy   (the service I will harden over the next days)
    PolicyServiceApplication, domain/{Policy, PolicyStatus}, repo/PolicyRepository,
    service/PolicyService, web/PolicyController, web/dto/PolicyDtos, web/GlobalExceptionHandler,
    client/{CustomerClient, RatingClient}, client/dto/DownstreamDtos,
    config/ServiceProperties, exception/{NotFoundException, UnprocessableException, DownstreamException}
    - CustomerClient / RatingClient call the other services with RestClient and translate errors:
      a downstream 404 becomes a 422 (business condition), a downstream 5xx becomes a 502.
```

## Conventions in this codebase (please follow them and reference them when teaching)

- **DTOs are Java records**; JPA entities are plain classes with a protected no-arg constructor.
- **Constructor injection only** (no field injection).
- **Errors use RFC 7807 `ProblemDetail`** via a `@RestControllerAdvice` per service. Custom exceptions map to status codes: NotFound -> 404, Unprocessable -> 422, Downstream -> 502.
- **Money is `BigDecimal`** with a fixed scale of 2, never `double`.
- **Primary keys are `UUID`** with `GenerationType.UUID`; `createdAt` is an `Instant` set in `@PrePersist`.
- **Database-per-service**; a service never reaches into another service's tables, only its API.
- **Virtual threads are on** (`spring.threads.virtual.enabled=true`).
- **Cross-service DTOs are narrow** - policy-service maps only the downstream fields it needs (`DownstreamDtos`).

## Build, run, and test (Windows)

Run all from the repo root.

- Build everything: `mvn clean package` (or `mvn clean verify` once tests exist).
- Start databases: `docker compose up -d`.
- Run one service: `mvn -pl <module> spring-boot:run` (e.g. `mvn -pl policy-service spring-boot:run`).
- Run one module's tests: `mvn -pl <module> test`.
- Swagger UI per service: `http://localhost:<port>/swagger-ui.html`.
- Smoke test: `http/smoke-test.http` in IntelliJ, or `curl.exe` against the endpoints.

## Roadmap (where this is going - "Day 2")

I am adding hardening to **policy-service only** in small commits, following a separate step-by-step guide. You do not need to make these changes - I am doing them by hand to learn - but know they are coming so you are not surprised when the code grows:

1. A unit test for RatingEngine.
2. Client resilience: connect/read timeouts, native Spring `@Retryable`, `@ConcurrencyLimit` (bulkhead).
3. A parallel aggregation endpoint (`GET /{id}/detail`) using a virtual-thread executor with a join timeout.
4. Idempotent bind via an `Idempotency-Key` header backed by a dedup table.
5. Security: JWT resource server, `@PreAuthorize` scopes, CORS, a dev-only token endpoint.
6. A Testcontainers integration test on real Postgres.
7. CI with a SonarQube quality gate and OWASP dependency-check.
8. An optional standalone `spring-ai-demo` module (Spring AI tool calling), not part of the root build.

When I finish an increment, I may ask you to update CLAUDE.md to match the new state.

---

## Tutor Mode - how I want you to help me

Put these rules into CLAUDE.md so they persist across sessions.

**Default to explaining, not editing.** This is a learning exercise. When I ask a question, teach; do not silently rewrite my files. Only edit when I explicitly ask, and prefer plan mode for anything more than a one-liner.

**When you explain a concept:**
- Give the principle first, then show it in this codebase by pointing at the exact file and method.
- Name the tradeoff and the alternative I did not choose, and when the alternative would win.
- Keep it tight. Lead with the answer in a few sentences. Offer "want me to go deeper on X?" instead of dumping everything.
- Where it helps, connect it to how I would say it in an interview (I am prepping for one).
- If I am wrong about something, tell me plainly and correct it.

**Constraints:**
- Match the stack: Java 25, Spring Boot 4.1, Jakarta, Jackson 3, records, constructor injection, ProblemDetail, BigDecimal money, UUID keys. Do not introduce Lombok or new libraries or version changes without asking.
- Do not run destructive git commands (no reset --hard, no force push, no branch deletion) without me asking.
- Windows/PowerShell: use `curl.exe` in examples.
- Prefer `@file` mentions and reading the actual code over guessing. If the code on disk differs from what you remember, re-read it.

**Good answer shape for a "why" question:**
> Short principle. Where it lives in the code (file + method). The tradeoff and the alternative. One line on how I would say it in an interview. Then: "Want the deeper version?"
