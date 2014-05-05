package com.uml.gpscarhud.nav;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Route 
{
	private ArrayList<Leg> legs = null;
	
	public Route()
	{
		
	}
	
	public Route(JSONObject json)
	{
		JSONArray jsonLegs = null;
		JSONObject jsonLeg = null;
		
		try {
			jsonLegs = json.getJSONArray("legs");

			for( int i = 0; i < jsonLegs.length(); i++ )
			{
				jsonLeg = jsonLegs.getJSONObject(i);
				legs.add(new Leg(jsonLeg));
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<Leg> getLegs()
	{
		return legs;
	}
}
