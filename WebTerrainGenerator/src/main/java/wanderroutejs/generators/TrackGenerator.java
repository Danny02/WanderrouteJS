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
package wanderroutejs.generators;

import com.fmt.gps.data.GpxFileDataAccess;
import com.fmt.gps.track.*;
import java.awt.Rectangle;
import java.io.*;
import java.net.URL;
import java.util.List;

import darwin.util.math.base.vector.Vector3;
import darwin.util.math.composits.Path;

/**
 *
 * @author simonschmidt
 */
public class TrackGenerator
{
    public static TrackGenerator fromFile(File file) throws FileNotFoundException
    {
        return TrackGenerator.fromStream(new FileInputStream(file));
    }

    public static TrackGenerator fromURl(URL url) throws IOException
    {
        return TrackGenerator.fromStream(url.openStream());
    }

    public static TrackGenerator fromStream(InputStream stream)
    {
        return new TrackGenerator(GpxFileDataAccess.getPoints(stream));
    }
    
    private final Trip trip;

    private TrackGenerator(List<TrackPoint> trackPoints)
    {
        trip = Trip.makeTrip(1, new TrackSegment(trackPoints, TrackSegment.caminarType.undef));
    }

    public Path<Vector3> getTripAsPath()
    {
        Path<Vector3> path = new Path<>();

        for (TrackPoint p : trip.getPoints()) {
            path.addPathElement(new Vector3(
                    (float) p.getLat(),
                    p.getElevation(),
                    (float) p.getLon()));

        }

        return path;
    }

    public Path<Vector3> getTripAsPath(float heightScale, float xOffset,
                                             float yOffset)
    {
        Path<Vector3> path = new Path<>();

        for (TrackPoint p : trip.getPoints()) {
            path.addPathElement(new Vector3(
                    (float) p.getLat() + xOffset,
                    p.getElevation() * heightScale,
                    (float) p.getLon() + yOffset));

        }

        return path;
    }

    public Trip getTrip()
    {
        return trip;
    }

    //TODO warum int casts und Math abs
    public Rectangle getTripBoundingBox()
    {
        TrackPoint[] minMax = trip.getMinMaxPoints();
        TrackPoint min = minMax[0];
        TrackPoint max = minMax[1];


        int x = (int) min.getLon();
        int y = (int) min.getLat();
        int width = (int) max.getLon() + 1 - x;
        int height = (int) max.getLat() + 1 - y;


        Rectangle box = new Rectangle(
                Math.abs(x),
                Math.abs(y),
                Math.abs(width),
                Math.abs(height));

        return box;
    }
}
