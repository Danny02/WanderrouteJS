/*
 * Copyright (C) 2012 simonschmidt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package wanderroutejs.generators;

import java.awt.Rectangle;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 *
 * @author simonschmidt
 */
public class SRTMGenerator {
    ArrayList<String> fileNames;
    ArrayList<String> files;
    
    public SRTMGenerator loadRectangle(Rectangle boundingBox) {
        // fetch SRTM
        generateFileNames(boundingBox);
        
        return this;
    }
    
    public SRTMGenerator loadSRTMFiles (String outputPath) {
        String urlString = this.getSRTMFileUrlString();
        
        downloadFiles(outputPath, urlString);
        
        return this;
    }
    
    public ArrayList<String> getFileNames() {
        return fileNames;
    }

    public ArrayList<String> getFiles() {
        return files;
    }

    private void generateFileNames(Rectangle boundingBox) {
        this.fileNames = new ArrayList<>();
        
        int startX = boundingBox.x,
            startY = boundingBox.y;
        
        for (int y = startY; y < startY + boundingBox.height; y++) {
            for (int x = startX; x < startX + boundingBox.width; x++) {
                // create filenames like "N50E011.hgt.zip"
                fileNames.add(String.format("N%1$02dE%2$03d.hgt", y, x));
            }
        }
    }

    private String getSRTMFileUrlString() {
        // TODO: determine continet
        String continet = "Eurasia";
        return "http://dds.cr.usgs.gov/srtm/version2_1/SRTM3/" + continet + "/";
    }

    private void downloadFiles(String outputPath, String urlString) {
		File dir = new File(outputPath);
		if (dir.exists() && !dir.isFile()) {
			dir.mkdirs();
		}
		
        files = new ArrayList<>();
        for(String fileName : fileNames) {
			System.out.println("trying to create " + outputPath + fileName + ".zip");
			try {
				downloadFile(outputPath + fileName + ".zip", 
						new URL(urlString + fileName + ".zip"));

				unzipFile(outputPath + fileName + ".zip", outputPath);

				files.add(outputPath + fileName);
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
            
        }
    }
    
    private void downloadFile(String outputPath, URL url) {
		System.out.println("Downloading: " + url.toString());
		try (FileOutputStream fileStream = new FileOutputStream(outputPath);) {
			ReadableByteChannel rbc = Channels.newChannel(url.openStream());
			fileStream.getChannel().transferFrom(rbc, 0, 1 << 24);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
    }
    
	final int BUFFER = 2048;
    private void unzipFile(String file, String outputPath){
        try {
			BufferedOutputStream dest = null;
			FileInputStream fis = new FileInputStream(file);
			ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
			ZipEntry entry;
			while((entry = zis.getNextEntry()) != null) {
				System.out.println("Extracting: " + entry);
				int count;
				byte data[] = new byte[BUFFER];
				// write the files to the disk
				FileOutputStream fos = new FileOutputStream(outputPath + entry.getName());
				dest = new BufferedOutputStream(fos, BUFFER);
				while ((count = zis.read(data, 0, BUFFER)) != -1) {
					dest.write(data, 0, count);
				}
				dest.flush();
				dest.close();
			}
			zis.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
    }

    

    
}
