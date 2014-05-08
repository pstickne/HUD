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
		
		public static final int TO_HOURS		= ( 1 << 5 );
		public static final int TO_MINUTES		= ( 1 << 6 );
		public static final int TO_SECODS		= ( 1 << 7 );
		
		/**
		 * @param input A value.
		 * @param unit The desired unit to convert to.
		 * @return Converted value.
		 */
		public static double convert( double input, int unit )
		{
			switch (unit) {
				case TO_FEET: 		return input * 3.28084;			
				case TO_KILOMETERS:	return input / 1000;
				case TO_MILES:		return input * 0.000621371;
				default:			return input;
			}
		}
		
		public static long convert( long input, int unit )
		{
			switch (unit) {
				case TO_MINUTES:	return input / 60;
				case TO_HOURS:		return input / 60 / 60;
				default:			return input;
			}
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
