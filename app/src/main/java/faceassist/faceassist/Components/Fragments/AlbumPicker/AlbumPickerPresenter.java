package faceassist.faceassist.Components.Fragments.AlbumPicker;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by QiFeng on 5/12/17.
 */

public class AlbumPickerPresenter implements AlbumPickerContract.Presenter, LoaderManager.LoaderCallbacks<Cursor> {
    private static final int LOADER_ID = 7;

    private AlbumPickerContract.View mPickerView;
    private Loader<Cursor> mCursorLoader;
    private LoaderManager mLoaderManager;


    public AlbumPickerPresenter(AlbumPickerContract.View view, Loader<Cursor> cursorLoader, LoaderManager loaderManager) {
        mPickerView = view;
        mCursorLoader = cursorLoader;
        mLoaderManager = loaderManager;
        view.setPresenter(this);
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
    public void clickItem(Album item) {
        File file = new File(item.getPath().getPath());
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
        if (cursor == null) return;

        int pathIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
        int bucketName = cursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME);

        final HashSet<String> tracker = new HashSet<>();
        final ArrayList<Album> albums = new ArrayList<>();


        if (cursor.moveToFirst()) {
            do {
                File item = new File(cursor.getString(pathIndex));
                //Log.i("TET", "onLoadFinished: "+item.getPath());
                if(!tracker.contains(item.getParent())){
                    albums.add(new Album(
                            Uri.fromFile(item.getParentFile()),
                            cursor.getString(bucketName),
                            Uri.fromFile(item)
                    ));
                    tracker.add(item.getParent());
                }
            } while (cursor.moveToNext());
        }

        mPickerView.updateAlbums(albums);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
