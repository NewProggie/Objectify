package de.hsrm.objectify.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import de.hsrm.objectify.R;
import de.hsrm.objectify.activities.fragments.ImageViewerFragment;
import de.hsrm.objectify.activities.fragments.ReconstructionListFragment;

/**
 * An activity representing a list of Reconstructions. This activity has different presentations for
 * handset and tablet-size devices. On handsets, the activity presents a list of items, which when
 * touched, lead to a {@link ReconstructionDetailActivity} representing item details. On tablets,
 * the activity presents the list of items and item details side-by-side using two vertical panes.
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ReconstructionListFragment} and the item details (if present) is a
 * {@link de.hsrm.objectify.activities.fragments.ImageViewerFragment}. This activity also implements
 * the required {@link de.hsrm.objectify.activities.fragments.ReconstructionListFragment.Callbacks}
 * interface to listen for item selections. */
public class ReconstructionListActivity extends Activity
        implements ReconstructionListFragment.Callbacks {

    /** Whether or not the activity is in two-pane mode, i.e. running on a tablet device. */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reconstruction_list);

        if (getIntent().getBooleanExtra(CameraActivity.RECONSTRUCTION, false)) {
            /** We are on a small-screen layout and a new reconstruction has been started. Moving
             * to reconstruction detail activity. */
            Intent view3DModel = new Intent(getApplicationContext(),
                    ReconstructionDetailActivity.class);
            startActivity(view3DModel);
            finish();
        } else if (findViewById(R.id.reconstruction_detail_container) != null) {
            /** The detail container view will be present only in the large-screen layouts
             * (res/values-large and res/values-sw600dp). If this view is present, then the activity
             * should be in two-pane mode. */
            mTwoPane = true;

            /* In two-pane mode, list items should be given the 'activated' state when touched. */
            ((ReconstructionListFragment) getFragmentManager()
                    .findFragmentById(R.id.reconstruction_list))
                    .setActivateOnItemClick(true);
        }
    }

    /** Callback method from {@link ReconstructionListFragment.Callbacks} indicating that the item
     * with the given ID was selected. */
    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            /** In two-pane mode, show the detail view in this activity by adding or replacing the
             * detail fragment using a fragment transaction. */
            ImageViewerFragment fragment = ImageViewerFragment.newInstance(id,
                    ReconstructionDetailActivity.REC_NORMALMAP);
            getFragmentManager().beginTransaction()
                    .replace(R.id.reconstruction_detail_container, fragment)
                    .commit();

        } else {
            /** In single-pane mode, simply start the detail activity for the selected item ID. */
            Intent detailIntent = new Intent(this, ReconstructionDetailActivity.class);
            detailIntent.putExtra(ImageViewerFragment.ARG_GALLERY_ID, id);
            startActivity(detailIntent);
        }
    }
}
