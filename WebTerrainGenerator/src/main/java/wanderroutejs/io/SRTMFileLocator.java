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
package wanderroutejs.io;

import java.awt.Rectangle;
import java.io.*;
import java.net.URL;
import java.nio.channels.*;
import java.util.*;
import java.util.zip.*;

/**
 *
 * @author simonschmidt
 */
public class SRTMFileLocator
{
    private List<String> fileNames;
    private List<File> files;

    public SRTMFileLocator loadRectangle(Rectangle boundingBox)
    {
        // fetch SRTM
        generateFileNames(boundingBox);

        return this;
    }

    public SRTMFileLocator loadSRTMFiles(File outputDir)
    {
        assert outputDir.isDirectory();
        assert fileNames != null : "no rectangle specified";

        String urlString = getSRTMFileUrlString();

        downloadFiles(outputDir, urlString);

        return this;
    }

    public Iterable<File> getFiles()
    {
        return files;
    }

    private void generateFileNames(Rectangle boundingBox)
    {
        fileNames = new ArrayList<>();

        int startX = boundingBox.x,
                startY = boundingBox.y;

        for (int y = startY; y < startY + boundingBox.height; y++) {
            for (int x = startX; x < startX + boundingBox.width; x++) {
                // create filenames like "N50E011.hgt.zip"
                fileNames.add(String.format("N%1$02dE%2$03d.hgt", y, x));
            }
        }
    }

    private String getSRTMFileUrlString()
    {
        // TODO: determine continet
        String continet = "Eurasia";
        return "http://dds.cr.usgs.gov/srtm/version2_1/SRTM3/" + continet + "/";
    }

    private void downloadFiles(File outputDir, String urlString)
    {
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        files = new ArrayList<>();
        for (String fileName : fileNames) {
            System.out.println("trying to create " + outputDir + fileName + ".zip");
            try {
                File path = new File(outputDir, fileName);
                File pathZip = new File(outputDir, fileName + ".zip");

                downloadFile(pathZip, new URL(urlString + fileName + ".zip"));

                unzipFile(pathZip, outputDir);

                files.add(path);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
    }

    private void downloadFile(File outputPath, URL url)
    {
        System.out.println("Downloading: " + url.toString());
        try (FileOutputStream fileStream = new FileOutputStream(outputPath);) {
            ReadableByteChannel rbc = Channels.newChannel(url.openStream());
            fileStream.getChannel().transferFrom(rbc, 0, 1 << 24);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    final int BUFFER = 2048;

    private void unzipFile(File file, File outputDir)
    {
        try (FileInputStream fis = new FileInputStream(file);
             ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                System.out.println("Extracting: " + entry);

                // write the files to the disk
                try (FileOutputStream fos = new FileOutputStream(new File(outputDir, entry.getName()));
                     BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);) {

                    byte data[] = new byte[BUFFER];
                    int count;
                    while ((count = zis.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, count);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        file.delete();
    }
}
