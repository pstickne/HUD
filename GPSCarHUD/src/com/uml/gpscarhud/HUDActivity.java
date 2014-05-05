package com.uml.gpscarhud;

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
import android.provider.Settings;
import android.util.Log;
import android.view.OrientationEventListener;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.uml.gpscarhud.api.NavigationDirections;
import com.uml.gpscarhud.api.NavigationSevice;
import com.uml.gpscarhud.nav.NavLocation;
import com.uml.gpscarhud.views.ArrowView;
import com.uml.gpscarhud.views.DistanceView;
import com.uml.gpscarhud.views.InstructionView;
import com.uml.gpscarhud.views.TimeToDestinationView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class HUDActivity extends Activity implements LocationListener
{
	private String 		destination 		= null;
	private long		minTime				= 3000;
	private float 		minDistance			= 5.0f; 
	private NavLocation	lastKnownLocation 	= null;
	private final int 	MINUTE 				= 60 * 1000;
	
	private InstructionView       viewInstruction	= null;
	private ArrowView 		      viewArrow			= null;
	private DistanceView          viewDistance		= null;
	private TimeToDestinationView viewTime			= null;
	
	private Switch		btnOrientationLock			= null;
	private boolean		orientationLock				= false;
	private boolean		foundFirstLocation			= false;
	private int 		currentOrientation			= ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
	private int 		lockedOrientation			= ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
	
	private OrientationEventListener OEL			= null;
	private LocationManager locationManager			= null;
	
	private NavigationSevice navService = new NavigationSevice();
	private NavigationDirections navDirections = new NavigationDirections();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_hud);
		
		viewTime = (TimeToDestinationView) findViewById(R.id.HUD_view_time);
		viewDistance = (DistanceView) findViewById(R.id.HUD_view_distance);
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
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		
		if( OEL != null && OEL.canDetectOrientation() )
			OEL.enable();

		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		
		if( locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) )
		{
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, this);
		}
		else
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder
				.setMessage("GPS service is disabled, do you want to enable it?")
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
						finish();
					}
				});
			builder.create().show();
		}
	}	
	
	@Override
	protected void onPause()
	{
		super.onPause();
		
		if( OEL != null && OEL.canDetectOrientation() )
			OEL.disable();
		
		locationManager.removeUpdates(this);
	}

	@Override
	public void onLocationChanged(Location location) {
		if( location != null ) 
		{
			NavLocation myNavLoc = new NavLocation(location);
			
			if( isBetterLocation(myNavLoc, lastKnownLocation) )
				lastKnownLocation = myNavLoc;
		}
	}
	@Override
	public void onProviderDisabled(String provider) {
	}
	@Override
	public void onProviderEnabled(String provider) {
	}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	public NavLocation getLastKnownLocation() {
		return lastKnownLocation;
	}
	
	public boolean isBetterLocation(NavLocation location, NavLocation currentBestLocation)
	{
		if( currentBestLocation == null )
			return true;
		
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > MINUTE;
		boolean isSignificantlyOlder = timeDelta < -MINUTE;
		boolean isNewer = timeDelta > 0;
		
		if( isSignificantlyNewer )
			return true;
		if( isSignificantlyOlder )
			return false;
		
		int accuracyDelta = (int)(location.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;
		boolean isFromSameProvider = (( location.getProvider() == null ) ?
											(currentBestLocation.getProvider() == null) :
											location.getProvider().equals(currentBestLocation.getProvider()) );
		
		if( isMoreAccurate ) 
			return true;
		if( isNewer && !isLessAccurate ) 
			return true;
		if( isNewer && !isSignificantlyLessAccurate && isFromSameProvider )
			return true;
		return false;
	}
}
