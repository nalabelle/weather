package com.nalabelle.weather;

/**
 * Created by nalabelle on 3/12/15.
 */
public class Util {

    /**
     * Gets the australian apparent temperature.
     *
     * @param ws wind speed in m/s at elevation of 10m
     * @param t dry bulb temperature in C
     * @param rh relative humidity %
     * @return AAT-converted temperature.
     */
    public static double getConversion(double ws, double t, double rh) {
        return aatConversion(ws, t, getWaterVaporPressure(t, rh));
    }

    /**
     * Gets the alternate calculation for temperature felt.
     *
     * @param ws wind speed in m/s at elevation of 10m
     * @param t temperature in C
     * @return an alternative wind speed-based calculation for temperature.
     */
    public static double getAltConversion(double ws, double t) {
        ws = convert_ms_to_mph(ws);
        t = convertCtoF(t);
        t = 35.74+(0.6215*t)
                -(35.75*(Math.pow(ws,0.16)))
                +(0.4275*t*(Math.pow(ws,0.16)));
        return convertFtoC(t);
    }

    /**
     * Converts meters/second to miles/hour.
     *
     * @param s speed in meters/second
     * @return speed in miles/hour
     */
    public static double convert_ms_to_mph(double s) {
        return s * 6.2137E-04 * 3600.0;
    }

    /**
     * Converts Celsius to Fahrenheit.
     *
     * @param t temperature in C
     * @return temperature in F
     */
    public static double convertCtoF(double t) {
        return ((9.0 * t) / 5.0) + 32.0;
    }

    /**
     * Converts Fahrenheit to Celsius.
     *
     * @param t temperature in F
     * @return temperature in C
     */
    public static double convertFtoC(double t) {
        return (t - 32.0) * 5.0 / 9.0;
    }

    /**
     * Australian Apparent Temperature
     * From: https://en.wikipedia.org/wiki/Wind_chill#Australian_Apparent_Temperature
     *
     * @param ws wind speed in m/s at elevation of 10m
     * @param t dry bulb temperature in C
     * @param w water vapor pressure hPa
     * @return australian apparent temperature in C
     */
    public static double aatConversion(double ws, double t, double w) {
        return t + .33*w - .70*ws - 4.00;
    }

    /**
     * Returns the water vapor pressure, given humidity and temperature.
     *
     * @param t dry bulb temperature in C
     * @param rh relative humidity %
     * @return double water vapor pressure in hPa
     */
    public static double getWaterVaporPressure(double t, double rh) {
        return rh * 6.105 * Math.exp(17.27*t/(237.7+t));
    }
}
