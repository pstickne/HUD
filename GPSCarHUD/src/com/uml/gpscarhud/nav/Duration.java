package com.uml.gpscarhud.nav;

import org.json.JSONException;
import org.json.JSONObject;

public class Duration 
{
	private String _dStr = "";
	private int _dInt = 0;
	
	public Duration()
	{
		
	}
	
	public Duration(JSONObject json)
	{
		try {
			_dStr = json.getString("text");
			_dInt = json.getInt("value");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public Duration(String s, int i)
	{
		_dStr = s;
		_dInt = i;
	}
	
	public String getDurationText() {
		return _dStr;
	}
	public int getDurationInt() {
		return _dInt;
	}
}
