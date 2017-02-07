package faceassist.faceassist.Camera.CameraComponents;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;


import faceassist.faceassist.Camera.CameraComponents.Utils.CustomFace;
import faceassist.faceassist.Camera.CameraComponents.Utils.SquareFaceView;
import faceassist.faceassist.Camera.CameraComponents.Utils.SquareFaceView.OnFaceSelected;
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

public class FacialRecFragment extends Fragment implements OnFaceSelected, OnFinished {

    public static final String TAG = FacialRecFragment.class.getSimpleName();

    private OnConfirmFace mOnConfirmFace;

    private static final String IMAGE_URI = "image_uri";

    private View vProgressBar;
    private View vConfirmButton;
    private ImageView vImageView;

    private FaceDetector mFaceDetector;

    private Uri mImageUri;

    private Subscription mFacialRecSubscription;
    private Subscription mCropSubscription;
    private FrameLayout vImageParent;
    private Toolbar vToolbar;

    private SquareFaceView mSelectedFace;
    private Bitmap mImageBitmap;


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

        vImageParent = (FrameLayout) root.findViewById(R.id.image_parent);
        vProgressBar = root.findViewById(R.id.progressbar);
        vConfirmButton = root.findViewById(R.id.confirm_button);
        vImageView = (ImageView) root.findViewById(R.id.image);

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


        mSelectedFace = null;

        if (mImageUri != null) {

            Glide.with(this)
                    .load(mImageUri)
                    .dontAnimate()
                    .into(vImageView);

            //loading image and processing image separately so it's faster and less lag
            DisplayMetrics metrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int smaller = metrics.widthPixels > metrics.heightPixels ? metrics.heightPixels : metrics.widthPixels;

            Glide.with(getContext())
                    .load(mImageUri)
                    .asBitmap()
//                    .centerCrop()
                    .listener(new RequestListener<Uri, Bitmap>() {
                        @Override
                        public boolean onException(Exception e, Uri model, Target<Bitmap> target, boolean isFirstResource) {
                            return true;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Uri model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            Log.i(TAG, "onResourceReady: w " + resource.getWidth());
                            Log.i(TAG, "onResourceReady: h " + resource.getHeight());
                            runFacialRec(resource);
                            return true;
                        }
                    }).into(smaller, smaller);

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

                            SquareFaceView v = new SquareFaceView(getContext(), face);
                            vImageParent.addView(v);
                            v.updatePosition();
                            v.setOnFaceSelected(FacialRecFragment.this);
                        }

                        vToolbar.setTitle("Select a face");
                    }
                });

    }

    private SparseArray<Face> detectFaces(Bitmap bitmap) {
        if (getContext() == null) return null;

        mFaceDetector = new FaceDetector.Builder(getContext())
                .setMinFaceSize(0.2f)
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
        if (mSelectedFace == null) {
            Toast.makeText(getContext(), "Please select a face", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgress(true);
        mCropSubscription = Observable.just(cropImage())
                .subscribeOn(io())
                .observeOn(mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String bitmap) {
                        if (bitmap != null && mOnConfirmFace != null) {
                            mOnConfirmFace.onConfirmFace(bitmap, FacialRecFragment.this);
                        } else {
                            onFinished();
                        }
                    }
                });
    }

    private String cropImage() {
        if (mImageBitmap != null) {
            CustomFace face = mSelectedFace.getFace();

            Bitmap bitmap = Bitmap.createBitmap(mImageBitmap, face.x, face.y, face.width, face.height);

            //NOTE: test code
            ImageUtils.savePicture(getContext(), bitmap);

            return ImageUtils.encodeImageBase64(bitmap);

        }

        return null;
    }

    private int clip(int input, int min, int max) {
        if (input < min) return 0;
        if (input > max) return max;

        return input;
    }

    @Override
    public void onFaceSelected(SquareFaceView v) {
        if (mSelectedFace != null) {
            mSelectedFace.setSelected(false);
            mSelectedFace.invalidate();
        }

        mSelectedFace = v;
        mSelectedFace.setSelected(true);
        mSelectedFace.invalidate();
    }

    @Override
    public void onFinished() {
        showProgress(false);
    }

    public interface OnConfirmFace {
        void onConfirmFace(String bitmapString, OnFinished onFinished);

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

