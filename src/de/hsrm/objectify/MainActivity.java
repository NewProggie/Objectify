package de.hsrm.objectify;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import de.hsrm.objectify.camera.CameraActivity;
import de.hsrm.objectify.gallery.GalleryActivity;
import de.hsrm.objectify.ui.BaseActivity;

public class MainActivity extends BaseActivity {
	
	private static final String TAG = "MainActivity";
	private Context context;
	private Button galleryButton, howtoButton, exportButton, shareButton, cameraButton;
	private ImageButton actionbarCamButton;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);
        context = this;
        
        galleryButton = (Button) findViewById(R.id.dashboard_gallery_button);
        howtoButton = (Button) findViewById(R.id.dashboard_howto_button);
        exportButton = (Button) findViewById(R.id.dashboard_export_button);
        shareButton = (Button) findViewById(R.id.dashboard_share_button);
        cameraButton = (Button) findViewById(R.id.dashboard_camera_button);
//        actionbarCamButton = (ImageButton) findViewById(R.id.actionbar_cam_button);
//        actionbarCamButton.setOnClickListener(onClickListener());
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
    
    private OnClickListener onClickListener() {
    	OnClickListener listener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent camera = new Intent(context, CameraActivity.class);
				startActivity(camera);
			}
		};
		return listener;
    }
}