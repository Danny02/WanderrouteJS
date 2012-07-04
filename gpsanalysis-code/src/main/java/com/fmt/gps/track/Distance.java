package com.fmt.gps.track;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Extra data surrounding each TrackPoint
 * <p/>
 * @author root
 *
 */
public class Distance
{
    /**
     * Feet per minute. *
     */
    public static final double FEET_PER_MILE = 5280.0;
    /**
     * Meters per minute. *
     */
    public static final double M_PER_KM = 1000.0;
    /**
     * default Feet per minute. *
     */
    public static final double DEFAULT_FPS = 5.0;

    /**
     * @return the fpsFromPrevious
     */
    public double getFpsFromPrevious()
    {
        return fpsFromPrevious;
    }

    /**
     * @param fpsFromPrevious the fpsFromPrevious to set
     */
    public void setFpsFromPrevious(double fpsFromPrevious)
    {
        this.fpsFromPrevious = fpsFromPrevious;
    }

    /**
     * @return the fpsToNext
     */
    public double getFpsToNext()
    {
        return fpsToNext;
    }

    /**
     * @param fpsToNext the fpsToNext to set
     */
    public void setFpsToNext(double fpsToNext)
    {
        this.fpsToNext = fpsToNext;
    }

    /**
     * @return the secondsFromPrevious
     */
    public int getSecondsFromPrevious()
    {
        return secondsFromPrevious;
    }

    /**
     * @param secondsFromPrevious the secondsFromPrevious to set
     */
    public void setSecondsFromPrevious(int secondsFromPrevious)
    {
        this.secondsFromPrevious = secondsFromPrevious;
    }

    /**
     * @return the secondsToNext
     */
    public int getSecondsToNext()
    {
        return secondsToNext;
    }

    /**
     * @param secondsToNext the secondsToNext to set
     */
    public void setSecondsToNext(int secondsToNext)
    {
        this.secondsToNext = secondsToNext;
    }

    /**
     * @return the feetFromPrevious
     */
    public double getFeetFromPrevious()
    {
        return feetFromPrevious;
    }

    /**
     * @param feetFromPrevious the feetFromPrevious to set
     */
    public void setFeetFromPrevious(double feetFromPrevious)
    {
        this.feetFromPrevious = feetFromPrevious;
    }

    /**
     * @return the feetToNext
     */
    public double getFeetToNext()
    {
        return feetToNext;
    }

    /**
     * @param feetToNext the feetToNext to set
     */
    public void setFeetToNext(double feetToNext)
    {
        this.feetToNext = feetToNext;
    }
    private double fpsFromPrevious;
    private double fpsToNext;
    private int secondsFromPrevious;
    private int secondsToNext;
    private double feetFromPrevious;
    private double feetToNext;
    /**
     * Date format for GPX files' dates: yyyy-MM-dd'T'HH:mm:ss'Z'. *
     */
    private static final SimpleDateFormat gpxDate;

    static {
        gpxDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        gpxDate.setLenient(false);
        gpxDate.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    /**
     * Degrees to Radians. *
     */
    public static final double d2r = (Math.PI / 180.0);

    /**
     * Getter for gpxDate.
     * <p/>
     * @return gpxDate
	 *
     */
    public static SimpleDateFormat getGpxDateFormatter()
    {
        return (SimpleDateFormat) gpxDate.clone();
    }

    @Override
    public Object clone()
    {
        //throw new CloneNotSupportedException();
        Distance newDist = new Distance();

        newDist.fpsFromPrevious = this.fpsFromPrevious;
        newDist.fpsToNext = this.fpsToNext;
        newDist.secondsFromPrevious = this.secondsFromPrevious;
        newDist.secondsToNext = this.secondsToNext;
        newDist.feetFromPrevious = this.feetFromPrevious;
        newDist.feetToNext = this.feetToNext;

        return newDist;
    }

    @Override
    public String toString()
    {
        String obj = String.format("FPS: %f seconds: %d - %d feet: %f - %f", fpsFromPrevious, secondsFromPrevious, secondsToNext, feetFromPrevious, feetToNext);

        return obj;
    }

    /*public static double mphToFps(double mph) {
     return FEET_PER_MILE*mph/TrackSegment.FMINUTE/TrackSegment.FMINUTE;
     }

     public static double fpsToMph(double fps) {
     return fps/FEET_PER_MILE*TrackSegment.FMINUTE*TrackSegment.FMINUTE;
     }*/
    /**
     * returns time difference
     * <p/>
     * @param one point
     * @param two point
     * <p/>
     * @return time between two points
	 *
     */
    public static int getSecondsDiff(TrackPoint one, TrackPoint two)
    {
        return getSecondsDiff(one.getTime(), two.getTime());
    }

    /**
     * returns time difference
     * <p/>
     * @param one date
     * @param two date
     * <p/>
     * @return time between two points
	 *
     */
    public static int getSecondsDiff(Date one, Date two)
    {
        return Math.abs(((int) (two.getTime() - one.getTime())) / TrackSegment.LMILLIS);
    }

    /**
     * Speed=distance/time.
     * <p/>
     * @param one point
     * @param two point
     * <p/>
     * @return speed between two points
	 *
     */
    @Deprecated
    public static double getFps(TrackPoint one, TrackPoint two)
    {
        int seconds = ((int) (two.getTime().getTime() - one.getTime().getTime())) / TrackSegment.LMILLIS;
        double feet = getFeet(one, two);
        //System.out.printf("%f %d %f%s", distance, time, distance/((double)time), Travelog.CR);

        if (seconds == 0) {
            return DEFAULT_FPS;
        }

        return feet / seconds;
    }

    /**
     * Gets distance between two points.
     * <p/>
     * @param one a Point
     * @param two another point
     * <p/>
     * @return distance between points
	 *
     */
    public static double getFeet(TrackPoint one, TrackPoint two)
    {
        return getFeet(one.getLat(), one.getLon(), two.getLat(), two.getLon());
    }

    /**
     * calculate haversine distance for linear distance between two lat/log
     * coordinates.
     * <p/>
     * @param lat1  latitude of a point
     * @param long1 longitude of a point
     * @param lat2  latitude of second point
     * @param long2 longitude of second point
     * <p/>
     * @return distance between them
	 *
     */
    public static double getFeet(double lat1, double long1, double lat2,
                                 double long2)
    {
        return Trip.USE_KM ? haversine_km(lat1, long1, lat2, long2) * M_PER_KM : haversine_mi(lat1, long1, lat2, long2) * FEET_PER_MILE;
    }

    /**
     * calculate haversine distance for linear distance between two lat/log
     * coordinates.
     * <p/>
     * @param lat1  latitude of a point
     * @param long1 longitude of a point
     * @param lat2  latitude of second point
     * @param long2 longitude of second point
     * <p/>
     * @return distance between them
	 *
     */
    private static double haversine_km(double lat1, double long1, double lat2,
                                       double long2)
    {
        double dlong = (long2 - long1) * d2r;
        double dlat = (lat2 - lat1) * d2r;
        double a = Math.pow(Math.sin(dlat / 2.0), 2) + Math.cos(lat1 * d2r) * Math.cos(lat2 * d2r) * Math.pow(Math.sin(dlong / 2.0), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = 6367 * c;

        return d;
    }

    /**
     * calculate haversine distance for linear distance between two lat/log
     * coordinates.
     * <p/>
     * @param lat1  latitude of a point
     * @param long1 longitude of a point
     * @param lat2  latitude of second point
     * @param long2 longitude of second point
     * <p/>
     * @return distance between them
	 *
     */
    private static double haversine_mi(double lat1, double long1, double lat2,
                                       double long2)
    {
        double dlong = (long2 - long1) * d2r;
        double dlat = (lat2 - lat1) * d2r;
        double a = Math.pow(Math.sin(dlat / 2.0), 2) + Math.cos(lat1 * d2r) * Math.cos(lat2 * d2r) * Math.pow(Math.sin(dlong / 2.0), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = 3956 * c;

        return d;
    }

    /**
     * calculate haversine distance for linear distance between two lat/log
     * coordinates.
     * <p/>
     * @param lat1  latitude of a point
     * @param long1 longitude of a point
     * @param lat2  latitude of second point
     * @param long2 longitude of second point
     * <p/>
     * @return distance between them
	 *
     */
    private static double haversine_km2(double lat1, double long1, double lat2,
                                        double long2)
    {
        double dlong = Math.abs(long2 - long1) * d2r;
        double dlat = Math.abs(lat2 - lat1) * d2r;
        double a = Math.pow(Math.sin(dlat / 2.0), 2) + Math.cos(lat1 * d2r) * Math.cos(lat2 * d2r) * Math.pow(Math.sin(dlong / 2.0), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = 6367 * c;

        return d;
    }
}
