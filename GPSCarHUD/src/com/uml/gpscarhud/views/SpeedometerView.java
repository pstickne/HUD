package com.uml.gpscarhud.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class SpeedometerView extends View 
{

	public SpeedometerView(Context context)
	{
		super(context);
		init();
	}
	public SpeedometerView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}
	public SpeedometerView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init();
	}

	private void init()
	{
		
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		Log.i("SpeedometerView", "onDraw()");
	}
}
