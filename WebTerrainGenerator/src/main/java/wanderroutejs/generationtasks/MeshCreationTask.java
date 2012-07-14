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

import darwin.geometrie.io.*;
import darwin.geometrie.unpacked.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.*;
import wanderroutejs.datasources.*;
import wanderroutejs.generators.*;

/**
 *
 * @author daniel
 */
public class MeshCreationTask implements Runnable {

    private final Future<BufferedImage> ambientMap;
    private final BufferedImage height;
    private final Path file;
    private final float heightScale;
    private final int tessFactor;

    public MeshCreationTask(Future<BufferedImage> ambientMap,
            BufferedImage height, Path file, float heightScale,
            int tessFactor) {
        this.ambientMap = ambientMap;
        this.height = height;
        this.file = file;
        this.heightScale = heightScale;
        this.tessFactor = tessFactor;
    }

    @Override
    public void run() {
        try {
            BufferedImage ambientOcclusionImg = ambientMap.get();
            Model m = generateMesh(height, ambientOcclusionImg);
            saveMesh(m, file);
        } catch (InterruptedException | ExecutionException | IOException ex) {
            ex.printStackTrace();
        }
    }

    private Model generateMesh(BufferedImage height, BufferedImage ambient) {
        HeightSource ambientSource = new ImageHeightSource(ambient, tessFactor * 3, 1f / 255);
        HeightmapGenerator generator = new GridHeightmapWithNormals(tessFactor, ambientSource, height);
        HeightSource source = new ImageHeightSource(height, tessFactor * 3, heightScale);
        Mesh mesh = generator.generateVertexData(source);
        return new Model(mesh, null);
    }

    private void saveMesh(Model mesh, Path file) throws IOException {
        ModelWriter writer = new CtmModelWriter();
        Path outFile = Paths.get(file.toString() + '.' + writer.getDefaultFileExtension());
        try (final OutputStream out = Files.newOutputStream(outFile)) {
            writer.writeModel(out, new Model[]{mesh});
        }
    }
}
