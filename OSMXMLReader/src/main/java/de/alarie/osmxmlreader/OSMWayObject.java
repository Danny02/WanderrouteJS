/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.alarie.osmxmlreader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author simonschmidt
 */
class OSMWayObject implements OSMObject {
	private long id;
	private String primaryType;
	private String secondaryType;
	
	private HashMap<String, String> attributes = new HashMap<>();
	
	private final ArrayList<OSMNodeObject> nodes = new ArrayList<>();
	

	public OSMWayObject(long id) {
		this.id = id;
	}
	
	public void setPrimaryType(String type) {
		this.primaryType = type;
	}
	
	public void setSecondaryType(String type) {
		this.secondaryType = type;
	}
	
	public String getAttribute(String key) {
		switch (key) {
			case "primary":
				return primaryType;
			case "secondary":
				return secondaryType;
			default:
				return attributes.get(key);
		}
	}
	
	@Override 
	public void addAttribute(String key, String value) {
		switch (key) {
			case "primary":
				setPrimaryType(value);
				break;
			case "secondary":
				setSecondaryType(value);
				break;
			default:
				attributes.put(key, value);
				break;
		}
	}
	
	@Override
	public String getTagName () {
		return "way";
	}
	
	@Override
	public void addNode (OSMNodeObject node) {
		nodes.add(node);
	}
	
	@Override
	public String toString () {
		StringBuilder result = new StringBuilder();
		
		result.append("{")
				.append(id)
				.append(": ")
				.append(primaryType) 
				.append(", ")
				.append(secondaryType)
				.append(", attributes: ");
		
		Iterator it = attributes.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry entry = (Map.Entry) it.next();
			result.append(entry.getKey())
					.append(": ")
					.append(entry.getValue())
					.append(", ");
		}
		
		
		result.append(", nodes: ");
	
		
		for(OSMNodeObject node : nodes){
			result.append(node.toString() + ", ");
		}
		result.append("}");
		
		return result.toString();
	}
}
