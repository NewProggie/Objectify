package de.hsrm.objectify;

import java.util.ArrayList;

import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import de.hsrm.objectify.camera.CameraFinder;
import de.hsrm.objectify.ui.SliderInputPreference;

/**
 * This class takes care of the user preferences such as setting front camera
 * resolutions and changing the default saving directory.
 * 
 * @author kwolf001
 * 
 */
public class SettingsActivity extends PreferenceActivity {

	private ListPreference cameraResolutions;
	private SliderInputPreference amountPictures;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		
		cameraResolutions = (ListPreference) findPreference(getString(R.string.settings_camera_resolutions));
		amountPictures = (SliderInputPreference) findPreference(getString(R.string.settings_amount_pictures));
		
		cameraResolutions.setEntries(getAvailableResolutions());
		cameraResolutions.setEntryValues(getAvailableResolutions());

	}
	
	/**
	 * Returns the persisted shared preferences
	 * @param context context wrapper
	 * @return shared preferences
	 */
	public static final SharedPreferences getSettings(final ContextWrapper context) {
		String name = context.getPackageName() + "_preferences";
		return context.getSharedPreferences(name, MODE_PRIVATE);
	}
	
	/**
	 * Reads available resolutions from the camera and returns an
	 * {@link CharSequence} array with the combined resolutions, e.g. 800x600
	 * 
	 * @return array with available resolutions stored as [width]x[height]
	 */
	private CharSequence[] getAvailableResolutions() {
		ArrayList<String> tempResolutions = new ArrayList<String>();
		CharSequence[] resolutions;
		Camera camera = CameraFinder.INSTANCE.open();
		if (camera != null) {
			Camera.Parameters params = camera.getParameters();
			for (Size size : params.getSupportedPictureSizes()) {
				tempResolutions.add(String.valueOf(size.width) + "x" + String.valueOf(size.height));
			}
			camera.release();
			resolutions = new CharSequence[tempResolutions.size()];
			tempResolutions.toArray(resolutions);
		} else {
			resolutions = new CharSequence[0];
		}
		return resolutions;
	}
	
}
