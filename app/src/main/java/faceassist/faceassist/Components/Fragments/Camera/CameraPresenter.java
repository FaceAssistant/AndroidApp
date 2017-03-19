package faceassist.faceassist.Components.Fragments.Camera;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.TextureView;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;

import static rx.android.schedulers.AndroidSchedulers.mainThread;
import static rx.schedulers.Schedulers.io;

/**
 * Created by QiFeng on 2/20/17.
 */

public class CameraPresenter implements CameraContract.Presenter {

    private static final String TAG = CameraPresenter.class.getSimpleName();
    private static final int MAX_RESOLUTION;
    static {
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        MAX_RESOLUTION = 2 * (width > height ? width : height);
    }

    private boolean mIsSafeToTakePhoto = false;
    private Camera mCamera;

    private Handler mTakePictureHandler;

    private Subscription mProcessImageSubscriptions;
    private Subscription mStartCameraSubscription;

    private CameraContract.View mCameraView;
    private BitmapSaver mBitmapSaver;
    private OrientationHelper mRotationHelper;


    public CameraPresenter(CameraContract.View cameraView, BitmapSaver bitmapSaver, OrientationHelper rotationHelper) {
        mTakePictureHandler = new Handler(Looper.getMainLooper());
        mCameraView = cameraView;
        mBitmapSaver = bitmapSaver;
        mRotationHelper = rotationHelper;

        cameraView.setPresenter(this);
    }

    /**
     * Takes Picture
     *
     * @param textureView - the TextureView showing camera preview
     * @param flash       - should image be taken with flash
     *                    <p>
     *                    Image is taken by freezing camera preview and taking bitmap of TextureView.
     */
    @Override
    public void takePicture(final CameraTextureView textureView, boolean flash) {
        if (mIsSafeToTakePhoto) {
            setSafeToTakePhoto(false);

            if (flash) {
                turnOnFlashLight(true);

                // need to wait for flashlight to turn on fully
                mTakePictureHandler.postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                takeImageOfView(textureView);
                            }
                        }, 300);

            } else takeImageOfView(textureView);
        }
    }

    @Override
    public void swapCamera(CameraTextureView textureView, SurfaceTexture surfaceTexture,int cameraId) {
        if (mIsSafeToTakePhoto)
            restart(textureView, surfaceTexture,cameraId);
    }


    @Override
    public void stopBackgroundTasks() {
        if (mProcessImageSubscriptions != null) mProcessImageSubscriptions.unsubscribe();
        if (mStartCameraSubscription != null) mStartCameraSubscription.unsubscribe();
        mTakePictureHandler.removeCallbacksAndMessages(null);
    }

    private void setSafeToTakePhoto(boolean safeToTakePhoto) {
        mIsSafeToTakePhoto = safeToTakePhoto;
    }


    //returns true if flash successfully turned on
    @Override
    public boolean turnOnFlashLight(boolean turnOn) {
        List<String> flashModes = mCamera.getParameters().getSupportedFlashModes();

        if (flashModes == null) return  false;

        String flashParam = turnOn ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF;

        if (flashModes.contains(flashParam)) {
            Camera.Parameters p = mCamera.getParameters();
            p.setFlashMode(flashParam);
            mCamera.setParameters(p);
            return true;
        }

        return false;
    }

    @Override
    public boolean hasActiveCamera() {
        return mCamera != null;
    }

    @Override
    public boolean safeToTakePictures() {
        return mIsSafeToTakePhoto;
    }

    /**
     * Start the camera preview
     */
    @Override
    public void start(CameraTextureView textureView, SurfaceTexture surfaceHolder) {
        setupCamera();
        mCamera.setDisplayOrientation(mRotationHelper.getOrientation());

        try {
            mCamera.setPreviewTexture(surfaceHolder);
            mCamera.startPreview();
            setSafeToTakePhoto(true);
            textureView.setIsFocusReady(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Setup the camera parameters
     */
    private void setupCamera() {
        // Never keep a global parameters
        Camera.Parameters parameters = mCamera.getParameters();

        Camera.Size bestPreviewSize = determineBestSize(mCamera.getParameters().getSupportedPreviewSizes());
        Log.i(TAG, String.format(Locale.ENGLISH, "cam size: %d x %d", bestPreviewSize.width, bestPreviewSize.height));
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
            return sizes.get(sizes.size() - 1);
        }

        return bestSize;
    }

    @Override
    public void restart(final CameraTextureView textureView, final SurfaceTexture holder, final int cameraId) {
        if (mCamera != null) {
            mStartCameraSubscription = Observable.create(
                    new Observable.OnSubscribe<Void>() {
                        @Override
                        public void call(Subscriber<? super Void> subscriber) {
                            setSafeToTakePhoto(false);
                            textureView.setIsFocusReady(false);
                            stop(textureView);
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
                                    if (getCamera(cameraId)) { //were able to find a camera to use
                                        textureView.setCamera(mCamera);
                                        start(textureView, holder);
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
        } else if (getCamera(cameraId)) { //were able to find a camera to use
            textureView.setCamera(mCamera);
            start(textureView, holder);
        }
    }

    //returns true if able to get camera and false if we werent able to
    private boolean getCamera(int cameraID) {
        try {
            mCamera = Camera.open(cameraID);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void takeImageOfView(TextureView textureView) {
        mProcessImageSubscriptions = Observable.just(getTextureBitmap(textureView))
                .subscribeOn(io())
                .observeOn(mainThread())
                .subscribe(
                        new Action1<Uri>() {
                            @Override
                            public void call(Uri uri) {
                                turnOnFlashLight(false);

                                if (uri == null) return;
                                if (mCameraView!= null) {
                                    mCameraView.onImageTaken(uri);
                                }

                                setSafeToTakePhoto(true);
                            }
                        }
                );
    }


    /**
     * Stop the camera preview
     */
    @Override
    public void stop(CameraTextureView textureView) {
        setSafeToTakePhoto(false);
        textureView.setIsFocusReady(false);

        // Nulls out callbacks, stops face detection
        textureView.setCamera(null);
        mCamera.stopPreview();
    }

    @Override
    public void release(CameraTextureView textureView) {
        if (mCamera != null) {
            turnOnFlashLight(false);

            // we will get a runtime exception if camera had already been released, either by onpause
            // or when we switched cameras
            try {
                mCamera.cancelAutoFocus();
                stop(textureView);
                mCamera.release();
                mCamera = null;
            } catch (RuntimeException e) {
                Log.i(TAG, "onPause: release error : ignore");
                mCamera.release();
                mCamera = null;
            }
        }
    }

    private Uri getTextureBitmap(TextureView view) {
        mCamera.stopPreview();

        Bitmap original = view.getBitmap();
        //return Uri.fromFile(ImageUtils.savePictureToCache(getContext(), original));

        int side = original.getWidth() > original.getHeight() ? original.getHeight() : original.getWidth();
        Bitmap previewBitmap = Bitmap.createBitmap(original, 0, 0, side, side);

        if (previewBitmap != original)
            original.recycle();

        //File f = ImageUtils.savePictureToCache(view.getContext(), previewBitmap);
        File f = mBitmapSaver.saveBitmapToCache(previewBitmap);
        previewBitmap.recycle();
        return f == null ? null : Uri.fromFile(f);
    }


    public interface BitmapSaver{
        File saveBitmapToCache(Bitmap bitmap);
    }

    public interface OrientationHelper {
        int getOrientation();
    }

}
