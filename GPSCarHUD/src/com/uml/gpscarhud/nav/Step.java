package com.uml.gpscarhud.nav;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import android.os.IBinder;
import android.os.RemoteException;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.internal.IPolylineDelegate;

public class Step 
{
	private Distance distance = null;
	private Duration duration = null;
	private NavLocation start_loc = null;
	private NavLocation end_loc = null;
	private String html_instruction = null;
	private String maneuver = null;
	
	private String tempManeuver = null;
	
	public Step()
	{
		
	}
	
	public Step(JSONObject json)
	{
		try {
			if( json.has("distance") )			distance = new Distance(json.getJSONObject("distance"));
			if( json.has("duration") ) 			duration = new Duration(json.getJSONObject("duration"));
			if( json.has("start_location") )	start_loc = new NavLocation(json.getJSONObject("start_location"));
			if( json.has("end_location") )		end_loc = new NavLocation(json.getJSONObject("end_location"));
			
			if( json.has("html_instructions") )	{
				html_instruction = json.getString("html_instructions");
				
				if( html_instruction.contains("<b>right</b>") )
					tempManeuver = "turn-right";
				else if( html_instruction.contains("<b>left</b>") )
					tempManeuver = "turn-left";
				else
					tempManeuver = "straight";
				
				html_instruction = Jsoup.parse(html_instruction).text();
			}
			
			if( json.has("maneuver") ) {
				maneuver = json.getString("maneuver");
			} else {
				if( tempManeuver != null )
					maneuver = tempManeuver;
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
	public NavLocation getStartAddress() {
		return start_loc;
	}
	public NavLocation getEndAddress() {
		return end_loc;
	}
	public String getInstruction() {
		return html_instruction;
	}
	public String getManeuver() {
		return maneuver;
	}
}
