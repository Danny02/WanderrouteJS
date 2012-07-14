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

import darwin.geometrie.io.ModelWriter;
import darwin.geometrie.unpacked.*;
import darwin.util.math.base.vector.Vector3;
import darwin.util.math.composits.Path;
import java.io.*;
import java.nio.file.Files;
import wanderroutejs.generators.PathTriangulator;
import wanderroutejs.io.PlainJSONModelWriter;

/**
 *
 * @author daniel
 */
public class PathMeshCreationTask implements Runnable
{
    private final Path<Vector3> path;
    private final java.nio.file.Path outPath;

    public PathMeshCreationTask(Path<Vector3> path, java.nio.file.Path outPath)
    {
        this.path = path;
        this.outPath = outPath;
    }

    @Override
    public void run()
    {
        PathTriangulator trian = new PathTriangulator();
        Mesh pathMesh = trian.buildPathMesh(path);
        ModelWriter writerJson = new PlainJSONModelWriter();
        java.nio.file.Path np = outPath.resolve("path."+writerJson.getDefaultFileExtension());
        
        try (final OutputStream out = Files.newOutputStream(np)) {
            writerJson.writeModel(out, new Model[]{new Model(pathMesh, null)});
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
