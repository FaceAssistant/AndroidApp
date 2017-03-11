package faceassist.faceassist.Components.Activities.AddFace;

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

import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import faceassist.faceassist.API.API;
import faceassist.faceassist.Components.Activities.AddFace.Models.Entry;
import faceassist.faceassist.Components.Activities.AddFace.Models.ImageEntry;
import faceassist.faceassist.Components.Activities.AddFace.Models.TextEntry;
import faceassist.faceassist.Components.Activities.AddFace.RecyclerView.AddFaceAdapter;
import faceassist.faceassist.Components.Activities.AddFace.RecyclerView.ImageEntryViewHolder;
import faceassist.faceassist.Components.Activities.Camera.PictureUriActivity;
import faceassist.faceassist.Components.Activities.Gallery.GalleryActivity;
import faceassist.faceassist.R;
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

public class AddFaceActivity extends AppCompatActivity implements View.OnClickListener, ImageEntryViewHolder.OnImageClick {

    private static final int IMAGES_START = 1;
    private static final int IMAGES_COUNT = 12;
    private static final String TAG = AddFaceActivity.class.getSimpleName();

    private AddFaceAdapter mAddFaceAdapter;
    private RecyclerView vRecyclerView;
    private View vProgress;

    private Subscription mUploadSubscription;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_face);
        vRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        vProgress = findViewById(R.id.progress);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position >= IMAGES_START && position < IMAGES_COUNT + IMAGES_START)
                    return 1;
                return 4;
            }
        });

        vRecyclerView.setLayoutManager(gridLayoutManager);
        vRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    View v = getCurrentFocus();
                    if (v != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }
        });


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        if (mAddFaceAdapter == null) mAddFaceAdapter = new AddFaceAdapter();

        mAddFaceAdapter.setOnImageClick(this);
        mAddFaceAdapter.setConfirmButton(this);

        vRecyclerView.setAdapter(mAddFaceAdapter);

    }


    @Override
    public void onClick(View view) {
        showProgress(true);
        Log.i(TAG, "onClick: ");
        mUploadSubscription = Observable.just(getRequestBody())
                .subscribeOn(io())
                .observeOn(mainThread())
                .subscribe(new Action1<HashMap<String, Object>>() {
                    @Override
                    public void call(HashMap<String, Object> stringObjectHashMap) {
                        API.post(new String[]{"face", "train"},
                                new HashMap<String, String>(),
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
        HashMap<String, Object> params = new HashMap<>();
        JSONArray array = new JSONArray();
        for (Entry entry : mAddFaceAdapter.getEntries()) {
            if (entry instanceof TextEntry) {
                TextEntry textEntry = (TextEntry) entry;
                params.put(textEntry.getTitle(), textEntry.getBody());
            } else if (entry instanceof ImageEntry) {
                ImageEntry imageEntry = (ImageEntry) entry;
                if (imageEntry.getImageUri() != null) {
                    try {
                        array.put(FileUtils.encodeFileBase64(new File(imageEntry.getContent())));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else Log.i(TAG, "getRequestBody: entry was null");
            }
        }
        params.put("images", array);
        return params;
    }

    private void showProgress(boolean show) {
        if (show) {
            vRecyclerView.setVisibility(View.GONE);
            vProgress.setVisibility(View.VISIBLE);
        } else {
            vProgress.setVisibility(View.GONE);
            vRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onImageClick(final int requestCode) {

        new AlertDialog.Builder(this)
                .setItems(new String[]{"Camera", "Gallery"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Class c = PictureUriActivity.class;

                        if (i == 1){
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
        if (pos > 0 && pos <= 12) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getParcelableExtra(GalleryActivity.URI_KEY);
                if (uri != null) {
                    Log.i(TAG, "onActivityResult: " + uri.getPath());
                    ((ImageEntry) mAddFaceAdapter.getEntries().get(pos)).setImageUri(uri);
                    mAddFaceAdapter.notifyItemChanged(pos);
                }
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
