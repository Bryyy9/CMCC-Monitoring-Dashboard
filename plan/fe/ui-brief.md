# UI Brief — CMCC Dashboard

**Project:** Centralized Monitoring Command Center (CMCC)
**Target:** Support Engineer dan System Administrator
**Stack UI:** Angular 14+ / Tailwind CSS 3+ / RxJS

---

## 1. Tujuan UI

UI CMCC harus memberi tampilan yang cepat dipindai, informatif, dan aman dipakai saat kondisi insiden. Fokus utamanya adalah menunjukkan status semua service dalam satu layar, memberi sinyal visual yang sangat jelas untuk status `UP`, `DOWN`, dan `UNKNOWN`, serta mendukung aksi cepat seperti re-check per service.

---

## 2. Prinsip Desain

- Informasi status harus terbaca dalam sekali lihat.
- Warna boleh membantu, tetapi selalu disertai label teks.
- Layout harus tetap rapi di mobile, tablet, dan desktop.
- State loading, error, dan stale data harus terlihat jelas.
- Aksi utama hanya satu: cek status service dan re-check manual.

---

## 3. Struktur Halaman

Halaman dashboard dibagi menjadi 4 area utama:

1. Header ringkas berisi judul aplikasi dan informasi last sync.
2. Summary bar yang menampilkan total service dan komposisi status.
3. Grid kartu service yang berisi detail setiap service.
4. Error banner yang muncul saat backend tidak bisa dijangkau.

---

## 4. Layout Utama

### Desktop

- Header berada di atas dengan judul besar dan indikator sync terakhir.
- Summary bar tampil satu baris penuh di bawah header.
- Service card memakai grid 3 kolom pada layar lebar.
- Error banner tampil sebagai panel lebar di atas grid.

### Tablet

- Service card memakai 2 kolom.
- Ringkasan tetap berada di atas daftar service.
- Tombol re-check tetap terlihat tanpa perlu scroll horizontal.

### Mobile

- Seluruh konten mengalir satu kolom.
- Informasi penting ditempatkan paling atas: status, nama service, latency, dan aksi.
- Elemen dekoratif dikurangi agar fokus tetap ke status.

---

## 5. Komponen UI

### 5.1 Header

Isi header:
- Nama aplikasi: CMCC.
- Subjudul singkat: Monitoring Command Center.
- Informasi last sync atau status polling.

Perilaku:
- Last sync berubah otomatis saat polling berhasil.
- Jika backend gagal dijangkau, header tetap menampilkan data terakhir yang valid.

### 5.2 Summary Bar

Menampilkan 4 metrik:
- Total services.
- Jumlah `UP`.
- Jumlah `DOWN`.
- Jumlah `UNKNOWN`.

Setiap metrik memakai chip berwarna agar mudah dipindai.

### 5.3 Service Card

Setiap kartu service berisi:
- Status badge.
- Nama service.
- Kategori.
- Latency dalam ms.
- Timestamp last checked.
- Tombol `Re-check`.
- Inline error message jika re-check gagal.

Aturan tampilan:
- Status badge selalu menampilkan teks, bukan hanya dot warna.
- Service dengan status `DOWN` memakai animasi blink/pulse 1 detik.
- Saat re-check berjalan, tombol disabled dan menampilkan spinner.

### 5.4 Status Badge

Mapping visual:
- `UP` memakai hijau solid.
- `DOWN` memakai merah solid dengan animasi blink.
- `UNKNOWN` memakai abu-abu.

Badge harus cukup besar untuk dibaca cepat dan tetap kontras di mode terang.

### 5.5 Error Banner

Banner muncul ketika polling backend gagal beberapa kali berturut-turut.

Isi banner:
- Pesan utama bahwa backend tidak dapat dijangkau.
- Info retry otomatis.
- Penegasan bahwa data yang terlihat adalah stale data.

Perilaku:
- Banner tidak menghapus data lama.
- Saat retry sukses, banner hilang otomatis.

---

## 6. State UI

### Loading

- Tampilan skeleton atau placeholder boleh dipakai saat initial load.
- Grid card tidak boleh terasa kosong total.

### Success

- Data service tampil lengkap.
- Polling memperbarui status tanpa refresh browser.

### Backend Unreachable

- Tampilkan error banner.
- Pertahankan data terakhir yang diketahui.
- Lanjutkan retry otomatis sesuai interval yang ditetapkan.

### Force Re-check Error

- Hanya kartu yang gagal yang menampilkan error inline.
- Kartu lain tetap normal.

---

## 7. Interaksi Utama

### Auto Refresh

- Dashboard melakukan polling berkala setiap 10 detik.
- Perubahan status harus terasa live tanpa transisi yang mengganggu.

### Force Re-check

- User klik tombol `Re-check` pada kartu service.
- Tombol langsung disabled untuk mencegah double submit.
- Setelah selesai, kartu diperbarui dengan status dan latency terbaru.
- Jika gagal, tampilkan pesan error hanya di kartu tersebut.

---

## 8. Visual Style

### Tone

- Modern, tegas, dan operasional.
- Tidak terlalu dekoratif.
- Harus terasa seperti panel kerja untuk kondisi produksi.

### Warna

- Hijau untuk `UP`.
- Merah untuk `DOWN`.
- Abu-abu untuk `UNKNOWN`.
- Latar belakang dan panel dibuat netral agar status lebih menonjol.

### Tipografi

- Judul tegas dan mudah dipindai.
- Angka latency dan status perlu dibuat menonjol.

### Motion

- Hanya gunakan animasi yang punya fungsi jelas.
- Blink/pulse dipakai untuk `DOWN`.
- Spinner dipakai saat re-check aktif.

---

## 9. Responsive Rules

| Device | Width | Layout |
|--------|-------|--------|
| Mobile | >= 375px | 1 kolom |
| Tablet | >= 768px | 2 kolom |
| Desktop | >= 1280px | 3 kolom |

Grid service card mengikuti breakpoint Tailwind:

```html
<div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
```

---

## 10. Aksesibilitas

- Semua status harus punya label teks.
- Kontras warna harus memenuhi WCAG AA.
- Tombol re-check harus bisa diakses via keyboard.
- Informasi penting jangan hanya disampaikan lewat warna.

---

## 11. Konten yang Tidak Ditampilkan

- Tidak perlu login screen.
- Tidak perlu navigasi antar halaman.
- Tidak perlu grafik histori atau tren.
- Tidak perlu tabel administratif yang padat.

---

## 12. Output yang Diinginkan

UI final harus terasa seperti satu dashboard operasional yang fokus pada deteksi masalah dan tindakan cepat. Pengguna harus bisa melihat service mana yang sehat, mana yang bermasalah, dan apakah backend monitoring sedang tidak stabil tanpa harus membuka halaman lain.
