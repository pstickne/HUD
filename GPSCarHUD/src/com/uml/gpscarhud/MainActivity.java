package com.uml.gpscarhud;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class MainActivity extends PreferenceActivity
{
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.preferences);
	}
	
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onPostCreate(Bundle savedInstanceState) 
	{
		super.onPostCreate(savedInstanceState);
		
		// Set Preference Change Listeners
		findPreference("destination_text").setOnPreferenceChangeListener(strPrefChangeListener);
		findPreference("avoid_highways_checkbox").setOnPreferenceChangeListener(boolPrefChangeListener);
		findPreference("avoid_tolls_checkbox").setOnPreferenceChangeListener(boolPrefChangeListener);
		findPreference("avoid_uturns_checkbox").setOnPreferenceChangeListener(boolPrefChangeListener);
		findPreference("avoid_ferries_checkbox").setOnPreferenceChangeListener(boolPrefChangeListener);
		
		// Set Preference Default Values
		findPreference("destination_text").setSummary(getPreferences(MODE_PRIVATE).getString("destination_text", ""));
		
		// Set Preference Intents
		findPreference("launch_hud").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(MainActivity.this, HUDActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(intent);
				return true;
			}
		});
		findPreference("launch_navigation").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				String dest = getPreferences(MODE_PRIVATE).getString("destination_text", "");
				
				if( dest.length() > 0 )
				{
					Intent intent = new Intent(MainActivity.this, HUDActivity.class);
					intent.putExtra("destination", dest);
					intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
					startActivity(intent);
				} else {
					AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
					builder
						.setTitle(R.string.pref_error_title)
						.setMessage(R.string.pref_error_msg)
						.setNeutralButton("OK", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						})
						.create().show();
					return false;
				}
				return true;
			}
		});
	}
	
	private Preference.OnPreferenceChangeListener strPrefChangeListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) 
		{
			getPreferences(MODE_PRIVATE).edit().putString(preference.getKey(), (String) newValue).apply();
			preference.setSummary((String) newValue);
			
			return true;
		}
	};
	
	private Preference.OnPreferenceChangeListener boolPrefChangeListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue)
		{
			getPreferences(MODE_PRIVATE).edit().putBoolean(preference.getKey(), (Boolean) newValue).apply();
			return true;
		}
	};
}
