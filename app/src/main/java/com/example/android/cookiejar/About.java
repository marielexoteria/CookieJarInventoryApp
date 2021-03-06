package com.example.android.cookiejar;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

public class About extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        /* Setting up the toolbar with the icons to save, go back to the previous activity and
        the overflow menu */
        configureToolbar();

        //Variables needed to build the footer
        TextView footerTextPart1 = (TextView) findViewById(R.id.footer_text_part_1);
        ImageView footerHeart = (ImageView) findViewById(R.id.footer_heart);
        TextView footerTextPart2 = (TextView) findViewById(R.id.footer_text_part_2);

        //Footer
        footerTextPart1.setText(R.string.footer_text_part_1);
        footerHeart.setImageResource(R.drawable.icon_heart);
        footerTextPart2.setText(R.string.footer_text_part_2);
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




}
