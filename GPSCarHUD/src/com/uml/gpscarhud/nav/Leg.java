package com.uml.gpscarhud.nav;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Leg 
{
	private Distance distance = null;
	private Duration duration = null;
	private String start_addr = "";
	private String end_addr = "";
	private Location start_loc = null;
	private Location end_loc = null;
	private ArrayList<Step> steps = null;
	
	public Leg()
	{
		
	}
	
	public Leg(JSONObject json)
	{
		try {
			JSONArray jsonSteps = null;
			JSONObject jsonStep = null;
			
			distance = new Distance(json.getJSONObject("distance"));
			duration = new Duration(json.getJSONObject("duration"));
			
			start_addr = json.getString("start_address");
			end_addr = json.getString("end_address");
			
			start_loc = new Location(json.getJSONObject("start_location"));
			end_loc = new Location(json.getJSONObject("end_location"));
			
			jsonSteps = json.getJSONArray("steps");
			for( int i = 0; i < jsonSteps.length(); i++ )
			{
				jsonStep = jsonSteps.getJSONObject(i);
				steps.add(new Step(jsonStep));
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public Distance getDistance() {
		return distance;
	}
	public Duration getDuration() {
		return duration;
	}
	public String getStartAddress() {
		return start_addr;
	}
	public String getEndAddress() {
		return end_addr;
	}
	public Location getStartLocation() {
		return start_loc;
	}
	public Location getEndLocation() {
		return end_loc;
	}
	public ArrayList<Step> getSteps() {
		return steps;
	}
}
