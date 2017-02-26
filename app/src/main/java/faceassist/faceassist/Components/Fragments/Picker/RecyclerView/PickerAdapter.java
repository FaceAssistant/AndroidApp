package faceassist.faceassist.Components.Fragments.Picker.RecyclerView;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.RequestManager;

import java.util.List;

import faceassist.faceassist.Components.Fragments.Picker.Models.GalleryItem;
import faceassist.faceassist.R;

/**
 * Created by QiFeng on 2/13/17.
 */

public class PickerAdapter extends RecyclerView.Adapter<PickerAdapter.GalleryViewHolder> {

    private List<GalleryItem> mGalleryItems;
    private GalleryItemSelected mGalleryItemSelected;
    private RequestManager mRequestManager;


    public PickerAdapter(List<GalleryItem> items) {
        mGalleryItems = items;
    }

    public void setGalleryItemSelected(GalleryItemSelected galleryItemSelected) {
        mGalleryItemSelected = galleryItemSelected;
    }

    public RequestManager getRequestManager() {
        return mRequestManager;
    }

    public void setRequestManager(RequestManager requestManager) {
        mRequestManager = requestManager;
    }

    @Override
    public GalleryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new GalleryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_picker, parent, false));
    }

    @Override
    public void onBindViewHolder(GalleryViewHolder holder, int position) {
        holder.bindView(mGalleryItems.get(position));
    }

    @Override
    public int getItemCount() {
        return mGalleryItems.size();
    }

    public class GalleryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView vImageView;
        GalleryItem mGalleryItem;

        public GalleryViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            vImageView = (ImageView) itemView.findViewById(R.id.image);
        }


        public void bindView(GalleryItem item) {
            mGalleryItem = item;

            mRequestManager.load(item.uri)
                    .asBitmap()
                    .thumbnail(0.1f)
                    .into(vImageView);

        }

        @Override
        public void onClick(View v) {
            if (mGalleryItemSelected != null) {
                mGalleryItemSelected.itemClicked(mGalleryItem);
            }
        }
    }


    public interface GalleryItemSelected {
        void itemClicked(GalleryItem item);
    }


}