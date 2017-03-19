package faceassist.faceassist.Components.Fragments.FacialRec;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import faceassist.faceassist.Components.Fragments.FacialRec.ImageView.FaceDetectionImageView;
import faceassist.faceassist.R;
import faceassist.faceassist.Utils.OnFinished;


/**
 * Created by QiFeng on 1/31/17.
 */

public class FacialRecFragment extends Fragment implements OnFinished, FacialRecContract.View {

    public static final String TAG = FacialRecFragment.class.getSimpleName();

    private OnFaceResult mOnFaceResult;

    private static final String IMAGE_URI = "image_uri";

    private View vProgressBar;
    private FloatingActionButton vConfirmButton;

    private Uri mImageUri;

    private Toolbar vToolbar;

    private FaceDetectionImageView vImageView;
    private FacialRecContract.Presenter mFacialRecPresenter;


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
            mOnFaceResult = (OnFaceResult) context;
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
        vConfirmButton = (FloatingActionButton) root.findViewById(R.id.confirm_button);
        vImageView = (FaceDetectionImageView) root.findViewById(R.id.image_view);

        vImageView.setFaceDetectionListener(mFacialRecPresenter);

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
                mFacialRecPresenter.clickedSubmit(vImageView);
            }
        });

        return root;
    }


    @Override
    public void setPresenter(FacialRecPresenter presenter){
        mFacialRecPresenter = presenter;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null)
            mImageUri = getArguments().getParcelable(IMAGE_URI);

        if (mImageUri != null) {
            vImageView.setImageUri(mImageUri);
        }
    }


    @Override
    public void showProgress(boolean show) {
        if (show) {
            vProgressBar.setVisibility(View.VISIBLE);
            vConfirmButton.setVisibility(View.INVISIBLE);
        } else {
            vConfirmButton.setVisibility(View.VISIBLE);
            vProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void setToolbarTitle(String title) {
        vToolbar.setTitle(title);
    }

    @Override
    public void showToast(String message) {
        if (getActivity() != null)
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showAlert(@StringRes int title, @StringRes int message) {
        if (getContext() != null)
            new AlertDialog.Builder(getActivity())
                    .setTitle(title)
                    .setMessage(message)
                    .setNegativeButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .show();
    }

    @Override
    public void setSubmitButtonImage(@DrawableRes int image) {
        vConfirmButton.setImageResource(image);
    }

    @Override
    public void faceCropped(Uri uri) {
        //ImageUtils.savePicture(getContext(), bitmap);

        if (mOnFaceResult != null)
            mOnFaceResult.onFaceResult(uri, this);

    }


    @Override
    public void onFinished() {
        showProgress(false);
    }

    public interface OnFaceResult {
        void onFaceResult(Uri uri, OnFinished onFinished);

        //called when this fragment is destroyed
        //stop actions if FacialRecFragment is destroyed
        void onSearchStopped();
    }


    @Override
    public void onStop() {
        super.onStop();
        mFacialRecPresenter.stopProcesses();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mOnFaceResult.onSearchStopped();
    }
}

