package com.udacity.stockhawk.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.udacity.stockhawk.data.Contract.Quote;

import timber.log.Timber;


class DbHelper extends SQLiteOpenHelper {


    private static final String NAME = "StockHawk.db";
    private static final int VERSION = 4;


    DbHelper(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String builder = "CREATE TABLE " + Quote.TABLE_NAME + " ("
                + Quote._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Quote.COLUMN_NAME + " TEXT NOT NULL, "
                + Quote.COLUMN_SYMBOL + " TEXT NOT NULL, "
                + Quote.COLUMN_PRICE + " REAL NOT NULL, "
                + Quote.COLUMN_ABSOLUTE_CHANGE + " REAL NOT NULL, "
                + Quote.COLUMN_PERCENTAGE_CHANGE + " REAL NOT NULL, "
                + Quote.COLUMN_HISTORY + " TEXT NOT NULL, "
                + Quote.COLUMN_DAY_HIGH + " REAL NOT NULL, "
                + Quote.COLUMN_DAY_LOW + " REAL NOT NULL, "
                + "UNIQUE (" + Quote.COLUMN_SYMBOL + ") ON CONFLICT REPLACE);";

        db.execSQL(builder);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL(" DROP TABLE IF EXISTS " + Quote.TABLE_NAME);
        Timber.d("Dropping Existing Table");
        onCreate(db);
    }
}
