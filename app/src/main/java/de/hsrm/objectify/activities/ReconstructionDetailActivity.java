/*
 * Objectify. Copyright (c) 2011-2016. Kai Wolf. All rights reserved.
 * Redistribution and use in source form with or without modification is not permitted.
 */

package de.hsrm.objectify.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.SpinnerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hsrm.objectify.R;
import de.hsrm.objectify.activities.fragments.ImageViewerFragment;
import de.hsrm.objectify.activities.fragments.ModelViewerFragment;
import de.hsrm.objectify.camera.Constants;
import de.hsrm.objectify.rendering.ReconstructionService;

/**
 * An activity representing a single Reconstruction detail screen. This activity is only
 * used on handset devices. On tablet-size devices, item details are presented
 * side-by-side with a
 * list of items in a {@link ReconstructionListActivity}. This activity is mostly just a
 * 'shell'
 * activity containing no more than a {@link
 * de.hsrm.objectify.activities.fragments.ImageViewerFragment}
 */
public class ReconstructionDetailActivity
    extends Activity implements ImageViewerFragment.OnFragmentInteractionListener,
                                ModelViewerFragment.OnFragmentInteractionListener {
    public static final String REC_NORMALMAP = "normalmap";
    public static final String REC_HEIGHTMAP = "heightmap";
    public static final String REC_3DMODEL = "3dmodel";
    private final String TAG = "ReconstructionDetailActivity";
    private LinearLayout mProgressScreen;
    private Fragment mCurrentFragment;
    private String mGalleryId;
    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                disableProgressScreen();
                mGalleryId = bundle.getString(ReconstructionService.GALLERY_ID);
                mCurrentFragment =
                    ImageViewerFragment.newInstance(mGalleryId, REC_NORMALMAP);
                updateCurrentViewFragment();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reconstruction_detail);

        /* populate android actionbar dropdown list for all fragments */
        List<Map<String, String>> data = new ArrayList<Map<String, String>>();
        String[] titles = getResources().getStringArray(R.array.reconstruction_views);
        String[] subs = getResources().getStringArray(R.array.reconstruction_views_sub);
        for (int i = 0; i < titles.length; i++) {
            Map<String, String> entry = new HashMap<String, String>(2);
            entry.put("title", titles[i]);
            entry.put("subtitle", subs[i]);
            data.add(entry);
        }

        mProgressScreen = (LinearLayout) findViewById(R.id.progress_screen);
        SpinnerAdapter spinnerAdapter = new SimpleAdapter(this, data,
            R.layout.subtitled_spinner_item, new String[] {"title", "subtitle"},
            new int[] {android.R.id.text1, android.R.id.text2});

        /* show the Up button in the action bar. */
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setListNavigationCallbacks(spinnerAdapter, spinnerNavigationCallback());

        /* check if called from reconstruction list or if new scan is happening */
        if (getIntent().hasExtra(ImageViewerFragment.ARG_GALLERY_ID)) {
            disableProgressScreen();
            mGalleryId = getIntent().getStringExtra(ImageViewerFragment.ARG_GALLERY_ID);
            mCurrentFragment = ImageViewerFragment.newInstance(mGalleryId, REC_NORMALMAP);
            updateCurrentViewFragment();
        } else {
            actionBar.hide();
        }
    }

    private void updateCurrentViewFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.reconstruction_detail_container, mCurrentFragment);
        transaction.commit();
    }

    private void disableProgressScreen() {
        getActionBar().show();
        mProgressScreen.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(ReconstructionService.NOTIFICATION));
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    private ActionBar.OnNavigationListener spinnerNavigationCallback() {
        return new ActionBar.OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                switch (itemPosition) {
                    case Constants.ReconstructionType.NORMALMAP:
                        mCurrentFragment =
                            ImageViewerFragment.newInstance(mGalleryId, REC_NORMALMAP);
                        break;
                    case Constants.ReconstructionType.HEIGHTMAP:
                        mCurrentFragment =
                            ImageViewerFragment.newInstance(mGalleryId, REC_HEIGHTMAP);
                        break;
                    case Constants.ReconstructionType.RECONSTRUCTION:
                        mCurrentFragment = ModelViewerFragment.newInstance(mGalleryId);
                        break;
                }

                updateCurrentViewFragment();
                return true;
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* inflate the menu; this adds items to the action bar, if it is present */
        getMenuInflater().inflate(R.menu.reconstruction, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_about:
                return true;
            case android.R.id.home:
                /* this ID represents the Home or Up button. In the case of this activity,
                 * the Up button is shown. Use NavUtils to allow users to navigate up one
                 * level
                 * in the application structure */
                NavUtils.navigateUpTo(
                    this, new Intent(this, ReconstructionListActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {}
}
