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


    public static void updateUserInfo(SharedPreferences preferences, String token, String email, String firstName,
                                      String lastName){
        if (mUserInfoSingleton != null){
            mUserInfoSingleton.mToken = token;
            mUserInfoSingleton.mEmail = email;
            mUserInfoSingleton.mFirstName = firstName;
            mUserInfoSingleton.mLastName = lastName;
            //mUserInfoSingleton.mPhotoUrl = photouUrl;
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(UserInfoConstants.TOKEN, token);
        editor.putString(UserInfoConstants.EMAIL, email);
        editor.putString(UserInfoConstants.FIRST_NAME, firstName);
        editor.putString(UserInfoConstants.LAST_NAME, lastName);
        //editor.putString(UserInfoConstants.PROFILE_URL, photouUrl);
        editor.apply();


    }

    public static void clean(SharedPreferences preferences){
        mUserInfoSingleton = null;
        updateUserInfo(preferences, null, null, null, null);
    }


    private String mToken = null;
    private String mEmail = null;
    private String mFirstName = null;
    private String mLastName = null;
    //private String mPhotoUrl = null;

    private UserInfo(SharedPreferences preferences){
        mToken = preferences.getString(UserInfoConstants.TOKEN, null);
        mEmail = preferences.getString(UserInfoConstants.EMAIL, null);
        mFirstName = preferences.getString(UserInfoConstants.FIRST_NAME, null);
        mLastName = preferences.getString(UserInfoConstants.LAST_NAME, null);
        //mPhotoUrl = preferences.getString(UserInfoConstants.PROFILE_URL, null);
    }


    public boolean isLoggedIn(){
        return mToken != null;
    }


    public String getToken() {
        return mToken;
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
