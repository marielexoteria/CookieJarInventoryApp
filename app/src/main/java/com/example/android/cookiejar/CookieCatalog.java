package com.example.android.cookiejar;

/* Nav drawer code From https://developer.android.com/training/
 * implementing-navigation/nav-drawer#top_of_page
 * and https://medium.com/@ssaurel/implement-a-navigation-
 * drawer-with-a-toolbar-on-android-m-68162f13d220
 */

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.AdapterView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import android.support.design.widget.FloatingActionButton;


//Contract class to connect to the SQLite db "cookiejar" and enable CRUD actions
import com.example.android.cookiejar.data.CookieJarContract.CookieEntry;

public class CookieCatalog extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    //Declaring a variable that we need to enable a nav drawer
    private DrawerLayout drawerLayout;

    //Declaring a constant to use with the CursorLoader
    private static final int COOKIE_LOADER = 0;

    //Creating an instance of the Adapter object to be used on the ListView
    CookieCursorAdapter cookieCursorAdapter;

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

        //Find the ListView which will be populated with the cookie data
        ListView cookieListView = (ListView) findViewById(R.id.cookie_list_view);

        //Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_state_no_data_layout);
        cookieListView.setEmptyView(emptyView);

        /* Setup an Adapter to create a list item for each row of the cookie data in the Cursor.
         * There is no cookie data yet (until the loader finishes) so pass in null for the Cursor.
         */
        cookieCursorAdapter = new CookieCursorAdapter(this, null);
        cookieListView.setAdapter(cookieCursorAdapter);

        //Setting up item click listener
        cookieListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            //int position = position of the item in the ListView (which here is sent via the variable adapterView)
            //long id = id of the item in the ListView
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //Create new intent to go to {@link CookieDetails}
                Intent intent = new Intent(CookieCatalog.this, CookieDetails.class);

                /* Form the content URI that represents the specific cookie that was clicked on
                 * by appending the "id" (passed as input to this method) onto the
                 * {@link CookieEntry#CONTENT_URI}. For example, if cookie with id of 2 would be clicked on
                 * the URI would be "content://com.example.android.cookiejar/cookies/2"
                 */
                Uri currentCookieUri = ContentUris.withAppendedId(CookieEntry.CONTENT_URI, id);

                //Set the data URI on the data field of the intent
                intent.setData(currentCookieUri);

                //Launch the {@link EditorActivity} to display the data for the current cookie
                startActivity(intent);
            }
        });

        //Kick off the loader
        getLoaderManager().initLoader(COOKIE_LOADER, null, this);
    }

    /* Setting the toolbar as the action bar and the hamburger menu icon (icon_menu.xml)
     * as the nav drawer button
     */
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
                        //Set item as selected to persist highlight
                        menuItem.setChecked(true);

                        //Close drawer when item is tapped
                        drawerLayout.closeDrawers();

                        //Update the UI based on the item selected.
                        switch (menuItem.getItemId()) {

                            //Respond to a click on the menu options
                            case R.id.nav_catalog:
                                Intent intentCatalog = new Intent(CookieCatalog.this, CookieCatalog.class);
                                startActivity(intentCatalog);
                                return true;

                            case R.id.nav_add_cookie:
                                Intent intentAddCookie = new Intent(CookieCatalog.this, EditCookie.class);
                                startActivity(intentAddCookie);
                                return true;

                            case R.id.nav_about:
                                Intent intentAbout = new Intent(CookieCatalog.this, About.class);
                                startActivity(intentAbout);
                                return true;
                        }

                        return true;
                    }
                });
    }


    //Helper method to insert hard-coded data into the db
    private void insertCookie() {
        //Creating a ContentValues object to insert data to the db
        ContentValues cookieValues = new ContentValues();

        //Populating the ContentValues object
        cookieValues.put(CookieEntry.COOKIE_NAME, "Ballerina");
        cookieValues.put(CookieEntry.COOKIE_DESCRIPTION, "Delicious cookie to have with your afternoon coffee.");
        cookieValues.put(CookieEntry.COOKIE_PRICE, "14.50");
        cookieValues.put(CookieEntry.COOKIE_QUANTITY, "5");
        cookieValues.put(CookieEntry.COOKIE_TYPE, CookieEntry.COOKIE_TYPE_SWEET);
        cookieValues.put(CookieEntry.COOKIE_SUPPLIER_NAME, "Fazer AB");
        cookieValues.put(CookieEntry.COOKIE_SUPPLIER_PHONE_NR, "12345678");
        cookieValues.put(CookieEntry.COOKIE_SUPPLIER_EMAIL, "yo@yo.com");

        /* Insert a new row for a cookie into the provider using the ContentResolver.
         * Use the {@link CookieEntry#CONTENT_URI} to indicate that we want to insert
         * into the cookiejar database table.
         * Receive the new content URI that will allow us to access the cookie data in the future.
         */
        Uri newUri = getContentResolver().insert(CookieEntry.CONTENT_URI, cookieValues);

        //Toast message "Cookie saved"
        Toast.makeText(this, getString(R.string.dummy_data_saved_in_db) , Toast.LENGTH_SHORT).show();
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

            //Respond to a click on the "Insert demo cookie" menu option
            case R.id.action_insert_demo_cookie:
                insertCookie();
                return true;

            //Respond to a click on the "Delete all cookies" menu option
            case R.id.action_delete_all_cookies:
                //Asking the user to confirm the deletion
                showDeleteConfirmationDialog();
                return true;

            //Open the drawer when the hamburger menu icon is tapped
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        //Define a projection that specifies the columns that are needed for the ListView
        String[] cookieProjection = {
                CookieEntry._ID,
                CookieEntry.COOKIE_NAME,
                CookieEntry.COOKIE_DESCRIPTION,
                CookieEntry.COOKIE_PRICE,
                CookieEntry.COOKIE_QUANTITY,
                CookieEntry.COOKIE_TYPE
        };

        //This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,  //Parent activity context
                CookieEntry.CONTENT_URI,       //Provider content URI to query
                cookieProjection,              //Columns to include in the resulting cursor
                null,                 //No selection clause
                null,             //No selection arguments
                null                 //Default sort order

        );
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        //Update {@Link CookieCursorAdapter} with the new cursor containing updated cookie data
        cookieCursorAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        //Callback called when the data needs to be deleted
        cookieCursorAdapter.swapCursor(null);

    }

    //Helper method to delete all cookies in the database.
    private void deleteAllCookies() {
        int rowsDeleted = getContentResolver().delete(CookieEntry.CONTENT_URI, null, null);
        Log.v("CookieCatalog", "Cookie deleted: " + rowsDeleted);
    }

    //Prompt the user to confirm that they want to delete all the cookies.
    private void showDeleteConfirmationDialog() {
        /* Create an AlertDialog.Builder and set the message, and click listeners
         * for the positive and negative buttons on the dialog.
         */
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogStyle);
        builder.setMessage(R.string.delete_dialog_all_cookies);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //The user clicked the "Delete" button, so delete all the cookies
                deleteAllCookies();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                /* User clicked the "Cancel" button, so dismiss the dialog
                 *  and continue editing the cookie.
                 */
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        //Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        /* Workaround until I find a better solution. Setting width and height for the dialog box
         * because I want to style the text (otherwise the question and the CTA buttons have the
         * same color and it doesn't provide a good user experience.
         * Answer from: https://stackoverflow.com/questions/4406804/how-to-control-the
         * -width-and-height-of-the-default-alert-dialog-in-android
         */
        alertDialog.getWindow().setLayout(550, 200);
        alertDialog.show();
    }

}
