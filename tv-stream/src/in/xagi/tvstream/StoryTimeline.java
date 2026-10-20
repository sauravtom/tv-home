package in.xagi.tvstream;

/** Uses narration position, so pause, seek, buffering and resume share one clock. */
public final class StoryTimeline {
    public static int beatAt(double[] starts, double seconds) {
        int beat = -1;
        for (int i = 0; i < starts.length && starts[i] <= seconds; i++) beat = i;
        return beat;
    }
    public static int imageAt(int[] images, double start, double duration, double seconds) {
        if (images.length == 0) return -1;
        int part = duration <= 0 ? 0 : (int) ((seconds - start) / duration * images.length);
        return images[Math.max(0, Math.min(images.length - 1, part))];
    }
}
