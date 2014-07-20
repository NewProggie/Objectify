package de.hsrm.objectify.activities.fragments;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
public class ImageViewerFragment extends IReconstructionFragment implements Updateable {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";
    private ImageView mReconstructionImageView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ImageViewerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
//            mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
//        }
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

    @Override
    public void update(ObjectModel objectModel, Bitmap heightmap, Bitmap normalmap) {
        if (normalmap != null)
            setImage(normalmap);
    }
}
