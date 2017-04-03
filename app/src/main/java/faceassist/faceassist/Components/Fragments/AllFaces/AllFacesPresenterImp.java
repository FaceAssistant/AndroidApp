package faceassist.faceassist.Components.Fragments.AllFaces;

import android.os.Handler;
import android.os.Looper;

import java.util.List;

import faceassist.faceassist.Components.Activities.Profile.BaseProfile;
import faceassist.faceassist.Components.Activities.Profile.LovedOneProfile;
import faceassist.faceassist.R;
import faceassist.faceassist.Utils.OnInteractorResult;

/**
 * Created by QiFeng on 4/2/17.
 */

public class AllFacesPresenterImp implements AllFacesContract.AllFacesPresenter, OnInteractorResult<LovedOneProfile> {

    private AllFacesContract.AllFacesView mAllFaceView;
    private AllFacesInteractor mAllFaceInteractor;
    private Handler mMainHandler = new Handler(Looper.getMainLooper());

    public AllFacesPresenterImp(AllFacesContract.AllFacesView view, AllFacesInteractor interactor){
        mAllFaceView = view;
        mAllFaceView.setPresenter(this);
        mAllFaceInteractor = interactor;
    }

    @Override
    public void reload() {
        mAllFaceInteractor.getAllFaces(this);
    }

    @Override
    public void delete(int pos, BaseProfile profile) {
        //// TODO: 4/2/17
        mAllFaceView.removeItemFromRV(pos);
    }

    @Override
    public void onGetAllResultResponse(final List<LovedOneProfile> results) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                mAllFaceView.updateRV(results);
                mAllFaceView.showProgress(false);
                mAllFaceView.showList(true);
            }
        });
    }

    @Override
    public void onDeleteResponse(boolean deleted, int pos) {

        //// TODO: 4/2/17

    }

    @Override
    public void onFailed() {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                mAllFaceView.showToast(R.string.failed_connection);
                mAllFaceView.showList(true);
            }
        });
    }

    @Override
    public void onError() {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                mAllFaceView.showToast(R.string.error_communicating_server);
                mAllFaceView.showList(true);
            }
        });
    }
}
