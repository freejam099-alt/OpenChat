# Arm64 release APK — size audit

Target: **31–35 MB** untuk APK arm64 release. **Hasil awal: 49 MB → hasil akhir: 28 MB** (signed arm64, CI sukses). Target terlampaui (lebih kecil).

## Hasil ukur (CI, 2026-11-27)

`app/build/outputs/apk/stable/release/LastChat_1.4.8_stable-arm64-v8a-release.apk` = **49 MB**.

Komposisi (unzip -l, ukuran sebelum kompresi ZIP internal):

| Bagian | Ukuran | Catatan |
|---|---:|---|
| `lib/` | 16,01 MB | `libmupdf_java.so` 9,44 MB; `libbarhopper_v3.so` 4,95 MB; `libquickjs-android-wrapper.so` 1,33 MB; proot/termux ~1 MB |
| `assets/` | 13,03 MB | terutama bundle React SPA `web-ui/build/client` |
| `classes.dex` + `classes2.dex` | 18,68 MB | Firebase, ML Kit, Ktor, Coil, Pebble, MCP, Room, Compose |
| `res/` + `resources.arsc` | 10,03 MB | font 3,9 MB, placeholder PNG 2,2 MB, 7 locale |

## Pemotongan yang sudah diterapkan (menghasilkan 28 MB)
- `:document` + MuPDF dihapus (PDF/DOCX sebagai prompt tidak didukung).
- ML Kit barcode + CameraX + Quickie dihapus (QR scanner tidak ada).
- Bundle `web-ui` tidak lagi dimasukkan ke APK (server Ktor/API tetap ada).
- Firebase Crashlytics + RemoteConfig dihapus.
- QuickJS native + JS tool dihapus; highlighter menjadi plain text.
- Tombol "+"/picker dibungkus guard agar tidak force-close, navbar diberi padding 104 dp.
- Glass polish: input bar (Kyant0 cylinder), FAB, search, send, chip saran, kartu apung.

## Kandidat pemotongan lanjutan (bila diperlukan)

| Prioritas | Potong | Estimasi hemat | Dampak |
|---|---|---:|---|
| 1 | Bundle `web-ui` dari APK (tetap simpan server Ktor/API) | ~3–5 MB | Akses browser/SPA hilang |
| 2 | Modul `:document` + MuPDF | ~4,4 MB | PDF/DOCX sebagai prompt tidak lagi didukung |
| 3 | ML Kit barcode + CameraX + quickie | ~3–4 MB | Scanner QR/barcode hilang |
| 4 | Firebase Crashlytics + RemoteConfig | ~1–2 MB | Telemetri & policy remote hilang |
| 5 | QuickJS (`highlight` + JS tool) | ~0,7 MB | Highlight & tool JS hilang |
| 6 | Font kustom → font sistem / subset; placeholder PNG → WebP | ~5 MB | Perubahan visual minor |

Kombinasi 1+2+3+6 ≈ 15–18 MB → kisaran **31–34 MB**. Tambah R8 full-mode / pembatasan locale bila perlu menyentuh ~30 MB.

## Sudah dilakukan (mengecilkan APK)

| Perubahan | Efek |
|---|---|
| Hapus `:local-llm` (LiteRT + localagents + protobuf + AAR/catalog) | Hilang semua native library inferensi on-device |
| Hapus stack sherpa lokal di `:speech` (7 file, controller, aset, AAR unduhan) | Hilang native library STT lokal; ASR cloud tetap ada |
| `jniLibs.useLegacyPackaging = false` | Native library **dikompresi** di APK; di minSdk 28+ Android langsung memetakan dari APK → hemat beberapa MB dibanding packaging lama (uncompressed) |
| ABI split (`arm64-v8a` saja saat build arm64) | Tidak menyertakan `armeabi-v7a`/`x86_64` |
| R8 + resource shrinking aktif di release (default `lastchat.release.minify=true`) | Dex & resource dipangkas |

## Penyumbang ukuran tersisa (urutanPrioritas)

| # | Item | Perkiraan (arm64) | Tindakan bila target belum tercapai |
|---|---|---:|---|
| 1 | `document/.../arm64-v8a/libmupdf_java.so` | 9,4 MB mentah (~4–5 MB terkompresi) | Jika fitur PDF viewer tidak wajib: hapus modul `:document`, `DocumentAsPromptTransformer`, dan pemanggil `PdfParser`. Penghematan terbesar. |
| 2 | `web-ui/build/client` (React SPA, di-build saat `preBuild`) | ~2–5 MB | Nonaktifkan `buildWebUi` + hapus `assets.srcDir("../web-ui/build/client")` bila akses web tidak diperlukan (web server Ktor bisa tetap untuk API). |
| 3 | `app/src/main/res/font/google_sans_flex.ttf` | 3,9 MB | Subset font ke glyph yang dipakai (Latin + locale aktif) atau pakai font sistem. Berisiko bagi i18n — lakukan terakhir. |
| 4 | `placeholder.png` + `placeholder_dark.png` | 2,3 MB | Konversi ke WebP (biasanya ~5–8× lebih kecil), nama resource tidak berubah. |
| 5 | Firebase Crashlytics + RemoteConfig, ML Kit barcode, Ktor server, Pebble, Coil3 | beberapa MB dex | Cabut fitur yang tidak dipakai; Crashlytics/RemoteConfig sudah bebas Analytics. |
| 6 | 7 locale + `assets/emoji/...json` (772 KB) | <1 MB | Hapus locale yang tidak dipakai hanya jika diizinkan (AGENTS.md melarang penambahan, bukan penghapusan). |

## Cara mengukur di mesin dengan JDK + Android SDK

Pastikan `local.properties` berisi keystore (file ini **sengaja tidak di-push**):

```properties
storeFile=/path/ke/Sandi6.jks
storePassword=Sandi6
keyAlias=sandi6
keyPassword=Sandi6
```

Lalu:

```bash
./gradlew buildArm64Release          # alias -> assembleStableArm64Release
ls -lh app/build/outputs/apk/stable/arm64/release/*.apk
```

Atau langsung:

```bash
./gradlew :app:assembleStableArm64Release
```

> `storePassword`/`keyPassword` = nama keystore (`Sandi6`), `keyAlias` = `sandi6` (dibaca dari isi JKS).

## Keputusan lanjutan

Jika APK masih > ~30 MB setelah diukur, potong berurutan: **(1) `:document`/MuPDF → (2) `web-ui` assets → (3) font subset → (4) placeholder WebP**. Jangan memotong Room/migrasi, Koin, atau pipeline AI cloud karena akan merusak fungsi inti.
