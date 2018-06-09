package com.example.android.cookiejar;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
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

/**
 * Allows user to add and edit cookies.
 */
public class EditCookie extends AppCompatActivity {

    /** Field to enter the name of the cookie */
    private EditText cookieNameEditText;

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

    /**
     * Type of cookie: 1 = sweet, 2 = savoury
     */
    private int cookieType = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_cookie);

        //Find all relevant views that we will need to read user input from
        cookieNameEditText = (EditText) findViewById(R.id.edit_cookie_name);
        cookiePriceEditText = (EditText) findViewById(R.id.edit_cookie_price);
        cookieQuantityEditText = (EditText) findViewById(R.id.edit_cookie_quantity);
        cookieSupplierNameEditText = (EditText) findViewById(R.id.edit_cookie_supplier_name);
        cookieSupplierPhoneNrEditText = (EditText) findViewById(R.id.edit_cookie_supplier_phone);
        cookieTypeSpinner = (Spinner) findViewById(R.id.spinner_cookie_type);

        setupSpinner();
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
                        cookieType = CookieEntry.COOKIE_TYPE_SWEET;
                    } else {
                        cookieType = CookieEntry.COOKIE_TYPE_SAVOURY;
                    }
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
        String priceString = cookiePriceEditText.getText().toString().trim();
        String quantityString = cookieQuantityEditText.getText().toString().trim();
        String supplierNameString = cookieSupplierNameEditText.getText().toString().trim();
        String supplierPhoneString = cookieSupplierPhoneNrEditText.getText().toString().trim();

        //Removing all white spaces in the phone number before converting it into an integer
        //From: https://stackoverflow.com/questions/6932163/removing-spaces-from-string
        String phoneStringNoWhiteSpace = supplierPhoneString.replaceAll("\\s+", "");

        //Converting into an integer, as specified in CookieJarContract.java
        int quantity = Integer.parseInt(quantityString);
        int phoneNumber = Integer.parseInt(phoneStringNoWhiteSpace);

        //Converting into a double
        //From: https://stackoverflow.com/questions/6866633/converting-string-to-double-in-android
        Double price = Double.parseDouble(priceString);

        //Creating an instance of type CookieJarDbHelper
        CookieJarDbHelper dbHelper = new CookieJarDbHelper(this);

        //Open the db in write mode
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //Creating a ContentValues object to insert data to the db
        ContentValues cookieValues = new ContentValues();

        //Populating the ContentValues object
        cookieValues.put(CookieEntry.COOKIE_NAME, nameString);
        cookieValues.put(CookieEntry.COOKIE_PRICE, price);
        cookieValues.put(CookieEntry.COOKIE_QUANTITY, quantity);
        cookieValues.put(CookieEntry.COOKIE_TYPE, cookieType);
        cookieValues.put(CookieEntry.COOKIE_SUPPLIER_NAME, supplierNameString);
        cookieValues.put(CookieEntry.COOKIE_SUPPLIER_PHONE_NR, phoneNumber);

        /* Inserting the data from the form into the table specified in TABLE_NAME (CookieJarContract.java)
         and capturing the value of the row in which the data was inserted in the variable newRowId
        */
        long newRowId = db.insert(CookieEntry.TABLE_NAME, null, cookieValues);

        //Displaying a toast to inform the user if adding the cookie was successful or not
        if (newRowId == -1) {
            Toast.makeText(this, "Error inserting cookie: " + nameString, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "New cookie added: " + nameString, Toast.LENGTH_SHORT).show();
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