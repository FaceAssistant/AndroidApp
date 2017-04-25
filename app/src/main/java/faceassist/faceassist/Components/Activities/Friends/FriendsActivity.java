package faceassist.faceassist.Components.Activities.Friends;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import faceassist.faceassist.Components.Fragments.Friends.FriendsFragment;
import faceassist.faceassist.Components.Fragments.Friends.FriendsInteractorImp;
import faceassist.faceassist.Components.Fragments.Friends.FriendsPresenterImp;
import faceassist.faceassist.R;

/**
 * Created by QiFeng on 4/24/17.
 */

public class FriendsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.empty_res);

        if (savedInstanceState == null) {
            FriendsFragment fragment = new FriendsFragment();
            new FriendsPresenterImp(fragment, new FriendsInteractorImp());

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }
}
