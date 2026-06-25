# CMCC — Centralized Monitoring Command Center

**Dokumentasi Lengkap Proyek**

| Metadata | |
|----------|---|
| Nama Proyek | CMCC — Centralized Monitoring Command Center |
| Inisiatif | Service Reliability Initiative |
| Target | Fullstack Developer Technical Test |
| Versi | 1.0.0 |
| Status | Production-ready prototype |

---

## Daftar Isi

1. [Ringkasan Proyek](#1-ringkasan-proyek)
2. [Business Context & Problem Statement](#2-business-context--problem-statement)
3. [Requirements](#3-requirements)
4. [Tech Stack](#4-tech-stack)
5. [Arsitektur Sistem](#5-arsitektur-sistem)
6. [Backend](#6-backend)
   - [6.1 Entity & Database](#61-entity--database)
   - [6.2 DTOs](#62-dtos)
   - [6.3 Repository Layer](#63-repository-layer)
   - [6.4 Service Layer](#64-service-layer)
   - [6.5 Controller Layer](#65-controller-layer)
   - [6.6 Scheduler](#66-scheduler)
   - [6.7 Config](#67-config)
   - [6.8 Exception Handling](#68-exception-handling)
   - [6.9 API Specification](#69-api-specification)
7. [Frontend](#7-frontend)
   - [7.1 Component Tree](#71-component-tree)
   - [7.2 Data Model](#72-data-model)
   - [7.3 Service Layer (RxJS)](#73-service-layer-rxjs)
   - [7.4 State Management](#74-state-management)
   - [7.5 Dashboard Component](#75-dashboard-component)
   - [7.6 Service Card Component](#76-service-card-component)
   - [7.7 UI Design System](#77-ui-design-system)
   - [7.8 Environment Config](#78-environment-config)
8. [Data Flow](#8-data-flow)
9. [Configuration](#9-configuration)
10. [Testing](#10-testing)
11. [Setup & Running](#11-setup--running)
12. [Project Structure](#12-project-structure)

---

## 1. Ringkasan Proyek

CMCC adalah fullstack web prototype untuk **monitoring kesehatan service secara real-time**. Sistem ini secara otomatis mengecek ketersediaan service internal/eksternal setiap 60 detik, menampilkan statusnya di dashboard Angular, dan memungkinkan engineer untuk melakukan force re-check manual.

### Tujuan Utama

- Menyediakan **dashboard terpusat** untuk melihat status semua service
- **Mendeteksi downtime secara otomatis** sebelum user melapor
- Memberikan **kontrol manual** (force re-check) untuk engineer
- **Self-observability** — sistem monitor bisa memonitor dirinya sendiri

---

## 2. Business Context & Problem Statement

### Current State (Reaktif)

```
User melapor error → Engineer cek log manual → Diagnosa → Fix
                   ↑ MTTD terlalu tinggi
```

- Service down baru ketahuan setelah user komplain
- Tidak ada dashboard terpusat — engineer harus cek log satu-satu
- Mean Time to Detection (MTTD) tinggi → outage berkepanjangan

### Target State (Proaktif)

```
Service down → CMCC deteksi dalam <65 detik → Dashboard merah → Engineer langsung tahu
```

- Engineer menerima notifikasi otomatis (via dashboard)
- Dashboard dengan high-glanceability — lihat 1 detik langsung tahu kondisi semua service
- Force re-check untuk konfirmasi cepat

### Target Uptime: **99.9%**

---

## 3. Requirements

### 3.1 Functional Requirements

#### Backend

| ID | Requirement | Implementasi |
|----|-------------|--------------|
| FR-01 | Inventory API (CRUD services) | `ServiceController` — 5 endpoint REST |
| FR-02 | Automated health check tiap 60 detik | `HealthCheckScheduler` — `@Scheduled(fixedRate=60000)` |
| FR-03 | Force re-check per service | `POST /api/services/{id}/check` |
| FR-04 | Self-observability | Spring Boot Actuator (`/actuator/health`, `/actuator/info`) |
| FR-05 | Consistent error response | `GlobalExceptionHandler` — `@RestControllerAdvice` |

#### Frontend

| ID | Requirement | Implementasi |
|----|-------------|--------------|
| FR-06 | Status dashboard | `DashboardComponent` — grid service cards |
| FR-07 | Visual indicators (UP/DOWN/UNKNOWN) | `StatusBadgeComponent` — warna + animasi + ikon |
| FR-08 | Real-time updates tanpa refresh | `MonitoringService` — RxJS polling tiap 10 detik |
| FR-09 | Force re-check button | `ServiceCardComponent` — tiap card ada tombol |
| FR-10 | Error state jika backend unreachable | `ErrorBannerComponent` + retry otomatis 30 detik |

### 3.2 Non-Functional Requirements

| Kategori | Target |
|----------|--------|
| Performance | API response < 500ms |
| Reliability | Scheduler auto-recover dari transient errors |
| Scalability | Tambah service baru tanpa kode changes |
| Observability | Semua health check events persist ke DB |
| Accessibility | WCAG 2.1 AA — warna bukan satu-satunya indikator |
| Browser | Latest 2 versi Chrome, Firefox, Edge, Safari |

---

## 4. Tech Stack

### 4.1 Backend

| Teknologi | Versi | Fungsi |
|-----------|-------|--------|
| Java JDK | 20+ | Runtime |
| Spring Boot | 3.3.0 | Framework utama |
| Spring Web | — | REST API (`@RestController`) |
| Spring Data JPA | — | ORM + database access |
| Spring Validation | — | Bean Validation (`@Valid`, `@NotBlank`) |
| Spring Actuator | — | Self-observability |
| H2 Database | Latest | Database development (in-memory) |
| PostgreSQL | 14+ | Database production |
| Maven | 3.9+ | Build tool |

### 4.2 Frontend

| Teknologi | Versi | Fungsi |
|-----------|-------|--------|
| Angular | 14+ | Frontend framework |
| TypeScript | ~4.8 | Type safety |
| RxJS | ~7.x | Reactive polling + state management |
| Tailwind CSS | 3+ | Utility-first styling |
| HttpClient | (built-in) | HTTP communication |
| Google Material Symbols | — | Icons |

### 4.3 Kenapa Stack Ini?

**Spring Boot 3.3:**
- `@Scheduled` untuk background job tanpa setup external
- Spring Data JPA — ganti database tinggal ganti profile
- Actuator — self-observability siap pakai
- Mature, enterprise-ready, alignment dengan ecosystem organisasi

**Angular 14 + RxJS:**
- TypeScript type safety untuk data model yang tetap
- RxJS `BehaviorSubject` untuk state management lightweight (tanpa NgRx)
- `HttpClient` built-in — no additional library needed
- Angular cocok untuk enterprise dashboard apps

**Tailwind CSS 3:**
- Utility-first: styling cepat tanpa CSS files terpisah
- Responsive design mudah dengan breakpoint prefix
- Custom design system (fonts, colors, spacing) via `tailwind.config.js`

**H2 (dev) → PostgreSQL (prod):**
- H2: zero setup, in-memory, cocok testing
- PostgreSQL: reliable, transactional, untuk production
- Ganti tinggal environment variable `SPRING_PROFILES_ACTIVE`

---

## 5. Arsitektur Sistem

### 5.1 High-Level Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                        Browser                               │
│  ┌──────────────────────────────────────────────────────┐   │
│  │              Angular 14 SPA                           │   │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────────────┐     │   │
│  │  │ Dashboard │ │ Service  │ │ StatusBadge     │     │   │
│  │  │ Component│ │ Card     │ │ Component       │     │   │
│  │  │          │ │ Component│ │                  │     │   │
│  │  └────┬─────┘ └────┬─────┘ └──────────────────┘     │   │
│  │       │             │                               │   │
│  │  ┌────▼─────────────▼──────────────────────────┐   │   │
│  │  │         MonitoringService                    │   │   │
│  │  │   BehaviorSubject<ServiceState>              │   │   │
│  │  │   timer(0, 10000) → switchMap → GET /api/.. │   │   │
│  │  └─────────────────────────────────────────────┘   │   │
│  └──────────────────────┬───────────────────────────┘   │
└─────────────────────────│──────────────────────────────┘
                          │ HTTP (localhost:4200 → proxy ke :8080)
                          ▼
┌──────────────────────────────────────────────────────────────┐
│                    Spring Boot 3.3                            │
│                                                              │
│  ┌──────────────────┐  ┌──────────────────┐  ┌────────────┐ │
│  │ ServiceController│  │HealthCheckSched. │  │ Actuator   │ │
│  │ REST API         │  │@Scheduled(60s)   │  │ /health    │ │
│  │ CRUD + check     │  │                  │  │ /info      │ │
│  └────────┬─────────┘  └────────┬─────────┘  └────────────┘ │
│           │                     │                           │
│  ┌────────▼─────────────────────▼───────────────────────┐  │
│  │  ServiceService         HealthCheckService            │  │
│  │  • CRUD logic           • HTTP GET ke service URL     │  │
│  │  • Entity↔DTO mapping   • Ukur latency                │  │
│  │                         • Simpan HealthCheckLog       │  │
│  │                         • Update ServiceEntity status │  │
│  └────────────────────────┬────────────────────────────┘  │
│                           │                                │
│  ┌────────────────────────▼────────────────────────────┐  │
│  │  ServiceRepository    HealthCheckLogRepository       │  │
│  │  JpaRepository<ServiceEntity, UUID>                 │  │
│  │  JpaRepository<HealthCheckLogEntity, UUID>          │  │
│  └────────────────────────┬────────────────────────────┘  │
└───────────────────────────│───────────────────────────────┘
                            │ JPA/Hibernate
                            ▼
              ┌──────────────────────────────┐
              │   Database                    │
              │   (H2 in-memory / PostgreSQL) │
              │                               │
              │   ┌────────────────────┐      │
              │   │ services           │      │
              │   │ • id (UUID, PK)    │      │
              │   │ • name             │      │
              │   │ • url              │      │
              │   │ • category         │      │
              │   │ • status           │      │
              │   │ • last_checked_at  │      │
              │   │ • latency_ms       │      │
              │   │ • created_at       │      │
              │   └────────────────────┘      │
              │   ┌────────────────────┐      │
              │   │ health_check_logs  │      │
              │   │ • id (UUID, PK)    │      │
              │   │ • service_id (FK)  │      │
              │   │ • status           │      │
              │   │ • latency_ms       │      │
              │   │ • checked_at       │      │
              │   │ • error_message    │      │
              │   └────────────────────┘      │
              └──────────────────────────────┘
```

### 5.2 Component Diagram (Frontend)

```
AppComponent
 └── DashboardComponent
      ├── SummaryBarComponent (total, UP, DOWN, UNKNOWN count)
      ├── ErrorBannerComponent (tampil kalau backend unreachable)
      ├── ServiceCardComponent (×N services)
      │    └── StatusBadgeComponent (UP/DOWN/UNKNOWN badge)
      └── [Loading/Skeleton State]
```

---

## 6. Backend

### 6.1 Entity & Database

#### `ServiceEntity` — `src/main/java/com/nuxatech/cmcc/entity/ServiceEntity.java`

| Field | Type | DB Column | Constraints |
|-------|------|-----------|-------------|
| `id` | `UUID` | `id` | PK, auto-generated |
| `name` | `String` | `name` | `@Column(nullable=false)` |
| `url` | `String` | `url` | `@Column(nullable=false)` |
| `category` | `String` | `category` | `@Column(nullable=false)` |
| `status` | `ServiceStatus` (enum) | `status` | `@Enumerated(STRING)`, default `UNKNOWN` |
| `lastCheckedAt` | `Instant` | `last_checked_at` | Nullable |
| `latencyMs` | `Long` | `latency_ms` | Nullable |
| `createdAt` | `Instant` | `created_at` | Auto-set via `@PrePersist` |

```java
@Entity
@Table(name = "services")
public class ServiceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceStatus status = ServiceStatus.UNKNOWN;

    @Column(name = "last_checked_at")
    private Instant lastCheckedAt;

    @Column(name = "latency_ms")
    private Long latencyMs;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
```

#### `ServiceStatus` — Enum

```java
public enum ServiceStatus {
    UP,
    DOWN,
    UNKNOWN
}
```

- `UP` — Health check sukses (HTTP 2xx)
- `DOWN` — Health check gagal (error, timeout, non-2xx)
- `UNKNOWN` — Belum pernah di-check (default saat create)

#### `HealthCheckLogEntity` — `src/main/java/com/nuxatech/cmcc/entity/HealthCheckLogEntity.java`

| Field | Type | DB Column | Constraints |
|-------|------|-----------|-------------|
| `id` | `UUID` | `id` | PK, auto-generated |
| `serviceId` | `UUID` | `service_id` | `@Column(nullable=false)` |
| `status` | `ServiceStatus` | `status` | `@Enumerated(STRING)` |
| `latencyMs` | `Long` | `latency_ms` | Nullable |
| `checkedAt` | `Instant` | `checked_at` | Auto-set via `@PrePersist` |
| `errorMessage` | `String` | `error_message` | `TEXT`, nullable |

```java
@Entity
@Table(name = "health_check_logs")
public class HealthCheckLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "service_id", nullable = false)
    private UUID serviceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceStatus status;

    @Column(name = "latency_ms")
    private Long latencyMs;

    @Column(name = "checked_at", nullable = false)
    private Instant checkedAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}
```

**Catatan:** `HealthCheckLogEntity` menyimpan **reference** ke `ServiceEntity` via `serviceId` (UUID), bukan via JPA `@ManyToOne` relationship. Ini adalah **intentional design choice** — relationship diputus di level entity untuk menjaga separation of concerns. Log health check bisa dilihat secara independen dari service.

#### Entity Relationship

```
ServiceEntity (1) ──────────── (N) HealthCheckLogEntity
    id (PK)                        serviceId (FK → ServiceEntity.id)
    name
    url
    category
    status
    lastCheckedAt
    latencyMs
    createdAt
```

**Cardinality:** One-to-Many (1 service punya banyak health check logs).

**Catatan:** Relationship ini **implisit** (via `serviceId` field), bukan JPA explicit mapping. Keuntungan:
- ServiceEntity tidak perlu load collection logs (performance)
- Log bisa di-query independen
- Tidak ada risiko LazyInitializationException

### 6.2 DTOs

#### `CreateServiceRequest` — Input untuk CREATE

```java
public class CreateServiceRequest {
    @NotBlank(message = "must not be blank")
    private String name;

    @NotBlank(message = "must not be blank")
    @Pattern(regexp = "^https?://.*", message = "must be a valid HTTP/HTTPS URL")
    private String url;

    @NotBlank(message = "must not be blank")
    private String category;
}
```

#### `UpdateServiceRequest` — Input untuk UPDATE

Sama dengan `CreateServiceRequest` (field sama).

#### `ServiceResponse` — Output untuk READ/LIST

```java
public class ServiceResponse {
    private UUID id;
    private String name;
    private String url;
    private String category;
    private ServiceStatus status;
    private Instant lastCheckedAt;
    private Long latencyMs;
    private Instant createdAt;

    public static ServiceResponse fromEntity(ServiceEntity entity) {
        // Mapping entity → DTO
    }
}
```

#### `CheckResponse` — Output untuk FORCE CHECK

```java
public class CheckResponse {
    private UUID serviceId;
    private ServiceStatus status;
    private Long latencyMs;
    private Instant checkedAt;
}
```

#### `ErrorResponse` — Error envelope

```java
public class ErrorResponse {
    private Instant timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
}
```

### 6.3 Repository Layer

#### `ServiceRepository`

```java
@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, UUID> {
}
```

#### `HealthCheckLogRepository`

```java
@Repository
public interface HealthCheckLogRepository extends JpaRepository<HealthCheckLogEntity, UUID> {
}
```

Keduanya extends `JpaRepository` — langsung dapat method CRUD standar (`findAll`, `findById`, `save`, `deleteById`, `existsById`) tanpa implementasi manual. Spring Data JPA generate implementasinya di runtime.

### 6.4 Service Layer

#### `ServiceService` — `src/main/java/com/nuxatech/cmcc/service/ServiceService.java`

Bertanggung jawab atas **CRUD service inventory**. Method:

| Method | Deskripsi |
|--------|-----------|
| `getAllServices()` | Return semua service sebagai `List<ServiceResponse>` |
| `getServiceById(UUID id)` | Cari by ID, throw `ServiceNotFoundException` jika tidak ada |
| `createService(CreateServiceRequest)` | Buat service baru, status default `UNKNOWN` |
| `updateService(UUID id, UpdateServiceRequest)` | Update service yang sudah ada |
| `deleteService(UUID id)` | Hapus service by ID |
| `getServiceEntity(UUID id)` | Return `ServiceEntity` langsung (dipakai scheduler & controller) |
| `getAllServiceEntities()` | Return semua `ServiceEntity` langsung |
| `saveServiceEntity(ServiceEntity)` | Simpan entity yang sudah dimodifikasi (dipakai setelah health check) |

**Design note:** Method `getServiceEntity`, `getAllServiceEntities`, `saveServiceEntity` ada karena `ServiceService` perlu expose entity ke `HealthCheckScheduler` dan `ServiceController` tanpa mengulang logika find/save. Controller dan scheduler tidak boleh langsung akses repository — tetap melalui service layer.

#### `HealthCheckService` — `src/main/java/com/nuxatech/cmcc/service/HealthCheckService.java`

Bertanggung jawab atas **eksekusi health check**. Method:

| Method | Deskripsi |
|--------|-----------|
| `checkService(ServiceEntity)` | Ping URL → catat latency → simpan log → update entity status |

**Alur `checkService()`:**

```
1. Catat start time (System.currentTimeMillis())
2. Buat HealthCheckLogEntity baru
3. HTTP GET ke service.url via RestTemplate
   │
   ├── Success (2xx) → status = UP, latency = now - start
   │
   ├── Timeout/ConnectionError → status = DOWN, error = "Timeout/connection error"
   │
   └── Other Exception → status = DOWN, error = e.getMessage()
4. Set checkedAt = Instant.now()
5. Simpan HealthCheckLogEntity ke DB
6. Update ServiceEntity: status, lastCheckedAt, latencyMs
7. Return HealthCheckLogEntity
```

**Separation of Concerns:**
- `HealthCheckService` hanya urusan **nge-ping + logging**
- `HealthCheckScheduler` yang orchestrasi (loop semua service, error handling siklus)
- `ServiceController` bisa panggil `checkService()` langsung untuk force re-check

#### Kenapa dipisah?

```java
// Scheduler — otomatis tiap 60 detik
@Scheduled(fixedRate = 60_000)
public void runHealthChecks() {
    // loop -> healthCheckService.checkService(service)
}

// Controller — manual force re-check
@PostMapping("/{id}/check")
public CheckResponse forceCheck(@PathVariable UUID id) {
    // healthCheckService.checkService(entity)
}
```

Tanpa separation, kode scheduler & controller akan duplikat. Dengan separation, `HealthCheckService.checkService()` bisa dipanggil dari dua entry point berbeda tanpa duplikasi.

### 6.5 Controller Layer

#### `ServiceController` — `src/main/java/com/nuxatech/cmcc/controller/ServiceController.java`

| Method | Endpoint | Deskripsi | Request Body | Response |
|--------|----------|-----------|-------------|----------|
| `GET` | `/api/services` | List semua service | — | `List<ServiceResponse>` (200) |
| `GET` | `/api/services/{id}` | Detail satu service | — | `ServiceResponse` (200) / Error (404) |
| `POST` | `/api/services` | Daftarkan service baru | `CreateServiceRequest` | `ServiceResponse` (201) |
| `PUT` | `/api/services/{id}` | Update service | `UpdateServiceRequest` | `ServiceResponse` (200) / Error (404) |
| `DELETE` | `/api/services/{id}` | Hapus service | — | 204 No Content / Error (404) |
| `POST` | `/api/services/{id}/check` | Force re-check satu service | — | `CheckResponse` (200) / Error (404) |
| `POST` | `/api/services/check` | Trigger check semua service | — | `List<CheckResponse>` (200) |

**Design note:** Controller hanya bertanggung jawab untuk:
1. Menerima HTTP request
2. Memvalidasi input (`@Valid`)
3. Memanggil service layer
4. Mengembalikan HTTP response

Logic bisnis **tidak ada** di controller — semua di service layer.

#### `forceCheck()` flow:

```java
@PostMapping("/{id}/check")
public CheckResponse forceCheck(@PathVariable UUID id) {
    var entity = serviceService.getServiceEntity(id);  // Dapat entity (throw 404 jika tidak ada)
    var result = healthCheckService.checkService(entity);  // Execute health check
    serviceService.saveServiceEntity(entity);  // Simpan updated entity
    return new CheckResponse(...);
}
```

### 6.6 Scheduler

#### `HealthCheckScheduler` — `src/main/java/com/nuxatech/cmcc/scheduler/HealthCheckScheduler.java`

```java
@Component
public class HealthCheckScheduler {
    private final AtomicBoolean running = new AtomicBoolean(false);

    @Scheduled(fixedRate = 60_000)
    public void runHealthChecks() {
        if (!running.compareAndSet(false, true)) {
            log.warn("Previous health check cycle still in progress, skipping");
            return;
        }
        try {
            var services = serviceService.getAllServiceEntities();
            for (var service : services) {
                try {
                    healthCheckService.checkService(service);
                    serviceService.saveServiceEntity(service);
                } catch (Exception e) {
                    log.error("Failed to check service {} ({}): {}",
                        service.getName(), service.getId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Health check cycle failed", e);
        } finally {
            running.set(false);
        }
    }
}
```

**Key design: `AtomicBoolean` idempotency guard**

**Masalah:** Scheduler trigger tiap 60 detik. Tapi kalau satu siklus health check memakan waktu >60 detik (misal karena ada service yang timeout 5 detik), siklus baru akan mulai sebelum siklus lama selesai. Akibatnya:
- Dua siklus berjalan bersamaan — overload
- Race condition update status service
- Multiple log entries untuk service yang sama dalam waktu hampir bersamaan

**Solusi:** `AtomicBoolean.compareAndSet(false, true)` — menjamin hanya satu siklus yang berjalan. Kalau siklus sebelumnya masih jalan, siklus baru di-skip dengan warning log.

**Error isolation:** Setiap service check di-wrap dalam `try-catch` individual. Satu service gagal (timeout, network error) tidak akan menghentikan pengecekan service lainnya.

#### `@EnableScheduling`

Aktivasi scheduler dilakukan di `CmccApplication.java`:

```java
@SpringBootApplication
@EnableScheduling
public class CmccApplication {
    public static void main(String[] args) {
        SpringApplication.run(CmccApplication.class, args);
    }
}
```

### 6.7 Config

#### `CorsConfig` — `src/main/java/com/nuxatech/cmcc/config/CorsConfig.java`

```java
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("http://localhost:4200", "http://localhost:3000")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*");
                registry.addMapping("/actuator/**")
                    .allowedOrigins("*")
                    .allowedMethods("GET")
                    .allowedHeaders("*");
            }
        };
    }
}
```

- **`/api/**`** — terbatas ke `localhost:4200` (Angular dev server) dan `localhost:3000`
- **`/actuator/**`** — terbuka untuk semua origin (public endpoint untuk monitoring)
- Method `OPTIONS` diizinkan untuk CORS preflight request

#### `RestClientConfig` — `src/main/java/com/nuxatech/cmcc/config/RestClientConfig.java`

```java
@Configuration
public class RestClientConfig {
    @Bean
    public RestTemplate restTemplate() {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5_000);
        factory.setReadTimeout(10_000);
        return new RestTemplate(factory);
    }
}
```

- **Connect timeout:** 5 detik — waktu maksimal untuk establish koneksi TCP
- **Read timeout:** 10 detik — waktu maksimal menunggu response setelah koneksi terbentuk

Kombinasi timeout ini mencegah health check cycle terblokir terlalu lama oleh service yang lambat atau unreachable.

#### `SchedulerHealthIndicator` — `src/main/java/com/nuxatech/cmcc/config/SchedulerHealthIndicator.java`

```java
@Component
public class SchedulerHealthIndicator implements HealthIndicator {
    private final HealthCheckScheduler scheduler;

    @Override
    public Health health() {
        if (scheduler.isRunning()) {
            return Health.up().withDetail("scheduler", "running").build();
        }
        return Health.up().withDetail("scheduler", "idle").build();
    }
}
```

Custom health indicator yang nge-expose status scheduler ke Actuator health endpoint. Status scheduler (`idle` atau `running`) muncul di `/actuator/health` sebagai komponen tambahan.

### 6.8 Exception Handling

#### `GlobalExceptionHandler` — `src/main/java/com/nuxatech/cmcc/exception/GlobalExceptionHandler.java`

| Exception | HTTP Status | Contoh |
|-----------|-------------|--------|
| `ServiceNotFoundException` | 404 | Service ID tidak ditemukan |
| `MethodArgumentNotValidException` | 400 | Validasi gagal (field blank, URL invalid) |
| `IllegalArgumentException` | 400 | Argument tidak valid |
| `Exception` (catch-all) | 500 | Unexpected error |

**Format response error:**

```json
{
    "timestamp": "2026-06-23T08:00:00Z",
    "status": 404,
    "error": "Not Found",
    "message": "Service not found: a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "path": "/api/services/a1b2c3d4-.../"
}
```

Semua error mengikuti format konsisten ini. Frontend bisa parsing dengan satu tipe data (`ErrorResponse`).

**Catatan:** `ServiceNotFoundException` sengaja tidak di-handle di controller. Dengan `@RestControllerAdvice`, semua exception yang dilempar dari service/controller akan tertangani secara terpusat. Tidak perlu try-catch di setiap endpoint.

### 6.9 API Specification

#### Base URL: `http://localhost:8080`

#### Service Inventory

##### `GET /api/services`
List semua service.

**Response 200:**
```json
[
    {
        "id": "a1b2c3d4-...",
        "name": "Payment Gateway",
        "url": "https://httpbin.org/status/200",
        "category": "Financial",
        "status": "UP",
        "lastCheckedAt": "2026-06-23T08:01:00Z",
        "latencyMs": 142,
        "createdAt": "2026-06-01T00:00:00Z"
    }
]
```

##### `GET /api/services/{id}`
Detail satu service.

**Response 200:** Sama seperti item di atas.
**Response 404:**
```json
{
    "timestamp": "...",
    "status": 404,
    "error": "Not Found",
    "message": "Service not found: a1b2c3d4-...",
    "path": "/api/services/a1b2c3d4-..."
}
```

##### `POST /api/services`
Daftarkan service baru.

**Request body:**
```json
{
    "name": "Payment Gateway",
    "url": "https://httpbin.org/status/200",
    "category": "Financial"
}
```

**Response 201:** Sama seperti item di list.
**Response 400 (validasi gagal):**
```json
{
    "timestamp": "...",
    "status": 400,
    "error": "Bad Request",
    "message": "Field 'url' must be a valid HTTP/HTTPS URL",
    "path": "/api/services"
}
```

##### `PUT /api/services/{id}`
Update service.

**Request body:** Sama seperti create.
**Response 200:** Sama seperti item di list.
**Response 404:** Service tidak ditemukan.

##### `DELETE /api/services/{id}`
Hapus service.

**Response 204:** No Content — sukses.
**Response 404:** Service tidak ditemukan.

#### Health Check

##### `POST /api/services/{id}/check`
Force re-check satu service.

**Response 200:**
```json
{
    "serviceId": "a1b2c3d4-...",
    "status": "UP",
    "latencyMs": 112,
    "checkedAt": "2026-06-23T08:05:33Z"
}
```

**Response 404:** Service tidak ditemukan.

##### `POST /api/services/check`
Trigger check semua service.

**Response 200:** Array of `CheckResponse`.

#### Self-Observability

##### `GET /actuator/health`
Health application + komponen (db, disk, scheduler).

**Response 200:**
```json
{
    "status": "UP",
    "components": {
        "db": { "status": "UP", "details": { "database": "H2" } },
        "diskSpace": { "status": "UP" },
        "ping": { "status": "UP" },
        "schedulerHealthIndicator": {
            "status": "UP",
            "details": { "scheduler": "idle" }
        }
    }
}
```

##### `GET /actuator/info`
Metadata aplikasi.

**Response 200:**
```json
{
    "app": {
        "name": "Centralized Monitoring Command Center",
        "version": "1.0.0",
        "description": "Service Reliability Initiative - Fullstack Developer Test",
        "java-version": "20",
        "spring-boot-version": "3.3.0"
    }
}
```

---

## 7. Frontend

### 7.1 Component Tree

```
AppComponent
└── DashboardComponent
    ├── Header (fixed top nav — "CMCC" + last sync time + refresh button)
    ├── ErrorBanner (conditional — tampil kalau backend unreachable)
    ├── SummaryBar (4 cards: Total, UP, DOWN, UNKNOWN)
    ├── ServiceCardComponent (×N — grid responsive 1/2/3 columns)
    │    └── StatusBadgeComponent (UP/DOWN/UNKNOWN)
    ├── [Skeleton shimmers] (loading state)
    └── [Empty state] (kalau tidak ada service)
```

### 7.2 Data Model

#### `service.model.ts`

```typescript
export interface Service {
  id: string;
  name: string;
  url: string;
  category: string;
  status: 'UP' | 'DOWN' | 'UNKNOWN';
  lastCheckedAt: string | null;
  latencyMs: number | null;
  createdAt: string;
}

export interface HealthCheckResult {
  serviceId: string;
  status: 'UP' | 'DOWN' | 'UNKNOWN';
  latencyMs: number | null;
  checkedAt: string;
}

export interface ServiceState {
  services: Service[];
  loading: boolean;
  error: string | null;
  lastSync: Date | null;
  consecutiveFailures: number;
}
```

### 7.3 Service Layer (RxJS)

#### `MonitoringService` — `src/main/webapp/app/services/monitoring.service.ts`

Service ini adalah **inti** dari frontend. Bertanggung jawab atas:

1. **State management** via `BehaviorSubject<ServiceState>`
2. **HTTP calls** ke backend API
3. **Polling** dengan RxJS
4. **Error handling** — consecutive failure guard

#### State Management: BehaviorSubject

```typescript
private state$ = new BehaviorSubject<ServiceState>({
    services: [],
    loading: false,
    error: null,
    lastSync: null,
    consecutiveFailures: 0,
});

readonly vm$ = this.state$.asObservable();
```

**Kenapa BehaviorSubject?**
- `getValue()` — akses state terkini secara synchronous (dipakai di `patchState`, `forceCheck`, `refreshNow`)
- `asObservable()` — component subscribe secara reaktif
- `.next({...state, ...partial})` — immutable update

**Alternatif yang dipertimbangkan:**
- **NgRx** — overkill untuk single-view SPA (boilerplate actions, reducers, effects)
- **Signal** (Angular 16+) — tidak available di Angular 14
- **Plain @Input/@Output** — tidak scalable, state jadi tersebar

#### Polling: timer + switchMap

```typescript
startPolling() {
    timer(0, environment.pollIntervalMs).pipe(
        takeUntil(this.destroy$),
        switchMap(() => this.http.get<Service[]>(url).pipe(
            map(services => {
                this.patchState({
                    services, loading: false, error: null,
                    lastSync: new Date(), consecutiveFailures: 0
                });
            }),
            catchError(error => {
                this.handlePollingError(error);
                return of(null);
            })
        ))
    ).subscribe();
}
```

**Alur:**
1. `timer(0, 10000)` — emit pertama langsung (delay 0), kemudian tiap 10 detik
2. `takeUntil(this.destroy$)` — unsubscribe otomatis saat `stopPolling()` dipanggil
3. `switchMap` — setiap ada emit timer baru, **unsubscribe dari HTTP request sebelumnya** (kalau masih pending)
4. `catchError` — tangkap error HTTP, increment failure counter
5. `of(null)` — setelah error, tetap lanjutkan stream (jangan throw)

**Kenapa `switchMap` bukan `mergeMap` atau `concatMap`?**

| Operator | Behavior | Cocok? |
|----------|----------|--------|
| `switchMap` | Cancel request sebelumnya, pakai yang baru | ✅ Ideal — stale response tidak mungkin menimpa data baru |
| `mergeMap` | Semua request jalan paralel, hasil bisa datang out-of-order | ❌ Risiko data lama timpa data baru |
| `concatMap` | Request antri, jalan sequential | ❌ Kalau request lambat, polling jadi tertunda |
| `exhaustMap` | Ignore emit baru kalau request sebelumnya masih jalan | ❌ Polling bisa terlewat |

#### Consecutive Failure Guard

```typescript
private handlePollingError(error: any) {
    const failures = this.state.consecutiveFailures + 1;
    if (failures >= environment.maxConsecutiveFailures) {
        this.stopPolling();
        this.patchState({
            error: 'Backend is unreachable',
            loading: false,
            consecutiveFailures: failures
        });
        setTimeout(() => this.retry(), environment.retryDelayMs);
    } else {
        this.patchState({ consecutiveFailures: failures, loading: false });
    }
}
```

**Flow:**
```
Failure #1 → increment counter → polling tetap jalan
Failure #2 → increment counter → polling tetap jalan
Failure #3 → STOP polling → show error banner → schedule retry dalam 30 detik
     │
     ▼
setTimeout 30 detik → retry() → reset counter → startPolling() lagi
     │
     ├── sukses → normal polling resumes
     └── gagal  → masuk error handling lagi (cycle repeats)
```

**Kenapa 3 failures?**
- 1 failure → bisa transient network glitch
- 2 failures → mungkin ada masalah, tapi belum yakin
- 3 failures → yakin backend unreachable, stop biar gak hammering

**Kenapa retry 30 detik?**
- Cukup lama untuk kasih waktu backend recovery
- Cukup singkat untuk tidak bikin user nunggu terlalu lama
- Tidak hammering backend (beda dengan polling 10 detik terus-terusan)

#### Force Check

```typescript
forceCheck(id: string): Observable<HealthCheckResult> {
    return this.http.post<HealthCheckResult>(url, {}).pipe(
        tap(result => {
            const updatedServices = this.state.services.map(s =>
                s.id === id ? { ...s, status: result.status,
                    latencyMs: result.latencyMs, lastCheckedAt: result.checkedAt } : s
            );
            this.patchState({ services: updatedServices });
        })
    );
}
```

Setelah dapat result, langsung update service yang relevan di local state tanpa menunggu polling berikutnya.

### 7.4 State Management

#### State Structure

```typescript
interface ServiceState {
    services: Service[];        // Daftar service + status terkini
    loading: boolean;            // Loading indicator (initial fetch)
    error: string | null;        // Error message (backend unreachable)
    lastSync: Date | null;       // Timestamp terakhir sukses fetch
    consecutiveFailures: number; // Counter untuk failure guard
}
```

#### State Transitions

| Event | `services` | `loading` | `error` | `consecutiveFailures` |
|-------|-----------|-----------|---------|----------------------|
| Initial state | `[]` | `false` | `null` | `0` |
| `startPolling()` | unchanged | `true` | `null` | `0` |
| Poll success | updated | `false` | `null` | `0` |
| Poll error (<3) | unchanged | `false` | `null` | incremented |
| Poll error (≥3) | unchanged | `false` | set | incremented |
| `forceCheck` success | 1 item updated | unchanged | unchanged | unchanged |
| `retry()` | unchanged | unchanged | `null` | `0` |

#### Why BehaviorSubject and not Component-local state?

Component-local state (via @Input/@Output) akan menyebarkan state ke banyak tempat. Dengan BehaviorSubject di service:
- **Single source of truth** — state hanya ada di satu tempat
- **Component apa pun bisa akses** tanpa props drilling
- **Testing mudah** — mock service saja

### 7.5 Dashboard Component

#### `dashboard.component.ts`

```typescript
export class DashboardComponent implements OnInit, OnDestroy {
    vm$ = this.monitoringService.vm$;
    checkingMap: { [id: string]: boolean } = {};
    lastSync: Date | null = null;
    refreshing = false;

    ngOnInit() {
        this.monitoringService.startPolling();
        this.monitoringService.vm$.subscribe(vm => {
            if (vm.lastSync) this.lastSync = vm.lastSync;
        });
    }

    ngOnDestroy() {
        this.monitoringService.stopPolling();
    }

    onRecheck(serviceId: string) {
        this.checkingMap[serviceId] = true;
        this.monitoringService.forceCheck(serviceId).subscribe({
            next: () => this.checkingMap[serviceId] = false,
            error: () => this.checkingMap[serviceId] = false
        });
    }
}
```

**Key points:**
- `vm$` di-assign sekali, template pakai `async` pipe — auto unsubscribe
- `checkingMap` track service mana yang sedang di-recheck (disable button + spinner)
- `ngOnDestroy` panggil `stopPolling()` — prevent memory leak saat navigate away
- `trackById` untuk `*ngFor` — optimasi rendering, Angular cuma re-render item yang berubah

### 7.6 Service Card Component

#### `service-card.component.html`

Ada 3 varian template (ngIf berdasarkan status):

**UP Card:**
- Border abu-abu tipis
- Badge hijau (StatusBadgeComponent)
- Latency ditampilkan dalam ms
- Tombol "Periksa Ulang" outline

**DOWN Card:**
- Border merah 2px (`border-2 border-[#ef4444]`)
- Background merah opacity 5% (via `absolute inset-0 bg-red opacity-5`)
- Badge merah dengan pulse animation
- Tombol "Periksa Ulang" filled hitam (lebih mencolok)
- Timestamp merah

**UNKNOWN Card:**
- Border abu-abu tipis
- Badge abu-abu
- Latency menampilkan `--`

### 7.7 UI Design System

#### Fonts

| Penggunaan | Font | Alasan |
|-----------|------|--------|
| UI Labels | `Inter` | Sans-serif, readable untuk text |
| Numeric data | `JetBrains Mono` | Monospace alignment — latency, timestamp, counter |

#### Colors (Custom Tailwind Theme)

| Token | Value | Penggunaan |
|-------|-------|-----------|
| `on-surface` | `#151c25` | Text utama |
| `surface` | `#f8f9ff` | Background utama |
| `outline-variant` | `#c6c6cd` | Border default |
| `surface-container-lowest` | `#ffffff` | Card background |

#### Status Colors

| Status | Warna | CSS |
|--------|-------|-----|
| UP | Green `#16a34a` | `text-[#16a34a]`, `bg-[#16a34a]` |
| DOWN | Red `#ef4444` | `text-[#ef4444]`, `bg-[#ef4444]` |
| UNKNOWN | Gray `#6b7280` | `text-gray-600`, `bg-gray-400` |

#### Responsive Layout

| Breakpoint | Kolom Service Card |
|------------|-------------------|
| Mobile (<768px) | 1 column |
| Tablet (768px+) | 2 columns |
| Desktop (1280px+) | 3 columns |

CSS: `grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3`

### 7.8 Environment Config

#### `environment.ts`

```typescript
export const environment = {
    production: false,
    apiBaseUrl: 'http://localhost:8080/api',
    pollIntervalMs: 10000,      // 10 detik
    maxConsecutiveFailures: 3,  // 3 kali gagal → stop
    retryDelayMs: 30000,        // 30 detik → retry
};
```

Semua config polling ada di environment — gampang diubah tanpa ubah kode logic.

---

## 8. Data Flow

### 8.1 Automated Health Check (Backend)

```
Waktu: T=0s
┌──────────────────────────────────────────────┐
│           HealthCheckScheduler                │
│  @Scheduled(fixedRate = 60_000)              │
│                                               │
│  1. compareAndSet(false, true) → running = OK │
│  2. serviceRepository.findAll()               │
│  3. For each service:                         │
│     ├─ healthCheckService.checkService(service)│
│     │  ├─ RestTemplate GET service.url        │
│     │  ├─ Catat latency                       │
│     │  ├─ Simpan HealthCheckLogEntity          │
│     │  └─ Update service.status               │
│     └─ serviceRepository.save(service)        │
│  4. running = false                           │
└──────────────────────────────────────────────┘
        │
Waktu: T+60s
        ├── Kalau siklus sebelumnya selesai ──► ulang langkah 1-4
        └── Kalau masih jalan ──► skip ("Previous cycle still in progress")

External Services:
        ▲
        │ HTTP GET
        │
┌───────┴────────┐
│ httpbin.org    │
│ /status/200    │ → UP + latency
│ /status/500    │ → DOWN
│ /delay/5       │ → UP (tapi lambat, latency ~5000ms)
│ (unreachable)  │ → DOWN (timeout setelah 10s)
└────────────────┘
```

### 8.2 Frontend Polling

```
Waktu: T=0s (page load)
┌──────────────────────────────────────────────┐
│  DashboardComponent.ngOnInit()               │
│  → monitoringService.startPolling()           │
└──────────────────────────────────────────────┘
        │
        ▼
┌──────────────────────────────────────────────┐
│  timer(0, 10000)                             │
│                                               │
│  Emit #0 (immediate)                          │
│  → switchMap → GET /api/services             │
│     │                                         │
│     ├── sukses → update state, reset counter  │
│     └── gagal  → increment counter            │
│                                               │
│  Emit #1 (T+10s)                              │
│  → switchMap → cancel request #0 (if pending) │
│  → GET /api/services baru                     │
│     │                                         │
│     └── ...                                   │
│                                               │
│  Emit #2 (T+20s)                              │
│  → ...                                        │
│                                               │
│  Kalau 3 kali gagal berturut-turut:          │
│  → stopPolling()                              │
│  → error banner muncul                         │
│  → setTimeout(retry, 30000)                   │
└──────────────────────────────────────────────┘

Waktu: T+30s (setelah stop)
┌──────────────────────────────────────────────┐
│  retry() → startPolling() lagi               │
│                                               │
│  ├── sukses → normal polling resumes           │
│  └── gagal  → stop lagi, setTimeout lagi      │
└──────────────────────────────────────────────┘
```

### 8.3 Force Re-check Flow

```
User klik "Periksa Ulang" di Service Card
        │
        ▼
┌──────────────────────────────────────────────┐
│  DashboardComponent                          │
│  checkingMap[id] = true  (disable button)    │
│  monitoringService.forceCheck(id)            │
│  → POST /api/services/{id}/check            │
└──────────────────────────────────────────────┘
        │
        ▼
┌──────────────────────────────────────────────┐
│  ServiceController.forceCheck(id)            │
│  → serviceService.getServiceEntity(id)       │
│  → healthCheckService.checkService(entity)   │
│     ├─ GET service.url                       │
│     ├─ Simpan log                            │
│     └─ Update entity status                  │
│  → serviceService.save(entity)               │
│  → return CheckResponse                      │
└──────────────────────────────────────────────┘
        │
        ▼
┌──────────────────────────────────────────────┐
│  Frontend menerima response                  │
│  → Update 1 item di services array           │
│  → checkingMap[id] = false  (enable button)  │
│  → UI langsung reflect perubahan             │
└──────────────────────────────────────────────┘
```

### 8.4 Error Handling Flow (Backend Unreachable)

```
┌──────────────────────────────────────────────┐
│  Timer emits new value                       │
│  → switchMap → GET /api/services             │
│                                               │
│  Connection refused / Network error           │
│                                               │
│  ┌──────────────────────────────────────┐    │
│  │ catchError → handlePollingError()    │    │
│  │                                      │    │
│  │ consecutiveFailures: 0 → 1          │    │
│  │ → polling tetap jalan               │    │
│  └──────────────────────────────────────┘    │
│                                               │
│  ... timer emit lagi, gagal lagi              │
│  consecutiveFailures: 1 → 2                  │
│                                               │
│  ... timer emit lagi, gagal lagi              │
│  consecutiveFailures: 2 → 3 ≥ 3             │
│                                               │
│  ┌──────────────────────────────────────┐    │
│  │ STOP polling                         │    │
│  │ error = "Backend is unreachable"     │    │
│  │ show error banner                    │    │
│  │ stale data tetap tampil              │    │
│  │ setTimeout(retry, 30000)            │    │
│  └──────────────────────────────────────┘    │
│                                               │
│  30 detik kemudian:                          │
│  retry() → startPolling()                    │
│  → GET /api/services                         │
│     │                                        │
│     ├── sukses → kembali normal              │
│     └── gagal  → stop lagi, retry 30s lagi   │
└──────────────────────────────────────────────┘
```

---

## 9. Configuration

### 9.1 Environment Profiles

#### `application.yml` (default)
```yaml
spring:
  profiles:
    active: local

server:
  port: 8080

management:
  endpoints:
    web:
      exposure:
        include: health,info

info:
  app:
    name: Centralized Monitoring Command Center
    version: 1.0.0
    description: Service Reliability Initiative - Fullstack Developer Test
    java-version: 20
    spring-boot-version: 3.3.0
```

#### `application-local.yml` (development)
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:cmcc
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    hibernate:
      ddl-auto: create-drop
    defer-datasource-initialization: true
  sql:
    init:
      mode: always
      data-locations: classpath:data.sql
```

#### `application-prod.yml` (production)
```yaml
spring:
  datasource:
    url: ${DATABASE_URL}
    driver-class-name: org.postgresql.Driver
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  sql:
    init:
      mode: never
```

### 9.2 Seed Data — `data.sql`

```sql
INSERT INTO services (id, name, url, category, status) VALUES
('a1b2c3d4-...', 'Payment Gateway',     'https://httpbin.org/status/200', 'Financial',      'UNKNOWN'),
('b2c3d4e5-...', 'Auth Service',        'https://httpbin.org/status/200', 'Internal',       'UNKNOWN'),
('c3d4e5f6-...', 'CRM API',             'https://httpbin.org/delay/5',    'External',       'UNKNOWN'),
('d4e5f6a7-...', 'Notification Service','https://httpbin.org/status/500', 'Internal',       'UNKNOWN'),
('e5f6a7b8-...', 'Logging Service',     'https://httpbin.org/status/200', 'Infrastructure', 'UNKNOWN');
```

Seed data menggunakan **httpbin.org** — service gratis untuk testing HTTP:
- `/status/200` — selalu return 200 → **UP**
- `/status/500` — selalu return 500 → **DOWN**
- `/delay/5` — delay 5 detik baru return 200 → **UP tapi lambat**

### 9.3 Frontend Environment

#### `environment.ts`
```typescript
export const environment = {
    production: false,
    apiBaseUrl: 'http://localhost:8080/api',
    pollIntervalMs: 10000,      // 10s
    maxConsecutiveFailures: 3,
    retryDelayMs: 30000,        // 30s
};
```

#### `environment.prod.ts` (jika ada)
```typescript
export const environment = {
    production: true,
    apiBaseUrl: '/api',
    pollIntervalMs: 10000,
    maxConsecutiveFailures: 3,
    retryDelayMs: 30000,
};
```

---

## 10. Testing

### 10.1 Backend Tests

Total: **21 tests**, 0 failures.

| Test Class | Jumlah | Coverage |
|-----------|--------|----------|
| `ServiceControllerTest` | 9 tests | HTTP layer, validation, 404 handling |
| `HealthCheckServiceTest` | 4 tests | UP detection, DOWN detection, timeout, state update |
| `ServiceServiceTest` | 7 tests | CRUD operations, exception propagation |
| `CmccApplicationTests` | 1 test | Spring context load |

#### `ServiceControllerTest` (9 tests)

Menggunakan `@WebMvcTest` + `@MockBean` untuk test controller layer saja (unit test, tanpa Spring context penuh).

**Test scenarios:**
- `GET /api/services` → 200, list services
- `GET /api/services/{id}` → 200, service detail
- `GET /api/services/{invalid-id}` → 404
- `POST /api/services` dengan valid body → 201
- `POST /api/services` dengan blank name → 400
- `POST /api/services` dengan invalid URL → 400
- `PUT /api/services/{id}` → 200
- `DELETE /api/services/{id}` → 204
- `POST /api/services/{id}/check` → 200

#### `HealthCheckServiceTest` (4 tests)

Menggunakan MockRestServiceServer untuk mock HTTP calls.

**Test scenarios:**
- HTTP 200 → status = UP, latency > 0
- HTTP 500 → status = DOWN, error message tercatat
- Timeout/connection error → status = DOWN
- After check, entity status & timestamp terupdate

#### `ServiceServiceTest` (7 tests)

**Test scenarios:**
- `getAllServices` → return list
- `getServiceById` found → return service
- `getServiceById` not found → throw ServiceNotFoundException
- `createService` → save & return, status = UNKNOWN
- `updateService` → update fields
- `deleteService` existing → delete
- `deleteService` not found → throw

### 10.2 Frontend Tests

Frontend menggunakan **Karma + Jasmine** (Angular 14 default). Terdapat spec files untuk:
- `app.component.spec.ts`
- `dashboard.component.spec.ts`
- `service-card.component.spec.ts`
- `summary-bar.component.spec.ts`
- `status-badge.component.spec.ts`
- `error-banner.component.spec.ts`

### 10.3 Running Tests

```bash
# Backend tests
mvn test

# Frontend tests
cd frontend
npm test
```

---

## 11. Setup & Running

### Prerequisites

| Tool | Version |
|------|---------|
| Java (JDK) | 20+ |
| Maven | 3.9+ |
| Node.js | 18+ |
| npm | 9+ |
| Angular CLI | 14+ |

### 1. Backend

```bash
# Clone project
git clone <repo-url>
cd nuxatech

# Run dengan Maven wrapper
./mvnw spring-boot:run

# Atau dengan Maven global
mvn spring-boot:run

# Backend akan start di http://localhost:8080
```

### 2. Frontend

```bash
cd frontend
npm install
npm start
# → http://localhost:4200
```

### 3. Verify

```bash
# Backend health
curl http://localhost:8080/actuator/health

# API services
curl http://localhost:8080/api/services

# Buka browser → http://localhost:4200
```

### 4. Production Mode

```bash
# Set environment
export SPRING_PROFILES_ACTIVE=prod
export DATABASE_URL=jdbc:postgresql://localhost:5432/cmcc
export DATABASE_USERNAME=postgres
export DATABASE_PASSWORD=secret

# Run
java -jar target/cmcc-1.0.0.jar
```

---

## 12. Project Structure

```
nuxatech/
├── pom.xml                                    # Maven project (Spring Boot 3.3)
├── README.md                                  # Dokumentasi utama proyek
├── smoke-test.bat                             # Smoke test Windows
├── smoke-test.ps1                             # Smoke test PowerShell
│
├── src/main/java/com/nuxatech/cmcc/
│   ├── CmccApplication.java                   # Entry point + @EnableScheduling
│   │
│   ├── controller/
│   │   └── ServiceController.java             # REST endpoints (7 endpoints)
│   │
│   ├── service/
│   │   ├── ServiceService.java                # CRUD logic service inventory
│   │   └── HealthCheckService.java            # HTTP ping + logging
│   │
│   ├── scheduler/
│   │   └── HealthCheckScheduler.java          # @Scheduled task (60s)
│   │
│   ├── entity/
│   │   ├── ServiceEntity.java                 # JPA entity — services table
│   │   ├── HealthCheckLogEntity.java          # JPA entity — health_check_logs table
│   │   └── ServiceStatus.java                 # Enum: UP, DOWN, UNKNOWN
│   │
│   ├── repository/
│   │   ├── ServiceRepository.java             # JPA repository
│   │   └── HealthCheckLogRepository.java      # JPA repository
│   │
│   ├── dto/
│   │   ├── CreateServiceRequest.java          # Input DTO (with validation)
│   │   ├── UpdateServiceRequest.java          # Input DTO (with validation)
│   │   ├── ServiceResponse.java               # Output DTO (+ fromEntity mapper)
│   │   ├── CheckResponse.java                 # Output DTO health check
│   │   └── ErrorResponse.java                 # Error envelope
│   │
│   ├── exception/
│   │   ├── ServiceNotFoundException.java      # Custom exception
│   │   └── GlobalExceptionHandler.java        # @RestControllerAdvice
│   │
│   └── config/
│       ├── CorsConfig.java                    # CORS configuration
│       ├── RestClientConfig.java              # RestTemplate with timeout
│       └── SchedulerHealthIndicator.java      # Custom actuator health
│
├── src/main/resources/
│   ├── application.yml                        # Default config (profile: local)
│   ├── application-local.yml                  # H2 in-memory config
│   ├── application-prod.yml                   # PostgreSQL config
│   └── data.sql                               # Seed data (5 services)
│
├── src/test/java/com/nuxatech/cmcc/
│   ├── CmccApplicationTests.java              # Context load test
│   ├── controller/
│   │   └── ServiceControllerTest.java         # 9 tests
│   └── service/
│       ├── HealthCheckServiceTest.java        # 4 tests
│       └── ServiceServiceTest.java            # 7 tests
│
├── frontend/                                  # Angular 14 app
│   ├── package.json
│   ├── angular.json
│   ├── tailwind.config.js                     # Custom design system
│   ├── karma.conf.js                          # Test config
│   │
│   └── src/
│       ├── index.html
│       ├── main.ts
│       ├── styles.css
│       │
│       ├── environments/
│       │   ├── environment.ts                 # Dev config
│       │   └── environment.prod.ts            # Prod config
│       │
│       └── app/
│           ├── app.module.ts                  # NgModule
│           ├── app.component.ts               # Root component
│           ├── app.component.html             # <app-dashboard>
│           │
│           ├── models/
│           │   └── service.model.ts           # Interfaces: Service, HealthCheckResult, ServiceState
│           │
│           ├── services/
│           │   └── monitoring.service.ts      # RxJS polling + BehaviorSubject state
│           │
│           └── components/
│               ├── dashboard/
│               │   ├── dashboard.component.ts
│               │   ├── dashboard.component.html
│               │   ├── dashboard.component.css
│               │   └── dashboard.component.spec.ts
│               │
│               ├── service-card/
│               │   ├── service-card.component.ts
│               │   ├── service-card.component.html
│               │   ├── service-card.component.css
│               │   └── service-card.component.spec.ts
│               │
│               ├── summary-bar/
│               │   ├── summary-bar.component.ts
│               │   ├── summary-bar.component.html
│               │   ├── summary-bar.component.css
│               │   └── summary-bar.component.spec.ts
│               │
│               ├── status-badge/
│               │   ├── status-badge.component.ts
│               │   ├── status-badge.component.html
│               │   ├── status-badge.component.css
│               │   └── status-badge.component.spec.ts
│               │
│               └── error-banner/
│                   ├── error-banner.component.ts
│                   ├── error-banner.component.html
│                   ├── error-banner.component.css
│                   └── error-banner.component.spec.ts
│
├── plan/                                      # Dokumentasi requirement
│   ├── requirement.md
│   ├── brief.md
│   ├── fe/
│   │   ├── fe.md
│   │   └── ui-brief.md
│   └── be/
│       ├── backend-implementation.md
│       └── backend-implementation-tasklist.md
│
└── assets/
    ├── cmccDashboard.png                      # Screenshot dashboard
    └── ArchitectureDiagram.png                # Diagram arsitektur
```

---

*Dokumentasi ini mencakup seluruh aspek proyek CMCC — dari business context, arsitektur, implementasi detail, hingga testing. Dibuat sebagai referensi lengkap untuk technical review dan knowledge transfer.*
