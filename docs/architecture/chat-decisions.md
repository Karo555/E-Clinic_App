# Chat Module — Architectural Decision Log


## Context

Story 1 requires a Firestore‐backed chat between patients and doctors. Before modelling the collection we needed to resolve several open scope questions.

## Decisions

| # | Topic                  | Decision                                                                                                                                     | Rationale                                                                                 |
| - | ---------------------- | -------------------------------------------------------------------------------------------------------------------------------------------- |-------------------------------------------------------------------------------------------|
| 1 | **Chat cardinality**   | A chat is **strictly between one patient and one doctor**.                                                                                   | Matches product requirements; simplifies security rules and indexing.                     |
| 2 | **Overlap policy**     | **Exactly one chat document per patient↔doctor pair** at any time. Subsequent visits reuse the same chat; the UI will show visit separators. | Avoids orphaned/duplicate threads; keeps message history unified.                         |
| 3 | **Soft‑delete**        | **Not implemented** in first iteration. All messages remain permanently unless manually removed.                                             | YAGNI; simplifies backend and UI. Soft‑delete can be layered later with a `deleted` flag. |
| 4 | **Message size limit** | **No explicit max size** enforced for now.                                                                                                   | Firebase’s 1 MiB doc limit is ample; premature optimisation avoided.                      |

## Non‑Goals

* Attachment storage (handled by Storage module).

## Impacted Artifacts

* `chat-schema.puml`– diagram reflects single chat doc, `messages` subcollection.
* Helper API signatures (`sendMessage`, `listMessages`) don’t need visit scope param.
* Security rules can assert patient Uid and doctor Uid match the doc ID.

## Next Steps

* Commit this file to `/docs/architecture/chat-decisions.md`.
* Reference in PR templates and README.
* Review periodically as new chat features are proposed.
