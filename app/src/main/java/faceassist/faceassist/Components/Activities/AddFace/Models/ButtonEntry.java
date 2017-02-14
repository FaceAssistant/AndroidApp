package faceassist.faceassist.Components.Activities.AddFace.Models;

import android.view.View;

/**
 * Created by QiFeng on 2/13/17.
 */

public class ButtonEntry extends Entry {

    String mTitle;

    View.OnClickListener mOnClickListener;

    public ButtonEntry(String title){
        mTitle = title;
    }

    public String getTitle() {
        return mTitle;
    }

    public View.OnClickListener getOnClickListener() {
        return mOnClickListener;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    @Override
    public String getContent() {
        return null;
    }
}
