package faceassist.faceassist.Components.Activities.Profile;

import android.net.Uri;
import android.os.Parcel;

import org.json.JSONObject;

import faceassist.faceassist.Utils.JSONHelper;

/**
 * Created by QiFeng on 2/7/17.
 */

public class LovedOneProfile extends BaseProfile {

    private String mRelationship;
    private String mLastViewed;
    private String mBirthday;
    private String mNote;

    public LovedOneProfile(JSONObject in){
        super(in);
        mBirthday = JSONHelper.getString("birthday", in);
        mRelationship = JSONHelper.getString("relationship", in);
        mLastViewed = JSONHelper.getString("last_viewed", in);
        mNote = JSONHelper.getString("note", in);
    }

    public LovedOneProfile(String id, String name, String birthday, String relationship, String lastViewed, String note){
        super(id, name);
        mBirthday = birthday;
        mRelationship = relationship;
        mLastViewed = lastViewed;
        mNote = note;
    }

    public String getRelationship() {
        return mRelationship;
    }

    public String getLastViewed() {
        return mLastViewed;
    }

    public String getBirthday() {
        return mBirthday;
    }

    public String getNote() {
        return mNote;
    }

    protected LovedOneProfile(Parcel in) {
        super(in.readString(), in.readString());
        mRelationship = in.readString();
        mBirthday = in.readString();
        mLastViewed = in.readString();
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
        parcel.writeString(mBirthday);
        parcel.writeString(mLastViewed);
        parcel.writeString(mNote);
    }
}
