package de.hsrm.objectify.ui;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class SliderInputPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener {
	
	private Context context;
	private int defaultValue, maxValue, value;
	private SeekBar slider;
	private String suffix;
	private TextView valueText;
	private static final String androidns="http://schemas.android.com/apk/res/android";
	private final int MIN_PICTURES = 3;
	private final int DEFAULT_PICTURES = 4;
	private final int MAX_PIXTURES = 9;
	
	public SliderInputPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		suffix = attrs.getAttributeValue(androidns, "text");
		defaultValue = attrs.getAttributeIntValue(androidns, "defaultValue", DEFAULT_PICTURES);
		maxValue = attrs.getAttributeIntValue(androidns, "max", MAX_PIXTURES);
	}

	public SliderInputPreference(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
		suffix = attrs.getAttributeValue(androidns, "text");
		defaultValue = attrs.getAttributeIntValue(androidns, "defaultValue", DEFAULT_PICTURES);
		maxValue = attrs.getAttributeIntValue(androidns, "max", MAX_PIXTURES);
	}
	
	@Override
	protected View onCreateDialogView() {
		LinearLayout.LayoutParams params;
		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(6, 6, 6, 6);
		
		valueText = new TextView(context);
		valueText.setGravity(Gravity.CENTER_HORIZONTAL);
		valueText.setTextSize(24);
		params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		layout.addView(valueText, params);
		
		slider = new SeekBar(context);
		slider.setOnSeekBarChangeListener(this);
		layout.addView(slider, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		
		if (shouldPersist()) {
			value = getPersistedInt(defaultValue);
		}
		
		slider.setMax(maxValue);
		slider.setProgress(value);
		return layout;
	}
	
	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
		slider.setMax(maxValue);
		slider.setProgress(value);
	}
	
	@Override
	protected void onSetInitialValue(boolean restorePersistedValue,
			Object defaultValue) {
		super.onSetInitialValue(restorePersistedValue, defaultValue);
		if (restorePersistedValue) {
			value = shouldPersist() ? getPersistedInt(this.defaultValue) : DEFAULT_PICTURES;
		} else {
			value = (Integer) defaultValue;
		}
	}
	
	@Override
	public void onProgressChanged(SeekBar seek, int value, boolean fromTouch) {
		String t = String.valueOf(value+MIN_PICTURES);
				valueText.setText(suffix == null ? t : t.concat(suffix));
		if (shouldPersist()) {
			persistInt(value);
		}
		callChangeListener(new Integer(value+MIN_PICTURES));
	}

	@Override
	public void onStartTrackingTouch(SeekBar seek) {}

	@Override
	public void onStopTrackingTouch(SeekBar seek) {}

}
