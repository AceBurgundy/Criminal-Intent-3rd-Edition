package com.aceburgundy.criminalintent.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.aceburgundy.criminalintent.Crime;
import com.aceburgundy.criminalintent.database.CrimeDbSchema.CrimeTable.Cols;

import java.util.Date;
import java.util.UUID;

public class CrimeCursorWrapper extends CursorWrapper {
    public CrimeCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Crime getCrime() {
        int requiresIntervention = getInt(getColumnIndex(Cols.REQUIRES_INTERVENTION));
        String suspectNumber = getString(getColumnIndex(Cols.SUSPECT_NUMBER));
        String suspectName = getString(getColumnIndex(Cols.SUSPECT_NAME));
        String uuidString = getString(getColumnIndex(Cols.UUID));
        String title = getString(getColumnIndex(Cols.TITLE));
        int isSolved = getInt(getColumnIndex(Cols.SOLVED));
        long date = getLong(getColumnIndex(Cols.DATE));
        long time = getLong(getColumnIndex(Cols.TIME));

        Crime crime = new Crime(UUID.fromString(uuidString));
        crime.setRequiresIntervention(requiresIntervention != 0);
        crime.setSuspectNumber(suspectNumber);
        crime.setSuspectName(suspectName);
        crime.setSolved(isSolved != 0);
        crime.setDate(new Date(date));
        crime.setTime(new Date(time));
        crime.setTitle(title);

        return crime;
    }

}
