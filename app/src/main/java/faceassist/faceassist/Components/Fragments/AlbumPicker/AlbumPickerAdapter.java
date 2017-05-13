package faceassist.faceassist.Components.Fragments.AlbumPicker;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;

import java.util.List;

import faceassist.faceassist.R;

/**
 * Created by QiFeng on 5/12/17.
 */

public class AlbumPickerAdapter extends RecyclerView.Adapter<AlbumPickerAdapter.AlbumViewHolder> {

    private List<Album> mAlbums;
    private RequestManager mRequestManager;
    private AlbumClickedListener mAlbumClickedListener;


    public AlbumPickerAdapter(List<Album> albums){
        mAlbums = albums;
    }

    public RequestManager getRequestManager() {
        return mRequestManager;
    }

    public void setRequestManager(RequestManager requestManager) {
        mRequestManager = requestManager;
    }

    public void setAlbumClickedListener(AlbumClickedListener albumClickedListener){
        mAlbumClickedListener = albumClickedListener;
    }

    @Override
    public AlbumViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AlbumViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_album, parent, false));
    }

    @Override
    public void onBindViewHolder(AlbumViewHolder holder, int position) {
        holder.bind(mAlbums.get(position));
    }

    @Override
    public int getItemCount() {
        return mAlbums.size();
    }

    public class AlbumViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView vImageView;
        TextView vName;
        Album mAlbum;

        public AlbumViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);

            vImageView = (ImageView) itemView.findViewById(R.id.image);
            vName = (TextView) itemView.findViewById(R.id.name);
        }

        public void bind(Album album){
            mAlbum = album;


            mRequestManager.load(album.getImage())
                    .asBitmap()
                    .thumbnail(0.1f)
                    .error(R.color.pureBlack)
                    .into(vImageView);

            vName.setText(album.getName());
        }


        @Override
        public void onClick(View view) {
            if (mAlbumClickedListener != null)
                mAlbumClickedListener.onItemClicked(mAlbum);
        }
    }

    public interface AlbumClickedListener{
        void onItemClicked(Album album);
    }
}
