package faceassist.faceassist.Components.Fragments.AllFaces;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import faceassist.faceassist.Components.Activities.Profile.LovedOneProfile;
import faceassist.faceassist.R;
import faceassist.faceassist.Utils.Base.BaseFragment;

/**
 * Created by QiFeng on 4/2/17.
 */

public class AllFacesFragment extends BaseFragment implements AllFacesContract.View, OnLongPressListener, View.OnClickListener {

    private AllFacesContract.Presenter mAllFacesPresenter;
    private android.view.View vProgress;
    private RecyclerView vRecyclerView;
    private List<LovedOneProfile> mLovedOneProfiles = new ArrayList<>();
    private AllFaceAdapter mAllFaceAdapter;
    private android.view.View vEmptyText;


    @Nullable
    @Override
    public android.view.View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        android.view.View root = inflater.inflate(R.layout.fragment_all_faces, container, false);

        vProgress = root.findViewById(R.id.progressbar);
        vRecyclerView = (RecyclerView) root.findViewById(R.id.recycler_view);
        vEmptyText = root.findViewById(R.id.empty_text);
        vEmptyText.setOnClickListener(this);

        return root;
    }


    @Override
    public void onViewCreated(android.view.View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAllFaceAdapter = new AllFaceAdapter(mLovedOneProfiles, this);
        vRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        vRecyclerView.setAdapter(mAllFaceAdapter);

        ((Toolbar) view.findViewById(R.id.toolbar)).setNavigationOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View view) {
                onNavigationIconPressed(view);
            }
        });

        mAllFacesPresenter.reload();
    }

    @Override
    public void showProgress(boolean show) {
        vProgress.setVisibility(show ? android.view.View.VISIBLE : android.view.View.GONE);
    }

    @Override
    public void showList(boolean show) {
        if (show) {
            if (mLovedOneProfiles.isEmpty()) {
                vRecyclerView.setVisibility(android.view.View.GONE);
                vEmptyText.setVisibility(android.view.View.VISIBLE);
            } else {
                vEmptyText.setVisibility(android.view.View.GONE);
                vRecyclerView.setVisibility(android.view.View.VISIBLE);
            }
        } else {
            vEmptyText.setVisibility(android.view.View.GONE);
            vRecyclerView.setVisibility(android.view.View.GONE);
        }
    }

    @Override
    public void removeItemFromRV(int pos) {
        mLovedOneProfiles.remove(pos);
        mAllFaceAdapter.notifyItemRemoved(pos);
        if (mLovedOneProfiles.isEmpty()){
            vRecyclerView.setVisibility(View.GONE);
            vEmptyText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void updateRV(List<LovedOneProfile> profiles) {
        mLovedOneProfiles.clear();
        mLovedOneProfiles.addAll(profiles);
        mAllFaceAdapter.notifyDataSetChanged();
    }

    @Override
    public void setPresenter(AllFacesContract.Presenter presenter) {
        mAllFacesPresenter = presenter;
    }

    @Override
    public void showToast(@StringRes int text) {
        if (getActivity() != null)
            Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void addItemToRV(int pos, LovedOneProfile profile) {
        int size = mLovedOneProfiles.size();
        if (pos > size){
            pos = size;
        }

        mLovedOneProfiles.add(pos, profile);
        mAllFaceAdapter.notifyItemInserted(pos);

        if (size == 0){
            vEmptyText.setVisibility(View.GONE);
            vRecyclerView.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onLongPress(final int pos, final LovedOneProfile profile) {
        if (getActivity() == null) return;
        new AlertDialog.Builder(getContext()).setItems(R.array.all_face_options,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0){
                            mAllFacesPresenter.delete(pos, profile);
                        }
                    }
                }).show();
    }

    @Override
    public void onClick(View view) {
        mAllFacesPresenter.reload();
    }
}
