package faceassist.faceassist.Components.Activities.AddFace.Models;

import android.net.Uri;

import java.util.ArrayList;

/**
 * Created by QiFeng on 2/13/17.
 */

//TODO MAKE PARCELABLE
public class Entry {

    private String mName;
    private String mRelationship;
    private String mLastViewed;
    private String mBirthday;
    private String mNotes;
    private Uri[] mImageList;

    public Entry(int numOfImages){
        mName = "";
        mRelationship = "";
        mLastViewed = "";
        mBirthday = "";
        mNotes = "";
        mImageList = new Uri[numOfImages];
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getRelationship() {
        return mRelationship;
    }

    public void setRelationship(String relationship) {
        mRelationship = relationship;
    }

    public String getNotes() {
        return mNotes;
    }

    public void setNotes(String notes) {
        mNotes = notes;
    }

    public Uri[] getImageList() {
        return mImageList;
    }

    public String getBirthday() {
        return mBirthday;
    }

    public void setBirthday(String birthday) {
        mBirthday = birthday;
    }

    public String getLastViewed() {
        return mLastViewed;
    }

    public void setLastViewed(String lastViewed) {
        mLastViewed = lastViewed;
    }

    public void setImageListItem(int index, Uri uri) {
        if (index < 0 || index >= mImageList.length) return;
        mImageList[index] = uri;
    }
}
