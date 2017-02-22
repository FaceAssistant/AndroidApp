package faceassist.faceassist.Components.Fragments.FacialRec;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;


import java.io.IOException;

import faceassist.faceassist.Components.Fragments.FacialRec.ImageView.CustomFace;
import faceassist.faceassist.Components.Fragments.FacialRec.ImageView.FaceDetectionImageView;
import faceassist.faceassist.R;
import faceassist.faceassist.Utils.ImageUtils;
import faceassist.faceassist.Utils.OnFinished;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;

import static rx.android.schedulers.AndroidSchedulers.mainThread;
import static rx.schedulers.Schedulers.io;
import static rx.schedulers.Schedulers.newThread;

/**
 * Created by QiFeng on 1/31/17.
 */

public class FacialRecFragment extends Fragment implements OnFinished {

    public static final String TAG = FacialRecFragment.class.getSimpleName();

    private OnConfirmFace mOnConfirmFace;

    private static final String IMAGE_URI = "image_uri";

    private View vProgressBar;
    private View vConfirmButton;

    private FaceDetector mFaceDetector;

    private Uri mImageUri;

    private Subscription mFacialRecSubscription;
    private Subscription mCropSubscription;

    private Toolbar vToolbar;
    private Bitmap mImageBitmap;

    private FaceDetectionImageView vImageView;


    public static FacialRecFragment newInstance(Uri imageUri) {
        FacialRecFragment fragment = new FacialRecFragment();

        Bundle args = new Bundle();
        args.putParcelable(IMAGE_URI, imageUri);

        fragment.setArguments(args);
        return fragment;
    }

    public FacialRecFragment() {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mOnConfirmFace = (OnConfirmFace) context;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_facial_rec, container, false);

        vProgressBar = root.findViewById(R.id.progressbar);
        vConfirmButton = root.findViewById(R.id.confirm_button);
        vImageView = (FaceDetectionImageView) root.findViewById(R.id.image_view);

        vToolbar = (Toolbar) root.findViewById(R.id.toolbar);
        vToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity() != null)
                    getActivity().onBackPressed();
            }
        });

        vConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onConfirmClick();
            }
        });

        return root;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null)
            mImageUri = getArguments().getParcelable(IMAGE_URI);

        if (mImageUri != null) {

            mFacialRecSubscription = Observable.just(loadImages(mImageUri))
                    .subscribeOn(io())
                    .observeOn(mainThread())
                    .subscribe(new Action1<Bitmap>() {
                        @Override
                        public void call(final Bitmap bitmap) {
                            if (bitmap != null) {
                                vImageView.setBitmap(bitmap);
                                vImageView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        runFacialRec(bitmap);
                                    }
                                });
                            }
                        }
                    });
        }
    }

    private Bitmap loadImages(Uri image){
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        int reqSize = height > width ? width : height;

        try {
            Bitmap cropped = ImageUtils.decodeUri(getContext(), image, reqSize);
            Log.i(TAG, "loadImages:cropped "+cropped.getHeight());
            if (cropped.getHeight() == reqSize) return cropped;


            Bitmap scaled = Bitmap.createScaledBitmap(cropped, reqSize, reqSize, true);
            Log.i(TAG, "loadImages: scaled "+scaled.getHeight());
            if (scaled != cropped) cropped.recycle();
            return scaled;

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error loading image", Toast.LENGTH_SHORT).show();
            return null;
        }

    }


    private void runFacialRec(Bitmap bitmap) {

        if (mImageBitmap != null && mImageBitmap != bitmap)
            mImageBitmap.recycle();

        mImageBitmap = bitmap;

        mFacialRecSubscription = Observable.just(detectFaces(bitmap))
                .subscribeOn(newThread())
                .observeOn(mainThread())
                .subscribe(new Action1<SparseArray<Face>>() {
                    @Override
                    public void call(SparseArray<Face> faceSparseArray) {
                        if (getContext() == null || faceSparseArray == null) return;
                        for (int i = 0; i < faceSparseArray.size(); i++) {

                            CustomFace face = new CustomFace(faceSparseArray.valueAt(i),
                                    mImageBitmap.getWidth(), mImageBitmap.getHeight());

                            Log.d(TAG, face.toString());

                            vImageView.addFace(face);
                        }

                        vToolbar.setTitle("Select a face");
                    }
                });

    }

    private SparseArray<Face> detectFaces(Bitmap bitmap) {
        if (getContext() == null) return null;

        mFaceDetector = new FaceDetector.Builder(getContext())
                .setMinFaceSize(0.25f) //NOTE: proportion to image. change accordingly
                .setTrackingEnabled(false)
                .build();

        if (!mFaceDetector.isOperational()) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Facial recognition not ready")
                    .setMessage("Your phone is currently installing facial recognition capabilities. Please try again when installation has finished.")
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).show();

            return null;
        }

        final Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        return mFaceDetector.detect(frame);
    }


    private void showProgress(boolean show) {
        if (show) {
            vProgressBar.setVisibility(View.VISIBLE);
            vConfirmButton.setVisibility(View.INVISIBLE);
        } else {
            vConfirmButton.setVisibility(View.VISIBLE);
            vProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void onConfirmClick() {
        if (getContext() == null) return;
        if (vImageView.getSelectedFace() == null) {
            Toast.makeText(getContext(), "Please select a face", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgress(true);
        mCropSubscription = Observable.just(cropImage())
                .subscribeOn(io())
                .observeOn(mainThread())
                .subscribe(new Action1<Bitmap>() {
                    @Override
                    public void call(Bitmap bitmap) {
                        if (bitmap != null && mOnConfirmFace != null) {
                            mOnConfirmFace.onConfirmFace(bitmap, FacialRecFragment.this);
                            //Log.i(TAG, "call: cropped");
                        } else {
                            onFinished();
                        }
                    }
                });
    }

    private Bitmap cropImage() {
        if (mImageBitmap != null) {
            CustomFace face = vImageView.getSelectedFace().getFace();

            Bitmap bitmap = Bitmap.createBitmap(mImageBitmap, face.x, face.y, face.getWidth(), face.getHeight());

            //NOTE: test code
            ImageUtils.savePicture(getContext(), bitmap);

            return bitmap; //ImageUtils.encodeImageBase64(bitmap);

        }

        return null;
    }


    @Override
    public void onFinished() {
        showProgress(false);
    }

    public interface OnConfirmFace {
        void onConfirmFace(Bitmap bitmap, OnFinished onFinished);

        //called when this fragment is destroyed
        //stop actions if FacialRecFragment is destroyed
        void onStopSearch();
    }


    @Override
    public void onStop() {
        super.onStop();
        if (mCropSubscription != null) mCropSubscription.unsubscribe();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFaceDetector != null) mFaceDetector.release();
        if (mFacialRecSubscription != null) mFacialRecSubscription.unsubscribe();

        mOnConfirmFace.onStopSearch();
    }
}

