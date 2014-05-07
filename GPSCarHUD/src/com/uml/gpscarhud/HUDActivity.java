package com.uml.gpscarhud;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TooManyListenersException;

import org.json.JSONException;

import android.annotation.SuppressLint;
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
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.uml.gpscarhud.api.Maneuvers;
import com.uml.gpscarhud.api.NavigationDirections;
import com.uml.gpscarhud.api.NavigationSevice;
import com.uml.gpscarhud.api.UnitConvert;
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
	private long		navComputeTime			= 0;
	private final int 	MINUTE 					= 60 * 1000;
	private long		minTime					= 5000;
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
	private boolean 	ttsWarnedFirstStep			= false;
	private boolean     arrivalTimeHasBeenSet       = false;
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
					
					if( OEL != null )
						OEL.disable();
				}
			}
		});
		
		avoidances = new HashMap<String, Boolean>();
		if( getIntent().getExtras() != null )
		{
			destination = getIntent().getExtras().getString("destination");
			highways = getIntent().getExtras().getBoolean("highways");
			tolls = getIntent().getExtras().getBoolean("tolls");
			
			avoidances.put("tolls", tolls);
			avoidances.put("highways", highways);
		}
		Log.i("onCreate", "Destination: " + ( destination == null ? "NULL" : destination));
		Log.i("onCreate", "Avoid Highways: " + ( highways ? "TRUE" : "FALSE"));
		Log.i("onCreate", "Avoid Tolls: " + ( tolls ? "TRUE" : "FALSE"));
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
					
					Geocoder geo = new Geocoder(this);
					List<Address> addresses = null;
					Address address = null;

					try {
						addresses = geo.getFromLocationName(destination, 1);
						if( addresses.size() == 0 )
						{
							AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
							dialogBuilder
								.setTitle("Error")
								.setMessage("Address could not be validated.")
								.setCancelable(false)
								.setPositiveButton("OK", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
										finish();
									}
								}).create().show();
							return;
						}
						
						address = addresses.get(0);
						navService.setSource(currentKnownLocation);
						navService.setDestination(new NavLocation(address.getLatitude(), address.getLongitude())); 
						navService.setAvoidances(avoidances);
						navDirections.loadRoute(navService.getDirections());
						navDirections.startNavigation();
						
						navComputeTime = System.currentTimeMillis();
						
						Log.i("HUDActivity", navDirections.getJSON().toString(2));

						viewInstruction.setText("Calculating route...");
						
					} catch (IOException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				else
				{
					// TODO: Still need to fix arrival time
					viewInstruction.setText(navDirections.getInstruction());
					viewArrow.setArrow(Maneuvers.getManeuver(navDirections.getManeuver()));
					
					//Arrival time should only be set once for each HTTP call, since leg's duration value will be the same.
					if( !arrivalTimeHasBeenSet )
					{
						viewTime.setText( "Arrival Time\n" + 
								(new SimpleDateFormat("hh:mm a").format(
										new Date(navComputeTime + 
												(navDirections.getLeg().getDuration().val() * 1000)))));
						arrivalTimeHasBeenSet = true;
					}
					
					// Just converts distance to feet if( distance < .1 mi )
					if( navDirections.getEndLocation().distanceTo(currentKnownLocation) <= 160 )
						viewDistance.setText( navDirections.getEndLocation().distanceTo(currentKnownLocation, UnitConvert.FEET) + " ft" );
					else
						viewDistance.setText(navDirections.getEndLocation().distanceTo(currentKnownLocation, UnitConvert.MILES) + " mi");

					
					
					// Placeholder for recalculating the route
					if( !navDirections.isOnRoute(currentKnownLocation) )
					{
						foundFirstLocation = false;
						arrivalTimeHasBeenSet = false;
						ttsWarnedFirstStep = false;
						ttsWarnedAtTurn = false;
						ttsWarnedBeforeTurn = false;
						TTS.speak("Recalculating", TextToSpeech.QUEUE_ADD, null);
					}
					
					
					
					// Warn them of the first instruction
					else if( !ttsWarnedFirstStep && 
							 (  navDirections.state == NavigationDirections.STATE_IN_STARTING_ZONE ||
							 	navDirections.state == NavigationDirections.STATE_ON_ROUTE ) )
					{
						ttsWarnedFirstStep = true;
						TTS.speak(navDirections.getInstruction(), TextToSpeech.QUEUE_ADD, null);
					}
					
					
					
					// Warn them of the instruction before the maneuver
					else if( !ttsWarnedBeforeTurn && 
							 navDirections.state == NavigationDirections.STATE_ON_ROUTE &&
							 navDirections.getEndLocation().distanceTo(currentKnownLocation) <= 325 )
					{
						ttsWarnedBeforeTurn = true;
						TTS.speak(	"In " + 
									navDirections.getEndLocation().distanceTo(currentKnownLocation, UnitConvert.MILES) +
									" miles, " + navDirections.getInstruction(), TextToSpeech.QUEUE_ADD, null);
					}
					
					
					
					// Warn them when they get to the maneuver
					else if( !ttsWarnedAtTurn && 
							 navDirections.state == NavigationDirections.STATE_ON_ROUTE &&
							 navDirections.getEndLocation().distanceTo(currentKnownLocation) <= 30 )
					{
						ttsWarnedAtTurn = true;
						TTS.speak(navDirections.getInstruction(), TextToSpeech.QUEUE_ADD, null);
						navDirections.state = NavigationDirections.STATE_IN_STEP_ZONE;
					}
					
					
					
					// They have passed through the step and must proceed to the next step
					else if( ttsWarnedBeforeTurn && ttsWarnedAtTurn &&
							 navDirections.state == NavigationDirections.STATE_IN_STEP_ZONE &&
							 navDirections.getEndLocation().distanceTo(currentKnownLocation) > 30 )
					{
						ttsWarnedFirstStep = false;
						ttsWarnedAtTurn = false;
						ttsWarnedBeforeTurn = false;
						navDirections.nextStep();
						navDirections.state = NavigationDirections.STATE_ON_ROUTE;
					}
				}
			}
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
