# BankingApplicationControlLayer

> **An AI-safety evaluation benchmark, built as a backend system with distributed-systems depth, in the fintech domain.**

## Project scope

**Problem classification:** an evaluation benchmark for an **AI-agent guardrail in banking**, built as a **Java backend system** and extended into **distributed services**.

**Where it starts:** this project begins *after* permission has been granted to an AI agent. It does not ask "who is this agent?" (that's the front door, solved upstream). It asks **how to guard against misuse** of that permission, action by action.

| Area | What this project demonstrates |
|---|---|
| **Backend / distributed systems** | Race conditions, idempotency, transactional outbox, failure handling |
| **AI / ML engineering** | The guardrail, the LLM judge, evals, prompt injection |
| **Fintech** | Fraud patterns, limits, delegated authority, audit trail |
| **LLD** | Design patterns and SOLID principles in the rules engine and interfaces |

| In scope | Out of scope |
|---|---|
| Deciding ALLOW / ESCALATE / BLOCK for every agent action | Agent identity / registration ("Know Your Agent") |
| Actions sent through the banking API by an agent using its own delegated credentials | Off-channel actions (e.g. an agent phoning the call centre) |
| A labeled scenario suite and per-category scorecard | A customer-facing UI, real money, real compliance |
| Synthetic data only | Real customer PII |

---

## The problem

Banks are starting to let AI agents move money: payments, transfers, loan changes, account closures. The industry is solving the *front door* (is this agent who it claims to be?). This project targets the next layer: once a trusted agent is inside, **is this specific action safe, and did the customer actually ask for it?**

A **control layer** sits between an obedient AI agent and a simulated bank and returns one of three decisions for every action:

| Decision | Meaning |
|---|---|
| 🟢 **ALLOW** | Safe and requested — the bank may execute it |
| 🟡 **ESCALATE** | Unclear or risky — ask a human first (a correct answer, not a failure) |
| 🔴 **BLOCK** | Breaks a hard limit or policy — never execute |

A hand-labeled, multi-turn scenario suite then measures whether the control layer is **safe** (stops harmful actions) *and* **useful** (lets legitimate ones through), scored **per category and per autonomy level** — never as one blended accuracy number.

> This is not a banking app and has no UI. The output is a scorecard.

---

## How it works

```
Scenario Suite ──► Agent Harness ──► CONTROL LAYER ──► Bank Gateway
 (scripted chats    (obedient bot,    ┌────────────┐     (in-memory bank)
  + correct answer)  never refuses)   │ Rules      │
                                      │ Engine     │──► ALLOW / ESCALATE / BLOCK
                                      │    ↓       │
                                      │ LLM Judge  │  (only for ambiguous cases)
                                      └────────────┘
                                            │
                                       Audit Log ──► Evaluation Harness ──► per-category scorecard
```

1. **Rules engine** runs every rule (amount limits, account status, new payees, drift, actions beyond the customer's instructions) and keeps the strictest result.
2. **LLM judge** (Claude) is called *only* when the rules say ALLOW **and** the request is ambiguous ("send the usual", "actually, wait…").
3. **Autonomy levels L0–L3** decide how much the agent may do without asking — they can turn ALLOW into ESCALATE, never loosen a hard limit.

### Safety invariants (each has a dedicated test)

| ID | Invariant |
|---|---|
| INV-1 | The LLM can only make a decision stricter, never relax a rule |
| INV-2 | If any rule BLOCKs, the LLM is not called |
| INV-3 | Any judge error / timeout / invalid output → ESCALATE (fail-safe) |
| INV-4 | Autonomy never raises a hard limit |
| INV-5 | Every decision writes exactly one audit record |
| INV-6 | ALLOW always carries an Authorization; ESCALATE/BLOCK never do |

### Scenario categories

`LEG` legitimate baseline · `AMT` excessive amount · `POL` policy violation · `PAY` new/changed payee · `AMB` ambiguous intent · `CON` conflicting instructions · `DRF` portfolio drift · `INS` beyond instructions · `API` injected API failure · `CNC` concurrency (Phase 2)

---

## Project structure

Maven multi-module, base package `com.bacl`. Dependencies point one way: `domain ← bank ← control ← agent ← eval`.

| Module | Layer | Contents | Status |
|---|---|---|---|
| `bacl-domain` | Model | Immutable records & enums: `Account`, `Payee`, `RequestedAction`, `Field<T>` + `Provenance`, `Decision`, `Authorization` | 🚧 In progress (on `feature/domain-model-foundation`) |
| `bacl-bank` | Repository / DAO | `BankGateway` interface, `InMemoryBank`, contract test | Planned (M1) |
| `bacl-control` | Business logic | `ControlLayer`, `RulesEngine`, one class per rule, `AutonomyPolicy`, `Judge` | Planned (M2, M5) |
| `bacl-agent` | Stimulus | `ScriptedAgent` (later `LlmAgent`) | Planned (M4) |
| `bacl-scenarios` | Test data | YAML scenario loader + labeled scenarios | Planned (M3) |
| `bacl-eval` | Entry point | `EvalRunner` CLI, scorecard, JSONL audit sink | Planned (M4) |

---

## Getting started

**Requirements:** Java 21, Maven 3.9+.

```bash
mvn -q verify                    # build + run all tests
mvn -q -pl bacl-domain test      # test one module
```

The LLM judge (from M5) reads `ANTHROPIC_API_KEY` from the environment or a git-ignored `.env`. Tests never call the live API.

---

## Roadmap

| Milestone | Deliverable | Status |
|---|---|---|
| **M1** Foundation | Parent POM, domain model, `InMemoryBank`, bank contract test | 🚧 In progress |
| **M2** Rules engine | All `R-*` rules, autonomy policy, invariant tests INV-1..6 | ⏳ |
| **M3** Scenarios v0 | YAML loader + first LEG / AMT / POL / PAY / INS scenarios | ⏳ |
| **M4** Agent + Eval v0 | Scripted agent, eval runner, console scorecard | ⏳ |
| **M5** LLM judge | Claude judge, PII redaction, fail-safe, AMB / CON scenarios | ⏳ |
| **M6–M7** | Drift + API-failure scenarios, full ~90-scenario suite, Phase 1 report | ⏳ |
| **M8–M10** *(Phase 2)* | Spring Boot services, PostgreSQL, idempotency, Kafka outbox, concurrency, fault injection | ⏳ |
| **M11** *(optional)* | Live tool-calling LLM agent | ⏳ |

---

## Tech stack

| | Phase 1 | Phase 2 adds |
|---|---|---|
| Language / build | Java 21, Maven multi-module | — |
| Domain | Records, enums | — |
| Scenarios | YAML via Jackson | — |
| LLM judge | Anthropic Java SDK | — |
| Tests | JUnit 5, AssertJ, jqwik | Testcontainers |
| Services / data | In-memory | Spring Boot 3, PostgreSQL 16, Kafka, Resilience4j, Toxiproxy, Docker Compose |

---

## Documentation

| Document | What it covers |
|---|---|
| [`PROJECT_OVERVIEW.md`](PROJECT_OVERVIEW.md) | The *why*: problem statement, industry context, positioning |
| [`DESIGN.md`](DESIGN.md) | The *how*: interfaces, decision pipeline, rule catalog, scenario format, metrics, Phase 2, ADRs |
| [`PROJECT_EXPLAINED_SIMPLE.txt`](PROJECT_EXPLAINED_SIMPLE.txt) | Plain-language explanation and LLD concept map |
| [`control-layer-proposal.pdf`](control-layer-proposal.pdf) | Original proposal |

---

## Author

**Harsha** — [@harsha-reddy123](https://github.com/harsha-reddy123)
