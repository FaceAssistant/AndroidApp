package faceassist.faceassist.Components.Fragments.Camera;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.TextureView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by QiFeng on 1/30/17.
 */

public class CameraTextureView extends TextureView {


    private static final int FOCUS_SQR_SIZE = 100;
    private static final int FOCUS_MAX_BOUND = 950;
    private static final int FOCUS_MIN_BOUND = -FOCUS_MAX_BOUND;

    // For focus
    private boolean mIsFocus;
    private boolean mIsFocusReady;

    protected Camera.Area mFocusArea;
    protected ArrayList<Camera.Area> mFocusAreas;

    protected Camera mCamera;

    private float mLastTouchX;
    private float mLastTouchY;

    //interface for focus callbacks
    private OnFocus mOnFocus;

    public CameraTextureView(Context context) {
        super(context);
        init();
    }

    public CameraTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CameraTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CameraTextureView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }


    private void init() {
        mFocusArea = new Camera.Area(new Rect(), 1000);
        mFocusAreas = new ArrayList<>();
        mFocusAreas.add(mFocusArea);
    }

    private static final double ASPECT_RATIO = 3.0 / 4.0;
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);

        final boolean isPortrait =
                getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        if (isPortrait) {
            if (width > height * ASPECT_RATIO) {
                width = (int) (height * ASPECT_RATIO + 0.5);
            } else {
                height = (int) (width / ASPECT_RATIO + 0.5);
            }
        } else {
            if (height > width * ASPECT_RATIO) {
                height = (int) (width * ASPECT_RATIO + 0.5);
            } else {
                width = (int) (height / ASPECT_RATIO + 0.5);
            }
        }

        setMeasuredDimension(width, height);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        final int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                mIsFocus = true;
                mLastTouchX = event.getRawX();
                mLastTouchY = event.getRawY();
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (mIsFocus && mIsFocusReady) {
                    try {
                        handleFocus(mCamera.getParameters());
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                if (mCamera != null) mCamera.cancelAutoFocus();
                mIsFocus = false;
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                break;
            }
        }

        return true;
    }


    private void handleFocus(Camera.Parameters params) {
        float x = mLastTouchX;
        float y = mLastTouchY;

        List<String> supportedFocusModes = params.getSupportedFocusModes();
        if (supportedFocusModes != null
                && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            //Log.d(TAG, mFocusAreas.size() + "");

            setFocusArea(x, y);
            if (mOnFocus != null) mOnFocus.onFocusStart(x, y);
            params.setFocusAreas(mFocusAreas);
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            mCamera.setParameters(params);
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    if (mOnFocus != null) mOnFocus.onFocusFinished();
                }
            });
        }
    }

    public void setOnFocus(OnFocus foc) {
        mOnFocus = foc;
    }


    // interface for focus callbacks
    // added incase we add in the future
    public interface OnFocus {
        void onFocusStart(float x, float y);

        void onFocusFinished();
    }


    public void setIsFocusReady(final boolean isFocusReady) {
        mIsFocusReady = isFocusReady;
    }

    private void setFocusArea(float x, float y) {
        int left = clamp(Float.valueOf((x / getWidth()) * 2000 - FOCUS_MAX_BOUND).intValue(), FOCUS_SQR_SIZE);
        int top = clamp(Float.valueOf((y / getHeight()) * 2000 - FOCUS_MAX_BOUND).intValue(), FOCUS_SQR_SIZE);
        mFocusArea.rect.set(left, top, left + FOCUS_SQR_SIZE, top + FOCUS_SQR_SIZE);
    }

    private int clamp(int touchCoordinateInCameraReper, int focusAreaSize) {
        int result;
        if (Math.abs(touchCoordinateInCameraReper) + focusAreaSize / 2 > FOCUS_MAX_BOUND) {
            if (touchCoordinateInCameraReper > 0) {
                result = FOCUS_MAX_BOUND - focusAreaSize / 2;
            } else {
                result = FOCUS_MIN_BOUND + focusAreaSize / 2;
            }
        } else {
            result = touchCoordinateInCameraReper - focusAreaSize / 2;
        }
        return result;
    }

    public void setCamera(Camera camera){
        mCamera = camera;
    }


//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        int height = MeasureSpec.getSize(heightMeasureSpec);
//        int width = MeasureSpec.getSize(widthMeasureSpec);
//        setMeasuredDimension(width, height);
//    }


}

