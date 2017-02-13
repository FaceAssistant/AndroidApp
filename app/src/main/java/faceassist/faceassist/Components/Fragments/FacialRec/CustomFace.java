package faceassist.faceassist.Components.Fragments.FacialRec;

import android.graphics.PointF;

import com.google.android.gms.vision.face.Face;

import java.util.Locale;

/**
 * Created by QiFeng on 2/7/17.
 */

// so our values are saved as int
// also solves case of face going out of image bounds
public class CustomFace {

    public final int x;
    public final int y;
    private int width;
    private int height;

    public CustomFace(Face face, int maxX, int maxY){
        PointF point = face.getPosition();

        //start less than 0
        if (point.x < 0){
            x = 0;
            width = (int)(face.getWidth() + point.x);
        }else {
            x = (int) point.x;
            width = (int) face.getWidth();
        }

        //end greater than image
        if (width + x > maxX) width = maxX - x;

        //top less than 0
        if (point.y < 0){
            y = 0;
            height = (int)(face.getHeight() + point.y);
        }else {
            y = (int)point.y;
            height = (int)face.getHeight();
        }

        //bottom greater than image
        if (height + y > maxY) height = maxY - y;
    }


    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH,"(%d, %d)", x, y);
    }
}
