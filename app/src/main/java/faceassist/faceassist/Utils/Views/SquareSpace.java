package faceassist.faceassist.Utils.Views;

import android.content.Context;
import android.support.v4.widget.Space;
import android.util.AttributeSet;

/**
 * Created by QiFeng on 1/30/17.
 */

public class SquareSpace extends Space {

    public SquareSpace(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SquareSpace(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareSpace(Context context) {
        super(context);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int side = widthMeasureSpec > heightMeasureSpec ? heightMeasureSpec : widthMeasureSpec;
        super.onMeasure(side, side);
    }
}
