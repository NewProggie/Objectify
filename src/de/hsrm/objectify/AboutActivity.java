package de.hsrm.objectify;

import android.os.Bundle;
import android.widget.TextView;
import de.hsrm.objectify.ui.BaseActivity;

public class AboutActivity extends BaseActivity {

	TextView aboutText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		
		aboutText = (TextView) findViewById(R.id.about_text);
		aboutText.setText("Copyright (c) 2011 Kai Wolf.\n All rights reserved");
		
		setupActionBar(getString(R.string.about), 0);
	}
	
}
