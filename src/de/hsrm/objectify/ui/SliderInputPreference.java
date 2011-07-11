package de.hsrm.objectify.ui;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import de.hsrm.objectify.R;
import de.hsrm.objectify.SettingsActivity;

/**
 * This class implements a custom dialog view in the settings. holding a slider to adjust the
 * amount of photos which will be used creating a 3d rendered object.
 * 
 * @author kwolf001
 * 
 */
public class SliderInputPreference extends DialogPreference implements
		SeekBar.OnSeekBarChangeListener {

	private Context context;
	private int currentNumberOfPictures;
	private SeekBar slider;
	private TextView numberOfPicturesText;
	/**
	 * Setting the constant value for minimum amount of pictures taken for a 3d reconstruction.
	 */
	private final int MIN_PICTURES = 3;
	/**
	 * Setting the constant value for maximum amount of pictures taken for a 3d reconstruction.
	 */
	private final int MAX_PIXTURES = 9;
	/**
	 * Setting the constant value for default amount of pictures taken for a 3d reconstruction.
	 */
	private final int DEFAULT_PICTURES = 4;

	public SliderInputPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

	public SliderInputPreference(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
	}

	@Override
	protected View onCreateDialogView() {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.slider_input, null);

		SharedPreferences prefs = SettingsActivity.getSettings((ContextWrapper) context);
		currentNumberOfPictures = prefs.getInt(context.getResources().getString(R.string.settings_amount_pictures), DEFAULT_PICTURES);
		
		numberOfPicturesText = (TextView) view.findViewById(R.id.curr_amount_photos);
		numberOfPicturesText.setText(String.valueOf(currentNumberOfPictures));
		slider = (SeekBar) view.findViewById(R.id.slider_photos);
		slider.setOnSeekBarChangeListener(this);
		slider.setMax(MAX_PIXTURES-MIN_PICTURES);
		slider.setProgress(currentNumberOfPictures-MIN_PICTURES);
		
		return view;
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
		if (restorePersistedValue) {
			SharedPreferences prefs = SettingsActivity.getSettings((ContextWrapper) context);
			currentNumberOfPictures = prefs.getInt(context.getResources().getString(R.string.settings_amount_pictures), DEFAULT_PICTURES);
		} else {
			currentNumberOfPictures = DEFAULT_PICTURES;
		}
	}

	@Override
	public void onProgressChanged(SeekBar seek, int value, boolean fromUser) {
		if (fromUser) {
			numberOfPicturesText.setText(String.valueOf(value + MIN_PICTURES));
			currentNumberOfPictures = value + MIN_PICTURES;
			callChangeListener(new Integer(value));
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch(which) {
		case DialogInterface.BUTTON_POSITIVE:
			if (shouldPersist())
				persistInt(currentNumberOfPictures);
			break;
		case DialogInterface.BUTTON_NEGATIVE:
			break;
		}
		
	}
	
	@Override
	public void onStartTrackingTouch(SeekBar seek) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seek) {
	}

}
