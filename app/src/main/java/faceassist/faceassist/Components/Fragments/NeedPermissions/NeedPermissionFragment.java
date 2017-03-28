package faceassist.faceassist.Components.Fragments.NeedPermissions;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import faceassist.faceassist.R;

/**
 * Created by QiFeng on 1/31/17.
 */

public class NeedPermissionFragment extends Fragment {

    public static final String TAG = NeedPermissionFragment.class.getSimpleName();
    private OnCheckPermissionClicked mOnCheckPermissionClicked;

    private static final String TITLE_ID = "title_id";
    private static final String TEXT_ID = "text_id";

    @StringRes
    private int mTitleId;

    @StringRes
    private int mTextId;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try{
            mOnCheckPermissionClicked = (OnCheckPermissionClicked) context;
        } catch (ClassCastException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnCheckPermissionClicked = null;
    }

    public static NeedPermissionFragment newInstance(@StringRes int title, @StringRes int text) {
        NeedPermissionFragment fragment = new NeedPermissionFragment();

        Bundle bundle = new Bundle();
        bundle.putInt(TITLE_ID, title);
        bundle.putInt(TEXT_ID, text);

        fragment.setArguments(bundle);

        return fragment;
    }


    public NeedPermissionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_need_permission, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null){
            mTitleId = getArguments().getInt(TITLE_ID);
            mTextId = getArguments().getInt(TEXT_ID);
        }

        view.findViewById(R.id.needPermission_text_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnCheckPermissionClicked != null)
                    mOnCheckPermissionClicked.onCheckPermissionClicked();
            }
        });

        ((TextView) view.findViewById(R.id.title)).setText(mTitleId);
        ((TextView) view.findViewById(R.id.text)).setText(mTextId);
    }


    public interface OnCheckPermissionClicked {
        void onCheckPermissionClicked();
    }
}
