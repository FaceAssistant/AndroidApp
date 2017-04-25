package faceassist.faceassist.Components.Fragments.History;

import android.support.annotation.StringRes;

import java.util.List;

import faceassist.faceassist.Components.Activities.Profile.BaseProfile;
import faceassist.faceassist.Components.Activities.Profile.LovedOneProfile;
import faceassist.faceassist.Utils.OnInteractorResult;

/**
 * Created by QiFeng on 4/24/17.
 */

public class HistoryContract {

    public interface Presenter {
        public void reload();
    }

    public interface View {
        public void showProgress(boolean show);

        public void showList(boolean show);

        public void updateRV(List<BaseProfile> profiles);

        public void setPresenter(Presenter presenter);

        public void showToast(@StringRes int text);
    }


    public interface Interactor {
        public void getAllFaces(OnInteractorResult<BaseProfile> onInteractorResult);
    }
}
