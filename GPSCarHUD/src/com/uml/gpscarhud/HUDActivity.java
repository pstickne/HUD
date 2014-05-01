package com.uml.gpscarhud;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.OrientationEventListener;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.google.android.gms.maps.model.LatLng;
import com.uml.gpscarhud.api.Maneuvers;
import com.uml.gpscarhud.api.Navigation;
import com.uml.gpscarhud.views.ArrowView;
import com.uml.gpscarhud.views.CompassView;
import com.uml.gpscarhud.views.InstructionView;
import com.uml.gpscarhud.views.SpeedometerView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class HUDActivity extends Activity implements LocationListener
{
	private String 	destination 	= null;
	private long	minTime			= 3000;
	private float 	minDistance		= 5.0f; 
	
	private InstructionView viewInstruction	= null;
	private ArrowView 		viewArrow		= null;
	private SpeedometerView viewSpeedometer	= null;
	private CompassView 	viewCompass		= null;
	
	private Switch		btnOrientationLock			= null;
	private boolean		orientationLock				= false;
	private int 		currentOrientation			= ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
	private int 		lockedOrientation			= ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
	
	private OrientationEventListener OEL			= null;
	private LocationManager locationManager			= null;
	
	private LatLng southCampus = new LatLng(42.642786, -71.335007);
	
	private Navigation directions = new Navigation();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_hud);
		
		viewInstruction = (InstructionView) findViewById(R.id.HUD_view_instruction);
		viewArrow 		= (ArrowView) findViewById(R.id.HUD_view_arrow);
		viewSpeedometer = (SpeedometerView) findViewById(R.id.HUD_view_speedometer);
		viewCompass 	= (CompassView) findViewById(R.id.HUD_view_compass);
		
		btnOrientationLock = (Switch) findViewById(R.id.HUD_button_orientation_lock);
		
		btnOrientationLock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				orientationLock = isChecked;
				
				if( orientationLock )
					lockedOrientation = currentOrientation;
			}
		});
		
		OEL = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
			@Override
			public void onOrientationChanged(int orientation) {
				if( orientation < 180 )	currentOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
				else					currentOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
				
				if( orientationLock )	setRequestedOrientation(lockedOrientation);
				else					setRequestedOrientation(currentOrientation);
			}
		};
		
		viewInstruction.setText("Somethingggg");
		viewInstruction.postInvalidate();
		
		viewArrow.setArrow(Maneuvers.getManeuver("turn-right"));
		viewArrow.postInvalidate();
		
		viewSpeedometer.postInvalidate();
		viewCompass.postInvalidate();
		
		directions.setDestination(southCampus);
		
		if (android.os.Build.VERSION.SDK_INT > 9) {
		      StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		      StrictMode.setThreadPolicy(policy);
		    }
	}

	@Override
	protected void onNewIntent(Intent intent) 
	{
		super.onNewIntent(intent);
		
		if( intent.getExtras() != null )
			destination = intent.getExtras().getString("destination");
		Log.i("onNewIntent", "Destination: " + ( destination == null ? "NULL" : destination));
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		
		if( OEL != null && OEL.canDetectOrientation() )
			OEL.enable();
		
		if( GPSenabled() )
		{
			locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
			locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			Log.i("HUD", locationManager.getProviders(true).toString());
			Log.i("HUD", "Now getting GPS updates.");
		}
		else
		{
			askToActivateGPS();
		}
	}
	
	private boolean GPSenabled()
	{
		final LocationManager mgr = (LocationManager)getSystemService(LOCATION_SERVICE);
	    if ( mgr == null ) return false;
	    final List<String> providers = mgr.getAllProviders();
	    if ( providers == null ) return false;
	    return providers.contains(LocationManager.GPS_PROVIDER);
	}
	
	private void askToActivateGPS() {
	    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
	           .setCancelable(false)
	           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	               public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
	                   startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
	               }
	           })
	           .setNegativeButton("No", new DialogInterface.OnClickListener() {
	               public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
	                    dialog.cancel();
	               }
	           });
	    final AlertDialog alert = builder.create();
	    alert.show();
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		
		if( OEL != null && OEL.canDetectOrientation() )
			OEL.disable();
		
		locationManager.removeUpdates(this);
	}
	
	private String getNextDirection(JSONObject direcs)
	{
		JSONObject firstStep = null;
		String firstDirection = "";
		try {
			 JSONObject routes = direcs.getJSONArray("routes").getJSONObject(0);
			 JSONObject leg = routes.getJSONArray("legs").getJSONObject(0);
			 JSONArray steps = leg.getJSONArray("steps");
			 firstStep = steps.getJSONObject(0);
		} catch (JSONException e) {
			
			e.printStackTrace();
		}
		
		if( firstStep == null )
			return firstDirection;
		
		try {
			firstDirection = firstStep.getString("html_instructions").replaceAll("<(.*?)*>", "");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return firstDirection;
	}

	//Re-usable JSONObject, creating a new variable everytime in the method would be slow.
	private JSONObject direcs = null;
	
	@Override
	public void onLocationChanged(Location location) {
		if( location != null )
		{
			Log.i("HUD", "GPS update event occuring.");
			directions.setSource(new LatLng(location.getLatitude(), location.getLongitude()));
			direcs = directions.getDirections();
			Log.i("HUD", getNextDirection(direcs));
		
			viewInstruction.setText(getNextDirection(direcs));
			viewInstruction.postInvalidate();
		}
	}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
	@Override
	public void onProviderEnabled(String provider) {
	}
	@Override
	public void onProviderDisabled(String provider) {
	}
}
