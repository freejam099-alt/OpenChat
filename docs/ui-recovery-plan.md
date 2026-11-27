# UI Recovery — Planning, Full Code Reading, 10 Revisions

Status: implemented, pending CI verification. User rating 0/9. Problems: chat layout broken, Liquid Glass only cosmetic, input bar/send/cylinder not glass, "+" force-close, unstable AI configuration.

## Planning
Five parallel full-domain code readings (chat composer, provider config, glass API, navigation/layout, stability regressions) + official Liquid Glass API dump verification. Then 10 revisions below, applied before build.

## Root causes found
- **Crash "+":** glass `Surface` inside AndroidX `ModalBottomSheet` (separate Dialog window) while `LiquidGlassTheme` records the Activity window → `localPositionOf` cross-root failure. Both composer "+" and provider "+" open such sheets.
- **Inert guards:** `try/catch` around `showPicker = true` cannot catch a compose-time crash.
- **Sheet race:** state flag cleared immediately after launching `hide()`; M3 1.5.0 anchor disposal can throw.
- **Recomposition loop:** `neighborsUnlocked` written during composition in provider drag list.
- **Layout:** navbar was an overlay sibling with hand-tuned `padding(bottom=104.dp)`; magic paddings stacked in composer/list/settings.
- **Glass invisible:** `Modifier.glassEffect` over opaque Material `Surface` samples its own flat fill; colour must live in the glass, container must be transparent.

## 10 Revisi
1. Semua `ModalBottomSheet` + `rememberModalBottomSheetState` (37 file) dipindah ke `com.wexpa.liquidglass.material3.*`; argumen `containerColor`/`sheetGesturesEnabled` dibuang (glass sheet menggambar di `GlassOverlay`, window yang sama).
2. Composer diganti ke komponen glass akar baru `GlassComposerCapsule`, `GlassComposerAddButton`, `GlassComposerActionButton` (`Surface` dari glass, container transparan, `glass = GlassStyle.inApp(...)` + fallback).
3. Hapus `lastChatBlurEffect`/`lastChatGlass` stack pada composer; satu sistem glass saja. Ikon Picker diubah dari `ModelSelector` ke `Icons.Rounded.Add`; cabang STT/Stop diperbaiki.
4. Navbar dijadikan `Scaffold.bottomBar` nyata di `AppRoutes`; `innerPadding` (`contentWindowInsets = WindowInsets(0)`) dipakai NavHost. Hapus `padding(bottom=104.dp)` dan `.navigationBarsPadding()` ganda di composer.
5. Perbaikan race sheet: `hide()` di-await, flag hanya di-clear bila `!isVisible`.
6. `neighborsUnlocked` diubah dari state yang ditulis saat komposisi menjadi `derivedStateOf`.
7. Filter tombstone `LiteRtLocal` di `settingsFlowRaw` dan `SettingsStore.update` agar tidak muncul sebagai provider NIL.
8. LazyColumn preset key memakai `id + index` (mencegah "Key was already used").
9. `AIRequestInterceptor` tidak lagi menimpa Authorization dengan `Bearer ` kosong; crash logger Haze juga menulis `crash-last.txt`.
10. `openProviderSheet`: haptic di-`runCatching`, state sheet di-set di luar try (open tidak lagi gagal silent).

## Verifikasi
- Build CI: `.github/workflows/arm64-release.yml`, target APK arm64 signed 28 MB.
- Jika crash masih ada, stack trace tersedia di `/sdcard/Android/data/lastchat.rikkafork.cocolal/files/crash-last.txt` (atau `adb logcat | grep LastChatCrash`).
