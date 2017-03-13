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

import faceassist.faceassist.R;

/**
 * Created by QiFeng on 3/12/17.
 */

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener{

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


    private void initGoogleSignIn(){
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
        signIn();
    }

    private void signIn(){
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(intent, SIGN_IN_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_IN_REQUEST){
            Log.i(TAG, "onActivityResult:m ");
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleResult(result);
        }
    }

    private void handleResult(GoogleSignInResult res){
        if (res.isSuccess()){
            GoogleSignInAccount account = res.getSignInAccount();

            try{
                Log.i(TAG, "handleResult: "+account.getIdToken());
                Log.i(TAG, "handleResult: "+account.getEmail());
            }catch (NullPointerException e){
                Toast.makeText(this, "Error retrieving token", Toast.LENGTH_SHORT).show();
            }


        }else {
            Log.e(TAG, "handleResult: bad result "+res.getStatus());
        }
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "onConnectionFailed: "+connectionResult.getErrorMessage());
        Toast.makeText(this, ""+connectionResult.getErrorCode(), Toast.LENGTH_SHORT).show();
    }
}
