package com.example.android.cookiejar.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

//Contract class to connect to the SQLite db "cookiejar" and enable CRUD actions
import com.example.android.cookiejar.data.CookieJarContract.CookieEntry;

public class CookieJarDbHelper extends SQLiteOpenHelper {

    //Name of the database
    private final static String DATABASE_NAME = "cookiejar.db";

    //Database version. If you change the db schema, you must increment the db version
    private final static int DATABASE_VERSION = 1;

    //Creating the constructor
    public CookieJarDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //Add the field for the picture later

    @Override
    public void onCreate(SQLiteDatabase cookieDB) {

        //String to create the table using the constants from the PetContract class
        String SQL_CREATE_COOKIE_TABLE = "CREATE TABLE " + CookieEntry.TABLE_NAME + "("
                + CookieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + CookieEntry.COOKIE_NAME + " TEXT NOT NULL, "
                + CookieEntry.COOKIE_DESCRIPTION + " TEXT NOT NULL, "
                + CookieEntry.COOKIE_PRICE + " REAL NOT NULL,"
                + CookieEntry.COOKIE_QUANTITY + " INTEGER NOT NULL, "
                + CookieEntry.COOKIE_TYPE + " INTEGER NOT NULL DEFAULT 1, "
                + CookieEntry.COOKIE_SUPPLIER_NAME + " TEXT NOT NULL, "
                + CookieEntry.COOKIE_SUPPLIER_PHONE_NR+ " INTEGER NOT NULL, "
                + CookieEntry.COOKIE_SUPPLIER_EMAIL + " TEXT NOT NULL)";

        //Create the table
        cookieDB.execSQL(SQL_CREATE_COOKIE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
