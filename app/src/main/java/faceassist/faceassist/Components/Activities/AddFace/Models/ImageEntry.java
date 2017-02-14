package faceassist.faceassist.Components.Activities.AddFace.Models;

import android.net.Uri;

/**
 * Created by QiFeng on 2/13/17.
 */

public class ImageEntry extends Entry {

    private Uri mImageUri;

    public ImageEntry(){

    }

    @Override
    public String getContent() {
        return mImageUri.getPath();
    }


    public boolean hasImage(){
        return mImageUri != null;
    }

    public Uri getImageUri(){
        return mImageUri;
    }

    public void setImageUri(Uri image){
        mImageUri = image;
    }

}
