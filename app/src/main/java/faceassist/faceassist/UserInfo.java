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


    public static void updateUserInfo(SharedPreferences preferences, String token, String email, String name){
        if (mUserInfoSingleton != null){
            mUserInfoSingleton.mToken = token;
            mUserInfoSingleton.mEmail = email;
            mUserInfoSingleton.mFullName = name;
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(UserInfoConstants.TOKEN, token);
        editor.putString(UserInfoConstants.EMAIL, email);
        editor.putString(UserInfoConstants.NAME, name);
        editor.apply();


    }

    public static void clean(SharedPreferences preferences){
        mUserInfoSingleton = null;
        updateUserInfo(preferences, null, null, null);
    }


    private String mToken = null;
    private String mEmail = null;
    private String mFullName = null;

    private UserInfo(SharedPreferences preferences){
        mToken = preferences.getString(UserInfoConstants.TOKEN, null);
        mEmail = preferences.getString(UserInfoConstants.EMAIL, null);
        mFullName = preferences.getString(UserInfoConstants.NAME, null);

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

    public String getFullName() {
        return mFullName;
    }
}
