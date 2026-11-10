# sdd-example Constitution

## Core Principles

### 1. Minimum Viable Diff (Strict Scope Discipline)
- **Rationale**: Unrequested code is an immediate liability: it introduces surface area for bugs, increases cognitive load for reviewers, bloats dependencies, and invalidates existing test suites. AI coding agents possess a systematic bias toward speculative completeness, unprompted refactoring, and adding unrequested configuration. Strict scope discipline ensures that every change is intentional, auditable, and traceable to a verified requirement.
- **Rule**:
  - Implement ONLY what is explicitly requested—nothing more.
  - MUST NOT add unrequested features, speculative configuration, extra CLI flags, defensive wrappers for impossible states, or "while I'm here" refactors of adjacent code.
  - Every single changed line in a diff MUST trace directly to an explicit user requirement or acceptance criterion.
  - If a cleaner design strictly requires modifying unrelated files, the agent MUST propose the refactoring in one sentence and wait for explicit approval before editing.
- **How To Apply**:
  - *Diff Review*: A reviewer must be able to inspect every modified line and ask "which requirement mandated this line?" If no requirement exists, the diff MUST be rejected without debate.
  - *Violation*: Adding `--format=yaml` when the user asked only for JSON output; adding null checks on an internal private helper whose caller is already strictly typed; cleaning up variable names or formatting in files adjacent to the feature.
  - *Compliant*: Modifying only the target function to satisfy the stated requirement, maintaining exact surrounding formatting and conventions.

### 2. Ambiguity Halts Execution (Ask, Never Assume)
- **Rationale**: Guessing requirements, data contracts, persistence semantics, or edge-case behavior produces plausible-looking implementations that solve the wrong problem. Reworking assumed code is exponentially more expensive than clarifying upfront. Halting execution on ambiguity preserves developer trust and prevents silent architectural drift.
- **Rule**:
  - MUST NOT guess business rules, data contracts, edge-case behavior, persistence semantics, or missing requirements.
  - When a prompt or spec contains multiple valid interpretations or missing constraints, the agent MUST stop before writing implementation code.
  - The agent MUST ask at most 3 concise, high-leverage clarifying questions focusing on decision forks.
  - MUST wait for explicit user confirmation before proceeding with implementation.
- **How To Apply**:
  - *Diff Review*: Reject any diff that unilaterally resolves an underspecified product rule or chooses an unconfirmed fallback strategy without documented clarification.
  - *Violation*: Silently choosing to drop malformed input records instead of aborting the operation when the specification did not define error-handling behavior; inventing a default expiration TTL not mentioned in the request.
  - *Compliant*: Halting after reading the task, identifying that two retention policies are possible, and asking: "Should expired tokens be soft-deleted or permanently purged?"

### 3. Do Not Distribute by Default
- **Rationale**: Distributed systems introduce network latency, partial failure modes, data consistency challenges, serialization overhead, and operational complexity. Monoliths and single-process executables scale far beyond typical operational demands when vertically scaled. Coordination is a cost measured in round trips, not milliseconds.
- **Rule**:
  - Vertical scaling and a single deployable process/binary MUST be the default architecture.
  - MUST NOT introduce a new process, microservice, background worker, message queue, or distributed cache without explicit user requirement or written architectural justification.
  - Introducing a new process, service, or queue MUST be justified by one of exactly four criteria:
    1. Working-set overflow (memory or compute exceeds the capacity of single-node hardware).
    2. Genuinely independent compute or resource scaling profiles (e.g., bursty GPU workloads vs. lightweight API).
    3. Geographic latency requirements (data must reside in a specific jurisdiction or close to edge users).
    4. Organizational independence (decoupled deployment lifecycles for separate teams with distinct SLAs).
- **How To Apply**:
  - *Diff Review*: Reject any diff introducing Celery, Redis queues, Kafka, or split microservices when an in-process thread pool, channel, or synchronous handler satisfies the requirement.
  - *Violation*: Introducing an asynchronous message broker for an audit log write in a service processing 10 requests per second.
  - *Compliant*: Handling audit logging in-process or via existing database transactions until volume measurements prove single-node limits.

### 4. Optimize for Deletion, Not Extension
- **Rationale**: Code is read and deleted far more often than it is extended through open-ended abstractions. Generic base classes, premature interfaces, and speculative extension points create rigid coupling and make systems painful to refactor. Code designed to be deleted can be replaced in hours when requirements evolve.
- **Rule**:
  - Every module MUST be small and self-contained enough that one engineer can delete and rewrite it in a single day.
  - MUST NOT introduce speculative abstractions, generic base classes, factory layers, or premature interfaces without at least two distinct concrete implementations existing in the codebase.
  - Inline logic until it hurts. Duplication below three occurrences is cheaper and safer than the wrong abstraction.
- **How To Apply**:
  - *Diff Review*: Flag any interface with only one concrete implementation, or any abstract class introduced "for future extensibility."
  - *Violation*: Creating `AbstractRepository<T>` and `RepositoryFactory` when the application only ever connects to PostgreSQL for a single entity type.
  - *Compliant*: Writing direct, readable database queries inside the module; extracting shared logic only when the identical query pattern appears in three distinct locations.

### 5. Make Dependencies Explicit
- **Rationale**: Hidden coupling, implicit global state, ambient contexts, and import-time side effects destroy modularity, make parallel testing impossible, and create unpredictable runtime bugs. When dependencies are declared explicitly, code structure is transparent and easily reasoned about in isolation.
- **Rule**:
  - A reader MUST be able to see every external dependency of a function in its signature or at the top of its file.
  - MUST NOT use implicit global state, mutable module-level variables, hidden singletons, or thread-local contexts for domain logic.
  - Modules MUST NOT execute import-time side effects (e.g., establishing database connections, opening network sockets, or mutating global registries upon import).
  - Use explicit dependency injection over singleton lookup or ambient service locators.
- **How To Apply**:
  - *Diff Review*: Reject functions that call out to global connection pools or read environment variables deep inside business logic.
  - *Violation*: `def calculate_tax(order): db = GlobalContext.get_db(); ...` or connecting to a database at the module level during `import my_module`.
  - *Compliant*: `def calculate_tax(order: Order, tax_rates: TaxRateTable) -> Decimal: ...` with dependencies passed directly from the caller.

### 6. Contract at the Boundary, Not in the Middle
- **Rationale**: Leaking transport, serialization, or persistence schemas into domain models creates fragile, cascading breakages across the codebase. Semantic reconciliation must happen where external data enters or leaves the system, owned by the boundary that understands both external and internal contexts.
- **Rule**:
  - Every producer/consumer boundary (HTTP endpoints, message queues, file interfaces, databases) MUST have a versioned, explicit schema.
  - Semantic reconciliation and data validation MUST happen strictly at the boundary.
  - MUST NOT share mutable schemas across decoupled boundaries.
  - Data transformations MUST preserve explicit invariants; never silently discard, reinterpret, or fabricate data without explicit user instruction.
- **How To Apply**:
  - *Diff Review*: Ensure that all incoming payloads are validated at the perimeter (e.g., via strict DTOs or schema validators) before domain logic executes. Ensure internal domain objects never leak raw external dicts.
  - *Violation*: Passing raw `request.json` dictionaries deep into internal service methods or database queries; silently dropping malformed CSV rows without logging or error notification.
  - *Compliant*: Validating external HTTP requests into immutable, typed input models at the controller boundary, and translating domain models to explicit versioned response schemas upon exit.

### 7. Test the Transformation, Not the Plumbing
- **Rationale**: Testing trivial getters, setters, standard library behavior, or heavily mocked framework plumbing produces brittle test suites that give false confidence while failing to catch actual regressions. High-value testing concentrates on pure transformation logic and true boundary verification.
- **Rule**:
  - Unit tests MUST cover pure transformation logic, domain calculations, and state transitions.
  - Integration tests MUST cover system boundaries and verify external contracts against real or high-fidelity boundary components.
  - MUST NOT mock code that you own; DO mock uncontrolled third-party services and external infrastructure.
  - MUST NOT write tautological tests for getters, setters, or framework internals.
  - When fixing a bug, the CI suite MUST include a new test reproducing the bug that failed prior to the fix. A green CI without a failing test for the bug you just fixed is not green.
- **How To Apply**:
  - *Diff Review*: Reject tests that mock internal collaborator classes or verify that a method simply forwarded an argument to another method. Reject any bug-fix PR that does not add a regression test replicating the defect.
  - *Violation*: Testing that `user.get_name()` returns `"Alice"`, or setting up 8 mock objects to test a 2-line controller that merely calls a service.
  - *Compliant*: Testing a complex pricing calculation with parameterized edge cases (zero, negative, overflow, discounts) without any mocks.

### 8. Emit Structured Events, Derive Everything Else
- **Rationale**: Unstructured log lines (plain strings, ad-hoc string formatting) are impossible to reliably filter, aggregate, or alert on across production environments. Logs, metrics, and traces are merely different projections of one core primitive: the structured event.
- **Rule**:
  - All logging and telemetry in new code MUST be emitted as structured events (key-value pairs or structured JSON).
  - MUST NOT emit unstructured log strings in new code.
  - High-cardinality fields (`user_id`, `request_id`, `tenant_id`, `feature_flag_state`) MUST be included in the structured event context when available.
  - MUST use existing project logging primitives; MUST NOT introduce new observability libraries, APM frameworks, or telemetry SDKs unless explicitly requested.
- **How To Apply**:
  - *Diff Review*: Scan diffs for `print()` calls, string concatenation in logs, or newly imported logging frameworks.
  - *Violation*: `logger.info(f"User {uid} failed to login from IP {ip}")` or adding an OpenTelemetry SDK dependency to a project already using standard structured logging.
  - *Compliant*: `logger.info("auth_login_failed", extra={"user_id": uid, "client_ip": ip, "reason": "invalid_credentials"})` using the existing project logger.

### 9. Recovery Over Prevention
- **Rationale**: Absolute failure prevention in software systems is impossible and pursuing it yields diminishing returns. Designing systems for rapid, automated, and low-risk recovery minimizes MTTR (Mean Time to Recovery) and blast radius far more effectively than complex preventive machinery.
- **Rule**:
  - Every change MUST be revertible in under five minutes without requiring code changes or hotfixes.
  - Feature flags MUST gate risky paths when a flagging mechanism already exists in the project. (Do NOT construct a new feature flagging framework unprompted).
  - Database migrations MUST follow the expand-then-contract pattern (forward-compatible schema expansion first, data migration second, contract/cleanup last across separate deployments).
  - Rollback capability MUST be verified as part of deployment planning, not assumed.
- **How To Apply**:
  - *Diff Review*: Reject database migrations that drop or rename active columns in a single step. Verify that risky code paths can be toggled off instantly via existing flags or configuration.
  - *Violation*: Renaming a column in production SQL in a single migration, which causes immediate downtime for existing running instances.
  - *Compliant*: Adding a new nullable column, deploying code that writes to both columns, backfilling existing rows, deploying code that reads from the new column, and dropping the old column in a subsequent release.

### 10. Attention Is Finite
- **Rationale**: Human attention is a scarce operational resource. Unactionable alerts, noisy notifications, and sprawling dashboards lead to alert fatigue, causing engineers to overlook genuine production outages. Every operational signal must earn its place.
- **Rule**:
  - Every alert MUST correspond directly to a user-visible symptom or critical capacity degradation and MUST link to an actionable runbook.
  - MUST NOT create alerts for raw component thresholds (e.g., CPU > 80%) unless directly correlated with user-facing degradation (e.g., error rate spike or latency breach).
  - Dashboards MUST be curated saved queries answering specific diagnostic questions during incidents, not decorative telemetry walls.
  - Observability signals, monitors, or alerts that have not fired a useful alert in 90 days SHOULD be deleted.
- **How To Apply**:
  - *Diff Review*: Reject alert configurations that lack a runbook URL, clear severity justification, or clear remediation steps.
  - *Violation*: Adding a PagerDuty page on arbitrary disk utilization without defining what the on-call engineer must do when paged.
  - *Compliant*: Alerting on `API_5xx_Rate > 1% for 3m` with runbook link `https://wiki.internal/runbooks/api-5xx` outlining immediate mitigation steps.

### 11. Value Is Realized at the User, Not at Merge
- **Rationale**: Code residing on a feature branch or merged into `main` provides zero user value while carrying latent inventory risk. A task is not done when tests pass locally; it is done only when safely deployed, verified in the target environment, observable, and confirmed stable.
- **Rule**:
  - A task or pull request is NOT complete until the change is verified in its target environment, observable via telemetry, and confirmed revertible.
  - "Shipped" strictly means deployed, instrumented, and monitored in the hands of users—never merely merged to the default branch.
  - Production Deployment Guardrail: Merging or local verification does NOT authorize autonomous deployment by coding agents; production deployments require explicit human instruction.
- **How To Apply**:
  - *Diff Review*: Acceptance criteria and PR descriptions must define user verification steps, observable metrics, and post-deployment validation.
  - *Violation*: Closing a feature ticket or marking an implementation task complete the moment the unit test suite passes locally.
  - *Compliant*: Verifying the change in staging/production, confirming the structured event appears in logs, and providing a verifiable smoke-test result before marking the task complete.

### 12. Commands Are Discoverable; Local Dev Matches CI
- **Rationale**: Differences between local developer environments and CI runners create "works on my machine" failures, increase onboarding friction, and make debugging pipeline failures painful. Discoverable, uniform interfaces eliminate hidden tribal knowledge.
- **Rule**:
  - Every repeatable developer action (`build`, `test`, `lint`, `format`, `migrate`, `deploy`, `seed`) MUST be a single, named command listed in one central location (e.g., `Makefile`, package scripts, or task runner).
  - Commands MUST run without hidden arguments, undocumented environment variables, or secret prerequisites.
  - The exact command a developer runs locally MUST be the identical command executed in CI pipelines.
  - MUST NOT use CI-only shell scripts, hidden build flags, or undocumented build steps. If a new contributor cannot discover and run every core command in 30 seconds, the interface is defective.
- **How To Apply**:
  - *Diff Review*: Reject any CI configuration change that executes raw multi-line shell scripts or commands not runnable locally through the primary command runner.
  - *Violation*: Writing a complex 15-line bash loop directly in `.github/workflows/ci.yml` that cannot be executed locally via `make test`.
  - *Compliant*: Defining `test: ...` in the `Makefile` and calling `make test` both from the local terminal and from the CI runner.

## Over-Engineering Defense & Operational Guardrails

### Crucial Meta-Rule: Anti-Gold-Plating Mandate
The coding agent MUST NOT use architectural, testing, or observability principles as an excuse to build unrequested frameworks, speculative infrastructure, or complex abstractions.
- Principles 3 through 12 govern how to build *what was requested*; they NEVER authorize building *what was not requested*.
- The agent MUST NOT unilaterally introduce dependency injection frameworks, event-sourcing engines, schema registries, microservice chassis, or observability pipelines under the banner of "best practices."
- Minimal in-place logic always beats generic infrastructure.

### Infrastructure Escalation Protocol
If satisfying an architectural principle—such as boundary schema validation (Principle 6), structured event logging (Principle 8), or recovery mechanisms (Principle 9)—appears to require new libraries, external infrastructure, or architectural tooling not currently present in the codebase:
1. The agent MUST PAUSE immediately before implementation.
2. The agent MUST ask the user whether to build minimal supporting infrastructure or use a localized, lightweight stub.
3. The agent MUST NOT proceed with infrastructure creation until receiving explicit user approval.

### Data Invariant & Semantic Integrity
- Data transformations MUST preserve explicit invariants: never silently discard, reinterpret, truncate, or fabricate data without an explicit specification.
- Boundary parsers that encounter malformed data MUST either reject the payload explicitly or record a structured failure event; silent degradation is strictly forbidden.

## Verification, Quality Gates & Review Standards

### Decision-Ready Diff Audit Gate
Every code change must pass an objective, non-interpretive review prior to acceptance:
1. **Traceability**: Every changed line maps to an explicit requirement in the user request or feature specification. (Principle 1)
2. **Ambiguity Resolution**: No unverified business assumptions or invented fallback behaviors exist in the code. (Principle 2)
3. **No Speculative Abstraction**: No single-implementation interfaces, empty extension points, or generic base classes. (Principle 4)
4. **Dependency Transparency**: All new dependencies are passed explicitly; no global singletons or import-time side effects. (Principle 5)
5. **Boundary Contracts**: All inputs crossing system boundaries are strictly validated against versioned schemas. (Principle 6)
6. **Regression Verification**: Every bug fix is accompanied by an automated test that reproduces the defect and fails without the fix. (Principle 7)
7. **Telemetry Hygiene**: No unstructured log calls (`print`, unkeyed string formatting); existing logging primitives used. (Principle 8)
8. **Command Uniformity**: Any new build, test, or lint step is exposed as a single named command runnable locally and in CI. (Principle 12)

## Governance

### Precedence Hierarchy
Governance in `sdd-example` is strictly ordered to prevent architectural drift and agent overreach:
1. **Priority 1 (Absolute Precedence)**:
   - **Principle 1 (Minimum Viable Diff)**
   - **Principle 2 (Ambiguity Halts Execution)**
2. **Priority 2 (Architectural & Operational Standards)**:
   - Principles 3 through 12, operational guardrails, and quality gates.

*Conflict Rule*: If a requirement of Principles 3 through 12 conflicts with Principle 1 (e.g., adding full structured logging or contract schemas would require modifying ten unrelated files or importing unrequested libraries), Principle 1 prevails. The agent MUST halt and follow the Infrastructure Escalation Protocol.

### Amendment Procedure
Amendments to this constitution must be deliberate and documented:
1. Proposed principle additions, removals, or modifications MUST be accompanied by a concrete rationale and impact assessment.
2. Changes require explicit review and approval by the project owner.
3. An amendment that deprecates or modifies existing principles requires a migration audit of active specs, plans, and implementations.

### Versioning Policy
This constitution adheres to semantic versioning (`MAJOR.MINOR.PATCH`):
- **MAJOR**: Backward-incompatible governance changes, removal or redefinition of core principles, or relaxing of scope/diff discipline.
- **MINOR**: Addition of new principles, new quality gates, or materially expanded guidance.
- **PATCH**: Non-semantic clarifications, wording refinements, typo fixes, or formatting adjustments.

### Compliance Review Expectations
Compliance with this constitution is non-negotiable and mandatory:
- Every Spec-Kit workflow (`/speckit-specify`, `/speckit-plan`, `/speckit-tasks`, `/speckit-implement`, `/speckit-analyze`) MUST load and enforce these principles.
- Code reviewers and automated review agents MUST cite specific principles by number and name when flagging diff violations.

**Version**: 1.0.0 | **Ratified**: 2026-11-09 | **Last Amended**: 2026-11-09
