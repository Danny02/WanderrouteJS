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
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import darwin.util.image.ImageUtil2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wanderroutejs.MightyGenerat0r;
import wanderroutejs.io.SRTMFileLocator;

/**
 *
 * @author daniel
 */
public class TerrainCreationTask implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(TerrainCreationTask.class);
    private final Rectangle boundingBox;
    private final MightyGenerat0r outer;
    private final Path outPath;
    private final float normalScale, heightScale;
    private final int tessFactor;

    public TerrainCreationTask(Rectangle boundingBox, MightyGenerat0r outer,
            Path outPath, float normalScale,
            float heightScale, int tessFactor) {
        this.boundingBox = boundingBox;
        this.outer = outer;
        this.outPath = outPath;
        this.normalScale = normalScale;
        this.heightScale = heightScale;
        this.tessFactor = tessFactor;
    }

    @Override
    public void run() {
        Callable<Path>[] files = new SRTMFileLocator(outPath).downloadFiles(boundingBox);

        for (Callable<Path> file : files) {
            try {
                this.generateMaps(file);
            } catch (Exception ex) {
                ex.printStackTrace();
            }            
        }
    }

    private void generateMaps(Iterable<Path> files) throws IOException {
        for (Path file : files) {
            this.generateMaps(file);
        }
    }

    private void generateMaps(Callable<Path> task) throws Exception {
        Path file = task.call();

        logger.info("Start loading heightmap texture for " + file + "...");
        long time = System.currentTimeMillis();
        BufferedImage height = ImageUtil2.loadImageByMimeType(file);
        logger.info("\tFinished loading in " + (System.currentTimeMillis() - time));

        Future<BufferedImage> normalMap = outer.submitTask("normal map creation", new NormalMapCreationTask(height, normalScale));
        Future<BufferedImage> ambientMap = outer.submitTask("ambient map creation", new AmbientCreationTask(height, normalScale));

        outer.submitTask("mesh creation", new MeshCreationTask(ambientMap, height, file, heightScale, tessFactor));
        outer.submitTask("normal map write", new ImageMapWriteTask(normalMap, "normalmap", outPath));
        outer.submitTask("ambient map write", new ImageMapWriteTask(ambientMap, "ambientocclusion", outPath));
        outer.submitTask("height map write", new HeightMapWriteTask(height, outPath));
    }
}
