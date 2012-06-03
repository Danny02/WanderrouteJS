package wanderroutejs.srtmreader;

import java.util.Collection;

public interface Generator
{
    public Collection<int[]> generateMesh(int[][] heights, int precision,
                                          int y1, int y2, int x1, int x2);
}
