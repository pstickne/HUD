package com.uml.gpscarhud.api;

/**
 * Utility class that allow for easy converting of distances.
 *
 */
public class UnitConvert 
{
	//Constants to identify unit metrics.
	public static final int METERS		= ( 1 << 0 );
	public static final int FEET 		= ( 1 << 1 );
	public static final int KILOMETERS	= ( 1 << 2 );
	public static final int MILES		= ( 1 << 3 );
	
	/**
	 * @param input A distance value.
	 * @param unit The desired unit to convert to.
	 * @return Converted distance.
	 */
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
	
	/**
	 * @param input A distance value.
	 * @param unit The desired unit to convert to.
	 * @param precision A desired decimal point precision.
	 * @return Converted distance with the desired precision.
	 */
	public static double convert( double input, int unit, int precision )
	{
		double d = convert( input, unit );
		return Double.valueOf(String.format("%." + precision + "f", d));
	}
}
