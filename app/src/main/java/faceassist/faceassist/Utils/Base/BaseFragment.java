package faceassist.faceassist.Utils.Base;

import android.support.v4.app.Fragment;
import android.view.View;

import faceassist.faceassist.Utils.OnNavigationIconClicked;

/**
 * Created by QiFeng on 3/19/17.
 */

public class BaseFragment extends Fragment {


    private OnNavigationIconClicked mOnNavigationIconClicked;

    protected void onNavigationIconPressed(View v){
        if (mOnNavigationIconClicked != null){
            mOnNavigationIconClicked.onNavIconClicked(v);
        }else if (getActivity() != null){
            getActivity().onBackPressed();
        }
    }

    public void setOnNavigationIconClicked(OnNavigationIconClicked onNavigationIconClicked){
        mOnNavigationIconClicked = onNavigationIconClicked;
    }

}
