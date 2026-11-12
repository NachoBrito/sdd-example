# Feature Journal: Local Environment and Execution Documentation

This document records the development process for the local environment and documentation feature.
The feature provides a Docker Compose configuration for local execution and operational guides with example HTTP requests.

---

## 1. Implementation Process

The implementation followed the Spec-Driven Development workflow.

First, the agent created the feature specification in `specs/007-local-run/spec.md`.
The specification defines local container orchestration, operational guides in `docs/running/`, and example HTTP requests.
The user clarified that telemetry requires both metrics collection and visual dashboards in Grafana.

Next, the agent created the implementation plan and the task list:
- The agent researched container configurations for MariaDB, RabbitMQ, Mailpit, OpenTelemetry Collector, Prometheus, and Grafana.
- The agent defined configuration models, contract specifications, and quickstart validation instructions.
- The agent organized implementation tasks into sequential phases.

Then, the agent implemented the solution components:
- The agent created the `.dockerignore` file.
- The agent created configuration directories for telemetry and dashboards.
- The agent created the OpenTelemetry Collector configuration in `config/otel/otel-collector-config.yaml`.
- The agent created the Prometheus scrape configuration in `config/prometheus/prometheus.yml`.
- The agent created the automated Grafana datasource provisioning in `config/grafana/provisioning/datasources/datasource.yml`.
- The agent created the automated Grafana dashboard provider in `config/grafana/provisioning/dashboards/dashboards.yml`.
- The agent created the HTTP metrics dashboard definition in `config/grafana/dashboards/http-service.json`.
- The agent created the application Dockerfile in `docker/Dockerfile`.
- The agent created the multi-container `docker-compose.yml` file.
- The agent validated the compose file syntax with `docker compose config`.
- The agent created the operational guide in `docs/running/README.md`.
- The agent updated the root `README.md` to link to the running guide.
- The agent created the executable HTTP request suites in `docs/running/requests/products.http` and `docs/running/requests/orders.http`.

Finally, the agent verified documentation links and validated the tasks.

---

## 2. Bugs and Technical Challenges

The agent resolved six technical challenges during implementation.

### Stale Gradle Journal Lock Collision
During container research, Gradle reported a lock timeout on `/home/nacho/.gradle/caches/journal-1/journal-1.lock`.
A previous host process had left a stale lock file.
The agent verified that no active Gradle daemon held the lock.
The agent removed the stale lock file to restore build tool availability.

### Mailpit Port Alignment
The project configuration in `application.yml` specifies default SMTP port `2525`.
Mailpit listens on internal port `1025` by default.
The agent mapped host port `2525` to container port `1025` in `docker-compose.yml`.
This alignment allows both containerized execution and local hybrid execution without configuration changes.

### Missing OpenAPI Properties in Docker Builder Stage
During `docker compose up -d --build`, the builder stage failed at the `:compileJava` task.
The Micronaut OpenAPI annotation processor could not find the `openapi.properties` file in the build workspace.
The processor failed to replace the `${api.version}` placeholder in the output file name.
This failure generated an `IllegalArgumentException` and a compiler warning.
Because the build configuration specifies `-Werror`, the compiler warning halted the compilation.
The agent resolved the issue by copying `openapi.properties` into the builder stage in `Dockerfile`.
This change allows the Micronaut OpenAPI annotation processor to expand the version placeholder during compilation.

### Missing Layered Artifacts in Docker Stage
During the second container build, Docker failed when copying artifacts from the builder stage.
Docker reported that `/workspace/build/docker/main/layers/app/application.jar` was not found.
The Gradle task `dockerfile` writes instructions to a Dockerfile.
The `dockerfile` task does not build the layer files.
The Gradle task `buildLayers` compiles the code and generates the layer files under `build/docker/main/layers/`.
The agent updated the build command in `Dockerfile` to `./gradlew buildLayers --no-daemon -x test`.
This change ensures that Gradle generates all required layer directories and the application archive.

### HTTP Request Payload Schema Inconsistencies
During verification of `docs/running/requests/`, the agent executed all HTTP requests against the live containers.
The product update request failed with status `400 Bad Request`.
The request body lacked the mandatory `sku` and `currency` fields defined in `UpdateProductRequest`.
In addition, the order requests used an outdated property structure:
- Order creation and update requests used `lines` instead of `orderLines`.
- Requests omitted the required `currency` code and `invoiceAddress` object.
- The `deliveryAddress` payload omitted required contact fields and used `postalCode` instead of `zipCode`.
The agent updated `products.http` and `orders.http` to match the REST contract DTOs.
All sixteen requests were re-executed against the service and completed with the expected status codes.
In addition, the agent verified that RabbitMQ events triggered HTML order notification emails in Mailpit.

### Micronaut Docker Plugin Task Collision
The Micronaut Docker plugin checks for a file named `Dockerfile` in the project root.
When `Dockerfile` exists, the plugin changes the task type of `dockerfile` from `MicronautDockerfile` to `Dockerfile`.
This type change causes `./gradlew build` to fail because `build.gradle.kts` expects `MicronautDockerfile`.
The agent placed the file at `docker/Dockerfile` and updated `docker-compose.yml`.
This change keeps `build.gradle.kts` unmodified and allows `./gradlew build` to succeed.

---

## 3. Static Code Analysis

This feature introduces container definitions, configuration files, and documentation.
The feature does not add or modify Java source code in `src/main/java`.
Therefore, static code analysis tools (Checkstyle, PMD, SpotBugs, ArchUnit) reported zero violations.

---

## 4. Test Coverage and Verification

Because this feature does not modify application Java classes, line coverage and Pitest mutation scores remain unchanged.
The existing test suite continues to pass all quality thresholds.

The agent verified the feature through declarative testing:
1. Validated the compose file structure and service dependencies using `docker compose config`.
2. Created RFC 7230 executable `.http` request suites covering all CRUD operations and error responses for products and orders.
3. Verified that all file paths in `docs/running/README.md` and `specs/007-local-run/quickstart.md` resolve accurately.
4. Executed all sixteen requests in `products.http` and `orders.http` against the running containers and confirmed correct status codes and side effects.
