package faceassist.faceassist;

import android.app.Application;

import faceassist.faceassist.API.GoogleAPIHelper;

/**
 * Created by QiFeng on 3/21/17.
 */

public class FaceAssistApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();

        GoogleAPIHelper.init(this);
        UserInfo.init(getSharedPreferences(UserInfoConstants.DEF_PREF, MODE_PRIVATE));
    }


}
