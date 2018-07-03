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
import android.util.Log;

//Contract class to connect to the SQLite db "cookiejar" and enable CRUD actions
import com.example.android.cookiejar.R;
import com.example.android.cookiejar.data.CookieJarContract.CookieEntry;

import static android.provider.Settings.Global.getString;

/**
 * {@link ContentProvider} for the Cookie Jar Inventory app.
 */

public class CookieJarProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = CookieJarProvider.class.getSimpleName();

    //Creating an object of type CookieJarDbHelper (database helper)
    private CookieJarDbHelper cookieDbHelper;

    /**
     * URI matcher code for the content URI for the cookies table (as per the CookieJarContract)
     */
    private static final int COOKIES = 100;

    /**
     * URI matcher code for the content URI for a single cookie in the cookies table
     * (as per the CookieJarContract)
     */
    private static final int COOKIE_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    //Static initializer. This is run the first time anything is called from this class.
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

    //Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        //Get readable database
        SQLiteDatabase database = cookieDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        //Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case COOKIES:
                /* For the COOKIES code, query the cookiejar table directly with the given
                 * projection, selection, selection arguments, and sort order. The cursor
                 * could contain multiple rows of the cookiejar table.
                 */
                cursor = database.query(CookieEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case COOKIE_ID:
                /* For the COOKIE_ID code, extract out the ID from the URI.
                 * For an example URI such as "content://com.example.android.cookiejar/cookies/3",
                 * the selection will be "_id=?" and the selection argument will be a
                 * String array containing the actual ID of 3 in this case.
                 * For every "?" in the selection, we need to have an element in the selection
                 * arguments that will fill in the "?". Since we have 1 question mark in the
                 * selection, we have 1 String in the selection arguments' String array.
                 */

                selection = CookieEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                /* This will perform a query on the cookie table where the _id equals 3 to return a
                 * Cursor containing that row of the table.
                 */
                cursor = database.query(CookieEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        /* Set notification URI on the Cursor, so we know what content URI the cursor was
         * created for. If the data at this URI changes, then we know we need to update
         * the cursor.
         */

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        //Return the cursor
        return cursor;
    }

    //Insert new data into the provider with the given ContentValues.
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

        /* Beginning: data validation - checking that the fields are not null.
         * Code issues solved thanks to the recommendation from @cbp on the Udacity forums:
         * https://discussions.udacity.com/t/app-crashes-upon-sanity-check/814508/8
         */

        String cookieName = contentValues.getAsString(CookieEntry.COOKIE_NAME);
        if (cookieName == null) {
            throw new IllegalArgumentException("The cookie needs a name. Please enter one and try again :)");
        }

        String cookieDescription = contentValues.getAsString(CookieEntry.COOKIE_DESCRIPTION);
        if (cookieDescription == null) {
            throw new IllegalArgumentException("The cookie needs a description. Please enter one and try again :)");
        }

        String cookiePrice = contentValues.getAsString(CookieEntry.COOKIE_PRICE);
        if (cookiePrice == null) {
            throw new IllegalArgumentException("The cookie needs a price. Please enter one and try again :)");
        }

        String cookieQuantity = contentValues.getAsString(CookieEntry.COOKIE_QUANTITY);
        if (cookieQuantity == null) {
            throw new IllegalArgumentException("The cookie needs a quantity in stock. Please enter one and try again :)");
        }

        String cookieType = contentValues.getAsString(CookieEntry.COOKIE_TYPE);
        if (cookieType == null) {
            throw new IllegalArgumentException("The cookie needs a type and will be sweet by default :)");
        }

        String supplierName = contentValues.getAsString(CookieEntry.COOKIE_SUPPLIER_NAME);
        if (supplierName == null) {
            throw new IllegalArgumentException("The cookie needs a supplier name. Please enter one and try again :)");
        }

        String supplierPhoneNr = contentValues.getAsString(CookieEntry.COOKIE_PRICE);
        if (supplierPhoneNr == null) {
            throw new IllegalArgumentException("The cookie needs a supplier phone number. Please enter one and try again :)");
        }

        String supplierEmail = contentValues.getAsString(CookieEntry.COOKIE_SUPPLIER_EMAIL);
        if (supplierEmail == null) {
            throw new IllegalArgumentException("The cookie needs a supplier e-mail. Please enter one and try again :)");
        }
        /* End: data validation */

        //Get writeable database
        SQLiteDatabase database = cookieDbHelper.getWritableDatabase();

        //Insert the new cookie with the given values
        long id = database.insert(CookieEntry.TABLE_NAME, null, contentValues);

        //If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Cannot insert row for " + uri);
            return null;
        }

        /*Notify all listeners that the data has changed for the cookie content URI
         * uri: content://com.example.android.cookiejar/cookiejar
         */
        getContext().getContentResolver().notifyChange(uri, null);


        /* Once we know the ID of the new row in the table,
         * return the new URI with the ID appended to the end of it
         */
        return ContentUris.withAppendedId(uri, id);
    }

    //Delete the data at the given selection and selection arguments.
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues,
                      @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case COOKIES:
                return updateCookie(uri, contentValues, selection, selectionArgs);
            case COOKIE_ID:
                /* For the COOKIE_ID code, extract out the ID from the URI,
                 * so we know which row to update. Selection will be "_id=?" and selection
                 * arguments will be a String array containing the actual ID.
                 */
                selection = CookieEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateCookie(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update the cookies in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more cookies).
     * Return the number of rows that were successfully updated.
     */
    private int updateCookie(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        /* If there are no values to update, then don't try to update the database.
         * Moving this IF-statement to the beginning as per a suggestion from jlin26 on
         * https://github.com/udacity/ud845-Pets/commit/a1bcc0c35b19ee0c5b85ba295312e439bcf74bd8
         */
        if (contentValues.size() == 0) {
            return 0;
        }
        /* Beginning: data validation - checking that the fields are not null.
         * Code issues solved thanks to the recommendation from @cbp on the Udacity forums:
         * https://discussions.udacity.com/t/app-crashes-upon-sanity-check/814508/8
         * Each IF-statement will run the code inside if the key CookieEntry.KEY_NAME are present
         */

        if (contentValues.containsKey(CookieEntry.COOKIE_NAME)) {
            String cookieName = contentValues.getAsString(CookieEntry.COOKIE_NAME);
            if (cookieName == null) {
                throw new IllegalArgumentException("The cookie needs a name. Please enter one and try again :)");
            }
        }

        if (contentValues.containsKey(CookieEntry.COOKIE_DESCRIPTION)) {
            String cookieDescription = contentValues.getAsString(CookieEntry.COOKIE_DESCRIPTION);
            if (cookieDescription == null) {
                throw new IllegalArgumentException("The cookie needs a description. Please enter one and try again :)");
            }
        }

        if (contentValues.containsKey(CookieEntry.COOKIE_PRICE)) {
            String cookiePrice = contentValues.getAsString(CookieEntry.COOKIE_PRICE);
            if (cookiePrice == null) {
                throw new IllegalArgumentException("The cookie needs a price. Please enter one and try again :)");
            }
        }

        if (contentValues.containsKey(CookieEntry.COOKIE_PRICE)) {
            String cookiePrice = contentValues.getAsString(CookieEntry.COOKIE_PRICE);
            if (cookiePrice == null) {
                throw new IllegalArgumentException("The cookie needs a price. Please enter one and try again :)");
            }
        }

        if (contentValues.containsKey(CookieEntry.COOKIE_QUANTITY)) {
            String cookieQuantity = contentValues.getAsString(CookieEntry.COOKIE_QUANTITY);
            if (cookieQuantity == null) {
                throw new IllegalArgumentException("The cookie needs a quantity in stock. Please enter one and try again :)");
            }
        }

        if (contentValues.containsKey(CookieEntry.COOKIE_TYPE)) {
            String cookieType = contentValues.getAsString(CookieEntry.COOKIE_TYPE);
            if (cookieType == null) {
                throw new IllegalArgumentException("The cookie needs a type and will be sweet by default :)");
            }
        }

        if (contentValues.containsKey(CookieEntry.COOKIE_SUPPLIER_NAME)) {
            String supplierName = contentValues.getAsString(CookieEntry.COOKIE_SUPPLIER_NAME);
            if (supplierName == null) {
                throw new IllegalArgumentException("The cookie needs a supplier name. Please enter one and try again :)");
            }
        }

        if (contentValues.containsKey(CookieEntry.COOKIE_SUPPLIER_PHONE_NR)) {
            String supplierPhoneNr = contentValues.getAsString(CookieEntry.COOKIE_PRICE);
            if (supplierPhoneNr == null) {
                throw new IllegalArgumentException("The cookie needs a supplier phone number. Please enter one and try again :)");
            }
        }

        if (contentValues.containsKey(CookieEntry.COOKIE_SUPPLIER_EMAIL)) {
            String supplierEmail = contentValues.getAsString(CookieEntry.COOKIE_SUPPLIER_EMAIL);
            if (supplierEmail == null) {
                throw new IllegalArgumentException("The cookie needs a supplier e-mail. Please enter one and try again :)");
            }
        }
        /* End: data validation */

        //Otherwise, get writeable database to update the data
        SQLiteDatabase database = cookieDbHelper.getWritableDatabase();

        //Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(CookieEntry.TABLE_NAME, contentValues, selection, selectionArgs);

        //If 1 or more rows were updated, then notify all listeners that the data at the given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        //Return the number of rows updated
        return rowsUpdated;

    }


    //Updates the data at the given selection and selection arguments, with the new ContentValues.
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        //Get writeable database
        SQLiteDatabase database = cookieDbHelper.getWritableDatabase();

        //Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case COOKIES:
                //Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(CookieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case COOKIE_ID:

                //Delete a single row given by the ID in the URI
                selection = CookieEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(CookieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        //If 1 or more rows were deleted, then notify all listeners that the data at the given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        //Return the number of rows deleted
        return rowsDeleted;
    }


    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case COOKIES:
                return CookieEntry.CONTENT_LIST_TYPE;
            case COOKIE_ID:
                return CookieEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
