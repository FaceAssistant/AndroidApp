package faceassist.faceassist.Components.Activities.AddFace.RecyclerView;

import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import faceassist.faceassist.Components.Activities.AddFace.Models.TextEntry;
import faceassist.faceassist.R;

/**
 * Created by QiFeng on 2/13/17.
 */

public class TextEntryViewHolder extends RecyclerView.ViewHolder {

    private EditText vEditText;
    private TextEntry mTextEntry;

    public TextEntryViewHolder(View itemView) {
        super(itemView);

        vEditText = (EditText)itemView.findViewById(R.id.edit_text);
        vEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mTextEntry.setBody(editable.toString());
            }
        });
    }


    public void bindView(TextEntry textEntry){
        mTextEntry = textEntry;

        vEditText.setHint(textEntry.getTitle());
        vEditText.setText(textEntry.getBody());

        //todo errors
    }
}
