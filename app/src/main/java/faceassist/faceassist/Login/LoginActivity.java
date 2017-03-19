package faceassist.faceassist.Login;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.security.ProviderInstaller;

import java.io.IOException;
import java.util.HashMap;

import faceassist.faceassist.API.API;
import faceassist.faceassist.R;
import faceassist.faceassist.UserInfo;
import faceassist.faceassist.UserInfoConstants;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by QiFeng on 3/12/17.
 */

public class LoginActivity extends AppCompatActivity implements View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener, ProviderInstaller.ProviderInstallListener {

    public static final String TAG = LoginActivity.class.getSimpleName();

    private static final int SIGN_IN_REQUEST = 4121;
    private GoogleApiClient mGoogleApiClient;
    private boolean mRetryProviderInstall;
    private boolean mFinishedInstall = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        findViewById(R.id.signin_button).setOnClickListener(this);
        updateAndroidSecurityProvider();
    }


    private void initGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    public void onClick(View view) {
        if (mFinishedInstall) {
            signIn();
        } else {
            Toast.makeText(this, R.string.security_updating, Toast.LENGTH_LONG).show();
        }
    }

    private void signIn() {
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(intent, SIGN_IN_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_IN_REQUEST) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleResult(result);
        } else if (requestCode == ERROR_DIALOG_REQUEST_CODE) {
            mRetryProviderInstall = true;
        }
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

    private void handleResult(final GoogleSignInResult res) {
        if (res.isSuccess()) {
            final GoogleSignInAccount account = res.getSignInAccount();

            if (account != null) {
                //Log.d(TAG, "handleResult: " + account.getIdToken());
                //Log.i(TAG, "handleResult: "+account.getEmail());
                //Toast.makeText(this, account.getIdToken(), Toast.LENGTH_LONG).show();

                API.post(new String[]{"users", "login"},
                        API.getMainHeader(account.getIdToken()),
                        new HashMap<String, Object>(),
                        new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Log.i(TAG, "onFailure: ");
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                if (response.isSuccessful()) {
                                    UserInfo.updateUserInfo(
                                            getSharedPreferences(UserInfoConstants.DEF_PREF, MODE_PRIVATE),
                                            account.getIdToken(),
                                            account.getEmail(),
                                            account.getGivenName(),
                                            account.getFamilyName()
                                    );


                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            //todo run
                                        }
                                    });
                                } else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(LoginActivity.this, R.string.server_comm_error, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        }
                );
            } else {
                Toast.makeText(this, R.string.error_getting_google_account, Toast.LENGTH_SHORT).show();
            }


        } else {
            Toast.makeText(this, R.string.error_getting_google_account, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "handleResult: bad result " + res.getStatus());
        }
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "onConnectionFailed: " + connectionResult.getErrorMessage());
        Toast.makeText(this, R.string.connection_failed, Toast.LENGTH_SHORT).show();
    }


    private void updateAndroidSecurityProvider() {
        ProviderInstaller.installIfNeededAsync(this, this);
    }

    @Override
    public void onProviderInstalled() {
        initGoogleSignIn();
        mFinishedInstall = true;
    }

    private static final int ERROR_DIALOG_REQUEST_CODE = 12;

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
}
