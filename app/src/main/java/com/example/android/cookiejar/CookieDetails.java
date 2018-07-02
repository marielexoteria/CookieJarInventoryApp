package com.example.android.cookiejar;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

//Contract class to connect to the SQLite db "cookiejar" and enable CRUD actions
import com.example.android.cookiejar.data.CookieJarContract;
import com.example.android.cookiejar.data.CookieJarContract.CookieEntry;

import java.text.DecimalFormat;

public class CookieDetails extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    //Identifier for the cookie data loader
    private static final int EXISTING_COOKIE_LOADER = 0;

    /**
     * Content URI for the existing cookie (null if it's a new cookie)
     */
    private Uri currentCookieUri;

    //TextViews to populate with data
    private TextView cookieNameTextView;
    private TextView cookieDescriptionTextView;
    private TextView cookiePriceTextView;
    private TextView cookieQuantityTextView;
    private TextView cookieTypeTextView;
    private TextView cookieSupplierNameTextView;
    private TextView cookieSupplierPhoneNrTextView;
    private TextView cookieSupplierEmailTextView;

    //To show the cookie picture
    private ImageView cookiePictureImageView;

    //ImageButtons that will enable contacting the supplier
    private ImageButton supplierPhoneImageButton;
    private ImageButton supplierEmailImageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cookie_details);

        /* Setting up the toolbar with the icons to save, go back to the previous activity and
        the overflow menu */
        configureToolbar();

        // Examine the intent that was used to launch this activity
        Intent intent = getIntent();
        currentCookieUri = intent.getData();

        //Find all relevant views that will display cookie data
        cookieNameTextView = (TextView) findViewById(R.id.cookie_name_details);
        cookieDescriptionTextView = (TextView) findViewById(R.id.cookie_description_details);
        cookiePriceTextView = (TextView) findViewById(R.id.cookie_price_details);
        cookieQuantityTextView = (TextView) findViewById(R.id.cookie_quantity_details);
        cookieTypeTextView = (TextView) findViewById(R.id.cookie_type_details);
        cookieSupplierNameTextView = (TextView) findViewById(R.id.supplier_name_details);
        cookieSupplierPhoneNrTextView = (TextView) findViewById(R.id.supplier_phone_details);
        cookieSupplierEmailTextView = (TextView) findViewById(R.id.supplier_email_details);
        cookiePictureImageView = (ImageView) findViewById(R.id.cookie_picture_details); //the picture will be assigned

        //Find the ImageButtons
        supplierPhoneImageButton = (ImageButton) findViewById(R.id.supplier_phone_image_button);
        supplierEmailImageButton = (ImageButton) findViewById(R.id.supplier_email_image_button);

        /* Initialize a loader to read the cookie data from the database
         * and display the current values in the details screen
         */
        getSupportLoaderManager().initLoader(EXISTING_COOKIE_LOADER, null, this);

        //Setting up the button to order cookies via a phone call
        supplierPhoneImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber = cookieSupplierPhoneNrTextView.getText().toString().trim();
                Intent intentPhone = new Intent(Intent.ACTION_DIAL);
                intentPhone.setData(Uri.parse("tel:" + phoneNumber));
                if (intentPhone.resolveActivity(getPackageManager()) != null) {
                    startActivity(intentPhone);
                }

            }
        });

        //Setting up the button to order cookies via e-mail
        supplierEmailImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Getting the values to send the e-mail
                String email = cookieSupplierEmailTextView.getText().toString().trim();
                String cookieNameToOrder = cookieNameTextView.getText().toString().trim();
                String supplierNameToOrder = cookieSupplierNameTextView.getText().toString().trim();

                //Composing the e-mail
                String subjectLine = getString(R.string.email_subject, cookieNameToOrder);
                String bodyText = getString(R.string.body_text, supplierNameToOrder, cookieNameToOrder);

                //Creating intent to send the e-mail on the user's preferred mail app
                Intent intentEmail = new Intent(Intent.ACTION_SENDTO);
                intentEmail.setData(Uri.parse("mailto:" + email));
                intentEmail.putExtra(Intent.EXTRA_SUBJECT, subjectLine); //subject line
                intentEmail.putExtra(Intent.EXTRA_TEXT, bodyText); //order summary: cookie name + supplier name
                if (intentEmail.resolveActivity(getPackageManager()) != null) {
                    startActivity(intentEmail);
                }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //Adding the menu items from res/menu/menu_edit_cookie.xml to the app bar
        getMenuInflater().inflate(R.menu.menu_cookie_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {

            //Respond to a click on the "Edit" menu option
            case R.id.action_edit:
                Intent intent = new Intent(CookieDetails.this, EditCookie.class);
                intent.setData(currentCookieUri);
                startActivity(intent);
                return true;

            //Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(CookieDetails.this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull
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

            //These are needed to assign the right photo according to the type of cookie
            int cookiePhoto = 0;

            //Extract out the value from the Cursor for the given column index
            String cookieName = cursor.getString(cookieNameColumnIndex);
            String cookieDescription = cursor.getString(cookieDescriptionColumnIndex);
            Double cookiePrice = cursor.getDouble(cookiePriceColumnIndex);
            int cookieQuantity = cursor.getInt(cookieQuantityColumnIndex);
            int cookieType = cursor.getInt(cookieTypeColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            int supplierPhoneNr = cursor.getInt(supplierPhoneNrColumnIndex);
            String supplierEmail = cursor.getString(supplierEmailColumnIndex);

            /* Formatting the price to show 2 decimal points when the price ends in 0 (for ex. 1.50).
             * Solution from Udacity's mentor Vlad and https://stackoverflow.com/questions/8065114/how-
             * to-print-a-double-with-two-decimals-in-android
             */
            DecimalFormat priceFormat = new DecimalFormat("0.00");
            String cookiePriceFormatted = priceFormat.format(cookiePrice);

            /* Turning the values 1 and 2 of the cookie type into "sweet" (1) or "savoury" (2),
             * as specified in the {@Link CookieJarContract}.
             * Set the cookie photo accordingly.
             */
            switch (cookieType) {
                case CookieEntry.COOKIE_TYPE_SWEET:
                    cookieTypeTextView.setText(getString(R.string.sweet_cookies_label));
                    cookiePhoto = R.drawable.sweet_cookies;
                    break;
                case CookieEntry.COOKIE_TYPE_SAVOURY:
                    cookieTypeTextView.setText(getString(R.string.savoury_cookies_label));
                    cookiePhoto = R.drawable.savoury_cookies;
                    break;
            }

            //Update the views on the screen with the values from the database
            cookieNameTextView.setText(cookieName);
            cookieDescriptionTextView.setText(cookieDescription);
            cookiePriceTextView.setText(cookiePriceFormatted);
            cookieQuantityTextView.setText(Integer.toString(cookieQuantity));
            cookieSupplierNameTextView.setText(supplierName);
            cookieSupplierPhoneNrTextView.setText(Integer.toString(supplierPhoneNr));
            cookieSupplierEmailTextView.setText(supplierEmail);
            cookiePictureImageView.setImageResource(cookiePhoto); //assigned above
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        //If the loader is invalidated, clear out all the data from the input fields.
        cookieNameTextView.setText("");
        cookieDescriptionTextView.setText("");
        cookiePriceTextView.setText("");
        cookieQuantityTextView.setText("");
        cookieTypeTextView.setText("");
        cookieSupplierNameTextView.setText("");
        cookieSupplierPhoneNrTextView.setText("");
        cookieSupplierEmailTextView.setText("");
    }

}
