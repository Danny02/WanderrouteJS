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

import wanderroutejs.MightyGenerat0r;
import wanderroutejs.generators.GridHeightmapWithNormals;
import wanderroutejs.generators.HeightmapGenerator;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.concurrent.*;
import wanderroutejs.datasources.*;

import darwin.geometrie.io.*;
import darwin.geometrie.unpacked.*;

/**
 *
 * @author daniel
 */
public class MeshCreationTask implements Runnable
{
    private static final int TESS_FACTOR = 100;
    private final Future<BufferedImage> ambientMap;
    private final BufferedImage height;
    private final File file;

    public MeshCreationTask(Future<BufferedImage> ambientMap,
                            BufferedImage height, File file)
    {
        this.ambientMap = ambientMap;
        this.height = height;
        this.file = file;
    }

    @Override
    public void run()
    {
        try {
            BufferedImage ambientOcclusionImg = ambientMap.get();
            Model m = generateMesh(height, ambientOcclusionImg);
            saveMesh(m, file);
        } catch (InterruptedException | ExecutionException | IOException ex) {
            ex.printStackTrace();
        }
    }

    private Model generateMesh(BufferedImage height, BufferedImage ambient)
    {
        HeightSource ambientSource = new ImageHeightSource(ambient, TESS_FACTOR * 3, 1f / 255);
        HeightmapGenerator generator = new GridHeightmapWithNormals(TESS_FACTOR, ambientSource, height);
        HeightSource source = new ImageHeightSource(height, TESS_FACTOR * 3, MightyGenerat0r.HEIGHT_SCALE);
        Mesh mesh = generator.generateVertexData(source);
        return new Model(mesh, null);
    }

    private void saveMesh(Model mesh, File file) throws IOException
    {
        ModelWriter writer = new CtmModelWriter();
        try (final OutputStream out = new FileOutputStream(file.getPath() + '.' + writer.getDefaultFileExtension())) {
            writer.writeModel(out, new Model[]{mesh});
        }
    }
}
