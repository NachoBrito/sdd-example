/speckit.constitution


Write a project constitution grounded in first-principles reasoning and practical engineering experience across five domains:

- Distributed systems
- Software design
- Data engineering
- DevOps
- Observability

The constitution must govern both engineering decisions and the behavior of AI coding agents working on this project.

## Design principles

Each principle must be decision-ready and enforceable. A reviewer must be able to identify a concrete violation in a code diff, architectural decision, specification, or operational change without subjective interpretation.

Use MUST, MUST NOT, SHOULD, and MAY consistently:

- MUST / MUST NOT: non-negotiable requirements.
- SHOULD / SHOULD NOT: defaults that may be overridden with explicit justification.
- MAY: optional practices.

For every principle, provide:

- Rationale: why the principle exists and what failure mode it prevents.
- Rule: precise, normative requirements.
- How To Apply: practical guidance, decision criteria, and examples of compliance or violations.

## Priority 1: Scope control and minimal changes

These principles take precedence over architectural preferences, optimization opportunities, and speculative improvements.

### 1. Intent Is the Contract

Implement only the behavior explicitly requested or strictly necessary to satisfy the request.

- MUST NOT introduce additional features, capabilities, workflows, configuration options, or behavior based on assumptions about what the user might want.
- MUST NOT interpret best practices, architectural principles, or future possibilities as authorization to expand scope.
- MUST preserve existing behavior unless changing it is explicitly required.
- MUST NOT implement optional enhancements merely because they are easy, beneficial, or technically interesting.
- MUST ask for clarification when an ambiguity materially affects functionality, scope, public contracts, persistence, security, architecture, or compatibility.
- MUST distinguish implementation decisions from product decisions. Agents may resolve the former using reasonable defaults but MUST NOT silently make consequential product decisions.

### 2. Minimal Change Is the Default

Prefer the smallest correct implementation that fully satisfies the requirements.

- MUST modify only the code, configuration, tests, and documentation necessary to fulfill the request.
- MUST NOT refactor, rename, reorganize, modernize, or otherwise modify unrelated code.
- MUST prefer existing mechanisms and conventions over introducing new abstractions or dependencies.
- MUST NOT introduce interfaces, layers, factories, strategies, registries, frameworks, or extension points without a concrete requirement demonstrating their necessity.
- MUST NOT add speculative error handling, defensive programming, fallback behavior, compatibility mechanisms, or configurability without a justified requirement or failure mode.
- MUST NOT duplicate functionality already available in the project.
- SHOULD prefer a localized change over a broad change when both satisfy the requirements.
- SHOULD minimize the behavioral, structural, and dependency footprint of each change.

### 3. Clarify Before Making Consequential Assumptions

Uncertainty MUST NOT become implicit scope expansion.

- MUST identify assumptions necessary to implement a request.
- MAY resolve minor implementation details using the simplest reasonable choice.
- MUST ask for clarification when competing interpretations could materially affect requirements, architecture, data, security, compatibility, or externally observable behavior.
- MUST NOT implement multiple alternatives to compensate for missing requirements.
- MUST NOT generalize a solution merely because the requirements are incomplete.
- When clarification is necessary, MUST identify the specific ambiguity, explain its consequences, and present the smallest useful set of alternatives.
- MUST NOT begin implementation of the affected decision until the ambiguity is resolved.

### 4. Explicit Authorization for Scope Expansion

The agent MUST distinguish between:

1. Required changes: directly necessary to fulfill the request.
2. Incidental changes: unavoidable consequences of the required changes.
3. Optional improvements: independently useful changes that are not necessary.

Required and incidental changes MAY be implemented.

Optional improvements MUST NOT be implemented without explicit authorization.

The agent MAY identify optional improvements separately, but MUST NOT bundle them into the requested implementation.

## Priority 2: Architecture and software design

### 5. Do Not Distribute by Default

Vertical scaling and a single deployable are the starting point.

A new process, service, or queue requires a written justification tied to one of:

- Working-set overflow.
- Genuinely independent compute.
- Geographic latency.
- Organizational independence.

Coordination is a cost measured in round trips, not milliseconds.

### 6. Optimize for Deletion, Not Extension

A module MUST be small enough that one engineer can delete and rewrite it in a day.

Reject speculative abstractions.

Inline until it hurts, then extract.

Duplication below three occurrences is generally cheaper than the wrong abstraction.

### 7. Make Dependencies Explicit

No hidden coupling, implicit global state, or import-time side effects.

A reader MUST be able to identify the dependencies of a function from its signature or the declarations at the top of its file.

Prefer dependency injection over singletons when dependencies require substitution, isolation, or lifecycle control.

Do not introduce dependency injection machinery without a concrete need.

### 8. Contract at the Boundary, Not in the Middle

Every producer/consumer boundary involving HTTP, queues, files, or databases MUST have an explicit schema appropriate to its compatibility and evolution requirements.

External and independently deployed contracts MUST be versioned when compatibility requires it.

Semantic reconciliation happens at the boundary, owned by the side that understands both contexts.

Avoid shared mutable schemas.

Do not create formal contracts for internal implementation details without a concrete need.

## Priority 3: Testing and data engineering

### 9. Test the Transformation, Not the Plumbing

Unit tests cover pure transformation logic.

Integration tests cover meaningful boundaries.

Do not mock what you own; mock what you do not.

A bug fix MUST include a regression test when the behavior can reasonably be reproduced and tested.

Tests MUST verify the requested behavior and relevant failure modes without introducing unnecessary test infrastructure.

A green CI without a failing test for a reproducible bug you just fixed is not green.

### 10. Data Integrity and Explicit Semantics

Data transformations MUST preserve explicitly defined invariants.

- MUST make units, ordering, nullability, identity, and precision explicit wherever ambiguity could cause incorrect results.
- MUST NOT silently discard, reinterpret, or fabricate data.
- MUST make destructive or irreversible data operations explicit.
- MUST preserve existing data semantics unless a change is requested.
- MUST prefer the simplest mechanism that satisfies the actual integrity and consistency requirements.
- MUST NOT introduce distributed transactions, event sourcing, elaborate migration frameworks, or additional persistence layers without demonstrated necessity.

## Priority 4: Operations and observability

### 11. Emit Structured Events, Derive Everything Else

Observability MUST provide actionable operational context.

- New application events MUST use structured logging.
- Logs, metrics, and traces SHOULD share consistent naming and contextual attributes.
- Correlation identifiers MUST be available where needed to investigate relevant operations.
- High-cardinality attributes MUST NOT be used as metric dimensions without explicit justification.
- Sensitive identifiers MUST be handled according to security and privacy requirements.
- Telemetry MUST NOT be introduced without a defined diagnostic or operational purpose.
- Do not create additional observability infrastructure when existing mechanisms are sufficient.

### 12. Recovery Proportional to Risk

Production changes MUST have recovery mechanisms appropriate to their risk.

- Risky changes MUST have a documented and practical recovery strategy.
- Destructive or irreversible operations MUST have explicit safeguards and recovery considerations.
- Database migrations SHOULD follow expand-then-contract when compatibility requires it.
- Feature flags SHOULD be used when they materially improve risk management.
- Rollback MUST be tested when the consequences of an untested rollback are significant.
- Do not introduce feature flags, rollback infrastructure, or elaborate deployment mechanisms for changes that do not justify them.

### 13. Attention Is Finite

Every actionable production alert MUST correspond to a meaningful operational or user-visible symptom and have an appropriate response procedure.

Dashboards MUST support a defined diagnostic or operational purpose.

Alerts, dashboards, and telemetry SHOULD be removed when they no longer provide useful information.

Do not introduce alerts or monitoring merely to demonstrate that a feature is instrumented.

### 14. Value Is Realized at the User

A change is complete when its requested behavior is implemented and verified.

For production-facing changes, deployment, observability, and operational readiness MUST be considered as part of delivery.

However:

- Deployment MUST NOT be performed without explicit authorization or an established deployment instruction.
- Merging MUST NOT be treated as authorization to deploy.
- Production changes MUST follow the project's deployment and approval procedures.
- A change MUST NOT be expanded merely to satisfy an aspirational definition of delivery.

## Priority 5: Developer experience

### 15. Commands Are Discoverable; Local Development Matches CI

Repeatable development and operational actions MUST be discoverable in one documented location.

The local and CI workflows SHOULD use the same underlying commands and checks.

- MUST NOT introduce hidden or undocumented execution requirements.
- MUST NOT introduce new command wrappers when existing commands are sufficient.
- MUST document any unavoidable differences between local and CI execution.
- MUST NOT require developers to reverse-engineer CI scripts to understand how to build or test the project.

## General decision rule

Engineering principles are constraints, not independent optimization targets.

An agent MUST NOT maximize adherence to one principle by violating another or introducing unnecessary complexity.

When multiple solutions satisfy the requirements, select the solution with:

1. The smallest scope.
2. The fewest changes.
3. The least architectural complexity.
4. The fewest new dependencies.
5. The least impact on existing behavior.

If a consequential trade-off cannot be resolved using these criteria, ask for clarification rather than making an assumption.

## Output requirements

Generate the constitution in the standard Spec Kit format.

For every principle, provide:

- Rationale
- Rule
- How To Apply

Preserve the standard Spec Kit constitution structure, including Governance and versioning.

Ensure principles are concise, non-contradictory, enforceable, and suitable for evaluating specifications, plans, architecture, and implementation diffs.

Do not introduce additional principles or requirements beyond those requested here unless they are strictly necessary for the constitution's governance.