package com.mihir.tvhome;

import android.service.dreams.DreamService;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import java.io.File;
import java.util.Locale;
import java.util.Random;

/** Photo-only screensaver. Uses the household album regardless of Home wallpaper mode. */
public class PhotoDreamService extends DreamService {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private ImageView image;
    private int last = -1;
    private boolean attached;
    private int generation;
    private final Random random = new Random();
    private final Runnable rotate = this::next;

    @Override public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setInteractive(false);
        setFullscreen(true);
        image = new ImageView(this);
        image.setBackgroundColor(Color.BLACK);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        setContentView(image);
        attached = true;
        next();
    }

    private void next() {
        handler.removeCallbacks(rotate);
        File[] files = new File(getExternalFilesDir(null), "wallpapers").listFiles(file -> {
            String name = file.getName().toLowerCase(Locale.ROOT);
            return file.isFile() && (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".webp"));
        });
        if (!attached || files == null || files.length == 0) return;
        java.util.Arrays.sort(files);
        int selected = files.length == 1 ? 0 : random.nextInt(files.length - (last >= 0 && last < files.length ? 1 : 0));
        if (files.length > 1 && last >= 0 && last < files.length && selected >= last) selected++;
        last = selected;
        File file = files[selected];
        final int request = ++generation;
        new Thread(() -> {
            Bitmap bitmap = null;
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(file.getAbsolutePath(), options);
                options.inSampleSize = 1;
                while (options.outWidth / options.inSampleSize > 1920 || options.outHeight / options.inSampleSize > 1080) options.inSampleSize *= 2;
                options.inJustDecodeBounds = false;
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
            } catch (RuntimeException | OutOfMemoryError ignored) { }
            final Bitmap result = bitmap;
            handler.post(() -> {
                if (!attached || request != generation) { if (result != null) result.recycle(); return; }
                if (result != null) image.setImageBitmap(result);
            });
        }).start();
        handler.postDelayed(rotate, 5 * 60 * 1000);
    }

    @Override public void onDetachedFromWindow() {
        attached = false;
        ++generation;
        handler.removeCallbacks(rotate);
        if (image != null) image.setImageDrawable(null);
        super.onDetachedFromWindow();
    }
}
