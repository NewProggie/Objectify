package de.hsrm.objectify;

import android.os.Bundle;
import android.widget.TextView;
import de.hsrm.objectify.ui.BaseActivity;

public class AboutActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TextView aboutText = new TextView(this);
		aboutText.setText("Copyright (c) 2011 Kai Wolf.\n All rights reserved");
		setContentView(aboutText);
		
		setupActionBar(getString(R.string.about), 0);
	}
	
}
