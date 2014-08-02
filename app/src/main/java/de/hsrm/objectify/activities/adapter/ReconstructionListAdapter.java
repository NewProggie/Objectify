package de.hsrm.objectify.activities.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import de.hsrm.objectify.database.DatabaseAdapter;

public class ReconstructionListAdapter extends CursorAdapter {

    private Cursor mCursor;

    public ReconstructionListAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        mCursor = cursor;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        TextView textView = new TextView(context);
        return textView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView textView = (TextView) view;
        String imgPath = cursor.getString(DatabaseAdapter.GALLERY_IMAGE_PATH_COLUMN);
        textView.setText(imgPath);
    }
}
