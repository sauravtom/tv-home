# Minimal streaming TV clients

Two small Android 9+ apps, using Android framework views and MediaPlayer with no bundled
Flutter runtime or media dependencies. BookReels reads the existing public books and stories
APIs; Ramen reads the existing published series API. No backend writes or credentials.

- D-pad and OK browse series/books, chapters, stories, and language choices.
- OK on Play/Pause toggles playback; Left/Right seek 15 seconds; Back returns to library.
- Native Android player streams directly from the published HTTPS URLs. No downloads,
  application disk cache, disk image cache, or offline media library.
- Small local resume preferences only; Android and the media stack use temporary RAM buffers.
- BookReels story illustrations and captions follow narration beat timestamps, including seeks
  and pause/resume. Images are streamed into a 4 MiB decoded-image memory cache, never disk.
  Images are decoded to at most 1280 x 720 with a 3 MiB transfer limit per illustration.
  One image request runs at a time, prefetching the next beat when possible.
  Social/account features of the main apps are not included in these minimal TV clients.
- Ramen keeps portrait video aspect ratio on a landscape TV.

Build on the existing AWS machine with JDK 17 and the existing Android SDK:

```
ANDROID_HOME=/home/saurav/Android/Sdk BUILD_TOOLS_VERSION=35.0.0 bash tv-stream/build.sh
```

Outputs: `tv-stream/build/{bookreels,ramen}/*-tv.apk`. Signing reuses the ignored
launcher debug keystore on the build host. Keep that original key for local updates; never
commit signing material. This is a household sideload build, not a store distribution.

## Validation on OnePlus Y Series (Android 9)

- Timeline boundary tests cover beat transitions, multi-image beats, empty data, and backward seeks.
- Streamed Hindi Gomini narration with the matching image and caption at 18 seconds.
- Remote OK paused playback; seeking backward selected the rice-in-the-sun image/caption at
  11 seconds. Seeking forward selected the goldsmith image/caption at 26 seconds while paused.
- No bundled illustrations, media files or added playback dependencies. BookReels APK: 25,068 bytes.
- Plain audiobook chapters retain the audio-only screen; illustrations require a story beat manifest.
