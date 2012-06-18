/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.alarie.osmxmlreader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author simonschmidt
 */
class OsmTypesHandler extends DefaultHandler {
    private final HashMap<Long, OSMNodeObject> nodes = new HashMap<>();
    private final SortedSet<String> types = new TreeSet<>();
    private final Stack<String> elements = new Stack<>();
	
	private final ArrayList<OSMObject> osmObjects = new ArrayList<>();
	private OSMObject currentOSMObject = null;

	// filter for:
	
	// landuse: Forest, meadow,...
	SortedSet<String> landuseTypes = new TreeSet<>();
	
	// highway: track/residential/footway/service
	//	- description, name, surface (concrete/ground/...), tracktype (grade), width, maxspeed
	SortedSet<String> highwayTypes = new TreeSet<>();
	
	// bridge: yes

	// waterway: river, stream
	//	- name
	SortedSet<String> waterwayTypes = new TreeSet<>();
	
	// natural: water, scrub
	//	- name
	SortedSet<String> naturalTypes = new TreeSet<>();
	
	
	// addr:city
	
	public ArrayList<OSMObject> getObjects () {
		return osmObjects;
	}
	
	
	public SortedSet<String> getTypes() {
        return types;
    }
	
	public SortedSet<String> getHighwayTypes() {
        return highwayTypes;
    }
	
	public SortedSet<String> getLanduseTypes() {
        return landuseTypes;
    }
	
	public SortedSet<String> getNaturalTypes() {
        return naturalTypes;
    }
	
	public SortedSet<String> getWaterwayTypes() {
        return waterwayTypes;
    }
	
	
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
		switch (qName) {
			case "way":
				handleWay(attrs);
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
		
        elements.push(qName);
    }
	
	private void handleWay (Attributes attrs) {
		currentOSMObject = parseWay(attrs);
	}
	
	private void handleNode (Attributes attrs) {
		OSMNodeObject node = parseNode(attrs);
		if (currentOSMObject != null) {
			currentOSMObject.addNode(node);
		}
	}
	
	private void handleNode (OSMNodeObject node) {
		if (currentOSMObject != null) {
			currentOSMObject.addNode(node);
		}
	}
	
	private void handleNodeReference (Attributes attrs) {
		long id = Long.valueOf(attrs.getValue("ref"));
		OSMNodeObject node = nodes.get(id);
		if (node != null) {
			handleNode(node);
		}
		else {
			System.out.printf("Node not found %i \n", id);
		}
	}
	
	private OSMWayObject parseWay(Attributes attrs) {
		long id = Long.valueOf(attrs.getValue("id"));
		
		OSMWayObject way = new OSMWayObject(id);
		
		return way;
	}
	
	private OSMNodeObject parseNode(Attributes attrs) {
		long id = Long.valueOf(attrs.getValue("id"));
		float lat = Float.valueOf(attrs.getValue("lat"));
		float lon = Float.valueOf(attrs.getValue("lat"));
		
		OSMNodeObject node = new OSMNodeObject(id, lat, lon);
		
		nodes.put(id, node);
		
		return node;
	}
	
	private void handleTag (Attributes attrs) {
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
		if(currentOSMObject != null){
			currentOSMObject.addAttribute("primary", "highway");
			currentOSMObject.addAttribute("secondary", attrs.getValue("v"));
		}		
		
		highwayTypes.add(attrs.getValue("v"));
	}
				
	private void hanldeLanduse(Attributes attrs) {
		if(currentOSMObject != null){
			currentOSMObject.addAttribute("primary", "landuse");
			currentOSMObject.addAttribute("secondary", attrs.getValue("v"));
		}		
		
		landuseTypes.add(attrs.getValue("v"));
	}
				
	private void hanldeNatural(Attributes attrs) {
		if(currentOSMObject != null){
			currentOSMObject.addAttribute("primary", "natural");
			currentOSMObject.addAttribute("secondary", attrs.getValue("v"));
		}		
		
		naturalTypes.add(attrs.getValue("v"));
	}
				
	private void hanldeWaterway(Attributes attrs) {
		if(currentOSMObject != null){
			currentOSMObject.addAttribute("primary", "waterway");
			currentOSMObject.addAttribute("secondary", attrs.getValue("v"));
		}		
		
		waterwayTypes.add(attrs.getValue("v"));
	}
    
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
		if (currentOSMObject != null && 
				currentOSMObject.getTagName().equals(qName) &&
				currentOSMObject.getAttribute("primary") != null) {
			osmObjects.add(currentOSMObject);
			currentOSMObject = null;
		}
        elements.pop();
    }
}
