package com.uml.gpscarhud;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.OrientationEventListener;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.uml.gpscarhud.api.Maneuvers;
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
			
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, this);
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
