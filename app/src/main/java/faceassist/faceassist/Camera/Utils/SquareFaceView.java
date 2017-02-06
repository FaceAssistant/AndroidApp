package faceassist.faceassist.Camera.Utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

import com.google.android.gms.vision.face.Face;

/**
 * Created by QiFeng on 2/5/17.
 */

public class SquareFaceView extends View {

    private Face mFace;
    private Paint mPaint = new Paint();
    private OnFaceSelected mOnFaceSelected;

    private boolean mSelected = false;

    public SquareFaceView(Context context, Face face) {
        super(context);
        init(face);
    }


    private void init(Face face){
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
        setMeasuredDimension((int)mFace.getWidth(), (int)mFace.getHeight());
    }

    public Face getFace(){
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
        setX(mFace.getPosition().x);
        setY(mFace.getPosition().y);
    }

    public void setOnFaceSelected(OnFaceSelected onFaceSelected){
        mOnFaceSelected = onFaceSelected;

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("FACE", "onClick: ");
                if(mOnFaceSelected != null)
                    mOnFaceSelected.onFaceSelected(SquareFaceView.this);
            }
        });
    }

    public interface OnFaceSelected{
        void onFaceSelected(SquareFaceView v);
    }
}
