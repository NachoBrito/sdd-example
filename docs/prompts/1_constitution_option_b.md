/speckit.constitution

Write principles grounded in first-principles reasoning across six domains:
agent execution & scope discipline, distributed systems, software design,
data engineering, devops, and observability.

Each principle must be decision-ready: a reviewer should be able to point at a
diff and say "this violates principle X" without interpretation.

Crucial meta-rule: The constitution must prevent the coding agent from
over-engineering, gold-plating, or inventing unrequested requirements. None of
the architectural or observability principles below may be used as an excuse
to build unrequested frameworks or infrastructure.

Required principles:

1. Minimum viable diff (strict scope discipline).
   Implement only what is explicitly requested—nothing more. Do not add
   unrequested features, speculative configuration, extra CLI flags, defensive
   wrappers for impossible states, or "while I'm here" refactors of adjacent
   code. Every changed line in a diff must trace directly to an explicit user
   requirement. If a cleaner design requires touching unrelated files, propose
   it in one sentence and wait for approval before editing.

2. Ambiguity halts execution (ask, never assume).
   Never guess business rules, data contracts, edge-case behavior, or missing
   requirements. When a prompt or spec has multiple valid interpretations or
   missing constraints, stop before writing implementation code and ask at most
   3 concise, high-leverage clarifying questions. Explicit user confirmation
   is cheaper than rewriting assumed code.

3. Do not distribute by default.
   Vertical scaling and a single deployable is the starting point. A new process,
   service, or queue requires a written justification tied to one of: working-set
   overflow, genuinely independent compute, geographic latency, or organizational
   independence. Coordination is a cost measured in round trips, not milliseconds.

4. Optimize for deletion, not extension.
   A module must be small enough that one engineer can delete and rewrite it in a
   day. Reject speculative abstractions, generic base classes, and premature
   interfaces. Inline until it hurts, then extract. Duplication below three
   occurrences is cheaper than the wrong abstraction.

5. Make dependencies explicit.
   No hidden coupling, no implicit global state, no import-time side effects.
   A reader must be able to see every dependency of a function in its signature
   or at the top of its file. Dependency injection over singletons.

6. Contract at the boundary, not in the middle.
   Every producer/consumer boundary (HTTP, queue, file, database) must have a
   schema with a version. Semantic reconciliation happens at the boundary, owned
   by the side that understands both contexts. No shared mutable schemas.

7. Test the transformation, not the plumbing.
   Unit tests cover pure transformation logic. Integration tests cover the
   boundaries. Do not mock what you own; do mock what you do not. A green CI
   without a failing test for the bug you just fixed is not green. Do not write
   tautological tests for getters, setters, or framework internals.

8. Emit structured events, derive everything else.
   Logs, metrics, and traces are projections of one primitive: the structured
   event. High-cardinality fields (user id, request id, tenant id, feature flag
   state) are required when available in context. No unstructured log lines in
   new code. Use existing project logging primitives; do not introduce new
   observability libraries unless explicitly requested.

9. Recovery over prevention.
   Every change must be revertible in under five minutes without a code change.
   Feature flags gate risky paths when a flagging mechanism exists. Migrations
   are expand-then-contract. Rollback is tested as part of the deploy, not
   assumed.

10. Attention is finite.
    Every alert must correspond to a user-visible symptom and a runbook.
    Dashboards are saved queries, not decoration. Delete signals that have not
    fired a useful page in 90 days.

11. Value is realized at the user, not at merge.
    A PR is not done until the change is in the hands of users, observable, and
    revertible. "Shipped" means deployed, instrumented, and monitored, not merged.

12. Commands are discoverable; local dev matches CI.
    Every repeatable action (build, test, lint, migrate, deploy, seed) is a
    single named command listed in one place and runnable with no hidden
    arguments. The command a developer runs locally is the same command CI
    runs. No "works on my machine" gap, no CI-only shell steps, no undocumented
    makefile targets. If a new contributor cannot list every command in 30
    seconds, the interface is broken.

Governance & Priority Order:
Include a Governance section stating that Principles 1 (Minimum Viable Diff)
and 2 (Ambiguity Halts Execution) take precedence over all other principles.
If satisfying Principle 6, 8, or 9 would require building new infrastructure
not requested by the user, the agent must pause and ask whether to build that
infrastructure or use a minimal stub.

Output the constitution in the standard Spec-Kit format with Rationale,
Rule, and How To Apply sections for each principle.