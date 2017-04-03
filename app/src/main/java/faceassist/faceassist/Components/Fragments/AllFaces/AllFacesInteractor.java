package faceassist.faceassist.Components.Fragments.AllFaces;

import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import faceassist.faceassist.API.API;
import faceassist.faceassist.API.GoogleAPIHelper;
import faceassist.faceassist.API.TokenRequestListener;
import faceassist.faceassist.Components.Activities.Profile.LovedOneProfile;
import faceassist.faceassist.Utils.OnInteractorResult;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by QiFeng on 4/2/17.
 */

public class AllFacesInteractor {

    public static final String TAG = AllFacesInteractor.class.getSimpleName();

    public AllFacesInteractor(){
    }


    public void getAllFaces(final OnInteractorResult<LovedOneProfile> callback){
        GoogleAPIHelper.getInstance().makeApiRequest(new TokenRequestListener() {
            @Override
            public void onTokenReceived(GoogleSignInAccount account) {
                makeGetAllRequest(account.getIdToken(), callback);
            }

            @Override
            public void onFailedToGetToken() {
                callback.onFailed();
            }
        });
    }

    private void makeGetAllRequest(String token, final OnInteractorResult<LovedOneProfile> callback){
        HashMap<String, Object> params = new HashMap<>();
        params.put("type", "profile");

        API.get(new String[]{"users", "loved-one"},
                API.getMainHeader(token),
                params,
                new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.i(TAG, "onFailure: ");
                        e.printStackTrace();
                        callback.onFailed();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()){
                            try {
                                callback.onGetAllResultResponse(parseProfiles(response.body().string()));
                            }catch (JSONException e){
                                e.printStackTrace();
                                callback.onError();
                            }
                        }else {
                            callback.onError();
                            Log.i(TAG, "onResponse: "+response.code()+" : "+response.body().toString());
                        }

                        response.close();
                    }
                });
    }


    private List<LovedOneProfile> parseProfiles(String body) throws JSONException{
        Log.i(TAG, "parseProfiles: "+body);
        JSONArray json = new JSONObject(body).getJSONArray("profiles");
        List<LovedOneProfile> profiles = new ArrayList<>();

        for (int i = 0; i < json.length(); i++){
            profiles.add(new LovedOneProfile(json.getJSONObject(i)));
        }

        return profiles;
    }



}
