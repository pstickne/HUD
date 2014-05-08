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
	private NavLocation start_loc = null;
	private NavLocation end_loc = null;
	private ArrayList<Step> steps = null;
	
	public Leg()
	{
		
	}
	
	public Leg(JSONObject json)
	{
		JSONArray jsonSteps = null;
		JSONObject jsonStep = null;
		
		steps = new ArrayList<Step>();
		
		try {
			if( json.has("distance") )			distance = new Distance(json.getJSONObject("distance"));
			if( json.has("duration") )			duration = new Duration(json.getJSONObject("duration"));
			if( json.has("start_address") )		start_addr = json.getString("start_address");
			if( json.has("end_address") )		end_addr = json.getString("end_address");
			if( json.has("start_location") )	start_loc = new NavLocation(json.getJSONObject("start_location"));
			if( json.has("end_location") )		end_loc = new NavLocation(json.getJSONObject("end_location"));
			
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
	public NavLocation getStartLocation() {
		return start_loc;
	}
	public NavLocation getEndLocation() {
		return end_loc;
	}
	public ArrayList<Step> getSteps() {
		return steps;
	}
	public void setSteps(ArrayList<Step> steps)
	{
		this.steps = steps;
	}
}
