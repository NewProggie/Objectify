/*
 * Objectify. Copyright (c) 2011-2016. Kai Wolf. All rights reserved.
 * Redistribution and use in source form with or without modification is not permitted.
 */

package de.hsrm.objectify.activities.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import de.hsrm.objectify.database.DatabaseAdapter;

public class ReconstructionListAdapter extends CursorAdapter {
    public ReconstructionListAdapter(Context context, Cursor cursor) {
        super(context, cursor);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return new TextView(context);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView textView = (TextView) view;
        String imgPath = cursor.getString(DatabaseAdapter.GALLERY_IMAGE_PATH_COLUMN);
        textView.setText(imgPath);
    }
}
