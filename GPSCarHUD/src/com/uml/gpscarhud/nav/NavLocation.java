package com.uml.gpscarhud.nav;

import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;

import com.uml.gpscarhud.api.UnitConvert;

public class NavLocation extends Location
{
	public NavLocation()
	{
		super("");
	}
	
	public NavLocation(Location loc)
	{
		super(loc);
	}
	
	public NavLocation(JSONObject json)
	{
		super("");
		try {
			setLatitude(json.getDouble("lat"));
			setLongitude(json.getDouble("lng"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public NavLocation(double lat, double lng)
	{
		super("");
		setLatitude(lat);
		setLongitude(lng);
	}
	
	public double getLat() {
		return getLatitude();
	}
	public double getLng() {
		return getLongitude();
	}
	
	public double distanceTo(NavLocation other)
	{
		return distanceTo(other, UnitConvert.METERS);
	}
	public double distanceTo(NavLocation other, int unit)
	{
		double ret = 0;
		Location thisLoc = new Location("");
		Location otherLoc = new Location("");
		
		thisLoc.setLatitude(getLat());
		thisLoc.setLongitude(getLng());
		
		otherLoc.setLatitude(other.getLat());
		otherLoc.setLongitude(other.getLng());
		
		ret = thisLoc.distanceTo(otherLoc);
		return UnitConvert.convert(ret, unit, 2);
	}
}
