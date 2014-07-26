package de.hsrm.objectify.activities.fragments;

import android.app.Fragment;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.hsrm.objectify.R;
import de.hsrm.objectify.database.DatabaseAdapter;
import de.hsrm.objectify.database.DatabaseProvider;

/**
 * A simple {@link Fragment} subclass. Activities that contain this fragment must implement the
 * {@link ModelViewerFragment.OnFragmentInteractionListener} interface to handle interactions */
public class ModelViewerFragment extends Fragment {

    private static final String ARG_GALLERY_ID = "gallery_id";
    private String mGalleryId;

    /** Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon
     * screen orientation changes) */
    public ModelViewerFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mGalleryId = getArguments().getString(ARG_GALLERY_ID);
            String path = getModelPathFromDatabase(mGalleryId);
            /* TODO: load object asynchronously */
        }
    }

    /**
     * Use this factory method to create a new instance of this fragment using the provided
     * parameters.
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_model_viewer, container, false);
    }

    private String getModelPathFromDatabase(String galleryId) {
        ContentResolver cr = getActivity().getContentResolver();
        Uri galleryItemUri = DatabaseProvider.CONTENT_URI.buildUpon()
                .appendPath(DatabaseAdapter.DATABASE_TABLE_GALLERY).build();
        Cursor c = cr.query(galleryItemUri, null, DatabaseAdapter.GALLERY_ID_KEY + "=?",
                new String[] { mGalleryId}, null);
        c.moveToFirst();
        String objectId = c.getString(DatabaseAdapter.GALLERY_OBJECT_ID_COLUMN);
        c.close();

        Uri objectItemUri = DatabaseProvider.CONTENT_URI.buildUpon()
                .appendPath(DatabaseAdapter.DATABASE_TABLE_OBJECT).build();
        c = cr.query(objectItemUri, null, DatabaseAdapter.OBJECT_ID_KEY + "=?",
                new String[] { objectId }, null);
        c.moveToFirst();
        String objFilePath = c.getString(DatabaseAdapter.OBJECT_FILE_PATH_COLUMN);
        c.close();

        return objFilePath;
    }

    /** This interface must be implemented by activities that contain this fragment to allow an
     * interaction in this fragment to be communicated to the activity and potentially other
     * fragments contained in that activity. */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }
}
