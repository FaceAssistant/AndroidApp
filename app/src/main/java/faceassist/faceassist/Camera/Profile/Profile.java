package faceassist.faceassist.Camera.Profile;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

import faceassist.faceassist.Utils.JSONHelper;

/**
 * Created by QiFeng on 2/7/17.
 */

public class Profile implements Parcelable {

    private String mName;
    private double mConfidence;


    public Profile(JSONObject in){
        mName = JSONHelper.getString("name", in);
        mConfidence = JSONHelper.getDouble("confidence", in);
    }


    public String getName() {
        return mName;
    }

    public double getConfidence() {
        return mConfidence;
    }

    protected Profile(Parcel in) {
        mName = in.readString();
        mConfidence = in.readDouble();
    }

    public static final Creator<Profile> CREATOR = new Creator<Profile>() {
        @Override
        public Profile createFromParcel(Parcel in) {
            return new Profile(in);
        }

        @Override
        public Profile[] newArray(int size) {
            return new Profile[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mName);
        parcel.writeDouble(mConfidence);
    }
}
