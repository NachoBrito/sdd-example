# sdd-example

`sdd-example` is a REST API project built with Java 25 and the Micronaut framework.
The project follows Hexagonal Architecture and Domain-Driven Design principles.

## Features

- **Language and Framework**: Java 25 and Micronaut 5.2.5.
- **Architecture**: Hexagonal architecture with isolated domain models and ports.
- **API Specification**: OpenAPI 3 with versioned URL paths (`/v1/...`).
- **Authentication & Authorization**: JWT bearer authentication (`/v1/auth/*`) with `ADMIN`, `CUSTOMER`, and `CATALOG` roles.
- **Database**: MariaDB with automated Liquibase migrations.
- **Event Bus**: RabbitMQ for asynchronous event messaging.
- **Telemetry**: OpenTelemetry metric collection through Micrometer and structured JSON logging.
- **Reflection-Free Design**: Micronaut Data, Micronaut Validation, Micronaut Serialization, and Micronaut Sourcegen.
- **Containerization**: Docker container setup with Eclipse Temurin JRE 25.
- **Strict Quality Gates**: Google Java Style (Checkstyle), PMD, SpotBugs, ArchUnit, and Pitest mutation testing.

## Prerequisites

- **Java**: Java 25. The Gradle Foojay toolchain resolver downloads a compliant JDK automatically if none is installed.
- **Docker**: Docker must run locally. Micronaut Test Resources uses Docker to manage MariaDB and RabbitMQ containers during test runs.

## Quick Start

### Build and Verify the Application

Run the complete build with all tests and static analysis gates:

```bash
./gradlew build
```

### Run Static Analysis and Tests Only

Run the verification tasks without creating deployment archives:

```bash
./gradlew check
```

### Run the Application

Start the local Micronaut HTTP server:

```bash
./gradlew run
```

### Run Locally with Docker Compose

Start the application with all dependencies (database, message broker, email server, and telemetry):

```bash
docker compose up -d --build
```

Read [Running the Service Locally](docs/running/README.md) for detailed execution instructions and example requests.

## Documentation

- [Running Locally](docs/running/README.md): Procedures to run the service and backing infrastructure with Docker Compose.
- [Project Setup](docs/project-setup.md): Complete guide to architecture, libraries, conventions, and operational tasks.
- [Static Analysis](docs/static-analysis.md): Rules, thresholds, and configurations for Checkstyle, PMD, SpotBugs, ArchUnit, and Pitest.
- [Agent Instructions](AGENTS.md): Operational rules and coding constraints for AI coding agents.
- [Project Constitution](.specify/memory/constitution.md): Core architectural principles and governance hierarchy.

