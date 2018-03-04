package com.example.abhinav.smartplanner;

/**
 * Created by abhi on 4/3/18.
 */

public class AccountManager {
    public static final int LOGIN_REQUEST = 1;

    private static String mEmail = null;

    public static boolean login(String email, String password) {
        mEmail = email;
        return true;
    }

    public static boolean logout() {
        mEmail = null;
        return true;
    }

    public static boolean isLoggedIn() {
        return !(mEmail == null);
    }
}
