package com.uml.gpscarhud;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.OrientationEventListener;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.google.android.gms.maps.model.LatLng;
import com.uml.gpscarhud.api.Maneuvers;
import com.uml.gpscarhud.api.NavigationSevice;
import com.uml.gpscarhud.views.ArrowView;
import com.uml.gpscarhud.views.CompassView;
//import com.uml.gpscarhud.views.DistanceView;
import com.uml.gpscarhud.views.InstructionView;
import com.uml.gpscarhud.views.SpeedometerView;
//import com.uml.gpscarhud.views.TimeToDestinationView;

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
	
	private InstructionView       viewInstruction	= null;
	private ArrowView 		      viewArrow		= null;
//	private DistanceView          viewDistance	= null;
//	private TimeToDestinationView viewTime	= null;
	
	private Switch		btnOrientationLock			= null;
	private boolean		orientationLock				= false;
	private int 		currentOrientation			= ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
	private int 		lockedOrientation			= ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
	
	private OrientationEventListener OEL			= null;
	private LocationManager locationManager			= null;
	
	private LatLng southCampus = new LatLng(42.642786, -71.335007);
	
	private NavigationSevice directions = new NavigationSevice();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_hud);
		
//		viewTime = (TimeToDestinationView) findViewById(R.id.HUD_view_time);
//		viewDistance = (DistanceView) findViewById(R.id.HUD_view_distance);
		viewArrow 		= (ArrowView) findViewById(R.id.HUD_view_arrow);
		viewInstruction = (InstructionView) findViewById(R.id.HUD_view_instruction);
		
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
		
		if( getIntent().getExtras() != null )
			destination = getIntent().getExtras().getString("destination");
		Log.i("onCreate", "Destination: " + ( destination == null ? "NULL" : destination));
		
		viewInstruction.setText("Somethingggg");
		viewInstruction.postInvalidate();
		
		viewArrow.setArrow(Maneuvers.getManeuver("turn-right"));
		viewArrow.postInvalidate();
		
		directions.setDestination(southCampus);
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
		LocationManager mgr = (LocationManager)getSystemService(LOCATION_SERVICE);
	    if ( mgr == null ) return false;
	    List<String> providers = mgr.getAllProviders();
	    if ( providers == null ) return false;
	    return providers.contains(LocationManager.GPS_PROVIDER);
	}
	
	private void askToActivateGPS() {
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
	           .setCancelable(false)
	           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
					}
				})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
	    builder.create().show();
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		
		if( OEL != null && OEL.canDetectOrientation() )
			OEL.disable();
		
		locationManager.removeUpdates(this);
	}
	
	private int stepCount = 0;
	
	private String getNextDirection(JSONObject direcs, String currentStreetName)
	{
		JSONObject firstStep = null;
		String firstDirection = "";
		try {
			 JSONObject routes = direcs.getJSONArray("routes").getJSONObject(0);
			 JSONObject leg = routes.getJSONArray("legs").getJSONObject(0);
			 JSONArray steps = leg.getJSONArray("steps");
			 firstStep = steps.getJSONObject(stepCount);
		} catch (JSONException e) {
			
			e.printStackTrace();
		}
		
		if( firstStep == null )
			return firstDirection;
		
		try {
			firstDirection = firstStep.getString("html_instructions").replaceAll("<(.*?)*>", "");
			if( firstDirection.contains(currentStreetName) )
			{
				stepCount++;
				firstDirection = getNextDirection(direcs, currentStreetName);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return firstDirection;
	}

	//Re-usable JSONObject, creating a new variable everytime in the method would be slow.
	private JSONObject direcs = null;
	String currentStreet;
	@Override
	public void onLocationChanged(Location location) {
		if( location != null )
		{
			Log.i("HUD", "GPS update event occuring.");
			directions.setSource(new LatLng(location.getLatitude(), location.getLongitude()));
			try {
				direcs = directions.getDirections();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			currentStreet = getCurrentStreetName(location);
			
			Log.i("HUD", getNextDirection(direcs, currentStreet));
		
			viewInstruction.setText(getNextDirection(direcs, currentStreet));
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

	private String getCurrentStreetName(Location location)
	{
		String streetName = "";

		Geocoder gcd = new Geocoder(this, Locale.getDefault());
		List<Address> addresses = null;
		try {
			addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if ( addresses != null && addresses.size() > 0 ) {
			streetName = addresses.get(0).getFeatureName();
		}

		return streetName;
	}
}
