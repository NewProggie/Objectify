package de.hsrm.objectify.ui;

import de.hsrm.objectify.MainActivity;
import de.hsrm.objectify.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * A base activity that implements common functionality across app activities such as the actionbar.
 * This class mustn't be used directly, instead activities should inherit from 
 * @author kwolf001
 *
 */
public abstract class BaseActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_empty);
	}
	
	/**
	 * Sets up the actionbar with the given title and the color. If the title is
	 * null, then the app name will be shown instead of the title. Otherwise, a
	 * home button and the given title are visible. If the color is null, then
	 * the default color is visible.
	 * 
	 * @param title title of actionbar
	 * @param color color of title
	 */
	private void setupActionBar(CharSequence title, int color) {
		ViewGroup actionBar = (ViewGroup) findViewById(R.id.actionbar);
		if (actionBar == null) {
			return;
		}
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.FILL_PARENT);
		View.OnClickListener homeClickListener = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				backToMain();
			}
		};
		
		if (title != null) {
			
		}
	}
	
	/**
	 * Starting home activity, returning to {@link de.hsrm.objectify.MainActivity }.
	 */
	private void backToMain() {
		Intent intent = new Intent(this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}
}
