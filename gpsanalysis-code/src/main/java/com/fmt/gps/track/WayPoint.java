package com.fmt.gps.track;

import java.util.Date;

import org.codehaus.jackson.annotate.JsonAutoDetect;

/**
 * Class relating to GPX-&lt;waypt&gt; xml node.
 * @author root
 **/
@JsonAutoDetect
public class WayPoint implements Point {
	//@DatabaseField
	private double lat;	
	//@DatabaseField
	private double lon;	
	//@DatabaseField
	private Date time;
	
	/**
	 * Sets time by miiliseconds.
	 * @param currentTimeMillis
	 **/
	public void setTime(long currentTimeMillis) {
		this.time= new Date(currentTimeMillis);
	}
	
	/**
	 * @return the lat
	 */
	public double getLat() {
		return lat;
	}
	/**
	 * @param lat the lat to set
	 */
	public void setLat(double lat) {
		this.lat = lat;
	}
	/**
	 * @return the lon
	 */
	public double getLon() {
		return lon;
	}
	/**
	 * @param lon the lon to set
	 */
	public void setLon(double lon) {
		this.lon = lon;
	}
	/**
	 * @return the time
	 */
	public Date getTime() {
		return time;
	}
	/**
	 * @param time the time to set
	 */
	public void setTime(Date time) {
		this.time = time;
	}
}
