package faceassist.faceassist;

import android.content.SharedPreferences;

/**
 * Created by QiFeng on 3/12/17.
 */

public class UserInfo {

    private static UserInfo mUserInfoSingleton;

    public static void init(SharedPreferences preferences){
        if (mUserInfoSingleton == null){
            mUserInfoSingleton = new UserInfo(preferences);
        }
    }

    public static UserInfo getInstance(){
        return mUserInfoSingleton;
    }


    public static void updateUserInfo(SharedPreferences preferences, boolean loggedIn, String email, String firstName,
                                      String lastName){
        if (mUserInfoSingleton != null){
            mUserInfoSingleton.mLoggedIn = loggedIn;
            mUserInfoSingleton.mEmail = email;
            mUserInfoSingleton.mFirstName = firstName;
            mUserInfoSingleton.mLastName = lastName;
            //mUserInfoSingleton.mPhotoUrl = photouUrl;
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(UserInfoConstants.LOGGED_IN, loggedIn);
        editor.putString(UserInfoConstants.EMAIL, email);
        editor.putString(UserInfoConstants.FIRST_NAME, firstName);
        editor.putString(UserInfoConstants.LAST_NAME, lastName);
        //editor.putString(UserInfoConstants.PROFILE_URL, photouUrl);
        editor.apply();


    }

    public static void clean(SharedPreferences preferences){
        mUserInfoSingleton = null;
        updateUserInfo(preferences, false, null, null, null);
    }

    private boolean mLoggedIn = true;
    private String mEmail = null;
    private String mFirstName = null;
    private String mLastName = null;
    //private String mPhotoUrl = null;


    private UserInfo(SharedPreferences preferences){
        mLoggedIn = preferences.getBoolean(UserInfoConstants.LOGGED_IN, true);
        mEmail = preferences.getString(UserInfoConstants.EMAIL, null);
        mFirstName = preferences.getString(UserInfoConstants.FIRST_NAME, null);
        mLastName = preferences.getString(UserInfoConstants.LAST_NAME, null);
        //mPhotoUrl = preferences.getString(UserInfoConstants.PROFILE_URL, null);
    }


    public boolean isLoggedIn(){
        return mLoggedIn;
    }

    public String getEmail() {
        return mEmail;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public String getLastName(){
        return mLastName;
    }

//    public String getPhotoUrl(){
//        return mPhotoUrl;
//    }
}
