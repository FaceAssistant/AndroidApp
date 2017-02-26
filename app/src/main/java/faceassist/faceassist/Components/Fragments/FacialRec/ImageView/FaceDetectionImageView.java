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

    private Bitmap mBitmap;

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
    }


    public void setImageUri(final Uri imageUri){
        post(new Runnable() {
            @Override
            public void run() {
                if (mFacialRecSubscription != null) mFacialRecSubscription.unsubscribe();

                mFacialRecSubscription = Observable.just(decodeUri(imageUri))
                        .subscribeOn(io())
                        .observeOn(mainThread())
                        .subscribe(new Observer<Bitmap>() {
                            @Override
                            public void onCompleted() {}

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onNext(Bitmap bitmap) {
                                if (bitmap != null) {
                                    Log.i(TAG, "onNext: ");
                                    setBitmap(bitmap);
                                    updateFaces();
                                }else mFaceDetectionListener.onFailed(FaceDetectionErrors.ERROR_DECODING_IMAGE);
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

            if(bitmap == null) return null;

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
            float bw = bitmap.getWidth();
            float bh = bitmap.getHeight();

            matrix.postTranslate((iw - bw) / 2, (ih - bh) / 2);
            vImageView.setImageMatrix(matrix);
            vImageView.setImageBitmap(bitmap);

//            if (bitmap != mBitmap) {
//                if (mBitmap != null && !mBitmap.isRecycled()) mBitmap.recycle();
//                mBitmap = bitmap;
//            }
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

        //update position
    }

    public Bitmap getBitmap() {
        Bitmap returnedBitmap = Bitmap.createBitmap(vImageView.getWidth(),
                vImageView.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas c = new Canvas(returnedBitmap);
        vImageView.draw(c);
        return returnedBitmap;
    }

    public Bitmap getCachedBitmap() {
        return mBitmap;
    }

    public SparseArray<Face> getFaces() {
        FaceDetector faceDetector = getFaceDetector();
        if (faceDetector == null) return null;

        Bitmap image = getBitmap();
        if (mBitmap != null && mBitmap != image && !mBitmap.isRecycled())
            mBitmap.recycle();

        mBitmap = image;
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

        if (mBitmap != null && !mBitmap.isRecycled()){
            mBitmap.recycle();
            mBitmap = null;
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

    public interface FaceDetectionListener {
        void onStartFacialRec();
        void onCompleteFacialRec();
        void onFailed(int error);
    }


}

