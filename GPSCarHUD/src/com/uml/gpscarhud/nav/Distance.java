package com.uml.gpscarhud.nav;

import org.json.JSONException;
import org.json.JSONObject;

public class Distance 
{
	private String _dStr = "";
	private int _dInt = 0;
	
	public Distance()
	{
		
	}
	
	public Distance(JSONObject json)
	{
		try {
			_dStr = json.getString("text");
			_dInt = json.getInt("value");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public Distance(String s, int i)
	{
		_dStr = s;
		_dInt = i;
	}
	
	public String getDistanceText() {
		return _dStr;
	}
	public int getDistanceInt() {
		return _dInt;
	}
}
