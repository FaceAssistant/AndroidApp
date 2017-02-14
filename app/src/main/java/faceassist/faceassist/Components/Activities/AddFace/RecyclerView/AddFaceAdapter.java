package faceassist.faceassist.Components.Activities.AddFace.RecyclerView;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import faceassist.faceassist.Components.Activities.AddFace.Models.ButtonEntry;
import faceassist.faceassist.Components.Activities.AddFace.Models.Entry;
import faceassist.faceassist.Components.Activities.AddFace.Models.ImageEntry;
import faceassist.faceassist.Components.Activities.AddFace.Models.TextEntry;
import faceassist.faceassist.R;

/**
 * Created by QiFeng on 2/13/17.
 */

public class AddFaceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String NAME_KEY = "name";

    private static final int VIEW_TEXT = 0;
    private static final int VIEW_IMAGE = 1;
    private static final int VIEW_BUTTON = 2;
    private ImageEntryViewHolder.OnImageClick mOnImageClick;

    private ArrayList<Entry> mEntries = new ArrayList<>();

    public AddFaceAdapter(){
        mEntries.add(new TextEntry(NAME_KEY));

        for (int i = 0; i < 12; i++){
            mEntries.add(new ImageEntry());
        }

        mEntries.add(new ButtonEntry("Upload"));

    }

    public void setOnImageClick(ImageEntryViewHolder.OnImageClick c){
        mOnImageClick = c;
    }

    public void setConfirmButton(View.OnClickListener onClickListener){
        ((ButtonEntry)mEntries.get(mEntries.size() - 1)).setOnClickListener(onClickListener);
    }

    public ArrayList<Entry> getEntries() {
        return mEntries;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType){
            case VIEW_TEXT:
                return new TextEntryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_text_entry, parent, false));
            case VIEW_IMAGE:
                return new ImageEntryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_image_entry, parent, false), mOnImageClick);
            case VIEW_BUTTON:
                return new ButtonEntryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_button_entry, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TextEntryViewHolder)
            ((TextEntryViewHolder)holder).bindView((TextEntry) mEntries.get(position));
        else if (holder instanceof ImageEntryViewHolder)
            ((ImageEntryViewHolder) holder).bindView((ImageEntry) mEntries.get(position));
        else if (holder instanceof ButtonEntryViewHolder)
            ((ButtonEntryViewHolder) holder).bindView((ButtonEntry) mEntries.get(position));
    }

    @Override
    public int getItemCount() {
        return mEntries.size();
    }

    @Override
    public int getItemViewType(int position) {
        Entry e = mEntries.get(position);
        if (e instanceof TextEntry)
            return VIEW_TEXT;
        else if (e instanceof ImageEntry)
            return VIEW_IMAGE;
        else
            return VIEW_BUTTON;

    }
}
