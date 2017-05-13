package faceassist.faceassist.Components.Fragments.AlbumPicker;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import faceassist.faceassist.R;
import faceassist.faceassist.Utils.GridSpaceDecoration;
import faceassist.faceassist.Utils.OnItemSelected;

/**
 * Created by QiFeng on 5/12/17.
 */

public class AlbumPickerFragment extends Fragment implements AlbumPickerContract.View, AlbumPickerAdapter.AlbumClickedListener {


    private List<Album> mAlbums = new ArrayList<>();
    private View vProgress;
    private View vEmpty;
    private RecyclerView vRecyclerView;

    private AlbumPickerAdapter mAlbumPickerAdapter;
    private AlbumItemSelected mAlbumOnItemSelected;

    private Handler mHandler = new Handler();
    private AlbumPickerContract.Presenter mPickerPresenter;


    public AlbumPickerFragment(){

    }

    public static AlbumPickerFragment newInstance(){
        return new AlbumPickerFragment();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mAlbumOnItemSelected = (AlbumItemSelected) context;
        }catch (ClassCastException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mAlbumOnItemSelected = null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_all_faces, container, false);

        Toolbar toolbar = (Toolbar) root.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) getActivity().onBackPressed();
            }
        });
        toolbar.setTitle(R.string.albums);

        vEmpty = root.findViewById(R.id.empty_text);
        vProgress = root.findViewById(R.id.progressbar);
        vRecyclerView = (RecyclerView) root.findViewById(R.id.recycler_view);
        vRecyclerView.setHasFixedSize(true);

        vRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        vRecyclerView.addItemDecoration(new GridSpaceDecoration(3, 4, false)); //add space decoration

        mAlbumPickerAdapter = new AlbumPickerAdapter(mAlbums);
        mAlbumPickerAdapter.setRequestManager(Glide.with(this));
        mAlbumPickerAdapter.setAlbumClickedListener(this);

        vRecyclerView.setAdapter(mAlbumPickerAdapter);

        if (mPickerPresenter != null)
            mPickerPresenter.start();

        return root;
    }

    @Override
    public void showProgress(boolean show) {
        if (show){
            vEmpty.setVisibility(View.GONE);
            vRecyclerView.setVisibility(View.GONE);
            vProgress.setVisibility(View.VISIBLE);
        }else {
            vProgress.setVisibility(View.GONE);
            if (mAlbums.isEmpty()){
                vRecyclerView.setVisibility(View.GONE);
                vEmpty.setVisibility(View.VISIBLE);
            }else {
                vEmpty.setVisibility(View.GONE);
                vRecyclerView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void updateAlbums(final List<Album> albums) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mAlbums.clear();
                mAlbums.addAll(albums);
                mAlbumPickerAdapter.notifyDataSetChanged();
                showProgress(false);
            }
        });
    }

    @Override
    public void runClickedItem(Album item) {
        mPickerPresenter.clickItem(item);
    }

    @Override
    public void showErrorToast() {
        if (getContext() != null)
            Toast.makeText(getContext(), R.string.error_decoding_image, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setPresenter(AlbumPickerContract.Presenter presenter) {
        mPickerPresenter = presenter;

    }

    @Override
    public void onItemClicked(Album album) {
        if (mAlbumOnItemSelected != null){
            mAlbumOnItemSelected.onItemSelected(album);
        }
    }

    public interface AlbumItemSelected extends OnItemSelected<Album>{}

}
