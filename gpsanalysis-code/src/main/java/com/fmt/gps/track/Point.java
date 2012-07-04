package com.fmt.gps.track;

import java.util.Date;
import org.codehaus.jackson.annotate.JsonAutoDetect;

/**
 * Represents one GPS point.
 * <p/>
 * @author root
 *
 */
@JsonAutoDetect
public interface Point
{
    /**
     * @return the lat
     */
    public double getLat();

    /**
     * @param lat the lat to set
     */
    public void setLat(double lat);

    /**
     * @return the lon
     */
    public double getLon();

    /**
     * @param lon the lon to set
     */
    public void setLon(double lon);

    /**
     * @return the time
     */
    public Date getTime();

    /**
     * @param time the time to set
     */
    public void setTime(Date time);

    public void setTime(long time);
}
