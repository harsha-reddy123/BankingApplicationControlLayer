# CLAUDE.md — BankingApplicationControlLayer

## What this project is

A **benchmark for an AI-agent guardrail in banking**. A control layer sits between an obedient AI banking agent and a simulated bank and returns **ALLOW / BLOCK / ESCALATE** for every action (payments, transfers, loan modifications, account changes). A hand-labeled, multi-turn scenario suite measures whether the control layer is **safe** (stops harmful actions) and still **useful** (lets legitimate ones through), scored per category.

- **Subject under test:** the control layer only. The agent is a deliberately obedient stimulus generator — never "fix" the agent to make scores better.
- **Not** a banking app, not a UI, not real compliance. The output is a scorecard.
- **Owner:** Harsha. Also uses this project to learn **LLD / system design for interviews** (see Working conventions).

## Status

**Design complete, no code yet.** Next milestone is **M1** (see Next steps). The repo currently contains only documents.

## Docs in this repo — read before changing design

| File | Purpose | When to use |
|---|---|---|
| `DESIGN.md` | **Source of truth for the "how"**: interfaces, decision pipeline, rule catalog, scenario format, metrics, Phase 2 design, ADRs, milestones | Before writing or changing any code |
| `PROJECT_OVERVIEW.md` | The "why": problem statement, industry context, positioning vs. published benchmarks (Mermaid diagrams) | Context / README-level questions |
| `PROJECT_EXPLAINED_SIMPLE.txt` | Plain-language explanation + LLD concept map, for interviews | Keep in sync when classes/patterns change |
| `INTERVIEW_NOTES.txt` | Growing Q&A bank (Q1, Q2, …) | Append to it — see conventions |
| `control-layer-proposal.html` / `.pdf` | Original proposal, revision 2 (adds industry landscape, prior art, design principles) | Historical reference |
| `autonomous-banking-agent-control-layer.pdf` | Original proposal, first draft | Historical reference |

If code and `DESIGN.md` disagree, raise it — update the doc (and add an ADR row in §11) rather than silently diverging.

## Architecture (summary of DESIGN.md §3–4)

```
Scenario Suite → Agent Harness → CONTROL LAYER (Rules Engine → LLM Judge) → Bank Gateway
                                        ↓
                                   Audit Sink → Evaluation Harness → per-category scorecard
```

- **Rules engine** (`R-*` rules, §4.4): runs **all** rules, takes the strictest outcome (`BLOCK > ESCALATE > ALLOW`).
- **LLM judge** (§4.5): called **only** if rules say ALLOW **and** an ambiguity trigger `T-1..T-4` fires.
- **Core contract:** `Decision evaluate(ConversationState, RequestedAction, AutonomyLevel)`.
- **Phase seam:** `ControlLayer`, `BankGateway`, `Judge`, `AuditSink` interfaces (§4.2). Phase 2 adds implementations; it must not rewrite core logic.

### Phases
- **Phase 1 (the real project):** single JVM, in-memory bank, sequential runs, JSONL audit log. **No database.**
- **Phase 2 (bonus, distributed):** Spring Boot control-service + bank-service, PostgreSQL, idempotency keys, atomic daily-limit reservation, transactional outbox → Kafka, Resilience4j, Toxiproxy fault injection. Adds the `CNC` (concurrency) category. Don't start until Phase 1 (M7) is done.

## Non-negotiable invariants (DESIGN.md §4.3)

Every one needs a dedicated test; never weaken these to make a scenario pass.

- **INV-1** LLM can only make a decision stricter, never relax a rules outcome.
- **INV-2** If any rule returns BLOCK, the LLM is not called.
- **INV-3** Any judge error / timeout / invalid output → ESCALATE (fail-safe, `triggeredBy = FAILSAFE`).
- **INV-4** Autonomy level can only turn ALLOW into ESCALATE; it never raises a hard limit.
- **INV-5** Every `evaluate` call writes exactly one audit record.
- **INV-6** ALLOW always carries an `Authorization`; ESCALATE/BLOCK never do.

## Key decisions (ADRs, DESIGN.md §11)

- ADR-001 Phase 1 is in-process, in-memory.
- ADR-002 Rules first; LLM only on ambiguity triggers.
- ADR-003 Judge failure → ESCALATE.
- ADR-004 Bank re-validates hard limits atomically at execution (defense in depth for races).
- ADR-005 Field-level **provenance** (`CUSTOMER_STATED` / `AGENT_INFERRED` / `EXTERNAL_CONTENT`) on every action field.
- ADR-006 Expected outcomes are per autonomy level (L0–L3).
- ADR-007 Transactional outbox for audit (Phase 2).
- ADR-008 Maven multi-module; interfaces are the phase seam.
- ADR-009 Spring Boot for Phase 2 (proposed).

Also from the proposal: **ESCALATE is a correct answer, not a fallback**; **never report one blended accuracy number** — always per category (and per autonomy level); scenario labeling is the actual research contribution.

## Scenario categories (DESIGN.md §6)

`LEG` legitimate baseline (needed to measure usefulness) · `AMT` excessive amount · `POL` policy violation · `PAY` new/changed payee · `AMB` ambiguous intent · `CON` conflicting instructions · `DRF` portfolio drift · `API` injected API failure (separate robustness axis) · `CNC` concurrency (Phase 2).

Scenario files: `bacl-scenarios/src/main/resources/scenarios/<CATEGORY>/<ID>.yaml`, format in §4.8. Each must include `rationale` and `references` (rule/section IDs). Expected outcomes are **per autonomy level**, and state must be traced per level: an ESCALATE means the action did **not** execute, which changes later turns (see the corrected AMT-003 example in §4.8).

## Tech stack

| | Phase 1 | Phase 2 adds |
|---|---|---|
| Language / build | Java 21, Maven multi-module | — |
| Domain | Records, enums, sealed interfaces | — |
| Scenarios | YAML via Jackson | — |
| LLM judge | Anthropic Java SDK (model set by `bacl.judge.model`) | — |
| Tests | JUnit 5, AssertJ, jqwik | Testcontainers |
| Services / data | — (in-memory) | Spring Boot 3, PostgreSQL 16 + Flyway, Kafka, Resilience4j, Toxiproxy, Docker Compose |

## Environment

- Java 21 (Homebrew `openjdk@21`) and Maven 3.10 are installed. git is installed.
- `JAVA_HOME` is set in `~/.zshrc` via `/usr/libexec/java_home -v 21`. **Homebrew's Maven also pulled in Java 27** — if `mvn -version` ever shows 27, `JAVA_HOME` isn't loaded. The parent POM should still pin `maven.compiler.release=21`.
- `ANTHROPIC_API_KEY` is **not** set yet; only needed from M5 (LLM judge). Read it from the environment / a git-ignored `.env`; never hard-code or commit it. Tests must use `RecordedJudge`/`StubJudge`, never the live API.
- When implementing `ClaudeJudge` (M5), check the current Anthropic Java SDK docs and model IDs rather than relying on memory; record the model version in every run's results.
- Docker is **not** installed; needed only for Phase 2.
- The folder is **not** a git repository yet. Suggest `git init` + `.gitignore` (`target/`, `results/`, `.env`, `.idea/`) as part of M1.

## Commands (once M1 exists)

```bash
mvn -q verify                       # build + all tests
mvn -q -pl bacl-control test        # one module
mvn -q -pl bacl-eval exec:java      # run the evaluation (to be wired in M4)
```

## Code conventions

- Base package `com.bacl`; modules `bacl-domain`, `bacl-bank`, `bacl-control`, `bacl-agent`, `bacl-scenarios`, `bacl-eval` (Phase 2: `services/*`). Dependency direction: `domain ← bank ← control ← agent ← eval`; no cycles.
- **Money is always `BigDecimal`**, never `double`/`float`. Compare with `compareTo`, not `equals`.
- Domain types are immutable **records**; outcomes/statuses are **enums**.
- One rule = one class implementing `Rule`, with a stable ID (`R-AMT-02`). New rule → new class + new row in DESIGN.md §4.4 + its own test.
- Cite design IDs in code comments, test names, and scenario files, e.g. `RulesEngineTest.r_amt_02_blocksWhenDailyLimitExceeded()`.
- Thresholds (10%, 72h, 50%, 3 actions, autonomy dollar limits) are design choices — keep them in config/constants, not scattered literals, and record changes as ADRs.
- `BankGatewayContractTest` must pass against every `BankGateway` implementation.
- Eval output goes to `results/<runId>/` (git-ignored).

## Working conventions with the owner

- **Interview notes:** whenever the owner asks an interview-style or conceptual question (e.g. "how is the rulebook different from if-else?"), **append the question and answer as the next `Qn` to `INTERVIEW_NOTES.txt`** in the same plain-text format (short answer first, then detail, follow-ups, patterns to name, DESIGN.md refs). Mention briefly that it was added.
- **LLD teaching:** when adding classes, say which design pattern / SOLID principle each demonstrates (Strategy for rules, Chain of Responsibility for rules→judge, Decorator for fault injection, Adapter for `ClaudeJudge`, Repository for `BankGateway`, Observer for `AuditSink`, Command for `RequestedAction`, etc.). Keep the concept map in `PROJECT_EXPLAINED_SIMPLE.txt` §10 in sync with real class names.
- Explain in simple words; keep scope to the current milestone.

## Next steps (DESIGN.md §12)

1. **M1 — Foundation (next):** `git init` + `.gitignore`; parent POM (Java 21, dependency management); `bacl-domain` records/enums incl. `Field<T>` + `Provenance`; `BankGateway` + `InMemoryBank`; `BankGatewayContractTest`.
2. **M2 — Rules engine:** all `R-*` rules, `AutonomyPolicy`, invariant tests INV-1..6.
3. **M3 — Scenarios v0:** YAML loader + 3 scenarios each for LEG, AMT, POL, PAY.
4. **M4 — Agent + Eval v0:** `ScriptedAgent`, `EvalRunner`, console scorecard (rules-only).
5. **M5 — LLM judge:** `ClaudeJudge`, triggers, response cache, fail-safe; AMB + CON scenarios.
6. **M6–M7:** drift + API-failure basics, full suite (~10 per category), Phase 1 report.
7. **M8–M10 (Phase 2):** services + Postgres + idempotency → concurrency + outbox → fault injection + final report.

## Open questions (DESIGN.md §13)

Q1 escalation terminal vs. `humanApproves`; Q2 drift thresholds; Q3 judge sees rule results or judges blind; Q4 LLM variance runs; Q5 second labeler for agreement; Q6 how to score ALLOW-then-rejected in CNC. Don't decide these silently — ask the owner.

## Caveats

- Prior-art details (FraudBench, AgentDojo, R-Judge, ToolEmu) in `PROJECT_OVERVIEW.md` are unverified — check original papers before citing.
- Mermaid diagrams render on GitHub / VS Code (with extension) / IntelliJ, not in plain viewers.
