package faceassist.faceassist.Components.Fragments.FacialRec.ImageView;

/**
 * Created by QiFeng on 3/1/17.
 */

public class TouchPoint {

    private float mX;
    private float mY;

    public TouchPoint(float x, float y){
        mX = x;
        mY = y;
    }

    public void setPoints(float x, float y){
        mX = x;
        mY = y;
    }

    public void changeBy(float dx, float dy){
        mX += dx;
        mY += dy;
    }

    public float getX() {
        return mX;
    }

    public float getY() {
        return mY;
    }

    public void copy(TouchPoint p){
        mX = p.mX;
        mY = p.mY;
    }

    public void scale(float scl){
        mY *= scl;
        mX *= scl;
    }

    public float getLength(){
        return (float)Math.sqrt(mY * mY + mX * mX);
    }


    public static TouchPoint subtract(TouchPoint a, TouchPoint b){
        return new TouchPoint(b.mX - a.mX, b.mY - a.mX);
    }

    public static void midpoint(TouchPoint start, TouchPoint end, TouchPoint res){
        float x = (end.mX + start.mX) / 2;
        float y = (end.mY + start.mY) / 2;
        res.setPoints(x,y);
    }

}
