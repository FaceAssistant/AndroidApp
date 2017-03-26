package faceassist.faceassist.Upload;


import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.util.Log;


import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

import faceassist.faceassist.API.API;
import faceassist.faceassist.API.GoogleAPIHelper;
import faceassist.faceassist.API.TokenRequestListener;
import faceassist.faceassist.Components.Activities.AddFace.Models.Entry;
import faceassist.faceassist.Components.Activities.Camera.FacialResultActivity;
import faceassist.faceassist.Login.LoginActivity;
import faceassist.faceassist.R;
import faceassist.faceassist.UserInfo;
import faceassist.faceassist.Utils.FileUtils;
import okhttp3.Response;

/**
 * Created by QiFeng on 6/25/16.
 */
public class UploadIntentService extends IntentService {

    public static final int ID = 0;
    private static int notificationId = 1;

    public static final String TAG = UploadIntentService.class.getSimpleName();
    public static final String PENDING_UPLOAD_KEY = "faceassist_pending_entry_key";

    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;

    private int mPendingFiles;

    public UploadIntentService() {
        super("UploadIntentService");
        setIntentRedelivery(true);
    }

    //// TODO: 6/30/16 move processing of video to this service?

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(android.R.drawable.stat_sys_upload);
        mPendingFiles = 0;
        notificationId = new Random().nextInt();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ++mPendingFiles;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Entry p = intent.getParcelableExtra(PENDING_UPLOAD_KEY);
        if (p != null) {
            checkTokenAndUpload(p);
        }
    }

    private void checkTokenAndUpload(final Entry entry) {
        GoogleAPIHelper.getInstance().makeApiRequest(new TokenRequestListener() {
            @Override
            public void onTokenReceived(GoogleSignInAccount account) {
                Log.d(TAG, "onTokenReceived: "+account.getIdToken());
                sendNextFile(entry, account.getIdToken());
            }

            @Override
            public void onFailedToGetToken() {
                failedToPost(entry);
            }
        });

    }

    private void sendNextFile(Entry entry, String token) {
        try {
            Uri imageUri = entry.getImageList()[0];
            File file = new File(imageUri.getPath());
            if (!file.exists()){
                failedToPost(entry);
                Log.e(TAG, "sendNextFile: no image");
                return;
            }

            Bitmap image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

            mBuilder.setContentTitle("Uploading faces")
                    .setContentText(mPendingFiles + (mPendingFiles > 1 ? "faces remaining" : "face remaining"))
                    .setLargeIcon(image)
                    .setProgress(0, 0, true)
                    .setContentIntent(null)
                    .setAutoCancel(false)
                    .setContentIntent(null);

            mNotificationManager.notify(ID, mBuilder.build());

            HashMap<String, Object> params = getRequestBody(entry);
            if(params == null){
                failedToPost(entry);
                Log.e(TAG, "sendNextFile: creating json body failed.");
                return;
            }

            Response r = API.postWithLongTimeout(new String[]{"face", "train"}, API.getMainHeader(token), params);

            if (r.isSuccessful()) {
                mBuilder.setContentTitle("Upload complete")
                        .setContentText("")
                        .setSmallIcon(R.drawable.ic_stat_untitled_4_01)
                        .setProgress(0, 0, false)
                        .setAutoCancel(true)
                        .setContentIntent(getIntent())
                        .setContentText(getPostText(entry.getName()));

                mNotificationManager.notify(ID, mBuilder.build());
                stopSelf();
            } else {
                failedToPost(entry);
                stopSelf();

            }

            image.recycle();
        } catch (IOException e) {
            failedToPost(entry);
            e.printStackTrace();

        }

        --mPendingFiles;
    }


    private HashMap<String, Object> getRequestBody(Entry entry) {
        try {
            JSONObject profile = new JSONObject();

            profile.put("name", entry.getName());
            profile.put("birthday", entry.getBirthday());
            profile.put("relationship", entry.getRelationship());
            profile.put("note", entry.getNotes());
            profile.put("last_viewed", entry.getLastViewed());

            HashMap<String, Object> params = new HashMap<>();
            params.put("profile", profile);

            JSONArray array = new JSONArray();

            for (Uri uri : entry.getImageList()) {
                array.put(FileUtils.encodeFileBase64(new File(uri.getPath())));
            }

            params.put("images", array);

            return params;
        } catch (JSONException|IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private PendingIntent getIntent() {
        Intent intent;
        if (!UserInfo.getInstance().isLoggedIn()) {
            intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        } else {
            intent = new Intent(this, FacialResultActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }

        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_UPDATE_CURRENT);
    }


    private String getPostText(String name) {
        return String.format(Locale.ENGLISH, "We can now recognize %s!", name);
    }


    private void failedToPost(final Entry p) {
        Intent i = new Intent(this, UploadIntentService.class);
        i.putExtra(PENDING_UPLOAD_KEY, p);
        mBuilder.setProgress(0, 0, false)
                .setSmallIcon(R.drawable.ic_stat_untitled_4_01)
                .setContentTitle("File failed to upload")
                .setContentText("Tap to retry")
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getService(this, notificationId, i, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_UPDATE_CURRENT));

        mNotificationManager.cancel(ID);
        mNotificationManager.notify(notificationId++, mBuilder.build());
    }
}