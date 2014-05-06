package com.uml.gpscarhud.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

public class Maneuvers 
{
	private static ArrayList<Point> arrowPoints = null;
	private static Map<String, ArrayList<Point>> arrowsMap = null;
	
	private static void initMap()
	{
		arrowsMap = new HashMap<String, ArrayList<Point>>();
		
		// turn-right
		arrowPoints = new ArrayList<Point>();
		arrowPoints.add(new Point(0, 100));
		arrowPoints.add(new Point(0, 16));
		arrowPoints.add(new Point(48, 16));
		arrowPoints.add(new Point(48, 0));
		arrowPoints.add(new Point(100, 24));
		arrowPoints.add(new Point(48, 48));
		arrowPoints.add(new Point(48, 32));
		arrowPoints.add(new Point(16, 32));
		arrowPoints.add(new Point(16, 100));
		arrowPoints.add(new Point(0, 100));
		arrowsMap.put("turn-right", arrowPoints);
		arrowsMap.put("turn-sharp-right", arrowPoints);
		
		// turn-left
		arrowPoints = new ArrayList<Point>();
		arrowPoints.add(new Point(100, 100));
		arrowPoints.add(new Point(100, 16));
		arrowPoints.add(new Point(52, 16));
		arrowPoints.add(new Point(52, 0));
		arrowPoints.add(new Point(0, 24));
		arrowPoints.add(new Point(52, 48));
		arrowPoints.add(new Point(52, 32));
		arrowPoints.add(new Point(84, 32));
		arrowPoints.add(new Point(84, 100));
		arrowPoints.add(new Point(100, 100));
		arrowsMap.put("turn-left", arrowPoints);
		arrowsMap.put("turn-sharp-left", arrowPoints);
		
		// merge
		arrowPoints = new ArrayList<Point>();
		arrowPoints.add(new Point(98, 98));
		arrowPoints.add(new Point(97, 73));
		arrowPoints.add(new Point(61, 41));
		arrowPoints.add(new Point(61, 29));
		arrowPoints.add(new Point(78, 29));
		arrowPoints.add(new Point(50, 2));
		arrowPoints.add(new Point(21, 29));
		arrowPoints.add(new Point(38, 29));
		arrowPoints.add(new Point(38, 41));
		arrowPoints.add(new Point(2, 73));
		arrowPoints.add(new Point(2, 98));
		arrowPoints.add(new Point(24, 98));
		arrowPoints.add(new Point(24, 78));
		arrowPoints.add(new Point(50, 57));
		arrowPoints.add(new Point(75, 78));
		arrowPoints.add(new Point(75, 98));
		arrowPoints.add(new Point(98, 98));
		arrowsMap.put("merge", arrowPoints);
		
		// straight
		arrowPoints = new ArrayList<Point>();
		arrowPoints.add(new Point(62, 98));
		arrowPoints.add(new Point(62, 29));
		arrowPoints.add(new Point(78, 29));
		arrowPoints.add(new Point(50, 2));
		arrowPoints.add(new Point(21, 29));
		arrowPoints.add(new Point(38, 29));
		arrowPoints.add(new Point(38, 98));
		arrowPoints.add(new Point(65, 98));
		arrowsMap.put("straight", arrowPoints);
	}
	
	public static Bitmap getManeuver(String m)
	{
		if( arrowsMap == null )
			initMap();
		
		Bitmap b = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(b);
		drawManeuver(c, m);
		
		return b;
	}
	
	private static Canvas drawManeuver(Canvas canvas, String m)
	{
		Path path = new Path();
		Paint paint = new Paint();
		ArrayList<Point> points = arrowsMap.get(m);
		Point p = points.get(0);
		
		paint.setStrokeWidth(0);
		paint.setColor(Color.RED);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		paint.setAntiAlias(true);
		
		path.moveTo(p.x, p.y);
		for (int i = 1; i < points.size(); i++) {
			p = points.get(i);
			path.lineTo(p.x, p.y);
		}
		
		canvas.drawPath(path, paint);
		
		return canvas;
	}
}
