/*
 * Objectify. Copyright (c) 2011-2016. Kai Wolf. All rights reserved.
 * Redistribution and use in source form with or without modification is not permitted.
 */

package de.hsrm.objectify.activities.fragments;

import android.app.Fragment;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import de.hsrm.objectify.R;
import de.hsrm.objectify.database.DatabaseAdapter;
import de.hsrm.objectify.database.DatabaseProvider;
import de.hsrm.objectify.rendering.ObjectModel;
import de.hsrm.objectify.rendering.TouchSurfaceView;
import de.hsrm.objectify.utils.Storage;

/**
 * A simple {@link Fragment} subclass. Activities that contain this fragment must
 * implement the
 * {@link ModelViewerFragment.OnFragmentInteractionListener} interface to handle
 * interactions
 */
public class ModelViewerFragment extends Fragment {
    private static final String ARG_GALLERY_ID = "gallery_id";
    private ObjectModel mObjectModel;
    private String mGalleryId;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the fragment
     * (e.g. upon
     * screen orientation changes)
     */
    public ModelViewerFragment() {}

    /**
     * Use this factory method to create a new instance of this fragment using the
     * provided
     * parameters.
     *
     * @param galleryId gallery database id
     * @return A new instance of fragment ModelViewerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ModelViewerFragment newInstance(String galleryId) {
        ModelViewerFragment fragment = new ModelViewerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_GALLERY_ID, galleryId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mGalleryId = savedInstanceState.getString(ARG_GALLERY_ID);
        } else if (getArguments().getString(ARG_GALLERY_ID) != null) {
            mGalleryId = getArguments().getString(ARG_GALLERY_ID);
            Log.i("ModelViewerFragment", "galleryID: " + mGalleryId);
            String path = getModelPathFromDatabase(mGalleryId);
            /* TODO: load object asynchronously */
            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
                mObjectModel = (ObjectModel) ois.readObject();
                ois.close();
                mObjectModel.setTextureBitmap(getTextureFromDatabase(mGalleryId));
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("ModelViewerFragment", "Could not read objectmodel file");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                Log.e("ModelViewerFragment", "Could not cast to ObjectModel");
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(ARG_GALLERY_ID, mGalleryId);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /* view container, in this case FrameLayout */
        View rootView =
            inflater.inflate(R.layout.fragment_model_viewer, container, false);
        FrameLayout frameLayout =
            (FrameLayout) rootView.findViewById(R.id.modelview_container);

        /* add touchsurface view, for displaying object model */
        Display d = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        d.getSize(size);
        TouchSurfaceView glSurfaceView =
            new TouchSurfaceView(getActivity(), size.x, size.y);
        frameLayout.addView(glSurfaceView);
        glSurfaceView.setObjectModel(mObjectModel);

        return rootView;
    }

    private Bitmap getTextureFromDatabase(String galleryId) {
        ContentResolver cr = getActivity().getContentResolver();
        Uri galleryItemUri = DatabaseProvider.CONTENT_URI.buildUpon()
                                 .appendPath(DatabaseAdapter.DATABASE_TABLE_GALLERY)
                                 .build();
        Cursor c = cr.query(galleryItemUri, null, DatabaseAdapter.GALLERY_ID_KEY + "=?",
            new String[] {mGalleryId}, null);
        c.moveToFirst();
        Bitmap texture = BitmapFactory.decodeFile(Storage.getExternalRootDirectory() + "/"
            + c.getString(DatabaseAdapter.GALLERY_IMAGE_PATH_COLUMN) + "/image_1.png");
        c.close();
        return texture;
    }

    private String getModelPathFromDatabase(String galleryId) {
        ContentResolver cr = getActivity().getContentResolver();
        Uri galleryItemUri = DatabaseProvider.CONTENT_URI.buildUpon()
                                 .appendPath(DatabaseAdapter.DATABASE_TABLE_GALLERY)
                                 .build();
        Cursor c = cr.query(galleryItemUri, null, DatabaseAdapter.GALLERY_ID_KEY + "=?",
            new String[] {mGalleryId}, null);
        c.moveToFirst();
        String objectId = c.getString(DatabaseAdapter.GALLERY_OBJECT_ID_COLUMN);
        c.close();

        Uri objectItemUri = DatabaseProvider.CONTENT_URI.buildUpon()
                                .appendPath(DatabaseAdapter.DATABASE_TABLE_OBJECT)
                                .build();
        c = cr.query(objectItemUri, null, DatabaseAdapter.OBJECT_ID_KEY + "=?",
            new String[] {objectId}, null);
        c.moveToFirst();
        String objFilePath = c.getString(DatabaseAdapter.OBJECT_FILE_PATH_COLUMN);
        c.close();

        return objFilePath;
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
