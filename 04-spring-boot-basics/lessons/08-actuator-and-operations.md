# Lesson 08 — Actuator and running in production

| | |
|---|---|
| **Goal** | Make the app observable (health, info, metrics), add a custom health check, and learn the settings and habits that matter once real users depend on it. |
| **Needs** | Lessons 01–07 |
| **Time** | ~30 min |
| **Next** | Module 05 — SQLDelight (compile-checked SQL) |
| **Exercise** | `src/main/kotlin/exercises/ex08/` |

## §1. Why operations matter

Code that works on your laptop isn't done. In production, someone (or some machine) must be able to ask:
*Is it alive? Can it serve traffic? How fast is it? What version is running?*

> 🧒 **Like I'm 5:**
> A car's dashboard: fuel gauge, temperature, warning lights. You don't open the bonnet while driving to
> check the engine — the dashboard tells you. Actuator is your app's dashboard.

`spring-boot-starter-actuator` adds those dashboard endpoints under `/actuator`. We expose four
([`application.yml`](../src/main/resources/application.yml)):

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, info, metrics, flyway
  endpoint:
    health:
      show-details: always      # fine for learning; restrict in production!
```

### Try it

```bash
curl -s "localhost:8080/actuator"
```
```json
{"_links":{
  "self":{"href":"http://localhost:8080/actuator"},
  "info":{"href":"http://localhost:8080/actuator/info"},
  "flyway":{"href":"http://localhost:8080/actuator/flyway"},
  "health":{"href":"http://localhost:8080/actuator/health"},
  "health-path":{"href":"http://localhost:8080/actuator/health/{*path}","templated":true},
  "metrics":{"href":"http://localhost:8080/actuator/metrics"},
  "metrics-requiredMetricName":{"href":"http://localhost:8080/actuator/metrics/{requiredMetricName}","templated":true}
}}
```

🔐 Actuator can expose a LOT (environment variables, bean lists, heap dumps, thread dumps). **Expose only what
you need**, and in production put actuator behind authentication or a separate internal port
(`management.server.port: 9090`). Module 06 secures endpoints.

## §2. Health

```bash
curl -s "localhost:8080/actuator/health"
```

Real response (pretty-printed):

```json
{
  "status": "UP",
  "components": {
    "db":        { "status": "UP", "details": { "database": "SQLite", "validationQuery": "isValid()" } },
    "diskSpace": { "status": "UP", "details": { "total": 494384795648, "free": 75469144064, "threshold": 10485760, "exists": true, "path": "…" } },
    "ex08Exercise": { "status": "UNKNOWN", "details": { "todo": "exercise 08.1" } },
    "ex08Solution": { "status": "UP", "details": { "totalBooks": 5, "outOfStock": ["Emma"] } },
    "livenessState":  { "status": "UP" },
    "readinessState": { "status": "UP" },
    "lowStock":  { "status": "UP", "details": { "threshold": 3, "lowStockBooks": ["Emma (0)", "Dune Messiah (2)"] } },
    "ping": { "status": "UP" },
    "ssl":  { "status": "UP", "details": { "expiringChains": [], "invalidChains": [], "validChains": [] } }
  },
  "groups": ["liveness", "readiness"]
}
```

Every *health indicator* contributes a component. The overall `status` is the worst of them: if `db` is
`DOWN`, the app is `DOWN` and the endpoint returns HTTP 503.

Boot provides `db` (borrows a pooled connection and checks it), `diskSpace`, `ping` and the liveness/readiness
states for free. `lowStock` is ours, and `ex08Exercise` / `ex08Solution` come from exercise 08. Notice that
`UNKNOWN` doesn't pull the overall status down; only `DOWN` or `OUT_OF_SERVICE` do.

### A custom health indicator

[`ops/LowStockHealthIndicator.kt`](../src/main/kotlin/bookstore/ops/LowStockHealthIndicator.kt):

```kotlin
@Component("lowStock")                       // the bean name becomes the component name in /actuator/health
class LowStockHealthIndicator(
    private val books: BookRepository,
    private val props: BookstoreProperties,
) : HealthIndicator {

    override fun health(): Health {
        val low = books.findLowStock(props.lowStockThreshold)
        return Health.up()                    // low stock is a warning, not an outage → still UP
            .withDetail("threshold", props.lowStockThreshold)
            .withDetail("lowStockBooks", low.map { "${it.title} (${it.stock})" })
            .build()
    }
}
```

It's just a bean implementing an interface (module 02). In Boot 4, the health API lives in
`org.springframework.boot.health.contributor`.

⚠️ **Gotcha:** health checks get called *often*, every few seconds by load balancers. Keep them cheap: no
heavy queries, no calls to slow external services.

### Liveness vs readiness

```bash
curl -s "localhost:8080/actuator/health/liveness"
# {"status":"UP"}
```

| Probe | Question | If it fails… |
|---|---|---|
| **liveness** `/actuator/health/liveness` | "Is the process stuck or broken beyond repair?" | the platform **restarts** it |
| **readiness** `/actuator/health/readiness` | "Can it take traffic *right now*?" (e.g. still starting, DB unreachable) | the platform **stops sending traffic** until it's ready |

> 🏗️ **In the real world:** Kubernetes `livenessProbe` and `readinessProbe` point exactly at these two URLs.

## §3. Info

```bash
curl -s "localhost:8080/actuator/info"
# {"app":{"description":"Kotlin + Spring Boot tutorial bookstore"}}
```

The output comes from `info.*` properties (enabled with `management.info.env.enabled: true`). Teams usually
add the **git commit** and **build version** here, so "which version is running?" has an instant answer.

## §4. Metrics

Boot measures a lot automatically with **Micrometer**, for example every HTTP request:

```bash
curl -s "localhost:8080/actuator/metrics/http.server.requests"
```

Real response, after a few test calls (shortened):

```json
{
  "name": "http.server.requests",
  "baseUnit": "seconds",
  "measurements": [
    { "statistic": "COUNT",      "value": 22.0 },
    { "statistic": "TOTAL_TIME", "value": 0.266687208 },
    { "statistic": "MAX",        "value": 0.065503291 }
  ],
  "availableTags": [
    { "tag": "uri",     "values": ["/api/books/{id}", "/api/orders", "/api/books", "…"] },
    { "tag": "status",  "values": ["200", "201", "400", "204", "404", "405", "409"] },
    { "tag": "outcome", "values": ["CLIENT_ERROR", "SUCCESS"] },
    { "tag": "method",  "values": ["DELETE", "POST", "PATCH", "PUT", "GET"] }
  ]
}
```

Drill down with tags: `/actuator/metrics/http.server.requests?tag=uri:/api/orders&tag=status:409`. Notice that
the `uri` tag uses the **template** `/api/books/{id}`, not `/api/books/1`, `/api/books/2` and so on, so the
number of metric series stays small.

Other useful built-in metrics: `jvm.memory.used`, `hikaricp.connections.active` (how busy the pool is),
`process.cpu.usage`.

> 🏗️ **In the real world:** nobody reads these with curl. A collector (Prometheus via
> `micrometer-registry-prometheus`, or OpenTelemetry via `spring-boot-starter-opentelemetry`) scrapes them,
> and dashboards and alerts are built on top. Module 08 (capstone) adds tracing and structured logs.

## §5. Logging

Boot logs with Logback, and levels are configuration:

```yaml
logging:
  level:
    root: info
    bookstore: debug                                  # your code, more detail
    org.springframework.jdbc.core: debug              # see every SQL JdbcClient runs
```

Or without editing files: `--logging.level.org.springframework.jdbc.core=debug`.

To log from your own code, use SLF4J (`private val log = LoggerFactory.getLogger(javaClass)`, then
`log.info("Order {} placed", id)`). Use placeholders, not string templates, so the message is only built if
the level is enabled.

For log aggregation tools, switch to JSON logs: `logging.structured.format.console: ecs` (or `logstash`).

🔐 Never log passwords, tokens, full card numbers, or whole request bodies containing personal data.

## §6. Production settings already in this app

```yaml
server:
  shutdown: graceful        # on SIGTERM: stop accepting new requests, finish in-flight ones, then exit
spring:
  threads:
    virtual:
      enabled: true         # one cheap JDK virtual thread per request (Java 21+)
```

- **Graceful shutdown:** deployments stop old instances constantly. Without it, requests in the middle of
  being served get cut off. The wait is capped by `spring.lifecycle.timeout-per-shutdown-phase` (default 30 s).
- **Virtual threads:** a Tomcat thread blocked on a database call used to tie up a heavyweight OS thread.
  Virtual threads make blocking code (like `JdbcClient`) scale to many concurrent requests: it's the
  "one chef juggling many dishes" idea from module 01's coroutine lessons, done by the JVM.
  ⚠️ The SQLite pool is still 4 connections: virtual threads don't create database capacity.

## §7. Shipping it: one runnable jar

```bash
./gradlew :04-spring-boot-basics:bootJar
java --enable-native-access=ALL-UNNAMED -jar 04-spring-boot-basics/build/libs/04-spring-boot-basics.jar
```

`bootJar` builds a **fat jar**: your classes plus every dependency plus the embedded Tomcat, in one file.
No application server is needed: `java -jar` is the whole deployment. (`--enable-native-access` silences
the JDK 25 warning from SQLite's native library.)

Configure it per environment using lesson 04's precedence, with no rebuild:

```bash
SPRING_DATASOURCE_URL='jdbc:sqlite:/var/data/bookstore.db?foreign_keys=true' \
BOOKSTORE_LOW_STOCK_THRESHOLD=5 \
java -jar bookstore.jar --server.port=8081
```

> 🏗️ **In the real world — the 12-factor app checklist:** one codebase, many deploys · config in the
> environment · backing services (DB) attached via URL · stateless processes (no session data in memory) ·
> fast startup + graceful shutdown · logs to stdout as event streams · dev/prod parity.
> This app already follows most of them.

## Recap

- Actuator = the app's dashboard. Expose only what you need, and secure it in production.
- `/actuator/health`: built-in plus custom `HealthIndicator`s; liveness (restart me) vs readiness (route traffic to me).
- `/actuator/info` for version info, `/actuator/metrics` for Micrometer measurements (HTTP, JVM, pool).
- Logging levels are configuration; use structured JSON logs and never log secrets.
- Graceful shutdown, virtual threads and a single fat jar configured by environment variables.
