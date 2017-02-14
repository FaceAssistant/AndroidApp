package faceassist.faceassist.Components.Fragments.Picker.Models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by QiFeng on 2/13/17.
 */

public class GalleryItem implements Parcelable{

    public final String id;
    public final Uri uri;
    public final int type;

    public final String bucketId;

    public GalleryItem(String id, Uri uri, int type, String bucketId){
        this.id = id;
        this.uri = uri;
        this.type = type;
        this.bucketId = bucketId;
    }

    protected GalleryItem(Parcel in) {
        id = in.readString();
        uri = in.readParcelable(Uri.class.getClassLoader());
        type = in.readInt();
        bucketId = in.readString();
    }

    public static final Creator<GalleryItem> CREATOR = new Parcelable.Creator<GalleryItem>() {
        @Override
        public GalleryItem createFromParcel(Parcel in) {
            return new GalleryItem(in);
        }

        @Override
        public GalleryItem[] newArray(int size) {
            return new GalleryItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeParcelable(uri, 0);
        dest.writeInt(type);
        dest.writeString(bucketId);
    }


}