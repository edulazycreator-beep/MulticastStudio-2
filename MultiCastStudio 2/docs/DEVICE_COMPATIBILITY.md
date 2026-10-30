# Device compatibility and test matrix

This starter does not assume every Android device can run multiple camera devices at once.

## Runtime behavior

- Android 11/API 30 and newer: the app queries `CameraManager.concurrentCameraIds` and reports whether Android advertises at least one concurrent camera pair.
- Android 10/API 29 and older: the starter reports that the concurrent-camera discovery path is unavailable.
- All versions supported by the project: the current app binds one CameraX Preview use case at a time and switches between front/rear cameras.
- Simultaneous preview/compositing is not implemented in this starter.

## Test matrix template

Fill this table only after testing on the physical device.

| Manufacturer/model | Android version | Concurrent IDs | Rear preview | Front preview | Simultaneous preview | Notes |
|---|---|---|---|---|---|---|
| Not tested | — | — | — | — | — | Add results from physical-device tests |

## Official references

- Android multi-camera API: https://developer.android.com/media/camera/camera2/multi-camera
- Concurrent camera streaming: https://source.android.com/docs/core/camera/concurrent-streaming
- CameraX release notes: https://developer.android.com/jetpack/androidx/releases/camera
