package com.uml.gpscarhud.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.uml.gpscarhud.nav.NavLocation;

/**
 * Accepts preference and location information and exposes a method to request directions from the Google Maps API.
 *
 */
public class NavigationSevice
{
	private NavLocation source = null;
	private NavLocation destinationLoc = null;
	private String destinationStr = null;
	private Map<String, Boolean> map = null;
	
	public NavigationSevice()
	{
		
	}
	
	public NavigationSevice(NavLocation s, NavLocation d)
	{
		source = s;
		destinationLoc = d;
	}
	
	public void setSource(NavLocation s)
	{
		source = s;
	}
	
	public void setDestination(NavLocation d)
	{
		destinationLoc = d;
	}
	
	public void setDestination(String d)
	{
		destinationStr = d;
	}
	
	public void setAvoidances(Map<String, Boolean> m)
	{
		map = m;
	}
	
	public JSONObject getDirections() throws InterruptedException
	{
		final NavigationInternalService nis = new NavigationInternalService();
		nis.result = null;
		
		//Create a non-UI thread for making the HTTP API request.
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {

				String str = null;
				StringBuilder sb = null;
				StringBuilder avoid = null;
				HttpClient client = null;
				HttpGet request = null;
				HttpResponse response = null;
				BufferedReader in = null;
				
				if( source == null || 
					( destinationLoc == null && destinationStr == null ) || 
					map == null )
					return;
				
				//Build the string for any preferred avoidances.
				try {
					avoid = new StringBuilder("");
					for( Map.Entry<String, Boolean> entry : map.entrySet() ) {
						if( entry.getValue() ) {
							if( avoid.length() > 0 )
								avoid.append("|" + entry.getKey());
							else
								avoid.append(entry.getKey());
						}
					}
					
					// If the destination is not already a string, convert it
					if( destinationLoc != null )
					{
						destinationStr = destinationLoc.getLat() + "," + destinationLoc.getLng();
					}

					//Creates the URL request string format.
					sb = new StringBuilder("");
					sb.append("http://maps.googleapis.com/maps/api/directions/json?");
					sb.append("origin=%f,%f&");
					sb.append("destination=%s&");
					sb.append("mode=driving&");
					sb.append("avoid=%s&");
					sb.append("sensor=false");
					
					//Apply the values to the string format created above
					str = String.format( sb.toString() , source.getLat(), source.getLng(), 
														 URLEncoder.encode(destinationStr, "UTF-8"),
														 URLEncoder.encode(avoid.toString(),"UTF-8"));

					Log.i("NavService", "URL: " + str);
					
					//Initialize the HTTP variables.
					client = new DefaultHttpClient();
					request = new HttpGet( str );
					response = client.execute(request);
					
					//Code for reading the response.
					in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
					sb = new StringBuilder("");
					String nl = System.getProperty("line.separator");
					
					while( (str = in.readLine()) != null )
						sb.append(str + nl);
					
					in.close();
					str = sb.toString();
					nis.result = new JSONObject(str);
					
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
		//Run the request thread and wait for it to finish.
		t.start();
		t.join();
		return nis.result;
	}
}

//Internal class for storing the JSON returned.
class NavigationInternalService 
{
	JSONObject result;
}
