package com.aceburgundy.criminalintent.crimefragmentview;

import static com.aceburgundy.criminalintent.crimefragmentview.CrimeFragmentUtilities.announceCaptureImageStatusForTalkback;
import static com.aceburgundy.criminalintent.crimefragmentview.CrimeFragmentUtilities.rotateImage;
import static com.aceburgundy.criminalintent.crimefragmentview.CrimeFragmentUtilities.saveBitmapImage;
import static com.aceburgundy.criminalintent.crimefragmentview.CrimeFragmentUtilities.updateDate;
import static com.aceburgundy.criminalintent.crimefragmentview.CrimeFragmentUtilities.updatePhotoView;
import static com.aceburgundy.criminalintent.utilities.Helper.onPhone;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.ContactsContract;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;

import com.aceburgundy.criminalintent.Crime;
import com.aceburgundy.criminalintent.DatePickerFragment;
import com.aceburgundy.criminalintent.R;
import com.aceburgundy.criminalintent.Serializable;

import java.io.File;
import java.util.Date;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class CrimeFragmentActivityContracts {

    private static FragmentActivity activity;
    private static Crime crime;

    public static void initializeCrime(Crime crime) {
        CrimeFragmentActivityContracts.crime = crime;
    }

    public static void initializeActivity(FragmentActivity activity) {
        CrimeFragmentActivityContracts.activity = activity;
    }

    private CrimeFragmentActivityContracts() {}

    public static void dateIntentLauncherResultContract(@NonNull ActivityResult result, TextView dateTextView) {
        if (result.getResultCode() != Activity.RESULT_OK) return;
        Date date = (Date) Serializable.getSerializable(result.getData(), DatePickerFragment.EXTRA_DATE);
        crime.setDate(date);
        updateDate(dateTextView);
    }

    public static void chooseAndProcessContactResult(@NonNull ActivityResult result, Function<Intent, String> setSuspectsName, Consumer<String> setSuspectsPhoneNumber) {
        if (result.getResultCode() != Activity.RESULT_OK) return;
        Intent data = result.getData();
        if (data == null) return;

        final String contactID = setSuspectsName.apply(data);
        if (!Objects.equals(contactID, "")) setSuspectsPhoneNumber.accept(contactID);
    }

    public static void registerPermissionRequest(Boolean permissionGranted, ActivityResultLauncher<Intent> chooseAndProcessContactFromIntent) {
        if (Boolean.FALSE.equals(permissionGranted)) return;
        final Intent pickContactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        chooseAndProcessContactFromIntent.launch(pickContactIntent);
    }

    public static void captureImageResultContract(@NonNull ActivityResult result, File photoFile, ImageView imageBox) {
        String noPhotoMessage = activity.getString(R.string.crime_photo_no_image_description);
        String hasPhotoMessage = activity.getString(R.string.crime_photo_image_description);

        if (result.getResultCode() != Activity.RESULT_OK) return;

        Uri uri = FileProvider.getUriForFile(activity, "com.bignerdranch.android.criminalintent.fileprovider", photoFile);
        activity.revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        if (onPhone(activity)) {
            Bitmap imageRotated = rotateImage(photoFile, 90);
            if (imageRotated != null) saveBitmapImage(imageRotated, photoFile);
        }

        boolean photoIsPresent = !photoFile.exists();
        announceCaptureImageStatusForTalkback(imageBox, photoIsPresent ? hasPhotoMessage : noPhotoMessage);
        updatePhotoView(photoFile, imageBox);
    }

}