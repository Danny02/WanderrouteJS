package strmreader;

import java.util.ArrayList;
import java.util.Iterator;

public class TriangelStripGenerator {
	
	public ArrayList<int[]> strip;
	
	public void generateMesh (int[][] heights, int precision, int y1, int y2, int x1, int x2) {
		int len = x2 - x1;
		boolean dir = true;
		
		strip = new ArrayList<int[]>();
		
		for(int i = y1; i < y2 - 1; i+=precision) {
			int[] p1, p2;
			
			dir = i % 2 == 1;
			
			
			for (int j = x1; j < x2; j+=precision) {
				System.out.printf("%d, %d%n", i, j);
				p1 = new int[3];
				p2 = new int[3];
				int x =  dir ? j - x1 : (x2 - 1) - j;
				
				p1[0] = x;
				p1[1] = heights[i+1][x];
				p1[2] = i+1;
				
				p2[0] = x;
				p2[1] = heights[i][x];
				p2[2] = i;
				
				strip.add(dir ? p1 : p2);
				strip.add(dir ? p2 : p1);
			}
			
		}
	}
	
	public ArrayList<int[]> getStrip () {
		return strip;
	}
	
	public String toJSON () {
		StringBuilder jsonBuilder = new StringBuilder();
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
		
		jsonBuilder.append("]");
		
		
		return jsonBuilder.toString();
	}
}
