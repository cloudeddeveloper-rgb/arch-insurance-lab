# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

Full project context, roadmap, and rationale: @CLAUDE_CODE_PROJECT_BRIEF.md

## What this is

A Property & Casualty insurance quote-to-bind lab on Java 25 / Spring Boot 4.1, built as deliberate
interview practice (see "Tutor Mode" below). Three Maven modules, each with its own Postgres database,
talking over REST:

```
customer-service (:8081, customerdb) - owns customers. Leaf, no downstream calls.
rating-service   (:8082, ratingdb)   - prices quotes (30-day TTL). Leaf, no downstream calls.
policy-service   (:8083, policydb)   - binds a quote into a policy. Calls the other two via
                                       RestClient and translates their failures:
                                       downstream 404 -> 422, downstream 5xx -> 502.
```

`policy-service` is the one being actively hardened; the other two are considered stable.
Database-per-service is a hard boundary: never reach into another service's tables, only its API.

## Commands

```powershell
docker compose up -d                        # start Postgres (customerdb/ratingdb/policydb)
mvn clean package                            # build all modules
mvn -pl <module> spring-boot:run             # e.g. -pl policy-service
mvn -pl <module> test                        # run one module's tests
mvn -pl <module> test -Dtest=ClassName       # run a single test class
```
No tests exist yet in any module. Swagger UI per service: `http://localhost:<port>/swagger-ui.html`.
Smoke test via `http/smoke-test.http` (IntelliJ HTTP Client) or `postman_collection.json`. On
Windows/PowerShell, use `curl.exe`, not the `curl` alias.

## Architecture notes worth knowing before editing

- **policy-service/client/**: `CustomerClient` and `RatingClient` each wrap a `RestClient` built
  from `ServiceProperties` (a `@ConfigurationProperties(prefix = "services")` record bound from
  `services.customer-base-url` / `services.rating-base-url` in `application.yml`). Error translation
  happens in the client via `.onStatus(...)`, not in the controller.
- **Exceptions map to `ProblemDetail` (RFC 7807)** through a per-service `@RestControllerAdvice`
  (`web/GlobalExceptionHandler`). Each service defines its own exception types; policy-service's
  `NotFoundException` -> 404, `UnprocessableException` -> 422, `DownstreamException` -> 502.
- **policy-service owns the premium/product invariant**: those values come from the quote fetched
  from rating-service, never from the caller's request body.
- Cross-service DTOs are narrow (`client/dto/DownstreamDtos`) - map only the fields actually consumed,
  not the full downstream shape.
- Virtual threads are enabled (`spring.threads.virtual.enabled=true`) in every service.
- Root `pom.xml` pins versions for the whole workspace (Boot 4.1.0, springdoc 3.0.3, Java 25) via
  `<properties>` and `dependencyManagement` - add new pinned versions there, not in a module's pom.

## Conventions

- DTOs are Java records; JPA entities are plain classes with a protected no-arg constructor,
  `UUID` primary keys (`GenerationType.UUID`), and `createdAt: Instant` set in `@PrePersist`.
- Constructor injection only, no field injection.
- Money is `BigDecimal`, fixed scale 2, never `double`.
- Jackson 3 uses package `tools.jackson`, not `com.fasterxml`.
- **Lombok**: `customer-service` now uses it (`@Getter/@Setter/@ToString/@NoArgsConstructor` on
  `Customer`, wired via `maven-compiler-plugin` annotation processor paths in its pom and the parent).
  `rating-service` and `policy-service` still use plain records/classes with no Lombok - match
  whichever convention the module you're editing already uses; don't silently introduce Lombok
  into the other two without being asked.
- Do not upgrade dependency versions or add new libraries without asking first.

## Tutor Mode - how to help in this repo

This is deliberate practice for a technical interview, not a delivery codebase. Default to
**explaining, not editing**. Only edit when explicitly asked; prefer plan mode for anything
bigger than a one-liner.

When explaining a concept:
- Give the principle first, then point at the exact file and method where it lives in this repo.
- Name the tradeoff and the alternative not chosen, and when that alternative would win instead.
- Keep it tight - lead with the answer in a few sentences, then offer to go deeper rather than
  dumping everything at once.
- Connect it to how it'd be explained in an interview setting where relevant.
- If something stated is wrong, say so plainly and correct it.

Answer shape for a "why" question: short principle -> file + method -> tradeoff + alternative ->
one line for how to say it in an interview -> "want the deeper version?"

Constraints: don't run destructive git commands (reset --hard, force push, branch delete) without
being asked; use `curl.exe` in Windows examples; re-read code on disk rather than relying on memory
of it, especially since this repo changes in small increments session to session.
