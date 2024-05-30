package com.aceburgundy.criminalintent.crimefragmentview;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;

import com.aceburgundy.criminalintent.Crime;
import com.aceburgundy.criminalintent.CrimeLab;
import com.aceburgundy.criminalintent.R;
import com.aceburgundy.criminalintent.utilities.Helper;

import java.io.File;
import java.util.List;

public class CrimeFragmentEvents {

    private static CrimeFragment.Callbacks callbacks;
    private static FragmentActivity activity;
    private static Crime crime;

    public static void initializeCallbacks(CrimeFragment.Callbacks callbacks) {
        CrimeFragmentEvents.callbacks = callbacks;
    }

    public static void initializeActivity(FragmentActivity activity) {
        CrimeFragmentEvents.activity = activity;
    }

    public static void initializeCrime(Crime crime) {
        CrimeFragmentEvents.crime = crime;
    }

    private CrimeFragmentEvents() {}

    @NonNull
    private static String getCrimeReport() {

        String solvedString = activity.getString(crime.isSolved() ? R.string.crime_report_solved : R.string.crime_report_unsolved);
        String dateString = DateFormat.format("EEE, MMM dd", crime.getDate()).toString();
        String suspect = crime.getSuspectName();

        if (suspect == null) {
            suspect = activity.getString(R.string.crime_report_no_suspect);
        } else {
            suspect = activity.getString(R.string.crime_report_suspect, suspect);
        }

        // returns the report
        return activity.getString(R.string.crime_report,crime.getTitle(), dateString, solvedString, suspect);
    }

    public static void updateCrime() {
        CrimeLab.get(activity).updateCrime(crime);
        callbacks.onCrimeUpdated(crime);
    }

    // LISTENERS

    @NonNull
    public static TextWatcher updateCrimeInstanceTextField() {
        return new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence sequence, int start, int count, int after) {
                // Left blank;
            }

            @Override
            public void onTextChanged(CharSequence sequence, int start, int before, int count) {
                crime.setTitle(sequence.toString());
                updateCrime();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Left blank;
            }

        };
    }

    public static void toggleCrimeIsSerious(CheckBox solvedCheckBox, @NonNull Boolean isChecked) {

        String disabledCrimeSolvedMessage = activity.getString(R.string.disable_toggle_crime_solved_message);
        String crimeSolvedMessage = activity.getString(R.string.toggle_crime_solved_message);

        if (isChecked) {
            solvedCheckBox.setContentDescription(disabledCrimeSolvedMessage);
            solvedCheckBox.setChecked(false);

            crime.setRequiresIntervention(true);
            crime.setSolved(false);

            Helper.disableElement(solvedCheckBox);
        } else {
            solvedCheckBox.setContentDescription(crimeSolvedMessage);
            Helper.enableElement(solvedCheckBox);
            crime.setRequiresIntervention(false);
        }

        updateCrime();
    }

    public static void toggleCrimeSolved(CheckBox crimeIsSeriousCheckBox, CheckBox solvedCheckBox, @NonNull Boolean isChecked) {

        String disableRequiresPoliceMessage = activity.getString(R.string.disable_crime_requires_intervention_message);
        String requiresPoliceMessage = activity.getString(R.string.crime_requires_intervention_message);

        if (isChecked) {
            crimeIsSeriousCheckBox.setContentDescription(disableRequiresPoliceMessage);
            solvedCheckBox.setChecked(true);

            crime.setRequiresIntervention(false);
            crime.setSolved(true);

            Helper.disableElement(crimeIsSeriousCheckBox);
        } else {
            crimeIsSeriousCheckBox.setContentDescription(requiresPoliceMessage);
            Helper.enableElement(crimeIsSeriousCheckBox);
            crime.setSolved(false);
        }

        updateCrime();
    }

    public static void captureImage(File photoFile, ActivityResultLauncher<Intent> captureImageIntentLauncherContract) {
        final Intent captureImageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        Uri uri = FileProvider.getUriForFile(activity,"com.bignerdranch.android.criminalintent.fileprovider", photoFile);
        captureImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

        @SuppressLint("QueryPermissionsNeeded")
        List<ResolveInfo> cameraActivities = activity.getPackageManager().queryIntentActivities(captureImageIntent, PackageManager.MATCH_DEFAULT_ONLY);

        for (ResolveInfo currentActivity : cameraActivities) {
            activity.grantUriPermission(currentActivity.activityInfo.packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }

        captureImageIntentLauncherContract.launch(captureImageIntent);
    }

    public static void attemptToCallSuspect() {
        if (crime.getSuspectNumber() != null) {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + crime.getSuspectNumber()));
            activity.startActivity(intent);
        } else {
            Toast.makeText(activity, "Suspect Does not have a number", Toast.LENGTH_SHORT).show();
        }
    }

    public static void attemptToReportSuspect() {
        ShareCompat.IntentBuilder intentBuilder = new ShareCompat.IntentBuilder(activity);
        intentBuilder.setType("text/plain");
        intentBuilder.setText(getCrimeReport());
        intentBuilder.setSubject(activity.getString(R.string.crime_report_subject));
        intentBuilder.setChooserTitle(activity.getString(R.string.send_report));
        Intent intent = intentBuilder.createChooserIntent();
        activity.startActivity(intent);
    }

}
