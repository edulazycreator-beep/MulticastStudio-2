# Architecture — current starter and planned modules

## Implemented vertical slice

```text
MainActivity
  ├── Runtime camera permission
  ├── CameraManager concurrent-ID discovery (API 30+)
  ├── CameraX ProcessCameraProvider
  │     └── One Preview use case bound to the Activity lifecycle
  └── Glassmorphism UI
        ├── Front/rear source selection
        └── Streaming-not-connected notice
```

## Planned production architecture

```text
Studio UI (Preview / Program / Sources / Scenes)
                  │
             ViewModel + StateFlow
                  │
          StudioSession / UseCases
          ┌───────┼─────────┐
          │       │         │
      Camera I/O  Audio I/O  Network Sources
          │       │         │
      Frame Router / Timestamp Synchronization
                  │
       GPU Compositor (EGL / OpenGL ES)
                  │
         MediaCodec Video + AAC Audio
                  │
           Muxer / RTMP(S) Transport
                  │
             Streaming Service
```

The planned boxes are architectural targets, not implemented functionality in the current starter ZIP.
