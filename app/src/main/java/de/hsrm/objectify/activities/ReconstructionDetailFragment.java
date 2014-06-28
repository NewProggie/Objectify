package de.hsrm.objectify.activities;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import de.hsrm.objectify.R;
import de.hsrm.objectify.activities.dummy.DummyContent;
import de.hsrm.objectify.rendering.ReconstructionService;

/**
 * A fragment representing a single Reconstruction detail screen.
 * This fragment is either contained in a {@link ReconstructionListActivity}
 * in two-pane mode (on tablets) or a {@link ReconstructionDetailActivity}
 * on handsets.
 */
public class ReconstructionDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";
    private ImageView mNormalMapView;
    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String normalMapPath = bundle.getString(ReconstructionService.NORMALMAP);
                mNormalMapView.setImageBitmap(BitmapFactory.decodeFile(normalMapPath));
            }
        }
    };

    /**
     * The dummy content this fragment is presenting.
     */
    private DummyContent.DummyItem mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ReconstructionDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(
                receiver, new IntentFilter(ReconstructionService.NOTIFICATION));
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(receiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_reconstruction_detail, container, false);

        mNormalMapView = (ImageView) rootView.findViewById(R.id.normalmap);

        return rootView;
    }
}
