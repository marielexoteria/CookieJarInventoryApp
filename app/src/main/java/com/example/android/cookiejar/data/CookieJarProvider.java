package com.example.android.cookiejar.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

//Contract class to connect to the SQLite db "cookiejar" and enable CRUD actions
import com.example.android.cookiejar.R;
import com.example.android.cookiejar.data.CookieJarContract.CookieEntry;

import static android.provider.Settings.Global.getString;

/**
 * {@link ContentProvider} for the Cookie Jar Inventory app.
 */

public class CookieJarProvider extends ContentProvider {

    /** Tag for the log messages */
    public static final String LOG_TAG = CookieJarProvider.class.getSimpleName();

    //Creating an object of type CookieJarDbHelper (database helper)
    private CookieJarDbHelper cookieDbHelper;

    /** URI matcher code for the content URI for the pets table */
    private static final int COOKIES = 100;

    /** URI matcher code for the content URI for a single pet in the pets table */
    private static final int COOKIE_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        sUriMatcher.addURI(CookieJarContract.CONTENT_AUTHORITY, CookieJarContract.PATH_COOKIEJAR, COOKIES);
        sUriMatcher.addURI(CookieJarContract.CONTENT_AUTHORITY, CookieJarContract.PATH_COOKIEJAR + "/#", COOKIE_ID);
    }

    @Override
    public boolean onCreate() {
        //Initializing the database helper object
        cookieDbHelper = new CookieJarDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        // Get readable database
        SQLiteDatabase database = cookieDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case COOKIES:
                // For the COOKIES code, query the cookiejar table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the cookiejar table.
                cursor = database.query(CookieEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case COOKIE_ID:
                // For the COOKIE_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.cookiejar/cookiejar/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = CookieEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(CookieEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case COOKIES:
                return insertCookie(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a cookie into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertCookie(Uri uri, ContentValues contentValues) {

        //Beginning: adding data validation
        String cookieName = contentValues.getAsString(CookieEntry.COOKIE_NAME);
        if (cookieName == null || cookieName.isEmpty() ) {
            throw new IllegalArgumentException("The cookie needs a name. Please enter one and try again :)");
            //Toast.makeText(this, R.string.cookie_name_empty, Toast.LENGTH_SHORT).show();
        }

        String cookieDescription = contentValues.getAsString(CookieEntry.COOKIE_DESCRIPTION);
        if (cookieDescription == null || cookieDescription.isEmpty() ) {
            throw new IllegalArgumentException("The cookie needs a description. Please enter one and try again :)");
        }

        Integer cookiePrice = contentValues.getAsInteger(CookieEntry.COOKIE_PRICE);
        if (cookiePrice == null || cookiePrice <= 0) {
            throw new IllegalArgumentException("The cookie needs a price. Please enter one and try again :)");
        }

        Integer cookieQuantity = contentValues.getAsInteger(CookieEntry.COOKIE_QUANTITY);
        if (cookieQuantity == null || cookieQuantity <= 0) {
            throw new IllegalArgumentException("The cookie needs a quantity in stock. Please enter one and try again :)");
        }

        Integer cookieType = contentValues.getAsInteger(CookieEntry.COOKIE_TYPE);
        if (cookieType == null || !CookieEntry.whichCookieType(cookieType)) {
            throw new IllegalArgumentException("Cookie is sweet by default");
        }

        String cookieSupplierName = contentValues.getAsString(CookieEntry.COOKIE_SUPPLIER_NAME);
        if (cookieSupplierName == null || cookieSupplierName.isEmpty() ) {
            throw new IllegalArgumentException("The cookie needs a supplier name. Please enter one and try again :)");
        }

        Integer cookieSupplierPhoneNr = contentValues.getAsInteger(CookieEntry.COOKIE_SUPPLIER_PHONE_NR);
        if (cookieSupplierPhoneNr == null || cookieSupplierPhoneNr <= 0) {
            throw new IllegalArgumentException("The cookie needs a supplier phone number. Please enter one and try again :)");
        }

        String cookieSupplierEmail = contentValues.getAsString(CookieEntry.COOKIE_SUPPLIER_EMAIL);
        if (cookieSupplierEmail == null || cookieSupplierEmail.isEmpty() ) {
            throw new IllegalArgumentException("The cookie needs a supplier e-mail. Please enter one and try again :)");
        }
        //End: adding data validation

        // Get writeable database
        SQLiteDatabase database = cookieDbHelper.getWritableDatabase();

        // Insert the new pet with the given values
        long id = database.insert(CookieEntry.TABLE_NAME, null, contentValues);

        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Cannot insert row for " + uri);
            return null;
        }

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues,
                      @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }
}
