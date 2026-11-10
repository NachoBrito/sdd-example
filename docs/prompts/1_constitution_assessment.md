# Constitution Prompt Assessment: Options A, B, and C

This document evaluates the three candidate prompts for generating the project constitution under Spec-Kit (`sdd-example`):

- **Option A**: [`docs/prompts/1_constitution_option_a.md`](./1_constitution_option_a.md)
- **Option B**: [`docs/prompts/1_constitution_option_b.md`](./1_constitution_option_b.md)
- **Option C**: [`docs/prompts/1_constitution_option_c.md`](./1_constitution_option_c.md)

---

## 1. Executive Summary & Final Recommendation

### **Verdict: Option B is the Most Appropriate Choice**

Option B provides the optimal balance of **strict agent alignment (anti-gold-plating)**, **decision-readiness**, and **token/context efficiency** for Spec-Kit workflows.

- **Option A is too permissive:** It conflates scope discipline with ambiguity resolution into a single principle and leaves architectural/observability rules unqualified. This creates loopholes where AI coding agents build unrequested frameworks under the justification of "satisfying the constitution."
- **Option C is overly verbose:** At 249 lines across 15 separate principles, it dilutes agent attention and imposes a continuous token tax on every downstream Spec-Kit command (`/speckit-specify`, `/speckit-plan`, `/speckit-tasks`, `/speckit-implement`), while redundantly dividing scope discipline across four separate principles.
- **Option B hits the operational sweet spot:** It introduces an explicit meta-rule forbidding infrastructure creation as an excuse to satisfy principles, cleanly separates *Minimum Viable Diff* from *Ambiguity Halts Execution*, adds self-restraint clauses directly to operational rules, and defines an unambiguous precedence hierarchy in governance.

---

## 2. Comparative Evaluation Matrix

| Evaluation Criteria | Option A (`1_constitution_option_a.md`) | Option B (`1_constitution_option_b.md`) — **Recommended** | Option C (`1_constitution_option_c.md`) |
| :--- | :--- | :--- | :--- |
| **Agent Guardrails & Scope Control** | ⚠️ **Moderate**<br>Scope and ambiguity are combined into Principle 2. | 🟢 **Excellent**<br>Dedicated principles for scope and ambiguity; explicit meta-rule. | 🟢 **Strong**<br>Very detailed, but spread across 4 overlapping principles. |
| **Over-Engineering Defense** | 🔴 **Weak**<br>No explicit guards against generating unrequested frameworks. | 🟢 **Strong**<br>Meta-rule + built-in constraints in logging, recovery, and boundaries. | 🟢 **Strong**<br>Extensive RFC 2119 `MUST NOT` negative constraints. |
| **Governance & Conflict Precedence** | ⚠️ **Basic**<br>Mentions review gates, but lacks explicit priority ordering among rules. | 🟢 **Clear**<br>Principles 1 & 2 explicitly override Principles 6, 8, and 9. | 🟢 **Comprehensive**<br>Multi-tier priority hierarchy and resolution framework. |
| **Context Window & Token Tax** | 🟢 **Low**<br>11 principles; concise prompt. | 🟢 **Low–Moderate**<br>12 focused, high-signal principles. | 🔴 **High**<br>15 principles with extensive sub-rules; inflates every task prompt. |
| **Diff Enforceability ("Decision-Ready")** | ⚠️ **Variable**<br>Compound principles make objective diff checks subjective. | 🟢 **High**<br>Quantifiable checks: line traceability, max 3 questions. | 🟢 **High**<br>Strict normative language (`MUST`, `MUST NOT`, `SHOULD`). |

---

## 3. In-Depth Analysis of Each Option

### Option A: `1_constitution_option_a.md`

#### Strengths
- Clean grounding across five foundational engineering domains (distributed systems, software design, data engineering, devops, observability).
- Familiar, time-tested software engineering idioms (e.g., vertical scaling, explicit dependencies, structured events).
- Concise instruction set.

#### Critical Failure Modes for AI Agents
1. **Conflated Scope & Clarification (Principle 2):** Bundling "minimum sufficient change" and "clarify over assume" weakens both. In practice, coding agents treat compound principles loosely, often failing to stop when requirements are ambiguous.
2. **The "Gold-Plating" Loophole:** Principles such as `#7` (*"Emit structured events, derive everything else"*) and `#8` (*"Recovery over prevention... Feature flags gate risky paths"*) are phrased unconditionally. Without guardrails, an AI agent working on a simple script or minor endpoint will often unilaterally introduce logging libraries, feature flag machinery, or event schemas because the constitution "mandated" it.
3. **No Conflict Precedence:** Does not specify what to do when an architectural principle (e.g., contract versioning or structured logging) requires introducing new dependencies that conflict with minimum change.

---

### Option B: `1_constitution_option_b.md` *(Recommended)*

#### Strengths
1. **Front-Loaded Agent Defense:** Includes an upfront **Crucial Meta-Rule**:
   > *"The constitution must prevent the coding agent from over-engineering, gold-plating, or inventing unrequested requirements. None of the architectural or observability principles below may be used as an excuse to build unrequested frameworks or infrastructure."*
2. **Distinct, High-Leverage Safety Gates:**
   - **Principle 1 (Minimum viable diff):** Strict scope discipline. Every line in a diff must trace to an explicit requirement. Touching unrelated files requires explicit prior approval.
   - **Principle 2 (Ambiguity halts execution):** Halts implementation immediately upon encountering underspecified requirements and caps clarifying inquiries at **at most 3 concise, high-leverage questions**.
3. **Defensive Modifiers on Architectural Rules:**
   - **Principle 7 (Testing):** Explicitly forbids tautological tests for getters/setters/framework internals.
   - **Principle 8 (Observability):** Requires using *existing project logging primitives* and forbids introducing new observability libraries unless explicitly requested.
   - **Principle 9 (Recovery):** Limits feature flag usage to *when a flagging mechanism already exists*.
4. **Governance Hierarchy:** Explicitly states that Principles 1 and 2 take precedence over all others. If satisfying boundary contracts (Principle 6), event schemas (Principle 8), or recovery (Principle 9) would require building new infrastructure, the agent **must pause and ask** whether to build it or use a minimal stub.

---

### Option C: `1_constitution_option_c.md`

#### Strengths
- Exceptional linguistic rigor utilizing formal RFC 2119 keywords (`MUST`, `MUST NOT`, `SHOULD`, `MAY`).
- Introduces valuable nuances such as **Principle 10 ("Data Integrity and Explicit Semantics")** and **Principle 12 ("Recovery Proportional to Risk")**.
- Principle 14 explicitly prevents agents from attempting unauthorized deployments or claiming a task is blocked without production release privileges.

#### Drawbacks
1. **Context Bloat:** Spec-Kit injects the rendered constitution into the agent's context across all lifecycle workflows (`/speckit-specify`, `/speckit-plan`, `/speckit-tasks`, `/speckit-implement`). A 15-principle constitution with Rationale, Rule, and How To Apply sections produces a 400–600 line document, consuming significant tokens and diluting agent focus.
2. **Redundancy across Priority 1:** Principles 1, 2, 3, and 4 (*Intent Is the Contract*, *Minimal Change Is the Default*, *Clarify Before Assuming*, *Explicit Authorization*) divide the same core scope-discipline premise across four overlapping sections.

---

## 4. Synthesis & Recommended Next Steps

### Action
Use **Option B** as the primary prompt to generate `.specify/memory/constitution.md`:

```bash
/speckit.constitution
```
*(Passing the contents of `docs/prompts/1_constitution_option_b.md`)*

### Optional High-Value Cherry-Picks from Option C
If desired, Option B can be subtly enriched with two specific clauses from Option C without adding bloat:

1. **Data Integrity Safeguard (from Option C, Principle 10):** Add a note to software design/contracts:
   > *"Data transformations must preserve explicit invariants; never silently discard, reinterpret, or fabricate data."*
2. **Deployment Guardrail (from Option C, Principle 14):** Add to Principle 11:
   > *"Merging or local testing does not authorize autonomous deployment; production deployments require explicit human instruction."*

