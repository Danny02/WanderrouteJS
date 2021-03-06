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

import darwin.util.math.base.vector.Vector3;
import darwin.util.math.composits.Path;
import java.awt.Rectangle;
import wanderroutejs.MightyGenerat0r;
import wanderroutejs.generators.TrackGenerator;

/**
 *
 * @author daniel
 */
public class PathCreationTask implements Runnable
{
    private final TrackGenerator trackGenerator;
    private final Rectangle boundingBox;
    private final MightyGenerat0r outer;
    private final java.nio.file.Path outPath;
    private final float heightScale;

    public PathCreationTask(TrackGenerator trackGenerator, Rectangle boundingBox,
                            MightyGenerat0r outer, java.nio.file.Path outPath,
                            float heightScale)
    {
        this.trackGenerator = trackGenerator;
        this.boundingBox = boundingBox;
        this.outer = outer;
        this.outPath = outPath;
        this.heightScale = heightScale;
    }

    @Override
    public void run()
    {
        Path<Vector3> path = trackGenerator.getTripAsPath(heightScale, -boundingBox.x, -boundingBox.y);
        outer.submitTask("path mesh creation", new PathMeshCreationTask(path, outPath));
        outer.submitTask("path prisma creation", new PathPrismaCreationTask(path));
    }
}
