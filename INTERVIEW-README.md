# CMCC — Centralized Monitoring Command Center

> Proyek fullstack monitoring dashboard untuk mendeteksi **kegagalan service secara real-time**. Dibangun sebagai bagian dari *Service Reliability Initiative* — target **99.9% uptime**.

---

## 📋 Ringkasan Proyek

| Aspek | Detail |
|-------|--------|
| **Masalah** | Engineer hanya tahu service down setelah user komplain. Tidak ada dashboard terpusat. MTTD (Mean Time to Detection) terlalu tinggi. |
| **Solusi** | Dashboard real-time yang otomatis nge-ping semua service tiap 60 detik, kasih visual status (UP/DOWN/UNKNOWN), dan bisa force re-check manual. |
| **Peran Saya** | Fullstack developer — dari requirement, arsitektur, implementasi backend (Spring Boot) + frontend (Angular), sampai testing. |
| **Durasi** | Prototype dalam waktu terbatas (technical test). |

---

## 🛠 Tech Stack & Alasan

### Backend: Spring Boot 3.3 + Java 20

**Kenapa Spring Boot?**
- **`@Scheduled`** — built-in, tinggal pakai untuk health check scheduler 60 detik. Gak perlu setup cron job external.
- **Spring Boot Actuator** — self-observability gratis (`/actuator/health`, `/actuator/info`). Persyaratan langsung terpenuhi tanpa ngoding manual.
- **JPA + Hibernate** — mapping entity ke database tinggal pake anotasi. Dev pake H2 (zero config), prod tinggal ganti `application-prod.yml` ke PostgreSQL.
- **`@RestControllerAdvice`** — global error handler, konsisten format respons error di semua endpoint.
- **Udah mature & enterprise-ready** — organisasi target pake Java ecosystem, jadi alignment-nya alami.

### Frontend: Angular 14 + Tailwind CSS 3

**Kenapa Angular?**
- **RxJS** — `BehaviorSubject` + `timer/switchMap` buat polling tiap 10 detik. Reactive state management tanpa perlu NgRx (overkill buat single-view SPA).
- **TypeScript** — type safety, apalagi karena data service udah punya model tetap.
- **HttpClient** — built-in, gak perlu tambahan library buat HTTP.

**Kenapa Tailwind?**
- **Utility-first** — styling cepat tanpa nulis CSS custom. Status badge tinggal pake `bg-green-500` / `bg-red-500`.
- **Responsive** — `sm:`, `md:`, `lg:` prefix langsung handle layout mobile ke desktop.
- **Design system custom** — `Inter` buat UI label, `JetBrains Mono` buat angka (latency, timestamp) biar alignment rapi.

### Database: H2 (dev) / PostgreSQL (prod)

- Dev pake H2 in-memory — **zero setup**. Clone, run, langsung jalan.
- Prod pake PostgreSQL — transactional, audit log, cocok buat production.
- Ganti profile tinggal set env `SPRING_PROFILES_ACTIVE=prod`.

### Kenapa polling, bukan WebSocket?

| Approach | Kelebihan | Kekurangan |
|----------|-----------|------------|
| **Polling (RxJS timer + switchMap)** | Sederhana, gak perlu koneksi persistent, gampang di-debug, error handling jelas | Ada delay sampai 10 detik, traffic HTTP tiap poll |
| **WebSocket** | Real-time, lebih efisien | Complexity tinggi buat MVP, perlu STOMP/Spring WebSocket config |

**Keputusan:** Polling dipilih karena:
1. **MVP scope** — delay 10 detik acceptable untuk use case monitoring.
2. **Sederhana** — error handling (3 consecutive failures → stop polling → retry 30 detik) gampang diimplement.
3. **switchMap** — kalau poll baru mulai sebelum poll sebelumnya selesai, request lama otomatis dicancel. Gak ada stale response.

---

## 🏗 Arsitektur Sistem

```
┌─────────────────────────────────┐       ┌──────────────────────────────────────────┐
│  Frontend (Angular 14)          │       │  Backend (Spring Boot 3.3)               │
│                                 │       │                                          │
│  ┌─────────────────────────┐    │ HTTP  │  ┌──────────────────────────────────┐   │
│  │  MonitoringService      │◄───┼───────┼──│  ServiceController               │   │
│  │  BehaviorSubject state  │    │       │  │  CRUD + check endpoints          │   │
│  │  RxJS polling (10s)     │───►┼───────┼──│  GlobalExceptionHandler          │   │
│  └─────────────────────────┘    │       │  └──────────────────┬───────────────┘   │
│                                 │       │                      │                   │
│  ┌─────────────────────────┐    │       │  ┌───────────────────▼───────────────┐  │
│  │  Component Tree         │    │       │  │  ServiceService + HealthCheckSvc │  │
│  │  ┌─ SummaryBar          │    │       │  └───────────────────┬───────────────┘  │
│  │  ├─ ServiceCard (×n)    │    │       │                      │                   │
│  │  │   └─ StatusBadge     │    │       │  ┌───────────────────▼───────────────┐  │
│  │  └─ ErrorBanner         │    │       │  │  HealthCheckScheduler             │  │
│  └─────────────────────────┘    │       │  │  @Scheduled(fixedRate=60000)     │──┼──► External
│                                 │       │  └───────────────────┬───────────────┘  │
└─────────────────────────────────┘       │  ┌───────────────────▼───────────────┐  │
                                          │  │  Database (JPA)                   │  │
                                          │  │  services                         │  │
                                          │  │  health_check_logs                │  │
                                          │  └───────────────────────────────────┘  │
                                          └──────────────────────────────────────────┘
```

### Alur Data: Automated Health Check

```
[Scheduler trigger tiap 60 detik]
        │
        ▼
[Fetch semua service dari DB]
        │
        ▼
[For each service: HTTP GET ke URL]
        │
        ├── 2xx ───► status = UP, catat latencyMs
        │
        └── error/timeout ──► status = DOWN, catat errorMessage
                │
                ▼
        [Simpan ke health_check_log]
                │
                ▼
        [Update service.status + lastCheckedAt]
```

### Alur Data: Frontend Polling

```
[MonitoringService.startPolling()]
        │
 timer(0, 10_000)
        │
        ▼
 switchMap → GET /api/services
        │
        ├── sukses ──► update BehaviorSubject, reset consecutiveFailures = 0
        │
        └── gagal ──► consecutiveFailures++
                      Kalau >= 3 → stop polling, tampilkan error banner
                                   setTimeout(retry, 30_000)
```

---

## 🔑 Key Design Decisions

### 1. BehaviorSubject > NgRx

Kenapa gak pake NgRx? Karena ini **single-view SPA** — cuma satu halaman dashboard. NgRx butuh boilerplate (actions, reducers, effects, selectors) yang gak sebanding manfaatnya.

`BehaviorSubject<ServiceState>`:
- `.getValue()` — akses state terbaru secara synchronous.
- `.asObservable()` — subscribe reaktif dari component.
- `.next({ ...state, ...partial })` — update state immutably.

```typescript
private state$ = new BehaviorSubject<ServiceState>({...});
readonly vm$ = this.state$.asObservable();
```

### 2. switchMap Polling — Cancel Request Sebelumnya

```typescript
timer(0, environment.pollIntervalMs).pipe(
  switchMap(() => this.http.get<Service[]>(url))
)
```

`switchMap()` otomatis **unsubscribe dari request sebelumnya** kalau ada yang baru mulai. Mencegah:
- Stale response (data lama timpa data baru)
- Race condition kalau backend lambat

### 3. Consecutive Failure Guard

```typescript
private handlePollingError(error: any) {
  const failures = this.state.consecutiveFailures + 1;
  if (failures >= environment.maxConsecutiveFailures) {
    this.stopPolling();
    setTimeout(() => this.retry(), environment.retryDelayMs);
  }
}
```

**Kenapa 3 kali?** Cukup untuk filter transient error (network glitch), tapi gak terlalu lama nunggu kalau backend beneran down.

### 4. AtomicBoolean Idempotency Guard (Backend)

```java
private final AtomicBoolean running = new AtomicBoolean(false);

@Scheduled(fixedRate = 60_000)
public void runHealthChecks() {
    if (!running.compareAndSet(false, true)) {
        log.warn("Previous health check cycle still in progress, skipping");
        return;
    }
    running.set(false);
}
```

**Masalah:** Kalau scheduler trigger lagi sebelum cycle selesai (misal karena ada service yang timeout lama), dua cycle bakal jalan bersamaan → race condition + overload.

**Solusi:** `AtomicBoolean.compareAndSet()` — jamin cuma satu cycle jalan. Kalau masih jalan, yang baru di-skip.

### 5. Separation of Concerns: HealthCheckService

Logic health check dipisah dari ServiceService:
- **ServiceService** — CRUD services.
- **HealthCheckService** — HTTP ping, ukur latency, simpan log, update status.
- **HealthCheckScheduler** — orchestrator: panggil service, looping, error handling.

Hasilnya: `HealthCheckService.checkService()` bisa dipanggil dari **scheduler** (otomatis 60s) dan **controller** (force re-check manual) tanpa duplikasi kode.

### 6. GlobalExceptionHandler — Consistent Error Response

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ServiceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(...) {
        return ResponseEntity.status(404).body(new ErrorResponse(404, "Not Found", ...));
    }
}
```

Semua error baliknya format konsisten: `{ status, error, message, path }`. Frontend tinggal parsing satu format.

### 7. Dual Database Strategy (H2 ↔ PostgreSQL)

- **H2** di profile `local` (default) — in-memory, jalan tanpa setup.
- **PostgreSQL** di profile `prod` — butuh env vars `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.

Ganti tinggal: `SPRING_PROFILES_ACTIVE=prod`

### 8. CORS Config

```java
registry.addMapping("/api/**")
    .allowedOrigins("http://localhost:4200", "http://localhost:3000");
```

Frontend Angular jalan di port 4200, backend di 8080. Tanpa CORS, browser bakal nge-block request.

---

## ✨ Fitur Utama

| Fitur | Backend | Frontend |
|-------|---------|----------|
| **CRUD Services** | `GET/POST/PUT/DELETE /api/services` | Form + service list |
| **Auto Health Check** | `@Scheduled(fixedRate=60000)` | Polling setiap 10 detik |
| **Force Re-check** | `POST /api/services/{id}/check` | Tombol "Periksa Ulang" tiap card |
| **Self-Observability** | `/actuator/health`, `/actuator/info` | — |
| **Visual Status** | — | Green (UP), Red pulsing (DOWN), Gray (UNKNOWN) |
| **Error Handling** | `GlobalExceptionHandler` → format konsisten | Error banner + retry otomatis 30s |
| **Skeleton Loading** | — | Shimmer animation pas initial load |

---

## 🎯 Skenario Interview

Berikut pertanyaan yang **paling mungkin ditanyakan** dan cara jawabnya:

### Q: "Kenapa pilih polling, bukan WebSocket?"

**Jawaban:**
> Untuk MVP, polling dengan RxJS switchMap sudah cukup. Delay 10 detik masih acceptable untuk monitoring — kita gak perlu real-time millisecond. WebSocket butuh koneksi persistent, STOMP protocol, handle reconnect logic, dsb. Itu complexity tambahan yang gak sebanding untuk prototype. Tapi kalau scale-nya besar (ribuan service) atau butuh notifikasi instant, WebSocket jelas lebih baik.

### Q: "Gimana handle kalau backend down?"

**Jawaban:**
> Ada dua lapis: backend pake `AtomicBoolean` idempotency guard biar scheduler gak tumpuk-tumpuk. Frontend punya failure counter — setelah 3 kali gagal berturut-turut, polling dihentikan, error banner muncul, dan state terakhir tetap ditampilkan (stale data). Setelah 30 detik, nyoba reconnect otomatis. Ini mencegah hammering backend yang sudah down.

### Q: "Apa tradeoff pake H2 di dev?"

**Jawaban:**
> Keuntungan: zero setup, gak perlu install database, cocok buat testing cepat. Kerugian: data hilang setiap restart, jadi gak bisa test persistence. Untuk prod tinggal ganti profile ke PostgreSQL — kode gak perlu diubah karena pake JPA abstraction.

### Q: "Kenapa pilih BehaviorSubject daripada NgRx?"

**Jawaban:**
> BehaviorSubject itu lightweight state management. NgRx ideal untuk aplikasi dengan banyak fitur, banyak state, dan butuh dev tools. Tapi dashboard ini cuma satu halaman dengan satu data stream (daftar service). NgRx akan nambah boilerplate besar tanpa manfaat signifikan. Prinsipnya: pake tools sesuai skala problem.

### Q: "Gimana scaling aplikasi ini?"

**Jawaban:**
> Untuk ribuan service, scheduler synchronous loop bisa jadi bottleneck. Solusi: pake thread pool (each service check di thread terpisah) atau message queue (RabbitMQ / Kafka) — scheduler publish task, consumer process. Frontend polling pake `switchMap` udah handle cancel request, jadi gak ada accumulation.

### Q: "Apa yang bakal kamu tambahin next?"

**Jawaban:**
> 1. **Alerting** — integrasi Slack/email pas service DOWN.
> 2. **Historical trends** — grafik uptime per service.
> 3. **WebSocket** — kalau polling latency jadi masalah.
> 4. **Auth** — minimal basic auth atau API key.
> 5. **Test coverage** — tambah integration test + E2E.

---

## 📊 Test Coverage

```
Tests run: 21, Failures: 0, Errors: 0, Skipped: 0

✓ ServiceControllerTest   (9 tests) — HTTP layer, validation, 404 handling
✓ HealthCheckServiceTest  (4 tests) — UP/DOWN/Timeout detection, state update
✓ ServiceServiceTest      (7 tests) — CRUD logic, exception propagation
✓ CmccApplicationTests    (1 test)  — Spring context load
```

---

## 📁 Struktur Folder

```
nuxatech/
├── pom.xml                          # Maven backend config
├── src/main/java/com/nuxatech/cmcc/
│   ├── CmccApplication.java         # Entry point
│   ├── controller/
│   │   └── ServiceController.java   # REST endpoints
│   ├── service/
│   │   ├── ServiceService.java      # CRUD logic
│   │   └── HealthCheckService.java  # HTTP ping + logging
│   ├── scheduler/
│   │   └── HealthCheckScheduler.java # Scheduled task (60s)
│   ├── entity/
│   │   ├── ServiceEntity.java       # JPA entity service
│   │   ├── HealthCheckLogEntity.java # JPA entity log
│   │   └── ServiceStatus.java       # Enum UP/DOWN/UNKNOWN
│   ├── repository/                  # JPA repositories
│   ├── dto/                         # Request/Response DTOs
│   ├── exception/                   # GlobalExceptionHandler
│   └── config/                      # CORS, RestTemplate
├── frontend/
│   ├── package.json
│   ├── src/app/
│   │   ├── services/
│   │   │   └── monitoring.service.ts # RxJS polling + state
│   │   ├── components/
│   │   │   ├── summary-bar/        # Statistik ringkasan
│   │   │   ├── service-card/       # Card per service
│   │   │   └── status-badge/       # Badge UP/DOWN/UNKNOWN
│   │   ├── models/
│   │   │   └── service.model.ts    # TypeScript interfaces
│   │   └── app.component.ts        # Root component
│   └── tailwind.config.js
├── plan/                           # Dokumentasi requirement & desain
└── assets/                         # Screenshot & diagram
```

---

## 💡 Poin Penting yang Bisa Kamu Tekankan di Interview

1. **End-to-end ownership** — dari requirement gathering, arsitektur, implementasi backend + frontend, testing, dokumentasi.
2. **Pragmatic decision making** — pilih polling instead of WebSocket, BehaviorSubject instead of NgRx, H2 instead of PostgreSQL — semuanya ada rationale, bukan asal pilih.
3. **Production mindset** — idempotency guard (`AtomicBoolean`), consecutive failure guard, retry logic, error handling, CORS, actuator.
4. **Clean architecture** — separation of concerns (ServiceService vs HealthCheckService), DTO pattern, global exception handler.
5. **Self-observability** — actuator endpoints untuk monitoring monitornya sendiri.

---

*Built for the Nuxatech Service Reliability Initiative — Fullstack Developer Assessment.*
