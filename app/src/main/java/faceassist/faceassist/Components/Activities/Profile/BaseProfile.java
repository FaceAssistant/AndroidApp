package faceassist.faceassist.Components.Activities.Profile;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

import faceassist.faceassist.Utils.JSONHelper;

/**
 * Created by QiFeng on 3/20/17.
 */

public class BaseProfile implements Parcelable{

    public static final String TYPE_LOVED_ONE = "loved-one";
    public static final String TYPE_CELEB = "celeb";

    private String mId;
    private String mName;
    private Uri mImage = null;

    public BaseProfile(JSONObject object){
        mId = JSONHelper.getString("id", object);
        mName = JSONHelper.getString("name", object);
    }

    public BaseProfile(String id, String name){
        mId = id;
        mName = name;
    }

    public Uri getImage() {
        return mImage;
    }

    public void setImage(Uri image) {
        mImage = image;
    }

    public String getId(){
        return mId;
    }

    public static final Creator<BaseProfile> CREATOR = new Creator<BaseProfile>() {
        @Override
        public BaseProfile createFromParcel(Parcel in) {
            return new BaseProfile(in);
        }

        @Override
        public BaseProfile[] newArray(int size) {
            return new BaseProfile[size];
        }
    };

    public String getName() {
        return mName;
    }


    protected BaseProfile(Parcel in){
        mId = in.readString();
        mName = in.readString();
        mImage = in.readParcelable(Uri.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mId);
        parcel.writeString(mName);
        parcel.writeParcelable(mImage, i);
    }
}
