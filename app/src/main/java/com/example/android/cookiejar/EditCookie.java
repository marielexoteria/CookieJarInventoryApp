package com.example.android.cookiejar;

/* Toolbar implementation code taken from https://medium.com/@ssaurel/implement-a-navigation-
 drawer-with-a-toolbar-on-android-m-68162f13d220 */

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telecom.Call;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

//Contract class to connect to the SQLite db "cookiejar" and enable CRUD actions
import com.example.android.cookiejar.data.CookieJarContract.CookieEntry;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import static com.example.android.cookiejar.data.CookieJarContract.CookieEntry.COOKIE_TYPE_SWEET;

/**
 * Allows user to add and edit cookies.
 */
public class EditCookie extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the cookie data loader */
    private static final int EXISTING_COOKIE_LOADER = 0;

    /**
     * Content URI for the existing cookie (null if it's a new cookie)
     */
    private Uri currentCookieUri;

    /**
     * Field to enter the name of the cookie
     */
    private EditText cookieNameEditText;

    /**
     * Field to enter the description of the cookie
     */
    private EditText cookieDescriptionEditText;

    /**
     * Field to enter the price of the cookie
     */
    private EditText cookiePriceEditText;

    /**
     * Field to enter the quantity of the cookie
     */
    private EditText cookieQuantityEditText;

    /**
     * Dropdown to choose the type of the cookie (sweet or savoury)
     */
    private Spinner cookieTypeSpinner;

    /**
     * Field to enter the supplier name of the cookie
     */
    private EditText cookieSupplierNameEditText;

    /**
     * Field to enter the phone number of the supplier of the cookie
     */
    private EditText cookieSupplierPhoneNrEditText;

    /**
     * Field to enter the e-mail of the supplier of the cookie
     */
    private EditText cookieSupplierEmailEditText;

    /**
     * Type of cookie: 0 = sweet, 1 = savoury
     */
    private int cookieType = 0;

    //Variables needed to extract the quantity of cookies and for the increment/decrement methods
    String quantityString;
    int quantity = 0;
    Button moreCookiesButton, fewerCookiesButton;

    /* Variable needed to warn the user about unsaved changes.
     * The variable will check whether the cookie was changed or not.
     */
    private boolean cookieHasChanged = false;

    /* OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the cookieHasChanged boolean to true.
     */
    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            cookieHasChanged = true;
            return false;
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_cookie);

        /* Examine the intent that was used to launch this activity, in order to figure out
         * if we're creating a new cookie or editing an existing one
         */
        Intent intent = getIntent();
        currentCookieUri = intent.getData();

        //If the intent does NOT contain a cookie URI, then we know we're creating a cookie
        if (currentCookieUri == null) {
            //New cookie, so change the app bar title to "Add a cookie"
            setTitle(getString(R.string.app_bar_title_add_cookie));

            /* Invalidate the options menu, so the "Delete" menu option can be hidden.
             * (It doesn't make sense to delete a cookie that hasn't been created yet.)
             */
            invalidateOptionsMenu();

        } else {
            //Existing cookie, so change the app bar title to "Edit cookie"
            setTitle(getString(R.string.app_bar_title_edit_cookie));

            /* Initialize a loader to read the cookie data from the database
             * and display the current values in the editor
             */
            getSupportLoaderManager().initLoader(EXISTING_COOKIE_LOADER, null, this);
        }

        /* Setting up the toolbar with the icons to save, go back to the previous activity and
        the overflow menu */
        configureToolbar();

        //Find all relevant views that we will need to read user input from
        cookieNameEditText = (EditText) findViewById(R.id.edit_cookie_name);
        cookieDescriptionEditText = (EditText) findViewById(R.id.edit_cookie_description);
        cookiePriceEditText = (EditText) findViewById(R.id.edit_cookie_price);
        cookieQuantityEditText = (EditText) findViewById(R.id.edit_cookie_quantity);
        cookieTypeSpinner = (Spinner) findViewById(R.id.spinner_cookie_type);
        cookieSupplierNameEditText = (EditText) findViewById(R.id.edit_cookie_supplier_name);
        cookieSupplierPhoneNrEditText = (EditText) findViewById(R.id.edit_cookie_supplier_phone);
        cookieSupplierEmailEditText = (EditText) findViewById(R.id.edit_cookie_supplier_email);

        /* Setting up OnTouchListeners on all the input fields, so we can determine if the user
         * has touched or modified them. This will let us know if there are unsaved changes
         * or not, if the user tries to leave the editor without saving.
         */
        cookieNameEditText.setOnTouchListener(touchListener);
        cookieDescriptionEditText.setOnTouchListener(touchListener);
        cookiePriceEditText.setOnTouchListener(touchListener);
        cookieQuantityEditText.setOnTouchListener(touchListener);
        cookieTypeSpinner.setOnTouchListener(touchListener);
        cookieSupplierNameEditText.setOnTouchListener(touchListener);
        cookieSupplierPhoneNrEditText.setOnTouchListener(touchListener);
        cookieSupplierEmailEditText.setOnTouchListener(touchListener);


        //Connecting the buttons to add or subtract cookies
        moreCookiesButton = (Button) findViewById(R.id.more_cookies);
        fewerCookiesButton = (Button) findViewById(R.id.fewer_cookies);

        //Changing the number of cookies accordingly
        moreCookies();
        fewerCookies();

        //Setting up the dropdown list with the 2 cookie type options (sweet/savoury)
        setupSpinner();
    }

    //Increasing the number of cookies
    public void moreCookies() {
        moreCookiesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quantityString = cookieQuantityEditText.getText().toString();
                quantity = Integer.parseInt(quantityString);

                //Increase the number of cookies by one
                quantity += 1;

                //Display the new number
                quantityString = Integer.toString(quantity);
                cookieQuantityEditText.setText(quantityString);
            }
        });
    }

    //Decreasing the number of cookies
    public void fewerCookies() {
        fewerCookiesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quantityString = cookieQuantityEditText.getText().toString();
                if (quantityString.isEmpty()) {
                    quantity = 0;
                } else {
                    quantity = Integer.parseInt(quantityString);
                }

                if (quantity > 0) {
                    //Decrease the number of cookies by one
                    quantity -= 1;
                } else {
                    /* getBaseContext() idea for the toast from
                     * https://stackoverflow.com/questions/14619234/how-to-make-toast-message-when-button-is-clicked
                     */
                    Toast.makeText(getBaseContext(), getString(R.string.cookie_quantity_negative_number),
                            Toast.LENGTH_SHORT).show();
                }

                //Display the new number
                quantityString = Integer.toString(quantity);
                cookieQuantityEditText.setText(quantityString);
            }
        });
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
                        cookieType = CookieEntry.COOKIE_TYPE_SWEET;
                    } else {
                        cookieType = CookieEntry.COOKIE_TYPE_SAVOURY;
                    }
                } else { //If nothing is chosen, this is the default value
                    cookieType = CookieEntry.COOKIE_TYPE_SWEET;
                }
            }

            //Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                cookieType = 1; //Sweet
            }
        });
    }


    /* Data validation. Checking that none of the fields are empty, except for the cookie
     * type. Code issues solved thanks to the recommendation from @cbp on the Udacity forums:
     * https://discussions.udacity.com/t/app-crashes-upon-sanity-check/814508/8
     */
    public boolean dataValidation() {
        /* Regex to check the e-mail is valid
         * from: https://stackoverflow.com/questions/22505336/email-and-phone-number-validation-in-android
         */
        String emailPattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        //trim() removes any white space at the beginning or the end of the input data
        String nameString = cookieNameEditText.getText().toString().trim();
        String descriptionString = cookieDescriptionEditText.getText().toString().trim();
        String priceString = cookiePriceEditText.getText().toString().trim();
        quantityString = cookieQuantityEditText.getText().toString();
        String supplierNameString = cookieSupplierNameEditText.getText().toString().trim();
        String supplierPhoneString = cookieSupplierPhoneNrEditText.getText().toString().trim();
        String supplierEmailString = cookieSupplierEmailEditText.getText().toString().trim();

        /* Beginning: data validation - checking that the fields are not empty
         * Recommendation from @cbp on the Udacity forums:
         * https://discussions.udacity.com/t/app-crashes-upon-sanity-check/814508/8
         */

        if (nameString.isEmpty()) {
            Toast.makeText(this, getString(R.string.cookie_name_empty), Toast.LENGTH_SHORT).show();
            return false;
        } else if (descriptionString.isEmpty()) {
            Toast.makeText(this, getString(R.string.cookie_description_empty), Toast.LENGTH_SHORT).show();
            return false;
        } else if (priceString.isEmpty()) {
            Toast.makeText(this, getString(R.string.cookie_price_empty), Toast.LENGTH_SHORT).show();
            return false;
        } else if (supplierNameString.isEmpty()) {
            Toast.makeText(this, getString(R.string.supplier_name_empty), Toast.LENGTH_SHORT).show();
            return false;
        } else if (supplierPhoneString.isEmpty()) {
            Toast.makeText(this, getString(R.string.supplier_phone_empty), Toast.LENGTH_SHORT).show();
            return false;
        } else if (supplierEmailString.isEmpty()) {
            Toast.makeText(this, getString(R.string.supplier_email_empty), Toast.LENGTH_SHORT).show();
            return false;
        } else if (!supplierEmailString.matches(emailPattern)) { //Checking that the email is correctly formatted
            Toast.makeText(this, getString(R.string.invalid_email), Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }

        //End: data validation

    }

    /*
     * Get user input from editor and save the new cookie into the db
     */
    private void saveCookie() {
        //Reading from the form on activity_edit_cookie.xml

        //trim() removes any white space at the beginning or the end of the input data
        String nameString = cookieNameEditText.getText().toString().trim();
        String descriptionString = cookieDescriptionEditText.getText().toString().trim();
        String priceString = cookiePriceEditText.getText().toString().trim();
        quantityString = cookieQuantityEditText.getText().toString();
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

        //Determine if this is a new or existing cookie by checking if currentCookieUri is null or not
        if (currentCookieUri == null) {
            /* This is a new cookie, so insert it into the provider and return the
             * content URI for it.
             */
            Uri newUri = getContentResolver().insert(CookieEntry.CONTENT_URI, cookieValues);

            //Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                //There was an error with insertion
                Toast.makeText(this, getString(R.string.editor_insert_cookie_failed) + nameString,
                        Toast.LENGTH_SHORT).show();
            } else {
                //The insertion was successful
                Toast.makeText(this, getString(R.string.editor_insert_cookie_successful) + nameString,
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            /* Otherwise this is an existing cookie, so update it with content URI: currentCookieUri
             * and pass in the new ContentValues. Pass in null for the selection and selection args
             * because currentCookieUri will already identify the correct row in the db that we want
             * to edit
             */
            int rowsAffected = getContentResolver().update(currentCookieUri, cookieValues, null, null);

            //Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                //There was an error with the update
                Toast.makeText(this, getString(R.string.editor_update_cookie_failed) + nameString,
                        Toast.LENGTH_SHORT).show();
            } else {
                //The update was successful and we can display a toast
                Toast.makeText(this, getString(R.string.editor_update_cookie_successful) + nameString,
                        Toast.LENGTH_SHORT).show();
            }
        }

        //Logging the newRowId for error checking
        Log.i("EditCookie.java", "New cookie: " + nameString);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //Adding the menu items from res/menu/menu_edit_cookie.xml to the app bar
        getMenuInflater().inflate(R.menu.menu_edit_cookie, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        //If this is a new cookie, hide the "Delete" menu item.
        if (currentCookieUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {

            //Respond to a click on the "Save" menu option
            case R.id.action_save:

                /* Data validation. Code issues solved thanks to the recommendation from @cbp on the Udacity forums:
                 * https://discussions.udacity.com/t/app-crashes-upon-sanity-check/814508/8
                 */
                dataValidation();

                //If all the fields are correct
                if (dataValidation()) {
                    //Save info about the cookie into the table
                    saveCookie();
                    //Exit activity and go back to CookieCatalog (where we came from)
                    finish();
                    return true;
                }

                return true;

            //Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;

            //Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                /* If the cookie hasn't changed, continue with navigating up to parent activity
                 * which is the {@link CookieCatalog}.
                 */
                if (!cookieHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditCookie.this);
                    return true;
                }

                /* Otherwise if there are unsaved changes, set up a dialog to warn the user.
                 * Create a click listener to handle the user confirming that changes
                 * should be discarded.
                 */
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditCookie.this);
                            }
                        };

                //Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
     * This method is called when the back button is pressed.
     */
     @Override
     public void onBackPressed() {
         //If the cookie hasn't changed, continue with handling back button press
         if (!cookieHasChanged) {
             super.onBackPressed();
             return;
         }

         /* Otherwise if there are unsaved changes, set up a dialog to warn the user.
          * Create a click listener to handle the user confirming that changes should be discarded.
          */
         DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialogInterface, int i) {
                 //User clicked "Discard" button, close the current activity.
                 finish();
             }
         };

         //Show dialog that there are unsaved changes
         showUnsavedChangesDialog(discardButtonClickListener);
     }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        //Since the editor shows all cookie attributes, define a projection that contains all columns from the cookies table
        String[] cookieProjection = {
                CookieEntry._ID,
                CookieEntry.COOKIE_NAME,
                CookieEntry.COOKIE_DESCRIPTION,
                CookieEntry.COOKIE_PRICE,
                CookieEntry.COOKIE_QUANTITY,
                CookieEntry.COOKIE_TYPE,
                CookieEntry.COOKIE_SUPPLIER_NAME,
                CookieEntry.COOKIE_SUPPLIER_PHONE_NR,
                CookieEntry.COOKIE_SUPPLIER_EMAIL
        };

        //This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   //Parent activity context
                currentCookieUri,               //Query the content URI for the current cookie
                cookieProjection,               //Columns to include in the resulting Cursor
                null,                  //No selection clause
                null,              //No selection arguments
                null);                //Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        //Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        /* Proceed with moving to the first row of the cursor and reading data from it
         * (this should be the only row in the cursor)
         */
        if (cursor.moveToFirst()) {
            //Find the columns of cookie attributes that we're interested in
            int cookieNameColumnIndex = cursor.getColumnIndex(CookieEntry.COOKIE_NAME);
            int cookieDescriptionColumnIndex = cursor.getColumnIndex(CookieEntry.COOKIE_DESCRIPTION);
            int cookiePriceColumnIndex = cursor.getColumnIndex(CookieEntry.COOKIE_PRICE);
            int cookieQuantityColumnIndex = cursor.getColumnIndex(CookieEntry.COOKIE_QUANTITY);
            int cookieTypeColumnIndex = cursor.getColumnIndex(CookieEntry.COOKIE_TYPE);
            int supplierNameColumnIndex = cursor.getColumnIndex(CookieEntry.COOKIE_SUPPLIER_NAME);
            int supplierPhoneNrColumnIndex = cursor.getColumnIndex(CookieEntry.COOKIE_SUPPLIER_PHONE_NR);
            int supplierEmailColumnIndex = cursor.getColumnIndex(CookieEntry.COOKIE_SUPPLIER_EMAIL);

            //Extract out the value from the Cursor for the given column index
            String cookieName = cursor.getString(cookieNameColumnIndex);
            String cookieDescription = cursor.getString(cookieDescriptionColumnIndex);
            Double cookiePrice = cursor.getDouble(cookiePriceColumnIndex);
            int cookieQuantity = cursor.getInt(cookieQuantityColumnIndex);
            int cookieType = cursor.getInt(cookieTypeColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            int supplierPhoneNr = cursor.getInt(supplierPhoneNrColumnIndex);
            String supplierEmail = cursor.getString(supplierEmailColumnIndex);

            //Update the views on the screen with the values from the database
            cookieNameEditText.setText(cookieName);
            cookieDescriptionEditText.setText(cookieDescription);
            cookiePriceEditText.setText(Double.toString(cookiePrice));
            cookieQuantityEditText.setText(Integer.toString(cookieQuantity));
            cookieSupplierNameEditText.setText(supplierName);
            cookieSupplierPhoneNrEditText.setText(Integer.toString(supplierPhoneNr));
            cookieSupplierEmailEditText.setText(supplierEmail);

            /* Type is a dropdown spinner, so map the constant value from the database
             * into one of the dropdown options (0 is Sweet, 1 is Savoury).
             * Then call setSelection() so that option is displayed on screen as the current selection.
             */
            switch (cookieType) {
                case CookieEntry.COOKIE_TYPE_SWEET:
                    cookieTypeSpinner.setSelection(0);
                    break;
                case CookieEntry.COOKIE_TYPE_SAVOURY:
                    cookieTypeSpinner.setSelection(1);
                    break;
                default:
                    cookieTypeSpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //If the loader is invalidated, clear out all the data from the input fields.
        cookieNameEditText.setText("");
        cookieDescriptionEditText.setText("");
        cookiePriceEditText.setText("");
        cookieQuantityEditText.setText("");
        cookieTypeSpinner.setSelection(0); //Select "Sweet" cookie type
        cookieSupplierNameEditText.setText("");
        cookieSupplierPhoneNrEditText.setText("");
        cookieSupplierEmailEditText.setText("");

    }

    /*
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     * @param discardButtonClickListener is the click listener for what to do when
     * the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        /* Create an AlertDialog.Builder and set the message, and click listeners
         * for the positive and negative buttons on the dialog.
         * Changing the color of the message (with issues) from:
         * https://stackoverflow.com/questions/44618542/how-to-change-the-color-of-an-alertdialog-message
         */
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                /* The User clicked the "Keep editing" button, so dismiss the dialog
                 * and continue editing the cookie.
                */
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        //Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //Prompt the user to confirm that they want to delete this cookie.
    private void showDeleteConfirmationDialog() {
        /* Create an AlertDialog.Builder and set the message, and click listeners
         * for the positive and negative buttons on the dialog.
         */
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //The user clicked the "Delete" button, so delete the cookie
                deleteCookie();
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
        alertDialog.show();
    }

    //Perform the deletion of the cookie in the database.
    private void deleteCookie() {
        //Only perform the delete if this is an existing cookie.
        if (currentCookieUri != null) {
            /* Call the ContentResolver to delete the cookie at the given content URI.
             * Pass in null for the selection and selection args because the currentCookieUri
             * content URI already identifies the cookie that we want.
             */
            int rowsDeleted = getContentResolver().delete(currentCookieUri, null, null);
            //Show a toast message depending on whether or not the delete was successful
            if (rowsDeleted == 0) {
                //There was an error with the delete
                Toast.makeText(this, getString(R.string.editor_delete_cookie_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                //The delete was successful
                Toast.makeText(this, getString(R.string.editor_delete_cookie_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        //Close the activity
        finish();
    }


}