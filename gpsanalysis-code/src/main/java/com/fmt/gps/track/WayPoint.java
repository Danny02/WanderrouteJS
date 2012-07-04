package com.fmt.gps.track;

import java.util.Date;
import org.codehaus.jackson.annotate.JsonAutoDetect;

/**
 * Class relating to GPX-&lt;waypt&gt; xml node.
 * <p/>
 * @author root
 * <p/>
 */
@JsonAutoDetect
public class WayPoint implements Point
{
    //@DatabaseField
    private double lat;
    //@DatabaseField
    private double lon;
    //@DatabaseField
    private Date time;

    /**
     * Sets time by miiliseconds.
     * <p/>
     * @param currentTimeMillis
     * <p/>
     */
    @Override
    public void setTime(long currentTimeMillis)
    {
        this.time = new Date(currentTimeMillis);
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
}
