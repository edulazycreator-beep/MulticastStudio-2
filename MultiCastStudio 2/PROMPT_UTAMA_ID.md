# PROMPT UTAMA — Aplikasi Android Live Multi-Camera seperti OBS

Salin seluruh prompt di bawah ini ke Claude Code, Gemini CLI, atau coding agent yang dapat mengedit repository dan menjalankan build.

---

## PERAN DAN TUJUAN

Kamu bertindak sebagai Senior Android Engineer, Real-time Video Engineer, dan UI/UX Designer. Bangun aplikasi Android bernama **MultiCast Studio**, yaitu aplikasi live production multi-camera yang terinspirasi workflow OBS Studio, tetapi dirancang khusus untuk Android dan perangkat mobile.

Hasil akhir yang wajib dibuat adalah project Android Studio yang benar-benar berisi source code, dapat dibuild, diuji, dan menghasilkan APK. Jangan hanya membuat mockup, pseudocode, atau UI statis.

**Bahasa utama: Kotlin** (gunakan Java hanya bila ada alasan teknis yang kuat). Gunakan Android SDK native, CameraX untuk use case kamera umum, dan Camera2 untuk pemeriksaan serta kontrol kamera tingkat lanjut. Arsitektur harus modular, ringan, dan mudah dikembangkan.

## 1. TARGET DAN BATASAN PLATFORM

- Platform: Android phone dan tablet.
- Minimum Android: API 26, tetapi fitur concurrent camera hanya diaktifkan bila API dan perangkat mendukung.
- Gunakan versi stabil terbaru dari Android Gradle Plugin, Kotlin, dan AndroidX yang kompatibel satu sama lain. Catat versi yang dipakai.
- Pertahankan kompatibilitas perangkat dengan fallback yang jelas.
- Jangan menganggap semua HP bisa menjalankan dua atau lebih kamera secara bersamaan.
- Pada Android 11/API 30 ke atas, deteksi kombinasi kamera menggunakan CameraManager.getConcurrentCameraIds() dan validasi konfigurasi sesi menggunakan Camera2 API yang sesuai. Untuk device yang tidak mendukung concurrent session, sediakan mode satu kamera aktif dengan perpindahan source.
- Pertimbangkan kamera internal, kamera USB/UVC, HDMI capture UVC, IP camera/RTSP, dan NDI sebagai jenis source yang berbeda. Jangan mengklaim source bekerja sebelum implementasi dan pengujian perangkat dilakukan. Source USB/HDMI memerlukan dukungan hardware, USB host, driver/protokol, serta validasi masing-masing perangkat.

## 2. DESAIN UI/UX — MODERN GLASSMORPHISM

Buat tampilan profesional bergaya control room broadcast, bukan tampilan aplikasi kamera sederhana.

Arah visual:
- Dark glassmorphism sebagai tema utama.
- Background gelap navy/charcoal dengan aksen cyan dan violet yang hemat.
- Panel semi-transparan, border tipis, blur atau simulasi blur yang ringan, corner radius konsisten, serta bayangan lembut.
- Kontras teks harus tinggi dan mudah dibaca di luar ruangan.
- Animasi pendek dan halus; hindari efek berat yang mengganggu preview.
- Layout responsif untuk portrait dan landscape; landscape menjadi workspace produksi utama.
- Sediakan mode compact untuk layar kecil.

Halaman utama harus menampilkan:
1. Header brand dan status koneksi.
2. Area Preview dan Program yang jelas.
3. Multiview grid untuk melihat source yang sedang aktif.
4. Daftar camera/source yang bisa dipilih.
5. Scene selector.
6. Tombol CUT dan AUTO dengan state/transisi yang terlihat.
7. Audio mixer dengan meter level yang benar-benar berasal dari audio pipeline.
8. Tombol Start/Stop Streaming dan status bitrate/FPS/dropped frames.
9. Panel quick settings untuk resolusi, frame rate, audio, dan orientasi.

Jangan menampilkan meter, FPS, bitrate, live status, atau data statistik palsu. Bila engine belum aktif, tampilkan status “Not connected”, “Unavailable”, atau “Not implemented”.

## 3. CAMERA SOURCE ENGINE

Implementasikan source abstraction agar setiap sumber memiliki interface yang konsisten, misalnya CameraSource, UsbCaptureSource, RtspSource, dan NdiSource (modul yang belum tersedia harus ditandai sebagai planned, bukan dibuat seolah-olah bekerja).

Prioritas implementasi:
- Kamera belakang dan depan dengan preview yang stabil.
- Deteksi daftar camera ID dan karakteristik kamera.
- Deteksi dukungan concurrent camera dan pasangan kamera yang valid.
- Dual-camera preview bila device benar-benar mendukung konfigurasi tersebut.
- Fallback ke single-camera switching bila concurrent camera tidak tersedia.
- Pengelolaan lifecycle: buka/tutup kamera saat activity/service berubah, tangani permission denial, camera in use, disconnect, dan perubahan orientasi.
- Sediakan resolusi adaptif, target awal 720p/30 untuk perangkat kelas menengah dan pilihan 1080p/30 atau lebih tinggi hanya jika didukung.
- Jangan membuka banyak stream beresolusi tinggi tanpa menghitung bandwidth ISP, GPU, memori, dan kemampuan encoder.

Untuk dual-camera, gunakan API resmi CameraX/Camera2 yang sesuai dengan versi dependency yang dipilih. Uji pasangan kamera yang dilaporkan perangkat. Jangan sekadar membuka dua CameraX Preview tanpa memverifikasi dukungan session.

## 4. SCENE COMPOSITOR / VIDEO PIPELINE

Rancang pipeline frame dengan pemrosesan GPU yang efisien:
- Gunakan Surface/SurfaceTexture dan OpenGL ES/EGL untuk compositing bila diperlukan.
- Hindari konversi frame berulang ke Bitmap.
- Hindari alokasi objek di jalur frame per frame.
- Implementasikan sinkronisasi timestamp antar source.
- Dukung layout dasar: Fullscreen, Picture-in-Picture, Side-by-Side, 2x2 grid, dan custom crop/position.
- Pisahkan Preview output dan Program output.
- CUT harus mengganti source secara instan; AUTO menjalankan transisi yang dipilih.
- Tambahkan overlay teks/logo dengan jalur rendering yang tidak memblokir camera thread.
- Tangani aspect ratio, rotasi sensor, crop, color space, dan frame pacing.

## 5. AUDIO MIXER

Rancang audio pipeline terpisah dari video:
- Pilihan input mikrofon internal, USB audio interface, atau audio capture yang didukung Android.
- Level meter peak/RMS yang berasal dari sampel audio aktual.
- Gain, mute, monitoring, dan master output.
- Audio focus, routing, sample rate, channel count, dan permission harus ditangani.
- Sinkronkan timestamp audio dan video.
- Jangan menganggap semua USB audio interface kompatibel tanpa pengujian.

## 6. LIVE STREAMING

Target streaming:
- RTMP dan RTMPS ke server kustom.
- URL server dan stream key disimpan dengan aman; jangan menulis stream key ke log.
- Video encoding menggunakan MediaCodec hardware encoder bila tersedia.
- H.264 wajib sebagai baseline; HEVC opsional jika server dan perangkat mendukung.
- Audio AAC.
- Pilihan 720p/1080p, 24/25/30/50/60 fps sesuai kemampuan perangkat.
- Bitrate control, reconnect strategy, network monitoring, dan status dropped frames.
- Gunakan Foreground Service untuk sesi streaming yang berjalan saat app tidak berada di foreground, dengan notification channel dan permission sesuai versi Android.
- Pastikan lifecycle dan pembatalan service aman.
- Jangan mengimplementasikan RTMP dengan menyalin kode/library tanpa memeriksa lisensi dan kompatibilitasnya. Jelaskan dependency, lisensi, batasan TLS/RTMPS, dan alasan pemilihannya.

## 7. ARSITEKTUR DAN KUALITAS KODE

Gunakan pemisahan modul/layer, contoh:
- app: entry point, navigation, dependency wiring.
- core-camera: camera discovery, CameraX/Camera2 adapters.
- core-video: frame pipeline, compositor, scene model.
- core-audio: capture, mixer, metering.
- core-stream: encoder, muxer, RTMP/RTMPS transport.
- feature-studio: production workspace UI.
- feature-settings: device and stream settings.

Gunakan MVVM, Kotlin Coroutines/Flow, lifecycle-aware collection, repository/interface abstraction, dan dependency injection hanya bila memberi manfaat nyata. Hindari overengineering.

Aturan performa:
- Jangan melakukan pekerjaan berat di Main Thread.
- Batasi jumlah thread dan ukuran buffer.
- Lepaskan Surface, Camera, AudioRecord, MediaCodec, EGL, dan socket saat sesi berhenti.
- Pantau memory pressure dan thermal throttling.
- Gunakan resolusi adaptif saat perangkat panas atau frame drop meningkat.
- Sediakan logging terstruktur tanpa data rahasia.
- Pastikan UI tetap responsif saat kamera/streaming gagal.

## 8. SECURITY DAN PERMISSIONS

- Minta CAMERA, RECORD_AUDIO, dan permission lain hanya ketika fitur terkait digunakan.
- Tangani Android runtime permission dan perubahan kebijakan versi Android.
- Simpan stream key dengan Android Keystore/encrypted storage; jangan hardcode.
- Jangan kirim data kamera atau audio ke layanan eksternal selain tujuan streaming yang dipilih pengguna.
- Tambahkan indikator kamera/mikrofon aktif yang jelas.

## 9. TESTING DAN VALIDASI

Wajib sediakan:
- Unit test untuk scene switching, source state, bitrate/settings validation, dan reconnect logic.
- Instrumentation test dasar untuk permission dan lifecycle.
- Test manual checklist pada beberapa perangkat.
- Catat model perangkat, versi Android, daftar concurrent camera IDs, resolusi, FPS, suhu, dan hasil uji.
- Uji camera unavailable, permission ditolak, app masuk background, rotasi layar, jaringan putus, server RTMP gagal, dan sesi dihentikan mendadak.
- Bedakan fitur yang sudah implemented, partially implemented, planned, dan tested.

## 10. BUILD, CI, DAN HASIL AKHIR

Project harus menyertakan:
- Source code lengkap dan struktur folder rapi.
- README.md berisi persyaratan, cara membuka project, cara build APK, cara install, dan keterbatasan.
- Gradle wrapper.
- ` .github/workflows/android.yml ` untuk build otomatis pada push, pull request, dan manual dispatch.
- Artifact APK debug dari GitHub Actions.
- ProGuard/R8 rules yang diperlukan.
- `docs/DEVICE_COMPATIBILITY.md` untuk matriks dukungan fitur.
- `docs/ARCHITECTURE.md` untuk diagram alur data.
- ZIP project yang siap diunduh.

Perintah validasi minimum:
- `./gradlew clean assembleDebug`
- `./gradlew test`
- jalankan lint bila konfigurasi project mendukungnya.

Jika ada build/test yang gagal, perbaiki lalu jalankan ulang. Jangan menyatakan build berhasil tanpa melihat output build sukses. Jangan menyatakan dua kamera, audio mixer, atau RTMP bekerja tanpa pengujian aktual pada hardware/server.

## 11. CARA BEKERJA

1. Audit repository yang tersedia sebelum mengubah file.
2. Buat implementation plan singkat.
3. Bangun vertical slice yang benar-benar berfungsi terlebih dahulu: permission → camera preview → source switching → capability discovery.
4. Tambahkan concurrent camera hanya setelah validasi device.
5. Lanjutkan scene compositor, audio pipeline, encoder, dan RTMP secara bertahap.
6. Build dan tes setiap tahap.
7. Perbarui README dan status fitur setiap kali ada perubahan.
8. Jangan meninggalkan tombol yang tampak aktif tetapi tidak melakukan apa-apa. Gunakan disabled state atau keterangan jelas untuk fitur yang belum ada.
9. Jangan mengganti kode nyata dengan mock data untuk mengejar tampilan.
10. Di akhir, berikan ringkasan file yang dibuat, fitur yang benar-benar bekerja, fitur yang belum selesai, bukti build/test, serta tautan artifact APK/ZIP.

**Mulai dengan memeriksa repository, lalu implementasikan aplikasi secara bertahap. Prioritaskan aplikasi yang ringan, stabil, dapat dibuild, dan jujur mengenai dukungan hardware.**
