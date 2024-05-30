package com.aceburgundy.criminalintent;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class TimePickerFragment extends DialogFragment {
    private static final String ARG_TIME = "TIME";
    public static final String EXTRA_TIME = "com.enfranchiser.android.criminal-intent.time";
    @NonNull
    public static TimePickerFragment newInstance(Date time) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_TIME, time);
        TimePickerFragment fragment = new TimePickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        TimePicker timePicker;

        assert getArguments() != null;
        final Date time = (Date) Serializable.getSerializable(getArguments(), ARG_TIME);

        Calendar calendar = Calendar.getInstance();

        assert time != null;

        calendar.setTime(time);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        View view = LayoutInflater.from(requireActivity()).inflate(R.layout.dialog_time, null);

        timePicker = view.findViewById(R.id.dialog_time_picker);
        timePicker.setHour(hour);
        timePicker.setMinute(minute);
        timePicker.setIs24HourView(false);

        return new AlertDialog.Builder(requireActivity())
            .setView(view)
            .setTitle(R.string.time_picker_title)
            .setPositiveButton(android.R.string.ok,
                (dialog, which) -> {
                    int alertHour = timePicker.getHour();
                    int alertMinute = timePicker.getMinute();

                    Calendar alertCalendar = Calendar.getInstance();

                    alertCalendar.setTime(time);
                    alertCalendar.set(Calendar.HOUR_OF_DAY, alertHour);
                    alertCalendar.set(Calendar.MINUTE, alertMinute);

                    Date newTime = alertCalendar.getTime();

                    sendResult(newTime);
                })
            .create();

    }

    private void sendResult(Date date) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_TIME, date);
        getParentFragmentManager().setFragmentResult(ARG_TIME, Objects.requireNonNull(intent.getExtras()));
    }

}
