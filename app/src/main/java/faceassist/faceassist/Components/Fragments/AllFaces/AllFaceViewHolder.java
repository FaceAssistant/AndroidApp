package faceassist.faceassist.Components.Fragments.AllFaces;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import faceassist.faceassist.Components.Activities.Profile.LovedOneProfile;
import faceassist.faceassist.R;

/**
 * Created by QiFeng on 4/2/17.
 */
public class AllFaceViewHolder extends RecyclerView.ViewHolder {

    private TextView vName;
    private TextView vRelationship;
    private TextView vBirthday;
    private TextView vNotes;
    private LovedOneProfile mBaseProfile;

    public AllFaceViewHolder(View itemView, final OnLongPressListener longPressListener) {
        super(itemView);

        vName = (TextView) itemView.findViewById(R.id.name);
        vRelationship = (TextView) itemView.findViewById(R.id.relationship);
        vBirthday = (TextView) itemView.findViewById(R.id.birthday);
        vNotes = (TextView) itemView.findViewById(R.id.notes);

        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                longPressListener.onLongPress(getAdapterPosition(), mBaseProfile);
                return true;
            }
        });

    }


    public void bind(LovedOneProfile profile){
        mBaseProfile = profile;
        vName.setText(mBaseProfile.getName());
        vRelationship.setText(mBaseProfile.getRelationship());
        vBirthday.setText(mBaseProfile.getBirthday());
        vNotes.setText(mBaseProfile.getNote());
    }
}
