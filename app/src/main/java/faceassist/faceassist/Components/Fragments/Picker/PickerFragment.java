package faceassist.faceassist.Components.Fragments.Picker;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import faceassist.faceassist.Components.Fragments.Picker.Models.BucketItem;
import faceassist.faceassist.Components.Fragments.Picker.Models.GalleryItem;
import faceassist.faceassist.Components.Fragments.Picker.RecyclerView.PickerAdapter;
import faceassist.faceassist.R;
import faceassist.faceassist.Utils.GridSpaceDecoration;

/**
 * Created by QiFeng on 2/13/17.
 */

public class PickerFragment extends Fragment implements PickerAdapter.GalleryItemSelected,
        AdapterView.OnItemSelectedListener, PickerContract.View {

    public static final String TAG = PickerFragment.class.getSimpleName();

    public List<GalleryItem> mUnfilteredGalleryItems = new ArrayList<>();
    public List<GalleryItem> mFilteredGalleryItems = new ArrayList<>();
    public List<BucketItem> mBucketList = new ArrayList<>(); //list of directories

    private View vProgress;
    private RecyclerView vRecyclerView;

    private PickerAdapter mPickerAdapter;
    private OnGalleryItemSelected mOnGalleryItemSelected;

    private Handler mHandler = new Handler();


    private AlertDialog mAlertDialog;
    private ArrayAdapter mSpinnerAdapter;
    private AppCompatSpinner vSpinner;

    private PickerContract.Presenter mPickerPresenter;



    private static final String[] mProjection = {
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Images.ImageColumns.BUCKET_ID
    };

    private static final String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
            + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

    private static final Uri queryUri = MediaStore.Files.getContentUri("external");


    public PickerFragment() {

    }

    public static PickerFragment newInstance() {
        return new PickerFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mOnGalleryItemSelected = (OnGalleryItemSelected) context;
        }catch (ClassCastException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_gallery_picker, container, false);

        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) getActivity().onBackPressed();
            }
        });


        mBucketList.add(new BucketItem("", "Gallery"));
        vSpinner = (AppCompatSpinner) toolbar.findViewById(R.id.spinner);
        vSpinner.setOnItemSelectedListener(this);
        toolbar.findViewById(R.id.spinner_arrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vSpinner.performClick();
            }
        });

        mSpinnerAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_text, mBucketList);
        mSpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown);

        vSpinner.setAdapter(mSpinnerAdapter);

        vProgress = rootView.findViewById(R.id.progress);
        vRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        vRecyclerView.setHasFixedSize(true);

        //grid layout with 3 items in each column
        vRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        vRecyclerView.addItemDecoration(new GridSpaceDecoration(3, 4, false)); //add space decoration

        mPickerAdapter = new PickerAdapter(mFilteredGalleryItems);
        mPickerAdapter.setRequestManager(Glide.with(this));
        mPickerAdapter.setGalleryItemSelected(this);

        vRecyclerView.setAdapter(mPickerAdapter);

        if (mPickerPresenter == null) {
            mPickerPresenter = new PickerPresenter(this, getLoader(), getLoaderManager());
            mPickerPresenter.setUnfilteredGalleryItems(mUnfilteredGalleryItems);
            mPickerPresenter.start();
        }

        return rootView;
    }

    public void showProgress(boolean show) {
        if (show) {
            vRecyclerView.setVisibility(View.INVISIBLE);
            vProgress.setVisibility(View.VISIBLE);
            vSpinner.setClickable(false);
        } else {
            vProgress.setVisibility(View.INVISIBLE);
            vRecyclerView.setVisibility(View.VISIBLE);
            vSpinner.setClickable(true);
        }
    }

    @Override
    public void updateBucketAndGallery(List<BucketItem> bucketItems, List<GalleryItem> galleryItems) {
        mBucketList.clear();
        mBucketList.addAll(bucketItems);

        mFilteredGalleryItems.clear();
        mFilteredGalleryItems.addAll(galleryItems);

        mSpinnerAdapter.notifyDataSetChanged();
        if (vSpinner.getSelectedItemPosition() == 0) {
            onItemSelected(null, null, 0, 0);
        }
    }

    @Override
    public void showUpdatedGallery(final List<GalleryItem> galleryItems) {
        mHandler.removeCallbacksAndMessages(null);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mFilteredGalleryItems.clear();
                mFilteredGalleryItems.addAll(galleryItems);
                mPickerAdapter.notifyDataSetChanged();
                showProgress(false);
            }
        });
    }

    @Override
    public void runClickedItem(GalleryItem item) {
        if (mOnGalleryItemSelected != null) mOnGalleryItemSelected.onGalleryItemSelected(item);
    }

    @Override
    public void showErrorToast(){
        if (getContext() != null){
            Toast.makeText(getContext(), "This file could not be opended", Toast.LENGTH_SHORT).show();
        }
    }

    private Loader<Cursor> getLoader(){
        return new CursorLoader(
                getContext(),
                queryUri,
                mProjection,
                selection,
                null, // Selection args (none).
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC" // Sort order.
        );
    }



    @Override
    public void onDestroyView() {
        //Log.i(TAG, "onDestroyView: ");
        super.onDestroyView();

        if (mPickerPresenter != null) {
            mPickerPresenter.stop();
            mPickerPresenter = null;
        }

        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
            mAlertDialog = null;
        }

        if (mPickerAdapter.getRequestManager() != null)
            mPickerAdapter.getRequestManager().onDestroy();
    }


    @Override
    public void itemClicked(GalleryItem item) {
        mPickerPresenter.clickGalleryItem(item);
    }


//    private void goToImageEdit(GalleryItem item) {
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeFile(item.path, options);
//
//        int rotation = 0;
//        try {
//            ExifInterface exif = new ExifInterface(item.path);
//            rotation = getRotation(exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        Dimens dimens = new Dimens(options.outWidth, options.outHeight);
//        dimens.setRotation(rotation);
//        if(mPostOptions == null){
//            mPostOptions = new PostOptions();
//        }
//        mPostOptions.type = PostOptions.ContentType.UploadedPhoto;
//
//        goToFragment(
//                EditFragment.newInstance(Uri.parse(item.path), mReturnType, dimens, mPostOptions),
//                EditFragment.TAG
//        );
//    }

//    private int getRotation(int exif){
//        switch (exif){
//            case ExifInterface.ORIENTATION_NORMAL:
//                return 0;
//            case ExifInterface.ORIENTATION_ROTATE_90:
//                return 90;
//            case ExifInterface.ORIENTATION_ROTATE_180:
//                return 180;
//            case ExifInterface.ORIENTATION_ROTATE_270:
//                return 270;
//            default:
//                return 0;
//        }
//    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //Log.i(TAG, "onItemSelected: " + position);
        mPickerPresenter.filter(mBucketList.get(position).id);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public interface OnGalleryItemSelected{
        void onGalleryItemSelected(GalleryItem item);
    }


}