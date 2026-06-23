# Frontend Implementation Plan — CMCC Dashboard

**Project:** Centralized Monitoring Command Center  
**Stack:** Angular 14+ / Tailwind CSS 3+ / RxJS  
**Target:** Dashboard SPA untuk Support Engineer & System Administrator

---

## 1. Project Initialization

### 1.1 Setup Angular

```bash
npx @angular/cli@14 new frontend --routing=false --style=css
cd frontend
```

### 1.2 Setup Tailwind CSS

- Install via `npm install -D tailwindcss@3 postcss autoprefixer`
- Generate config: `npx tailwindcss init`
- Konfigurasi `tailwind.config.js` dengan `content: ['./src/**/*.{html,ts}']`
- Tambahkan `@tailwind` directives ke `src/styles.css`
- Pastikan tidak ada konflik dengan CSS default Angular

---

## 2. Directory Structure

```
src/
  app/
    components/
      dashboard/          # Halaman utama
      service-card/       # Card per service
      status-badge/       # Badge reusable UP/DOWN/UNKNOWN
      error-banner/       # Banner backend unreachable
      summary-bar/        # Summary stats (total, UP, DOWN, UNKNOWN)
    services/
      monitoring.service.ts    # HTTP calls + RxJS polling + state
    models/
      service.model.ts         # Interface Service & HealthCheckResult
  assets/
  styles.css                    # Tailwind + custom CSS (blink animation)
```

> `store/service.store.ts` dihapus — state dikelola langsung di `MonitoringService` untuk menghindari sinkronisasi yang tidak perlu pada single-view SPA ini.

---

## 3. Models

### `service.model.ts`

```typescript
export interface Service {
  id: string;
  name: string;
  url: string;
  category: string;
  status: 'UP' | 'DOWN' | 'UNKNOWN';
  lastCheckedAt: string | null;
  latencyMs: number | null;
  createdAt: string; // dikembalikan backend, tidak ditampilkan di card
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

---

## 4. `MonitoringService`

Satu service yang memegang seluruh HTTP calls, polling logic, dan state.

| Method | HTTP | Endpoint |
|--------|------|----------|
| `getServices()` | GET | `/api/services` |
| `forceCheck(id)` | POST | `/api/services/{id}/check` |

**State (BehaviorSubject):**

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

**Polling logic:**
- RxJS `interval(pollIntervalMs)` + `switchMap` → `getServices()`
- `shareReplay({ bufferSize: 1, refCount: true })` agar tidak ada memory leak saat semua subscriber unsubscribe
- Consecutive failures dihitung di state; setelah 3 → stop polling + emit error
- Method `startPolling()` / `stopPolling()` / `retry()`

**Force re-check:**
- Gunakan `exhaustMap` (atau `take(1)`) — bukan `switchMap` — agar klik ganda diabaikan
- Response sukses: update service yang bersangkutan di array state secara lokal
- Response error: lempar ke caller untuk ditangani inline per card

---

## 5. Component Tree

```
AppComponent
  └── DashboardComponent
        ├── SummaryBarComponent
        │     (total / UP / DOWN / UNKNOWN count)
        ├── ServiceCardComponent (×n)
        │     ├── StatusBadgeComponent
        │     └── [Force Re-check button]
        └── ErrorBannerComponent
              (shown when backend unreachable)
```

### 5.1 `DashboardComponent`

- Subscribe ke `monitoringService.vm$`
- Panggil `monitoringService.startPolling()` di `ngOnInit`, `stopPolling()` di `ngOnDestroy`
- Pass data ke children via `@Input()`
- Handler untuk event `@Output()` re-check dari card

### 5.2 `ServiceCardComponent`

**Inputs:**
- `service: Service`
- `checking: boolean` (sedang di-recheck)

**Outputs:**
- `recheck: EventEmitter<string>`

**Template:**
- Status badge (kiri)
- Nama + kategori
- Latency (ms)
- Last checked timestamp
- Tombol "Re-check" dengan disabled + spinner saat `checking`
- Inline error message jika force re-check gagal

### 5.3 `StatusBadgeComponent`

**Inputs:**
- `status: 'UP' | 'DOWN' | 'UNKNOWN'`

**Logic:**
- `UP` → `bg-green-500`, dot hijau solid
- `DOWN` → `bg-red-500`, class `status-down` (animasi blink)
- `UNKNOWN` → `bg-gray-400`, dot abu-abu
- Selalu sertakan text label (aksesibilitas WCAG AA)

### 5.4 `SummaryBarComponent`

**Inputs:**
- `services: Service[]`

Hitung total, UP, DOWN, UNKNOWN — tampilkan sebagai chip berwarna.

### 5.5 `ErrorBannerComponent`

**Inputs:**
- `message: string`
- `hasStaleData: boolean`

Banner di atas dashboard saat backend tidak bisa dijangkau. Tampilkan data terakhir yang diketahui (stale) di belakang banner.

---

## 6. Visual Indicators

### Status Colors

| Status | Tailwind Class | Animasi |
|--------|---------------|---------|
| UP | `bg-green-500` / `text-green-600` | none |
| DOWN | `bg-red-500` / `text-red-600` | blink 1s pulse |
| UNKNOWN | `bg-gray-400` / `text-gray-500` | none |

### Blink Animation (`styles.css`)

```css
@keyframes pulse-blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

.status-down {
  animation: pulse-blink 1s ease-in-out infinite;
}
```

---

## 7. Force Re-check Flow

```
User klik [Re-check]
  → button disabled + spinner muncul di card tersebut
  → monitoringService.forceCheck(id) dipanggil (exhaustMap / take(1))
  → response sukses:
      → update service di state (status + latencyMs + lastCheckedAt)
      → button normal kembali
  → response error:
      → tampilkan inline error di card tersebut saja
      → button normal kembali
```

Kegagalan re-check satu card tidak mempengaruhi card lain.

---

## 8. Error Handling: Backend Unreachable

```
Polling gagal (timeout / network error)
  → consecutiveFailures++
  → jika < 3: log warning, lanjut polling
  → jika >= 3:
      → stopPolling()
      → set error state → tampilkan ErrorBannerComponent
      → pertahankan services terakhir (stale data)
      → setTimeout(retryDelayMs) → retry()
  → retry sukses:
      → reset error + consecutiveFailures = 0
      → startPolling() normal
```

---

## 9. Responsive Breakpoints

| Device | Width | Layout |
|--------|-------|--------|
| Mobile | ≥ 375px | 1 column |
| Tablet | ≥ 768px | 2 columns |
| Desktop | ≥ 1280px | 3 columns |

```html
<div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
```

---

## 10. Environment Config

### `src/environments/environment.ts`

```typescript
export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8080/api',
  pollIntervalMs: 10000,
  maxConsecutiveFailures: 3,
  retryDelayMs: 30000,
};
```

---

## 11. Implementation Order

| # | Task | Notes |
|---|------|-------|
| 1 | Init Angular project + Tailwind | Pastikan Tailwind berfungsi |
| 2 | Buat models (`Service`, `HealthCheckResult`, `ServiceState`) | |
| 3 | Buat `MonitoringService` (HTTP + polling + state) | |
| 4 | Buat `StatusBadgeComponent` | Paling sederhana, bisa ditest sendiri |
| 5 | Buat `ServiceCardComponent` | Integrasi badge + button |
| 6 | Buat `SummaryBarComponent` | Hitung statistik |
| 7 | Buat `ErrorBannerComponent` | |
| 8 | Buat `DashboardComponent` | Gabung semua, konek ke MonitoringService |
| 9 | Responsive layout + blink animation | Grid breakpoints |
| 10 | Smoke test end-to-end dengan backend | |

---

## 12. Key Decisions

| Aspek | Pilihan | Alasan |
|-------|---------|--------|
| State management | `BehaviorSubject` di `MonitoringService` | Tidak perlu file store terpisah untuk single-view SPA |
| Polling | RxJS `interval` + `switchMap` | Cancel otomatis request sebelumnya |
| Force re-check | `exhaustMap` / `take(1)` | Mencegah double-submit; button sudah disabled tapi semantik lebih tepat |
| `shareReplay` | `{ bufferSize: 1, refCount: true }` | Mencegah memory leak saat semua subscriber unsubscribe |
| Styling | Tailwind utility | Cepat, responsive built-in |
| No routing | Single view | Hanya 1 view dashboard |
| No NgRx / Signal | Overkill untuk MVP | `BehaviorSubject` cukup |
