package com.uml.gpscarhud.api;

import org.json.JSONObject;

import com.uml.gpscarhud.nav.Leg;
import com.uml.gpscarhud.nav.Location;
import com.uml.gpscarhud.nav.Route;
import com.uml.gpscarhud.nav.Step;

public class NavigationDirections 
{
	private int legIndex = 0;
	private int stepIndex = 0;
	
	private Route route = null;
	private Leg currentLeg = null;
	private Step currentStep = null;
	
	private Location currentLoc = null;
	
	private static NavigationDirections _instance = null;
	public static NavigationDirections instance()
	{
		if( _instance == null )
			_instance = new NavigationDirections();
		return _instance;
	}
	public NavigationDirections()
	{
		
	}
	
	public void loadRoute(JSONObject json)
	{
		route = new Route(json);
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
	public Location getNextEndLocation() {
		calcStep();
		return currentStep.getEndAddress();
	}
	
	public void setCurrentLocation(Location current)
	{
		currentLoc = current;
	}
	
	
}
