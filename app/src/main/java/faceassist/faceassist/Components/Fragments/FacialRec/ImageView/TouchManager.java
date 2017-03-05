package faceassist.faceassist.Components.Fragments.FacialRec.ImageView;

import android.graphics.Matrix;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Created by QiFeng on 3/1/17.
 */

public class TouchManager {

    public static final int MAX_FINGERS = 2;
    public static final float MIN_SCALE = 1f;
    public static final float MAX_SCALE = 3f;

    private TouchPoint[] mCurrentTouches = new TouchPoint[2];
    private TouchPoint[] mPreviousTouches = new TouchPoint[2];

    private TouchPoint mMove = new TouchPoint(0,0);
    private TouchPoint mMidpoint = new TouchPoint(0,0);

    private int mBitmapHeight;
    private int mBitmapWidth;
    private int mViewHeight;
    private int mViewWidth;

    private float mScale = 1f;

    private int mWidthScaled;
    private int mHeightScaled;


    public void setViewDimens(int viewWidth, int viewHeight){
        mViewHeight = viewHeight;
        mViewWidth = viewWidth;
    }

    public void setBitmapDimens(int width, int height){
        mBitmapHeight = height;
        mBitmapWidth = width;

        mWidthScaled = width;
        mHeightScaled = height;
    }

    public boolean onTouch(MotionEvent e){
        int touchIndex = e.getActionIndex();
        if (touchIndex > 1) return false;

        if (isUpAction(e.getActionMasked())){
            mCurrentTouches[touchIndex] = null;
            mPreviousTouches[touchIndex] = null;

        }else {
            updatePoints(e);
        }

        int numOfTouches = numOfTouches();

        if (numOfTouches == 1){
            bitmapDragged();
        }else if (numOfTouches == 2){
            bitmapScaled();
            //checkbounds();
        }



        return true;
    }



    public void resetImage(){
        mMove.setPoints((mViewWidth - mBitmapWidth) / 2, (mViewHeight - mBitmapHeight) / 2);
    }


    public Matrix getNewImageMatrix(){
        //reset?
        Matrix m = new Matrix();
        m.postScale(mScale, mScale , mMidpoint.getX(), mMidpoint.getY());
        //m.postScale(1.5f, 1.5f , mMidpoint.getX(), mMidpoint.getY());
        m.postTranslate(mMove.getX(), mMove.getY());
        return m;
    }


    private void updatePoints(MotionEvent event){
        for (int i = 0; i < MAX_FINGERS; i++) {
            if (i < event.getPointerCount()) {
                final float eventX = event.getX(i);
                final float eventY = event.getY(i);

                if (mCurrentTouches[i] == null) {
                    mCurrentTouches[i] = new TouchPoint(eventX, eventY);
                    mPreviousTouches[i] = null;
                } else {
                    if (mPreviousTouches[i] == null) {
                        mPreviousTouches[i] = new TouchPoint(0,0);
                    }

                    mPreviousTouches[i].copy(mCurrentTouches[i]);
                    mCurrentTouches[i].setPoints(eventX, eventY);
                }
            } else {
                mPreviousTouches[i] = null;
                mCurrentTouches[i] = null;
            }
        }
    }


    private void bitmapDragged(){
        TouchPoint prev = getPreviousTouch(0);
        TouchPoint curr = getCurrentTouch(0);

        if (curr == null || prev == null) return;
        float dx = clip(mMove.getX(), mViewWidth, curr.getX() - prev.getX(), mWidthScaled);
        float dy = clip(mMove.getY(), mViewHeight, curr.getY() - prev.getY(), mHeightScaled);

        Log.i("test", "drag: "+mHeightScaled+ " " + mWidthScaled + " " + mScale);
        Log.i("test", "bitmapDragged: " + dx +" "+ dy);

        mMove.setPoints(dx, dy);
    }

    private void bitmapScaled(){
        TouchPoint prev = TouchPoint.subtract(getPreviousTouch(0), getPreviousTouch(1));
        TouchPoint curr = TouchPoint.subtract(getCurrentTouch(0), getCurrentTouch(1));

        TouchPoint.midpoint(getPreviousTouch(0), getPreviousTouch(1), mMidpoint);

        float prevLen = prev.getLength();
        if (prevLen == 0) return;

        mScale = clip(mScale * curr.getLength() / prevLen, MIN_SCALE, MAX_SCALE);
        //mMove.scale(mScale);

        adjustScaledBitmapSize();
    }

    private void adjustScaledBitmapSize(){
        mWidthScaled = (int)(mScale * mBitmapWidth);
        mHeightScaled = (int)(mScale * mBitmapHeight);
    }

    private float clip(float val, float min, float max){
        if (val <= min) return min;
        else if (val >= max) return max;
        return val;
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
        if (newVal + bitmapDimen <= viewDimen) return (viewDimen - bitmapDimen);
        else return newVal;
    }

    private static boolean isUpAction(int actionMasked) {
        return actionMasked == MotionEvent.ACTION_POINTER_UP || actionMasked == MotionEvent.ACTION_UP;
    }

    private int numOfTouches(){
        int touches = 0;

        for (int i = 0; i < MAX_FINGERS; i++){
            if (hasTouch(i)) touches++;
        }

        return touches;
    }


    private TouchPoint getCurrentTouch(int index){
        return mCurrentTouches[index];
    }

    private TouchPoint getPreviousTouch(int index){
        return mPreviousTouches[index] == null ? mCurrentTouches[index] : mPreviousTouches[index];
    }

    private boolean hasTouch(int index){
        return mCurrentTouches[index] != null;
    }

}
