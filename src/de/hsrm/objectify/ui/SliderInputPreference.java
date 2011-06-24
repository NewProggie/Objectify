package de.hsrm.objectify.ui;

import de.hsrm.objectify.R;
import de.hsrm.objectify.R.menu;
import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * This class implements a custom dialog view holding a slider to adjust the
 * amount of photos which will be used creating a 3d rendered object
 * 
 * @author kwolf001
 * 
 */
public class SliderInputPreference extends DialogPreference implements
		SeekBar.OnSeekBarChangeListener {

	private Context context;
	private int defaultNumberOfPictures, maxNumberOfPictures, currentNumberOfPictures;
	private SeekBar slider;
	private String suffix;
	private TextView numberOfPicturesText;
	private static final String androidns = "http://schemas.android.com/apk/res/android";
	private final int MIN_PICTURES = 3;
	private final int MAX_PIXTURES = 9;
	private final int DEFAULT_PICTURES = 4;

	public SliderInputPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		suffix = " " + context.getString(R.string.settings_suffix_pictures);
		defaultNumberOfPictures = attrs.getAttributeIntValue(androidns, "defaultValue", DEFAULT_PICTURES);
		maxNumberOfPictures = attrs.getAttributeIntValue(androidns, "max", MAX_PIXTURES);
	}

	public SliderInputPreference(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
		suffix = " " + context.getString(R.string.settings_suffix_pictures);
		defaultNumberOfPictures = attrs.getAttributeIntValue(androidns, "defaultValue", DEFAULT_PICTURES);
		maxNumberOfPictures = attrs.getAttributeIntValue(androidns, "max", MAX_PIXTURES);
	}

	@Override
	protected View onCreateDialogView() {
		LinearLayout.LayoutParams params;
		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(6, 6, 6, 6);

		numberOfPicturesText = new TextView(context);
		numberOfPicturesText.setGravity(Gravity.CENTER_HORIZONTAL);
		numberOfPicturesText.setTextSize(24);
		params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		layout.addView(numberOfPicturesText, params);

		slider = new SeekBar(context);
		slider.setOnSeekBarChangeListener(this);
		layout.addView(slider, new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));

		if (shouldPersist()) {
			currentNumberOfPictures = getPersistedInt(defaultNumberOfPictures);
		}

		slider.setMax(maxNumberOfPictures);
		slider.setProgress(currentNumberOfPictures);
		
		return layout;
	}

	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
		slider.setMax(maxNumberOfPictures);
		slider.setProgress(currentNumberOfPictures);
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
		super.onSetInitialValue(restorePersistedValue, defaultValue);
		if (restorePersistedValue) {
			currentNumberOfPictures = shouldPersist() ? getPersistedInt(this.defaultNumberOfPictures) : DEFAULT_PICTURES;
		} else {
			currentNumberOfPictures = (Integer) defaultValue;
		}
	}

	@Override
	public void onProgressChanged(SeekBar seek, int value, boolean fromUser) {
		String t = String.valueOf(value+MIN_PICTURES);
		numberOfPicturesText.setText(suffix == null ? t : t.concat(suffix));
		if (shouldPersist()) {
			persistInt(value+MIN_PICTURES);
		}
		callChangeListener(new Integer(value));
	}

	@Override
	public void onStartTrackingTouch(SeekBar seek) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seek) {
	}

}
