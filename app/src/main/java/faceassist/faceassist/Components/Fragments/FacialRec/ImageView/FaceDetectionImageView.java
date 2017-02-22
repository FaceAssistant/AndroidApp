package faceassist.faceassist.Components.Fragments.FacialRec.ImageView;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import faceassist.faceassist.Utils.Views.SquareFrameLayout;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
/**
 * Created by QiFeng on 2/21/17.
 */

public class FaceDetectionImageView extends SquareFrameLayout implements FaceView.OnFaceSelected {

    private ImageView vImageView;
    private List<CustomFace> mCustomFaces = new ArrayList<>();
    private FaceView vSelectedFace;

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

    private void init(Context context){
        vImageView = new ImageView(context);
        LayoutParams params = new LayoutParams(MATCH_PARENT, MATCH_PARENT);
        vImageView.setLayoutParams(params);
        vImageView.setScaleType(ImageView.ScaleType.MATRIX);
        addView(vImageView);
    }


    public void setBitmap(Bitmap bitmap){ //add runnable ?
        vImageView.setImageBitmap(bitmap);
    }


    public FaceView getSelectedFace(){
        return vSelectedFace;
    }

    public void clearFaceViews(){
        //note: assumes ImageView is the only other child
        vSelectedFace = null;
        removeViews(1, mCustomFaces.size());

        mCustomFaces.clear();
    }

    public void addFace(CustomFace face){
        mCustomFaces.add(face);
        FaceView faceView = new FaceView(getContext(), face);
        faceView.setOnFaceSelected(this);
        addView(faceView);

        //update position
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

    public interface ImageManipulationListener{
        // change in scale value
        void onScaled(float dscaled);
        void onPositionChanged(float dx, float dy);
    }
}

