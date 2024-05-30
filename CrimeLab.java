package com.aceburgundy.criminalintent;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.aceburgundy.criminalintent.database.CrimeBaseHelper;
import com.aceburgundy.criminalintent.database.CrimeCursorWrapper;
import com.aceburgundy.criminalintent.database.CrimeDbSchema.CrimeTable;
import com.aceburgundy.criminalintent.database.CrimeDbSchema.CrimeTable.Cols;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CrimeLab {
    private final SQLiteDatabase database;
    @SuppressLint("StaticFieldLeak")
    private static CrimeLab crimeLab;
    private final Context context;

    public static CrimeLab get(Context context) {
        if (crimeLab == null) crimeLab = new CrimeLab(context);
        return crimeLab;
    }

    private CrimeLab(Context context) {
        this.context = context.getApplicationContext();
        CrimeBaseHelper helper = new CrimeBaseHelper(context);
        database = helper.getWritableDatabase();
    }

    public void addCrime(Crime newCrime) {
        ContentValues values = getContentValues(newCrime);
        database.insert(CrimeTable.NAME, null, values);
    }

    public void deleteCrime(Crime crimeToDelete) {
        String uuidString = crimeToDelete.getId().toString();
        database.delete(CrimeTable.NAME, Cols.UUID + " = ?", new String[] { uuidString });
    }

    private CrimeCursorWrapper queryCrimes(String whereClause, String[] whereArgs) {
        Cursor cursor = database.query(
                CrimeTable.NAME,
                null, // columns - null selects all columns
                whereClause,
                whereArgs,
                null, // groupBy
                null, // having
                null  // orderBy
        );
        return new CrimeCursorWrapper(cursor);
    }

    public void updateCrime(Crime crime) {
        String uuidString = crime.getId().toString();
        ContentValues values = getContentValues(crime);
        database.update(CrimeTable.NAME, values, CrimeTable.Cols.UUID + " = ?", new String[] { uuidString });
    }

    private static ContentValues getContentValues(Crime crime) {
        ContentValues values = new ContentValues();
        values.put(Cols.REQUIRES_INTERVENTION, crime.requiresIntervention() ? 1 : 0);
        values.put(Cols.SUSPECT_NAME, crime.getSuspectName());
        values.put(Cols.SOLVED, crime.isSolved() ? 1 : 0);
        values.put(Cols.DATE, crime.getDate().getTime());
        values.put(Cols.TIME, crime.getDate().getTime());
        values.put(Cols.UUID, crime.getId().toString());
        values.put(Cols.TITLE, crime.getTitle());
        return values;
    }

    public List<Crime> getCrimes() {
        List<Crime> crimes = new ArrayList<>();
        CrimeCursorWrapper cursor = queryCrimes(null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            crimes.add(cursor.getCrime());
            cursor.moveToNext();
        }
        cursor.close();
        return crimes;
    }

    public Crime getCrime(UUID id) {
        CrimeCursorWrapper cursor = queryCrimes(Cols.UUID + " = ?", new String[] { id.toString() });
        try {
            if (cursor.getCount() == 0) return null;
            cursor.moveToFirst();
            return cursor.getCrime();
        } finally {
            cursor.close();
        }
    }

    public File getPhotoFile(Crime crime) {
        File filesDir = context.getFilesDir();
        return new File(filesDir, crime.getPhotoFilename());
    }

}