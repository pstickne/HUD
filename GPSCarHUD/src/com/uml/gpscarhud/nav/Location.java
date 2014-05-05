package com.uml.gpscarhud.nav;

import org.json.JSONException;
import org.json.JSONObject;

import com.uml.gpscarhud.api.UnitConvert;

public class Location
{
	private double lat = 0;
	private double lng = 0;
	
	public Location()
	{
		
	}
	
	public Location(JSONObject json)
	{
		try {
			lat = json.getDouble("lat");
			lng = json.getDouble("lng");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public Location(double lat, double lng)
	{
		this.lat = lat;
		this.lng = lng;
	}
	
	public double getLat() {
		return lat;
	}
	public double getLng() {
		return lng;
	}
	
	public double distanceTo(Location other)
	{
		return distanceTo(other, UnitConvert.METERS);
	}
	public double distanceTo(Location other, int unit)
	{
		double ret = 0;
		android.location.Location thisLoc = new android.location.Location("");
		android.location.Location otherLoc = new android.location.Location("");
		
		thisLoc.setLatitude(lat);
		thisLoc.setLongitude(lng);
		
		otherLoc.setLatitude(other.getLat());
		otherLoc.setLongitude(other.getLng());
		
		ret = thisLoc.distanceTo(otherLoc);
		return UnitConvert.convert(ret, unit, 2);
	}
}
