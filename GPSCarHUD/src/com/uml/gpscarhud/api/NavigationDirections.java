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
	public static int STATE_IN_STARTING_ZONE 	= ( 1 << 1 );
	public static int STATE_ON_ROUTE			= ( 1 << 2 );
	public static int STATE_IN_STEP_ZONE		= ( 1 << 3 );
	public static int STATE_IN_ENDING_ZONE		= ( 1 << 4 );
	
	private int legIndex = 0;
	private int stepIndex = 0;
	public int state = 0;
	
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
		if( route == null )
			throw new Error("Route is null");
		
		legIndex = 0;
		stepIndex = 0;
		
		state = STATE_IN_STARTING_ZONE;
	}
	
	public boolean isOnRoute(NavLocation currentLoc)
	{
		return true;
	}
	
	public void nextStep() {
		stepIndex++;
	}
	
	public JSONObject getJSON()					{ 	return json;								}
	public Route getRoute()						{	return route;								}
	public Leg getLeg() 						{	return getRoute().getLegs().get(legIndex);	}
	public Step getStep() 						{	return getLeg().getSteps().get(stepIndex);	}
	public String getInstruction() 				{	return getStep().getInstruction();			}
	public String getManeuver() 				{	return getStep().getManeuver();				}
	public Distance getDistance() 				{	return getStep().getDistance();				}
	public Duration getDuration() 				{	return getStep().getDuration();				}
	public NavLocation getStartLocation()		{ 	return getStep().getStartAddress();			}
	public NavLocation getEndLocation() 		{	return getStep().getEndAddress();			}
}
