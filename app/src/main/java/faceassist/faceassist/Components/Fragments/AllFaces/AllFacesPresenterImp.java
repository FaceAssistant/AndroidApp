package faceassist.faceassist.Components.Fragments.AllFaces;

import android.os.Handler;
import android.os.Looper;

import java.util.List;

import faceassist.faceassist.Components.Activities.Profile.LovedOneProfile;
import faceassist.faceassist.R;
import faceassist.faceassist.Utils.OnInteractorResult;

/**
 * Created by QiFeng on 4/2/17.
 */

public class AllFacesPresenterImp implements AllFacesContract.Presenter, OnInteractorResult<LovedOneProfile> {

    private AllFacesContract.View mAllFaceView;
    private AllFacesContract.Interactor mAllFaceInteractor;
    private Handler mMainHandler = new Handler(Looper.getMainLooper());

    public AllFacesPresenterImp(AllFacesContract.View view, AllFacesContract.Interactor interactor){
        mAllFaceView = view;
        mAllFaceView.setPresenter(this);
        mAllFaceInteractor = interactor;
    }

    @Override
    public void reload() {
        mAllFaceView.showList(false);
        mAllFaceView.showProgress(true);
        mAllFaceInteractor.getAllFaces(this);
    }

    @Override
    public void delete(int pos, LovedOneProfile profile) {
        mAllFaceView.removeItemFromRV(pos);
        mAllFaceInteractor.deleteFace(pos, profile, this);
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
    public void onDeleteResponse(boolean deleted, final int pos, final LovedOneProfile profile) {
        if (deleted) return;

        //failed to delete. re-add to RV
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                mAllFaceView.addItemToRV(pos, profile);
            }
        });
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
