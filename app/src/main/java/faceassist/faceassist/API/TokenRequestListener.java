package faceassist.faceassist.API;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

/**
 * Created by QiFeng on 3/21/17.
 */

public interface TokenRequestListener {

    void onTokenReceived(GoogleSignInAccount account);
    void onFailedToGetToken();

}
