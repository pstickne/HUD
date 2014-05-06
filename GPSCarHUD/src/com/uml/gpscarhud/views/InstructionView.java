package com.uml.gpscarhud.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

@SuppressLint("DrawAllocation")
public class InstructionView extends View 
{
	private String text = null;
	TextPaint paint = null;
	StaticLayout textLayout = null;
	
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
		paint = new TextPaint();
		paint.setAntiAlias(true);
		paint.setColor(Color.RED);
		paint.setAlpha(255);
		paint.setTextSize(72);
	}
	
	public void setText(String t)
	{
		text = t;
		if( text != null )
			textLayout = new StaticLayout(text, paint, this.getWidth(), Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
		invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas)
	{
		canvas.save();
		canvas.translate(getWidth(), 0);
		canvas.scale(-1, 1);

		if( text != null ) {
			super.onDraw(canvas);
			textLayout.draw(canvas);
		}
		
		canvas.restore();
		Log.i("InstructionView", "onDraw()");
	}
}
