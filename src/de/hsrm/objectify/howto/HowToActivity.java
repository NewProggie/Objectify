package de.hsrm.objectify.howto;

import android.os.Bundle;
import de.hsrm.objectify.R;
import de.hsrm.objectify.actionbarcompat.ActionBarActivity;

public class HowToActivity extends ActionBarActivity {

	private static final String TAG = "HowToActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(getString(R.string.howto));
		setContentView(R.layout.howto);
	}
}
