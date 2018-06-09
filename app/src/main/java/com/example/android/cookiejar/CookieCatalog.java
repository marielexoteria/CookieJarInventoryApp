package com.example.android.cookiejar;

/* Nav drawer code From https://developer.android.com/training/
 * implementing-navigation/nav-drawer#top_of_page
 * and https://medium.com/@ssaurel/implement-a-navigation-
 * drawer-with-a-toolbar-on-android-m-68162f13d220
*/

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.support.design.widget.FloatingActionButton;

//Contract class to connect to the SQLite db "cookiejar" and enable CRUD actions
import com.example.android.cookiejar.data.CookieJarContract.CookieEntry;
import com.example.android.cookiejar.data.CookieJarDbHelper;

public class CookieCatalog extends AppCompatActivity {

    //Declaring a variable that we need to enable a nav drawer
    private DrawerLayout drawerLayout;

    //Declaring an instance of the class CookieJarDbHelper so that we can enable CRUD actions
    private CookieJarDbHelper cookieJarDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cookie_catalog);

        //Attaching the variable to the nav drawer resource
        drawerLayout = findViewById(R.id.drawer_layout);

        //Setting up the nav drawer and toolbar
        configureNavigationDrawer();
        configureToolbar();


        //Setup FAB to open EditCookie
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

    /* Setting the toolbar as the action bar and the hamburger menu icon (icon_menu.xml)
        as the nav drawer button */
    private void configureToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setHomeAsUpIndicator(R.drawable.icon_menu);
        actionbar.setDisplayHomeAsUpEnabled(true);

    }

    //Setting up the nav drawer and giving each option an action
    private void configureNavigationDrawer() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        drawerLayout.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here
                        switch (menuItem.getItemId()) {

                            //Respond to a click on the menu options
                            case R.id.nav_camera:
                                Toast.makeText(CookieCatalog.this, "Go to shop/All inventory coming soon",
                                        Toast.LENGTH_SHORT).show();
                                return true;

                            case R.id.nav_gallery:
                                Intent intent = new Intent(CookieCatalog.this, EditCookie.class);
                                startActivity(intent);
                                return true;

                            case R.id.nav_share:
                                Toast.makeText(CookieCatalog.this, "Settings coming soon",
                                        Toast.LENGTH_SHORT).show();
                                return true;

                            case R.id.nav_send:
                                Toast.makeText(CookieCatalog.this, "About coming soon",
                                        Toast.LENGTH_SHORT).show();
                                return true;
                        }

                        return true;
                    }
                });
    }


    //This method will refresh the CookieCatalog with the info about the new cookie after it was inserted
    @Override
    protected void onStart() {
        super.onStart();
        displayDatabaseInfo();
    }

    /**
     * Temporary helper method to display information in the onscreen TextView about the state of
     * the cookies table.
     */
    private void displayDatabaseInfo() {
        //To access our database, we instantiate our subclass of SQLiteOpenHelper
        //and pass the context, which is the current activity
        CookieJarDbHelper tempDbHelper = new CookieJarDbHelper(this);

        // Create and/or open a database to read from it
        SQLiteDatabase db = tempDbHelper.getReadableDatabase();

        //Perform a SQL query "SELECT * FROM cookies" using Query() rather than db.rawQuery()

        //First we make a projection of the fields
        String[] cookieProjection = {
                CookieEntry._ID,
                CookieEntry.COOKIE_NAME,
                CookieEntry.COOKIE_PRICE,
                CookieEntry.COOKIE_QUANTITY,
                CookieEntry.COOKIE_TYPE,
                CookieEntry.COOKIE_SUPPLIER_NAME,
                CookieEntry.COOKIE_SUPPLIER_PHONE_NR
        };

        //Passing the projection into the query method by means of a cursor,
        //which will consist of the result from the query
        Cursor cursor = db.query(
                CookieEntry.TABLE_NAME,
                cookieProjection,
                null,
                null,
                null,
                null,
                null
        );

        TextView displayView = (TextView) findViewById(R.id.text_view_cookie);

        try {
            //Creating a header in the TextView that looks like this:
            //Number of cookies in the cookie jar: NUMBER
            //_id - name - price - quantity - type - supplier name - supplier phone number

            // In the while loop below, iterate through the rows of the cursor and display
            // the information from each column in this order.
            displayView.setText(getString(R.string.db_row_info) + cursor.getCount() + "\n\n");
            displayView.append(CookieEntry._ID + " - " +
                    CookieEntry.COOKIE_NAME + " - " +
                    CookieEntry.COOKIE_PRICE + " - " +
                    CookieEntry.COOKIE_QUANTITY + " - " +
                    CookieEntry.COOKIE_TYPE + " - " +
                    CookieEntry.COOKIE_SUPPLIER_NAME + " - " +
                    CookieEntry.COOKIE_SUPPLIER_PHONE_NR + "\n"
            );

            // Figure out the index of each column
            int idColumnIndex = cursor.getColumnIndex(CookieEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(CookieEntry.COOKIE_NAME);
            int priceColumnIndex = cursor.getColumnIndex(CookieEntry.COOKIE_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(CookieEntry.COOKIE_QUANTITY);
            int typeColumnIndex = cursor.getColumnIndex(CookieEntry.COOKIE_TYPE);
            int supplierNameColumnIndex = cursor.getColumnIndex(CookieEntry.COOKIE_SUPPLIER_NAME);
            int supplierPhoneNrColumnIndex = cursor.getColumnIndex(CookieEntry.COOKIE_SUPPLIER_PHONE_NR);

            //Looping through all the returned rows in the cursor
            while (cursor.moveToNext()) {
                //Use that index to extract the String/Double/int value of the word
                //at the current row the cursor is on.
                int currentID = cursor.getInt(idColumnIndex);
                String currentName = cursor.getString(nameColumnIndex);
                Double currentPrice = cursor.getDouble(priceColumnIndex);
                int currentQuantity = cursor.getInt(quantityColumnIndex);
                int currentType = cursor.getInt(typeColumnIndex);
                String currentSupplierName = cursor.getString(supplierNameColumnIndex);
                int currentSupplierPhoneNr = cursor.getInt(supplierPhoneNrColumnIndex);


                //Changing the type from 1/2 to Sweet/Savoury
                String type = "";
                switch(currentType) {
                    case 1:
                        type = "Sweet";
                        break;
                    case 2:
                        type = "Savoury";
                        break;
                }


                //Displaying the values from each column of the current row in the cursor in the TextView
                displayView.append(("\n" + currentID + " - " +
                        currentName + " - " + currentPrice + " - " + currentQuantity + " - "
                        + type + " - " + currentSupplierName + " - " + currentSupplierPhoneNr));
            }
        } finally {
            //Closing the cursor when done reading it to release resources and make it invalid
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

        //Checking out whether the insert command was successful or not
        Log.i("CookieCatalog", "New row ID = " + newRowId);
    }

    //Creating the overflow menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //Adding the menu items from res/menu/menu_cookie_catalog.xml to the app bar
        getMenuInflater().inflate(R.menu.menu_cookie_catalog, menu);
        return true;
    }

    //Enabling the actions to be done in the overflow menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //If the user clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {

            //Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertCookie();
                displayDatabaseInfo();
                return true;

            //Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                Toast.makeText(this, "Coming soon - Delete all entries", Toast.LENGTH_SHORT).show();
                return true;

            //Open the drawer when the hamburger menu icon is tapped
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }
}
