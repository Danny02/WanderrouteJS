/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.alarie.osmxmlreader;

/**
 *
 * @author simonschmidt
 */
public interface OSMObject {
	public String getTagName();
	public void addNode(OSMNodeObject node);
	public void addAttribute(String key, String value);
	public String getAttribute(String key);
	@Override
	public String toString();
}
