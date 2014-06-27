package de.hsrm.objectify.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.ImageView;

public class LightSourceView extends ImageView {

    private OnLightSourceChangeListener onLightSourceChangeListener;

    public LightSourceView(Context context) {
        super(context);
    }

    public LightSourceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LightSourceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setLightSourceChangeListener(OnLightSourceChangeListener listener) {
        this.onLightSourceChangeListener = listener;
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        if (onLightSourceChangeListener != null) {
            onLightSourceChangeListener.lightSourceChanged();
        }
    }

    public static interface OnLightSourceChangeListener {
        public void lightSourceChanged();
    }
}