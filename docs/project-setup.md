# Project Setup and Conventions

This document describes the technical configuration, architecture, and standards for the `sdd-example` project.

## 1. Technology Stack

The project uses the following components:

- **Language**: Java 25.
- **Framework**: Micronaut 5.2.5.
- **Build tool**: Gradle 9.9.0 with Kotlin DSL and Gradle Version Catalog (`gradle/libs.versions.toml`).
- **Database**: MariaDB.
- **Database migrations**: Liquibase.
- **Event bus**: RabbitMQ.
- **API specification**: OpenAPI 3 with compile-time generation.
- **Telemetry**: Micrometer with OpenTelemetry (OTLP) metrics exporter.
- **Logging**: Logback with structured JSON encoder.
- **Containerization**: Docker using Eclipse Temurin JRE 25 images.

## 2. Micronaut-Native Libraries

The project prohibits reflection-heavy libraries. Use Micronaut-native compile-time solutions exclusively:

- Use **Micronaut Data JDBC** instead of Hibernate or JPA.
- Use **Micronaut Validation** instead of Hibernate Validator.
- Use **Micronaut Serialization (Serde)** instead of Jackson Databind.
- Use **Micronaut Sourcegen** instead of Lombok.

ArchUnit checks enforce these restrictions automatically during the build.

## 3. Architecture and Package Layout

The application follows Hexagonal Architecture (Ports and Adapters) and Domain-Driven Design (DDD).

All code resides under the base package `es.nachobrito.sdd.example`:

```
es.nachobrito.sdd.example
├── domain                  # Pure business logic and domain models. Zero framework imports.
│   └── model
├── application             # Use cases and boundary contracts.
│   └── port
│       ├── in              # Inbound ports (use case interfaces).
│       └── out             # Outbound ports (repository and messaging interfaces).
├── adapter
│   ├── in                  # Inbound adapters.
│   │   ├── rest            # REST controllers (@Controller).
│   │   └── messaging       # RabbitMQ consumers (@RabbitListener).
│   └── out                 # Outbound adapters.
│       ├── persistence     # Data repositories (@Repository).
│       └── messaging       # RabbitMQ publishers (@RabbitClient).
└── Application.java        # Application bootstrap entry point.
```

### Architectural Rules

- **Domain isolation**: The domain layer must not depend on application, adapter, or framework packages.
- **Application ports**: Inbound adapters invoke application use cases through inbound ports. Outbound adapters implement outbound ports.
- **Adapter segregation**: Inbound adapters must not call outbound adapters directly.

Read [Static Analysis](static-analysis.md) for detailed architectural verification rules.

## 4. API Design and Versioning

- **Versioned paths**: All REST controllers must expose versioned paths (for example, `/v1/resources`, `/v2/resources`).
- **Backward compatibility**: Existing endpoint contracts must remain backward compatible. Increment the major version path for breaking changes.
- **OpenAPI documentation**: Controllers must declare OpenAPI annotations. Micronaut generates the OpenAPI document at build time.
- **Error handling**: Use Micronaut `ExceptionHandler` classes instead of error controllers. Assign exactly one `ExceptionHandler` class to each REST controller. Domain exceptions extend a module-specific base exception (for example, `ProductException`).

## 5. Database and Migrations

- Liquibase manages all database schema changes.
- The master change log resides at `src/main/resources/db/changelog/db.changelog-master.xml`.
- Add new change sets as independent XML or YAML files in that directory.
- Apply the expand-then-contract migration pattern. Do not modify merged change sets.

## 6. Testing Strategy

- **Unit tests**: JUnit 5 tests execute fast business and domain validation without mocks.
- **Mocking**: Mockito provides mock objects when necessary. The build attaches the Mockito Java agent for JDK 25 compatibility.
- **Integration testing**: REST Assured tests verify HTTP endpoints against real running server instances.
- **Test resources**: Micronaut Test Resources starts real MariaDB and RabbitMQ test containers automatically when Docker is running.
- **Mutation testing**: Pitest enforces 80% line coverage and 90% mutation score thresholds.

## 7. Operational Commands

### Compile the Code

```bash
./gradlew compileJava
```

### Run Tests and Quality Checks

```bash
./gradlew check
```

### Package the Application

```bash
./gradlew build
```

### Generate the Dockerfile

```bash
./gradlew dockerfile
```

## 8. Development Standards

- **Commit messages**: Use [Conventional Commits](https://www.conventionalcommits.org/) (for example, `feat(auth): add login endpoint`).
- **Version management**: Follow [Semantic Versioning](https://semver.org/) (MAJOR.MINOR.PATCH) in `gradle.properties`.
- **Documentation**: Write all documentation, commit messages, and code comments in ASD-STE100 Simplified Technical English.
- **Agent instructions**: Consult [AGENTS.md](../AGENTS.md) before writing code.

