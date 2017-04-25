package faceassist.faceassist.Components.Fragments.Friends;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import faceassist.faceassist.Components.Activities.Profile.LovedOneProfile;
import faceassist.faceassist.R;

/**
 * Created by QiFeng on 4/24/17.
 */
public class FriendsVH extends RecyclerView.ViewHolder{

    private OnFriendsVHClicked mOnFriendsVHClicked;
    private LovedOneProfile mLovedOneProfile;

    private TextView vName;
    private TextView vRelationship;

    public FriendsVH(View itemView, OnFriendsVHClicked friendsVHClicked) {
        super(itemView);
        mOnFriendsVHClicked = friendsVHClicked;
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnFriendsVHClicked != null)
                    mOnFriendsVHClicked.onFriendsVHClicked(getAdapterPosition(), mLovedOneProfile);
            }
        });

        vName = (TextView) itemView.findViewById(R.id.name);
        vRelationship = (TextView) itemView.findViewById(R.id.relationship);
    }


    public void bind(LovedOneProfile profile){
        mLovedOneProfile = profile;

        vName.setText(profile.getName());
        vRelationship.setText(profile.getRelationship());
    }


    public interface OnFriendsVHClicked{
        public void onFriendsVHClicked(int pos, LovedOneProfile profile);
    }
}


