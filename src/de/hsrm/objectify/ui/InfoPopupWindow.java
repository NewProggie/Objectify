package de.hsrm.objectify.ui;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import de.hsrm.objectify.R;

/**
 * This class displays wraps most of the work of {@link PopupWindow} and
 * displays some information as an overlay to the current activity.
 * 
 * @author kwolf001
 * 
 */
public class InfoPopupWindow {

	protected final View anchor;
	private final PopupWindow window;
	private View root;
	private Drawable background = null;
	private final WindowManager windowManager;
	
	/**
	 * Creates a popup window
	 * @param anchor the view that InfoPopupWindow will be displaying from
	 */
	public InfoPopupWindow(View anchor) {
		this.anchor = anchor;
		this.window = new PopupWindow(anchor.getContext());
		
		// when user touches outside the window, the popup will disappear
		this.window.setTouchInterceptor(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
					InfoPopupWindow.this.window.dismiss();
					return true;
				}
				return false;
			}
		});
		
		this.windowManager = (WindowManager) this.anchor.getContext().getSystemService(Context.WINDOW_SERVICE);
		onCreate();
	}
	
	protected void onCreate() {
		LayoutInflater inflater = (LayoutInflater) this.anchor.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ViewGroup root = (ViewGroup) inflater.inflate(R.layout.info_display, null);
		
		this.setContentView(root);
	}
	
	protected void onShow() {
		// in case there is some stuff to do right before displaying
	}
	
	private void preShow() {
		if (this.root == null) {
			throw new IllegalStateException("setContentView was called without a view to display");
		}
		onShow();
		
		if (this.background == null) {
			this.window.setBackgroundDrawable(new BitmapDrawable());
		} else {
			this.window.setBackgroundDrawable(this.background);
		}
		
		this.window.setWidth(WindowManager.LayoutParams.FILL_PARENT);
		this.window.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
		this.window.setTouchable(true);
		this.window.setFocusable(true);
		this.window.setOutsideTouchable(true);
		this.window.setContentView(this.root);
	}
	
	public void setBackgroundDrawable(Drawable background) {
		this.background = background;
	}
	
	public void setContentView(View root) {
		this.root = root;
		this.window.setContentView(root);
	}
	
	public void setContentView(int layoutResID) {
		LayoutInflater inflater = (LayoutInflater) this.anchor.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.setContentView(inflater.inflate(layoutResID, null));
	}
	
	public void setOnDismissListener(PopupWindow.OnDismissListener listener) {
		this.window.setOnDismissListener(listener);
	}
	
	public void showInfoPopupWindow() {
		this.showInfoPopupWindow(0,0);
	}
	
	public void showInfoPopupWindow(int xoff, int yoff) {
		this.preShow();
		this.window.setAnimationStyle(R.style.Animations_PopupWindow);
		this.window.showAsDropDown(this.anchor, xoff, yoff);
	}
	
	public void dismiss() {
		this.window.dismiss();
	}
}
