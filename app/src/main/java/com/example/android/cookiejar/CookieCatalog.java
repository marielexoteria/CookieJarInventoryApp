package com.example.android.cookiejar;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.support.design.widget.FloatingActionButton;

//Contract class to connect to the SQLite db "cookiejar" and enable CRUD actions
import com.example.android.cookiejar.data.CookieJarContract.CookieEntry;

import com.example.android.cookiejar.data.CookieJarDbHelper;

public class CookieCatalog extends AppCompatActivity {

    //Declaring an instance of the class CookieJarDbHelper so that we can enable CRUD actions
    private CookieJarDbHelper cookieJarDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cookie_catalog);

        // Setup FAB to open EditCookie
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CookieCatalog.this, EditCookie.class);
                startActivity(intent);
            }
        });

        //Instantiating the DB Helper variable
        cookieJarDbHelper = new CookieJarDbHelper(this);
        displayDatabaseInfo();
    }

    //This method will refresh the CookieCatalog with the info about the new cookie after it was inserted
    @Override
    protected void onStart() {
        super.onStart();
        displayDatabaseInfo();
    }

    /**
     * Temporary helper method to display information in the onscreen TextView about the state of
     * the pets database.
     */
    private void displayDatabaseInfo() {
        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        CookieJarDbHelper tempDbHelper = new CookieJarDbHelper(this);

        // Create and/or open a database to read from it
        SQLiteDatabase db = tempDbHelper.getReadableDatabase();

        // Perform this raw SQL query "SELECT * FROM pets"
        // to get a Cursor that contains all rows from the pets table.
        Cursor cursor = db.rawQuery("SELECT * FROM " + CookieEntry.TABLE_NAME, null);
        try {
            // Display the number of rows in the Cursor (which reflects the number of rows in the
            // pets table in the database).
            TextView displayView = (TextView) findViewById(R.id.text_view_cookie);
            displayView.setText(R.string.db_row_info + cursor.getCount());
        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        }
    }

    private void insertCookie() {
        //Open the db in write mode
        SQLiteDatabase db = cookieJarDbHelper.getWritableDatabase();

        //Creating a ContentValues object to insert data to the db
        ContentValues cookieValues = new ContentValues();

        //Populating the ContentValues object
        cookieValues.put(CookieEntry.COOKIE_NAME, "Ballerina");
        cookieValues.put(CookieEntry.COOKIE_PRICE, "14.50");
        cookieValues.put(CookieEntry.COOKIE_QUANTITY, "5");
        cookieValues.put(CookieEntry.COOKIE_TYPE, CookieEntry.COOKIE_TYPE_SWEET);
        cookieValues.put(CookieEntry.COOKIE_SUPPLIER_NAME, "Fazer AB");
        cookieValues.put(CookieEntry.COOKIE_SUPPLIER_PHONE_NR, "12345678");

        //Inserting the data
        long newRowId = db.insert(CookieEntry.TABLE_NAME, null, cookieValues);

        //Checking out whether the insert command was successfull or not
        Log.i("CookieCatalog", "New row ID = " + newRowId);
    }

    //Creating the overflow menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_cookie_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_cookie_catalog, menu);
        return true;
    }

    //Enabling the actions to be done in the overflow menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {

            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                //Toast.makeText(this, "Coming soon - Insert dummy data", Toast.LENGTH_SHORT).show();
                insertCookie();
                displayDatabaseInfo();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:

                Toast.makeText(this, "Coming soon - Delete all entries", Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
