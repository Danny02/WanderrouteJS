package strmreader;

import java.util.ArrayList;
import java.util.Iterator;

public class TrianglesGenerator extends Generator {
	
	public final String mesh_type = "TRIANGLES";
	
	public TrianglesGenerator() {
		super("TRIANGLES"); 
	}
	
	public void generateMesh (int[][] heights, int precision, int y1, int y2, int x1, int x2) {
		int len = x2 - x1;
		
		strip = new ArrayList<int[]>();
		
		for(int i = y1; i < y2 - 1; i += precision) {
			int[] p1, p2, p3, p4;
			
			for (int j = x1; j < x2; j += precision) {
				System.out.printf("%d, %d%n", i, j);
				p1 = new int[3];
				p2 = new int[3];
				p3 = new int[3];
				p4 = new int[3];
				int x =  j - x1;
				
				p1[0] = x;
				p1[1] = heights[i + precision][x];
				p1[2] = i + precision;
				
				p2[0] = x;
				p2[1] = heights[i][x];
				p2[2] = i;
				
				p3[0] = x + precision;
				p3[1] = heights[i][x];
				p3[2] = i;
				
				p4[0] = x + precision;
				p4[1] = heights[i + precision][x];
				p4[2] = i + precision;
				

				strip.add(p1);
				strip.add(p2);
				strip.add(p3);

				strip.add(p3);
				strip.add(p2);
				strip.add(p4);
			}
			
		}
	}

}
