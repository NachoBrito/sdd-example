# Feature Journal: Load Testing and Performance Validation

This document records the development process for the load testing and performance validation feature.
The feature introduces automated performance testing tooling using Grafana k6 against the local Docker Compose environment.

---

## 1. Implementation Process

The implementation followed the Spec-Driven Development workflow.

First, the agent created the feature specification in `specs/009-load-testing/spec.md`.
The specification defines four prioritized user scenarios:
- Product catalog performance (P1).
- Order processing throughput (P2).
- Mixed realistic e-commerce shopping journeys (P3).
- Sustained stability and endurance validation (P4).

Next, the agent created the implementation plan, research decisions, data model, contracts, and quickstart guide:
- The agent evaluated k6 against Gatling, Locust, and JMeter.
- The agent selected Grafana k6 because of low resource overhead, Docker execution, and built-in exit codes on threshold violations.
- The agent designed modular scenarios in JavaScript and an automated runner script in Bash.
- The agent generated 15 execution tasks in `specs/009-load-testing/tasks.md`.

Then, the agent implemented the solution components:
- The agent created the load testing directory structure (`load-tests/helpers/` and `load-tests/scenarios/`).
- The agent implemented the shared synthetic payload generator in `load-tests/helpers/payloads.js`.
- The agent created the executable runner script in `load-tests/run.sh` with argument parsing and connectivity checks.
- The agent implemented the product catalog scenario in `load-tests/scenarios/products.js`.
- The agent implemented the order processing scenario in `load-tests/scenarios/orders.js`.
- The agent implemented the mixed e-commerce scenario in `load-tests/scenarios/mixed.js`.
- The agent implemented the stability scenario in `load-tests/scenarios/stability.js`.
- The agent created the operational guide in `docs/running/load-testing.md`.
- The agent updated the local execution guide in `docs/running/README.md`.

Finally, the agent validated all scenarios against the running Docker environment and confirmed that all performance thresholds passed.

---

## 2. Bugs Found and Technical Challenges

The agent identified four technical challenges during implementation.

### Docker Socket Permission in Sandbox
The agent initially attempted to run `docker compose ps` within the sandboxed command environment.
The command failed because access to `/var/run/docker.sock` is outside the sandboxed file boundaries.
The agent resolved this by executing Docker container and compose commands outside the sandbox.

### Order Product Reference Invariant
The order management service enforces that orders must reference existing, active products.
If an order contains a non-existent product identifier, the service rejects the request with HTTP 400 Bad Request.
The agent resolved this by adding a setup lifecycle in `load-tests/scenarios/orders.js`, `mixed.js`, and `stability.js`.
The setup function queries active products or seeds initial products before virtual users execute order placements.
This ensures all order lines reference valid product records.

### Network Host Mapping Across Operating Systems
In Linux environments, Docker supports the `--network host` flag to communicate directly with host ports.
On macOS and Windows, Docker executes in a virtual machine where host ports are reachable via `host.docker.internal`.
The agent resolved this by detecting the operating system in `load-tests/run.sh` and translating `localhost` targets appropriately.

### Grafana Metric Names and Datasource UID Mismatch
During load test verification, Grafana displayed no telemetry data on the HTTP metrics dashboard.
The agent diagnosed three contributing causes:
1. **Metric Unit Name**: The dashboard queried `http_server_requests_seconds_count` and `http_server_requests_seconds_bucket`.
   Micronaut Micrometer with OTLP exports duration metrics in milliseconds (`http_server_requests_milliseconds_count`).
   The agent corrected the dashboard queries to target `http_server_requests_milliseconds_*`.
2. **Datasource UID**: Dashboard panels specified datasource UID `prometheus`.
   Grafana provisioning generated an arbitrary UID (`PBFA97CFB590B2093`) when `uid` was omitted in `datasource.yml`.
   The agent configured explicit `uid: prometheus` in `datasource.yml` and restarted Grafana.
3. **Missing Histogram Distribution**: Micrometer disabled duration histogram buckets by default.
   The agent enabled `micronaut.metrics.binders.web.server.histogram: true` in `application.yml`.
   This change generates exponential histogram buckets and allows `histogram_quantile` calculations.

---

## 3. Static Code Analysis

The feature added standalone load testing scripts and documentation outside the Java source tree (`src/`).
The agent executed `./gradlew build` to verify the project status.
All static analysis checks passed with zero errors or warnings:
- Checkstyle: Passed with zero violations.
- PMD: Passed with zero violations.
- SpotBugs: Passed with zero bugs found.
- ArchUnit: Passed with zero architectural rule violations.

The agent did not need to adjust build configurations or suppress any analysis rules.

---

## 4. Test Coverage and Mutation Testing

The existing Java application test suite enforces:
- Minimum line coverage threshold of 80 percent.
- Minimum mutation score threshold of 90 percent via Pitest.

The load testing feature did not alter or introduce production Java application code.
All existing unit tests, integration tests, and Pitest mutation tests passed cleanly (`19 actionable tasks: 1 executed, 18 up-to-date`).
The load testing suite itself serves as an automated performance regression barrier.
The suite validates system throughput, error rates, and 95th percentile response times below 500 milliseconds.

---

## 5. Incident Report: Missing Metrics in Grafana Dashboard

### Incident Summary
The user executed `./load-tests/run.sh` against the local Docker Compose environment.
All k6 performance thresholds passed.
The user opened the Grafana dashboard at `http://localhost:3000/d/sdd-http-service/sdd-example-http-service-metrics`.
All dashboard panels reported "No data".

### Symptoms and Investigation
The agent examined the telemetry pipeline:
- MariaDB, RabbitMQ, and the application container operated normally.
- The OpenTelemetry Collector received OTLP metrics on port 4317.
- Prometheus scraped the collector metrics on port 8889 successfully.
- Grafana returned empty metric arrays for all dashboard queries.

The agent identified three root causes:

1. **Datasource UID Mismatch**:
   The dashboard panels referenced a datasource with `uid: prometheus`.
   The datasource provisioning file `datasource.yml` omitted the `uid` field.
   Grafana generated an arbitrary UID (`PBFA97CFB590B2093`) upon initial startup.
   Grafana could not match the dashboard queries to the active datasource.

2. **Metric Unit Mismatch (Seconds vs Milliseconds)**:
   The dashboard queried `http_server_requests_seconds_count` and `http_server_requests_seconds_bucket`.
   Micronaut Micrometer with OTLP exports duration metrics in milliseconds.
   The exported metrics in Prometheus were named `http_server_requests_milliseconds_*`.
   The queried metrics did not exist in the Prometheus database.

3. **Disabled Histogram Buckets**:
   Micronaut disables HTTP duration histogram buckets by default.
   The `histogram_quantile` function requires `_bucket` time series to compute response latencies.
   Without histogram buckets, latency queries returned empty results.

### Corrective Actions
The agent applied four corrective changes:

1. **Explicit Datasource UID**:
   The agent updated `config/grafana/provisioning/datasources/datasource.yml`.
   The agent configured explicit `uid: prometheus`.
   The agent added a `deleteDatasources` directive to remove the previous datasource entry.

2. **Metric Query Alignment**:
   The agent updated `config/grafana/dashboards/http-service.json`.
   The agent changed the metric targets to `http_server_requests_milliseconds_count`.
   The agent added a fallback expression to calculate average latency from request sums.
   The agent wrapped heap memory metrics in `sum()` to aggregate all JVM memory pools.

3. **Histogram Configuration**:
   The agent added `micronaut.metrics.binders.web.server.histogram: true` in `src/main/resources/application.yml`.
   This configuration activates histogram bucket publication for incoming HTTP requests.

4. **Container Rebuild and Verification**:
   The agent rebuilt the application image with `docker compose up -d --build app`.
   The agent restarted the Grafana container with `docker restart sdd-grafana`.
   The agent executed the load test scenario `mixed.js`.
   The agent verified through the Grafana HTTP API that all panels receive live telemetry data.


