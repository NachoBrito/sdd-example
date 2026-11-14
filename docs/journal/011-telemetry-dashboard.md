# Feature Journal: Complete Telemetry Dashboard

This document records the development process for the complete telemetry dashboard feature.
The feature renames the Grafana dashboard to `"SDD Example - Telemetry"` and adds Database, RabbitMQ, and SMTP panels.

---

## 1. Implementation Process

The implementation followed the Spec-Driven Development workflow:

1. Created the specification in `specs/011-telemetry-dashboard/spec.md` with four user stories:
   - Rename the dashboard and retain existing HTTP and runtime panels (P1).
   - Add database connection pool and query duration metrics (P1).
   - Add RabbitMQ queue size, message throughput, and consumer metrics (P2).
   - Add SMTP sent, failed, and send duration metrics (P2).
2. Created the design artifacts in `specs/011-telemetry-dashboard/` (`plan.md`, `research.md`, `data-model.md`, `contracts/metrics-contract.md`, `quickstart.md`).
3. Generated the dependency-ordered task list in `specs/011-telemetry-dashboard/tasks.md` (`T001`–`T017`).
4. Implemented the solution without modifying `build.gradle.kts`, `gradle/libs.versions.toml`, or `config/` static analysis rules:
   - Added `@DbQueryMetrics` and `DbQueryMetricsInterceptor` in `adapter/out/persistence/` using Micronaut AOP to record `db.query.duration` on all four persistence adapters.
   - Added `@SmtpMetrics` and `SmtpMetricsInterceptor` in `adapter/out/smtp/` using Micronaut AOP to record `smtp.messages` and `smtp.send.duration` on `SmtpClient.sendEmail(...)`.
   - Enabled `rabbitmq_prometheus` on port `15692` in `docker-compose.yml` and added the `rabbitmq:15692` scrape job in `config/prometheus/prometheus.yml`.
   - Updated `config/grafana/dashboards/http-service.json` to `"SDD Example - Telemetry"` and added the `HTTP & Runtime`, `Database`, `RabbitMQ`, and `SMTP` sections.

---

## 2. Bugs Found

No bugs were found in the existing codebase during this implementation.
One design subtlety was handled during planning: `SmtpNotificationTransport.sendNotification(...)` catches `IllegalArgumentException` and `IOException` internally and logs them without rethrowing.
Because of this, `@SmtpMetrics` was placed on `SmtpClient.sendEmail(...)` rather than `SmtpNotificationTransport`, so failed network sends are directly observable by the AOP interceptor.

---

## 3. Static Code Analysis Fixes

The `./gradlew build` verification caught four static analysis findings in the new code, which were all fixed in code without suppressions or config changes:

1. **Checkstyle (`VariableDeclarationUsageDistance`)**:
   - In `DbQueryMetricsInterceptorTest`, `interceptor` was declared four lines before its first use.
   - Fix: Moved the `interceptor` declaration immediately before the invocation in both test methods.
2. **PMD (`AvoidCatchingGenericException`)**:
   - Both `DbQueryMetricsInterceptor` and `SmtpMetricsInterceptor` initially used `catch (Exception ex)` to detect failed calls.
   - Fix: Replaced the `catch` block with a `boolean completed = false` flag set after `context.proceed()` and evaluated inside `finally`.
3. **SpotBugs (`CT_CONSTRUCTOR_THROW`)**:
   - Both interceptors initially called `Objects.requireNonNull(meterRegistry, ...)` inside the constructor of a non-final Micronaut bean.
   - Fix: Removed `Objects.requireNonNull` from the constructors, matching `AuthorizationInterceptor`.
4. **SpotBugs (`EI_EXPOSE_REP2`)**:
   - Storing the mutable `MeterRegistry` class directly in the interceptor fields triggered `EI_EXPOSE_REP2` because `config/spotbugs/exclude.xml` only excludes `*Service` and `*PersistenceAdapter`.
   - Fix: Changed the constructor parameter and field to the `jakarta.inject.Provider<MeterRegistry>` interface, which Micronaut injects natively.

---

## 4. Test Coverage and Mutation Testing

Two unit test classes were added as part of the implementation (`DbQueryMetricsInterceptorTest` and `SmtpMetricsInterceptorTest`) using `SimpleMeterRegistry` and Mockito `MethodInvocationContext` mocks.
Both classes test the success (`success` / `sent`) and failure (`error` / `failed`) paths and assert both metric presence and recorded counts.
No additional tests were needed to satisfy coverage thresholds: `./gradlew build` passed Pitest mutation testing with 98% line coverage (`1359/1385`) and 90% mutation coverage (`603/667` killed, 91% test strength).

