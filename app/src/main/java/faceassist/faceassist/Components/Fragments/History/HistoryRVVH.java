package faceassist.faceassist.Components.Fragments.History;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import faceassist.faceassist.Components.Activities.Profile.BaseProfile;
import faceassist.faceassist.R;

/**
 * Created by QiFeng on 4/24/17.
 */
public class HistoryRVVH extends RecyclerView.ViewHolder implements View.OnClickListener {

    private OnHistoryVHClickListener mOnHistoryVHClickListener;
    private TextView vNameText;

    private BaseProfile mBaseProfile;

    private View vButtons;
    private View vAnsweredText;

    public HistoryRVVH(View itemView, OnHistoryVHClickListener listener) {
        super(itemView);
        mOnHistoryVHClickListener = listener;

        vNameText = (TextView) itemView.findViewById(R.id.name);

        vButtons = itemView.findViewById(R.id.buttons);
        vAnsweredText = itemView.findViewById(R.id.answered_text);

        vButtons.findViewById(R.id.yes_button).setOnClickListener(this);
        vButtons.findViewById(R.id.no_button).setOnClickListener(this);

    }

    public void onBind(BaseProfile profile) {
        mBaseProfile = profile;

        vNameText.setText(mBaseProfile.getName());

        //// TODO: 4/24/17 visibility
    }

    @Override
    public void onClick(View view) {
        //// TODO: 4/24/17 check answer

        if (mOnHistoryVHClickListener != null) {
            if (view.getId() == R.id.yes_button) {
                mOnHistoryVHClickListener.onClickYes(getAdapterPosition(), mBaseProfile);
            } else {
                mOnHistoryVHClickListener.onClickNo(getAdapterPosition(), mBaseProfile);
            }
        }

        vButtons.setVisibility(View.INVISIBLE);
        vAnsweredText.setVisibility(View.VISIBLE);
    }


    public interface OnHistoryVHClickListener {
        public void onClickYes(int pos, BaseProfile profile);

        public void onClickNo(int pos, BaseProfile profile);
    }
}
