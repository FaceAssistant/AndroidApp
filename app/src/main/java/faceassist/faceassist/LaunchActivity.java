package faceassist.faceassist;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import faceassist.faceassist.Components.Activities.Camera.CameraActivity;

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
    }


    @Override
    protected void onPostResume() {
        super.onPostResume();

        //start next activity
        Intent i = new Intent(LaunchActivity.this, CameraActivity.class);
        startActivity(i);
        finish();
    }
}
