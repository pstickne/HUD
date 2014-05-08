package com.uml.gpscarhud.nav;

import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;

import com.uml.gpscarhud.api.Unit;

public class NavLocation extends Location
{
	private long timestamp = 0;
	
	public NavLocation()
	{
		super("");
		timestamp = System.currentTimeMillis();
	}
	
	public NavLocation(Location loc)
	{
		super(loc);
		timestamp = System.currentTimeMillis();
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
		timestamp = System.currentTimeMillis();
	}
	
	public NavLocation(double lat, double lng)
	{
		super("");
		setLatitude(lat);
		setLongitude(lng);
		timestamp = System.currentTimeMillis();
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	public double getLat() {
		return getLatitude();
	}
	public double getLng() {
		return getLongitude();
	}
	
	public double distanceTo(NavLocation other)
	{
		return distanceTo(other, Unit.Converter.TO_METERS);
	}
	public double distanceTo(NavLocation other, int unit)
	{
		return distanceTo(other, unit, 2);
	}
	public double distanceTo(NavLocation other, int unit, int precision)
	{
		double ret = 0;
		Location thisLoc = new Location("");
		Location otherLoc = new Location("");
		
		thisLoc.setLatitude(getLat());
		thisLoc.setLongitude(getLng());
		
		otherLoc.setLatitude(other.getLat());
		otherLoc.setLongitude(other.getLng());
		
		ret = thisLoc.distanceTo(otherLoc);
		return Unit.Converter.convert(ret, unit, precision);
	}
	
	public double speedFrom(NavLocation other)
	{
		return speedFrom(other, Unit.Converter.TO_MILES, Unit.Converter.TO_HOURS);
	}
	public double speedFrom(NavLocation other, int distanceUnit, int timeUnit)
	{
		double d = distanceTo(other, distanceUnit);
		long t = Unit.Converter.convert((getTimestamp() - other.getTimestamp())/1000, timeUnit);
		return d / t;
	}
	
	public String toString()
	{
		return getLat() + ", " + getLng();
	}
}
