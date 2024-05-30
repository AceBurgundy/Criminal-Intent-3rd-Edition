package com.aceburgundy.criminalintent.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.view.WindowManager;
import android.view.WindowMetrics;

public class PictureUtilities {

    private PictureUtilities() {}
    public static Bitmap getScaledBitmap(String path, int destWidth, int destHeight) {

        // Read in the dimensions of the image on disk
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(path, options);
        float sourceWidth = options.outWidth;
        float sourceHeight = options.outHeight;

        // Figure out how much to scale down by
        int inSampleSize = 1;

        if (sourceHeight > destHeight || sourceWidth > destWidth) {
            float heightScale = sourceHeight / destHeight;
            float widthScale = sourceWidth / destWidth;
            inSampleSize = Math.round(Math.max(heightScale, widthScale));
        }

        options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;

        // Read in and create final bitmap
        return BitmapFactory.decodeFile(path, options);
    }

    public static Bitmap getScaledBitmap(String path, Context context) {
        WindowManager windowManager = context.getSystemService(WindowManager.class);
        WindowMetrics windowMetrics = windowManager.getCurrentWindowMetrics();
        Rect bounds = windowMetrics.getBounds();
        int height = bounds.height();
        int width = bounds.width();
        return getScaledBitmap(path, width, height);
    }

}
