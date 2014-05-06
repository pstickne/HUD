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
	private int legIndex = 0;
	private int stepIndex = 0;
	
	private Route route = null;
	
	public NavigationDirections()
	{
		
	}
	
	public void loadRoute(JSONObject json) throws JSONException
	{
		route = new Route(json.getJSONArray("routes").getJSONObject(0));
	}
	
	public void startNavigation()
	{
		if( route == null )
			throw new Error("Route is null");
		
		legIndex = 0;
		stepIndex = 0;
	}
	
	public void nextStep() {
		stepIndex++;
	}

	public Route getRoute()						{	return route;								}
	public Leg getLeg() 						{	return getRoute().getLegs().get(legIndex);	}
	public Step getStep() 						{	return getLeg().getSteps().get(stepIndex);	}
	public String getNextInstruction() 			{	return getStep().getInstruction();			}
	public String getNextManeuver() 			{	return getStep().getManeuver();				}
	public Distance getDistance() 				{	return getStep().getDistance();				}
	public Duration getDuration() 				{	return getStep().getDuration();				}
	public NavLocation getNextEndLocation() 	{	return getStep().getEndAddress();			}
}
