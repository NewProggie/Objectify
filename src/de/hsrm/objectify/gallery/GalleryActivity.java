package de.hsrm.objectify.gallery;

import java.io.File;
import java.io.FilenameFilter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Gallery;
import android.widget.ImageView;
import de.hsrm.objectify.R;
import de.hsrm.objectify.SettingsActivity;

public class GalleryActivity extends Activity {

	private static final String TAG = "GalleryActivity";
	private Gallery gallery;
	private GalleryAdapter adapter;
	private ImageView currentImg;
	private Context context;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery);
		context = this;
		
		gallery = (Gallery) findViewById(R.id.picture_gallery);
		currentImg = (ImageView) findViewById(R.id.current_img_gallery);
		
		SharedPreferences prefs = SettingsActivity.getSettings(this);
		String path = prefs.getString("path", null);
		if (path == null) {
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				File savingDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/.objectify/");
				if (savingDir.mkdirs()) {
					Editor editor = prefs.edit();
					editor.putString("path", Environment.getExternalStorageDirectory().getAbsolutePath() + "/.objectify/");
					editor.commit();
					path = savingDir.getAbsolutePath() + "/";
					File[] imageList = new File(path).listFiles(filterPictures());
					if (imageList.length == 0) {
						showMessageAndExit(getString(R.string.gallery), getString(R.string.no_objects_saved));
					} else {
						adapter = new GalleryAdapter(this, imageList);
						gallery.setAdapter(adapter);
					}
				} else {
					if (!savingDir.exists()) {
						showMessageAndExit(getString(R.string.error), getString(R.string.couldnt_create_directory));
					}	
				}
			} else {
				showMessageAndExit(getString(R.string.error), getString(R.string.external_not_mounted));
			}
		} else {
			File[] imageList = new File(path).listFiles(filterPictures());
			if (imageList.length == 0) {
				showMessageAndExit(getString(R.string.gallery), getString(R.string.no_objects_saved));
			} else {
				adapter = new GalleryAdapter(this, imageList);
				gallery.setAdapter(adapter);
			}
		}
	}
	
	/**
	 * Filter all images in given directory
	 * @return new FilenameFilter
	 */
	private FilenameFilter filterPictures() {
		FilenameFilter filter = new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String filename) {
				return (filename.endsWith(".jpg") || filename.endsWith(".png"));
			}
		};
		return filter;
	}
	
	/**
	 * Shows specific Dialog to user and finishes current Activity 
	 * @param title title of dialog
	 * @param msg message of dialog
	 */
	private void showMessageAndExit(String title, String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title).setMessage(msg).setCancelable(false).setPositiveButton(getString(R.string.submit), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				((Activity) context).finish();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
}
