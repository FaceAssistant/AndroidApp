package faceassist.faceassist.Components.Fragments.AllFaces;

import android.support.annotation.StringRes;

import java.util.List;

import faceassist.faceassist.Components.Activities.Profile.LovedOneProfile;
import faceassist.faceassist.Utils.OnInteractorResult;

/**
 * Created by QiFeng on 4/2/17.
 */

public class AllFacesContract {


    public interface Presenter {
        public void reload();

        public void delete(int pos, LovedOneProfile profile);
    }

    public interface View {
        public void showProgress(boolean show);

        public void showList(boolean show);

        public void removeItemFromRV(int pos);

        public void updateRV(List<LovedOneProfile> profiles);

        public void setPresenter(Presenter presenter);

        public void showToast(@StringRes int text);

        public void addItemToRV(int pos, LovedOneProfile profile);
    }

    public interface Interactor {
        public void getAllFaces(OnInteractorResult<LovedOneProfile> onInteractorResult);

        public void deleteFace(int pos, LovedOneProfile profile, OnInteractorResult<LovedOneProfile> onInteractorResult);
    }
}
