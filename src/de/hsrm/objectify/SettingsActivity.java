package de.hsrm.objectify;

import java.util.ArrayList;

import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.util.Log;
import de.hsrm.objectify.camera.CameraFinder;

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		
		ListPreference cameraResolutions = (ListPreference) findPreference(getString(R.string.settings_camera_resolutions));
		EditTextPreference savingDir = (EditTextPreference) findPreference(getString(R.string.settings_saving_directory));
		
		CharSequence[] cs;
		CharSequence[] cs2;
		ArrayList<Size> sizes = new ArrayList<Camera.Size>();
		Camera camera = CameraFinder.INSTANCE.open();
		Camera.Parameters params = camera.getParameters();
		for (Size size : params.getSupportedPictureSizes()) {
			sizes.add(size);
		}
		cs = new CharSequence[sizes.size()];
		for (int i=0; i< sizes.size(); i++) {
			Size s = sizes.get(i);
			CharSequence c = String.valueOf(s.width) + "x" + String.valueOf(s.height);
			cs[i] = c;
		}

		camera.release();

		cameraResolutions.setEntries(cs);
		cameraResolutions.setEntryValues(cs);
	}
	
	public static final SharedPreferences getSettings(final ContextWrapper context) {
		String name = context.getPackageName() + "_preferences";
		return context.getSharedPreferences(name, MODE_PRIVATE);
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		// TODO wird bisher nicht aufgerufen
		Log.d("SETTING", key);
	}

}
