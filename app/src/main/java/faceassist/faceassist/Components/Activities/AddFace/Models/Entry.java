package faceassist.faceassist.Components.Activities.AddFace.Models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by QiFeng on 2/13/17.
 */

//TODO MAKE PARCELABLE
public class Entry implements Parcelable{

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

    protected Entry(Parcel in) {
        mName = in.readString();
        mRelationship = in.readString();
        mLastViewed = in.readString();
        mBirthday = in.readString();
        mNotes = in.readString();
        mImageList = in.createTypedArray(Uri.CREATOR);
    }

    public static final Creator<Entry> CREATOR = new Creator<Entry>() {
        @Override
        public Entry createFromParcel(Parcel in) {
            return new Entry(in);
        }

        @Override
        public Entry[] newArray(int size) {
            return new Entry[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mName);
        parcel.writeString(mRelationship);
        parcel.writeString(mLastViewed);
        parcel.writeString(mBirthday);
        parcel.writeString(mNotes);
        parcel.writeTypedArray(mImageList, i);
    }
}
