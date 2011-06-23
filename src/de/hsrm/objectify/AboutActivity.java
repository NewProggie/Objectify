package de.hsrm.objectify;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import de.hsrm.objectify.ui.BaseActivity;

public class AboutActivity extends BaseActivity {

	private TextView aboutText;
	private Button sendFeedback;
	private Context context;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		setupActionBar(getString(R.string.about), 0);
		this.context = this;
		
		aboutText = (TextView) findViewById(R.id.about_text);
		sendFeedback = (Button) findViewById(R.id.feedback_button);
		
		aboutText.setText("Copyright (c) 2011 Kai Wolf.\n All rights reserved");
		sendFeedback.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				Intent writeFeedback = new Intent(android.content.Intent.ACTION_SEND);
				writeFeedback.setType("plain/text");
				PackageManager pm = getPackageManager();
				// Nachschauen, on Mail auf dem Device eingerichtet ist
				List<ResolveInfo> list = pm.queryIntentActivities(writeFeedback, PackageManager.MATCH_DEFAULT_ONLY);
				if (list.size() > 0) {
					String developerMailAdress = getString(R.string.developer_mail_address);
					writeFeedback.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { developerMailAdress });
					writeFeedback.putExtra(android.content.Intent.EXTRA_SUBJECT, "Feedback " + getString(R.string.app_name));
					startActivity(writeFeedback);
					((Activity) context).finish();
				} else {
					Toast.makeText(view.getContext(), getString(R.string.no_mailclient_found), Toast.LENGTH_LONG).show();
				}
			}
		});
		
	}
	
}