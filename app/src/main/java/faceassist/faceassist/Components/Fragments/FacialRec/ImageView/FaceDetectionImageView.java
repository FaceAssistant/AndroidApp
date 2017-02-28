package faceassist.faceassist.Components.Fragments.FacialRec.ImageView;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.IOException;
import java.util.Locale;

import faceassist.faceassist.Utils.ImageUtils;
import faceassist.faceassist.Utils.Views.SquareFrameLayout;
import rx.Observable;
import rx.Observer;
import rx.Subscription;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static rx.android.schedulers.AndroidSchedulers.mainThread;
import static rx.schedulers.Schedulers.io;

/**
 * Created by QiFeng on 2/21/17.
 */

public class FaceDetectionImageView extends SquareFrameLayout implements FaceView.OnFaceSelected {

    private static final String TAG = FaceDetectionImageView.class.getSimpleName();
    private ImageView vImageView;
    private FaceView vSelectedFace;

    private FaceDetector mFaceDetector;
    private Subscription mFacialRecSubscription;

    private FaceDetectionListener mFaceDetectionListener;

    private Bitmap mCacheBitmap;

    private float mTranslationX;
    private float mTranslationY;
    private float mScale = 1;

    private int mOgBitmapWidth;
    private int mOgBitmapHeight;

    private boolean mCropable = true;

    //private ScaleGestureDetector mScaleGestureDetector;

    public FaceDetectionImageView(Context context) {
        super(context);
        init(context);
    }

    public FaceDetectionImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FaceDetectionImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public FaceDetectionImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        vImageView = new ImageView(context);
        LayoutParams params = new LayoutParams(MATCH_PARENT, MATCH_PARENT);
        vImageView.setLayoutParams(params);
        vImageView.setScaleType(ImageView.ScaleType.MATRIX);
        vImageView.setImageMatrix(new Matrix());
        addView(vImageView);

        //mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
    }


    public void setImageUri(final Uri imageUri) {
        post(new Runnable() {
            @Override
            public void run() {
                mFaceDetectionListener.onBitmapLoadStarted();

                if (mFacialRecSubscription != null) mFacialRecSubscription.unsubscribe();

                mFacialRecSubscription = Observable.just(decodeUri(imageUri))
                        .subscribeOn(io())
                        .observeOn(mainThread())
                        .subscribe(new Observer<Bitmap>() {
                            @Override
                            public void onCompleted() {
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onNext(Bitmap bitmap) {
                                if (bitmap != null) {
                                    setBitmap(bitmap);
                                    mFaceDetectionListener.onBitmapLoaded();
                                } else
                                    mFaceDetectionListener.onFailed(FaceDetectionErrors.ERROR_DECODING_IMAGE);
                            }
                        });
            }
        });
    }

    private Bitmap decodeUri(Uri image) {
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        int reqSize = height > width ? width : height;

        try {
            Bitmap bitmap = ImageUtils.decodeUri(getContext(), image, reqSize);

            if (bitmap == null) return null;

            boolean heightShorter = bitmap.getWidth() > bitmap.getHeight();

            int shorter, longer;
            if (heightShorter) {
                shorter = bitmap.getHeight();
                longer = bitmap.getWidth();
            } else {
                shorter = bitmap.getWidth();
                longer = bitmap.getHeight();
            }

            //check if need to scale
            Log.i(TAG, "loadImages:smaller side " + shorter);
            if (shorter == reqSize) return bitmap;

            int reqLonger = reqSize * longer / shorter;

            Bitmap scaled = heightShorter ?
                    Bitmap.createScaledBitmap(bitmap, reqLonger, reqSize, true) :
                    Bitmap.createScaledBitmap(bitmap, reqSize, reqLonger, true);

            Log.i(TAG, String.format(Locale.ENGLISH, "loadImages:scaled w-%d h-%d", scaled.getWidth(), scaled.getHeight()));

            if (scaled != bitmap) bitmap.recycle();
            return scaled;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    private void setBitmap(Bitmap bitmap) { //add runnable ?
        if (bitmap != null) {
            clearFaceViews();

            Matrix matrix = new Matrix();

            float iw = vImageView.getWidth();
            float ih = vImageView.getHeight();

            mOgBitmapWidth = bitmap.getWidth();
            mOgBitmapHeight = bitmap.getHeight();

            mTranslationX = (iw - mOgBitmapWidth) / 2;
            mTranslationY = (ih - mOgBitmapHeight) / 2;

            matrix.postTranslate(mTranslationX, mTranslationY);
            vImageView.setImageMatrix(matrix);
            vImageView.setImageBitmap(bitmap);
        }
    }

    public void updateFaces() {
        vImageView.post(new Runnable() {
            @Override
            public void run() {
                clearFaceViews();
                if (mFacialRecSubscription != null) mFacialRecSubscription.unsubscribe();

                mFaceDetectionListener.onStartFacialRec();

                mFacialRecSubscription = Observable.just(getFaces())
                        .subscribeOn(io())
                        .observeOn(mainThread())
                        .subscribe(new Observer<SparseArray<Face>>() {
                            @Override
                            public void onCompleted() {
                                mFaceDetectionListener.onCompleteFacialRec();
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                                mFaceDetectionListener.onFailed(FaceDetectionErrors.ERROR_GETTING_FACES);
                            }

                            @Override
                            public void onNext(SparseArray<Face> faceSparseArray) {
                                if (getContext() == null || faceSparseArray == null) return;
                                for (int i = 0; i < faceSparseArray.size(); i++) {
                                    CustomFace face = new CustomFace(faceSparseArray.valueAt(i),
                                            getWidth(), getHeight());

                                    Log.d(TAG, face.toString());
                                    addFace(face);
                                }
                            }
                        });
            }
        });
    }

    public FaceView getSelectedFace() {
        return vSelectedFace;
    }

    public void clearFaceViews() {
        //note: assumes ImageView is the only other child
        vSelectedFace = null;

        if (getChildCount() > 1) {
            removeViews(1, getChildCount() - 1);
        }
    }

    public void addFace(CustomFace face) {
        FaceView faceView = new FaceView(getContext(), face);
        faceView.setOnFaceSelected(this);
        addView(faceView);
    }

    public Bitmap getBitmap() {
        Bitmap returnedBitmap = Bitmap.createBitmap(vImageView.getWidth(),
                vImageView.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas c = new Canvas(returnedBitmap);
        vImageView.draw(c);
        return returnedBitmap;
    }

    public Bitmap getCachedBitmap() {
        return mCacheBitmap;
    }


    //run facial rec on mBitmap
    public SparseArray<Face> getFaces() {
        FaceDetector faceDetector = getFaceDetector();
        if (faceDetector == null) return null;

        Bitmap image = getBitmap();
        if (mCacheBitmap != null && mCacheBitmap != image && !mCacheBitmap.isRecycled())
            mCacheBitmap.recycle();

        mCacheBitmap = image;
        final Frame frame = new Frame.Builder().setBitmap(image).build();
        return mFaceDetector.detect(frame);
    }

    public FaceDetector getFaceDetector() {
        if (mFaceDetector == null) {
            mFaceDetector = new FaceDetector.Builder(getContext())
                    .setMinFaceSize(0.25f) //NOTE: proportion to image. change accordingly
                    .setTrackingEnabled(false)
                    .build();
        }

        if (!mFaceDetector.isOperational()) {
            mFaceDetectionListener.onFailed(FaceDetectionErrors.ERROR_NO_LIBRARY);
            return null;
        }

        return mFaceDetector;

    }

    public void setFaceDetectionListener(FaceDetectionListener faceDetectionListener) {
        mFaceDetectionListener = faceDetectionListener;
    }


    public void stopProcesses() {
        if (mFacialRecSubscription != null)
            mFacialRecSubscription.unsubscribe();

        if (mFaceDetector != null) {
            mFaceDetector.release();
            mFaceDetector = null;
        }
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopProcesses();

        if (mCacheBitmap != null && !mCacheBitmap.isRecycled()) {
            mCacheBitmap.recycle();
            mCacheBitmap = null;
        }
    }

    @Override
    public void onFaceSelected(FaceView v) {
        if (vSelectedFace != null) {
            vSelectedFace.setSelected(false);
            vSelectedFace.invalidate();
        }

        vSelectedFace = v;
        vSelectedFace.setSelected(true);
        vSelectedFace.invalidate();
    }

    public void setCropable(boolean cropable) {
        mCropable = cropable;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mCropable)
            handleMoveAndScale(event);


        return true;
    }


    private static final int INVALID_POINTER_ID = -1;

    private float mPLastX;
    private float mPLastY;
    private int mActivePointerId = INVALID_POINTER_ID;

    private float mLastX;
    private float mLastY;
    private int mSecondaryPointerId = INVALID_POINTER_ID;

    private float mFocusX;
    private float mFocusY;

    private double mOldDistance;


    private boolean handleMoveAndScale(MotionEvent event) {
        int pointerIndex;
        int pointerId;

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: //first touch, save initial location
                Log.i(TAG, "handleMoveAndScale: primary down");
                pointerIndex = event.getActionIndex();

                mPLastX = event.getX(pointerIndex);
                mPLastY = event.getY(pointerIndex);

                mActivePointerId = event.getPointerId(pointerIndex);
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                pointerIndex = event.getActionIndex();
                if (mSecondaryPointerId == INVALID_POINTER_ID && mActivePointerId != pointerIndex) {
                    Log.i(TAG, "handleMoveAndScale: secondary set");
                    mSecondaryPointerId = event.getPointerId(pointerIndex);
                    mLastX = event.getX(pointerIndex);
                    mLastY = event.getY(pointerIndex);
                    mOldDistance = Math.sqrt(Math.pow(mLastX - mPLastX, 2) + Math.pow(mLastY - mPLastY, 2));
                }
            case MotionEvent.ACTION_MOVE: //finger moves
                pointerIndex = event.getActionIndex();

                pointerId = event.getPointerId(pointerIndex);

                float x = event.getX(pointerIndex);
                float y = event.getY(pointerIndex);

                if (mSecondaryPointerId == INVALID_POINTER_ID) {
                    Log.i(TAG, "handleMoveAndScale: move");
                    if (pointerId != mActivePointerId) break;
                    moveImage(x - mPLastX, y - mPLastY);
                    //updateImage(1f,x - mPLastX, y - mPLastY );
                    mPLastX = x;
                    mPLastY = y;
                } else {

                    Log.i(TAG, "handleMoveAndScale: scale");
                    mFocusX = (int)((mLastX + mPLastX) / 2);
                    mFocusY = (int)((mLastY + mPLastY) / 2);

                    if (pointerId == mActivePointerId) {
                        Log.i(TAG, "handleMoveAndScale: active move");
                        mPLastX = x;
                        mPLastY = y;
                    } else {
                        Log.i(TAG, "handleMoveAndScale: seconday move");
                        mLastX = x;
                        mLastY = y;
                    }

                    double newDis = Math.sqrt(Math.pow(mLastX - mPLastX, 2) + Math.pow(mLastY - mPLastY, 2));
                    updateImage((float)(newDis / mOldDistance), 0, 0);
                    //scaleImage((float)(newDis / mOldDistance), mFocusX, mFocusY);
                    mOldDistance = newDis;
                }

                break;
            case MotionEvent.ACTION_UP:
                mActivePointerId = INVALID_POINTER_ID;
                break;

            case MotionEvent.ACTION_CANCEL:
                mActivePointerId = INVALID_POINTER_ID;
                break;

            case MotionEvent.ACTION_POINTER_UP: //a finger was lifted up
                pointerIndex = event.getActionIndex();
                pointerId = event.getPointerId(pointerIndex);

                //the current active was the one lifted, set the other finger as active
                if (pointerId == mActivePointerId) {
                    Log.i(TAG, "handleMoveAndScale: primary up");
                    int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mPLastX = event.getX(newPointerIndex);
                    mPLastY = event.getY(newPointerIndex);
                    mActivePointerId = event.getPointerId(newPointerIndex);
                    mSecondaryPointerId = INVALID_POINTER_ID;
                } else if (pointerId == mSecondaryPointerId) {
                    Log.i(TAG, "handleMoveAndScale: secondary up");
                    mSecondaryPointerId = INVALID_POINTER_ID;
                }
                break;

            default:
                return false;
        }

        return true;
    }


    private void updateImage(float scale, float dx, float dy){
        mScale = clip(mScale * scale, 1f, 3f);

        //mTranslationY = clip(mTranslationY, vImageView.getHeight(), dy, (int)(mOgBitmapHeight * mScale));
        //mTranslationX = clip(mTranslationX, vImageView.getWidth(), dx, (int)(mOgBitmapWidth * mScale));

        Matrix matrix = vImageView.getMatrix();
        matrix.postScale(mScale, mScale, mFocusX, mFocusY);
        matrix.postTranslate(mTranslationX, mTranslationY);
        vImageView.setImageMatrix(matrix);

    }

    private void moveImage(float dx, float dy) {

        //double scal = Math.sqrt(Math.pow(mScale, 2) + Math.pow(mScale, 2));
        mTranslationY = clip(mTranslationY, vImageView.getHeight(), dy, (int)(mOgBitmapHeight ));
        mTranslationX = clip(mTranslationX, vImageView.getWidth(), dx, (int)(mOgBitmapWidth ));

        Matrix matrix = vImageView.getMatrix();
        matrix.postScale(mScale, mScale, mFocusX, mFocusY);
        matrix.postTranslate(mTranslationX, mTranslationY);
        vImageView.setImageMatrix(matrix);
    }

    /**
     * clips values - don't want image moving out of view
     *
     * @param curr        - current translation
     * @param viewDimen   - imageView dimension
     * @param change      - change in translation
     * @param bitmapDimen - bitmap dimension
     *                    ex. to find new X translation, params are (mTranslationX, vImageView.getWidth, dx, mOgBitmapWidth)
     */

    private float clip(float curr, int viewDimen, float change, int bitmapDimen) {
        float newVal = curr + change;
        if (newVal >= 0) return 0;
        if (newVal + bitmapDimen <= viewDimen) return (viewDimen - bitmapDimen) / 2;
        else return newVal;
    }

    private float clip(float val, float min, float max) {
        if (val <= min) return min;
        else if (val >= max) return max;
        return val;
    }


//    private void scaleImage(float newScale, float focusX, float focusY) {
//        mScale = clip(mScale * newScale, 1f, 3f);
//        Matrix matrix = vImageView.getMatrix();
//        matrix.preScale(mScale, mScale, focusX, focusY);
//
//        vImageView.setImageMatrix(matrix);
//    }


    public interface FaceDetectionListener {
        void onBitmapLoadStarted();

        void onBitmapLoaded();

        void onStartFacialRec();

        void onCompleteFacialRec();

        void onFailed(int error);
    }


//    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener{
//        @Override
//        public boolean onScale(ScaleGestureDetector detector) {
//            float newScale = clip(mScale * detector.getScaleFactor(), 1f, 4f);
//            scaleImage(newScale, detector.getFocusX(), detector.getFocusY());
//            return true;
//        }
//    }


}

