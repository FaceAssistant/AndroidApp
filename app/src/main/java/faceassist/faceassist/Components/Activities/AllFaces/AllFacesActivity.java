package faceassist.faceassist.Components.Activities.AllFaces;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import faceassist.faceassist.Components.Activities.Profile.LovedOneProfile;
import faceassist.faceassist.Components.Fragments.AllFaces.AllFacesContract;
import faceassist.faceassist.Components.Fragments.AllFaces.AllFacesFragment;
import faceassist.faceassist.Components.Fragments.AllFaces.AllFacesInteractor;
import faceassist.faceassist.Components.Fragments.AllFaces.AllFacesPresenterImp;
import faceassist.faceassist.R;
import faceassist.faceassist.Utils.OnInteractorResult;

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


    private AllFacesContract.Interactor mInteractor = new AllFacesContract.Interactor() {
        int count = 0;
        @Override
        public void getAllFaces(OnInteractorResult<LovedOneProfile> onInteractorResult) {
            List<LovedOneProfile> profiles = new ArrayList<>();
            profiles.add(new LovedOneProfile("12", "John", "1-1-1990", "DAD", "12-1-1990", "TEST TEST TEST TEST"));
            onInteractorResult.onGetAllResultResponse(profiles);
        }

        @Override
        public void deleteFace(final int pos, final LovedOneProfile profile, final OnInteractorResult<LovedOneProfile> onInteractorResult) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (count == 0){
                        count++;
                        onInteractorResult.onDeleteResponse(false, pos, profile);
                        onInteractorResult.onError();
                    }else if (count == 1){
                        count++;
                        onInteractorResult.onDeleteResponse(false, pos, profile);
                        onInteractorResult.onFailed();
                    }else {
                        onInteractorResult.onDeleteResponse(true, pos, profile);
                    }
                }
            }, 2000);
        }
    };

}
