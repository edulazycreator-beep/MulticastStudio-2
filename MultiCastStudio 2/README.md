# MultiCast Studio — Android starter project

A lightweight Kotlin/CameraX starter for an OBS-inspired mobile live-production app with a dark glassmorphism interface.

## Current implementation status

| Capability | Status |
|---|---|
| Glassmorphism studio UI | Implemented |
| Runtime camera permission | Implemented |
| CameraX preview | Implemented (one active camera at a time) |
| Front/rear camera switching | Implemented |
| Android concurrent-camera capability discovery | Implemented (API 30+) |
| Simultaneous dual-camera preview/compositing | Not implemented in this starter |
| Scenes, CUT/AUTO transitions | Planned |
| Audio mixer | Not implemented |
| RTMP/RTMPS streaming and encoder | Not implemented |
| USB/UVC, HDMI capture, RTSP, NDI | Planned; requires separate integration and device testing |

**Important:** this ZIP is a starter source project, not a finished OBS replacement and not a prebuilt APK. The app currently displays one CameraX preview, allows front/rear switching, and reports whether Android exposes a concurrent camera pair. The Go Live control explains that streaming is not yet wired.

## Requirements

- Android Studio with a compatible Android Gradle Plugin.
- JDK 17.
- Android SDK Platform 36.
- Android device/emulator with a camera; Android 11+ is required for the concurrent-camera capability query used by this starter.

## Open and build

1. Extract the ZIP.
2. Open the `MultiCastStudio` folder in Android Studio.
3. The included `gradlew` / `gradlew.bat` launcher bootstraps the pinned Gradle distribution on first use; an internet connection is needed for that first download.
4. Allow Gradle sync to complete, connect an Android device, and grant camera permission.
5. Build with **Build → Build Bundle(s) / APK(s) → Build APK(s)**, or run:

```bash
./gradlew clean assembleDebug
```

APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Build on GitHub

The workflow is at `.github/workflows/android.yml`. Push the project to a GitHub repository and run the **Android Build** workflow. The generated debug APK is uploaded as the `MultiCastStudio-debug-apk` Actions artifact.

The workflow uses `android-actions/setup-android@v4` and installs `platform-tools`, `platforms;android-36`, and `build-tools;36.0.0`. It intentionally does not request the deprecated SDK `tools` package.

## Technical notes

- Language: Kotlin.
- Camera: CameraX 1.6.2.
- Min SDK: 26.
- Compile/target SDK: 36.
- Android Gradle Plugin: 9.3.1.
- Kotlin Gradle Plugin: 2.4.21.
- Pinned Gradle distribution: 9.5.0 (bootstrapped by the included `gradlew` launcher). The official Gradle wrapper JAR is not bundled; the included shell/Windows launchers download and invoke the pinned Gradle distribution directly.

Concurrent camera behavior is device-dependent. Android exposes supported concurrent camera ID combinations, but that capability does not automatically create a dual preview or guarantee every stream size/format. See the Android multi-camera documentation linked in `docs/DEVICE_COMPATIBILITY.md`.

## Next engineering steps

1. Implement and validate CameraX/Camera2 concurrent sessions for supported camera pairs.
2. Add a GPU compositor for Preview/Program, PIP, split-screen, and scene switching.
3. Add an audio capture/mixer pipeline.
4. Add hardware video/audio encoding and a properly licensed RTMP/RTMPS transport.
5. Test on real devices and publish a compatibility matrix before advertising multi-camera support.

See `PROMPT_UTAMA_ID.md` for the full implementation prompt.
