package com.aceburgundy.criminalintent.crimefragmentview;
import static com.aceburgundy.criminalintent.crimefragmentview.CrimeFragmentActivityContracts.captureImageResultContract;
import static com.aceburgundy.criminalintent.crimefragmentview.CrimeFragmentActivityContracts.chooseAndProcessContactResult;
import static com.aceburgundy.criminalintent.crimefragmentview.CrimeFragmentActivityContracts.dateIntentLauncherResultContract;
import static com.aceburgundy.criminalintent.crimefragmentview.CrimeFragmentActivityContracts.registerPermissionRequest;
import static com.aceburgundy.criminalintent.crimefragmentview.CrimeFragmentEvents.attemptToCallSuspect;
import static com.aceburgundy.criminalintent.crimefragmentview.CrimeFragmentEvents.attemptToReportSuspect;
import static com.aceburgundy.criminalintent.crimefragmentview.CrimeFragmentEvents.captureImage;
import static com.aceburgundy.criminalintent.crimefragmentview.CrimeFragmentEvents.toggleCrimeIsSerious;
import static com.aceburgundy.criminalintent.crimefragmentview.CrimeFragmentEvents.toggleCrimeSolved;
import static com.aceburgundy.criminalintent.crimefragmentview.CrimeFragmentEvents.updateCrime;
import static com.aceburgundy.criminalintent.crimefragmentview.CrimeFragmentEvents.updateCrimeInstanceTextField;
import static com.aceburgundy.criminalintent.crimefragmentview.CrimeFragmentUtilities.updateDate;
import static com.aceburgundy.criminalintent.crimefragmentview.CrimeFragmentUtilities.updatePhotoView;
import static com.aceburgundy.criminalintent.crimefragmentview.CrimeFragmentUtilities.updateTime;
import static com.aceburgundy.criminalintent.utilities.Helper.onPhone;
import static com.aceburgundy.criminalintent.utilities.Helper.onTablet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.aceburgundy.criminalintent.Crime;
import com.aceburgundy.criminalintent.CrimeLab;
import com.aceburgundy.criminalintent.CrimeListFragment;
import com.aceburgundy.criminalintent.DatePickerFragment;
import com.aceburgundy.criminalintent.DatePickerFragmentActivity;
import com.aceburgundy.criminalintent.ImageDisplayFragment;
import com.aceburgundy.criminalintent.R;
import com.aceburgundy.criminalintent.Serializable;
import com.aceburgundy.criminalintent.TimePickerFragment;
import com.aceburgundy.criminalintent.utilities.Helper;

import java.io.File;
import java.util.Date;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class CrimeFragment extends Fragment {
    private ActivityResultLauncher<String> requestReadContactsPermissionContract;
    private ActivityResultLauncher<Intent> captureImageIntentLauncherContract;
    private ActivityResultLauncher<Intent> dateIntentLauncherContract;
    private static final int DELETE_BUTTON_ID = R.id.delete_button;
    private static final String DIALOG_IMAGE = "DIALOG_IMAGE";
    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_TIME = "DialogTime";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String ZOOM_IMAGE = "ZOOM_IMAGE";
    private static final String DATE = "DATE";
    private static final String TIME = "TIME";
    private FragmentActivity activity;
    private TextView dateTextView;
    private TextView timeTextView;
    private Button suspectButton;
    private Callbacks callbacks;
    private Button reportButton;
    private ImageView imageBox;
    private Button callButton;
    private File photoFile;
    private Crime crime;
    private View view;

    /**
     * Required interface for hosting activities
     */
    public interface Callbacks {
        void onCrimeUpdated(Crime crime);
    }

    @NonNull
    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);
        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = requireActivity();
        callbacks = (Callbacks) context;
    }

    @SuppressLint({"Range", "Recycle"})
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        UUID crimeId = (UUID) Serializable.getSerializable(getArguments(), ARG_CRIME_ID);

        assert crimeId != null;
        crime = CrimeLab.get(activity).getCrime(crimeId);
        photoFile = CrimeLab.get(activity).getPhotoFile(crime);

        initializeHelperClasses();

        ActivityResultLauncher<Intent> chooseAndProcessContactFromIntent = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> chooseAndProcessContactResult(result, setSuspectsName, setSuspectsPhoneNumber)
        );

        captureImageIntentLauncherContract = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> captureImageResultContract(result, photoFile, imageBox)
        );

        requestReadContactsPermissionContract = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                result -> registerPermissionRequest(result, chooseAndProcessContactFromIntent)
        );

        dateIntentLauncherContract = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> dateIntentLauncherResultContract(result, dateTextView)
        );

    }

    private void initializeHelperClasses() {
        CrimeFragmentActivityContracts.initializeActivity(activity);
        CrimeFragmentActivityContracts.initializeCrime(crime);

        CrimeFragmentEvents.initializeCallbacks(callbacks);
        CrimeFragmentEvents.initializeActivity(activity);
        CrimeFragmentEvents.initializeCrime(crime);

        CrimeFragmentUtilities.initializeCrime(crime);
        CrimeFragmentUtilities.initializeFragment(this);

    }
    public <T extends View> T findView(@IdRes  int id) {
        return view.findViewById(id);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        CheckBox crimeIsSeriousCheckBox;
        ImageButton captureImageButton;
        CheckBox solvedCheckBox;
        Button timeButton;

        view = inflater.inflate(R.layout.fragment_crime, container, false);

        crimeIsSeriousCheckBox = findView(R.id.crime_serious_checkbox);
        EditText titleField = findView(R.id.crime_title);
        Button dateButton = findView(R.id.crime_date);
        dateTextView = findView(R.id.date_text_view);
        timeTextView = findView(R.id.time_text_view);
        solvedCheckBox = findView(R.id.crime_solved);
        timeButton = findView(R.id.crime_time);

        titleField.setText(crime.getTitle());
        titleField.addTextChangedListener(updateCrimeInstanceTextField());

        dateButton.setOnClickListener(dateButtonView -> openDialog(DATE));
        timeButton.setOnClickListener(timeButtonView -> openDialog(TIME));

        updateDate(dateTextView);
        updateTime(timeTextView);

        setUpCheckBoxesState(crimeIsSeriousCheckBox, solvedCheckBox);

        crimeIsSeriousCheckBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                toggleCrimeIsSerious(solvedCheckBox, isChecked)
        );

        solvedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                toggleCrimeSolved(crimeIsSeriousCheckBox, solvedCheckBox, isChecked)
        );

        suspectButton = findView(R.id.choose_crime_suspect_id);
        setUpSuspectButton(suspectButton);

        reportButton = findView(R.id.send_crime_report_id);
        reportButton.setOnClickListener(reportView -> attemptToReportSuspect());

        callButton = findView(R.id.call_suspect_id);
        callButton.setOnClickListener(callView -> attemptToCallSuspect());

        if (crime.getSuspectName() == null) {
            reportButton.setVisibility(View.GONE);
            callButton.setVisibility(View.GONE);
        }

        ConstraintLayout imageAndCaptureButtonBox = findView(R.id.photo_and_capture_button_box);
        imageAndCaptureButtonBox.setVisibility(photoFile != null ? View.VISIBLE : View.GONE);

        captureImageButton = findView(R.id.crime_camera);
        captureImageButton.setOnClickListener(captureView ->
            captureImage(photoFile, captureImageIntentLauncherContract)
        );

        imageBox = findView(R.id.crime_photo);
        imageBox.setOnClickListener(zoomImageView -> openDialog(ZOOM_IMAGE));

        updatePhotoView(photoFile, imageBox);

        return view;

    }

    private void setUpSuspectButton(@NonNull Button suspectButton) {
        String hasASuspectMessage = "You've set " + crime.getSuspectName() + " as your suspect using this";
        String pickASuspectMessage = getString(R.string.pick_a_suspect_from_contacts);

        boolean userHasChosenASuspect = crime.getSuspectName() != null;

        if (userHasChosenASuspect) {
            suspectButton.setText(crime.getSuspectName());
            suspectButton.setContentDescription(hasASuspectMessage);
        } else {
            suspectButton.setContentDescription(pickASuspectMessage);
        }

        suspectButton.setOnClickListener(suspectView ->
                requestReadContactsPermissionContract.launch(Manifest.permission.READ_CONTACTS)
        );
    }

    private void setUpCheckBoxesState(CheckBox crimeIsSeriousCheckBox, CheckBox solvedCheckBox) {

        String disabledCrimeIsSeriousCheckBoxMessage = getString(R.string.disable_crime_requires_intervention_message);
        String disabledSolvedCheckboxMessage = getString(R.string.disable_toggle_crime_solved_message);
        String crimeIsSeriousCheckBoxMessage = getString(R.string.crime_requires_police_introduction);
        String solvedCheckBoxMessage = getString(R.string.toggle_crime_solved_message);

        if (crime.requiresIntervention()) {
            crimeIsSeriousCheckBox.setContentDescription(crimeIsSeriousCheckBoxMessage);
            solvedCheckBox.setContentDescription(disabledSolvedCheckboxMessage);

            crimeIsSeriousCheckBox.setChecked(true);
            solvedCheckBox.setChecked(false);

            Helper.disableElement(solvedCheckBox);
        }

        if (crime.isSolved()) {
            crimeIsSeriousCheckBox.setContentDescription(disabledCrimeIsSeriousCheckBoxMessage);
            solvedCheckBox.setContentDescription(solvedCheckBoxMessage);

            crimeIsSeriousCheckBox.setChecked(false);
            solvedCheckBox.setChecked(true);

            Helper.disableElement(crimeIsSeriousCheckBox);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        CrimeLab.get(activity).updateCrime(crime);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = null;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == DELETE_BUTTON_ID) {
            if (onTablet(activity)) {
                updateCrimeListAndClearCrimeFragment();
            } else {
                CrimeLab.get(activity).deleteCrime(crime);
                activity.finish();
            }
        }

        return super.onOptionsItemSelected(item);

    }

    private void updateCrimeListAndClearCrimeFragment() {
        FragmentManager manager = activity.getSupportFragmentManager();
        Fragment crimeListFragmentLayout = manager.findFragmentById(R.id.fragment_container);
        CrimeListFragment crimeListFragment = (CrimeListFragment) crimeListFragmentLayout;

        // Updates clime list fragment and opens closest crime if not null
        if (crimeListFragment != null) {
            crimeListFragment.clearCrimeFragmentLayout(crime);
            crimeListFragment.removeCrimeAndNavigateToClosestCrime(crime);
        }
    }

    private void openDialog(@NonNull String requestedTypeOfPicker) {

        FragmentManager manager = getParentFragmentManager();

        switch (requestedTypeOfPicker) {

            case TIME:
                TimePickerFragment timeDialog = TimePickerFragment.newInstance(crime.getTime());
                manager.setFragmentResultListener(TIME, this, this::processManagerResult);
                timeDialog.show(manager, DIALOG_TIME);
                break;

            case DATE:

                if (onPhone(activity)) {
                    Intent intent = DatePickerFragmentActivity.newIntent(activity, crime.getDate());
                    dateIntentLauncherContract.launch(intent);
                    break;
                }

                DatePickerFragment dateDialog = DatePickerFragment.newInstance(crime.getDate());
                manager.setFragmentResultListener(DATE, this, this::processManagerResult);
                dateDialog.show(manager, DIALOG_DATE);
                break;

            case ZOOM_IMAGE:
                if (photoFile == null || !photoFile.exists()) break;
                ImageDisplayFragment displayImageDialog = ImageDisplayFragment.newInstance(photoFile.getPath());
                displayImageDialog.show(manager, DIALOG_IMAGE);
                break;

            default:
                break;
        }

    }

    private void processManagerResult(@NonNull String requestKey, Bundle result) {

        if (requestKey.equals(DATE)) {
            Date date = (Date) Serializable.getSerializable(result, DatePickerFragment.EXTRA_DATE);
            assert date != null;
            crime.setDate(date);
            updateCrime();
            updateDate(dateTextView);
            return;
        }

        Date time = (Date) Serializable.getSerializable(result, TimePickerFragment.EXTRA_TIME);
        assert time != null;
        crime.setTime(time);
        updateCrime();
        updateTime(timeTextView);

    }

    @SuppressLint("Range")
    private final Function<Intent, String> setSuspectsName = data -> {

        // Get the contact URI from the data
        Uri contactUri = data.getData();

        // Specify the fields to return values for
        String[] queryFields = new String[]{
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME
        };

        // Perform the query - the contactUri is like a "where" clause here
        assert contactUri != null;
        Cursor cursor = activity.getContentResolver().query(contactUri, queryFields, null, null, null);

        String contactID;

        try {

            // Double-check that you actually got results
            assert cursor != null;

            if (cursor.getCount() == 0) return "";

            // Pull out the first column of the first row of data, that is your suspect's name
            cursor.moveToFirst();

            String suspectName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            crime.setSuspectName(suspectName);
            updateCrime();
            suspectButton.setText(suspectName);
            suspectButton.setContentDescription("You've set " + suspectName + " as your suspect using this");

            contactID = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

            callButton.setVisibility(View.VISIBLE);
            reportButton.setVisibility(View.VISIBLE);

        } finally {
            // Close the cursor
            assert cursor != null;
            cursor.close();
        }

        return contactID;
    };

    @SuppressLint("Range")
    private final Consumer<String> setSuspectsPhoneNumber = contactID -> {
        // Set the contact URI to the phone content URI
        Uri contactUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

        // Set the query fields to the phone number
        String[] queryFields = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};

        // Set the query key to the lookup key
        String queryKey = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? ";

        // Set the query key value to the suspects contact lookup key
        String[] queryKeyValue = new String[]{contactID};

        // Perform the query
        Cursor cursor = activity.getContentResolver().query(contactUri, queryFields, queryKey, queryKeyValue, null);

        try {
            assert cursor != null;
            if (cursor.getCount() == 0) return;

            // Get the first row of data, which is your suspect's phone number
            cursor.moveToFirst();
            String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            crime.setSuspectNumber(number);
        } finally {
            // Close the cursor
            assert cursor != null;
            cursor.close();
        }
    };

}
