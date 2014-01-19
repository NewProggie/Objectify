package de.hsrm.objectify.howto;

import android.app.Activity;
import android.os.Bundle;
import de.hsrm.objectify.R;

public class HowToActivity extends Activity {

	private static final String TAG = "HowToActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(getString(R.string.howto));
		setContentView(R.layout.howto);
	}
}
