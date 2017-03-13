package faceassist.faceassist;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import faceassist.faceassist.Components.Activities.Camera.FacialResultActivity;
import faceassist.faceassist.Login.LoginActivity;

/**
 * Created by QiFeng on 1/30/17.
 *
 *
 * When our app starts with this activity.
 * We will put logic in here to determine if an user is logged in, etc...
 *
 */

public class LaunchActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UserInfo.init(getSharedPreferences(UserInfoConstants.DEF_PREF, MODE_PRIVATE));
    }


    @Override
    protected void onPostResume() {
        super.onPostResume();

        UserInfo info = UserInfo.getInstance();
        Class nextActivity = FacialResultActivity.class; //info.isLoggedIn() ? FacialResultActivity.class : LoginActivity.class;

        Intent i = new Intent(LaunchActivity.this, nextActivity);
        startActivity(i);
        finish();
    }
}
