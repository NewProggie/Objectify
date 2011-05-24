package de.hsrm.objectify.howto;

import android.os.Bundle;
import android.webkit.WebView;
import de.hsrm.objectify.R;
import de.hsrm.objectify.ui.BaseActivity;

public class HowToActivity extends BaseActivity {

	private static final String TAG = "HowToActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		WebView webView = new WebView(this);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.loadUrl("http://www.innovations-report.de/html/berichte/maschinenbau/bericht-56413.html");
		setContentView(webView);
		setupActionBar(getString(R.string.howto), 0);
	}
}
