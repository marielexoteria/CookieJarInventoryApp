package com.example.android.cookiejar.data;

import android.provider.BaseColumns;

public final class CookieJarContract {
    public static abstract class CookieEntry implements BaseColumns {

        public static final String TABLE_NAME = "cookies";

        //Table fields
        public static final String _ID = BaseColumns._ID;
        public static final String COOKIE_NAME = "name";
        public static final String COOKIE_PRICE = "price";
        public static final String COOKIE_QUANTITY = "quantity";
        public static final String COOKIE_TYPE = "type";
        public static final String COOKIE_SUPPLIER_NAME = "supplier_name";
        public static final String COOKIE_SUPPLIER_PHONE_NR = "supplier_phone_number";

        //Possible values for the cookie type
        public static final int COOKIE_TYPE_SWEET = 1;
        public static final int COOKIE_TYPE_SALTY = 2;

    }
}
