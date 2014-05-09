package com.uml.gpscarhud.api;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.uml.gpscarhud.nav.Distance;
import com.uml.gpscarhud.nav.Duration;
import com.uml.gpscarhud.nav.Leg;
import com.uml.gpscarhud.nav.NavLocation;
import com.uml.gpscarhud.nav.Route;
import com.uml.gpscarhud.nav.Step;

public class NavigationDirections 
{
	//State variables
	public static int STATE_IN_STARTING_ZONE 	= ( 1 << 1 ); // 2
	public static int STATE_ON_ROUTE			= ( 1 << 2 ); // 4
	public static int STATE_IN_STEP_ZONE		= ( 1 << 3 ); // 8
	public static int STATE_IN_ENDING_ZONE		= ( 1 << 4 ); // 16
	
	private int legIndex = 0;
	private int stepIndex = 0;
	public int state = 0;
	private int failCount = 0;
	private NavLocation lastGoodLocation = null;
	
	private JSONObject json = null;
	private Route route = null;
	
	public NavigationDirections()
	{
		
	}
	
	public void loadRoute(JSONObject json) throws JSONException
	{
		Log.i("NavDir", json.toString(2));
		
		this.json = json;
		this.route = new Route(json.getJSONArray("routes").getJSONObject(0));
		
		addEndingDestination();
	}
	
	public void addEndingDestination()
	{
		ArrayList<Step> steps = getLeg().getSteps();
		JSONObject lastStep = new JSONObject();
		JSONObject temp = null;
		
		try {
			lastStep.put("html_instructions", "Arrive at your destination");
			
			temp = new JSONObject();
			temp.put("lat", getLeg().getEndLocation().getLat());
			temp.put("lng", getLeg().getEndLocation().getLng());
			lastStep.put("end_location", temp);
			
			temp = new JSONObject();
			temp.put("lat", getLeg().getStartLocation().getLat());
			temp.put("lng", getLeg().getStartLocation().getLng());
			lastStep.put("start_location", temp);
			
			temp = new JSONObject();
			temp.put("text", "0 mi");
			temp.put("value", 0);
			lastStep.put("distance", temp);
			
			temp = new JSONObject();
			temp.put("text", "0 min");
			temp.put("value", 0);
			lastStep.put("duration", temp);
			
			steps.add(new Step(lastStep));
			getLeg().setSteps(steps);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
	public void startNavigation()
	{
		//The directions have been initialized properly.
		if( route == null )
			throw new Error("Route is null");
		
		legIndex = 0;
		stepIndex = 0;
		
		//Initial state
		state = STATE_IN_STARTING_ZONE;
	}
	
	/**
	 * This function will test if the driver is still on route, we test 3 locations before determining that they are off the correct route.
	 * @param currentLoc The most recent location that has been reported.
	 * @param lastLoc The location before the current location to determine if the bearing and distance are moving correctly.
	 * @return True if the driver appears to still be on the correct course.
	 */
	public boolean isOnRoute(NavLocation currentLoc, NavLocation lastLoc)
	{
		if ( lastLoc == null ) {
			failCount++;
			
			if( failCount > 5 )
				return false;
				
			failCount = 0;
			lastGoodLocation = currentLoc;
			return true;
		}

		//Calculate bearing variables and distances
		float myBearing = lastLoc.bearingTo(currentLoc);
		float desiredBearing = currentLoc.bearingTo(getStartLocation());
		double lastDistance = lastLoc.distanceTo(getStartLocation());
		double currentDistance = currentLoc.distanceTo(getStartLocation());
		
		Log.i("isOnRoute", "My Bearing: " + myBearing);
		Log.i("isOnRoute", "Desired Bearing: " + desiredBearing);
		Log.i("isOnRoute", "Distance to Last: " + lastDistance);
		Log.i("isOnRoute", "Distance to Start: " + currentDistance);
		if( lastGoodLocation != null )
			Log.i("isOnRoute", "MyPos to LastGoodLoc: " + lastGoodLocation.distanceTo(currentLoc));
		Log.i("isOnRoute", "Failed Count: " + failCount);
		
		
		//If the bearing is in the right direction and we are getting closer, then we are still on course.
		if( Math.floor(currentDistance) <= Math.floor(lastDistance) &&
			( myBearing < desiredBearing + 50 && myBearing > desiredBearing - 50 ) )
		{
			failCount = 0;
			lastGoodLocation = currentLoc;
			return true;
		}
		
		//We have failed to be on the correct route, outside of the radius
		else if( lastGoodLocation != null && lastGoodLocation.distanceTo(currentLoc) > 60 )
		{
			if( failCount > 5 ) {
				failCount = 0;
				return false;
			}
		}

		// We have failed to be on the correct route, but we're still in the radius 
		failCount++;
		return true;
	}
	
	/**
	 * Increment the directions step by 1.
	 */
	public void nextStep() {
		if( stepIndex < getLeg().getSteps().size() - 1 )
			stepIndex++;
	}
	
	public JSONObject getJSON()					{ 	return json;								}
	public Route getRoute()						{	return route;								}
	public int getCurrentStep()					{ 	return stepIndex;							}
	public Leg getLeg() 						{	return getRoute().getLegs().get(legIndex);	}
	public Step getStep() 						{	return getLeg().getSteps().get(stepIndex);	}
	public Step getStep(int i)					{ 	return getLeg().getSteps().get(i);			}
	public String getInstruction() 				{	return getStep().getInstruction();			}
	public String getInstruction(int i)			{ 	return getStep(i).getInstruction();			}
	public String getManeuver() 				{	return getStep().getManeuver();				}
	public Distance getDistance() 				{	return getStep().getDistance();				}
	public Distance getDistance(int i)			{	return getStep(i).getDistance();			}
	public Duration getDuration() 				{	return getStep().getDuration();				}
	public NavLocation getStartLocation()		{ 	return getStep().getStartAddress();			}
	public NavLocation getEndLocation() 		{	return getStep().getEndAddress();			}
}
