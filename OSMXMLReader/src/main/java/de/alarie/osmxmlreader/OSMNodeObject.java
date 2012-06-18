/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.alarie.osmxmlreader;

import darwin.util.math.base.vector.Vector3;
import org.xml.sax.Attributes;

/**
 *
 * @author simonschmidt
 */
class OSMNodeObject implements OSMObject{
	long id;
	private float lat;
	private float lon;
	private Vector3 vec;
	
	public OSMNodeObject (long id, float lat, float lon) {
		this.id = id;
		this.lat = lat;
		this.lon = lon;
		this.vec = new Vector3(lat, lon, 0f);
	}
	
	public Vector3 getVector() {
		return vec;
	}
	
	@Override
	public String getAttribute(String key) {
		return null;
	};
	
	@Override
	public void addAttribute(String key, String value) {
	}
	
	@Override
	public void addNode(OSMNodeObject node) {
	}
	
	@Override
	public String toString () {
		return "{" + id + ": " + lat + ", " + lon + "}";
	}
	
	@Override
	public String getTagName () {
		return "node";
	}
	
	
}
