package faceassist.faceassist.Components.Fragments.FacialRec.ImageView;

import android.graphics.Matrix;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Created by QiFeng on 3/1/17.
 */

public class TouchManager {

    private static final int INVALID_POINTER = -1;

    private TouchPoint mCurrentTouch = null;
    private TouchPoint mPreviousTouch = null;

    private TouchPoint mNewTranslation = new TouchPoint(0,0);

    private int mActivePointer = INVALID_POINTER;

    private int mBitmapHeight;
    private int mBitmapWidth;
    private int mViewHeight;
    private int mViewWidth;

    public void setViewDimens(int viewWidth, int viewHeight){
        mViewHeight = viewHeight;
        mViewWidth = viewWidth;
    }

    public void setBitmapDimens(int width, int height){
        mBitmapHeight = height;
        mBitmapWidth = width;
    }



    public boolean onTouch(MotionEvent e){
        int touchIndex = e.getActionIndex();
        int id = e.getPointerId(touchIndex);

        switch (e.getActionMasked()){
            case MotionEvent.ACTION_UP:
                mCurrentTouch = null;
                mPreviousTouch = null;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (mActivePointer == id){
                    int newIndex = touchIndex == 0 ? 1 : 0;
                    mPreviousTouch = null;
                    mCurrentTouch.setPoints(e.getX(newIndex), e.getY(newIndex));
                    mActivePointer = id;
                }
                break;

            case MotionEvent.ACTION_DOWN:
                mPreviousTouch = null;
                mCurrentTouch = new TouchPoint(e.getX(touchIndex), e.getY(touchIndex));
                mActivePointer = id;
                break;

            case MotionEvent.ACTION_MOVE:
                if (mPreviousTouch == null){
                    mPreviousTouch = new TouchPoint(0,0);
                }

                mPreviousTouch.copy(mCurrentTouch);
                mCurrentTouch.setPoints(e.getX(touchIndex), e.getY(touchIndex));
                bitmapDragged();

                break;

            case MotionEvent.ACTION_CANCEL:
                mPreviousTouch = null;
                mCurrentTouch = null;
                mActivePointer = INVALID_POINTER;
                break;

            default:
                break;
        }


        return true;
    }



    public void resetImage(){
        mNewTranslation.setPoints((mViewWidth - mBitmapWidth) / 2, (mViewHeight - mBitmapHeight) / 2);
    }


    public Matrix getNewImageMatrix(){
        //reset?
        Matrix m = new Matrix();
        m.postTranslate(mNewTranslation.getX(), mNewTranslation.getY());
        return m;
    }


    private void bitmapDragged(){
        TouchPoint prev = getPreviousTouch();
        TouchPoint curr = getCurrentTouch();

        if (curr == null || prev == null) return;
        float dx = clip(mNewTranslation.getX(), mViewWidth, curr.getX() - prev.getX(), mBitmapWidth);
        float dy = clip(mNewTranslation.getY(), mViewHeight, curr.getY() - prev.getY(), mBitmapHeight);

        mNewTranslation.setPoints(dx, dy);
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


    private TouchPoint getCurrentTouch(){
        return mCurrentTouch;
    }

    private TouchPoint getPreviousTouch(){
        return mPreviousTouch == null ? mCurrentTouch : mPreviousTouch;
    }

}
