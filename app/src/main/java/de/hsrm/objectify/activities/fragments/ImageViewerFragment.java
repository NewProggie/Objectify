/*
 * Objectify. Copyright (c) 2011-2016. Kai Wolf. All rights reserved.
 * Redistribution and use in source form with or without modification is not permitted.
 */

package de.hsrm.objectify.activities.fragments;

import android.app.Fragment;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import de.hsrm.objectify.R;
import de.hsrm.objectify.activities.ReconstructionDetailActivity;
import de.hsrm.objectify.database.DatabaseAdapter;
import de.hsrm.objectify.database.DatabaseProvider;
import de.hsrm.objectify.rendering.ReconstructionService;
import de.hsrm.objectify.utils.BitmapUtils;
import de.hsrm.objectify.utils.Storage;

/**
 * A fragment representing a single Reconstruction detail screen.
 * This fragment is either contained in a {@link
 * de.hsrm.objectify.activities.ReconstructionListActivity}
 * in two-pane mode (on tablets) or a {@link
 * de.hsrm.objectify.activities.ReconstructionDetailActivity}
 * on handsets.
 */
public class ImageViewerFragment extends Fragment {
    public static final String ARG_GALLERY_ID = "gallery_id";
    public static final String ARG_IMAGE_TYPE = "image_type";
    private String mGalleryId;
    private String mImagePath;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the fragment
     * (e.g. upon screen orientation changes)
     */
    public ImageViewerFragment() {}

    /**
     * Use this factory method to create a new instance of this fragment using the
     * provided
     * parameters.
     *
     * @param galleryId gallery database id
     * @return A new instance of fragment ModelViewerFragment.
     */
    public static ImageViewerFragment newInstance(String galleryId, String recType) {
        ImageViewerFragment fragment = new ImageViewerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_GALLERY_ID, galleryId);
        args.putString(ARG_IMAGE_TYPE, recType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mGalleryId = getArguments().getString(ARG_GALLERY_ID);
            String imageType = getArguments().getString(ARG_IMAGE_TYPE);
            String dirPath = getDirectoryPathFromDatabase(mGalleryId);
            mImagePath = getReconstructionTypeImagePath(dirPath, imageType);
        }
    }

    private String getReconstructionTypeImagePath(String dirPath, String recType) {
        if (recType.equalsIgnoreCase(ReconstructionDetailActivity.REC_NORMALMAP)) {
            return Storage.getExternalRootDirectory() + "/" + dirPath + "/"
                + ReconstructionService.NORMAL_IMG_NAME;
        } else if (recType.equalsIgnoreCase(ReconstructionDetailActivity.REC_HEIGHTMAP)) {
            return Storage.getExternalRootDirectory() + "/" + dirPath + "/"
                + ReconstructionService.HEIGHT_IMG_NAME;
        }

        return null;
    }

    private String getDirectoryPathFromDatabase(String galleryId) {
        ContentResolver cr = getActivity().getContentResolver();
        Uri galleryItemUri = DatabaseProvider.CONTENT_URI.buildUpon()
                                 .appendPath(DatabaseAdapter.DATABASE_TABLE_GALLERY)
                                 .build();
        Cursor c = cr.query(galleryItemUri, null, DatabaseAdapter.GALLERY_ID_KEY + "=?",
                new String[]{mGalleryId}, null);
        String imgFilePath = null;
        if (c != null) {
            c.moveToFirst();
            imgFilePath = c.getString(DatabaseAdapter.GALLERY_IMAGE_PATH_COLUMN);
            c.close();
        }

        return imgFilePath;
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView =
            inflater.inflate(R.layout.fragment_normal_map_view, container, false);
        ImageView reconstructionImageView =
            (ImageView) rootView.findViewById(R.id.reconstruction_image);
        reconstructionImageView.setImageBitmap(BitmapUtils.openBitmap(mImagePath));
        return rootView;
    }

    /**
     * This interface must be implemented by activities that contain this fragment to
     * allow an
     * interaction in this fragment to be communicated to the activity and potentially
     * other
     * fragments contained in that activity.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
