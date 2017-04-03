package faceassist.faceassist.Components.Fragments.AllFaces;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import faceassist.faceassist.Components.Activities.Profile.BaseProfile;
import faceassist.faceassist.Components.Activities.Profile.LovedOneProfile;
import faceassist.faceassist.R;
import faceassist.faceassist.Utils.Base.BaseFragment;

/**
 * Created by QiFeng on 4/2/17.
 */

public class AllFacesFragment extends BaseFragment implements AllFacesContract.AllFacesView, OnLongPressListener {

    private AllFacesContract.AllFacesPresenter mAllFacesPresenter;
    private View vProgress;
    private RecyclerView vRecyclerView;
    private List<LovedOneProfile> mLovedOneProfiles = new ArrayList<>();
    private AllFaceAdapter mAllFaceAdapter;
    private View vEmptyText;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_all_faces, container, false);

        vProgress = root.findViewById(R.id.progressbar);
        vRecyclerView = (RecyclerView) root.findViewById(R.id.recycler_view);
        vEmptyText = root.findViewById(R.id.empty_text);

        return root;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAllFaceAdapter = new AllFaceAdapter(mLovedOneProfiles, this);
        vRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        vRecyclerView.setAdapter(mAllFaceAdapter);

        ((Toolbar) view.findViewById(R.id.toolbar)).setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onNavigationIconPressed(view);
            }
        });

        mAllFacesPresenter.reload();
    }

    @Override
    public void showProgress(boolean show) {
        vProgress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showList(boolean show) {
        if (show){
            if (mLovedOneProfiles.isEmpty()){
                vRecyclerView.setVisibility(View.GONE);
                vEmptyText.setVisibility(View.VISIBLE);
            }else {
                vEmptyText.setVisibility(View.GONE);
                vRecyclerView.setVisibility(View.VISIBLE);
            }
        }else {
            vEmptyText.setVisibility(View.GONE);
            vRecyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void removeItemFromRV(int pos) {
        mLovedOneProfiles.remove(pos);
        mAllFaceAdapter.notifyItemRemoved(pos);
    }

    @Override
    public void updateRV(List<LovedOneProfile> profiles) {
        mLovedOneProfiles.clear();
        mLovedOneProfiles.addAll(profiles);
        mAllFaceAdapter.notifyDataSetChanged();
    }

    @Override
    public void setPresenter(AllFacesContract.AllFacesPresenter presenter) {
        mAllFacesPresenter = presenter;
    }

    @Override
    public void showToast(@StringRes int text) {
        if (getActivity() != null)
            Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onLongPress(int pos, BaseProfile profile) {

    }
}
