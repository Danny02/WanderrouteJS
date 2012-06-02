package strmreader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteOrder;

public class SRTMReader {
	
	public static final int SRTM1 = 1;
	public static final int SRTM3 = 3;
	
	private static int srtm1_grid_size = 3601;
	private static int srtm3_grid_size = 1201;
	
	private static final int entry_size_in_byte = 2;
	
	private int grid_size;
	
	private ByteOrder plattformByteOrder;
	
	private int[][] heights;
	 
	
	public SRTMReader() {
		
	}
	
	public void init (int type) {
		switch (type) {
		case SRTMReader.SRTM1:
			grid_size = this.srtm1_grid_size;
			break;
		case SRTMReader.SRTM3:
			grid_size = this.srtm3_grid_size;
			break;
		} 
		
		plattformByteOrder = ByteOrder.nativeOrder();
	}
	
	public void read(File file) {

		int fileLength = (int) file.length();
		
		
		heights = new int[grid_size][grid_size];
		
		try{
			InputStream in = null;
			int totalReadBytes = 0;
			int bytesRead = 0;
			
			byte[] bytes = new byte[entry_size_in_byte];
			
			in = new BufferedInputStream(new FileInputStream(file));
			
			while(totalReadBytes < fileLength) {
				bytesRead = in.read(bytes, 0, entry_size_in_byte);
				
				if (bytesRead == 2) {
					createAndAddHeight(totalReadBytes / 2, bytes);
				}
				
				totalReadBytes += bytesRead;
			}
		}
		catch(Exception ex) {
			ex.printStackTrace(System.err);
		}
	}
	
	public int[][] getHeightArray () {
		return heights;
	}
	
	private void createAndAddHeight(int position, byte[] value) {
		int height = byteArrayToInteger(value);
		heights[position / grid_size][position % grid_size] = height;
	}
	
	
	private int byteArrayToInteger (byte[] value) {
		int intVal = 0;
		
		if (plattformByteOrder == ByteOrder.BIG_ENDIAN) {
			for (int i = 0; i < value.length; i++) {
			   intVal += ((int) value[i] & 0xff) << (8 * i);
			}
		}
		else{
			for (int i = 0; i < value.length; i++) {
			   intVal += (intVal << 8) + (value[i] & 0xff);
			}
		}
		
		return intVal;
	}
}
