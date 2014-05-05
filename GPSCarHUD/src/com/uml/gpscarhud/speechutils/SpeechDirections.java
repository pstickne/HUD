package com.uml.gpscarhud.speechutils;

import java.util.Locale;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;

/**
 * @author John This class is to serve as a Singleton for issuing speaking
 *         instructions. It will be started once the object has been created and
 *         will be set to the default language of the device.
 */

public class SpeechDirections {

	TextToSpeech speech;

	public SpeechDirections(Context context) {
		speech = new TextToSpeech(context, new OnInitListener() {

			public void onInit(int status) {
				if (status == TextToSpeech.SUCCESS)
					// Set the language to be used as the device default.
					speech.setLanguage(Locale.getDefault());
				else
					Log.i("SPEECH", "Did not start properly.");
			}
		});
	}

	/**
	 * Call when done with this object to release system resources.
	 */
	public void remove() {
		speech.stop();
		speech.shutdown();
	}

	/**
	 * @param message
	 *            A string to be spoken by the device.
	 * @param ensureQueueIsCleared
	 *            True if it is important that nothing else will be said before
	 *            this call.
	 */
	public void say(String message, Boolean ensureQueueIsCleared) {
		if (ensureQueueIsCleared)
			speech.speak(message, TextToSpeech.QUEUE_FLUSH, null);
		else
			speech.speak(message, TextToSpeech.QUEUE_ADD, null);
	}

}
