package com.uml.gpscarhud.api;

public class UnitConvert 
{
	public static final int METERS		= ( 1 << 0 );
	public static final int FEET 		= ( 1 << 1 );
	public static final int KILOMETERS	= ( 1 << 2 );
	public static final int MILES		= ( 1 << 3 );
	
	public static double convert( double input, int unit )
	{
		if( unit == FEET )
			return input * 3.28084;
		else if( unit == KILOMETERS )
			return input / 1000;
		else if( unit == MILES )
			return input * 0.000621371;
		else
			return input;
	}
	
	public static double convert( double input, int unit, int precision )
	{
		double d = convert( input, unit );
		return Double.valueOf(String.format("%." + precision + "f", d));
	}
}
