package com.uml.gpscarhud.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class CompassView extends View 
{

	public CompassView(Context context) 
	{
		super(context);
		init();
	}
	public CompassView(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		init();
	}
	public CompassView(Context context, AttributeSet attrs, int defStyle) 
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
		Log.i("CompassView", "onDraw()");
	}
}
