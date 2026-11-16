# FFmpeg Media Encoder

<p align="center">
  <img src="logo.png" alt="FFmpeg Media Encoder Logo" width="160" height="160" style="border-radius: 24px;" />
</p>

<p align="center">
  <strong>A powerful, native Android media transcoder and converter powered by FFmpegKit and Jetpack Compose.</strong><br>
  <em>Aplikasi konverter dan transkoder media Android bertenaga FFmpegKit dan Jetpack Compose.</em>
</p>

<p align="center">
  <a href="https://github.com/rise-crystal/FFmpegMediaEncoder/releases/latest"><img src="https://img.shields.io/badge/Download-APK-7C3AED?style=for-the-badge&logo=android&logoColor=white" alt="Download APK" /></a>
  <a href="https://github.com/rise-crystal/FFmpegMediaEncoder/releases"><img src="https://img.shields.io/github/v/release/rise-crystal/FFmpegMediaEncoder?style=for-the-badge&color=10B981&label=Latest%20Release" alt="Latest Release" /></a>
  <a href="https://developer.android.com/about/versions/oreo"><img src="https://img.shields.io/badge/Platform-Android%208.0%2B-2DD4BF?style=for-the-badge&logo=android&logoColor=white" alt="Platform: Android 8.0+" /></a>
  <a href="https://kotlinlang.org/docs/whatsnew19.html"><img src="https://img.shields.io/badge/Kotlin-1.9.0-8B5CF6?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin 1.9.0" /></a>
  <a href="https://github.com/arthenica/ffmpeg-kit"><img src="https://img.shields.io/badge/FFmpegKit-v6.0-FF5722?style=for-the-badge&logo=ffmpeg&logoColor=white" alt="FFmpegKit v6.0" /></a>
  <a href="https://developer.android.com/jetpack/compose"><img src="https://img.shields.io/badge/Compose-Material%203-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose Material 3" /></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-F59E0B?style=for-the-badge" alt="License: MIT" /></a>
</p>

---

<p align="center">
  <a href="#bahasa-indonesia"><strong>Bahasa Indonesia</strong></a> &nbsp;•&nbsp; 
  <a href="#english"><strong>English</strong></a>
</p>

---

<a name="bahasa-indonesia"></a>
## 🇮🇩 Bahasa Indonesia

### 📌 Tentang Proyek
**FFmpeg Media Encoder** adalah aplikasi Android open-source modern untuk melakukan konversi, kompresi, dan transkoding file video serta audio secara lokal langsung di perangkat pengguna tanpa memerlukan koneksi internet maupun server pihak ketiga. Dibangun menggunakan teknologi antarmuka terbaru [Jetpack Compose (Material 3)](https://developer.android.com/jetpack/compose) dengan tema OLED-friendly serta ditenagai oleh [FFmpegKit Full 6.0](https://github.com/arthenica/ffmpeg-kit), aplikasi ini mendukung berbagai macam codec dan format kontainer media populer.

---

### 🚀 Fitur Utama
- **⚡ Bertenaga [FFmpegKit Full](https://github.com/arthenica/ffmpeg-kit)**: Mendukung beragam format video (`MP4`, `MKV`, `MOV`, `AVI`, `WEBM`, `TS`, `3GP`) dan audio (`MP3`, `AAC`, `WAV`, `FLAC`, `OGG`, `M4A`).
- **📦 Antrean File (Batch Queue)**: Pilih banyak file sekaligus untuk dikonversi secara berurutan dalam satu kali klik.
- **🛠️ Preset Cepat & Fleksibel**:
  - **Original Quality**: Salin stream audio/video (`copy`) dengan sangat cepat tanpa penurunan kualitas.
  - **Compress Video**: Kompresi video menggunakan encoder H.264 (`libx264` CRF 28) untuk menghemat ruang penyimpanan.
  - **720p**: Ubah skala resolusi video menjadi 720p HD dengan rasio aspek tetap terjaga.
  - **Video to MP3**: Ekstrak audio dari video menjadi format MP3 berkualitas tinggi secara otomatis.
- **🔄 Layanan Latar Belakang ([Foreground Service](https://developer.android.com/develop/background-work/services/foreground-services))**: Konversi tetap berjalan lancar di latar belakang meskipun layar mati atau Anda sedang membuka aplikasi lain, lengkap dengan notifikasi progress interaktif dan tombol pembatalan.
- **🛡️ Manajemen Cache Cerdas**: Mencegah kebocoran memori dengan pembersihan otomatis file sementara (`cacheDir`) melalui blok *safe-cleanup*.
- **📊 Terminal Log Interaktif**: Pantau log baris perintah FFmpeg secara *real-time* dengan penampil terminal bertema Hacker/Linux, dilengkapi fitur salin log ke clipboard.
- **📜 Riwayat Konversi ([Room Database](https://developer.android.com/training/data-storage/room))**: Semua riwayat hasil konversi disimpan secara lokal dengan Room DB, lengkap dengan status, durasi, dan perintah FFmpeg yang digunakan.
- **🎨 Antarmuka Modern**: Desain elegan berbasis Jetpack Compose dengan palet warna OLED Zinc Dark yang hemat baterai.

---

### 📥 Unduh & Pasang (Instalasi)

#### Langkah-langkah Instalasi:
1. **Unduh File APK**:
   Klik salah satu tombol di bawah ini untuk mengunduh versi rilis terbaru:
   <p align="left">
     <a href="https://github.com/rise-crystal/FFmpegMediaEncoder/releases/download/v1.0.1/FFmpegMediaEncoder-v1.0.1.apk"><img src="https://img.shields.io/badge/Unduh%20Langsung-APK%20(v1.0.1)-7C3AED?style=for-the-badge&logo=android&logoColor=white" alt="Unduh Langsung APK v1.0.1" /></a>
     <a href="https://github.com/rise-crystal/FFmpegMediaEncoder/releases/latest"><img src="https://img.shields.io/badge/Halaman%20Rilis-GitHub-10B981?style=for-the-badge&logo=github&logoColor=white" alt="Halaman Rilis GitHub" /></a>
   </p>
2. **Izinkan Instalasi dari Sumber Tidak Dikenal**:
   Jika ini pertama kalinya Anda memasang file APK di luar Google Play Store:
   - Buka **Pengaturan (Settings)** > **Keamanan & Privasi (Security & Privacy)**.
   - Aktifkan opsi **Pasang Aplikasi dari Sumber Tidak Dikenal (Install Unknown Apps)** untuk browser atau file manager yang Anda gunakan.
3. **Pasang Aplikasi**:
   Buka file `.apk` yang telah diunduh, lalu ketuk **Instal**.
4. **Buka Aplikasi**:
   Setelah selesai, buka **FFmpeg Media Encoder** dan berikan izin penyimpanan / notifikasi yang diminta agar aplikasi dapat memproses file dan menampilkan status di latar belakang.

---

### 📖 Panduan Penggunaan
1. **Menambah File**: Ketuk tombol **+ Tambah File** pada tab *Converter* dan pilih satu atau beberapa file video/audio dari galeri atau file manager Anda.
2. **Pilih Pengaturan**:
   - Tentukan **Format Output** yang diinginkan (misal: `mp4`, `mkv`, `mp3`, `aac`).
   - Pilih **Preset** konversi yang sesuai.
   - Tentukan **Folder Output** tempat hasil konversi akan disimpan.
   - *(Opsional)* Centang opsi *Hapus file asli setelah konversi* jika ingin menghemat ruang secara otomatis.
3. **Mulai Konversi**: Ketuk tombol **Mulai Konversi**. Aplikasi akan otomatis membuka tab *Terminal Log*.
4. **Pantau & Batalkan**: Anda dapat memantau proses konversi dari layar log atau langsung dari bilah notifikasi HP. Ketuk tombol **Batalkan** kapan saja jika ingin menghentikan proses.
5. **Cek Riwayat**: Masuk ke tab *History* untuk melihat daftar file yang sudah selesai diproses beserta rinciannya.

---

### 🛠️ Arsitektur & Teknologi
- **Bahasa**: [Kotlin 1.9.0](https://kotlinlang.org/docs/whatsnew19.html)
- **Framework UI**: [Jetpack Compose (Material 3)](https://developer.android.com/jetpack/compose)
- **Mesin Media**: [FFmpegKit Full 6.0](https://github.com/arthenica/ffmpeg-kit) (`com.arthenica:ffmpeg-kit-full:6.0-2`)
- **Arsitektur**: MVVM + Singleton Engine (`ConversionManager`) + [Android Foreground Service](https://developer.android.com/develop/background-work/services/foreground-services)
- **Konkurensi**: [Kotlin Coroutines & Flow](https://kotlinlang.org/docs/coroutines-overview.html) (`StateFlow`, `Dispatchers.IO`)
- **Database**: [Android Jetpack Room](https://developer.android.com/training/data-storage/room) via KSP
- **Penyimpanan**: [Storage Access Framework (SAF)](https://developer.android.com/guide/topics/providers/document-provider) + DocumentFile + Seekable Cache Bridge
- **Target SDK**: [Android 14 (API 34)](https://developer.android.com/about/versions/14) | **Min SDK**: [Android 8.0 (API 26)](https://developer.android.com/about/versions/oreo)

---
---

<a name="english"></a>
## 🇬🇧 English

### 📌 About The Project
**FFmpeg Media Encoder** is a modern, open-source Android application designed for local video and audio conversion, compression, and transcoding directly on your device without relying on internet connectivity or external servers. Powered by [FFmpegKit Full 6.0](https://github.com/arthenica/ffmpeg-kit) and crafted with [Jetpack Compose (Material 3)](https://developer.android.com/jetpack/compose) in an OLED-friendly dark palette, it delivers high performance and extensive codec compatibility.

---

### 🚀 Key Features
- **⚡ Powered by [FFmpegKit Full](https://github.com/arthenica/ffmpeg-kit)**: Comprehensive support for diverse video containers (`MP4`, `MKV`, `MOV`, `AVI`, `WEBM`, `TS`, `3GP`) and audio formats (`MP3`, `AAC`, `WAV`, `FLAC`, `OGG`, `M4A`).
- **📦 Batch Processing Queue**: Select multiple media files and process them sequentially with a single tap.
- **🛠️ Fast & Versatile Presets**:
  - **Original Quality**: Ultra-fast stream-copy (`copy`) preserving original video/audio quality.
  - **Compress Video**: Compresses video using H.264 (`libx264` CRF 28) to save disk space.
  - **720p**: Downscales video to 720p HD while maintaining aspect ratio.
  - **Video to MP3**: Seamlessly extracts audio tracks into high-quality MP3 files.
- **🔄 Robust [Foreground Service](https://developer.android.com/develop/background-work/services/foreground-services)**: Conversions run reliably in the background even if the screen turns off or another app is opened, complete with a live progress notification and cancel action.
- **🛡️ Memory & Cache Safe**: Zero temporary storage leaks thanks to strictly managed `try-finally` cache cleanup and orphan cache purges on startup.
- **📊 Real-time Terminal Log Viewer**: Watch FFmpeg output line-by-line in a sleek hacker-style monospace terminal viewer with one-tap clipboard copy.
- **📜 Conversion History ([Room Database](https://developer.android.com/training/data-storage/room))**: Keeps local records of completed conversions including status, duration, and exact FFmpeg command strings.
- **🎨 Modern OLED UI**: Clean Material 3 design optimized for battery savings on OLED/AMOLED screens.

---

### 📥 Download & Installation

#### Installation Steps:
1. **Download the APK**:
   Click either button below to get the latest release:
   <p align="left">
     <a href="https://github.com/rise-crystal/FFmpegMediaEncoder/releases/download/v1.0.1/FFmpegMediaEncoder-v1.0.1.apk"><img src="https://img.shields.io/badge/Direct%20Download-APK%20(v1.0.1)-7C3AED?style=for-the-badge&logo=android&logoColor=white" alt="Direct Download APK v1.0.1" /></a>
     <a href="https://github.com/rise-crystal/FFmpegMediaEncoder/releases/latest"><img src="https://img.shields.io/badge/Releases%20Page-GitHub-10B981?style=for-the-badge&logo=github&logoColor=white" alt="GitHub Releases Page" /></a>
   </p>
2. **Allow Installation from Unknown Sources**:
   If installing an APK outside Google Play for the first time:
   - Navigate to **Settings** > **Security & Privacy**.
   - Enable **Install Unknown Apps** for your browser or file manager.
3. **Install the App**:
   Tap the downloaded `.apk` file and tap **Install**.
4. **Launch & Grant Permissions**:
   Open **FFmpeg Media Encoder** and allow notification / storage permissions when prompted to ensure proper background processing.

---

### 📖 How to Use
1. **Add Files**: Tap **+ Add File** on the *Converter* tab to pick one or more videos or audio tracks.
2. **Configure Settings**:
   - Choose your target **Output Format** (e.g., `mp4`, `mkv`, `mp3`, `aac`).
   - Select the desired **Preset**.
   - Select the **Output Folder** where converted files will be saved.
   - *(Optional)* Check *Delete original file after conversion* to free up storage automatically.
3. **Start Conversion**: Tap **Start Conversion**. The app will navigate to the *Terminal Log* tab.
4. **Monitor or Cancel**: View real-time progress in the terminal log or via the persistent notification. You can cancel at any time with the **Cancel** button.
5. **View History**: Switch to the *History* tab to inspect all past conversion records and details.

---

### 🛠️ Tech Stack & Architecture
- **Language**: [Kotlin 1.9.0](https://kotlinlang.org/docs/whatsnew19.html)
- **UI Framework**: [Jetpack Compose (Material 3)](https://developer.android.com/jetpack/compose)
- **Engine**: [FFmpegKit Full 6.0](https://github.com/arthenica/ffmpeg-kit) (`com.arthenica:ffmpeg-kit-full:6.0-2`)
- **Architecture**: MVVM + Singleton Engine (`ConversionManager`) + [Android Foreground Service](https://developer.android.com/develop/background-work/services/foreground-services)
- **Concurrency**: [Kotlin Coroutines & Flow](https://kotlinlang.org/docs/coroutines-overview.html) (`StateFlow`, `Dispatchers.IO`)
- **Database**: [Android Jetpack Room](https://developer.android.com/training/data-storage/room) via KSP
- **Storage**: [Storage Access Framework (SAF)](https://developer.android.com/guide/topics/providers/document-provider) + DocumentFile + Seekable Cache Bridge
- **Target SDK**: [Android 14 (API 34)](https://developer.android.com/about/versions/14) | **Min SDK**: [Android 8.0 (API 26)](https://developer.android.com/about/versions/oreo)

---

### 📄 License
This project is open-source and licensed under the terms of the [MIT License](LICENSE).
