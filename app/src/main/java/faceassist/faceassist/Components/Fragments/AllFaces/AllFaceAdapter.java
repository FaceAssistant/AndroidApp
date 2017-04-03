package faceassist.faceassist.Components.Fragments.AllFaces;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import faceassist.faceassist.Components.Activities.Profile.LovedOneProfile;
import faceassist.faceassist.R;

/**
 * Created by QiFeng on 4/2/17.
 */

public class AllFaceAdapter extends RecyclerView.Adapter<AllFaceViewHolder> {

    private OnLongPressListener mOnLongPressListener;
    private List<LovedOneProfile> mLovedOneProfiles;

    public AllFaceAdapter(List<LovedOneProfile> profiles, OnLongPressListener listener){
        mLovedOneProfiles = profiles;
        mOnLongPressListener = listener;
    }

    @Override
    public AllFaceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AllFaceViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_all_faces, parent, false), mOnLongPressListener);
    }

    @Override
    public void onBindViewHolder(AllFaceViewHolder holder, int position) {
        holder.bind(mLovedOneProfiles.get(position));
    }

    @Override
    public int getItemCount() {
        return mLovedOneProfiles.size();
    }
}
