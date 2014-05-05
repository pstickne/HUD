package com.uml.gpscarhud.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class InstructionView extends View 
{
	private String text = null;
	private Paint paint = null;
	
	public InstructionView(Context context) 
	{
		super(context);
		init();
	}
	public InstructionView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}
	public InstructionView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init();
	}
	
	private void init()
	{
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.RED);
		paint.setAlpha(255);
		paint.setTextSize(70);
	}
	
	public void setText(String t)
	{
		text = t;
		invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas)
	{
		canvas.save();
		canvas.translate(getWidth(), 0);
		canvas.scale(-1, 1);

		super.onDraw(canvas);
		canvas.drawText(text, 0, 50, paint);
		
		canvas.restore();
		Log.i("InstructionView", "onDraw()");
	}
}
