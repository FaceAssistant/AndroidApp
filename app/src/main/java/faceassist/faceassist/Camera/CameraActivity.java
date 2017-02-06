package faceassist.faceassist.Camera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;


import faceassist.faceassist.Camera.CameraComponents.CameraFragment;
import faceassist.faceassist.Camera.CameraComponents.FacialRecFragment;
import faceassist.faceassist.Camera.CameraComponents.NeedPermissionFragment;
import faceassist.faceassist.R;
import faceassist.faceassist.Utils.PermissionUtils;

public class CameraActivity extends AppCompatActivity implements CameraFragment.OnImageTaken,
        NeedPermissionFragment.OnCheckPermissionClicked, FacialRecFragment.OnConfirmFace{

    public static final String TAG = CameraActivity.class.getSimpleName();
    private static final String CAMERA_FRAGS = "camera_fragments";

    //using these two guys because permission results are returned before onResume
    protected boolean mHasCameraPermission = false;
    protected boolean mReceivedRequestPermissionResults = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        clearBackStack();
        checkPermissions();

        if (savedInstanceState == null && mHasCameraPermission)
            launchFragment(CameraFragment.newInstance(), CameraFragment.TAG);

    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mReceivedRequestPermissionResults) { //only runs if we have updated permissions information
            clearBackStack(); //clears gallery or camera fragment
            if (mHasCameraPermission)
                launchFragment(CameraFragment.newInstance(), CameraFragment.TAG);
            else
                launchFragment(NeedPermissionFragment.newInstance(R.string.camera_perm_title,
                        R.string.camera_perm_text),
                        NeedPermissionFragment.TAG);

            mReceivedRequestPermissionResults = false;
        }
    }

    public void clearBackStack() { //pops all frag with name
        getSupportFragmentManager().popBackStack(CAMERA_FRAGS, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    protected final void launchFragment(Fragment fragment, String tag){
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment, tag)
                .commit();
    }

    protected final void addFragment(Fragment fragment, String tag){
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment, tag)
                .addToBackStack(CAMERA_FRAGS)
                .commit();
    }


    protected static final int REQUEST_PERMISSIONS = 21;

    //return true if have permissions
    public void checkPermissions() {
        //check for camera
        if (!PermissionUtils.hasCameraPermission(this)) {
            mHasCameraPermission = false;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSIONS);
        }else {
            mHasCameraPermission = true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS:

                mReceivedRequestPermissionResults = true;

                for (int result : grantResults) // if we didn't get approved for a permission, show permission needed frag
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        mHasCameraPermission = false;
                        return;
                    }

                mHasCameraPermission = true;
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }




    @Override
    public void onImageTake(Uri image) {
        addFragment(FacialRecFragment.newInstance(image), FacialRecFragment.TAG);
    }

    @Override
    public void onCheckPermissionClicked() {
        checkPermissions();
    }

    @Override
    public void onConfirmFace(Bitmap bitmap) {

    }
}
