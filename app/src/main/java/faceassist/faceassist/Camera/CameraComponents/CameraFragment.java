package faceassist.faceassist.Camera.CameraComponents;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.util.List;

import faceassist.faceassist.R;
import faceassist.faceassist.Utils.ImageUtils;
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

public class CameraFragment extends Fragment implements TextureView.SurfaceTextureListener {

    public static final String TAG = CameraFragment.class.getSimpleName();
    public static final int MAX_RESOLUTION = 3200;

    private Camera mCamera;
    private CameraTextureView mCameraTextureView;
    private SurfaceTexture mSurfaceHolder;

    private OnImageTaken mOnImageTaken;

    private Subscription mProcessImageSubscriptions;
    private Subscription mStartCameraSubscription;

    private boolean mSurfaceAlreadyCreated = false;
    private boolean mIsSafeToTakePhoto = false;

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

        mCameraTextureView = (CameraTextureView) root.findViewById(R.id.texture);
        mCameraTextureView.setSurfaceTextureListener(this);

        root.findViewById(R.id.capture_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });

        ((Toolbar) root.findViewById(R.id.toolbar)).setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsSafeToTakePhoto)
                    getActivity().finish();
            }
        });


        return root;
    }


    private void takePicture() {
        if (mIsSafeToTakePhoto) {
            setSafeToTakePhoto(false);
            takeImageOfView();
        }
    }

    private void takeImageOfView() {
        mProcessImageSubscriptions = Observable.just(stopCameraAndSaveImage())
                .subscribeOn(io())
                .observeOn(mainThread())
                .subscribe(
                        new Action1<Uri>() {
                            @Override
                            public void call(Uri uri) {
                                if (getActivity() == null) return;

                                if (mOnImageTaken != null) {
                                    mOnImageTaken.onImageTake(uri);
                                }

                                setSafeToTakePhoto(true);
                            }
                        }
                );
    }

    private Uri stopCameraAndSaveImage() {
        mCamera.stopPreview();
        return saveBitmap();
    }

    private Uri saveBitmap() {
        Bitmap original = mCameraTextureView.getBitmap();
        int width = original.getWidth();
        Bitmap previewBitmap = Bitmap.createBitmap(original, 0, 0, width, width);

        if (previewBitmap != original)
            original.recycle();

        Uri uri = ImageUtils.savePictureToCache(getContext(), previewBitmap);
        previewBitmap.recycle();
        return uri;
    }

    @Override
    public void onResume() {
        super.onResume();
        //restartPreview() is called when camera preview surface is created
        //to prevent redundancy, only run this one if surfaceCreated() isn't called when fragment resumed
        if (mSurfaceAlreadyCreated && PermissionUtils.hasCameraPermission(getContext()) && mCamera == null) {
            restartPreview();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        mShowCameraHandler.removeCallbacksAndMessages(null);

        if (mStartCameraSubscription != null) mStartCameraSubscription.unsubscribe();
        if (mProcessImageSubscriptions != null) mProcessImageSubscriptions.unsubscribe();


        // stop the preview
        if (mCamera != null) {
            // we will get a runtime exception if camera had already been released, either by onpause
            // or when we switched cameras
            try {
                mCamera.cancelAutoFocus();
                stopCameraPreview();
                mCamera.release();
                mCamera = null;
            } catch (RuntimeException e) {
                Log.i(TAG, "onPause: release error : ignore");
                mCamera.release();
                mCamera = null;
            }
        }
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
                    if (mCamera == null) restartPreview();
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
        if (mCamera != null) {
            // we will get a runtime exception if camera had already been released, either by onpause
            // or when we switched cameras
            try {
                stopCameraPreview();
                mCamera.release();
                mCamera = null;
            } catch (RuntimeException e) {
                Log.i(TAG, "onSurfaceTextureDestroyed: release error : ignore");
                mCamera.release();
                mCamera = null;
            }
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }

    /**
     * Stop the camera preview
     */

    private void stopCameraPreview() {
        setSafeToTakePhoto(false);
        setCameraFocusReady(false);


        // Nulls out callbacks, stops face detection
        mCameraTextureView.setCamera(null);
        mCamera.stopPreview();
    }


    /**
     * Start the camera preview
     */
    private void startCameraPreview() {
        determineDisplayOrientation();
        setupCamera();

        try {
            mCamera.setPreviewTexture(mSurfaceHolder);
            mCamera.startPreview();
            setSafeToTakePhoto(true);
            setCameraFocusReady(true);
        } catch (IOException e) {
            Log.d(TAG, "Can't start camera preview due to IOException " + e);
            e.printStackTrace();
        }
    }


    private void setSafeToTakePhoto(final boolean isSafeToTakePhoto) {
        mIsSafeToTakePhoto = isSafeToTakePhoto;
    }

    private void setCameraFocusReady(final boolean isFocusReady) {
        if (this.mCameraTextureView != null) {
            mCameraTextureView.setIsFocusReady(isFocusReady);
        }
    }


    //returns true if able to get camera and false if we werent able to
    private boolean getCamera(int cameraID) {
        try {
            mCamera = Camera.open(cameraID);
            mCameraTextureView.setCamera(mCamera);
            return true;
        } catch (Exception e) {
            Log.d(TAG, "Can't open camera with id " + cameraID);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Restart the camera preview
     */
    private void restartPreview() {
        if (mCamera != null) {

            mStartCameraSubscription = Observable.create(
                    new Observable.OnSubscribe<Void>() {
                        @Override
                        public void call(Subscriber<? super Void> subscriber) {
                            setSafeToTakePhoto(false);
                            setCameraFocusReady(false);
                            stopCameraPreview();
                            mCamera.release();
                            mCamera = null;
                            subscriber.onCompleted();
                        }
                    }).subscribeOn(io())
                    .observeOn(mainThread())
                    .subscribe(
                            new Subscriber<Void>() {
                                @Override
                                public void onCompleted() {
                                    if (getCamera(getBackCameraID())) { //were able to find a camera to use
                                        startCameraPreview();
                                    }
                                }

                                @Override
                                public void onError(Throwable e) {
                                    e.printStackTrace();
                                }

                                @Override
                                public void onNext(Void aVoid) {
                                }
                            });
        } else if (getCamera(getBackCameraID())) { //were able to find a camera to use
            startCameraPreview();
        }
    }

    /**
     * Setup the camera parameters
     */
    private void setupCamera() {
        // Never keep a global parameters
        Camera.Parameters parameters = mCamera.getParameters();

        Camera.Size bestPreviewSize = determineBestSize(mCamera.getParameters().getSupportedPreviewSizes());
        parameters.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height);

        List<String> focusmodes = parameters.getSupportedFocusModes();

        // Set continuous picture focus, if it's supported
        if (focusmodes != null && focusmodes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        //parameters.setZoom(0);

        // Lock in the changes
        mCamera.setParameters(parameters);
    }


    private Camera.Size determineBestSize(List<Camera.Size> sizes) {

        Camera.Size bestSize = null;

        for (Camera.Size size : sizes) {
            boolean isDesireRatio = (size.width / 4) == (size.height / 3) || (size.height / 4) == (size.width / 3);
            boolean isBetterSize = (bestSize == null) || size.width > bestSize.width;

            if (isDesireRatio && isBetterSize && size.height < MAX_RESOLUTION && size.width < MAX_RESOLUTION) {
                bestSize = size;
            }
        }

        if (bestSize == null) {
            Log.d(TAG, "cannot find the best camera size");
            return sizes.get(sizes.size() - 1);
        }

        return bestSize;
    }


    private void determineDisplayOrientation() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(getBackCameraID(), cameraInfo);

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

        mCamera.setDisplayOrientation(displayOrientation);
    }

    private int getBackCameraID() {
        return Camera.CameraInfo.CAMERA_FACING_BACK;
    }


    // What to do with image after image taken
    public interface OnImageTaken {
        void onImageTake(Uri image);
    }


}
