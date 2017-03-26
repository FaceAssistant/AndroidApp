package faceassist.faceassist.Login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;
import java.util.HashMap;

import faceassist.faceassist.API.API;
import faceassist.faceassist.Components.Activities.Main.MainActivity;
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
        GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = LoginActivity.class.getSimpleName();

    private static final int SIGN_IN_REQUEST = 4121;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        findViewById(R.id.signin_button).setOnClickListener(this);
        initGoogleSignIn();
    }


    private void initGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    public void onClick(View view) {
        signIn();
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
        }
    }

    private void handleResult(final GoogleSignInResult res) {
        if (res.isSuccess()) {
            final GoogleSignInAccount account = res.getSignInAccount();

            if (account != null) {
//                Log.d(TAG, "handleResult: " + account.getIdToken());
//                Log.d(TAG, "handleResult: "+account.getEmail());
//                Log.d(TAG, "handleResult: "+account.getFamilyName());
//                Log.d(TAG, "handleResult: "+account.getGivenName());


                updateAccountInfo(account);
                enterApplication();

            } else {
                Toast.makeText(this, R.string.error_getting_google_account, Toast.LENGTH_SHORT).show();
            }


        } else {
            Toast.makeText(this, R.string.error_getting_google_account, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "handleResult: bad result " + res.getStatus());
        }
    }


    private void updateAccountInfo(GoogleSignInAccount account) {
        UserInfo.updateUserInfo(
                getSharedPreferences(UserInfoConstants.DEF_PREF, MODE_PRIVATE),
                true,
                account.getEmail(),
                account.getGivenName(),
                account.getFamilyName()
        );
    }

    private void enterApplication(){
        Intent cam = new Intent(LoginActivity.this, MainActivity.class);
        cam.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(cam);
        finish();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "onConnectionFailed: " + connectionResult.getErrorMessage());
        Toast.makeText(this, R.string.connection_failed, Toast.LENGTH_SHORT).show();
    }



}
