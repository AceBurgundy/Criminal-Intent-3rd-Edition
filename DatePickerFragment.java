package com.aceburgundy.criminalintent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.aceburgundy.criminalintent.utilities.Helper;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DatePickerFragment extends DialogFragment {
    private static final String ARG_DATE = "DATE";
    public static final String EXTRA_DATE = "com.enfranchiser.android.criminal-intent.date";

    public static DatePickerFragment newInstance(Date date) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_DATE, date);
        DatePickerFragment fragment = new DatePickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        DatePicker datePicker;
        Button applyButton;

        assert getArguments() != null;
        Date date = (Date) Serializable.getSerializable(getArguments(), ARG_DATE);

        Calendar calendar = Calendar.getInstance();

        assert date != null;

        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_date, null);

        datePicker = view.findViewById(R.id.dialog_date_picker);
        datePicker.init(year, month, day, null);

        applyButton = view.findViewById(R.id.dialog_date_apply);
        applyButton.setOnClickListener(applyView -> {

            Date applyDate = new GregorianCalendar(
                datePicker.getYear(),
                datePicker.getMonth(),
                datePicker.getDayOfMonth()

            ).getTime();

            sendResult(applyDate);
            dismiss();

        });

        return view;

    }

    private void sendResult(Date date) {

        boolean onTablet = Helper.onTablet(requireActivity());

        if (onTablet) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(EXTRA_DATE, date);
            getParentFragmentManager().setFragmentResult("DATE", bundle);
        } else {
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DATE, date);
            requireActivity().setResult(Activity.RESULT_OK, intent);
            requireActivity().finish();
        }

    }

}
