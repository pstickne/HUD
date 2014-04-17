package com.uml.gpscarhud.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class ArrowView extends View
{
	private Bitmap bitmap = null;
	
	public ArrowView(Context context) 
	{
		super(context);
		init();
	}
	public ArrowView(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		init();
	}
	public ArrowView(Context context, AttributeSet attrs, int defStyle) 
	{
		super(context, attrs, defStyle);
		init();
	}

	private void init()
	{
		
	}
	
	public void setArrow(Bitmap b)
	{
		this.bitmap = b;
		invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas)
	{
		canvas.drawBitmap(bitmap, 0, 0, null);
		Log.i("ArrowView", "onDraw()");
	}
}
