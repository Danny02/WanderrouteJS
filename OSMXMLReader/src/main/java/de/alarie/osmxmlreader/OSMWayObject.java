/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.alarie.osmxmlreader;

import darwin.util.math.base.vector.Vector2;
import darwin.util.math.composits.Path;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author simonschmidt
 */
public class OSMWayObject implements OSMObject {

    public static final String PRIMARY = "primary";
    public static final String SECONDARY = "secondary";
    public static final String TAG = "way";
    private final long id;
    private final Map<String, String> attributes = new HashMap<>();
    private final List<OSMNodeObject> nodes = new ArrayList<>();
    private Rectangle2D.Float area;

    public OSMWayObject(long id) {
        this.id = id;
    }

    public void setPrimaryType(String type) {
        attributes.put(PRIMARY, type);
    }

    public void setSecondaryType(String type) {
        attributes.put(SECONDARY, type);
    }

    public String getPrimaryType() {
        return attributes.get(PRIMARY);
    }

    public long getId() {
        return id;
    }

    public String getSecondaryType() {
        return attributes.get(SECONDARY);
    }

    public String getAttribute(String key) {
        return attributes.get(key);
    }

    public void addAttribute(String key, String value) {
        attributes.put(key, value);
    }

    @Override
    public String getTagName() {
        return TAG;
    }

    public void addNode(OSMNodeObject node) {
        nodes.add(node);
        if (area == null) {
            area = new Rectangle2D.Float(node.getLatitude(), node.getLongitude(), 0, 0);
        }
        else
        {
            area.add(node.getLatitude(), node.getLongitude());
        }
    }

    public Rectangle2D.Float getArea() {
        return area;
    }    

    public List<OSMNodeObject> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    public Path<Vector2> getPath() {
        Path<Vector2> path = new Path<>();
        for (OSMNodeObject node : getNodes()) {
            path.addPathElement(node.getVector());
        }
        return path;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append("{").append(id).append(": ").append(getPrimaryType()).append(", ").append(getSecondaryType()).append(", attributes: ");

        for (Map.Entry entry : attributes.entrySet()) {
            result.append(entry.getKey()).append(": ").append(entry.getValue()).append(", ");
        }
        result.append(", nodes: ");


        for (OSMNodeObject node : nodes) {
            result.append(node.toString()).append(", ");
        }
        result.append("}");

        return result.toString();
    }
}
