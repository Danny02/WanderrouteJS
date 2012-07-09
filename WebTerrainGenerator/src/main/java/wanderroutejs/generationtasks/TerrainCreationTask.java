/*
 * Copyright (C) 2012 daniel
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
package wanderroutejs.generationtasks;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.concurrent.Future;
import org.slf4j.*;
import wanderroutejs.MightyGenerat0r;
import wanderroutejs.imageprocessing.ImageUtil2;
import wanderroutejs.io.SRTMFileLocator;

/**
 *
 * @author daniel
 */
public class TerrainCreationTask implements Runnable
{
    private final static Logger logger = LoggerFactory.getLogger(TerrainCreationTask.class);
    private final Rectangle boundingBox;
    private final MightyGenerat0r outer;
    private final File outPath;
    private final float normalScale, heightScale;
    private final int tessFactor;

    public TerrainCreationTask(Rectangle boundingBox, MightyGenerat0r outer,
                               File outPath, float normalScale,
                               float heightScale, int tessFactor)
    {
        this.boundingBox = boundingBox;
        this.outer = outer;
        this.outPath = outPath;
        this.normalScale = normalScale;
        this.heightScale = heightScale;
        this.tessFactor = tessFactor;
    }

    @Override
    public void run()
    {
        try {
            long time = System.currentTimeMillis();
            logger.info("Download SRTM tiles");
            Iterable<File> files = new SRTMFileLocator().loadRectangle(boundingBox).loadSRTMFiles(outPath).getFiles();
            logger.info("\tFinishe downloading SRTM tiles in: " + (System.currentTimeMillis() - time));

            generateMaps(files);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void generateMaps(Iterable<File> files) throws IOException
    {
        for (File file : files) {
            this.generateMaps(file);
        }
    }

    private void generateMaps(File file) throws IOException
    {
        logger.info("Start loading heightmap texture for " + file + "...");
        long time = System.currentTimeMillis();
        BufferedImage height = ImageUtil2.loadImage(file.toURI().toURL());
        logger.info("\tFinished loading in " + (System.currentTimeMillis() - time));

        Future<BufferedImage> normalMap = outer.submitTask("normal map creation", new NormalMapCreationTask(height, normalScale));
        Future<BufferedImage> ambientMap = outer.submitTask("ambient map creation", new AmbientCreationTask(height, normalScale));

        outer.submitTask("mesh creation", new MeshCreationTask(ambientMap, height, file, heightScale, tessFactor));
        outer.submitTask("normal map write", new ImageMapWriteTask(normalMap, "normalmap", outPath));
        outer.submitTask("ambient map write", new ImageMapWriteTask(ambientMap, "ambientocclusion", outPath));
        outer.submitTask("height map write", new HeightMapWriteTask(height, outPath));
    }
}
