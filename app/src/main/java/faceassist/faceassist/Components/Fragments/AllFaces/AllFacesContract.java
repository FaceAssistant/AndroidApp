package faceassist.faceassist.Components.Fragments.AllFaces;

import android.support.annotation.StringRes;

import java.util.List;

import faceassist.faceassist.Components.Activities.Profile.BaseProfile;
import faceassist.faceassist.Components.Activities.Profile.LovedOneProfile;

/**
 * Created by QiFeng on 4/2/17.
 */

public class AllFacesContract {


    public interface AllFacesPresenter {
        public void reload();

        public void delete(int pos, BaseProfile profile);
    }

    public interface AllFacesView {
        public void showProgress(boolean show);

        public void showList(boolean show);

        public void removeItemFromRV(int pos);

        public void updateRV(List<LovedOneProfile> profiles);

        public void setPresenter(AllFacesPresenter presenter);

        public void showToast(@StringRes int text);
    }
}
