package wanderroutejs.srtmreader;

import java.util.*;

public class TriangleStripGenerator implements Generator
{
    @Override
    public Collection<int[]> generateMesh(int[][] heights, int precision,
                                          int y1, int y2, int x1, int x2)
    {
        List<int[]> strip = new ArrayList<>();

        for (int i = y1; i < y2 - 1; i += precision) {
            int[] p1, p2;

            boolean dir = i % 2 == 1;


            for (int j = x1; j < x2; j += precision) {
                System.out.printf("%d, %d%n", i, j);
                p1 = new int[3];
                p2 = new int[3];
                int x = dir ? j - x1 : (x2 - 1) - j;

                p1[0] = x;
                p1[1] = heights[i + precision][x];
                p1[2] = i + precision;

                p2[0] = x;
                p2[1] = heights[i][x];
                p2[2] = i + precision;

                strip.add(dir ? p1 : p2);
                strip.add(dir ? p2 : p1);
            }

        }
        return strip;
    }
}
