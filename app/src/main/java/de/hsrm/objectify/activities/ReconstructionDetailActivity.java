package de.hsrm.objectify.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import de.hsrm.objectify.R;
import de.hsrm.objectify.activities.fragments.ModelViewerFragment;
import de.hsrm.objectify.activities.fragments.ReconstructionDetailFragment;
import de.hsrm.objectify.utils.BitmapUtils;
import de.hsrm.objectify.utils.Storage;

/**
 * An activity representing a single Reconstruction detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ReconstructionListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link ReconstructionDetailFragment}.
 */
public class ReconstructionDetailActivity extends Activity implements ModelViewerFragment.OnFragmentInteractionListener {

    private SpinnerAdapter mSpinnerAdapter;
    private ReconstructionDetailFragment mReconstructionDetailFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reconstruction_detail);

        /* show the Up button in the action bar. */
        getActionBar().setDisplayHomeAsUpEnabled(true);

        mSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.reconstruction_views,
                android.R.layout.simple_spinner_dropdown_item);

        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getActionBar().setListNavigationCallbacks(mSpinnerAdapter,
                new ActionBar.OnNavigationListener() {

            @Override
            public boolean onNavigationItemSelected(int position, long itemId) {
                switch (position) {
                    case 0:
                        Bitmap norms = BitmapUtils.openBitmap(Storage.getExternalRootDirectory() +
                                "/aaaaaaaaaa/normals.png");
                        mReconstructionDetailFragment.setImage(norms);
                        return true;
                    case 1:
                        Bitmap heights = BitmapUtils.openBitmap(Storage.getExternalRootDirectory() +
                                "/aaaaaaaaaa/heights.png");
                        mReconstructionDetailFragment.setImage(heights);
                        return true;
                    case 2:
                        ModelViewerFragment mvFrag = new ModelViewerFragment();
                        getFragmentManager().beginTransaction().add(R.id.reconstruction_detail_container, mvFrag).commit();
                        return true;
                }

                return false;
            }
        });

        /* savedInstanceState is non-null when there is fragment state saved from previous
         * configurations of this activity (e.g. when rotating the screen from portrait to
         * landscape). In this case, the fragment will automatically be re-added to its container
         * so we don't need to manually add it */
        if (savedInstanceState == null) {
            /* create the detail fragment and add it to the activity using a fragment transaction */
            Bundle arguments = new Bundle();
            arguments.putString(ReconstructionDetailFragment.ARG_ITEM_ID,
                    getIntent().getStringExtra(ReconstructionDetailFragment.ARG_ITEM_ID));
            mReconstructionDetailFragment = new ReconstructionDetailFragment();
            mReconstructionDetailFragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .add(R.id.reconstruction_detail_container, mReconstructionDetailFragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            /* this ID represents the Home or Up button. In the case of this activity, the Up
             * button is shown. Use NavUtils to allow users to navigate up one level in the
             * application structure */
            NavUtils.navigateUpTo(this, new Intent(this, ReconstructionListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
