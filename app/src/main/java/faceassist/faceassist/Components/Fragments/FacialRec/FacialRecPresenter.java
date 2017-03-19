package faceassist.faceassist.Components.Fragments.FacialRec;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import faceassist.faceassist.Components.Fragments.FacialRec.ImageView.CustomFace;
import faceassist.faceassist.Components.Fragments.FacialRec.ImageView.FaceDetectionErrors;
import faceassist.faceassist.Components.Fragments.FacialRec.ImageView.FaceDetectionImageView;
import faceassist.faceassist.R;
import faceassist.faceassist.Utils.ImageUtils;
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
    private boolean mBitmapLoaded = false;


    public FacialRecPresenter(FacialRecContract.View view){
        mFacialRecView = view;
        mFacialRecView.setPresenter(this);
    }

    @Override
    public void clickedSubmit(FaceDetectionImageView imageView) {
        if (!mBitmapLoaded) return;
        if (!imageView.isCropable()) {
            if (imageView.getSelectedFace() == null) {
                mFacialRecView.showToast("Select a face");
                return;
            }

            Log.i("TEST", "clickedSubmit: 0");
            mFacialRecView.showProgress(true);

            if (mCropSubscription != null) mCropSubscription.unsubscribe();
            mCropSubscription = Observable.just(cropImage(imageView))
                    .subscribeOn(io())
                    .observeOn(mainThread())
                    .subscribe(new Action1<Uri>() {
                        @Override
                        public void call(Uri uri) {
                            if (uri != null) {
                                Log.i("TEST", "clickedSubmit: 1");
                                mFacialRecView.faceCropped(uri);
                                //Log.i(TAG, "call: cropped");
                            } else {
                                mFacialRecView.showProgress(false);
                                mFacialRecView.showToast("Error cropping image");
                            }
                        }
                    });
        }else {
            detectFaces(imageView);
        }
    }

    @Override
    public void detectFaces(FaceDetectionImageView imageView) {
        imageView.setCropable(false);
        imageView.updateFaces();
    }


    private Uri cropImage(FaceDetectionImageView imageView) {
        Bitmap cachedBitmap = imageView.getCachedBitmap();
        if (cachedBitmap != null) {
            CustomFace face = imageView.getSelectedFace().getFace();
            Bitmap map = Bitmap.createBitmap(cachedBitmap, face.x, face.y, face.getWidth(), face.getHeight());
            return Uri.fromFile(ImageUtils.savePictureToCache(imageView.getContext(), map));
        }

        return null;
    }

    @Override
    public void stopProcesses() {
        if (mCropSubscription != null) mCropSubscription.unsubscribe();
    }

    @Override
    public void onBitmapLoadStarted() {
        mFacialRecView.setToolbarTitle("Loading image...");
    }

    @Override
    public void onBitmapLoaded() {
        mBitmapLoaded = true;
        mFacialRecView.setToolbarTitle("Scale and crop image");
    }

    @Override
    public void onStartFacialRec() {
        mFacialRecView.showProgress(true);
        mFacialRecView.setToolbarTitle("Processing image...");
    }

    @Override
    public void onCompleteFacialRec() {
        mFacialRecView.setSubmitButtonImage(R.drawable.ic_action_send);
        mFacialRecView.showProgress(false);
        mFacialRecView.setToolbarTitle("Select a face");
    }

    @Override
    public void onFailed(int error) {

        if (error == FaceDetectionErrors.ERROR_NO_LIBRARY){
            mFacialRecView.showAlert(R.string.no_lib_title, R.string.no_lib_text);
        }else {
            String text = error == FaceDetectionErrors.ERROR_GETTING_FACES ?
                    "Error detecting faces" :
                    "Error decoding image";

            mFacialRecView.showToast(text);
        }
    }
}
