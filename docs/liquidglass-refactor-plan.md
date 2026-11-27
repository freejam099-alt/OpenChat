# LastChat — Planning, Audit & Revisi: Liquid Glass + Penghapusan AI Lokal

> Status: **PLANNING → AUDIT → REVISI 1 → REVISI 2 → IMPLEMENTASI**
> Tanggal: 2026-11-27
> Keputusan user: (1) hapus penuh fitur AI lokal dari build arm64, (2) floating navbar global 4 item, (3) planning/audit/revisi dulu baru implementasi.

---

## 1. Planning

### 1.1 Tujuan

| ID | Tujuan | Dampak |
|---|---|---|
| G1 | Hapus semua menu "Local LLM" (teks **dan** suara/STT) yang terkait web server, serta seluruh fitur model AI lokal dari build `:app` | APK arm64 lebih kecil; tidak ada opsi lokal di UI/Android maupun web |
| G2 | Upgrade Compose Material 3 ke **1.5.0 stabil** (saat ini `1.5.0-alpha08`) | Expressive API final, perbaikan bug, kompatibilitas Liquid Glass |
| G3 | Integrasi **Liquid Glass** (`dev.shayxo.liquidglass` 1.0.0) sesuai [panduan resmi](https://github.com/shayann07/liquidglass) | Theme wrapper + glass Material 3 imports + `glassEffect` pada komponen kunci |
| G4 | Floating navbar gaya iOS **global 4 item**: `Chat`, `Provider`, `UI Customization`, `Lainnya` | Navigasi konsisten; layar Chat tetap menjadi "AI surface" yang bersih (tanpa chrome settings) |

### 1.2 Fakta dasar (terverifikasi dari kode)

- `gradle/libs.versions.toml:87` → `androidx-material3 = "1.5.0-alpha08"` (**belum** stabil 1.5.0; stabil rilis 18 Nov 2026).
- Tidak ada menu Local LLM/STT di `web-ui/`; satu-satunya jejak web ada di `WebDtos.kt:963` (`ProviderSetting.LiteRtLocal -> "litert_local"`).
- UI Android lokal: `ui/pages/setting/locallm/SettingLocalLlmPage.kt` + VM, `ui/pages/setting/localstt/SherpaModelCards.kt` + VM, rute `Screen.SettingLocalLlm` di `RouteActivity.kt:942,1406`.
- Modul lokal: `:local-llm` (LiteRT), `:speech` (ASR cloud **+** sherpa lokal). Modul `:speech` **tidak boleh dihapus seluruhnya** karena berisi controller ASR cloud.
- Liquid Glass: plugin `dev.shayxo.liquidglass` v1.0.0 / BOM `dev.shayxo.liquidglass:liquidglass-bom:1.0.0`; paket Material 3 glass = `com.wexpa.liquidglass.material3.*`; `LiquidGlassTheme` wajib di root; `GlassTabBar` untuk navbar gaya iOS; shader penuh Android 13+, fallback di bawahnya.
- Material3 1.5.0: `Slider`/`RangeSlider` stateless **deprecated** (masih kompil), `LocalMotionScheme` deprecated → `MaterialTheme.motionScheme`. ~20 pemanggilan `Slider` di `:app` perlu dicek saat kompilasi.

### 1.3 Tahapan implementasi

**Fase A — Hapus AI lokal (G1)**
1. Hapus modul `:local-llm` dari `settings.gradle.kts` + dependensi `:app`.
2. `:speech`: hapus hanya stack sherpa/lokal, pertahankan 6 controller ASR cloud + `ASRController`/`ASRProviderSetting`.
3. Hapus UI/VM/rute Local LLM & Local STT, hook STT lokal, state, preset, entri katalog, mapping `WebDtos`, service download, permission/manifest, string i18n orphan.
4. Jaga kompatibilitas deserialisasi Settings lama (lihat Risiko R1).

**Fase B — Dependency & Theme (G2+G3)**
5. `libs.versions.toml`: material3 `1.5.0`; tambah `liquidglass`, `liquidglass-material3`, BOM; daftarkan plugin di `settings.gradle.kts` + root `build.gradle.kts`.
6. `:app/build.gradle.kts`: terapkan plugin Liquid Glass (atau deps eksplisit BOM) — **tanpa** `liquidGlass { material3.set(false) }`.
7. `RouteActivity.setContent`: bungkus dengan `LiquidGlassTheme(background = ...)`, sediakan `fallbackSurface`, override `LocalHapticFeedback` → `PremiumHaptics` (R2).

**Fase C — Adopsi komponen glass (G3)**
8. Ubah import Material 3 → `com.wexpa.liquidglass.material3.*` pada komponen kunci: `Scaffold`, `NavigationBar`, `TopAppBar`, `Card`, `Button`, `IconButton`, `Chip`, `Switch`, `Surface`, `ModalBottomSheet`, `AlertDialog`, `Snackbar`, `SegmentedButton`.
9. Tambah `Modifier.glassEffect(..., shape)` + `GlassEffectContainer` pada permukaan apung (chat composer bar, bubble aksi, dialog, toolbar).
10. Verifikasi batasan renderer: glass menggambar di atas konten non-glass; alpha parent tidak memudarkan glass.

**Fase D — Floating navbar global (G4)**
11. Tambah shell navbar di `AppRoutes`: `Box { NavHost(...); GlassTabBar(itemCount=4, ...) }` di dalam `LiquidGlassTheme`.
12. Pemetaan tab: 0=`Screen.Chat` (via `navigateToChatPage`), 1=`Screen.SettingProvider`, 2=`Screen.SettingUICustomization`, 3=`Screen.Setting`.
13. Seleksi tab diturunkan dari `currentBackStackEntryAsState`; `onSelected` memakai `launchSingleTop` + `popUpTo` sesuai aturan navigasi repo.
14. Nonaktifkan bottom-bar lokal pada settings bila ada duplikasi; pastikan Chat tidak menampilkan chrome settings.

**Fase E — Verifikasi**
15. `git diff --stat`, pencarian sisa referensi lokal, dan (jika toolchain tersedia) `./gradlew :app:compileStableDebugKotlin`.

### 1.4 Risiko & mitigasi

| ID | Risiko | Mitigasi |
|---|---|---|
| R1 | Settings lama berisi `@SerialName("litert_local")` polymorphic; menghapus subtype bisa merusak decode | Gunakan decoder toleran per-elemen (`SettingsPreferencesDecode.kt`); bila belum cukup, pertahankan **tombstone data class tanpa provider/native** agar kompatibel, lalu hapus dari UI & build |
| R2 | `GlassTabBar` memakai `LocalHapticFeedback` (AGENTS.md melarang pemakaian langsung) | Sediakan `HapticFeedback` adapter yang meneruskan ke `PremiumHaptics` via `CompositionLocalProvider` |
| R3 | Material3 1.5.0 source-breaking `Slider`/`RangeSlider` | Perbaiki call site bila kompilasi gagal (stateful `rememberSliderState` + `onValueChange`) |
| R4 | Kombinasi Compose BOM 2025.11.00 + material3 1.5.0 + Liquid Glass 1.0.0 bisa konflik versi compose ui/foundation | Kunci eksplisit versi material3; uji resolusi dependency; bila gagal, naikkan Compose BOM ke versi yang mendukung 1.5.0 |
| R5 | minSdk 28 < Android 13 → shader tidak penuh | Beri `fallbackSurface` dari `AppShapes`/color scheme aplikasi |
| R6 | Menghapus modul `:speech` utuh memutus ASR cloud | Hapus selektif (hanya paket `asr/local/*`, `Sherpa*`, UI local STT) |

### 1.5 Kriteria penerimaan

- Tidak ada string/menu `Local LLM`, `Local STT`, `LiteRT`, `sherpa` di UI Android maupun web-ui.
- Tidak ada native `.so`/AAR local LLM/STT yang masuk ke packaging arm64.
- `material3` = 1.5.0 stabil; Liquid Glass aktif dengan `LiquidGlassTheme` di root.
- Navbar 4 tab mengapung dengan material kaca; chat tetap usable tanpa tab settings.
- Tidak ada `LocalHapticFeedback` pemakaian langsung di kode aplikasi.

---

## 2. Audit (dari kode aktual)

### 2.1 Temuan kritis

| ID | Temuan | Keputusan |
|---|---|---|
| A1 | `ProviderSetting.LiteRtLocal` adalah subtype sealed `@SerialName("litert_local")` (`ai/.../ProviderSetting.kt:270-318`). Settings lama bisa berisi tipe ini. `SettingsPreferencesDecode.kt:13-33` sudah skip per-elemen, tetapi `WebdavSync.kt:470` memakai `decodeFromString<Settings>` **strict**. | **Tombstone** serialization-only + perluas `PreferenceStoreV1Migration.kt:43-62` untuk membuang `"litert_local"`, dan buat restore WebDAV toleran. |
| A2 | `Settings.sttModelId` tidak dibersihkan oleh `clearMissingModelReferences()` (`PreferencesModels.kt:630-684`). Mic bisa tetap aktif walaupun provider lokal hilang. | Tambahkan pembersihan `sttModelId` saat normalisasi. |
| A3 | Room v26→27→28→29 sudah membuat lalu menghapus `local_model_install`; DB v39 tidak perlu diubah. | **Jangan sentuh** migrasi DB / `version = 39`. |
| A4 | Tidak ada entri LiteRT/sherpa/`local llm` di `catalog/lastchat_catalog.json`; `stt_providers` sudah cloud-only. | Katalog tidak diubah. |
| A5 | `:speech` berisi controller ASR cloud (OpenAI-compatible/realtime, DashScope, Volcengine, MiMo, Step) + sherpa lokal. | Hapus **hanya** `asr/local/*`, `SherpaASRController`, aset & AAR; cloud tetap. |
| A6 | Tidak ada `.so` lokal AI yang di-commit; LiteRT dari Maven, sherpa dari AAR unduhan. `jniLibs` proot/mupdf **tidak** terkait. | Hapus AAR + task unduh; jangan hapus jniLibs lain. |
| A7 | Tidak ada menu lokal di `web-ui/`; jejak web hanya `WebDtos.kt:963`. | Hapus satu branch tersebut. |
| A8 | Environment ini **tanpa Java & Android SDK**; mustahil kompilasi. | Verifikasi statis (grep + diff) + review manual. |

### 2.2 Inventaris perubahan (ringkas)

- **Gradle/native**: `settings.gradle.kts:38`, `app/build.gradle.kts:247,402-404,407`, `speech/build.gradle.kts:12-49,93,101-102`, `libs.versions.toml:58-60,158-160`, `app/proguard-rules.pro:51-59`, `speech/consumer-rules.pro`, manifest `local-llm/` & `speech/`, `values/strings.xml:1438-1492,1576-1597`.
- **Hapus file/dir**: `local-llm/` (seluruhnya), `app/data/ai/local/`, `app/data/datastore/LocalModelSettingsSync.kt`, `app/utils/LocalInferenceErrors.kt`, `common/inference/`, `ui/pages/setting/locallm/`, `ui/pages/setting/localstt/`, `speech/asr/local/`, `speech/asr/providers/SherpaASRController.kt`, aset sherpa, tes terkait.
- **`:ai`**: `ProviderSetting.kt` (tombstone), `ProviderManager.kt:63-65`, `ApiKeyResolution.kt:44,52`.
- **`:app` data/DI/startup**: `GenerationHandler.kt:2000,2053`, `CatalogSettingsMerger.kt:74,127,136`, `ModelCatalog.kt:1176,1200`, `ModelMetadataResolver.kt:311`, `PreferencesModels.kt:330-354`, `PreferencesStore.kt:301,328-330,483`, `SecretKeyManager.kt:31,174-185,438,649-651`, `PreferenceStoreV1Migration.kt`, `WebdavSync.kt:470`, `CherryStudioProviderImporter.kt:126`, `DataSourceModule.kt:402-463`, `ViewModelModule.kt:47-51,101-122`, `LastChatApp.kt:31,73,221-228,280-299,313,326`.
- **`:app` UI/nav**: `RouteActivity.kt:86,942-946,1406`, `ui/hooks/STT.kt`, `AIIcon.kt:71-80`, `ChatPage.kt:194,694`, `AssistantOverlayScreen.kt:142,235`, `SettingVM.kt:38-66`, `SettingModelPage.kt:208-212`, `SettingProviderPage.kt` (6 blok), `SettingProviderDetailPage.kt` (8 blok), `ProviderConfigure.kt`, `ProviderPresets.kt`, `OnboardingVM.kt`, `OnboardingPage.kt`, `BackupVM.kt:187`, `WebDtos.kt:963`.
- **Tes**: `ProviderSettingsNormalizationTest.kt:145-186` hapus + adaptasi tes migrasi; `SettingsPreferencesDecodeTest.kt` tambah kasus skip.

---

## 3. Revisi 1 — koreksi terhadap temuan audit

1. **R1 diperkuat**: bukan sekadar "hapus subtype", tapi **tombstone `@Serializable @SerialName("litert_local")` tanpa perilaku** (tidak terdaftar di `Types`, tidak punya provider/native). Ini menjaga kompatibilitas baca settings lama dan mencegah `WebdavSync` strict-decode crash. Migration V1 juga membuang entri lama.
2. **R2 ditambahkan**: normalisasi settings menghapus `sttModelId` yang mengacu ke model lokal yang sudah dihapus, sehingga tombol mic tidak aktif tanpa provider.
3. **R3 dikunci**: semua migrasi Room (termasuk v40→39) dan `version = 39` dibiarkan apa adanya.
4. **R5 diperketat**: `:speech` tidak dihapus; hanya paket lokal. `ASRProviderSetting.SystemSTT` (SpeechRecognizer platform) ikut dipertahankan.
5. **R6 diperjelas**: `workspace`, `document`, dan `jniLibs` proot/mupdf bukan AI lokal → tidak tersentuh.
6. **Verifikasi realistis**: karena tidak ada JDK/SDK, kriteria penerimaan kompilasi diganti dengan grep nol-hit + review diff; error kompilasi yang mungkin tersisa dicatat sebagai catatan untuk user.

---

## 4. Revisi 2 — finalisasi desain navbar & integrasi glass

1. **Navbar global 4 item** (sesuai pilihan user): `Chat`, `Provider`, `UI Customization`, `Lainnya`.
   - `Chat` → `navigateToChatPage(...)` (aturan repo tetap dipatuhi).
   - `Provider` → `Screen.SettingProvider`; `UI Customization` → `Screen.SettingUICustomization`; `Lainnya` → `Screen.Setting`.
   - Status seleksi dibaca dari `currentBackStackEntryAsState` dan di-*map* ke salah satu dari 4 tab; rute lain (assistant, backup, imagegen, dll) mewarisi tab `Lainnya` agar tidak ada tab "kosong".
2. **Isolasi Chat tetap terjaga secara visual**: tab Chat hanya membuka permukaan chat/AI; tidak ada chrome pengaturan di dalamnya. Navbar bersifat global, jadi tidak ada konflik dengan opsi "isolated" yang awalnya disebut.
3. **Haptic**: `GlassTabBar` memakai `LocalHapticFeedback` (dilarang langsung oleh `AGENTS.md`). Solusi: `CompositionLocalProvider(LocalHapticFeedback provides PremiumHapticFeedbackAdapter)` yang meneruskan `SegmentTick`/`SegmentStart` ke `PremiumHaptics.Tick`/`Pop`. Tidak ada pemanggilan `LocalHapticFeedback.current.performHapticFeedback(...)` di kode aplikasi.
4. **Theme**: `LiquidGlassTheme(background = ...)` dipasang di `RouteActivity.setContent` di atas `AppRoutes`; `background` diambil dari warna surface aktual tema (dark → near-black, light → near-white). `fallbackSurface` diberikan pada glass dengan label untuk API < 33.
5. **Import glass**: pakai import eksplisit `com.wexpa.liquidglass.material3.*` (bukan wildcard) agar `Text`/`Icon`/`MaterialTheme` tetap dari Material; star import existing tidak dihapus.
6. **Material 3 1.5.0**: perubahan `Slider`/`RangeSlider` bersifat *deprecated*, bukan *removed* → call site existing tetap kompil; perbaikan hanya bila ada error. `LocalMotionScheme` tidak dipakai di repo.
7. **Urutan eksekusi final**: Fase A (penghapusan lokal, 3 agen paralel per-domain) → Fase B+C+D (Material3 + Liquid Glass + navbar, satu tangan agar `RouteActivity` tidak bentrok) → verifikasi statis.

### Checklist eksekusi final

- [ ] A1 — Gradle, modul, native, proguard, manifest, strings, tes
- [ ] A2 — data layer, DI, startup, settings migration/sync
- [ ] A3 — UI, navigasi, onboarding, web DTO, `:ai` provider
- [ ] B — Material3 1.5.0 + dependensi Liquid Glass + `LiquidGlassTheme`
- [ ] C — Adopsi komponen glass pada komponen kunci
- [ ] D — `GlassTabBar` global 4 tab + haptic adapter
- [ ] E — Grep verifikasi + review diff

---

## 5. Implementasi & Verifikasi (selesai)

### 5.1 Yang sudah dikerjakan
- **A1** (`✓`): modul `:local-llm` dihapus; Gradle/AAR/proguard/manifest/string lokal dibersihkan; stack sherpa lokal di `:speech` dihapus, controller ASR cloud tetap ada.
- **A2** (`✓`): data layer, DI, startup, migrasi settings, secret manager, WebDAV restore toleran, `sttModelId` dibersihkan. Room v39 & migrasi tidak disentuh.
- **A3** (`✓`): UI/VM/rute lokal dihapus; `:ai` menyimpan **tombstone** `litert_local` serialization-only untuk kompatibilitas; `WebDtos` dibersihkan.
- **B** (`✓`): `material3 = 1.5.0` stabil; plugin + BOM + artifact Liquid Glass 1.0.0; `LastChatTheme` membungkus `RikkahubTheme` + `LiquidGlassTheme` dengan `background` dari theme & adaptive quality.
- **C** (`✓`): glass Material 3 diadopsi pada komponen yang API-nya kompatibel (`Surface`, `Switch`, `SegmentedButton*`) di 78+ file; helper `lastChatGlass` dengan `fallbackSurface` untuk API < 33; diterapkan pada kartu apung chat.
- **D** (`✓`): `LastChatFloatingNavBar` memakai `GlassTabBar` 4 tab (Chat/Provider/UI/More), haptic dijembatani lewat `PremiumHaptics` adapter; NavHost diberi bottom padding; status tab dibaca dari back stack.
- **E** (`✓` verifikasi statis): 0 referensi lokal di source/build (kecuali tombstone + tes historis + migration literal); brace/paren seimbang; tanpa import duplikat hasil edit kami.

### 5.2 Catatan penting untuk build di mesin dengan JDK
1. Environment ini **tidak punya Java/Android SDK**, jadi kompilasi tidak dijalankan.
2. Material3 1.5.0 stabil mengubah beberapa API (`Slider`/`RangeSlider` stateful, `Card`/`Button` shape/colors). Perbaiki call site bila kompilasi melaporkan error.
3. Adopsi glass penuh dapat diselesaikan dengan task resmi library:
   ```
   ./gradlew liquidGlassAdopt --dry-run   # lihat perubahan
   ./gradlew liquidGlassAdopt             # terapkan sekali
   ```
   Script import manual kita sengaja dibatasi pada komponen yang signature-nya identik menurut API dump resmi `liquidglass-material3.api` (`Surface`, `Switch`, `SegmentedButton*`) agar tidak memecah argumen `colors`/`elevation`.
4. Cek resolusi plugin `dev.shayxo.liquidglass:1.0.0` (mavenCentral) saat Gradle sync pertama.

### 5.3 Kriteria yang perlu dicek di device
- Navbar glass mengapung dan tetap terlihat di layar chat; tab berpindah sesuai rute.
- Kartu apung chat (tool approval/questionnaire/workspace) benar-benar refraktif di Android 13+; fallback terbaca di Android < 13.
- Tidak ada menu Local LLM/STT di Android maupun web; tidak ada library native LiteRT/sherpa ikut ter-packaging pada APK arm64.
