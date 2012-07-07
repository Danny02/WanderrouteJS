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

import java.io.*;
import wanderroutejs.generators.PathTriangulator;

import darwin.geometrie.io.*;
import darwin.geometrie.unpacked.*;
import darwin.util.math.base.vector.Vector3;
import darwin.util.math.composits.Path;

/**
 *
 * @author daniel
 */
public class PathPrismaCreationTask implements Runnable
{
    private final Path<Vector3> path;

    public PathPrismaCreationTask(Path<Vector3> path)
    {
        this.path = path;
    }

    @Override
    public void run()
    {
        PathTriangulator trian = new PathTriangulator();
        //TODO buged floating point math
//        Mesh pathPrisma = trian.buildExtrudedPrisma(0.0001f, 5f, path);
//        ModelWriter writer = new CtmModelWriter();
//        File pathFile = new File(GenerationTest.OUTPUT_PATH, "path." + writer.getDefaultFileExtension());
//        try (final OutputStream out = new FileOutputStream(pathFile)) {
//            writer.writeModel(out, new Model[]{new Model(pathPrisma, null)});
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
    }
}
