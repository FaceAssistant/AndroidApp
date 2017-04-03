package faceassist.faceassist.Components.Activities.Profile;

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
    BaseProfile mProfile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProfile = getIntent().getParcelableExtra(ARG_PROFILE);
        int layout = mProfile instanceof LovedOneProfile ?
                R.layout.activity_profile_loved_one :
                R.layout.activity_profile;

        setContentView(layout);

        setUpToolbar();
        setUpData();
        setUpLovedOneData();
    }


    private void setUpToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        toolbar.setTitle(mProfile.getName());
    }

    public void setUpData() {
        ((TextView) findViewById(R.id.name)).setText(mProfile.getName());
    }

    private void setUpLovedOneData() {

        if (!(mProfile instanceof LovedOneProfile)) return;

        LovedOneProfile lovedOneProfile = (LovedOneProfile) mProfile;

        ((TextView) findViewById(R.id.relationship)).setText(lovedOneProfile.getRelationship());
        ((TextView) findViewById(R.id.last_viewed)).setText(lovedOneProfile.getLastViewed());
        ((TextView) findViewById(R.id.note)).setText(lovedOneProfile.getNote());
        ((TextView) findViewById(R.id.birthday)).setText(lovedOneProfile.getBirthday());
    }

}
