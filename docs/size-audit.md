# Arm64 release APK — size audit

Target: **kecil, ~30 MB** untuk APK arm64 release. Environment agent **tidak punya JDK / Android SDK**, jadi ukuran final belum bisa diukur di sini. Audit ini memetakan penyumbang ukuran dan langkah yang sudah/perlu dilakukan.

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
