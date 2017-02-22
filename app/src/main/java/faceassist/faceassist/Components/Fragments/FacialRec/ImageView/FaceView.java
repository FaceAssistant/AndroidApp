package faceassist.faceassist.Components.Fragments.FacialRec.ImageView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by QiFeng on 2/5/17.
 */

public class FaceView extends View implements View.OnClickListener{

    private CustomFace mFace;
    private Paint mPaint = new Paint();
    private OnFaceSelected mOnFaceSelected;

    private boolean mSelected = false;

    public FaceView(Context context, CustomFace face) {
        super(context);
        init(face);
    }

    public FaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public FaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
        mPaint.setStrokeWidth(6);
        mPaint.setStyle(Paint.Style.STROKE);
        setOnClickListener(this);
    }

    private void init(CustomFace face){
        mFace = face;
        init();
        updatePosition(face.x, face.y);
    }

    public void setFace(CustomFace face){
        mFace = face;
        updatePosition(face.x, face.y);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setColor(mSelected ? Color.GREEN : Color.RED);
        canvas.drawRect(0, 0,mFace.getWidth(), mFace.getHeight(), mPaint);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mFace.getWidth(), mFace.getHeight());
    }

    public CustomFace getFace(){
        return mFace;
    }

    @Override
    public boolean isSelected() {
        return mSelected;
    }

    @Override
    public void setSelected(boolean selected) {
        mSelected = selected;
    }


    public void updatePosition(int x, int y){
        setX(x);
        setY(y);
    }

    public void setOnFaceSelected(OnFaceSelected onFaceSelected){
        mOnFaceSelected = onFaceSelected;

    }

    @Override
    public void onClick(View view) {
        if(mOnFaceSelected != null)
            mOnFaceSelected.onFaceSelected(this);
    }

    public interface OnFaceSelected{
        void onFaceSelected(FaceView v);
    }
}
