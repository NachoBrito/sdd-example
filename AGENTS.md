# AGENTS.md

Instructions for coding agents that work on this repository.

Read the project constitution in [.specify/memory/constitution.md](.specify/memory/constitution.md) first. The constitution has precedence over this file.

## 1. Build and verify

- Always run the build command after a change:

  ```bash
  ./gradlew build
  ```

- The build runs all the checks: compilation with warnings as errors, unit and integration tests, ArchUnit, Checkstyle, PMD, SpotBugs, and Pitest. A change is not complete until the build passes.
- Docker must be available. Micronaut Test Resources uses it to start MariaDB and RabbitMQ for the tests.

## 2. Fix errors in the code, not in the build

- If the build fails, read the error and the report (`build/reports/`). Find the root cause and fix the code.
- Do not change the build configuration without explicit authorization from the user. This includes:
  - `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`, `gradle/libs.versions.toml`, and the Gradle wrapper.
  - All the files in `config/` (Checkstyle, PMD, SpotBugs).
  - The ArchUnit rules in `src/test/java/**/architecture/`.
  - The thresholds for coverage and mutation testing.
- Do not add suppressions (`@SuppressWarnings`, `@SuppressFBWarnings`, `// NOPMD`, `CHECKSTYLE:OFF`) without explicit authorization from the user.

## 3. Use AOP for crosscutting concerns

- Use Aspect-Oriented Programming (Micronaut AOP) for crosscutting concerns: logging, security, transactions, metrics, tracing, retries, and validation.
- Use the Micronaut annotations and interceptors (`@Transactional`, `@Secured`, `@Retryable`, `@Timed`, `@Valid`, `@Around` with a `MethodInterceptor`). Do not write this logic in each business method.
- Keep the domain model free of crosscutting code. Apply the interceptors on the application services and the adapters.

## 4. Write documentation in ASD-STE100

Always use ASD-STE100 Simplified Technical English when you write documentation, commit messages, and source comments:

- Use short sentences. Procedural sentences: 20 words maximum. Descriptive sentences: 25 words maximum.
- Write one instruction in each sentence.
- Use the active voice. Use the imperative form for instructions.
- Use only approved words, with one meaning for each word. Use the technical names from this project.
- Do not use phrasal verbs, contractions, or slang.
- Use articles ("the", "a") where possible.

## 5. Project conventions

- **Architecture**: hexagonal architecture and Domain-Driven Design. Packages under `es.nachobrito.sdd.example`:
  - `domain` — the domain model. Plain Java. No framework dependencies.
  - `application` — use cases and ports (`application.port.in`, `application.port.out`).
  - `adapter.in` — inbound adapters (`rest`, `messaging`).
  - `adapter.out` — outbound adapters (`persistence`, `messaging`).
- **REST**: use versioned paths (`/v1/...`, `/v2/...`). Always keep backward compatibility. Document the API with OpenAPI annotations.
- **Libraries**: use Micronaut-native solutions. Use Micronaut Data, Micronaut Validation, Micronaut Serialization, and Micronaut Sourcegen. Do not use Hibernate/JPA, Hibernate Validator, Jackson Databind, or Lombok.
- **Database**: use Liquibase change sets in `src/main/resources/db/changelog/`. Do not change a change set after it is merged.
- **Commits**: use [Conventional Commits](https://www.conventionalcommits.org/). Example: `feat(orders): add the order creation endpoint`.
- **Versions**: use [Semantic Versioning](https://semver.org/). The version is in `gradle.properties`.

Read [docs/project-setup.md](docs/project-setup.md) and [docs/static-analysis.md](docs/static-analysis.md) for more information.

