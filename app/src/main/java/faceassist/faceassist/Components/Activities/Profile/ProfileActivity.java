package faceassist.faceassist.Components.Activities.Profile;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.Locale;

import faceassist.faceassist.Components.Activities.Friends.FriendsActivity;
import faceassist.faceassist.R;

/**
 * Created by QiFeng on 2/7/17.
 */

public class ProfileActivity extends AppCompatActivity {

    public static final String ARG_PROFILE = "user_profile";
    private static final String TAG = ProfileActivity.class.getSimpleName();
    private BaseProfile mProfile;
    private TextToSpeech mTextToSpeech;


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
        setUpButtons();

        if (mProfile instanceof LovedOneProfile) {
            setUpLovedOneData();
        }


        mTextToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR) {
                    mTextToSpeech.setLanguage(Locale.ENGLISH);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mTextToSpeech.speak(mProfile.getName(), TextToSpeech.QUEUE_FLUSH, null, null);
                    } else {
                        mTextToSpeech.speak(mProfile.getName(), TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mTextToSpeech != null) {
            mTextToSpeech.stop();
            mTextToSpeech.shutdown();
        }
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
        ImageView imageView = (ImageView) findViewById(R.id.profile_image);
        Glide.with(this)
                .load(mProfile.getImage())
                .into(imageView);

        ((TextView) findViewById(R.id.name)).setText(String.format("Name: %s",mProfile.getName()));
    }

    private void setUpLovedOneData() {
        LovedOneProfile lovedOneProfile = (LovedOneProfile) mProfile;

        Log.i(TAG, "setUpLovedOneData: rel "+ lovedOneProfile.getRelationship());
        Log.i(TAG, "setUpLovedOneData: last " + lovedOneProfile.getLastViewed());

        ((TextView) findViewById(R.id.relationship)).setText(String.format("Relationship: %s",lovedOneProfile.getRelationship()));
        ((TextView) findViewById(R.id.last_viewed)).setText(String.format("Last Viewed: %s ", lovedOneProfile.getLastViewed()));
        ((TextView) findViewById(R.id.note)).setText(String.format("Notes: %s",lovedOneProfile.getNote()));
        ((TextView) findViewById(R.id.birthday)).setText(String.format("Birthday: %s", lovedOneProfile.getBirthday()));
    }

    private void setUpButtons() {
        findViewById(R.id.yes_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ProfileActivity.this, R.string.model_updated, Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        findViewById(R.id.no_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, FriendsActivity.class);
                startActivity(intent);
                finish();

            }
        });
    }

}
