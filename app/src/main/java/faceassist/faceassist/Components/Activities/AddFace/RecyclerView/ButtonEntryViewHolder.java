package faceassist.faceassist.Components.Activities.AddFace.RecyclerView;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import faceassist.faceassist.Components.Activities.AddFace.Models.ButtonEntry;
import faceassist.faceassist.R;

/**
 * Created by QiFeng on 2/13/17.
 */

public class ButtonEntryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    private Button vButton;
    private ButtonEntry mButtonEntry;

    public ButtonEntryViewHolder(View itemView) {
        super(itemView);

        vButton = (Button) itemView.findViewById(R.id.button);
        vButton.setOnClickListener(this);
    }

    public void bindView(ButtonEntry entry){
        mButtonEntry = entry;
        vButton.setText(entry.getTitle());
    }

    @Override
    public void onClick(View view) {
        if (mButtonEntry != null && mButtonEntry.getOnClickListener() != null)
            mButtonEntry.getOnClickListener().onClick(view);
    }
}
