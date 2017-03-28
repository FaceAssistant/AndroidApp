package faceassist.faceassist.Components.Activities.Profile;

import android.os.Parcel;

import org.json.JSONObject;

import faceassist.faceassist.Utils.JSONHelper;

/**
 * Created by QiFeng on 2/7/17.
 */

public class LovedOneProfile extends BaseProfile {

    private String mRelationship;
    private long mLastViewed;
    private String mNote;


    public LovedOneProfile(JSONObject in){
        super(JSONHelper.getString("name", in), JSONHelper.getDouble("confidence", in));
        mRelationship = JSONHelper.getString("relationship", in);
       //todo mLastViewed
        mNote = JSONHelper.getString("note", in);
    }

    public String getRelationship() {
        return mRelationship;
    }

    public long getLastViewed() {
        return mLastViewed;
    }

    public String getNote() {
        return mNote;
    }

    protected LovedOneProfile(Parcel in) {
        super(in.readString(), in.readDouble());
        mRelationship = in.readString();
        mLastViewed = in.readLong();
        mNote = in.readString();
    }

    public static final Creator<LovedOneProfile> CREATOR = new Creator<LovedOneProfile>() {
        @Override
        public LovedOneProfile createFromParcel(Parcel in) {
            return new LovedOneProfile(in);
        }

        @Override
        public LovedOneProfile[] newArray(int size) {
            return new LovedOneProfile[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeString(mRelationship);
        parcel.writeLong(mLastViewed);
        parcel.writeString(mNote);
    }
}
