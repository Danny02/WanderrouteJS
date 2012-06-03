package wanderroutejs.srtmreader;

import java.io.*;
import java.nio.ByteOrder;

public class SRTMReader
{
    public enum Version
    {
        SRTM1(3601), SRTM3(1201);
        public final int gridSize;

        private Version(int gridSize)
        {
            this.gridSize = gridSize;
        }
    }
    private static final int ENTRY_SIZE = 2;
    private static final ByteOrder plattformByteOrder = ByteOrder.nativeOrder();
    private final Version version;

    public SRTMReader(Version version)
    {
        this.version = version;
    }

    public int[][] parseFile(File file) throws IOException
    {
        int gs = version.gridSize;
        int[][] heights = new int[gs][gs];

        try (InputStream in = new BufferedInputStream(new FileInputStream(file));) {
            int totalReadBytes = 0;
            byte[] bytes = new byte[ENTRY_SIZE];
            while (totalReadBytes < file.length()) {
                int bytesRead = in.read(bytes, 0, ENTRY_SIZE);

                if (bytesRead == ENTRY_SIZE) {
                    int position = totalReadBytes / ENTRY_SIZE;
                    int height = byteArrayToInteger(bytes);
                    heights[position / gs][position % gs] = height;
                }
                //TODO was ist wenn nicht zwei bytes gelesen wurden?

                totalReadBytes += bytesRead;
            }
        }

        return heights;
    }

    private int byteArrayToInteger(byte[] value)
    {
        int intVal = 0;

        if (plattformByteOrder == ByteOrder.BIG_ENDIAN) {
            for (int i = 0; i < value.length; i++) {
                intVal += ((int) value[i] & 0xff) << (8 * i);
            }
        } else {
            for (int i = 0; i < value.length; i++) {
                intVal += (intVal << 8) + (value[i] & 0xff);
            }
        }

        return intVal;
    }
}
