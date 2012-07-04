package com.fmt.gps.track;

import com.fmt.gps.Travelog;
import java.util.Date;
import org.codehaus.jackson.annotate.JsonAutoDetect;

/**
 * Track Point to be used in Trip lists.
 * <p/>
 * @author root
 *
 */
@JsonAutoDetect
public class TrackPoint implements Point
{
    private double lat;
    private double lon;
    private Date time;
    private int elevation;
    private boolean distanceSet = false;
    private int position = 0;
    private Distance distance = null;

    @Override
    public String toString()
    {
        String obj = String.format("lat: %f lon: %f time: %s ", lat, lon, Travelog.TIME.format(this.getTime()));
        if (null != this.distance) {
            obj += this.distance.toString(); //+Travelog.CR;
        }

        return obj;
    }

    /**
     * Creates TrackPoint (for Scala compatibility).
     * <p/>
     * @return instantiated TrackPoint object
	 *
     */
    public static TrackPoint trackPointFactory()
    {
        return new TrackPoint();
    }

    @Override
    public Object clone()
    {
        TrackPoint newPt = new TrackPoint();

        newPt.lat = this.lat;
        newPt.lon = this.lon;
        newPt.time = (Date) this.time.clone();
        newPt.position = this.position;
        if (this.distanceSet && null != this.distance) {
            newPt.distanceSet = this.distanceSet;
            newPt.distance = (Distance) this.distance.clone();
        } else {
            newPt.distanceSet = false;
            newPt.distance = null;
        }

        return newPt;
    }

    /**
     * Constructor: needed by OrmLite.
	 *
     */
    public TrackPoint()
    {
        distanceSet = false;
    }

    /**
     * @return the lat
     */
    @Override
    public double getLat()
    {
        return lat;
    }

    /**
     * @param lat the lat to set
     */
    @Override
    public void setLat(double lat)
    {
        this.lat = lat;
    }

    /**
     * @return the lon
     */
    @Override
    public double getLon()
    {
        return lon;
    }

    /**
     * @param lon the lon to set
     */
    @Override
    public void setLon(double lon)
    {
        this.lon = lon;
    }

    /**
     * @return the time
     */
    @Override
    public Date getTime()
    {
        return time;
    }

    /**
     * @param time the time to set
     */
    @Override
    public void setTime(Date time)
    {
        this.time = time;
    }

    /**
     * Sets time by miiliseconds.
     * <p/>
     * @param currentTimeMillis
	 *
     */
    @Override
    public void setTime(long currentTimeMillis)
    {
        this.time = new Date(currentTimeMillis);
    }

    /**
     * @return the elevation
     */
    public int getElevation()
    {
        return elevation;
    }

    /**
     * @param elevation the elevation to set
     */
    public void setElevation(int elevation)
    {
        this.elevation = elevation;
    }

    /**
     * @return the distanceSet
     */
    public boolean isDistanceSet()
    {
        return distanceSet;
    }

    /**
     * @param distanceSet the distanceSet to set
     */
    public void setDistanceSet(boolean distanceSet)
    {
        this.distanceSet = distanceSet;
    }

    /**
     * @return the position
     */
    public int getPosition()
    {
        return position;
    }

    /**
     * @param position the position to set
     */
    public void setPosition(int position)
    {
        this.position = position;
    }

    /**
     * @return the distance
     */
    public Distance getDistance()
    {
        return distance;
    }

    /**
     * @param distance the distance to set
     */
    public void setDistance(Distance distance)
    {
        this.distance = distance;
    }
}
