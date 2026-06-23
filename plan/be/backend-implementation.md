# Backend Implementation Plan
## Centralized Monitoring Command Center (CMCC)

**Scope:** Backend only
**Status:** Draft
**Reference:** [brief.md](brief.md)

---

## 1. Tujuan

Membangun backend CMCC yang mampu:
- Menyimpan dan mengelola inventaris service yang dipantau.
- Menjalankan health check otomatis setiap 60 detik.
- Menyediakan endpoint force re-check untuk satu service.
- Mencatat riwayat health check ke database.
- Mengekspos health dan info aplikasi via Actuator.
- Menangani error secara konsisten tanpa menghentikan scheduler.

---

## 2. Ruang Lingkup Backend

### In Scope
- REST API untuk CRUD service.
- Endpoint `POST /api/services/{id}/check`.
- Scheduler health check otomatis untuk seluruh service terdaftar.
- Entity dan repository untuk service serta health check log.
- Konfigurasi Actuator.
- Validasi input dan error response yang seragam.
- Seed data untuk kebutuhan development lokal.
- Konfigurasi CORS untuk Angular dev server.

### Out of Scope
- Authentication dan authorization.
- WebSocket real-time push.
- Frontend Angular.
- Notifikasi email/Slack/PagerDuty.
- Analitik historis dan dashboard tren.

---

## 3. Asumsi Teknis

- Stack utama mengikuti brief: Spring Boot 3.x, JPA, H2 untuk lokal, PostgreSQL untuk production.
- Polling frontend akan membaca data dari endpoint service yang sama, jadi backend harus menjaga response stabil dan konsisten.
- Health check scheduler berjalan di proses aplikasi yang sama dengan REST API.
- Untuk MVP, endpoint Actuator dibuka tanpa autentikasi.
- Health endpoint harus mencerminkan konektivitas database dan liveness scheduler.

---

## 4. Urutan Implementasi

### Phase 1 - Fondasi Project
1. Inisialisasi project Spring Boot.
2. Tambahkan dependency inti:
   - Spring Web
   - Spring Data JPA
   - Validation
   - Actuator
   - H2 Database
   - PostgreSQL Driver
3. Siapkan struktur package yang rapi.
4. Tambahkan konfigurasi awal `application.yml` atau `application.properties`.
5. Aktifkan scheduling dan CORS untuk origin Angular dev server.
6. Tambahkan seed data lokal jika diperlukan.

### Phase 2 - Model Data dan Persistensi
1. Buat entity `Service`.
2. Buat entity `HealthCheckLog`.
3. Buat enum status service: `UP`, `DOWN`, `UNKNOWN`.
4. Buat repository untuk masing-masing entity.
5. Pastikan relasi data antara service dan log tervalidasi dengan baik.
6. Tetapkan field service sesuai brief: `id`, `name`, `url`, `category`, `status`, `lastCheckedAt`, `latencyMs`, `createdAt`.
7. Tetapkan field health check log sesuai brief: `id`, `serviceId`, `status`, `latencyMs`, `checkedAt`, `errorMessage`.

### Phase 3 - CRUD Service API
1. Buat DTO untuk request dan response.
2. Buat service layer untuk logika CRUD.
3. Buat controller REST untuk endpoint:
   - `GET /api/services`
   - `GET /api/services/{id}`
   - `POST /api/services`
   - `PUT /api/services/{id}`
   - `DELETE /api/services/{id}`
4. Tambahkan validasi untuk `name` dan `url`.
5. Tambahkan validasi `category`.
6. Pastikan response mengikuti format yang konsisten.
7. Pastikan service baru default ke `UNKNOWN`.
8. Pastikan delete tidak menghapus audit log yang sudah tersimpan.

### Phase 4 - Health Check Engine
1. Buat komponen utilitas untuk melakukan HTTP GET ke URL service.
2. Ukur latency per request.
3. Tentukan status berdasarkan hasil request:
   - `UP` jika HTTP 2xx.
   - `DOWN` jika error, timeout, atau non-2xx.
4. Simpan hasil ke `HealthCheckLog`.
5. Update status terakhir pada entity `Service`.
6. Pastikan kegagalan satu service tidak menghentikan proses service lain.
7. Simpan `errorMessage` saat request gagal dan `null` saat request berhasil.

### Phase 5 - Scheduler Otomatis
1. Aktifkan scheduling di aplikasi.
2. Buat job yang berjalan setiap 60 detik.
3. Job memproses seluruh service terdaftar.
4. Tambahkan proteksi agar scheduler tidak berjalan dobel jika ada overlap.
5. Logging scheduler harus cukup detail untuk debugging.
6. Pastikan scheduler tidak memutus job saat satu service gagal.

### Phase 6 - Force Re-check Endpoint
1. Tambahkan endpoint `POST /api/services/{id}/check`.
2. Endpoint memicu health check untuk satu service secara sinkron.
3. Kembalikan response berisi status, latency, dan timestamp check.
4. Jika service tidak ditemukan, kembalikan `404` dengan payload error yang jelas.
5. Re-check menggunakan executor yang sama dengan scheduler agar tidak terjadi duplikasi logika.

### Phase 7 - Actuator dan Self-Observability
1. Aktifkan endpoint `GET /actuator/health`.
2. Aktifkan endpoint `GET /actuator/info`.
3. Tambahkan indikator health untuk konektivitas database.
4. Tambahkan indikator health yang merepresentasikan scheduler liveness.
5. Pastikan endpoint health bisa diakses publik pada environment MVP.
6. Pastikan `/actuator/info` memuat metadata aplikasi minimum.

### Phase 8 - Error Handling dan Quality Gates
1. Buat handler global untuk error validasi dan error runtime umum.
2. Samakan format error response untuk seluruh endpoint API.
3. Tambahkan logging yang mudah ditelusuri.
4. Validasi bahwa timeout, exception jaringan, dan error database ditangani aman.
5. Tambahkan test minimal untuk alur utama.
6. Pastikan response 400, 404, dan 500 mengikuti envelope yang sama.

---

## 5. Rancangan Modul Backend

### Package yang Disarankan
- `config` - konfigurasi scheduling, HTTP client, Actuator custom jika ada.
- `controller` - REST controller.
- `dto` - request dan response model.
- `entity` - JPA entity.
- `exception` - custom exception dan global handler.
- `repository` - Spring Data repository.
- `service` - business logic.
- `scheduler` - job health check.
- `util` - helper untuk status mapping dan request timing.

### Komponen Utama
- `ServiceEntity`
- `HealthCheckLogEntity`
- `ServiceRepository`
- `HealthCheckLogRepository`
- `ServiceController`
- `ServiceManagementService`
- `HealthCheckScheduler`
- `HealthCheckExecutor`
- `GlobalExceptionHandler`

---

## 6. Desain API Backend

### Service Inventory
- `GET /api/services`
- `GET /api/services/{id}`
- `POST /api/services`
- `PUT /api/services/{id}`
- `DELETE /api/services/{id}`

### Health Check
- `POST /api/services/{id}/check`

### Observability
- `GET /actuator/health`
- `GET /actuator/info`

### Error Format
Semua error API harus memakai envelope konsisten:

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

## 7. Aturan Bisnis Inti

- Service baru default ke status `UNKNOWN` sampai health check pertama selesai.
- Service dianggap `UP` hanya jika respons HTTP 2xx.
- Semua error jaringan, timeout, dan non-2xx dianggap `DOWN`.
- Setiap hasil pemeriksaan harus disimpan sebagai log.
- Scheduler tidak boleh gagal total hanya karena satu service bermasalah.
- Jika service dihapus, riwayat log tetap disimpan untuk audit.

---

## 8. Strategi Validasi dan Testing

### Unit Test
- Validasi service creation dan update.
- Mapping status hasil health check.
- Penanganan timeout dan exception HTTP.
- Global exception handler.

### Integration Test
- CRUD endpoint dengan database in-memory.
- Endpoint force re-check.
- Scheduler atau executor health check pada beberapa service.
- Actuator health endpoint.
- CORS untuk origin Angular dev server.

### Smoke Check Lokal
- Service CRUD berhasil.
- Health check otomatis menulis log.
- Force re-check mengubah status service.
- `/actuator/health` aktif dan dapat diakses.

---

## 9. Urutan Deliverable

1. Project Spring Boot dasar berjalan.
2. Schema database dan entity siap.
3. CRUD service selesai.
4. Health check executor selesai.
5. Scheduler otomatis berjalan.
6. Force re-check endpoint selesai.
7. Actuator aktif.
8. Error handling konsisten.
9. Test dasar lulus.
10. Dokumentasi backend diperbarui.
11. Konfigurasi CORS dan metadata info tervalidasi.

---

## 10. Risiko dan Mitigasi

### Risiko
- Timeout HTTP ke service target dapat memperlambat scheduler.
- Scheduler overlap jika jumlah service bertambah.
- Format error response tidak konsisten antar endpoint.
- Koneksi database gagal sehingga health endpoint ikut turun.

### Mitigasi
- Terapkan timeout HTTP yang jelas.
- Jalankan health check per service dengan isolasi error.
- Gunakan global exception handler.
- Tambahkan indikator health untuk database dan logging yang cukup.
- Tambahkan guard agar scheduler tidak menjalankan siklus baru saat siklus sebelumnya belum selesai.

---

## 11. Definition of Done

Backend dianggap selesai jika:
- Semua endpoint inventory API tersedia.
- Health check otomatis berjalan setiap 60 detik.
- Force re-check bekerja untuk satu service.
- Log pemeriksaan tersimpan ke database.
- Actuator health dan info aktif.
- Error validation dan not-found konsisten.
- Test utama lulus.

---

## 12. Catatan Implementasi

- Fokus awal sebaiknya pada model data dan CRUD, karena itu menjadi dasar untuk scheduler dan force re-check.
- Scheduler dan endpoint manual sebaiknya memakai komponen executor yang sama agar logika health check tidak duplikatif.
- Dokumentasi backend sebaiknya diperbarui bersamaan saat endpoint selesai dibuat.
