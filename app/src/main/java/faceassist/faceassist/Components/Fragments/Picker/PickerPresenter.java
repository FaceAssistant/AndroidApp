package faceassist.faceassist.Components.Fragments.Picker;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import faceassist.faceassist.Components.Fragments.Picker.Models.BucketItem;
import faceassist.faceassist.Components.Fragments.Picker.Models.GalleryItem;

/**
 * Created by QiFeng on 2/20/17.
 */

public class PickerPresenter implements PickerContract.Presenter, LoaderManager.LoaderCallbacks<Cursor> {
    private static final int LOADER_ID = 4;

    private LoaderManager mLoaderManager;
    private Loader<Cursor> mCursorLoader;
    private PickerContract.View mPickerView;
    private List<GalleryItem> mUnfilteredGalleryItems;

    PickerPresenter(PickerContract.View view, Loader<Cursor> cursorLoader, LoaderManager loaderManager) {
        mPickerView = view;
        mCursorLoader = cursorLoader;
        mLoaderManager = loaderManager;
    }

    @Override
    public void setUnfilteredGalleryItems(List<GalleryItem> items){
        mUnfilteredGalleryItems = items;
    }

    @Override
    public void start() {
        mLoaderManager.initLoader(LOADER_ID, null, this);
    }

    @Override
    public void stop() {
        mLoaderManager.destroyLoader(LOADER_ID);
    }

    @Override
    public void filter(String filter) {
        mPickerView.showProgress(true);

        final List<GalleryItem> temp;

        if (filter.isEmpty()) {
            temp = mUnfilteredGalleryItems;
        } else {
            temp = new ArrayList<>();
            for (GalleryItem item : mUnfilteredGalleryItems)
                if (item.bucketId.equals(filter))
                    temp.add(item);
        }

        mPickerView.showUpdatedGallery(temp);
    }

    @Override
    public void clickGalleryItem(GalleryItem item) {
        File file = new File(item.uri.getPath());
        if (file.exists()) mPickerView.runClickedItem(item);
        else mPickerView.showErrorToast();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        mPickerView.showProgress(true);
        return mCursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        int pathIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
        int idIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns._ID);
        int mediaIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE);
        int bucketName = cursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME);
        int bucketId = cursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_ID);


        final HashSet<BucketItem> bucketItems = new HashSet<>();

        //mUnfilteredGalleryItems.clear();
        ArrayList<GalleryItem> galleryItems = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                String buckId = cursor.getString(bucketId);
                File f = new File(cursor.getString(pathIndex));
                //if (!f.getPath().endsWith(".gif")) {
                    galleryItems.add(
                            new GalleryItem(
                                    cursor.getString(idIndex),
                                    Uri.fromFile(f),
                                    cursor.getInt(mediaIndex),
                                    buckId
                            )
                    );

                    bucketItems.add(new BucketItem(buckId, cursor.getString(bucketName)));
                //}

            } while (cursor.moveToNext());
        }

        mUnfilteredGalleryItems.clear();
        mUnfilteredGalleryItems.addAll(galleryItems);
        ArrayList<BucketItem> bucketItemList = new ArrayList<>();

        bucketItemList.add(new BucketItem("", "Gallery")); //option for all images
        bucketItemList.addAll(bucketItems);

        mPickerView.updateBucketAndGallery(bucketItemList, mUnfilteredGalleryItems);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}
