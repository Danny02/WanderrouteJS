/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.alarie.osmxmlreader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author simonschmidt
 */
public class OsmTypesHandler extends DefaultHandler {

    private final Map<Long, OSMNodeObject> nodes = new HashMap<>();
    private final List<OSMWayObject> osmObjects = new ArrayList<>();
    private OSMWayObject currentOSMObject = null;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
        switch (qName) {
            case "way":
                currentOSMObject = parseWay(attrs);
                break;
            case "node":
                handleNode(attrs);
                break;
            case "nd":
                handleNodeReference(attrs);
                break;
            case "tag":
                handleTag(attrs);
                break;
        }
    }

    public Collection<OSMWayObject> getOsmObjects() {
        return osmObjects;
    }

    private void handleNode(Attributes attrs) {
        OSMNodeObject node = parseNode(attrs);
        if (currentOSMObject != null) {
            currentOSMObject.addNode(node);
        }
    }

    private void handleNodeReference(Attributes attrs) {
        long id = Long.valueOf(attrs.getValue("ref"));
        OSMNodeObject node = nodes.get(id);
        if (node != null) {
            if (currentOSMObject != null) {
                currentOSMObject.addNode(node);
            }
        } else {
            System.out.printf("Node not found %i \n", id);
        }
    }

    private OSMWayObject parseWay(Attributes attrs) {
        long id = Long.valueOf(attrs.getValue("id"));

        return new OSMWayObject(id);
    }

    private OSMNodeObject parseNode(Attributes attrs) {
        long id = Long.valueOf(attrs.getValue("id"));
        float lat = Float.valueOf(attrs.getValue("lat"));
        float lon = Float.valueOf(attrs.getValue("lon"));

        OSMNodeObject node = new OSMNodeObject(id, lat, lon);

        nodes.put(id, node);

        return node;
    }

    private void handleTag(Attributes attrs) {
        switch (attrs.getValue("k")) {
            case "highway":
                hanldeHighway(attrs);
                break;
            case "landuse":
                hanldeLanduse(attrs);
                break;
            case "natural":
                hanldeNatural(attrs);
                break;
            case "waterway":
                hanldeWaterway(attrs);
                break;
            case "name":
            case "description":
            case "tracktype":
            case "surface":
            case "width":
            case "maxspeed":
            case "foot":
            case "bicycle":
                handleAttribute(attrs);
                break;

        }
    }

    private void handleAttribute(Attributes attrs) {
        if (currentOSMObject != null) {
            currentOSMObject.addAttribute(attrs.getValue("k"), attrs.getValue("v"));
        }
    }

    private void hanldeHighway(Attributes attrs) {
        if (currentOSMObject != null) {
            currentOSMObject.addAttribute("primary", "highway");
            currentOSMObject.addAttribute("secondary", attrs.getValue("v"));
        }
    }

    private void hanldeLanduse(Attributes attrs) {
        if (currentOSMObject != null) {
            currentOSMObject.addAttribute("primary", "landuse");
            currentOSMObject.addAttribute("secondary", attrs.getValue("v"));
        }
    }

    private void hanldeNatural(Attributes attrs) {
        if (currentOSMObject != null) {
            currentOSMObject.addAttribute("primary", "natural");
            currentOSMObject.addAttribute("secondary", attrs.getValue("v"));
        }
    }

    private void hanldeWaterway(Attributes attrs) {
        if (currentOSMObject != null) {
            currentOSMObject.addAttribute("primary", "waterway");
            currentOSMObject.addAttribute("secondary", attrs.getValue("v"));
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (currentOSMObject != null
                && currentOSMObject.getTagName().equals(qName)
                && currentOSMObject.getAttribute("primary") != null) {
            osmObjects.add(currentOSMObject);
            currentOSMObject = null;
        }
    }
}
