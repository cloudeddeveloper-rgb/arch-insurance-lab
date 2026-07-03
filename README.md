# Arch Insurance Lab - Quote-to-Bind Microservices

A small, realistic P&C insurance backend on **Java 25** and **Spring Boot 4.1**, built to
practice the exact patterns a Spring/Java interview probes: multi-module Maven,
database-per-service, service-to-service calls with deliberate error translation, records,
Bean Validation, RFC 7807 `ProblemDetail`, and OpenAPI/Swagger.

## Stack (all pinned and current)

| Component            | Version | Notes                                                    |
|----------------------|---------|----------------------------------------------------------|
| Java                 | 25 (LTS)| Boot 4.1 supports Java 17-26                              |
| Spring Boot          | 4.1.0   | Spring Framework 7, Jakarta EE 11, Jackson 3             |
| Maven                | 3.9.16  | build tool                                               |
| springdoc-openapi    | 3.0.3   | the Boot-4 line (2.8.x is Boot 3 only)                   |
| Postgres             | 17      | one container, three databases, via Docker Compose       |

> New machine? Do `SETUP_WINDOWS.md` first (JDK 25, Maven, IntelliJ, Docker, Git SSH),
> then come back here.

---

## Architecture

```
                       POST /api/v1/policies (bind)
                                  |
                                  v
   +-------------------+   RestClient   +-------------------+
   |  policy-service   |--------------->|  customer-service |   (customer exists?)
   |  :8083  policydb  |                |  :8081  customerdb|
   |                   |   RestClient   +-------------------+
   |                   |--------------->|  rating-service   |   (quote valid + owned?)
   +-------------------+                |  :8082  ratingdb  |
                                        +-------------------+

  customer-service : owns customers.           Leaf - no downstream calls.
  rating-service   : prices quotes (30-day TTL). Leaf - no downstream calls.
  policy-service   : binds a quote into a policy. Calls the other two, then
                     translates their failures: downstream 404 -> 422, 5xx -> 502.
```

Each service owns its own database (`customerdb`, `ratingdb`, `policydb`) - no shared
tables, no cross-service joins. That boundary is the whole point of the exercise.

---

## Run it

### 1. Start Postgres (one command - no manual image pull needed)
```powershell
cd path\to\arch-insurance-lab
docker compose up -d
docker compose ps        # wait for STATUS = healthy
```

### 2. Start the three services
Order does not matter for startup, but the natural call order is customer -> rating -> policy.

**Option A - three terminals (PowerShell):**
```powershell
mvn -pl customer-service spring-boot:run
mvn -pl rating-service   spring-boot:run
mvn -pl policy-service   spring-boot:run
```
`-pl` = "in this module". Run each line in its own terminal. Leave them running.

**Option B - IntelliJ (nicer for the practice loop):**
Open each `*Application.java` and click the green run arrow, or use the **Services** tool
window (`Alt+8`) as a run dashboard to start/stop all three and see their consoles together.

### 3. Confirm they are up
```powershell
curl.exe http://localhost:8081/actuator/health
curl.exe http://localhost:8082/actuator/health
curl.exe http://localhost:8083/actuator/health
```
Each returns `{"status":"UP",...}`.

> **PowerShell gotcha:** plain `curl` is an alias for `Invoke-WebRequest` and behaves
> differently. Use **`curl.exe`** (real curl, shipped with Windows) in every command below.

---

## Smoke test with curl

The happy path is: create a customer, price a quote for them, bind the quote into a policy.
IDs are server-generated UUIDs - copy the `id` from each response into the next command.

```powershell
# 1) Create a customer -> note the "id" in the response
curl.exe -X POST http://localhost:8081/api/v1/customers `
  -H "Content-Type: application/json" `
  -d '{"firstName":"Ada","lastName":"Lovelace","email":"ada@example.com","dateOfBirth":"1990-12-10"}'

# 2) Price a quote (paste the customer id) -> note the quote "id"
curl.exe -X POST http://localhost:8082/api/v1/quotes `
  -H "Content-Type: application/json" `
  -d '{"customerId":"PASTE_CUSTOMER_ID","productType":"AUTO","coverageAmount":50000}'

# 3) Bind the policy (paste both ids)
curl.exe -X POST http://localhost:8083/api/v1/policies `
  -H "Content-Type: application/json" `
  -d '{"customerId":"PASTE_CUSTOMER_ID","quoteId":"PASTE_QUOTE_ID"}'

# 4) Read the policy back
curl.exe http://localhost:8083/api/v1/policies/PASTE_POLICY_ID
```

**Error cases worth seeing** (all return a `ProblemDetail` JSON body):
```powershell
# Missing customer -> 422 Unprocessable Entity
curl.exe -i -X POST http://localhost:8083/api/v1/policies `
  -H "Content-Type: application/json" `
  -d '{"customerId":"00000000-0000-0000-0000-000000000000","quoteId":"PASTE_QUOTE_ID"}'

# Bad input -> 400 Bad Request (validation)
curl.exe -i -X POST http://localhost:8081/api/v1/customers `
  -H "Content-Type: application/json" `
  -d '{"firstName":"","lastName":"X","email":"nope","dateOfBirth":"2999-01-01"}'
```

**Chained one-shot (optional, native PowerShell)** - captures ids automatically:
```powershell
$c = irm -Method Post http://localhost:8081/api/v1/customers -ContentType application/json `
     -Body '{"firstName":"Ada","lastName":"Lovelace","email":"ada2@example.com","dateOfBirth":"1990-12-10"}'
$q = irm -Method Post http://localhost:8082/api/v1/quotes -ContentType application/json `
     -Body (@{customerId=$c.id; productType="AUTO"; coverageAmount=50000} | ConvertTo-Json)
$p = irm -Method Post http://localhost:8083/api/v1/policies -ContentType application/json `
     -Body (@{customerId=$c.id; quoteId=$q.id} | ConvertTo-Json)
$p    # the bound policy
```

---

## Testing with Postman

1. Import **`postman_collection.json`** (File > Import).
2. It ships with collection variables for the three base URLs and empty `customerId`,
   `quoteId`, `policyId`.
3. Run the requests **in order** - each request's "Tests" script captures the returned id
   into a collection variable, so "Create quote" and "Bind policy" reuse them automatically.
   (Or use the Collection Runner to fire them in sequence.)

Prefer to stay in the IDE? Open **`http/smoke-test.http`** and run it with IntelliJ's HTTP
Client - it captures ids between steps the same way and includes the failure cases.

---

## Swagger UI and the OpenAPI documents

Each service serves interactive docs and machine-readable specs (springdoc):

| Service          | Swagger UI                              | OpenAPI JSON                         | OpenAPI YAML                              |
|------------------|-----------------------------------------|--------------------------------------|-------------------------------------------|
| customer-service | http://localhost:8081/swagger-ui.html   | http://localhost:8081/v3/api-docs    | http://localhost:8081/v3/api-docs.yaml    |
| rating-service   | http://localhost:8082/swagger-ui.html   | http://localhost:8082/v3/api-docs    | http://localhost:8082/v3/api-docs.yaml    |
| policy-service   | http://localhost:8083/swagger-ui.html   | http://localhost:8083/v3/api-docs    | http://localhost:8083/v3/api-docs.yaml    |

### How the YAML is generated
You do not write the OpenAPI file - **springdoc builds it at runtime** by reflecting over
your `@RestController`s, method signatures, records, and `jakarta.validation` annotations.
`@Tag`/`@Operation` add titles and summaries. Hitting `/v3/api-docs` returns JSON;
`/v3/api-docs.yaml` returns the same document as YAML. The operation ids default to your
controller method names (`create`, `getById`, `bind`, ...), which matters for client generation.

Save a spec to a file (needed for the generator step below):
```powershell
curl.exe http://localhost:8081/v3/api-docs.yaml -o customer-openapi.yaml
```

> If `/v3/api-docs` ever returns a Base64 blob instead of JSON, you are on an old springdoc.
> This project pins **3.0.3**, which fixes that Spring Framework 7 issue.

---

## Generate a typed client (with beans) from the YAML

Use the **OpenAPI Generator** Maven plugin to turn a saved spec into a Java client - model
beans plus an API class you call directly. This is how a consuming service avoids
hand-writing DTOs and URLs.

Put the saved spec (e.g. `customer-openapi.yaml`) in `src/main/resources/openapi/` of the
consumer module, then add:

```xml
<plugin>
    <groupId>org.openapitools</groupId>
    <artifactId>openapi-generator-maven-plugin</artifactId>
    <version>7.23.0</version>
    <executions>
        <execution>
            <goals><goal>generate</goal></goals>
            <configuration>
                <inputSpec>${project.basedir}/src/main/resources/openapi/customer-openapi.yaml</inputSpec>
                <generatorName>java</generatorName>
                <library>restclient</library>            <!-- matches this project's HTTP client -->
                <apiPackage>com.arch.client.customer.api</apiPackage>
                <modelPackage>com.arch.client.customer.model</modelPackage>
                <configOptions>
                    <useJakartaEe>true</useJakartaEe>     <!-- REQUIRED on Boot 3/4 (jakarta.*) -->
                    <useSpringBoot3>true</useSpringBoot3>
                </configOptions>
            </configuration>
        </execution>
    </executions>
</plugin>
```

The generated code needs a couple of compile-time deps in the consumer module:
```xml
<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-validation</artifactId></dependency>
```

Generate and use it:
```powershell
mvn -pl <consumer-module> generate-sources
```
```java
// Generated: ApiClient + one Api class per @Tag + model beans.
var apiClient = new com.arch.client.customer.ApiClient();   // wraps RestClient
apiClient.setBasePath("http://localhost:8081");

var customersApi = new com.arch.client.customer.api.CustomersApi(apiClient);
var customer = customersApi.getById(UUID.fromString("..."));   // method name = operationId
System.out.println(customer.getEmail());
```

> Tip: give endpoints explicit ids with `@Operation(operationId = "getCustomer")` to get
> clean, stable generated method names instead of the defaults.
> If your org is on WebClient/RestTemplate instead, swap `<library>` to `webclient` or
> `resttemplate`.

---

## Working efficiently in IntelliJ (Ultimate)

**Run dashboard (Services window, `Alt+8`).** Start/stop all three apps in one place, see
combined logs, restart individually. Far better than juggling terminals.

**HTTP Client (`.http` files).** `http/smoke-test.http` runs inside the editor with
response-variable capture - no context switch to Postman. Great for the tight edit-run loop.

**Database tool window (`View > Tool Windows > Database`).** Connect straight to the running
container to inspect what your services wrote:
- `+ > Data Source > PostgreSQL`
- Host `localhost`, Port `5432`, User `appuser`, Password `apppass`, Database `policydb`
  (add separate data sources for `customerdb` and `ratingdb`)
- Now you can run SQL, browse the `policies`/`quotes`/`customers` tables, and confirm the
  database-per-service split is real. The first connect will offer to download the JDBC driver.

**Docker tool window.** The bundled Docker plugin attaches to Docker Desktop - start/stop the
`arch-lab-postgres` container, tail its logs, and open a shell, all without leaving the IDE.
You can also right-click `docker-compose.yml` > Run.

**Actuator + live metrics.** With the Spring Boot run config selected, the **Actuator** tab
exposes health/mappings/beans live. Endpoints per service: `/actuator/health`,
`/actuator/health/liveness`, `/actuator/health/readiness`, `/actuator/metrics`.

**DevTools live restart.** `spring-boot-devtools` is on the classpath, so saving a `.java`
file auto-restarts the app (Ctrl+F9 to force a build). Keeps the loop fast while you practice.

**Shortcuts worth muscle memory:** `Ctrl+Shift+A` (find any action), `Shift+Shift` (search
everywhere), `Ctrl+B` (jump to bean/definition), `Alt+Enter` (fix/import), `Ctrl+F9` (build),
`Ctrl+Shift+F10` (run current class).

**Terminal inside IntelliJ.** `Alt+F12` opens the PowerShell terminal in the project root -
run `docker compose`, `mvn`, `git`, and `curl.exe` without leaving the window.

---

## Stretch exercise - add a `claims-service` yourself

The fastest way to regain fluency is to build a fourth service from muscle memory. Add
`claims-service` (port 8084, database `claimsdb`) that:
- Owns a `Claim` (id, policyId, amount, status FILED/APPROVED/DENIED, filedAt).
- On `POST /api/v1/claims`, calls **policy-service** (`GET /api/v1/policies/{id}`) via a
  `RestClient`, and rejects the claim (422) if the policy is missing or not `ACTIVE`.
- Reuses the exact patterns here: records, `ProblemDetail`, `ServiceProperties`, a client
  with `onStatus` translation, and its own database.

Wiring checklist: add the module to the root `pom.xml` `<modules>`, add
`CREATE DATABASE claimsdb;` to `db/init/01-create-databases.sql` (run `docker compose down -v`
then `up -d` to re-init), and add a `services.policy-base-url` property. Do it without
copy-paste - that is the point.

---

## Troubleshooting

- **`Port 8081 already in use`** - something else is on that port. Find it:
  `Get-NetTCPConnection -LocalPort 8081 | Select OwningProcess`, then stop that PID, or
  change `server.port` in the service's `application.yml`.
- **Service fails to start with a JDBC/connection error** - Postgres is not ready. Check
  `docker compose ps` shows `healthy`; give it a few seconds after `up -d`.
- **`mvn` runs on the wrong Java** - `mvn -v` should say Java 25. If not, fix `JAVA_HOME`
  (see `SETUP_WINDOWS.md`, step 2).
- **Databases missing after changing the Postgres version** - the init script only runs on a
  fresh volume. Run `docker compose down -v` then `docker compose up -d`.
- **`curl` behaves oddly in PowerShell** - use `curl.exe`, not the `curl` alias.
- **Swagger UI 404** - use `/swagger-ui.html` (configured path); the raw spec is at
  `/v3/api-docs`.

## Project layout
```
arch-insurance-lab/
  pom.xml                     # parent: pins Boot 4.1.0, Java 25, springdoc 3.0.3
  docker-compose.yml          # Postgres 17 + three databases
  db/init/                    # one-time CREATE DATABASE script
  http/smoke-test.http        # runnable in IntelliJ HTTP Client
  postman_collection.json     # importable Postman collection
  customer-service/           # :8081  customerdb   (leaf)
  rating-service/             # :8082  ratingdb     (leaf)
  policy-service/             # :8083  policydb     (calls customer + rating)
```
