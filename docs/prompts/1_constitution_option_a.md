/speckit.constitution

Write principles grounded in first-principles reasoning across five domains:
distributed systems, software design, data engineering, devops, and observability.

Each principle must be decision-ready: a reviewer must be able to point at a
diff and say "this violates principle X" without interpretation.

Required principles:

1. Do not distribute by default.
   Vertical scaling and a single deployable is the starting point. A new process,
   service, or queue requires a written justification tied to one of: working-set
   overflow, genuinely independent compute, geographic latency, or organizational
   independence. Coordination is a cost measured in round trips, not milliseconds.

2. Minimum sufficient change; clarify over assume.
   Every change must represent the absolute minimum delta required to satisfy the
   explicit specification. Unrequested features, speculative fields/endpoints,
   convenience helpers, and proactive refactoring of untouched code are strictly
   prohibited. If a requirement or edge case is ambiguous, halt and ask for
   clarification before writing code. Guessing user intent is a defect.

3. Optimize for deletion, not extension.
   A module must be small enough that one engineer can delete and rewrite it in a
   day. Reject speculative abstractions. Inline until it hurts, then extract.
   Duplication below three occurrences is cheaper than the wrong abstraction.
   Do not write code for anticipated future requirements.

4. Make dependencies explicit.
   No hidden coupling, no implicit global state, no import-time side effects.
   A reader must be able to see every dependency of a function in its signature
   or at the top of its file. Dependency injection over singletons.

5. Contract at the boundary, not in the middle.
   Every producer/consumer boundary (HTTP, queue, file, database) must have a
   schema with a version. Semantic reconciliation happens at the boundary, owned
   by the side that understands both contexts. No shared mutable schemas.

6. Test the transformation, not the plumbing.
   Unit tests cover pure transformation logic. Integration tests cover the
   boundaries. Do not mock what you own; do mock what you do not. A green CI
   without a failing test for the bug you just fixed is not green.

7. Emit structured events, derive everything else.
   Logs, metrics, and traces are projections of one primitive: the structured
   event. High-cardinality fields (user id, request id, tenant id, feature flag
   state) are required, not optional. No unstructured log lines in new code.

8. Recovery over prevention.
   Every change must be revertible in under five minutes without a code change.
   Feature flags gate risky paths. Migrations are expand-then-contract. Rollback
   is tested as part of the deploy, not assumed.

9. Attention is finite.
   Every alert must correspond to a user-visible symptom and a runbook. Dashboards
   are saved queries, not decoration. Delete signals that have not fired a
   useful page in 90 days.

10. Value is realized at the user, not at merge.
    A PR is not done until the change is in the hands of users, observable, and
    revertible. "Shipped" means deployed, instrumented, and monitored, not merged.

11. Commands are discoverable; local dev matches CI.
    Every repeatable action (build, test, lint, migrate, deploy, seed) is a
    single named command listed in one place and runnable with no hidden
    arguments. The command a developer runs locally is the same command CI
    runs. No "works on my machine" gap, no CI-only shell steps, no undocumented
    makefile targets. If a new contributor cannot list every command in 30
    seconds, the interface is broken.

Generation Instructions & Constraints:
- Output the constitution in the standard Spec-Kit format with Rationale,
  Rule, and How To Apply sections for each principle.
- Only generate the 11 principles listed above; do not invent extra principles.
- In the "Development Workflow & Review Gates" section, include an explicit
  "Scope & Clarification Gate" that mandates rejecting any diff introducing
  unrequested changes, untouched file edits, or unilateral guesses.
- In the "Governance" section, mandate that AI agents must stop and prompt
  the user when encountering underspecified requirements.