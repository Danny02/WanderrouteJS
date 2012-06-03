package strmreader;

import java.util.ArrayList;
import java.util.Iterator;

public abstract class Generator {
	protected ArrayList<int[]> strip;
	
	private String mesh_type;
	
	public Generator(String mesh_type) {
		this.mesh_type = mesh_type;
	}
	
	public abstract void generateMesh (int[][] heights, int precision, int y1, int y2, int x1, int x2);
	
	public ArrayList<int[]> getStrip () {
		return strip;
	}
	
	public String getMeshType () {
		return mesh_type;
	}
	
	public String toJSON (String variableName) {
		StringBuilder jsonBuilder = new StringBuilder();
		
		if (!variableName.isEmpty()) {
			jsonBuilder.append("var ");
			jsonBuilder.append(variableName);
			jsonBuilder.append(" = ");
		}
		
		if (strip == null || strip.isEmpty()) {
			jsonBuilder.append("[];");
		}
		else {
			Iterator<int[]> it = strip.iterator();
			jsonBuilder.append("[");
			jsonBuilder.append("\n");
			
			while (it.hasNext()) {
				jsonBuilder.append("\t");
				int[] points = it.next();
				jsonBuilder.append(points[0]);
				jsonBuilder.append(",");
				jsonBuilder.append(points[1]);
				jsonBuilder.append(",");
				jsonBuilder.append(points[2]);
				
				if (it.hasNext()) {
					jsonBuilder.append(",");
				}
				jsonBuilder.append("\n");
			}
			
			jsonBuilder.append("];");
		}
		
		return jsonBuilder.toString();
	}
	
	
	
	public static Generator createGenerator(String type) {
		if (type.equals("TRIANGLE_STRIP")) {
			return new TriangleStripGenerator();
		}
		else {
			return new TrianglesGenerator();
		}
	}
}
