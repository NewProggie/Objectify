package de.hsrm.objectify;

import de.hsrm.objectify.camera.CameraActivity;
import de.hsrm.objectify.gallery.GalleryActivity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class ObjectifyWidgetProvider extends AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		final int N = appWidgetIds.length;
		
		for (int i=0; i<N; i++) {
			int appWidgetId = appWidgetIds[i];
			
			Intent startApp = new Intent(context, MainActivity.class);
			PendingIntent pendingAppIntent = PendingIntent.getActivity(context, 0, startApp, 0);
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.objectify_appwidget);
			views.setOnClickPendingIntent(R.id.widget_app_launcher, pendingAppIntent);
			
			Intent startCam = new Intent(context, CameraActivity.class);
			PendingIntent pendingCamIntent = PendingIntent.getActivity(context, 0, startCam, 0);
			views.setOnClickPendingIntent(R.id.widget_cam_launcher, pendingCamIntent);
			
			Intent startGallery = new Intent(context, GalleryActivity.class);
			PendingIntent pendingGalleryIntent = PendingIntent.getActivity(context, 0, startGallery, 0);
			views.setOnClickPendingIntent(R.id.widget_gallery_launcher, pendingGalleryIntent);
			
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}
}
