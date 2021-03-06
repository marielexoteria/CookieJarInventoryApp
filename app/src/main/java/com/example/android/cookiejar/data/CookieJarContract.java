package com.example.android.cookiejar.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class CookieJarContract {

    //Defining the parts that will make up the ContentProvider URI
    public static final String CONTENT_AUTHORITY = "com.example.android.cookiejar";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_COOKIEJAR = "cookiejar";

    public static abstract class CookieEntry implements BaseColumns {

        //ContentProvider URI
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_COOKIEJAR);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of cookies.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COOKIEJAR;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single cookie.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COOKIEJAR;

        public static final String TABLE_NAME = "cookies";

        //Table fields
        public static final String _ID = BaseColumns._ID;
        public static final String COOKIE_NAME = "name";
        public static final String COOKIE_DESCRIPTION = "description";
        public static final String COOKIE_PRICE = "price";
        public static final String COOKIE_QUANTITY = "quantity";
        public static final String COOKIE_TYPE = "type";
        public static final String COOKIE_SUPPLIER_NAME = "supplier_name";
        public static final String COOKIE_SUPPLIER_PHONE_NR = "supplier_phone_number";
        public static final String COOKIE_SUPPLIER_EMAIL = "supplier_email";

        //Possible values for the cookie type
        public static final int COOKIE_TYPE_SWEET = 1;
        public static final int COOKIE_TYPE_SAVOURY = 2;


        //Returns whether or not the given cookie type is sweet or savoury
        public static boolean whichCookieType(int cookieType) {
            if (cookieType == COOKIE_TYPE_SWEET || cookieType == COOKIE_TYPE_SAVOURY) {
                return true;
            }
            return false;
        }

    }
}
