package faceassist.faceassist.Components.Fragments.History;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import faceassist.faceassist.Components.Activities.Profile.BaseProfile;
import faceassist.faceassist.R;
import faceassist.faceassist.Utils.Base.BaseFragment;

/**
 * Created by QiFeng on 4/24/17.
 */

public class HistoryFragment extends BaseFragment implements HistoryContract.View, View.OnClickListener{

    private HistoryContract.Presenter mPresenter;
    private List<BaseProfile> mBaseProfiles = new ArrayList<>();
    private HistoryRVAdapter mHistoryRVAdapter;

    private View vProgress;
    private RecyclerView vRecyclerView;
    private View vEmptyText;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_all_faces, container, false);

        vProgress = root.findViewById(R.id.progressbar);
        vRecyclerView = (RecyclerView) root.findViewById(R.id.recycler_view);
        vEmptyText = root.findViewById(R.id.empty_text);
        vEmptyText.setOnClickListener(this);

        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mHistoryRVAdapter = new HistoryRVAdapter(mBaseProfiles, new HistoryRVVH.OnHistoryVHClickListener() {
            @Override
            public void onClickYes(int pos, BaseProfile profile) {
                if (getContext() != null)
                    Toast.makeText(getContext(), R.string.recorded_answer, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onClickNo(int pos, BaseProfile profile) {

            }
        });


        vRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        vRecyclerView.setAdapter(mHistoryRVAdapter);

        Toolbar toolbar = (Toolbar)view.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.history);
        toolbar.setNavigationOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View view) {
                onNavigationIconPressed(view);
            }
        });

        mPresenter.reload();
    }

    @Override
    public void showProgress(boolean show) {
        vProgress.setVisibility(show ? android.view.View.VISIBLE : android.view.View.GONE);
    }

    @Override
    public void showList(boolean show) {
        if (show) {
            if (mBaseProfiles.isEmpty()) {
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
    public void updateRV(List<BaseProfile> profiles) {
        mBaseProfiles.clear();
        mBaseProfiles.addAll(profiles);
        mHistoryRVAdapter.notifyDataSetChanged();
    }

    @Override
    public void setPresenter(HistoryContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showToast(@StringRes int text) {
        if (getActivity() != null)
            Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View view) {
        mPresenter.reload();
    }
}
