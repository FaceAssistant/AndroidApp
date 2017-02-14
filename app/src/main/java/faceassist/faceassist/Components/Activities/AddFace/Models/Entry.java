package faceassist.faceassist.Components.Activities.AddFace.Models;

/**
 * Created by QiFeng on 2/13/17.
 */

public abstract class Entry {

    private boolean mHasError = false;

    public abstract String getContent();

    public boolean hasError(){
        return mHasError;
    }

    public void setHasError(boolean hasError){
        mHasError = hasError;
    }
}
