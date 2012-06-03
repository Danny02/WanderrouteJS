package wanderroutejs.srtmreader;

import java.util.*;

public class TrianglesGenerator implements Generator
{
    @Override
    public Collection<int[]> generateMesh(int[][] heights, int precision,
                                          int y1, int y2, int x1, int x2)
    {
        int xHalf = (x2 - x1) / 2;
        int yHalf = (y2 - y1) / 2;

        List<int[]> strip = new ArrayList<>();

        for (int i = y1; i < y2 - 1; i += precision) {
            int[] p1, p2, p3, p4;

            for (int j = x1; j < x2; j += precision) {
                p1 = new int[3];
                p2 = new int[3];
                p3 = new int[3];
                p4 = new int[3];
                int x = j - x1;
                int y = i - y1;

                p1[0] = x - xHalf;
                p1[1] = heights[i + precision][x];
                p1[2] = y + precision - yHalf;

                p2[0] = x - xHalf;
                p2[1] = heights[i][x];
                p2[2] = y - yHalf;

                p3[0] = x + precision - xHalf;
                p3[1] = heights[i + precision][x + precision];
                p3[2] = y + precision - yHalf;

                p4[0] = x + precision - xHalf;
                p4[1] = heights[i][x + precision];
                p4[2] = y - yHalf;


                strip.add(p1);
                strip.add(p2);
                strip.add(p3);

                strip.add(p3);
                strip.add(p2);
                strip.add(p4);
            }

        }
        return strip;
    }
}
