This Java project defines a REST API. The project setup will follow these practices:

Basic project configuration & practices:

- We will use Java 25 as the language version.
- We will use OpenAPI to document our spec.
- We will version our endpoints using versioned paths (e.g. /v1/endpoints, /v2/endpoints, etc). We will always maintain backwards compatibility.
- We will use gradle as the project automation tool.
- We will use the latest Micronaut version available as a framework.
- We will use liquibase for database migration.
- We will use Docker for containerization.
- We will use Conventional Commits for commit messages.
- We will use Semantic Versioning for versioning. 
- We will use MariaDB as the database.
- We will use RabbitMQ as the event bus system.
- We will use micrometer for opentelemetry observation.
- We always prefer Miconaut-native solutions over reflection-based alternatives: Micronaut Data instead of Hibernate/JPA, Micronaut Validation instead of Hibernate Validator, Micronaut sourcegen instead of Lombok, etc.

Testing:

- We will use Junit 5 for testing, with Mockito for mocking and REST Assured for functional and integration testing.
- We will use Micronaut TestResources for local database and message queue setup.

Static analysis & test coverage:

- We will use ArchUnit for architectural compliance testing.
- We will use SpotBugs, PMD and Checkstyle (google's java style) for code quality.
- We will use pitest for mutation testing: 80% line coverage, 90% mutation coverage.
- The build must fail if any rule is violated. Run all the checks on every build.

Architecture & Design:

- We will use hexagonal architecture and domain driven design.

Tasks:

1. Generate gradle configuration
2. Configure every tool with strict rules following industry standards. 
3. Generate AGENTS.md with the following principles:

   - Always run the build command to ensure that all the checks pass.
   - In case of error, analyze the error and fix the code. Never modify project build configuration without explicit authorization.
   - Apply AOP principles for crosscutting concerns (logging, security, transactions, etc).
   - Always use the ASD-STE100 standard when writing documentation, commit messages and source comments.

4. Document the generated setup in docs/project-setup.md
5. Document all the static analysis rules and thresholds configured in docs/static-analysis.md (linked from docs/project-setup.md)
5. Generate introductory README.md, with a link to docs/project-setup.md

