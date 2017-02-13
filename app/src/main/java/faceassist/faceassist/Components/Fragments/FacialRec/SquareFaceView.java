package faceassist.faceassist.Components.Fragments.FacialRec;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

/**
 * Created by QiFeng on 2/5/17.
 */

public class SquareFaceView extends View {

    private CustomFace mFace;
    private Paint mPaint = new Paint();
    private OnFaceSelected mOnFaceSelected;

    private boolean mSelected = false;

    public SquareFaceView(Context context, CustomFace face) {
        super(context);
        init(face);
    }


    private void init(CustomFace face){
        mFace = face;
        mPaint.setStrokeWidth(6);
        mPaint.setStyle(Paint.Style.STROKE);
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


    public void updatePosition(){
        setX(mFace.x);
        setY(mFace.y);
    }

    public void setOnFaceSelected(OnFaceSelected onFaceSelected){
        mOnFaceSelected = onFaceSelected;

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //Log.i("FACE", "onClick: ");
                if(mOnFaceSelected != null)
                    mOnFaceSelected.onFaceSelected(SquareFaceView.this);
            }
        });
    }

    public interface OnFaceSelected{
        void onFaceSelected(SquareFaceView v);
    }
}
