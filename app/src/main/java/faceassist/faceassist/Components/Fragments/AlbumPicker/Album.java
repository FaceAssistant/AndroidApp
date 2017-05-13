package faceassist.faceassist.Components.Fragments.AlbumPicker;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by QiFeng on 5/12/17.
 */

public class Album implements Parcelable {


    private Uri mPath;
    private String mName;
    private Uri mImage;

    public Album(Uri path, String name, Uri image){
        mImage = image;
        mName = name;
        mPath = path;
    }


    public Uri getPath() {
        return mPath;
    }

    public String getName() {
        return mName;
    }

    public Uri getImage() {
        return mImage;
    }

    protected Album(Parcel in) {
        mPath = in.readParcelable(Uri.class.getClassLoader());
        mName = in.readString();
        mImage = in.readParcelable(Uri.class.getClassLoader());
    }

    public static final Creator<Album> CREATOR = new Creator<Album>() {
        @Override
        public Album createFromParcel(Parcel in) {
            return new Album(in);
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(mPath, i);
        parcel.writeString(mName);
        parcel.writeParcelable(mImage, i);
    }

    @Override
    public int hashCode() {
        return mPath.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Album && ((Album)obj).getPath().equals(mPath);
    }
}
