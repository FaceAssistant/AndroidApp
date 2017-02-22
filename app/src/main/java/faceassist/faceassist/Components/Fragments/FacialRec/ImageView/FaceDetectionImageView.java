package faceassist.faceassist.Components.Fragments.FacialRec.ImageView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.widget.ImageView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

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
    //private List<CustomFace> mCustomFaces = new ArrayList<>();
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

    public ImageView getvImageView(){
        return vImageView;
    }


    public void setBitmap(Bitmap bitmap) { //add runnable ?
        Matrix matrix = getMatrix();

        matrix.postTranslate((vImageView.getWidth() - bitmap.getWidth()) / 2f,
                (vImageView.getHeight() - bitmap.getHeight()) / 2f);

        vImageView.setImageMatrix(matrix);

        vImageView.setImageBitmap(bitmap);
    }

    public void updateFaces() {
        clearFaceViews();
        if (mFacialRecSubscription != null) mFacialRecSubscription.unsubscribe();

        mFacialRecSubscription = Observable.just(getFaces())
                .subscribeOn(io())
                .observeOn(mainThread())
                .subscribe(new Observer<SparseArray<Face>>() {
                    @Override
                    public void onCompleted() {
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

    public FaceView getSelectedFace() {
        return vSelectedFace;
    }

    public void clearFaceViews() {
        //note: assumes ImageView is the only other child
        vSelectedFace = null;

        if (getChildCount() > 1) {
            removeViews(1, getChildCount() - 1);
            //mCustomFaces.clear();
        }
    }

    public void addFace(CustomFace face) {
        //mCustomFaces.add(face);
        FaceView faceView = new FaceView(getContext(), face);
        faceView.setOnFaceSelected(this);
        addView(faceView);

        //update position
    }

    public Bitmap getBitmap() {
        Bitmap returnedBitmap = Bitmap.createBitmap(vImageView.getWidth(), vImageView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(returnedBitmap);
        vImageView.draw(c);
        return returnedBitmap;
    }

    public Bitmap getCachedBitmap(){
        return mBitmap;
    }

    public SparseArray<Face> getFaces() {
        FaceDetector faceDetector = getFaceDetector();
        if (faceDetector == null) return null;

        Bitmap image = getBitmap();
        if (mBitmap != null && mBitmap != image)
            mBitmap.recycle();

        mBitmap = image;

        final Frame frame = new Frame.Builder().setBitmap(mBitmap).build();
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
            mFaceDetectionListener.onFailed(FaceDetectionErrors.NO_LIBRARY);
            return null;
        }

        return mFaceDetector;

    }

    public void setFaceDetectionListener(FaceDetectionListener faceDetectionListener){
        mFaceDetectionListener = faceDetectionListener;
    }


    public void stopProcesses() {
        if (mFacialRecSubscription != null)
            mFacialRecSubscription.unsubscribe();
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopProcesses();
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
        void onStart();

        void onComplete();

        void onFailed(int error);
    }
}

