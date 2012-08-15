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
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.*;
import java.util.concurrent.Callable;

/**
 *
 * @author simonschmidt
 */
public class SRTMFileLocator {

    public static final String SERVER_URL = "http://dds.cr.usgs.gov/srtm/version2_1/SRTM3/";
    private static final String SRTM_TILE_FILE_NAME_FORMAT = "N%1$02dE%2$03d.hgt.zip";
    private final Path outputDir;

    public SRTMFileLocator(Path outputDir) {
        if (!Files.isDirectory(outputDir)) {
            throw new IllegalArgumentException();
        }
        this.outputDir = outputDir;
    }

    public Callable<Path>[] downloadFiles(Rectangle bBox) {
        Callable<Path>[] tasks = new Callable[bBox.height * bBox.width];
        int startX = bBox.x;
        int startY = bBox.y;
        for (int y = 0; y < bBox.height; y++) {
            for (int x = 0; x < bBox.width; x++) {
                URL tile = getSRTMTileUrl(startX + x, startY + y);
                tasks[y * bBox.width + x] = new SRTMDownloadTask(tile, outputDir);
            }
        }
        return tasks;
    }

    private URL getSRTMTileUrl(int x, int y) {
        // TODO: determine continet
        String continet = "Eurasia/";

        StringBuilder builder = new StringBuilder(SERVER_URL).
                append(continet).
                append(String.format(SRTM_TILE_FILE_NAME_FORMAT, y, x));

        try {
            return new URL(builder.toString());
        } catch (MalformedURLException ex) {
            throw new RuntimeException("Unreachable code path");
        }
    }
}
