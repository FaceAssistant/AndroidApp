package faceassist.faceassist.Components.Fragments.Camera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import faceassist.faceassist.R;
import faceassist.faceassist.Utils.ImageUtils;
import faceassist.faceassist.Utils.OnToolbarMenuIconPressed;
import faceassist.faceassist.Utils.PermissionUtils;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;

import static rx.android.schedulers.AndroidSchedulers.mainThread;
import static rx.schedulers.Schedulers.io;

/**
 * Created by QiFeng on 1/30/17.
 */

public class CameraFragment extends Fragment implements TextureView.SurfaceTextureListener, CameraPresenter.RotationHelper,
        CameraContract.View, CameraPresenter.BitmapSaver {

    public static final String TAG = CameraFragment.class.getSimpleName();

    private CameraTextureView mCameraTextureView;
    private SurfaceTexture mSurfaceHolder;

    private OnImageTaken mOnImageTaken;
    private boolean mSurfaceAlreadyCreated = false;

    //private int mCameraId;

    private AppCompatCheckBox mReverseCheckbox;
    private AppCompatCheckBox mFlashCheckbox;

    private CameraContract.Presenter mCameraPresenter;


    public CameraFragment() {

    }

    public static CameraFragment newInstance() {
        CameraFragment cameraFragment = new CameraFragment();
        return cameraFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mOnImageTaken = (OnImageTaken) context;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_camera, container, false);

        mCameraPresenter = new CameraPresenter(this, this, this);

        mCameraTextureView = (CameraTextureView) root.findViewById(R.id.texture);
        mCameraTextureView.setSurfaceTextureListener(this);

        mFlashCheckbox = (AppCompatCheckBox) root.findViewById(R.id.flash);
        mReverseCheckbox = (AppCompatCheckBox) root.findViewById(R.id.reverse);

        setUpReverseButton();

        root.findViewById(R.id.capture_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCameraPresenter.takePicture(mCameraTextureView, mFlashCheckbox.isChecked());
            }
        });

        ((Toolbar) root.findViewById(R.id.toolbar)).setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCameraPresenter.safeToTakePictures())
                    ((OnToolbarMenuIconPressed) getActivity()).onToolbarMenuIconPressed();
            }
        });


        return root;
    }

    private void setUpReverseButton(){
        if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)){
            mReverseCheckbox.setChecked(false);
            mReverseCheckbox.setClickable(false);
        }else {
            mReverseCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    mCameraPresenter.swapCamera(mCameraTextureView, mSurfaceHolder,getCameraId());
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //restartPreview() is called when camera preview surface is created
        //to prevent redundancy, only run this one if surfaceCreated() isn't called when fragment resumed
        if (mSurfaceAlreadyCreated && PermissionUtils.hasCameraPermission(getContext()) && !mCameraPresenter.hasActiveCamera()) {
            mCameraPresenter.restart(mCameraTextureView, mSurfaceHolder,getCameraId());
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        mShowCameraHandler.removeCallbacksAndMessages(null);
        mCameraPresenter.stopBackgroundTasks();
        mCameraPresenter.release(mCameraTextureView);
    }

    //delay drawing the camera preview
    //opening the camera in onresume causes a slight lag when resuming
    final Handler mShowCameraHandler = new Handler();

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        mSurfaceHolder = surfaceTexture;
        if (PermissionUtils.hasCameraPermission(getContext())) {
            mSurfaceAlreadyCreated = true;

            //start camera after slight delay. Without delay,
            //there is huge lag time between active and inactive app state
            mShowCameraHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!mCameraPresenter.hasActiveCamera())
                        mCameraPresenter.restart(mCameraTextureView, mSurfaceHolder, getCameraId());
                }
            }, 250);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        mSurfaceAlreadyCreated = false;
        mShowCameraHandler.removeCallbacksAndMessages(null);
        // stop the preview
        mCameraPresenter.release(mCameraTextureView);
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }


    @Override
    public int getRotation() {
        int cameraId = getCameraId();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);

        // Clockwise rotation needed to align the window display to the natural position
        int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0: {
                degrees = 0;
                break;
            }
            case Surface.ROTATION_90: {
                degrees = 90;
                break;
            }
            case Surface.ROTATION_180: {
                degrees = 180;
                break;
            }
            case Surface.ROTATION_270: {
                degrees = 270;
                break;
            }
        }

        // CameraInfo.Orientation is the angle relative to the natural position of the device
        // in clockwise rotation (angle that is rotated clockwise from the natural position)
        /*
      Determine the current display orientation and rotate the camera preview
      accordingly
     */
        int displayOrientation;
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            // Orientation is angle of rotation when facing the camera for
            // the camera image to match the natural orientation of the device
            displayOrientation = (cameraInfo.orientation + degrees) % 360;
            displayOrientation = (360 - displayOrientation) % 360;
        } else {
            displayOrientation = (cameraInfo.orientation - degrees + 360) % 360;
        }

        return displayOrientation;
    }


    private int getCameraId(){
        return mReverseCheckbox.isChecked() ? getFrontCameraID() : getBackCameraID();
    }
    private int getBackCameraID() {
        return Camera.CameraInfo.CAMERA_FACING_BACK;
    }

    private int getFrontCameraID() {
        PackageManager pm = getActivity().getPackageManager();
        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            return Camera.CameraInfo.CAMERA_FACING_FRONT;
        }

        return getBackCameraID();
    }

    @Override
    public void onImageTaken(Uri image) {
        mOnImageTaken.onImageTake(image);
    }

    @Override
    public File saveBitmapToCache(Bitmap bitmap) {
        if (getActivity() == null) return null;
        return ImageUtils.savePictureToCache(getActivity(), bitmap);
    }


    // What to do with image after image taken
    public interface OnImageTaken {
        void onImageTake(Uri image);
    }


}
