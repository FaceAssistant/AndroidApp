package faceassist.faceassist.Components.Fragments.History;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import faceassist.faceassist.Components.Activities.Profile.BaseProfile;
import faceassist.faceassist.R;

/**
 * Created by QiFeng on 4/24/17.
 */

public class HistoryRVAdapter extends RecyclerView.Adapter<HistoryRVVH> {

    private List<BaseProfile> mBaseProfiles;
    private HistoryRVVH.OnHistoryVHClickListener mOnHistoryVHClickListener;

    public HistoryRVAdapter(List<BaseProfile> profiles, HistoryRVVH.OnHistoryVHClickListener onHistoryVHClickListener){
        mBaseProfiles = profiles;
        mOnHistoryVHClickListener = onHistoryVHClickListener;
    }


    @Override
    public HistoryRVVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new HistoryRVVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_history, parent, false),
                mOnHistoryVHClickListener);
    }

    @Override
    public void onBindViewHolder(HistoryRVVH holder, int position) {
        holder.onBind(mBaseProfiles.get(position));
    }

    @Override
    public int getItemCount() {
        return mBaseProfiles.size();
    }
}
