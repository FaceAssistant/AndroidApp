package faceassist.faceassist;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.security.ProviderInstaller;

import faceassist.faceassist.Components.Activities.Main.MainActivity;
import faceassist.faceassist.Login.LoginActivity;

/**
 * Created by QiFeng on 1/30/17.
 * <p>
 * <p>
 * When our app starts with this activity.
 * We will put logic in here to determine if an user is logged in, etc...
 */

public class LaunchActivity extends AppCompatActivity implements ProviderInstaller.ProviderInstallListener {

    private boolean mRetryProviderInstall = true;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (mRetryProviderInstall) {
            // We can now safely retry installation.
            updateAndroidSecurityProvider();
        }
        mRetryProviderInstall = false;

    }


    @Override
    public void onProviderInstallFailed(int i, Intent intent) {
        GoogleApiAvailability activity = GoogleApiAvailability.getInstance();
        if (activity.isUserResolvableError(i)) {
            // Recoverable error. Show a dialog prompting the user to
            // install/update/enable Google Play services.
            GoogleApiAvailability.getInstance().getErrorDialog(
                    this, i, ERROR_DIALOG_REQUEST_CODE, new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            showRational();
                        }
                    });
        } else {
            // Google Play services is not available.
            showNotAvailable();
        }
    }


    private void showNotAvailable() {
        Toast.makeText(this, R.string.security_update_unavailable, Toast.LENGTH_SHORT).show();
    }

    private void showRational() {
        new AlertDialog.Builder(this).setTitle(R.string.error_updating_secutiry_title)
                .setMessage(R.string.error_updating_security)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        updateAndroidSecurityProvider();
                    }
                }).show();
    }

    @Override
    public void onProviderInstalled() {
        UserInfo info = UserInfo.getInstance();
        Class nextActivity = info.isLoggedIn() ? MainActivity.class : LoginActivity.class;

        Intent i = new Intent(LaunchActivity.this, nextActivity);
        startActivity(i);
        finish();
    }

    private static final int ERROR_DIALOG_REQUEST_CODE = 12;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ERROR_DIALOG_REQUEST_CODE) {
            mRetryProviderInstall = true;
        }
    }


    private void updateAndroidSecurityProvider() {
        ProviderInstaller.installIfNeededAsync(this, this);
    }
}
