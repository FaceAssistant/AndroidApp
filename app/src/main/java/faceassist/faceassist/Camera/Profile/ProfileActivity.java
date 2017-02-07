package faceassist.faceassist.Camera.Profile;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import faceassist.faceassist.R;

/**
 * Created by QiFeng on 2/7/17.
 */

public class ProfileActivity extends AppCompatActivity {

    public static final String ARG_PROFILE = "user_profile";
    Profile mProfile;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mProfile = getIntent().getParcelableExtra(ARG_PROFILE);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        toolbar.setTitle(mProfile.getName());

        ((TextView)findViewById(R.id.name)).setText(mProfile.getName());
        ((TextView)findViewById(R.id.confidence)).setText(String.valueOf(mProfile.getConfidence()));

    }


}
