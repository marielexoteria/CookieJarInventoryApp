package com.example.android.cookiejar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class CookieDetails extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cookie_details);

        /* Setting up the toolbar with the icons to save, go back to the previous activity and
        the overflow menu */
        configureToolbar();
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
                startActivity(intent);
                return true;

            //Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(CookieDetails.this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
