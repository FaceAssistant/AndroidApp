package faceassist.faceassist.Components.Activities.AddFace.RecyclerView;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import faceassist.faceassist.Components.Activities.AddFace.Models.ImageEntry;
import faceassist.faceassist.R;

/**
 * Created by QiFeng on 2/13/17.
 */

public class ImageEntryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public static final int REQUEST_CODE_BASE = 3245;

    private ImageView vImageView;
    private ImageEntry mImageEntry;
    private OnImageClick mOnImageClick;

    public ImageEntryViewHolder(View itemView, OnImageClick onImageClick) {
        super(itemView);
        mOnImageClick = onImageClick;

        itemView.setOnClickListener(this);
        vImageView = (ImageView) itemView.findViewById(R.id.image_view);

    }

    public void bindView(ImageEntry entry){
        mImageEntry = entry;

        Glide.with(itemView.getContext())
                .load(mImageEntry.getImageUri() == null ? R.drawable.ic_blank_profile_picture : mImageEntry.getImageUri())
                .into(vImageView);

        //todo errors

    }

    @Override
    public void onClick(View view) {
        if (mOnImageClick != null)
            mOnImageClick.onImageClick(REQUEST_CODE_BASE + getAdapterPosition());
    }

    public interface OnImageClick{
        void onImageClick(int requestCode);
    }
}
