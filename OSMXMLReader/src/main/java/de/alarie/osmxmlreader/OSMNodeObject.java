/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.alarie.osmxmlreader;

import darwin.util.math.base.vector.Vector2;

/**
 *
 * @author simonschmidt
 */
public class OSMNodeObject implements OSMObject {

    long id;
    private float lat;
    private float lon;

    public OSMNodeObject(long id, float lat, float lon) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
    }

    public Vector2 getVector() {
        return new Vector2(lon, lat);
    }

    public float getLongitude() {
        return lon;
    }

    public float getLatitude() {
        return lat;
    }

    @Override
    public String toString() {
        return "{" + id + ": " + lat + ", " + lon + "}";
    }

    @Override
    public String getTagName() {
        return "node";
    }
}
