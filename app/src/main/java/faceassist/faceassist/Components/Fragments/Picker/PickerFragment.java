package faceassist.faceassist.Components.Fragments.Picker;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import faceassist.faceassist.Components.Fragments.Picker.Models.BucketItem;
import faceassist.faceassist.Components.Fragments.Picker.Models.GalleryItem;
import faceassist.faceassist.Components.Fragments.Picker.RecyclerView.PickerAdapter;
import faceassist.faceassist.R;
import faceassist.faceassist.Utils.GridSpaceDecoration;

/**
 * Created by QiFeng on 2/13/17.
 */

public class PickerFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        PickerAdapter.GalleryItemSelected, AdapterView.OnItemSelectedListener {

    public static final String TAG = PickerFragment.class.getSimpleName();

    public ArrayList<GalleryItem> mUnfilteredGalleryItems = new ArrayList<>();
    public ArrayList<GalleryItem> mFiltedGalleryItems = new ArrayList<>();
    public ArrayList<BucketItem> mBucketList = new ArrayList<>(); //list of directories

    private View vProgress;
    private RecyclerView vRecyclerView;

    private PickerAdapter mPickerAdapter;
    private OnGalleryItemSelected mOnGalleryItemSelected;

    private Handler mHandler = new Handler();

    private static final int LOADER_ID = 0;

    final String[] mProjection = {
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Images.ImageColumns.BUCKET_ID
    };


    private AlertDialog mAlertDialog;
    private ArrayAdapter mSpinnerAdapter;
    private AppCompatSpinner vSpinner;

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
        //vSpinner.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.pure_white), PorterDuff.Mode.SRC_ATOP);
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

        mPickerAdapter = new PickerAdapter(mFiltedGalleryItems);
        mPickerAdapter.setRequestManager(Glide.with(this));
        mPickerAdapter.setGalleryItemSelected(this);

        vRecyclerView.setAdapter(mPickerAdapter);

        getLoaderManager().initLoader(LOADER_ID, null, this);

        return rootView;
    }

    private void showProgress(boolean show) {
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
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        showProgress(true);

        if (id == LOADER_ID) {
            String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                    + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

            Uri queryUri = MediaStore.Files.getContentUri("external");

            if (getContext() == null) return null;

            return new CursorLoader(
                    getContext(),
                    queryUri,
                    mProjection,
                    selection,
                    null, // Selection args (none).
                    MediaStore.Files.FileColumns.DATE_ADDED + " DESC" // Sort order.
            );
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor cursor) {
        int pathIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
        int idIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns._ID);
        int mediaIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE);
        int bucketName = cursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME);
        int bucketId = cursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_ID);


        final HashSet<BucketItem> bucketItems = new HashSet<>();

        mUnfilteredGalleryItems.clear();

        if (cursor.moveToFirst()) {
            do {
                String buckId = cursor.getString(bucketId);
                File f = new File(cursor.getString(pathIndex));
                if (!f.getPath().endsWith(".gif")) {
                    mUnfilteredGalleryItems.add(
                            new GalleryItem(
                                    cursor.getString(idIndex),
                                    Uri.fromFile(f),
                                    cursor.getInt(mediaIndex),
                                    buckId
                            )
                    );

                    bucketItems.add(new BucketItem(buckId, cursor.getString(bucketName)));
                }

            } while (cursor.moveToNext());
        }

        mBucketList.clear();
        mBucketList.add(new BucketItem("", "Gallery")); //option for all images
        mBucketList.addAll(bucketItems);
        mSpinnerAdapter.notifyDataSetChanged();
        if (vSpinner.getSelectedItemPosition() == 0) {
            onItemSelected(null, null, 0, 0);
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        getLoaderManager().destroyLoader(LOADER_ID);

        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
            mAlertDialog = null;
        }

        if (mPickerAdapter.getRequestManager() != null)
            mPickerAdapter.getRequestManager().onDestroy();
    }


    @Override
    public void itemClicked(GalleryItem item) {
        File file = new File(item.uri.getPath());
        if (file.exists()) {
            if (mOnGalleryItemSelected != null) mOnGalleryItemSelected.onGalleryItemSelected(item);
        } else if (getContext() != null)
            Toast.makeText(getContext(), "This file could not be opended", Toast.LENGTH_SHORT).show();
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
        showProgress(true);
        String filter = mBucketList.get(position).id;
        final ArrayList<GalleryItem> temp;

        if (filter.isEmpty()) {
            temp = mUnfilteredGalleryItems;
        } else {
            temp = new ArrayList<>();
            for (GalleryItem item : mUnfilteredGalleryItems)
                if (item.bucketId.equals(filter))
                    temp.add(item);
        }

        mHandler.removeCallbacksAndMessages(null);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mFiltedGalleryItems.clear();
                mFiltedGalleryItems.addAll(temp);
                mPickerAdapter.notifyDataSetChanged();
                showProgress(false);
            }
        });
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public interface OnGalleryItemSelected{
        void onGalleryItemSelected(GalleryItem item);
    }


}