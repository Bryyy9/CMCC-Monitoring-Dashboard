# Backend Implementation Tasklist
## Centralized Monitoring Command Center (CMCC)

**Fokus:** Implementasi backend
**Status:** Draft
**Referensi:** [brief.md](brief.md)

---

## 1. Target Implementasi

Backend harus menyediakan:
- CRUD service yang dipantau.
- Health check otomatis tiap 60 detik.
- Endpoint force re-check per service.
- Pencatatan hasil health check ke database.
- Actuator untuk health dan info aplikasi.
- Error response yang konsisten.

---

## 2. Struktur Modul yang Akan Dibuat

### Package
- `config` untuk konfigurasi scheduler, HTTP client, Actuator, dan CORS.
- `controller` untuk REST endpoint.
- `dto` untuk request/response.
- `entity` untuk JPA entity.
- `exception` untuk custom exception dan global handler.
- `repository` untuk Spring Data repository.
- `service` untuk business logic.
- `scheduler` untuk job health check otomatis.
- `util` untuk helper status dan timing.

### File Utama
- `ServiceEntity`
- `HealthCheckLogEntity`
- `ServiceRepository`
- `HealthCheckLogRepository`
- `ServiceController`
- `ServiceService`
- `HealthCheckService`
- `HealthCheckScheduler`
- `GlobalExceptionHandler`
- `ServiceNotFoundException`
- `ApiErrorResponse`

---

## 3. Urutan Pengerjaan

### Langkah 1 - Inisialisasi Project
- Buat project Spring Boot 3.x.
- Tambahkan dependency:
  - Spring Web
  - Spring Data JPA
  - Validation
  - Actuator
  - H2 Database
  - PostgreSQL Driver
  - Test starter
- Aktifkan scheduling di aplikasi.
- Aktifkan CORS untuk origin Angular dev server.
- Siapkan konfigurasi environment untuk lokal dan production.

### Langkah 2 - Database dan Entity
- Buat enum status: `UP`, `DOWN`, `UNKNOWN`.
- Buat entity service dengan field:
  - `id`
  - `name`
  - `url`
  - `category`
  - `status`
  - `lastCheckedAt`
  - `latencyMs`
  - `createdAt`
- Buat entity health check log dengan field:
  - `id`
  - `serviceId`
  - `status`
  - `latencyMs`
  - `checkedAt`
  - `errorMessage`
- Hubungkan entity dengan repository JPA.

### Langkah 3 - CRUD Service API
- Buat DTO untuk create dan update service.
- Buat DTO response service.
- Implement endpoint:
  - `GET /api/services`
  - `GET /api/services/{id}`
  - `POST /api/services`
  - `PUT /api/services/{id}`
  - `DELETE /api/services/{id}`
- Tambahkan validasi:
  - `name` wajib diisi.
  - `url` wajib format HTTP/HTTPS valid.
  - `category` wajib diisi.
- Pastikan service baru default ke `UNKNOWN`.
- Pastikan delete service tidak menghapus health check log yang sudah ada.

### Langkah 4 - Mesin Health Check
- Buat komponen yang melakukan HTTP GET ke URL service.
- Atur timeout request agar tidak menggantung.
- Ukur latency setiap request.
- Tentukan status:
  - `UP` jika response 2xx.
  - `DOWN` jika timeout, error jaringan, atau response non-2xx.
- Simpan hasil ke tabel health check log.
- Update status service terakhir setelah check selesai.
- Simpan `errorMessage` saat gagal dan kosongkan saat sukses.

### Langkah 5 - Scheduler Otomatis
- Buat job scheduler yang berjalan setiap 60 detik.
- Scheduler mengambil semua service dari database.
- Jalankan health check per service secara terisolasi.
- Pastikan error pada satu service tidak menghentikan service lain.
- Tambahkan logging untuk memudahkan tracing.
- Tambahkan guard agar tidak ada overlap eksekusi scheduler.

### Langkah 6 - Force Re-check
- Tambahkan endpoint `POST /api/services/{id}/check`.
- Endpoint menjalankan health check untuk service tertentu secara langsung.
- Response berisi status, latency, dan timestamp.
- Jika service tidak ada, kembalikan `404`.
- Gunakan executor yang sama dengan scheduler.

### Langkah 7 - Actuator dan Health Endpoint
- Aktifkan `GET /actuator/health`.
- Aktifkan `GET /actuator/info`.
- Pastikan endpoint health dapat diakses tanpa autentikasi.
- Tambahkan indikator untuk koneksi database.
- Tambahkan indikator scheduler liveness.
- Pastikan `/actuator/info` memuat metadata aplikasi minimum.

### Langkah 8 - Error Handling
- Buat global exception handler.
- Samakan format error response untuk semua endpoint.
- Tangani validasi gagal dengan response `400`.
- Tangani data tidak ditemukan dengan response `404`.
- Pastikan error dari health check disimpan di log, bukan dilempar ke layer atas.
- Pastikan response `400`, `404`, dan `500` memakai envelope yang sama.

### Langkah 9 - Testing
- Buat unit test untuk service layer.
- Buat integration test untuk endpoint CRUD.
- Buat integration test untuk endpoint force re-check.
- Buat test untuk global error handler.
- Buat smoke test untuk actuator health.

---

## 4. Desain Endpoint Backend

### Inventory Service
- `GET /api/services`
- `GET /api/services/{id}`
- `POST /api/services`
- `PUT /api/services/{id}`
- `DELETE /api/services/{id}`

### Health Check Manual
- `POST /api/services/{id}/check`

### Observability
- `GET /actuator/health`
- `GET /actuator/info`

---

## 5. Format Response yang Disarankan

### Service Response
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

### Health Check Response
```json
{
  "serviceId": "uuid",
  "status": "UP",
  "latencyMs": 112,
  "checkedAt": "2026-06-23T08:05:33Z"
}
```

### Error Response
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

## 6. Checklist Pengerjaan

### Setup
- [ ] Project Spring Boot berhasil dijalankan.
- [ ] Konfigurasi H2 untuk lokal tersedia.
- [ ] Konfigurasi PostgreSQL untuk production tersedia.
- [ ] Actuator aktif.

### Data Layer
- [ ] Entity service dibuat.
- [ ] Entity health check log dibuat.
- [ ] Repository service dibuat.
- [ ] Repository health check log dibuat.

### API Layer
- [ ] CRUD service selesai.
- [ ] Endpoint force re-check selesai.
- [ ] Validasi input aktif.
- [ ] Error response konsisten.

### Scheduler
- [ ] Scheduler 60 detik aktif.
- [ ] Health check per service berjalan.
- [ ] Log hasil pemeriksaan tersimpan.
- [ ] Error satu service tidak menghentikan job.

### Observability
- [ ] `/actuator/health` dapat diakses.
- [ ] `/actuator/info` dapat diakses.
- [ ] Database connectivity terlihat di health status.
- [ ] Scheduler liveness terlihat di health status.

### Testing
- [ ] Unit test service layer lulus.
- [ ] Integration test endpoint lulus.
- [ ] Smoke test actuator lulus.
- [ ] CORS untuk Angular dev server tervalidasi.

---

## 7. Definition of Done

Backend dinyatakan siap jika:
- Semua endpoint utama berjalan.
- Health check otomatis menulis log ke database.
- Force re-check memperbarui status service.
- Actuator health dan info aktif.
- Error handling konsisten.
- Test dasar lulus.

---

## 8. Commit Message

`docs: tambah tasklist implementasi backend CMCC`
