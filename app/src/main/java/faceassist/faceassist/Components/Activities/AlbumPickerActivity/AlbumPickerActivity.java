package faceassist.faceassist.Components.Activities.AlbumPickerActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;

import faceassist.faceassist.Components.Fragments.AlbumPicker.Album;
import faceassist.faceassist.Components.Fragments.AlbumPicker.AlbumPickerFragment;
import faceassist.faceassist.Components.Fragments.AlbumPicker.AlbumPickerPresenter;
import faceassist.faceassist.Components.Fragments.NeedPermissions.NeedPermissionFragment;
import faceassist.faceassist.Components.Fragments.Picker.PickerConstants;
import faceassist.faceassist.R;

/**
 * Created by QiFeng on 5/12/17.
 */

public class AlbumPickerActivity extends AppCompatActivity implements NeedPermissionFragment.OnCheckPermissionClicked, AlbumPickerFragment.AlbumItemSelected {


    public static final int REQ_READ_EXT_STORAGE = 57;

    private boolean mPermissionUpdated = false;


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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_READ_EXT_STORAGE:
                mPermissionUpdated = true;
                break;
        }
    }


    private void addPermissionsFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, NeedPermissionFragment.newInstance(R.string.gallery_perm_title, R.string.gallery_perm_text))
                .commit();
    }

    private void addPickerFragment() {
        AlbumPickerFragment fragment = AlbumPickerFragment.newInstance();
        new AlbumPickerPresenter(fragment, getLoader(), getSupportLoaderManager());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    public Loader<Cursor> getLoader(){
        return new CursorLoader(
                this,
                PickerConstants.QUERY_URI,
                PickerConstants.PROJECTION,
                PickerConstants.SELECTION,
                null, // Selection args (none).
                PickerConstants.SORT_BY
        );
    }


    @Override
    public void onCheckPermissionClicked() {
        if (hasReadPermission()) addPickerFragment();
        else getReadPermission();
    }

    @Override
    public void onItemSelected(Album item) {
        Intent i = getIntent();
        i.setData(item.getPath());
        setResult(RESULT_OK, i);
        finish();
    }
}
