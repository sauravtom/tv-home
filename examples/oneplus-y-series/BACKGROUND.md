# Change the background on the TV

Use the remote to open **Background** in the top bar, then **Choose image from TV or USB**.
Allow storage access when prompted. Browse Internal storage (Download or Pictures), or a USB drive,
and select a JPG, PNG, or WebP image. The launcher saves its own resized copy, so the USB drive
can be removed afterward. The image remains selected after restarting the launcher.

Choose **Use rotating nature wallpapers** to return to the bundled backgrounds.
Landscape images work best; the picture fills the screen and may be cropped.

This Android 9 build uses a built-in file browser because the TV only supplies a stub for
Android's standard document picker. It requests read-only shared-storage permission.
User images are stored privately on the TV and are not included in Git backups.

## Personal photo rotation

The default mode is **Rotate my photos (1 hour)**, using JPG/PNG/WebP files in the
launcher's external app directory, `files/wallpapers`. A different photo is chosen whenever
Home opens, and every hour while it stays visible (no immediate repeat with multiple images).
**Use rotating nature backgrounds (1 hour)** switches back to the bundled nature set.
Single-image selection remains available. Personal photos are stored separately from the APK;
updating the launcher preserves them, while uninstalling it removes its app files.

The supplied ZIP remains the original photo backup. Personal photos and resized copies are
excluded from GitHub backups.
