package faceassist.faceassist.Components.Activities.History;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import faceassist.faceassist.Components.Fragments.History.HistoryFragment;
import faceassist.faceassist.Components.Fragments.History.HistoryInteractorImp;
import faceassist.faceassist.Components.Fragments.History.HistoryPresenterImp;
import faceassist.faceassist.R;
/**
 * Created by QiFeng on 4/24/17.
 */

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.empty_res);

        if (savedInstanceState == null) {
            HistoryFragment fragment = new HistoryFragment();
            new HistoryPresenterImp(fragment, new HistoryInteractorImp());

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }

}
