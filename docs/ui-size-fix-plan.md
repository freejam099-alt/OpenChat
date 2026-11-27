# UI Glass Polish + Crash Fix + Size 31–35 MB — Planning, Audit (5×), Revisi

Tanggal: 2026-11-27. Build arm64 signed saat ini: **49 MB** (CI sukses). Target: **31–35 MB**.

## Planning

1. Perbaiki layout/force-close pada tombol "+" (chat composer & provider FAB / bottom sheet).
2. Poles total komponen dengan Liquid Glass: input bar, popup/sheet, search, FAB, tombol send, chip, animasi chat.
3. Kecilkan APK arm64 ke 31–35 MB dengan memotong fitur berat yang tidak inti.
4. Verifikasi via GitHub Actions (`arm64-release.yml`) dan minta pengguna menguji ulang UI.

## AuditPass 1 — tombol "+"
- Chat: `MinimalChatInput.kt:780-815` `LastChatComposerAddButton` → `showPicker = true`.
- Provider: `SettingProviderPage.kt:1315` FAB `Icons.Rounded.Add` → `openProviderSheet` → `ModalBottomSheet` + preset list.
- Screenshot: navbar glass menimpa konten; FAB "+"/sheet bertumpuk dengan navbar.

## AuditPass 2 — modul berat (data)
- `:document` + MuPDF: `libmupdf_java.so` 9,44 MB (≈4,4 MB di APK).
- Dipakai `DocumentPromptParser.kt` (PdfParser/DocxParser) + `DocumentAsPromptTransformer`.

## AuditPass 3 — Firebase & scanner
- Firebase: `AppModule.kt` (Crashlytics, RemoteConfig), `LastChatApp.kt:106,225`, `AIRequestInterceptor.kt`, `DataSourceModule.kt:273`.
- ML Kit barcode: `libbarhopper_v3.so` 4,95 MB; `QRCode.kt`, `ShareSheet.kt`, `SettingProviderPage.kt`.

## AuditPass 4 — web-ui & resource
- `assets/` 13,03 MB di APK; lokal web-ui build = 5,05 MB mentah / **1,87 MB terkompresi**.
- `res/` 8 MB: font 3,81 MB + placeholder PNG 2,17 MB + 7 locale.
- Navbar `RouteActivity.kt:822` sudah ada `padding(bottom=82.dp)`, namun konten masih bertabrakan pada layar settings/provider.

## AuditPass 5 — QuickJS & glass adoption
- QuickJS: `libquickjs-android-wrapper.so` 1,33 MB; `LocalTools.kt` (JS tool), `highlight` module.
- Glass baru hanya di 3 kartu (`lastChatGlass`) + navbar; input bar, FAB, search, popup, send, animasi belum kaca.

## Revisi (keputusan setelah 5 audit)
- Potong: `:document`/MuPDF, ML Kit barcode+camera, bundle web-ui, Firebase, QuickJS tool/native, font→subset & placeholder→WebP.
- Perbaikan crash: hardening FAB/bottom-sheet picker (guard state), hapus FAB ganda, beri content padding dinamis agar navbar tidak menimpa daftar.
- Glass: helper `lastChatGlass` dipakai ke input bar, FAB, search, sheet, tombol send, chip saran, kartu AI; `GlassTabBar` tetap global.
- Target setelah potong: ~31–34 MB. Jika masih >35 MB, subset font lebih agresif / hapus locale non-utama.
