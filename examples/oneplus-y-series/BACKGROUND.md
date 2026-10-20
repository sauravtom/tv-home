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

The default mode is **Rotate my photos (5 minutes)**, using JPG/PNG/WebP files in the
launcher's external app directory, `files/wallpapers`. A different photo is chosen whenever
Home opens, and every five minutes while it stays visible (no immediate repeat with multiple images).
**Use rotating nature backgrounds (5 minutes)** switches back to the bundled nature set.
Single-image selection remains available. Personal photos are stored separately from the APK;
updating the launcher preserves them, while uninstalling it removes its app files.

The supplied ZIP remains the original photo backup. Personal photos and resized copies are
excluded from GitHub backups.

Use **Next bg** in the Home top bar to skip immediately to another image in the active rotation.
BookReels and Ramen follow YouTube, Hotstar, Netflix and Prime Video in the dock.

## Clock and birthdays

The date uses a larger font, with no greeting. The next three recurring birthdays appear below
it, ordered by their next occurrence, including the next year when a date has passed.
Household birthday data lives in the app's external `files/birthdays.json`, separate from source
and excluded from GitHub. Each entry has `name`, `month` (1–12), and `day`.

## Photo-only screensaver

The launcher also provides **My photo screensaver**, an Android DreamService. It reads only the
uploaded photo album, regardless of Home's nature-background setting, and changes photos every
five minutes. Remote input dismisses the screensaver normally. The previous Android screensaver
selection is saved locally under the ignored backup directory before changing the TV setting.
