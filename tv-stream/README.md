# Minimal streaming TV clients

Two small Android 9+ apps, using Android framework views and MediaPlayer with no bundled
Flutter runtime or media dependencies. BookReels reads the existing public books and stories
APIs; Ramen reads the existing published series API. No backend writes or credentials.

- D-pad and OK browse series/books, chapters, stories, and language choices.
- OK on Play/Pause toggles playback; Left/Right seek 15 seconds; Back returns to library.
- Native Android player streams directly from the published HTTPS URLs. No downloads,
  application disk cache, image cache, or offline media library.
- Small local resume preferences only; Android and the media stack use temporary RAM buffers.
- BookReels stories currently provide narration; the illustrated slideshow and social/account
  features of the main apps are not included in these minimal TV clients.
- Ramen keeps portrait video aspect ratio on a landscape TV.

Build on the existing AWS machine with JDK 17 and the existing Android SDK:

```
ANDROID_HOME=/home/saurav/Android/Sdk BUILD_TOOLS_VERSION=35.0.0 bash tv-stream/build.sh
```

Outputs: `tv-stream/build/{bookreels,ramen}/*-tv.apk`. Signing reuses the ignored
launcher debug keystore on the build host. Keep that original key for local updates; never
commit signing material. This is a household sideload build, not a store distribution.
