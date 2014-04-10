package com.uml.gpscarhud.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;

public class Navigation
{
	private LatLng source = null;
	private LatLng destination = null;
	
	public Navigation()
	{
		
	}
	
	public Navigation(LatLng s, LatLng d)
	{
		source = s;
		destination = d;
	}
	
	public void setSource(LatLng s)
	{
		source = s;
	}
	
	public void setDestination(LatLng d)
	{
		destination = d;
	}
	
	public JSONObject getDirections()
	{
		JSONObject result = null;
		
		String str = null;
		StringBuilder sb = null;
		HttpClient client = null;
		HttpGet request = null;
		HttpResponse response = null;
		BufferedReader in = null;
		
		if( source == null || destination == null )
			return null;

		try {
			
			sb = new StringBuilder("");
			sb.append("http://maps.googleapis.com/maps/api/directions/json?");
			sb.append("origin=%f,%f&");
			sb.append("destination=%f,%f&");
			sb.append("sensor=false");
			
			str = String.format( sb.toString() , source.latitude, source.longitude, 
												 destination.latitude, destination.longitude);
			
			client = new DefaultHttpClient();
			request = new HttpGet(str);
			response = client.execute(request);
			
			in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			sb = new StringBuilder("");
			String nl = System.getProperty("line.separator");
			
			while( (str = in.readLine()) != null )
				sb.append(str + nl);
			
			in.close();
			str = sb.toString();
			result = new JSONObject(str);
			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return result;
	}
}
