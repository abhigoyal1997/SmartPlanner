package com.example.abhinav.smartplanner;

import android.media.Image;
import android.net.Uri;
import android.util.Log;

/**
 * Created by abhi on 4/3/18.
 */

public class AccountManager {
    public static final int LOGIN_REQUEST = 1;
    private static final AccountManager mInstance = new AccountManager();
    public static final int LOGOUT_REQUEST = 0;

    public static AccountManager getInstance() {
        return mInstance;
    }

    public String getEmail() {
        return mEmail;
    }

    public String getName() {
        return mName;
    }

    public Uri getImageUrl() {
        return mImageUrl;
    }

    private String mEmail = null;
    private String mName = null;
    private Uri mImageUrl = null;

    public boolean login(String email, String name, Uri photoUrl) {
        mEmail = email;
        mName = name;
        mImageUrl = photoUrl;
        return true;
    }

    public boolean logout() {
        mEmail = null;
        mName = null;
        mImageUrl = null;
        return true;
    }

    public boolean isLoggedIn() {
        return !(mEmail == null);
    }
}
