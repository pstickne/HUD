package com.uml.gpscarhud;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;

import android.annotation.SuppressLint;
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
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.uml.gpscarhud.api.Maneuvers;
import com.uml.gpscarhud.api.NavigationDirections;
import com.uml.gpscarhud.api.NavigationSevice;
import com.uml.gpscarhud.api.Unit;
import com.uml.gpscarhud.nav.NavLocation;
import com.uml.gpscarhud.views.ArrivalTimeView;
import com.uml.gpscarhud.views.ArrowView;
import com.uml.gpscarhud.views.DistanceView;
import com.uml.gpscarhud.views.InstructionView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
@SuppressLint("SimpleDateFormat")
public class HUDActivity extends Activity implements LocationListener
{
	private String 		destination 			= null;
	
	private Map<String, Boolean> avoidances		= null;
	private boolean		highways				= false;
	private boolean 	tolls					= false;
	
	private NavLocation	currentKnownLocation 	= null;
	private NavLocation lastKnownLocation		= null;
	private long		navComputeTime			= 0;
	private final int 	MINUTE 					= 60 * 1000;
	private long		minTime					= 2000;
	private float 		minDistance				= 0.0f; 
	
	private InstructionView		viewInstruction		= null;
	private ArrowView			viewArrow			= null;
	private DistanceView		viewDistance		= null;
	private ArrivalTimeView		viewTime			= null;
	
	private Switch		btnOrientationLock			= null;
	private boolean		orientationLock				= false;
	private boolean		foundFirstLocation			= false;
	private boolean 	ttsWarnedBeforeTurn			= false;
	private boolean		ttsWarnedAtTurn				= false;
	private int 		currentOrientation			= ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
	private int 		lockedOrientation			= ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
	
	private OrientationEventListener OEL			= null;
	private TextToSpeech TTS						= null;
	private LocationManager locationManager			= null;
	
	private NavigationSevice navService = new NavigationSevice();
	private NavigationDirections navDirections = new NavigationDirections();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_hud);
		
		viewTime = (ArrivalTimeView) findViewById(R.id.HUD_view_time);
		viewDistance = (DistanceView) findViewById(R.id.HUD_view_distance);
		viewArrow 		= (ArrowView) findViewById(R.id.HUD_view_arrow);
		viewInstruction = (InstructionView) findViewById(R.id.HUD_view_instruction);
		
		btnOrientationLock = (Switch) findViewById(R.id.HUD_button_orientation_lock);
		
		
		OEL = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
			@Override
			public void onOrientationChanged(int orientation) {
				if( orientation < 180 )	currentOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
				else					currentOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
				
				if( orientationLock )	setRequestedOrientation(lockedOrientation);
				else					setRequestedOrientation(currentOrientation);
			}
		};
		btnOrientationLock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				orientationLock = isChecked;
				
				if( orientationLock )
				{
					lockedOrientation = currentOrientation;
					btnOrientationLock.setVisibility(View.GONE);
				}
			}
		});
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		WindowManager.LayoutParams params = getWindow().getAttributes();
		params.screenBrightness = 1.0f;
		getWindow().setAttributes(params);
		
		avoidances = new HashMap<String, Boolean>();
		if( getIntent().getExtras() != null )
		{
			destination = getIntent().getExtras().getString("destination");
			highways = getIntent().getExtras().getBoolean("highways");
			tolls = getIntent().getExtras().getBoolean("tolls");
			
			avoidances.put("tolls", tolls);
			avoidances.put("highways", highways);
		}
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		
		if( OEL != null && OEL.canDetectOrientation() )
			OEL.enable();

		TTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
			@Override
			public void onInit(int status) {
				if( status == TextToSpeech.SUCCESS )
					TTS.setLanguage(Locale.getDefault());
			}
		});
		
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
		
		viewInstruction.setText("Waiting for GPS Signal...");
		viewTime.setText("");
		viewDistance.setText("");
		viewArrow.setArrow(null);
	}	
	
	@Override
	protected void onPause()
	{
		super.onPause();
		
		if( OEL != null && OEL.canDetectOrientation() )
			OEL.disable();
		
		if( TTS != null ) {
			TTS.stop();
			TTS.shutdown();
		}

		locationManager.removeUpdates(this);
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.i("HUDActivity", "onLocationChanged()");
		
		if( location != null ) 
		{
			NavLocation myNavLoc = new NavLocation(location);
			
			if( isBetterLocation(myNavLoc, currentKnownLocation) )
			{
				currentKnownLocation = myNavLoc;

				if( foundFirstLocation == false )
				{
					foundFirstLocation = true;
					
					try {
						navService.setSource(currentKnownLocation);
						navService.setDestination(destination); 
						navService.setAvoidances(avoidances);
						navDirections.loadRoute(navService.getDirections());
						navDirections.startNavigation();
						
						navComputeTime = System.currentTimeMillis();
						
						Log.i("HUDActivity", navDirections.getJSON().toString(2));

						viewInstruction.setText(navDirections.getInstruction());
						TTS.speak(navDirections.getInstruction(), TextToSpeech.QUEUE_ADD, null);
						
					} catch (JSONException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				else
				{
					
					///////////////////////////////////////////////////////////////////////////////////////////////
					//	Base Case - Arrived at destination
					//				No further computation needed  
					if( navDirections.state == NavigationDirections.STATE_IN_ENDING_ZONE )
					{
						viewInstruction.setText("You have arrived at " + navDirections.getLeg().getEndAddress());
						viewArrow.setArrow(null);
						viewTime.setText("");
						viewDistance.setText("");
						return;
					}
					if( navDirections.getLeg().getSteps().size() - 1 == navDirections.getCurrentStep() &&
						navDirections.getLeg().getEndLocation().distanceTo(currentKnownLocation) < 40 )
					{
						navDirections.state = NavigationDirections.STATE_IN_ENDING_ZONE;
						TTS.speak("Arriving at destination", TextToSpeech.QUEUE_ADD, null);
						return;
					}
					///////////////////////////////////////////////////////////////////////////////////////////////
					
					
					
					
					

					///////////////////////////////////////////////////////////////////////////////////////////////
					// Leaving Starting location
					if( navDirections.state == NavigationDirections.STATE_IN_STARTING_ZONE )
					{
						if( navDirections.getLeg().getStartLocation().distanceTo(currentKnownLocation) > 40 )
						{
							navDirections.state = NavigationDirections.STATE_ON_ROUTE;
							navDirections.nextStep();
						}
						lastKnownLocation = currentKnownLocation;
						return;
					}
					///////////////////////////////////////////////////////////////////////////////////////////////
					
					
					
					
					

					///////////////////////////////////////////////////////////////////////////////////////////////
					// Update all the views' displays
					updateDisplay();
					///////////////////////////////////////////////////////////////////////////////////////////////

					
					
					

					
					///////////////////////////////////////////////////////////////////////////////////////////////
					// They are in a step zone
					if( navDirections.state == NavigationDirections.STATE_IN_STEP_ZONE )
					{
						if( navDirections.getStartLocation().distanceTo(currentKnownLocation) > 60 )
						{
							navDirections.state = NavigationDirections.STATE_ON_ROUTE;
							navDirections.nextStep();
							updateDisplay();
							TTS.speak(	"In " + 
										navDirections.getStartLocation().distanceTo(currentKnownLocation, Unit.Converter.TO_MILES) +
										" miles, " + navDirections.getInstruction(), TextToSpeech.QUEUE_ADD, null);
						}
					}
					///////////////////////////////////////////////////////////////////////////////////////////////
					
					
					
					

					
					///////////////////////////////////////////////////////////////////////////////////////////////
					// They are on route
					else if( navDirections.state == NavigationDirections.STATE_ON_ROUTE )
					{
						// Check to see if they are on route
						if( !navDirections.isOnRoute(currentKnownLocation, lastKnownLocation) )
						{
							foundFirstLocation = false;
							ttsWarnedAtTurn = false;
							ttsWarnedBeforeTurn = false;
							TTS.speak("Recalculating", TextToSpeech.QUEUE_ADD, null);
							return;
						}
						
						
						// Warn the driver of upcoming instructions
						if( navDirections.getStartLocation().distanceTo(currentKnownLocation) <= 50 &&
							!ttsWarnedAtTurn )
						{
							ttsWarnedAtTurn = true;
							navDirections.state = NavigationDirections.STATE_IN_STEP_ZONE;
							TTS.speak(navDirections.getInstruction(), TextToSpeech.QUEUE_ADD, null);
						}
						else if( navDirections.getStartLocation().distanceTo(currentKnownLocation) <= 325 &&
								 !ttsWarnedBeforeTurn )
						{
							ttsWarnedBeforeTurn = true;
							navDirections.state = NavigationDirections.STATE_ON_ROUTE;
							TTS.speak(	"In " + 
										navDirections.getStartLocation().distanceTo(currentKnownLocation, Unit.Converter.TO_MILES) +
										" miles, " + navDirections.getInstruction(), TextToSpeech.QUEUE_ADD, null);
						}
					}
					///////////////////////////////////////////////////////////////////////////////////////////////
					
					
					
					
					
					
//					Log.i("HUDActivity", "Nav State: " + navDirections.state);
//					Log.i("HUDActivity", "Current Loc: " + currentKnownLocation.toString());
//					Log.i("HUDActivity", "Last Loc: " + lastKnownLocation.toString());
//					Log.i("HUDActivity", "End Loc: " + navDirections.getEndLocation().toString());
//					Log.i("HUDActivity", "Start Log: " + navDirections.getLeg().getStartLocation().toString());
//					Log.i("HUDActivity", "Distance from start: " + navDirections.getLeg().getStartLocation().distanceTo(currentKnownLocation));
//					Log.i("HUDActivity", "Distance to end: " + navDirections.getEndLocation().distanceTo(currentKnownLocation));

					lastKnownLocation = currentKnownLocation;
				}
			}
		}
	}
	
	private void updateDisplay()
	{
		viewInstruction.setText(navDirections.getInstruction());
		viewArrow.setArrow(Maneuvers.getManeuver(navDirections.getManeuver()));
		viewTime.setText( "Arrival Time\n" + 
				(new SimpleDateFormat("hh:mm a").format(
						new Date(navComputeTime + 
								(navDirections.getLeg().getDuration().val() * 1000)))));
		
		// Just converts distance to feet if( distance < .1 mi )
		if( navDirections.getStartLocation().distanceTo(currentKnownLocation) <= 160 )
			viewDistance.setText("Checkpoint In\n" + navDirections.getStartLocation().distanceTo(currentKnownLocation, Unit.Converter.TO_FEET) + " ft" );
		else
			viewDistance.setText("Checkpoint In\n" + navDirections.getStartLocation().distanceTo(currentKnownLocation, Unit.Converter.TO_MILES) + " mi");
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
