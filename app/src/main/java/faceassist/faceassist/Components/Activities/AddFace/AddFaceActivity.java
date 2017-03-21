package faceassist.faceassist.Components.Activities.AddFace;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import faceassist.faceassist.API.API;
import faceassist.faceassist.Components.Activities.AddFace.Models.Entry;
import faceassist.faceassist.Components.Activities.AddFace.RecyclerView.AddFaceAdapter;
import faceassist.faceassist.Components.Activities.AddFace.RecyclerView.ImageEntryViewHolder;
import faceassist.faceassist.Components.Activities.Camera.PictureUriActivity;
import faceassist.faceassist.Components.Activities.Gallery.GalleryActivity;
import faceassist.faceassist.R;
import faceassist.faceassist.UserInfo;
import faceassist.faceassist.Utils.FileUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;

import static rx.android.schedulers.AndroidSchedulers.mainThread;
import static rx.schedulers.Schedulers.io;

/**
 * Created by QiFeng on 2/13/17.
 */


//TODO: clean up

public class AddFaceActivity extends AppCompatActivity implements View.OnClickListener, ImageEntryViewHolder.OnImageClick {

    private static final int IMAGES_COUNT = 12;
    private static final String TAG = AddFaceActivity.class.getSimpleName();

    private AddFaceAdapter mAddFaceAdapter;
    private RecyclerView vRecyclerView;
    private View vProgress;
    private Entry mEntry = new Entry(IMAGES_COUNT);
    private Subscription mUploadSubscription;

    private EditText vNameEditText;
    private EditText vRelationshipEditText;
    private EditText vNotesEditText;

    private ViewSwitcher vViewSwitcher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_face);

        vViewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);

        vRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        vProgress = findViewById(R.id.progress);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4);

        vRecyclerView.setLayoutManager(gridLayoutManager);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        if (mAddFaceAdapter == null)
            mAddFaceAdapter = new AddFaceAdapter(mEntry.getImageList());

        mAddFaceAdapter.setOnImageClick(this);
        vRecyclerView.setAdapter(mAddFaceAdapter);

        vNameEditText = (EditText) findViewById(R.id.name);
        vRelationshipEditText = (EditText) findViewById(R.id.relationship);
        vNotesEditText = (EditText) findViewById(R.id.note);


        findViewById(R.id.next_button).setOnClickListener(this);
        findViewById(R.id.submit_button).setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.next_button) {
            hideKeyboard();
            verifyInputText();
        } else {
            verifyImages();
        }
    }


    private void hideKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void verifyInputText() {
        vNameEditText.setError(null);
        vRelationshipEditText.setError(null);
        boolean error = false;
        if (vNameEditText.getText().toString().isEmpty()) {
            error = true;
            vNameEditText.setError(getString(R.string.required_field));
        }

        if (vRelationshipEditText.getText().toString().isEmpty()) {
            error = true;
            vRelationshipEditText.setError(getString(R.string.required_field));
        }

        if (!error) {
            mEntry.setName(vNameEditText.getText().toString());
            mEntry.setNotes(vNotesEditText.getText().toString());
            mEntry.setRelationship(vRelationshipEditText.getText().toString());
            mEntry.setLastViewed("2017-1-1");
            mEntry.setBirthday("2017-1-1");
            vViewSwitcher.showNext();
        }
    }

    private void verifyImages() {
        Log.i(TAG, "verifyImages: "+UserInfo.getInstance().getToken());
        for (Uri uri : mEntry.getImageList()){
            if (uri == null){
                Toast.makeText(this, R.string.not_enough_images, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        upload();
    }

    private void upload() {
        showProgress(true);
        if (mUploadSubscription != null) mUploadSubscription.unsubscribe();
        mUploadSubscription = Observable.just(getRequestBody())
                .subscribeOn(io())
                .observeOn(mainThread())
                .subscribe(
                        new Action1<HashMap<String, Object>>() {
                            @Override
                            public void call(HashMap<String, Object> stringObjectHashMap) {
                                API.post(new String[]{"face", "train"},
                                        API.getMainHeader(UserInfo.getInstance().getToken()),
                                        stringObjectHashMap,
                                        new Callback() {
                                            @Override
                                            public void onFailure(Call call, IOException e) {
                                                Log.i(TAG, "onFailure: ");
                                            }

                                            @Override
                                            public void onResponse(Call call, Response response) throws IOException {
                                                if (response.isSuccessful()) {
                                                    Log.i(TAG, "onResponse: " + response.body().string());
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            finish();
                                                        }
                                                    });
                                                } else {
                                                    Log.e(TAG, "onResponse: " + response.code() + " " + response.body().string());
                                                }
                                            }
                                        });
                            }
                        });
    }

    private HashMap<String, Object> getRequestBody() {
        try {
            JSONObject profile = new JSONObject();

            profile.put("name", mEntry.getName());
            profile.put("birthday", mEntry.getBirthday());
            profile.put("relationship", mEntry.getRelationship());
            profile.put("note", mEntry.getNotes());
            profile.put("last_viewed", mEntry.getLastViewed());

            HashMap<String, Object> params = new HashMap<>();
            params.put("profile", profile);

            JSONArray array = new JSONArray();

            for (Uri uri : mEntry.getImageList()){
                try {
                    array.put(FileUtils.encodeFileBase64(new File(uri.getPath())));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            params.put("images", array);

            return params;
        }catch (JSONException e){
            e.printStackTrace();
            return null;
        }
    }

    private void showProgress(boolean show) {
        if (show) {
            vViewSwitcher.setVisibility(View.GONE);
            vProgress.setVisibility(View.VISIBLE);
        } else {
            vProgress.setVisibility(View.GONE);
            vViewSwitcher.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onImageClick(final int requestCode) {

        new AlertDialog.Builder(this)
                .setItems(new String[]{"Camera", "Gallery"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Class c = PictureUriActivity.class;

                        if (i == 1) {
                            c = GalleryActivity.class;
                        }

                        Intent intent = new Intent(AddFaceActivity.this, c);
                        startActivityForResult(intent, requestCode);
                    }
                }).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        int pos = requestCode - ImageEntryViewHolder.REQUEST_CODE_BASE;

        //not position is max 12 atm
        if (pos >= 0 && pos < IMAGES_COUNT && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            if (uri != null) {
                Log.i(TAG, "onActivityResult: " + uri.getPath());
                mEntry.setImageListItem(pos, uri);
                mAddFaceAdapter.notifyItemChanged(pos);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUploadSubscription != null) mUploadSubscription.unsubscribe();
    }
}
