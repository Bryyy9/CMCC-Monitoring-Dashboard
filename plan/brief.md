# Product Requirements Document
## Centralized Monitoring Command Center (CMCC)

**Project:** Service Reliability Initiative — Fullstack Developer Test  
**Version:** 1.0.0  
**Status:** Draft  
**Last Updated:** June 2026  

---

## Table of Contents

1. [Overview](#1-overview)
2. [Problem Statement](#2-problem-statement)
3. [Goals & Success Metrics](#3-goals--success-metrics)
4. [Scope](#4-scope)
5. [User Personas](#5-user-personas)
6. [Functional Requirements](#6-functional-requirements)
7. [Non-Functional Requirements](#7-non-functional-requirements)
8. [Tech Stack](#8-tech-stack)
9. [System Architecture](#9-system-architecture)
10. [API Specification](#10-api-specification)
11. [UI/UX Requirements](#11-uiux-requirements)
12. [Error Handling](#12-error-handling)
13. [Delivery Checklist](#13-delivery-checklist)
14. [Out of Scope](#14-out-of-scope)

---

## 1. Overview

The **Centralized Monitoring Command Center (CMCC)** is a fullstack web prototype that serves as the "eyes and ears" of Support Engineers and System Administrators. It provides real-time visibility into the health status of all registered internal and external services, enabling teams to detect and respond to failures before end users are impacted.

This system is the first deliverable under the organization's **Service Reliability Initiative**, which targets **99.9% uptime** across all platforms.

---

## 2. Problem Statement

### Current State (Reactive Culture)
- Engineers discover service failures only **after users report them**
- Log inspection is done **manually** with no centralized view
- There is **no automated mechanism** to detect downtime proactively
- Mean time to detection (MTTD) is too high, leading to extended outages and revenue loss

### Desired State (Proactive & Resilient Culture)
- Engineers are **notified of failures automatically**, in real-time
- A single dashboard provides **high-glanceability** status for all services
- On-demand re-checks give engineers **immediate control** when needed
- The monitoring tool itself is **observable** — its own health is always visible

---

## 3. Goals & Success Metrics

### Goals

| # | Goal |
|---|------|
| G1 | Provide a centralized, real-time view of all monitored service statuses |
| G2 | Automate health checks every 60 seconds without manual intervention |
| G3 | Allow engineers to trigger an immediate health check for any service |
| G4 | Expose the monitor's own health via a self-observability endpoint |
| G5 | Handle backend failures gracefully on the frontend |

### Success Metrics

| Metric | Target |
|--------|--------|
| Dashboard load time | < 2 seconds |
| Health check latency logging accuracy | ± 50ms |
| UI status refresh interval | ≤ 10 seconds (polling) or real-time (WebSocket) |
| Uptime of the CMCC itself | 99.9% |
| Time from failure to dashboard indicator | < 65 seconds |

---

## 4. Scope

### In Scope (MVP)
- Service inventory management (CRUD)
- Automated background health checks (60s interval)
- Real-time status dashboard with visual indicators
- Manual "Force Re-check" trigger per service
- Self-observability via Actuator (or equivalent)
- Graceful UI error state when backend is unreachable

### Out of Scope
- User authentication and role-based access control
- Push notifications or alerting (email, Slack, PagerDuty)
- Historical analytics or trend charts
- Multi-tenant or multi-environment support
- Mobile native application

---

## 5. User Personas

### Persona 1 — Support Engineer (Primary)
> **"I need to know at a glance which services are down right now, without digging through logs."**

- Monitors multiple services simultaneously during a shift
- Needs **fast visual triage** — color and status, not tables of text
- Occasionally needs to trigger a manual check to confirm a suspected outage

### Persona 2 — System Administrator (Primary)
> **"I want to register new services and trust that they'll be monitored automatically."**

- Manages the inventory of services being monitored
- Needs a reliable CRUD interface to add, edit, or remove services
- Relies on the monitor's self-health endpoint to confirm the tool is running

### Persona 3 — Engineering Manager (Secondary)
> **"I want confidence that our team will know about a problem before our customers do."**

- Reviews the dashboard periodically, not in real-time
- Interested in overall system posture, not individual log entries

---

## 6. Functional Requirements

### 6.1 Backend — Inventory API

Manages the registry of services to be monitored.

**Endpoints:**

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/services` | List all registered services |
| `GET` | `/api/services/{id}` | Get a single service by ID |
| `POST` | `/api/services` | Register a new service |
| `PUT` | `/api/services/{id}` | Update an existing service |
| `DELETE` | `/api/services/{id}` | Remove a service from monitoring |

**Service Data Model:**

```json
{
  "id": "uuid",
  "name": "Payment Gateway",
  "url": "https://pay.internal.example.com/health",
  "category": "Financial",
  "status": "UP",
  "lastCheckedAt": "2026-06-23T08:00:00Z",
  "latencyMs": 142,
  "createdAt": "2026-06-01T00:00:00Z"
}
```

**Field Definitions:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `id` | UUID | Auto | System-generated unique identifier |
| `name` | String | Yes | Human-readable service name |
| `url` | String | Yes | HTTP/HTTPS endpoint or IP to ping |
| `category` | String | Yes | Grouping label (e.g. Financial, Internal, External) |
| `status` | Enum | Auto | `UP`, `DOWN`, or `UNKNOWN` |
| `lastCheckedAt` | ISO 8601 | Auto | Timestamp of the last health check |
| `latencyMs` | Integer | Auto | Response time in milliseconds |

---

### 6.2 Backend — Automated Health Check

A **scheduled background task** that continuously monitors all registered services.

**Requirements:**
- Runs every **60 seconds** using a scheduler (e.g. Spring `@Scheduled` or equivalent)
- For each service, performs an HTTP GET request to the registered URL
- Records **status** (`UP` if HTTP 2xx, `DOWN` for any error or timeout)
- Records **latency** in milliseconds
- Persists each result as a log entry in the database
- Individual service failures must **not** crash or interrupt the entire job

**Health Check Log Model:**

```json
{
  "id": "uuid",
  "serviceId": "uuid",
  "status": "UP",
  "latencyMs": 98,
  "checkedAt": "2026-06-23T08:01:00Z",
  "errorMessage": null
}
```

---

### 6.3 Backend — Self-Observability

The CMCC must be capable of **monitoring itself**.

**Requirements:**
- Implement **Spring Boot Actuator** (or equivalent in alternative stack)
- Expose at minimum:
  - `GET /actuator/health` — overall application health
  - `GET /actuator/info` — application metadata
- The health endpoint must be **publicly accessible** (no auth required for MVP)
- Health status must reflect database connectivity and scheduler liveness

---

### 6.4 Frontend — Status Dashboard

The primary view for engineers on duty.

**Requirements:**
- Displays **all registered services** in a responsive card or table layout
- Each service entry shows:
  - Service name and category
  - Current status with visual indicator (see 6.5)
  - Last checked timestamp
  - Latency in milliseconds
  - "Force Re-check" button
- Responsive layout: functional on desktop (1280px+), tablet (768px+), and mobile (375px+)
- Built with **Angular** components and styled with **Tailwind CSS**

---

### 6.5 Frontend — Visual Indicators

Status must be **immediately recognizable** without reading text.

| Status | Visual Treatment |
|--------|-----------------|
| `UP` | Solid green badge / indicator dot — `#22c55e` |
| `DOWN` | Blinking/pulsing red badge — `#ef4444` with CSS animation |
| `UNKNOWN` | Gray badge — `#6b7280`, shown on first load or pending check |

**Additional rules:**
- The blinking animation for `DOWN` must use CSS `@keyframes` with a 1s pulse cycle
- Color alone must not be the only differentiator — include a text label as well (accessibility)
- High-contrast mode must remain legible (WCAG AA minimum)

---

### 6.6 Frontend — Real-Time Updates

The dashboard must reflect changes **without requiring a browser refresh**.

**Preferred approach — RxJS polling:**
- Angular service polls `GET /api/services` every **10 seconds** using RxJS `interval` + `switchMap`
- On each successful response, the component store/state is updated reactively
- Failed poll requests increment a retry counter; after 3 failures, trigger the error state (see 6.7)

**Alternative approach — WebSockets:**
- Backend pushes status updates over a WebSocket connection
- Angular subscribes via `WebSocketSubject` from `rxjs/webSocket`
- More complex to implement but preferred for latency-sensitive scenarios

---

### 6.7 Frontend — Force Re-check

Allows an engineer to trigger an **immediate health check** for a specific service.

**Trigger:**
- `POST /api/services/{id}/check` (backend endpoint required)
- Called when the user clicks the "Force Re-check" button on a service card

**UI behavior:**
- Button shows a loading spinner while the check is in progress
- Button is **disabled** for the duration of the request to prevent double-submission
- On completion, the service row updates with the new status and latency
- On failure, display an inline error message on that card only

---

## 7. Non-Functional Requirements

| Category | Requirement |
|----------|-------------|
| Performance | API response time < 500ms under normal load |
| Reliability | Health check scheduler must recover automatically from transient errors |
| Scalability | Architecture must support adding new services without code changes |
| Observability | All health check events must be persisted for audit purposes |
| Accessibility | UI must meet WCAG 2.1 AA color contrast standards |
| Browser support | Latest 2 versions of Chrome, Firefox, Edge, Safari |
| Code quality | Clear module separation; backend and frontend in separate directories |

---

## 8. Tech Stack

### Preferred Stack

| Layer | Technology | Version | Justification |
|-------|-----------|---------|---------------|
| Frontend framework | Angular | v14+ | Aligns with current ecosystem; strong typing via TypeScript |
| Frontend styling | Tailwind CSS | v3+ | Utility-first; rapid responsive development |
| Reactive data | RxJS | (bundled with Angular) | Native polling and stream management |
| Backend framework | Java Spring Boot | v3.x | Production-grade scheduler, REST, and Actuator out of the box |
| Database (dev) | H2 (in-memory) | Latest | Zero-config local development |
| Database (prod) | PostgreSQL | v14+ | Proven relational persistence for audit logs |
| Self-observability | Spring Boot Actuator | (bundled) | Standard observability without custom implementation |

### Alternative Stack Policy

If an alternative stack is chosen, the submission must include a written section documenting:

1. **Scalability** — how the chosen stack handles increased service count and check frequency
2. **Developer-friendliness** — ease of onboarding for engineers already in the organization's ecosystem
3. **Realistic justification** — honest tradeoffs given the time constraints of this test

---

## 9. System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Browser (Engineer)                       │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │              Angular SPA (Tailwind CSS)                 │   │
│  │   Dashboard Component  │  Service List  │  Error State  │   │
│  │        RxJS Polling / WebSocket subscription            │   │
│  └───────────────────────────┬─────────────────────────────┘   │
└──────────────────────────────│──────────────────────────────────┘
                               │ HTTP / WebSocket
                               ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Spring Boot Application                       │
│                                                                 │
│  ┌──────────────┐   ┌──────────────┐   ┌────────────────────┐  │
│  │ REST API     │   │ Scheduler    │   │ Actuator           │  │
│  │ /api/serv..  │   │ @Scheduled   │   │ /actuator/health   │  │
│  │ CRUD + check │   │ every 60s    │   │ /actuator/info     │  │
│  └──────┬───────┘   └──────┬───────┘   └────────────────────┘  │
│         │                  │                                    │
│  ┌──────▼──────────────────▼───────────────────────────────┐   │
│  │              Service & Repository Layer                  │   │
│  └──────────────────────────┬──────────────────────────────┘   │
└─────────────────────────────│───────────────────────────────────┘
                              │ JPA / JDBC
                              ▼
              ┌───────────────────────────────┐
              │    Database (H2 / PostgreSQL)  │
              │  services  │  health_check_log │
              └───────────────────────────────┘
```

### Data Flow: Automated Health Check

```
[Scheduler triggers every 60s]
        │
        ▼
[Fetch all services from DB]
        │
        ▼
[For each service: HTTP GET to registered URL]
        │
        ├── 2xx response ──► status = UP, record latencyMs
        │
        └── error / timeout ──► status = DOWN, record errorMessage
                │
                ▼
        [Persist to health_check_log]
                │
                ▼
        [Update service.status + service.lastCheckedAt]
```

---

## 10. API Specification

### Additional Endpoint — Force Re-check

```
POST /api/services/{id}/check
```

**Response (200 OK):**
```json
{
  "serviceId": "3f2a1b4c-...",
  "status": "UP",
  "latencyMs": 112,
  "checkedAt": "2026-06-23T08:05:33Z"
}
```

**Response (404 Not Found):**
```json
{
  "error": "Service not found",
  "serviceId": "3f2a1b4c-..."
}
```

### Standard Error Format

All API errors must return a consistent envelope:

```json
{
  "timestamp": "2026-06-23T08:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Field 'url' must not be blank",
  "path": "/api/services"
}
```

---

## 11. UI/UX Requirements

### Dashboard Layout

```
┌─────────────────────────────────────────────────────────────┐
│  🔴 CMCC — Monitoring Command Center         [Last sync 3s] │
├─────────────────────────────────────────────────────────────┤
│  All Services (12)    🟢 9 UP    🔴 2 DOWN    ⚪ 1 UNKNOWN  │
├─────────────────────────────────────────────────────────────┤
│  ┌──────────────────┐ ┌──────────────────┐ ┌─────────────┐ │
│  │ 🟢 Payment GW    │ │ 🔴 Auth Service  │ │ ⚪ CRM API  │ │
│  │ Financial        │ │ Internal  BLINK  │ │ External    │ │
│  │ 142ms            │ │ — ms             │ │ Pending...  │ │
│  │ Checked: 8:01am  │ │ Checked: 7:59am  │ │ —           │ │
│  │ [Re-check]       │ │ [Re-check]       │ │ [Re-check]  │ │
│  └──────────────────┘ └──────────────────┘ └─────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### Component Breakdown

| Component | Responsibility |
|-----------|----------------|
| `AppComponent` | Root shell, routing |
| `DashboardComponent` | Main view, summary stats |
| `ServiceCardComponent` | Individual service tile |
| `StatusBadgeComponent` | Reusable UP/DOWN/UNKNOWN badge |
| `MonitoringService` | HTTP calls + RxJS polling logic |
| `ServiceStore` / `BehaviorSubject` | Local state management |

---

## 12. Error Handling

### Backend Unreachable (Frontend)

When the Angular polling detects 3 consecutive failed requests to the backend:

1. Stop the polling interval
2. Display a full-page or prominent banner error state:
   > ⚠️ "Unable to reach the monitoring service. Retrying in 30 seconds..."
3. Show the **last known status** of each service (stale data indicator)
4. Auto-retry after 30 seconds; resume normal polling on success

### Individual Service Check Failure (Backend)

- Log the error message to `health_check_log.errorMessage`
- Do **not** throw an exception that would abort the entire scheduled job
- Set service status to `DOWN` with `latencyMs = null`

### Invalid Service Registration (Backend)

- Return `400 Bad Request` with field-level validation messages
- Validate: `url` must be a valid HTTP/HTTPS format; `name` must not be blank

---

## 13. Delivery Checklist

### Backend
- [ ] Spring Boot project initialized (Maven or Gradle)
- [ ] `Service` entity and repository wired to H2 / PostgreSQL
- [ ] `HealthCheckLog` entity and repository
- [ ] REST controller with all CRUD endpoints
- [ ] `POST /api/services/{id}/check` endpoint
- [ ] `@Scheduled` health check job running every 60 seconds
- [ ] Spring Boot Actuator enabled at `/actuator/health`
- [ ] CORS configured to allow Angular dev server origin
- [ ] Basic error handling with consistent response envelope

### Frontend
- [ ] Angular project initialized with Tailwind CSS
- [ ] `MonitoringService` with RxJS polling (10s interval)
- [ ] `DashboardComponent` showing all services
- [ ] `ServiceCardComponent` with status badge, latency, timestamp
- [ ] Green / blinking-red / gray visual indicators
- [ ] "Force Re-check" button with loading state
- [ ] Backend-unreachable error state with last-known data
- [ ] Responsive layout (mobile, tablet, desktop)

### Documentation
- [ ] `README.md` with setup and run instructions
- [ ] Stack justification (if alternative stack used)
- [ ] Sample `.env` or `application.properties` with required config keys
- [ ] Seed data or `import.sql` for local testing

---

## 14. Out of Scope

The following are explicitly **not required** for the MVP prototype:

| Feature | Reason deferred |
|---------|----------------|
| Authentication / login | Out of test scope; assume internal trusted network |
| Alerting (email, Slack) | Future enhancement; not part of prototype objective |
| Historical charts / analytics | Requires additional data viz layer; beyond prototype |
| CI/CD pipeline | Infrastructure concern, not evaluated in this test |
| Multi-environment config | Single-env (local) sufficient for prototype |
| Rate limiting / throttling | Not applicable at prototype scale |

---

*Document owner: Fullstack Developer Candidate*  
*Reviewed by: Engineering Lead, Service Reliability Initiative*  
*Next review: Upon prototype submission*