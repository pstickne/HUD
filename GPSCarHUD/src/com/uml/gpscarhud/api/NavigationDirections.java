package com.uml.gpscarhud.api;

import org.json.JSONObject;

import com.uml.gpscarhud.nav.Route;

public class NavigationDirections 
{
	private int legIndex = 0;
	private int stepIndex = 0;
	private Route route = null;
	
	public NavigationDirections()
	{
		
	}
	
	public NavigationDirections(JSONObject json)
	{
		legIndex = 0;
		stepIndex = 0;
		route = new Route(json);
		
		
	}
	
	
}
