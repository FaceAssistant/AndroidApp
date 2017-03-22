package faceassist.faceassist.Components.Activities.AddFace.RecyclerView;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;



import faceassist.faceassist.R;

/**
 * Created by QiFeng on 2/13/17.
 */

public class AddFaceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_IMAGE = 0;

    private ImageEntryViewHolder.OnImageClick mOnImageClick;

    private Uri[] mImageUris;

    public AddFaceAdapter(Uri[] imageUris) {
        mImageUris = imageUris;
    }

    public void setOnImageClick(ImageEntryViewHolder.OnImageClick c) {
        mOnImageClick = c;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ImageEntryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_image_entry, parent, false), mOnImageClick);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((ImageEntryViewHolder) holder).bindView(mImageUris[position]);
    }

    @Override
    public int getItemCount() {
        return mImageUris.length;
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_IMAGE;
    }
}
