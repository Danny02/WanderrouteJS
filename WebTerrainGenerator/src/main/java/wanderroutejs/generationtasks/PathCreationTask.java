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
import java.awt.Rectangle;
import wanderroutejs.generators.TrackGenerator;

import darwin.util.math.base.vector.Vector3;
import darwin.util.math.composits.Path;

/**
 *
 * @author daniel
 */
public class PathCreationTask implements Runnable
{
    private final TrackGenerator trackGenerator;
    private final Rectangle boundingBox;
    private final MightyGenerat0r outer;

    public PathCreationTask(TrackGenerator trackGenerator, Rectangle boundingBox,
                            final MightyGenerat0r outer)
    {
        this.outer = outer;
        this.trackGenerator = trackGenerator;
        this.boundingBox = boundingBox;
    }

    @Override
    public void run()
    {
        Path<Vector3> path = trackGenerator.getTripAsPath(MightyGenerat0r.HEIGHT_SCALE, -boundingBox.x, -boundingBox.y);
        outer.submitTask("path mesh creation", new PathMeshCreationTask(path));
        outer.submitTask("path prisma creation", new PathPrismaCreationTask(path));
    }

}
