package de.hsrm.objectify.activities.fragments;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import de.hsrm.objectify.R;
import de.hsrm.objectify.rendering.ObjectModel;
import de.hsrm.objectify.rendering.ReconstructionService;

/**
 * A fragment representing a single Reconstruction detail screen.
 * This fragment is either contained in a {@link de.hsrm.objectify.activities.ReconstructionListActivity}
 * in two-pane mode (on tablets) or a {@link de.hsrm.objectify.activities.ReconstructionDetailActivity}
 * on handsets.
 */
public class ImageViewerFragment extends Fragment {

    public static final String ARG_GALLERY_ID = "gallery_id";
    public static final String ARG_IMAGE_TYPE = "image_type";
    private String mGalleryId;
    private String mImageType;
    private ImageView mReconstructionImageView;

    /** Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon
     * screen orientation changes) */
    public ImageViewerFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mGalleryId = getArguments().getString(ARG_GALLERY_ID);
            mImageType = getArguments().getString(ARG_IMAGE_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_normal_map_view, container, false);
        mReconstructionImageView = (ImageView) rootView.findViewById(R.id.normalmap);
        return rootView;
    }

    public void setImage(Bitmap image) {
        mReconstructionImageView.setImageBitmap(image);
    }

    /**
     * This interface must be implemented by activities that contain this fragment to allow an
     * interaction in this fragment to be communicated to the activity and potentially other
     * fragments contained in that activity. */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }


}
