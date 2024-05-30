package com.aceburgundy.criminalintent.crimefragmentview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.aceburgundy.criminalintent.Crime;
import com.aceburgundy.criminalintent.R;
import com.aceburgundy.criminalintent.utilities.PictureUtilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

public class CrimeFragmentUtilities {

    private static Crime crime;
    private static Fragment fragment;

    public static void initializeFragment(Fragment fragment) {
        CrimeFragmentUtilities.fragment = fragment;
    }

    public static void initializeCrime(Crime crime) {
        CrimeFragmentUtilities.crime = crime;
    }

    private CrimeFragmentUtilities() {}

    public static void updateDate(@NonNull TextView dateTextView) {
        dateTextView.setText(crime.getFormattedDate());
    }

    public static void updateTime(@NonNull TextView timeTextView) {
        timeTextView.setText(crime.getFormattedTime());
    }

    public static void announceCaptureImageStatusForTalkback(ImageView imageBox, String message) {
        Looper looper = Objects.requireNonNull(Looper.myLooper());
        new Handler(looper).postDelayed(() ->
            imageBox.announceForAccessibility(message)
        , 1000);
    }

    public static void updatePhotoView(File photoFile, ImageView imageBox) {

        String noPhotoMessage = fragment.getString(R.string.crime_photo_no_image_description);
        String hasPhotoMessage = fragment.getString(R.string.crime_photo_image_description);

        if (photoFile == null || !photoFile.exists()) {
            imageBox.setImageDrawable(null);
            imageBox.setContentDescription(noPhotoMessage);
            return;
        }

        ViewTreeObserver observer = imageBox.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(() -> {
            int width = imageBox.getMeasuredWidth();
            int height = imageBox.getMeasuredHeight();
            Bitmap bitmap = PictureUtilities.getScaledBitmap(photoFile.getPath(), width, height);

            // Attach the canvas to the ImageView
            imageBox.setImageBitmap(bitmap);
            imageBox.setContentDescription(hasPhotoMessage);
            announceCaptureImageStatusForTalkback(imageBox, hasPhotoMessage);
        });
    }

    public static Bitmap rotateImage(File photoFile, int degrees) {
        try {
            Bitmap originalBitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
            Matrix matrix = new Matrix();
            matrix.postRotate(degrees);
            return Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true);
        } catch (Exception error) {
            Log.d("Rotate Image Fail", error.toString());
            return null;
        }
    }

    public static void saveBitmapImage(Bitmap bitmap, File file) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (IOException error) {
            Log.d("Save Image Fail", error.toString());
        }
    }
}
