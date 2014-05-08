package com.uml.gpscarhud.api;

/**
 * Utility class that allow for easy converting of distances.
 *
 */
public class Unit 
{
	public static final int METERS	= ( 1 << 1 );
	public static final int FEET 	= ( 1 << 2 );
	
	public static class Converter 
	{
		// Constants to identify unit metrics.
		public static final int TO_METERS		= ( 1 << 1 );
		public static final int TO_FEET 		= ( 1 << 2 );
		public static final int TO_KILOMETERS	= ( 1 << 3 );
		public static final int TO_MILES		= ( 1 << 4 );
		
		/**
		 * @param input A distance value.
		 * @param unit The desired unit to convert to.
		 * @return Converted distance.
		 */
		public static double convert( double input, int unit )
		{
			if( unit == TO_FEET )
				return input * 3.28084;
			else if( unit == TO_KILOMETERS )
				return input / 1000;
			else if( unit == TO_MILES )
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
}
