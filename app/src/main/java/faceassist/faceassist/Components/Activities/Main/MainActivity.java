package faceassist.faceassist.Components.Activities.Main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;

import faceassist.faceassist.API.API;
import faceassist.faceassist.API.GoogleAPIHelper;
import faceassist.faceassist.API.TokenRequestListener;
import faceassist.faceassist.Components.Activities.AddFace.AddFaceActivity;
import faceassist.faceassist.Components.Activities.AllFaces.AllFacesActivity;
import faceassist.faceassist.Components.Activities.Profile.BaseProfile;
import faceassist.faceassist.Components.Fragments.Camera.CameraFragment;
import faceassist.faceassist.Components.Fragments.Camera.CameraPresenter;
import faceassist.faceassist.Components.Fragments.FacialRec.FacialRecFragment;
import faceassist.faceassist.Components.Fragments.FacialRec.FacialRecPresenter;
import faceassist.faceassist.Components.Fragments.NeedPermissions.NeedPermissionFragment;
import faceassist.faceassist.Components.Activities.Profile.LovedOneProfile;
import faceassist.faceassist.Components.Activities.Profile.ProfileActivity;
import faceassist.faceassist.Login.LoginActivity;
import faceassist.faceassist.R;
import faceassist.faceassist.UserInfo;
import faceassist.faceassist.UserInfoConstants;
import faceassist.faceassist.Utils.FileUtils;
import faceassist.faceassist.Utils.OnFinished;
import faceassist.faceassist.Utils.OnNavigationIconClicked;
import faceassist.faceassist.Utils.PermissionUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements CameraFragment.OnImageTaken,
        NeedPermissionFragment.OnCheckPermissionClicked, FacialRecFragment.OnFaceResult,
        NavigationView.OnNavigationItemSelectedListener, OnNavigationIconClicked {

    public static final String TAG = MainActivity.class.getSimpleName();
    private static final String CAMERA_FRAGS = "camera_fragments";

    //using these two guys because permission results are returned before onResume
    protected boolean mHasCameraPermission = false;
    protected boolean mReceivedRequestPermissionResults = false;

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    private Call mFacialSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facial_rec);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) mDrawerLayout.findViewById(R.id.drawer);
        mNavigationView.setNavigationItemSelectedListener(this);

        setUpNavDrawerHeader();

        //clearBackStack();
        checkPermissions();

        if (savedInstanceState == null && mHasCameraPermission) {
            CameraFragment fragment = CameraFragment.newInstance(R.drawable.ic_action_menu);
            fragment.setOnNavigationIconClicked(this);
            new CameraPresenter(fragment, fragment, fragment);
            launchFragment(fragment, CameraFragment.TAG);
        }

    }

    private void setUpNavDrawerHeader() {
        TextView textView = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.username);
        textView.setText(UserInfo.getInstance().getFirstName() + " " + UserInfo.getInstance().getLastName());
        textView = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.subtitle);
        textView.setText(UserInfo.getInstance().getEmail());
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mReceivedRequestPermissionResults) { //only runs if we have updated permissions information
            clearBackStack(); //clears gallery or camera fragment
            if (mHasCameraPermission) {
                CameraFragment fragment = CameraFragment.newInstance(R.drawable.ic_action_menu);
                fragment.setOnNavigationIconClicked(this);
                new CameraPresenter(fragment, fragment, fragment);
                launchFragment(fragment, CameraFragment.TAG);
            } else
                launchFragment(NeedPermissionFragment.newInstance(R.string.camera_perm_title, R.string.camera_perm_text),
                        NeedPermissionFragment.TAG);

            mReceivedRequestPermissionResults = false;
        }
    }

    public void clearBackStack() { //pops all frag with name
        getSupportFragmentManager().popBackStack(CAMERA_FRAGS, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    protected final void launchFragment(Fragment fragment, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment, tag)
                .commit();
    }

    protected final void addFragment(Fragment fragment, String tag) {
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
        } else {
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


    //when cameraFragment takes picture
    //input is the uri of image
    @Override
    public void onImageTaken(Uri image) {
        FacialRecFragment facialRecFragment = FacialRecFragment.newInstance(image);
        new FacialRecPresenter(facialRecFragment);
        addFragment(facialRecFragment, FacialRecFragment.TAG);
    }

    //check permissions
    @Override
    public void onCheckPermissionClicked() {
        //check for camera
        checkPermissions();
        if (mHasCameraPermission) {
            CameraFragment fragment = CameraFragment.newInstance(R.drawable.ic_action_menu);
            fragment.setOnNavigationIconClicked(this);
            new CameraPresenter(fragment, fragment, fragment);
            launchFragment(fragment, CameraFragment.TAG);
        }

    }

    //when a face is selected
    @Override
    public void onFaceResult(final Uri uri, final OnFinished onFinished) {
        final WeakReference<OnFinished> onFinishedWeakReference = new WeakReference<>(onFinished);

        GoogleAPIHelper.getInstance().makeApiRequest(new TokenRequestListener() {
            @Override
            public void onTokenReceived(GoogleSignInAccount account) {
                Log.d(TAG, "onTokenReceived: " + account.getIdToken());

                sendFaceToServer(account.getIdToken(), uri, onFinishedWeakReference);
            }

            @Override
            public void onFailedToGetToken() {
                Toast.makeText(MainActivity.this, R.string.failed_connection, Toast.LENGTH_SHORT).show();

                if (onFinishedWeakReference.get() != null)
                    onFinishedWeakReference.get().onFinished();
            }
        });

    }


    private void sendFaceToServer(String token, Uri uri, final WeakReference<OnFinished> onFinishedWeakReference) {
        try {
            HashMap<String, Object> params = new HashMap<>();
            params.put("image", FileUtils.encodeFileBase64(new File(uri.getPath())));


            if (mFacialSearch != null) mFacialSearch.cancel();

            mFacialSearch = API.post(new String[]{"face", "infer"},
                    API.getMainHeader(token),
                    params,
                    new Callback() {

                        @Override
                        public void onFailure(Call call, IOException e) {
                            if (call.isCanceled()) return;
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, R.string.failed_connection, Toast.LENGTH_SHORT).show();
                                    if (onFinishedWeakReference.get() != null)
                                        onFinishedWeakReference.get().onFinished();
                                }
                            });
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (response.isSuccessful()) {
                                try {
                                    String header = response.header("Person-Type", null);
                                    if (header == null) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(MainActivity.this, R.string.error_communicating_server, Toast.LENGTH_SHORT).show();
                                                if (onFinishedWeakReference.get() != null)
                                                    onFinishedWeakReference.get().onFinished();
                                            }
                                        });
                                        return;
                                    }

                                    JSONObject obj = new JSONObject(response.body().string());

                                    Log.d(TAG, "onResponse: " + obj.toString(4));

                                    final BaseProfile profile;
                                    if (header.equals(BaseProfile.TYPE_CELEB)) {
                                        profile = new BaseProfile(obj);
                                    } else {
                                        profile = new LovedOneProfile(obj);
                                    }

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (onFinishedWeakReference.get() != null)
                                                onFinishedWeakReference.get().onFinished();

                                            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                                            intent.putExtra(ProfileActivity.ARG_PROFILE, profile);
                                            startActivity(intent);
                                        }
                                    });

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(MainActivity.this, R.string.error_communicating_server, Toast.LENGTH_SHORT).show();
                                            if (onFinishedWeakReference.get() != null)
                                                onFinishedWeakReference.get().onFinished();
                                        }
                                    });

                                }
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, R.string.error_communicating_server, Toast.LENGTH_SHORT).show();
                                        if (onFinishedWeakReference.get() != null)
                                            onFinishedWeakReference.get().onFinished();
                                    }
                                });

                                Log.d(TAG, "onResponse: " + response.code() + " " + response.body().string());
                            }
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onSearchStopped() {
        if (mFacialSearch != null) mFacialSearch.cancel();
        mFacialSearch = null;
    }

    private Handler mOnDrawerClosedHandler = new Handler();

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        //note: use a runnable 250ish
        final Class c;
        switch (item.getItemId()) {
            case R.id.menu_create:
                startNextActivity(AddFaceActivity.class);
                break;
            case R.id.menu_current:
                startNextActivity(AllFacesActivity.class);
                break;
//            case R.id.menu_settings:
//                //// TODO: 2/13/17 settings
//                break;
            case R.id.log_out:
                logout();
            default:
                break;
        }


        return true;
    }


    private void logout() {
        UserInfo.clean(getSharedPreferences(UserInfoConstants.DEF_PREF, MODE_PRIVATE));
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }


    private void startNextActivity(final Class c) {
        mDrawerLayout.closeDrawers();

        if (c != null) {
            mOnDrawerClosedHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent i = new Intent(MainActivity.this, c);
                    startActivity(i);
                }
            }, 350);
        }
    }


    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(mNavigationView)) {
            mDrawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onNavIconClicked(View v) {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            super.onBackPressed();
        } else {
            mDrawerLayout.openDrawer(mNavigationView);
        }
    }
}
