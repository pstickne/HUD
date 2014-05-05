package com.uml.gpscarhud.speechutils;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;

public class SpeechDirections {
	
	TextToSpeech speech;
	
	public SpeechDirections(Context context)
	{
		speech = new TextToSpeech(context, new OnInitListener() {
			
			public void onInit(int status) {
				if( status == TextToSpeech.ERROR)
					Log.i("SPEECH", "Did not start properly.");				
			}
		});
	}
	
	public void remove()
	{
		speech.stop();
		speech.shutdown();
	}
	
	public void say(String message)
	{
		speech.speak(message, TextToSpeech.QUEUE_ADD, null);
	}

}
