package faceassist.faceassist.Components.Fragments.FacialRec;

import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import faceassist.faceassist.Components.Fragments.FacialRec.ImageView.FaceDetectionImageView;

/**
 * Created by QiFeng on 2/22/17.
 */

public class FacialRecContract {

    public interface View{
        void showProgress(boolean show);
        void setToolbarTitle(@StringRes int title);
        void showToast(@StringRes int message);
        void showAlert(@StringRes int title, @StringRes int message);
        void setSubmitButtonImage(@DrawableRes int image);
        void faceCropped(Uri uri);
        void setPresenter(FacialRecPresenter presenter);
    }

    public interface Presenter extends FaceDetectionImageView.FaceDetectionListener{
        void clickedSubmit(FaceDetectionImageView imageView);
        void detectFaces(FaceDetectionImageView imageView);
        void stopProcesses();
    }
}
