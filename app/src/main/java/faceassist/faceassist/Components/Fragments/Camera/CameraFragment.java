package faceassist.faceassist.Components.Fragments.Camera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import java.io.File;

import faceassist.faceassist.R;
import faceassist.faceassist.Utils.CameraUtils;
import faceassist.faceassist.Utils.ImageUtils;
import faceassist.faceassist.Utils.PermissionUtils;

/**
 * Created by QiFeng on 1/30/17.
 */

public class CameraFragment extends Fragment implements TextureView.SurfaceTextureListener, CameraPresenter.OrientationHelper,
        CameraContract.View, CameraPresenter.BitmapSaver {

    public static final String TAG = CameraFragment.class.getSimpleName();
    public static final String ARGS_ICON = "back_icon";

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

    public static CameraFragment newInstance(@DrawableRes int backIcon) {
        CameraFragment cameraFragment = new CameraFragment();
        Bundle args = new Bundle();
        args.putInt(ARGS_ICON, backIcon);
        cameraFragment.setArguments(args);
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

        Toolbar toolbar = (Toolbar) root.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCameraPresenter.safeToTakePictures() && getActivity() != null) {
                    getActivity().onBackPressed();
                }
            }
        });

        int icon = getArguments().getInt(ARGS_ICON, -1);
        toolbar.setNavigationIcon(icon == -1 ? R.drawable.ic_action_menu : icon);


        return root;
    }

    private void setUpReverseButton() {
        if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            mReverseCheckbox.setChecked(false);
            mReverseCheckbox.setClickable(false);
        } else {
            mReverseCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    mCameraPresenter.swapCamera(mCameraTextureView, mSurfaceHolder, getCameraId());
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
            mCameraPresenter.restart(mCameraTextureView, mSurfaceHolder, getCameraId());
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
    public int getOrientation() {
        int cameraId = getCameraId();
        return CameraUtils.getOrientation(cameraId, getActivity());
    }


    private int getCameraId() {
        return mReverseCheckbox.isChecked() ?
                CameraUtils.getFrontCameraID(getContext()) : CameraUtils.getBackCameraID();
    }


    @Override
    public void onImageTaken(Uri image) {
        mOnImageTaken.onImageTaken(image);
    }

    @Override
    public File saveBitmapToCache(Bitmap bitmap) {
        if (getActivity() == null) return null;
        return ImageUtils.savePictureToCache(getActivity(), bitmap);
    }


    // What to do with image after image taken
    public interface OnImageTaken {
        void onImageTaken(Uri image);
    }


}
