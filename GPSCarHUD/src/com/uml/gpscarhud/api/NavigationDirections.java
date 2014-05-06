package com.uml.gpscarhud.api;

import org.json.JSONException;
import org.json.JSONObject;

import com.uml.gpscarhud.nav.Leg;
import com.uml.gpscarhud.nav.NavLocation;
import com.uml.gpscarhud.nav.Route;
import com.uml.gpscarhud.nav.Step;

public class NavigationDirections 
{
	private int legIndex = 0;
	private int stepIndex = 0;
	
	private Route route = null;
	private Leg currentLeg = null;
	private Step currentStep = null;
	
	private NavLocation currentLoc = null;
	
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
	
	private void calcStep()
	{
		currentLeg = route.getLegs().get(legIndex);
		currentStep = currentLeg.getSteps().get(stepIndex);
	}
	public void nextStep() {
		stepIndex++;
	}

	public Route getRoute() {
		calcStep();
		return route;
	}
	public String getNextInstruction() {
		calcStep();
		return currentStep.getInstruction();
	}
	public String getNextManeuver() {
		calcStep();
		return currentStep.getManeuver();
	}
	public String getDistance() {
		calcStep();
		return currentStep.getDistance().getDistanceText();
	}
	public String getDuration() {
		calcStep();
		return currentStep.getDuration().getDurationText();
	}
	public NavLocation getNextEndLocation() {
		calcStep();
		return currentStep.getEndAddress();
	}
	
	public void setCurrentLocation(NavLocation current)
	{
		currentLoc = current;
	}
	
	
}
