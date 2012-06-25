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
import java.io.BufferedOutputStream;
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
                files.add(String.format("N%1$2dE%2$3d.hgt", y, x));
            }
        }
    }

    private String getSRTMFileUrlString() {
        // TODO: determine continet
        String continet = "Eurasia";
        return "http://dds.cr.usgs.gov/srtm/version2_1/SRTM3/" + continet + "/";
    }

    private void downloadFiles(String outputPath, String urlString) {
        files = new ArrayList<>();
        for(String fileName : fileNames) {
            try {
                FileOutputStream fileStream = new FileOutputStream(outputPath + fileName + ".zip");
                
                this.downloadFile(fileStream, 
                        new URL(urlString + fileName + ".zip"));
                
                this.unzipFile(outputPath + fileName + ".zip");
                
                files.add(outputPath + fileName);
            }
            catch (Exception ex) {
                System.err.println(ex.getMessage());
            }
        }
    }
    
    private void downloadFile(FileOutputStream outputPath, URL url) throws IOException {
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        outputPath.getChannel().transferFrom(rbc, 0, 1 << 24);
    }
    
    private void unzipFile(String file){
        Enumeration entries;
        ZipFile zipFile;
        try {
            zipFile = new ZipFile(file);
            entries = zipFile.entries();
            while(entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry)entries.nextElement();

                InputStream in = zipFile.getInputStream(entry);
                OutputStream out = new BufferedOutputStream(new FileOutputStream(entry.getName()));

                byte[] buffer = new byte[1024];
                int len;
                while((len = in.read(buffer)) >= 0)
                out.write(buffer, 0, len);
                in.close();
                out.close();

                zipFile.close();
            }
        } catch (IOException ioe) {
            System.err.println("Unhandled exception:");
            ioe.printStackTrace();
            return;
        }
    }

    

    
}
