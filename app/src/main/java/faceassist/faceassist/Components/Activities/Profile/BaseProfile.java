package faceassist.faceassist.Components.Activities.Profile;

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

    private String mName;
    private double mConfidence;

    public BaseProfile(JSONObject object){
        mName = JSONHelper.getString("name", object);
        mConfidence = JSONHelper.getDouble("confidence",object);
    }


    public BaseProfile(String name, double confidence){
        mName = name;
        mConfidence = confidence;
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

    public double getConfidence() {
        return mConfidence;
    }

    protected BaseProfile(Parcel in){
        mName = in.readString();
        mConfidence = in.readDouble();
    }

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
