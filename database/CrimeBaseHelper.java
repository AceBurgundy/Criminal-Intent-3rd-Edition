package com.aceburgundy.criminalintent.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.aceburgundy.criminalintent.database.CrimeDbSchema.CrimeTable.Cols;
public class CrimeBaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "crimeBase.db";
    private static final int VERSION = 1;
    public CrimeBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + CrimeDbSchema.CrimeTable.NAME + "(" +
                " _id integer primary key autoincrement, " +
                Cols.REQUIRES_INTERVENTION + ", " +
                Cols.SUSPECT_NUMBER + ", " +
                Cols.SUSPECT_NAME + ", " +
                Cols.TITLE + ", " +
                Cols.UUID + ", " +
                Cols.DATE + ", " +
                Cols.TIME + ", " +
                Cols.SOLVED +
            ")"
        );
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // document why this method is empty
    }
}