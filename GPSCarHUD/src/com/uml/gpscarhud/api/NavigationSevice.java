package com.uml.gpscarhud.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.uml.gpscarhud.nav.NavLocation;

public class NavigationSevice
{
	private NavLocation source = null;
	private NavLocation destination = null;
	private Map<String, Boolean> map = null;
	
	public NavigationSevice()
	{
		
	}
	
	public NavigationSevice(NavLocation s, NavLocation d)
	{
		source = s;
		destination = d;
	}
	
	public void setSource(NavLocation s)
	{
		source = s;
	}
	
	public void setDestination(NavLocation d)
	{
		destination = d;
	}
	
	public void setAvoidances(Map<String, Boolean> m)
	{
		map = m;
	}
	
	public JSONObject getDirections() throws InterruptedException
	{
		final NavigationInternalService nis = new NavigationInternalService();
		nis.result = null;
		
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
				
				if( source == null || destination == null || map == null )
					return;
				
				try {
					// TODO: Test the avoidances
					avoid = new StringBuilder("");
					for( Map.Entry<String, Boolean> entry : map.entrySet() ) {
						if( entry.getValue() ) {
							if( avoid.length() > 0 )
								avoid.append("|" + entry.getKey());
							else
								avoid.append(entry.getKey());
						}
					}

					sb = new StringBuilder("");
					sb.append("http://maps.googleapis.com/maps/api/directions/json?");
					sb.append("origin=%f,%f&");
					sb.append("destination=%f,%f&");
					sb.append("mode=driving&");
					sb.append("avoid=%s&");
					sb.append("sensor=false");
					
					str = String.format( sb.toString() , source.getLat(), source.getLng(), 
														 destination.getLat(), destination.getLng(),
														 avoid.toString());

					Log.i("NavService", "URL: " + str);
					
					client = new DefaultHttpClient();
					request = new HttpGet( str );
					response = client.execute(request);
					
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
		t.start();
		t.join();
		return nis.result;
	}
}

class NavigationInternalService 
{
	JSONObject result;
}
