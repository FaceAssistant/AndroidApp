package faceassist.faceassist.Components.Activities.AllFaces;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import faceassist.faceassist.Components.Fragments.AllFaces.AllFacesFragment;
import faceassist.faceassist.Components.Fragments.AllFaces.AllFacesInteractor;
import faceassist.faceassist.Components.Fragments.AllFaces.AllFacesPresenterImp;
import faceassist.faceassist.R;

/**
 * Created by QiFeng on 4/2/17.
 */

public class AllFacesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.empty_res);

        if (savedInstanceState == null) {
            AllFacesFragment fragment = new AllFacesFragment();
            new AllFacesPresenterImp(fragment, new AllFacesInteractor());

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }



}
