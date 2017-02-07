package faceassist.faceassist.API;

/**
 * Created by QiFeng on 2/6/17.
 */

import org.json.JSONObject;

import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by QiFeng on 11/21/15.
 */

public class API{

    public static String TAG = API.class.getSimpleName();


    // API ENDPOINT URL
    private static final String SCHEME = "http";
    private static final String HOST_LIVE = "34.199.64.11/api";
    private static final String VERSION_LIVE = "v1";



    //JSON TYPE
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    //PlainText Type
    public static final MediaType EMPTY = MediaType.parse("text/plain; charset=utf-8");

    // API get method
    public static Call get(String[] path,
                           Map<String, String> headers,
                           Map<String, Object> parameters,
                           Callback callback) {

        OkHttpClient client = new OkHttpClient();
        Headers requestHeaders = Headers.of(headers); //add headers
        HttpUrl.Builder url = new HttpUrl.Builder()   //build url
                .scheme(SCHEME)
                .host(HOST_LIVE)
                .addPathSegment(VERSION_LIVE);

        if (path != null) {
            for (String p : path) {
                if (p != null) {
                    url.addPathSegment(p);
                }
            }
        }

        if (parameters != null) {
            for (Map.Entry<String, Object> parameter : parameters.entrySet()) { //add parameters
                url.addQueryParameter(parameter.getKey(), parameter.getValue().toString());
            }
        }

        HttpUrl request = url.build();
        Call call = client.newCall(new Request.Builder().url(request).headers(requestHeaders).build());
        call.enqueue(callback);
        return call;
    }

    //API POST
    public static Call post(String[] path,
                            Map<String, String> headers,
                            Map<String, Object> parameters,
                            Callback callback) {

        JSONObject json = new JSONObject(parameters);
        RequestBody body = RequestBody.create(JSON, json.toString()); //get requestbody
        OkHttpClient client = new OkHttpClient();
        Headers requestHeaders = Headers.of(headers);   //add headers
        String url = getURL(path);
        Call call = client.newCall(new Request.Builder().url(url).headers(requestHeaders).method("POST", body).build());
        call.enqueue(callback);
        return call;
    }



    //API PUT
    public static Call put(String[] path,
                           Map<String, String> headers,
                           Map<String, Object> parameters,
                           Callback callback) {

        JSONObject json = new JSONObject(parameters);
        RequestBody body = RequestBody.create(JSON, json.toString()); //create json
        OkHttpClient client = new OkHttpClient();
        Headers requestHeaders = Headers.of(headers); //add headers
        String url = getURL(path);
        Call call = client.newCall(new Request.Builder().url(url).method("PUT", body).headers(requestHeaders).build());
        call.enqueue(callback);

        return call;
    }

    //API DELETE
    public static Call delete(String[] path,
                              Map<String, String> headers,
                              Map<String, Object> params,
                              Callback callback) {

        OkHttpClient client = new OkHttpClient();

        Headers requestHeaders = Headers.of(headers); //add headers

        JSONObject json = new JSONObject(params);
        RequestBody body = RequestBody.create(JSON, json.toString()); //create json

        String url = getURL(path);

        Call call = client.newCall(new Request.Builder().url(url).method("DELETE", body).headers(requestHeaders).build());
        call.enqueue(callback);

        return call;
    }

    public static String getURL(String[] path) {
        StringBuilder url = new StringBuilder();
        url.append(SCHEME);
        url.append("://");
        url.append(HOST_LIVE);
        url.append("/");
        url.append(VERSION_LIVE);
        url.append("/");

        for (String p : path){
            url.append(p);
            url.append("/");
        }

        return url.toString();
    }

}
