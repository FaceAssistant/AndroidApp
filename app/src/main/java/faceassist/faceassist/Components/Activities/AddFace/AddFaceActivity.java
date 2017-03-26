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
import faceassist.faceassist.Components.Activities.AddFace.Models.Entry;
import faceassist.faceassist.Components.Activities.AddFace.RecyclerView.AddFaceAdapter;
import faceassist.faceassist.Components.Activities.AddFace.RecyclerView.ImageEntryViewHolder;
import faceassist.faceassist.Components.Activities.Camera.PictureUriActivity;
import faceassist.faceassist.Components.Activities.Gallery.GalleryActivity;
import faceassist.faceassist.R;
import faceassist.faceassist.Upload.UploadIntentService;

/**
 * Created by QiFeng on 2/13/17.
 */


//TODO: clean up

public class AddFaceActivity extends AppCompatActivity implements View.OnClickListener, ImageEntryViewHolder.OnImageClick {

    private static final int IMAGES_COUNT = 12;
    private static final String TAG = AddFaceActivity.class.getSimpleName();

    private AddFaceAdapter mAddFaceAdapter;
    private Entry mEntry = new Entry(IMAGES_COUNT);

    private EditText vNameEditText;
    private EditText vRelationshipEditText;
    private EditText vNotesEditText;

    private ViewSwitcher vViewSwitcher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_face);

        vViewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);

        RecyclerView vRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

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


    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
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
        for (Uri uri : mEntry.getImageList()) {
            if (uri == null) {
                Toast.makeText(this, R.string.not_enough_images, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        startUpload();
    }

    private void startUpload(){
        Toast.makeText(this, R.string.uploading, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, UploadIntentService.class);
        intent.putExtra(UploadIntentService.PENDING_UPLOAD_KEY, mEntry);
        startService(intent);
        finish();
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
                //Log.i(TAG, "onActivityResult: " + uri.getPath());
                mEntry.setImageListItem(pos, uri);
                mAddFaceAdapter.notifyItemChanged(pos);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
