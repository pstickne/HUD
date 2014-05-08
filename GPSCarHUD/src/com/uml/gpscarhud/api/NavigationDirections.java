package com.uml.gpscarhud.api;

import org.json.JSONException;
import org.json.JSONObject;

import com.uml.gpscarhud.nav.Distance;
import com.uml.gpscarhud.nav.Duration;
import com.uml.gpscarhud.nav.Leg;
import com.uml.gpscarhud.nav.NavLocation;
import com.uml.gpscarhud.nav.Route;
import com.uml.gpscarhud.nav.Step;

public class NavigationDirections 
{
	//State variables
	public static int STATE_IN_STARTING_ZONE 	= ( 1 << 1 );
	public static int STATE_ON_ROUTE			= ( 1 << 2 );
	public static int STATE_IN_STEP_ZONE		= ( 1 << 3 );
	public static int STATE_IN_ENDING_ZONE		= ( 1 << 4 );
	
	private int legIndex = 0;
	private int stepIndex = 0;
	public int state = 0;
	//Variable to check if there were two route compliance failures in a row.
	private int isOnRouteFailed = 0;
	
	private JSONObject json = null;
	private Route route = null;
	
	public NavigationDirections()
	{
		
	}
	
	public void loadRoute(JSONObject json) throws JSONException
	{
		this.json = json;
		this.route = new Route(json.getJSONArray("routes").getJSONObject(0));
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
			isOnRouteFailed = 0;
			return true;
		}

		//Calculate bearing variables and distances
		float myBearing = lastLoc.bearingTo(currentLoc);
		float desiredBearing = currentLoc.bearingTo(getEndLocation());
		double lastDistance = lastLoc.distanceTo(getEndLocation());
		double currentDistance = currentLoc.distanceTo(getEndLocation());
		
		//If the bearing is in the right direction and we are getting closer, then we are still on course.
		if( currentDistance < lastDistance &&
			( myBearing < desiredBearing + 23 && myBearing > desiredBearing - 23 ) )
		{
			isOnRouteFailed = 0;
			return true;
		}
		
		//We have failed to be on the correct route twice in a row.
		if( isOnRouteFailed == 2 )
			return false;

		//We have failed to be on the correct route for one check.
		isOnRouteFailed++;
		return true;
	}
	
	/**
	 * Increment the directions step by 1.
	 */
	public void nextStep() {
		stepIndex++;
	}
	
	public JSONObject getJSON()					{ 	return json;								}
	public Route getRoute()						{	return route;								}
	public int getCurrentStep()					{ 	return stepIndex;							}
	public Leg getLeg() 						{	return getRoute().getLegs().get(legIndex);	}
	public Step getStep() 						{	return getLeg().getSteps().get(stepIndex);	}
	public String getInstruction() 				{	return getStep().getInstruction();			}
	public String getManeuver() 				{	return getStep().getManeuver();				}
	public Distance getDistance() 				{	return getStep().getDistance();				}
	public Duration getDuration() 				{	return getStep().getDuration();				}
	public NavLocation getStartLocation()		{ 	return getStep().getStartAddress();			}
	public NavLocation getEndLocation() 		{	return getStep().getEndAddress();			}
}
