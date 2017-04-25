package faceassist.faceassist.Components.Fragments.Friends;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import faceassist.faceassist.Components.Activities.Profile.LovedOneProfile;
import faceassist.faceassist.R;

/**
 * Created by QiFeng on 4/24/17.
 */

public class FriendsRVAdapter extends RecyclerView.Adapter<FriendsVH>{

    private List<LovedOneProfile> mLovedOneProfiles;
    private FriendsVH.OnFriendsVHClicked mOnFriendsVHClicked;

    public FriendsRVAdapter(List<LovedOneProfile> profiles, FriendsVH.OnFriendsVHClicked onFriendsVHClicked){
        mLovedOneProfiles = profiles;
        mOnFriendsVHClicked = onFriendsVHClicked;
    }


    @Override
    public FriendsVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FriendsVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_friends, parent, false),
                mOnFriendsVHClicked);
    }

    @Override
    public void onBindViewHolder(FriendsVH holder, int position) {
        holder.bind(mLovedOneProfiles.get(position));
    }

    @Override
    public int getItemCount() {
        return mLovedOneProfiles.size();
    }
}
