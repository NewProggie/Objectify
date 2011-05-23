package de.hsrm.objectify;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import de.hsrm.objectify.camera.CameraPreview;
import de.hsrm.objectify.gallery.GalleryActivity;
import de.hsrm.objectify.ui.BaseActivity;

/**
 * Front door {@link Activity} that displays Dashboardlayout with different
 * features of this app. Inherits an action bar from {@link BaseActivity} and
 * initializes it.
 * 
 * @author kwolf001
 * 
 */
public class MainActivity extends BaseActivity {
	
	private Context context;
	
	@SuppressWarnings("unused")
	private Button galleryButton, howtoButton, exportButton, shareButton, cameraButton;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);
        setupActionBar(null, 0);
//        addNewActionButton(R.drawable.ic_title_camera, R.string.camera, new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				Intent camera = new Intent(v.getContext(), CameraActivity.class);
//				startActivity(camera);
//			}
//		});
        context = this;
        
        galleryButton = (Button) findViewById(R.id.dashboard_gallery_button);
        howtoButton = (Button) findViewById(R.id.dashboard_howto_button);
        exportButton = (Button) findViewById(R.id.dashboard_export_button);
        shareButton = (Button) findViewById(R.id.dashboard_share_button);
        cameraButton = (Button) findViewById(R.id.dashboard_camera_button);
    }
    
    public void buttonClick(View target) {
    	switch (target.getId()) {
    	case R.id.dashboard_gallery_button:
    		Intent gallery = new Intent(context, GalleryActivity.class);
    		startActivity(gallery);
    		break;
    	case R.id.dashboard_howto_button:
    		Toast.makeText(context, "howto", Toast.LENGTH_SHORT).show();
    		break;
    	case R.id.dashboard_export_button:
    		Toast.makeText(context, "export", Toast.LENGTH_SHORT).show();
    		break;
    	case R.id.dashboard_share_button:
    		Toast.makeText(context, "share", Toast.LENGTH_SHORT).show();
    		break;
    	case R.id.dashboard_camera_button:
    		Intent camera = new Intent(context, CameraPreview.class);
    		startActivity(camera);
    		break;
    	}
    }
   
}