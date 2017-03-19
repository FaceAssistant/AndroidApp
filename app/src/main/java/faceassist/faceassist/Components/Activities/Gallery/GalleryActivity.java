package faceassist.faceassist.Components.Activities.Gallery;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;

import java.io.IOException;
import java.lang.ref.WeakReference;

import faceassist.faceassist.Components.Fragments.FacialRec.FacialRecFragment;
import faceassist.faceassist.Components.Fragments.FacialRec.FacialRecPresenter;
import faceassist.faceassist.Components.Fragments.NeedPermissions.NeedPermissionFragment;
import faceassist.faceassist.Components.Fragments.Picker.Models.GalleryItem;
import faceassist.faceassist.Components.Fragments.Picker.PickerFragment;
import faceassist.faceassist.Components.Fragments.Picker.PickerPresenter;
import faceassist.faceassist.R;
import faceassist.faceassist.Utils.ImageUtils;
import faceassist.faceassist.Utils.OnFinished;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;

import static rx.android.schedulers.AndroidSchedulers.mainThread;
import static rx.schedulers.Schedulers.io;

public class GalleryActivity extends AppCompatActivity implements NeedPermissionFragment.OnCheckPermissionClicked,
        PickerFragment.OnGalleryItemSelected, FacialRecFragment.OnFaceResult {

    public static final String TAG = GalleryActivity.class.getSimpleName();

    public static final String URI_KEY = "uri_key";

    public static final int REQ_READ_EXT_STORAGE = 52;

    private Subscription mScaleSubscription;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        if (hasReadPermission()) {
            addPickerFragment();
        } else {
            addPermissionsFragment();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPermissionUpdated){
            mPermissionUpdated = false;
            if (hasReadPermission()) {
                addPickerFragment();
            } else {
                addPermissionsFragment();
            }
        }
    }

    private void addPickerFragment() {
        PickerFragment fragment = PickerFragment.newInstance();
        new PickerPresenter(fragment, getLoader(), getSupportLoaderManager());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }


    public Loader<Cursor> getLoader(){
        return new CursorLoader(
                this,
                PickerFragment.QUERY_URI,
                PickerFragment.PROJECTION,
                PickerFragment.SELECTION,
                null, // Selection args (none).
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC" // Sort order.
        );
    }


    private void addPermissionsFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, NeedPermissionFragment.newInstance(R.string.gallery_perm_title, R.string.gallery_perm_text))
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0)
            setResult(RESULT_CANCELED);

        super.onBackPressed();
    }

    private boolean mPermissionUpdated = false;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_READ_EXT_STORAGE:
                mPermissionUpdated = true;
                break;
        }
    }

    //Permissions
    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasReadPermission() {
        return hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    public void getReadPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQ_READ_EXT_STORAGE);
    }


    @Override
    public void onCheckPermissionClicked() {
        if (hasReadPermission()) addPickerFragment();
        else getReadPermission();
    }

    @Override
    public void onGalleryItemSelected(GalleryItem item) {
        FacialRecFragment fragment = FacialRecFragment.newInstance(item.uri);
        new FacialRecPresenter(fragment);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onFaceResult(Uri uri, OnFinished onFinished) {

        final WeakReference<OnFinished> mOnFinishedWeakReference = new WeakReference<>(onFinished);

        mScaleSubscription = Observable.just(scaleBitmap(uri))
                .subscribeOn(io())
                .observeOn(mainThread())
                .subscribe(new Action1<Uri>() {
                    @Override
                    public void call(Uri uri) {
                        if (mOnFinishedWeakReference.get() != null){
                            mOnFinishedWeakReference.get().onFinished();
                        }

                        if (uri != null) {
                            Intent i = getIntent();
                            i.putExtra(URI_KEY, uri);
                            setResult(RESULT_OK, i);
                            finish();
                        }
                    }
                });
    }



    private Uri scaleBitmap(Uri uri){

        try {
            Bitmap bitmap = ImageUtils.decodeUri(this, uri, 100);

            Bitmap scaled;

            if (bitmap.getWidth() > bitmap.getHeight()) {
                scaled = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * 100f / bitmap.getHeight()), 100, true);
            } else {
                scaled = Bitmap.createScaledBitmap(bitmap, 100, (int) (bitmap.getHeight() * 100f / bitmap.getWidth()), true);
            }

            if (scaled != bitmap) bitmap.recycle();

            return Uri.fromFile(ImageUtils.savePictureToCache(this, scaled).getAbsoluteFile());
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mScaleSubscription != null) mScaleSubscription.unsubscribe();
    }

    @Override
    public void onSearchStopped() {

    }
}
