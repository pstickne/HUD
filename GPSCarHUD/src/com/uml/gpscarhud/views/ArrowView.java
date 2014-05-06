package com.uml.gpscarhud.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
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
		if( bitmap != null )
		{
			float scaleWidth = ((float) canvas.getWidth()) / bitmap.getWidth() * 0.5f;
			float scaleHeight = ((float) canvas.getHeight()) / bitmap.getHeight() * 0.7f;
			Matrix matrix = new Matrix();
			
			matrix.postScale(scaleWidth, scaleHeight);
			
			bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
			
			canvas.save();
			canvas.translate(getWidth(), 0);
			canvas.scale(-1, 1);
			canvas.drawBitmap(bitmap, canvas.getWidth()/4, 50, null);
			canvas.restore();
		}
		Log.i("ArrowView", "onDraw()");
	}
}
