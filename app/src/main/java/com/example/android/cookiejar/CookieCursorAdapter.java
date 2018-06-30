package com.example.android.cookiejar;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.cookiejar.data.CookieJarContract;

/**
 * {@link CookieCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of pet data as its data source. This adapter knows
 * how to create list items for each row of pet data in the {@link Cursor}.
 */

public class CookieCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link CookieCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public CookieCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.cookie_list_item, parent, false);
    }

    /**
     * This method binds the cookie data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current cookie can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        //Find the views to be populated
        TextView cookieNameTextView = (TextView) view.findViewById(R.id.cookie_name_list_item);
        TextView cookieDescriptionTextView = (TextView) view.findViewById(R.id.cookie_description_list_item);
        TextView cookiePriceTextView = (TextView) view.findViewById(R.id.cookie_price_list_item);
        TextView cookieQuantityTextView = (TextView) view.findViewById(R.id.cookie_quantity_list_item);
        TextView cookieTypeTextView = (TextView) view.findViewById(R.id.cookie_type_list_item);

        //Find the columns we're interested in
        int nameColumnIndex = cursor.getColumnIndex(CookieJarContract.CookieEntry.COOKIE_NAME);
        int descriptionColumnIndex = cursor.getColumnIndex(CookieJarContract.CookieEntry.COOKIE_DESCRIPTION);
        int priceColumnIndex = cursor.getColumnIndex(CookieJarContract.CookieEntry.COOKIE_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(CookieJarContract.CookieEntry.COOKIE_QUANTITY);
        int typeColumnIndex = cursor.getColumnIndex(CookieJarContract.CookieEntry.COOKIE_TYPE);

        //Read the cookie attributes from the Cursor for the current cookie
        String cookieName = cursor.getString(nameColumnIndex);
        String cookieDescription = cursor.getString(descriptionColumnIndex);
        String cookiePrice = cursor.getString(priceColumnIndex);
        String cookieQuantity = cursor.getString(quantityColumnIndex);
        String cookieTypeNumber = cursor.getString(typeColumnIndex);

        /* Turning the values 1 and 2 of the cookie type into "sweet" (1) or "savoury" (2),
         * as specified in the {@Link CookieJarContract}
        */
        String cookieType = "";
        switch(cookieTypeNumber) {
            case "1":
                cookieType = "Sweet cookies";
                break;
            case "2":
                cookieType = "Savoury cookies";
                break;
        }

        //Update the TextViews with the attributes for the current cookie
        cookieNameTextView.setText(cookieName);
        cookieDescriptionTextView.setText(cookieDescription);
        cookiePriceTextView.setText(cookiePrice);
        cookieQuantityTextView.setText(cookieQuantity);
        cookieTypeTextView.setText(cookieType);
    }
}
