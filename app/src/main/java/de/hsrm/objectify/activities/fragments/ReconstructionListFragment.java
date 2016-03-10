/*
 * Objectify. Copyright (c) 2011-2016. Kai Wolf. All rights reserved.
 * Redistribution and use in source form with or without modification is not permitted.
 */

package de.hsrm.objectify.activities.fragments;

import android.app.Activity;
import android.app.ListFragment;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import de.hsrm.objectify.activities.adapter.ReconstructionListAdapter;
import de.hsrm.objectify.database.DatabaseAdapter;
import de.hsrm.objectify.database.DatabaseProvider;

/**
 * A list fragment representing a list of Reconstructions. This fragment also supports
 * tablet devices by allowing list items to be given an 'activated' state upon selection.
 * This helps indicate which item is currently being viewed in a {@link
 * ImageViewerFragment}.
 * Activities containing this fragment MUST implement the {@link Callbacks} interface
 */
public class ReconstructionListFragment extends ListFragment {
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The fragment's current callback object, which is notified of list item clicks
     */
    private Callbacks mCallbacks;

    /**
     * The current activated item position. Only used on tablets
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the fragment
     * (e.g. upon
     * screen orientation changes)
     */
    public ReconstructionListFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri galleryUri = DatabaseProvider.CONTENT_URI.buildUpon()
                             .appendPath(DatabaseAdapter.DATABASE_TABLE_GALLERY)
                             .build();
        Cursor cursor = getActivity().managedQuery(galleryUri, null, null, null, null);
        ReconstructionListAdapter adapter =
            new ReconstructionListAdapter(getActivity().getApplicationContext(), cursor);
        setListAdapter(adapter);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstance) {
        super.onViewCreated(view, savedInstance);

        /* restore the previously serialized activated item position */
        if (savedInstance != null
            && savedInstance.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstance.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        /* activities containing this fragment must implement its callbacks */
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException(
                "Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        /* notify the active callbacks interface (the activity, if the fragment is
         * attached to one)
         * that an item has been selected */
        mCallbacks.onItemSelected(String.valueOf(id));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            /* serialize and persist the activated item position */
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be given the
     * 'activated' state when touched
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        /* when setting CHOICE_MODE_SINGLE, ListView will automatically give items the
         * 'activated'
         * state when touched */
        getListView().setChoiceMode(activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
                                                        : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

    /**
     * A callback interface that all activities containing this fragment must implement.
     * This
     * mechanism allows activities to be notified of item selections
     */
    public interface Callbacks { void onItemSelected(String id); }
}
