package faceassist.faceassist.Components.Fragments.FacialRec;

import android.graphics.Bitmap;

import faceassist.faceassist.Components.Fragments.FacialRec.ImageView.CustomFace;
import faceassist.faceassist.Components.Fragments.FacialRec.ImageView.FaceDetectionErrors;
import faceassist.faceassist.Components.Fragments.FacialRec.ImageView.FaceDetectionImageView;
import faceassist.faceassist.R;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;

import static rx.android.schedulers.AndroidSchedulers.mainThread;
import static rx.schedulers.Schedulers.io;

/**
 * Created by QiFeng on 2/22/17.
 */

public class FacialRecPresenter implements FacialRecContract.Presenter {


    private FacialRecContract.View mFacialRecView;
    private Subscription mCropSubscription;

    public FacialRecPresenter(FacialRecContract.View view){
        mFacialRecView = view;
    }

    @Override
    public void clickedSubmit(FaceDetectionImageView imageView) {
        if (imageView.getSelectedFace() == null) {
            mFacialRecView.showToast("Select a face");
            return;
        }

        mFacialRecView.showProgress(true);

        if (mCropSubscription != null) mCropSubscription.unsubscribe();
        mCropSubscription = Observable.just(cropImage(imageView))
                .subscribeOn(io())
                .observeOn(mainThread())
                .subscribe(new Action1<Bitmap>() {
                    @Override
                    public void call(Bitmap bitmap) {
                        if (bitmap != null) {
                            mFacialRecView.faceCropped(bitmap);
                            //Log.i(TAG, "call: cropped");
                        } else {
                            mFacialRecView.showProgress(false);
                            mFacialRecView.showToast("Error cropping image");
                        }
                    }
                });
    }


    private Bitmap cropImage(FaceDetectionImageView imageView) {
        Bitmap cachedBitmap = imageView.getCachedBitmap();
        if (cachedBitmap != null) {
            CustomFace face = imageView.getSelectedFace().getFace();
            return Bitmap.createBitmap(cachedBitmap, face.x, face.y, face.getWidth(), face.getHeight());
        }

        return null;
    }

    @Override
    public void stopProcesses() {
        if (mCropSubscription != null) mCropSubscription.unsubscribe();
    }

    @Override
    public void onStartFacialRec() {
        mFacialRecView.setToolbarTitle("Processing image...");
    }

    @Override
    public void onCompleteFacialRec() {
        mFacialRecView.setToolbarTitle("Select a face");
    }

    @Override
    public void onFailed(int error) {

        if (error == FaceDetectionErrors.ERROR_NO_LIBRARY){
            mFacialRecView.showAlert(R.string.no_lib_title, R.string.no_lib_title);
        }else {
            String text = error == FaceDetectionErrors.ERROR_GETTING_FACES ?
                    "Error detecting faces" :
                    "Error decoding image";

            mFacialRecView.showToast(text);
        }
    }
}
