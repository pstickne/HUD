package com.uml.gpscarhud.components;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * A class that served as a proof-of-concept for showing how to display text in the way which we desire to work with a mirror/windshield.
 *
 */
public class MirroredTextView extends TextView
{

	public MirroredTextView(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		canvas.save();
		canvas.translate(getWidth(), 0);
		//canvas.scale(-1,1) will result in the text being reversed and upside down.
		canvas.scale(-1, 1);
		super.onDraw(canvas);
		canvas.restore();
	}
}
