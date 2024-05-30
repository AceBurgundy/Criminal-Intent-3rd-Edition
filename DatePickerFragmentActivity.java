package com.aceburgundy.criminalintent;

import android.content.Context;
import android.content.Intent;
import androidx.fragment.app.Fragment;
import java.util.Date;

public class DatePickerFragmentActivity extends SingleFragmentActivity {
    private static final String EXTRA_DATE = "com.android.criminal-intent.crime_date";

    public static Intent newIntent(Context packageContext, Date date) {
        Intent intent = new Intent(packageContext, DatePickerFragmentActivity.class);
        intent.putExtra(EXTRA_DATE, date);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        final Date date = (Date) Serializable.getSerializable(getIntent(), EXTRA_DATE);
        return DatePickerFragment.newInstance(date);
    }
}