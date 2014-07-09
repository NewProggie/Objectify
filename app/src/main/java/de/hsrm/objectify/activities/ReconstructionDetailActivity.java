package de.hsrm.objectify.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import de.hsrm.objectify.R;
import de.hsrm.objectify.activities.fragments.HeightMapViewFragment;
import de.hsrm.objectify.activities.fragments.InputImagesViewFragment;
import de.hsrm.objectify.activities.fragments.ModelViewerFragment;
import de.hsrm.objectify.activities.fragments.NormalMapViewFragment;
import de.hsrm.objectify.camera.Constants;

/**
 * An activity representing a single Reconstruction detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ReconstructionListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link de.hsrm.objectify.activities.fragments.NormalMapViewFragment}.
 */
public class ReconstructionDetailActivity extends Activity implements
        HeightMapViewFragment.OnFragmentInteractionListener,
        InputImagesViewFragment.OnFragmentInteractionListener,
        ModelViewerFragment.OnFragmentInteractionListener{

    private SpinnerAdapter mSpinnerAdapter;
    private Fragment mCurrentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reconstruction_detail);

        mSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.reconstruction_views,
                android.R.layout.simple_spinner_dropdown_item);

        /* show the Up button in the action bar. */
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setListNavigationCallbacks(mSpinnerAdapter, spinnerNavigationCallback());

        /* savedInstanceState is non-null when there is fragment state saved from previous
         * configurations of this activity (e.g. when rotating the screen from portrait to
         * landscape). In this case, the fragment will automatically be re-added to its container
         * so we don't need to manually add it */
        if (savedInstanceState == null) {
            /* create the detail fragment and add it to the activity using a fragment transaction */
            mCurrentFragment = new NormalMapViewFragment();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.add(R.id.reconstruction_detail_container, mCurrentFragment);
            transaction.commit();
        }
    }

    private ActionBar.OnNavigationListener spinnerNavigationCallback() {
        return new ActionBar.OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                switch (itemPosition) {
                    case Constants.ReconstructionType.NORMALMAP:
                        mCurrentFragment = new NormalMapViewFragment();
                        break;
                    case Constants.ReconstructionType.HEIGHTMAP:
                        mCurrentFragment = new HeightMapViewFragment();
                        break;
                    case Constants.ReconstructionType.RECONSTRUCTION:
                        mCurrentFragment = new ModelViewerFragment();
                        break;
                }

                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.reconstruction_detail_container, mCurrentFragment);
                transaction.commit();
                return true;
            }
        };
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
