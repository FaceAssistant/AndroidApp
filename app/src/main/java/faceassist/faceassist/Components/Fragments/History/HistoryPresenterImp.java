package faceassist.faceassist.Components.Fragments.History;

import android.os.Handler;
import android.os.Looper;

import java.util.List;

import faceassist.faceassist.Components.Activities.Profile.BaseProfile;
import faceassist.faceassist.R;
import faceassist.faceassist.Utils.OnInteractorResult;

/**
 * Created by QiFeng on 4/24/17.
 */

public class HistoryPresenterImp implements HistoryContract.Presenter, OnInteractorResult<BaseProfile> {

    private HistoryContract.Interactor mInteractor;
    private HistoryContract.View mView;

    private Handler mHandler = new Handler(Looper.getMainLooper());


    public HistoryPresenterImp(HistoryContract.View view, HistoryContract.Interactor interact){
        mView = view;
        mView.setPresenter(this);
        mInteractor = interact;
    }

    @Override
    public void reload() {
        mView.showList(false);
        mView.showProgress(true);
        mInteractor.getAllFaces(this);
    }

    @Override
    public void onGetAllResultResponse(final List<BaseProfile> results) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mView.updateRV(results);
                mView.showProgress(false);
                mView.showList(true);
            }
        });
    }

    @Override
    public void onDeleteResponse(boolean deleted, int pos, BaseProfile profile) {

    }

    @Override
    public void onFailed() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mView.showToast(R.string.failed_connection);
                mView.showList(true);
            }
        });
    }

    @Override
    public void onError() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mView.showToast(R.string.error_communicating_server);
                mView.showList(true);
            }
        });
    }
}
