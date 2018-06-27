package com.example.android.cookiejar;

/* Toolbar implementation code taken from https://medium.com/@ssaurel/implement-a-navigation-
 drawer-with-a-toolbar-on-android-m-68162f13d220 */

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

//Contract class to connect to the SQLite db "cookiejar" and enable CRUD actions
import com.example.android.cookiejar.data.CookieJarContract.CookieEntry;
import com.example.android.cookiejar.data.CookieJarDbHelper;

import static com.example.android.cookiejar.data.CookieJarContract.CookieEntry.COOKIE_TYPE_SWEET;

/**
 * Allows user to add and edit cookies.
 */
public class EditCookie extends AppCompatActivity {

    /** Field to enter the name of the cookie */
    private EditText cookieNameEditText;

    /** Field to enter the description of the cookie */
    private EditText cookieDescriptionEditText;

    /** Field to enter the price of the cookie */
    private EditText cookiePriceEditText;

    /** Field to enter the quantity of the cookie */
    private EditText cookieQuantityEditText;

    /** Dropdown to choose the type of the cookie (sweet or savoury) */
    private Spinner cookieTypeSpinner;

    /** Field to enter the supplier name of the cookie */
    private EditText cookieSupplierNameEditText;

    /** Field to enter the phone number of the supplier of the cookie */
    private EditText cookieSupplierPhoneNrEditText;

    /** Field to enter the e-mail of the supplier of the cookie */
    private EditText cookieSupplierEmailEditText;

    /**
     * Type of cookie: 1 = sweet, 2 = savoury
     */
    private int cookieType = 0;

    //Variables needed to extract the quantity of cookies and for the increment/decrement methods
    String quantityString;
    int quantity = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_cookie);

        /* Setting up the toolbar with the icons to save, go back to the previous activity and
        the overflow menu */
        configureToolbar();

        //Find all relevant views that we will need to read user input from
        cookieNameEditText = (EditText) findViewById(R.id.edit_cookie_name);
        cookieDescriptionEditText = (EditText) findViewById(R.id.edit_cookie_description);
        cookiePriceEditText = (EditText) findViewById(R.id.edit_cookie_price);
        cookieQuantityEditText = (EditText) findViewById(R.id.edit_cookie_quantity);
        cookieSupplierNameEditText = (EditText) findViewById(R.id.edit_cookie_supplier_name);
        cookieSupplierPhoneNrEditText = (EditText) findViewById(R.id.edit_cookie_supplier_phone);
        cookieSupplierEmailEditText = (EditText) findViewById(R.id.edit_cookie_supplier_email);
        cookieTypeSpinner = (Spinner) findViewById(R.id.spinner_cookie_type);

        setupSpinner();
    }

    /* Setting the toolbar as the action bar and the UP navigation icon (icon_arrow_back.xml)
        as the nav drawer button */
    private void configureToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setHomeAsUpIndicator(R.drawable.icon_arrow_back);
        actionbar.setDisplayHomeAsUpEnabled(true);
    }


    /**
     * Setup the dropdown spinner that allows the user to select the type of cookie
     */
    private void setupSpinner() {
        //Create adapter for spinner. The list options are from the String array it will use
        //and the spinner will use the default layout
        ArrayAdapter cookieTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_cookie_options, android.R.layout.simple_spinner_item);

        //Specify dropdown layout style - simple list view with 1 item per line
        cookieTypeAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        //Apply the adapter to the spinner
        cookieTypeSpinner.setAdapter(cookieTypeAdapter);

        //Set the integer mSelected to the constant values
        cookieTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.cookie_type_sweet))) {
                        cookieType = COOKIE_TYPE_SWEET;
                    } else {
                        cookieType = CookieEntry.COOKIE_TYPE_SAVOURY;
                    }
                } else {
                    cookieType = COOKIE_TYPE_SWEET;
                }
            }

            //Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                cookieType = 1; // Sweet
            }
        });
    }

    /*
     * Get user input from editor and save the new cookie into the db
     */
    private void insertCookie() {
        //Reading from the form on activity_edit_cookie.xml

        //trim() removes any white space at the beginning or the end of the input data
        String nameString = cookieNameEditText.getText().toString().trim();
        String descriptionString = cookieDescriptionEditText.getText().toString().trim();
        String priceString = cookiePriceEditText.getText().toString().trim();
        quantityString = cookieQuantityEditText.getText().toString();
        //String typeString = cookieTypeSpinner.toString();
        String supplierNameString = cookieSupplierNameEditText.getText().toString().trim();
        String supplierPhoneString = cookieSupplierPhoneNrEditText.getText().toString().trim();
        String supplierEmailString = cookieSupplierEmailEditText.getText().toString().trim();

        //Declaring variables that will be needed to insert data to the db
        Double price = 0.00;
        int phoneNumber = 0;

        //Converting values to their data types
        price = Double.parseDouble(priceString);
        phoneNumber = Integer.parseInt(supplierPhoneString);
        quantity = Integer.parseInt(quantityString);

        //Creating a ContentValues object to insert data to the db
        ContentValues cookieValues = new ContentValues();

        //Populating the ContentValues object
        cookieValues.put(CookieEntry.COOKIE_NAME, nameString);
        cookieValues.put(CookieEntry.COOKIE_DESCRIPTION, descriptionString);
        cookieValues.put(CookieEntry.COOKIE_PRICE, price);
        cookieValues.put(CookieEntry.COOKIE_QUANTITY, quantity);
        cookieValues.put(CookieEntry.COOKIE_TYPE, cookieType);
        cookieValues.put(CookieEntry.COOKIE_SUPPLIER_NAME, supplierNameString);
        cookieValues.put(CookieEntry.COOKIE_SUPPLIER_PHONE_NR, phoneNumber);
        cookieValues.put(CookieEntry.COOKIE_SUPPLIER_EMAIL, supplierEmailString);

        // Insert a new cookie into the provider, returning the content URI for the new pet.
        Uri newUri = getContentResolver().insert(CookieEntry.CONTENT_URI, cookieValues);

        //Displaying a toast to inform the user if adding the cookie was successful or not
        if (newUri == null) {
            //There was an error with adding the cookie
            Toast.makeText(this, getString(R.string.editor_insert_cookie_failed)
                    + nameString, Toast.LENGTH_SHORT).show();
        } else {
            //Adding the cookie was successful
            Toast.makeText(this, getString(R.string.editor_insert_cookie_successful)
                    + nameString, Toast.LENGTH_SHORT).show();
        }

        //Logging the newRowId for error checking
        Log.i("EditCookie", "New phone number: " + phoneNumber);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //Adding the menu items from res/menu/menu_edit_cookie.xml to the app bar
        getMenuInflater().inflate(R.menu.menu_edit_cookie, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {

            // Respond to a click on the "Save" menu option
            case R.id.action_save:

                //Save info about the cookie into the table
                insertCookie();

                //Exit activity and go back to CookieCatalog (where we came from)
                finish();
                return true;

            //Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                Toast.makeText(this, "Coming soon - Delete cookie", Toast.LENGTH_SHORT).show();
                return true;

            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                //Navigate back to parent activity (CookieCatalog)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


}