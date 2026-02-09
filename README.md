# Job Stories Tracker - Backend

**Problem Statement**
This service manages career accomplishment stories and emits domain events when stories transition through their lifecycle. It is intentionally designed to demonstrate service ownership, event-driven architecture, and cloud portability rather than UI complexity.The service runs locally using Docker Compose with Postgres and Kafka.

**Why I built this**
I wanted a lightweight, production-minded backend service that helps me capture senior-level accomplishment stories while I’m actively interviewing. The goal is not a feature-heavy product—it’s a small system that demonstrates service ownership, event-driven design, and cloud portability.


**Tradeoffs / Decisions**

* **Kafka for domain events:** Chosen to model decoupled workflows and demonstrate event-driven design; using a simple producer first to avoid overbuilding consumers.
* **Status-based lifecycle:**`DRAFT → READY` is explicit to avoid boolean flags and allow future states (e.g., `ARCHIVED`).
* **Auth deferred:** Authentication is intentionally postponed to keep the core domain and event flow clear; will be added once the service boundaries are solid.
