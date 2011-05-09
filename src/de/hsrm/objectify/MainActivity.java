package de.hsrm.objectify;

import de.hsrm.objectify.camera.CameraActivity;
import de.hsrm.objectify.gallery.GalleryActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private static final String TAG = "MainActivity";
	private Context context;
	private Button galleryButton, howtoButton, exportButton, shareButton, cameraButton;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
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
    		Intent camera = new Intent(context, CameraActivity.class);
    		startActivity(camera);
    		break;
    	}
    }
}