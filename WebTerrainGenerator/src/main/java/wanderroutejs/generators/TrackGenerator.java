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
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import darwin.util.math.base.vector.Vector3;
import darwin.util.math.composits.Path;

/**
 *
 * @author simonschmidt
 */
public class TrackGenerator {

    public static TrackGenerator fromFile(File file) {
        TrackGenerator generator = null;
        try {
            InputStream in = new FileInputStream(file);
            generator = TrackGenerator.fromStream(in);
        } catch (FileNotFoundException ex) {
            System.err.println("File not found: " + file.getPath());
        }
        return generator;

    }

	public static TrackGenerator fromStream(InputStream stream) {
        return new TrackGenerator(GpxFileDataAccess.getPoints(stream));
    }

    public static TrackGenerator fromURl (URL url) {
        throw new NotImplementedException();
    }
    private List<TrackPoint> trackPoints;
    private Trip trip;
    private Path<Vector3> path;

    private TrackGenerator(List<TrackPoint> trackPoints)
    {
        this.trackPoints = trackPoints;
    }

    public TrackGenerator makeTrip()
    {
        trip = Trip.makeTrip(1, new TrackSegment(trackPoints, TrackSegment.caminarType.undef));

        path = new Path<>();

        for (TrackPoint p : trip.getPoints()) {
            path.addPathElement(new Vector3(
                    (float) p.getLat(),
                    (float) p.getLon(),
                    p.getElevation() / 100f));

        }

        return this;
    }

    public Path<Vector3> getTripAsPath()
    {
        return this.path;
    }

    public Rectangle getTripBoundingBox()
    {
        List<TrackPoint> corners = trip.getMinMaxPoints();

        int x = (int) ((TrackPoint) corners.get(0)).getLon(),
            y = (int) ((TrackPoint) corners.get(0)).getLat(),
            width = (int) ((TrackPoint) corners.get(1)).getLon() + 1 - x,
            height = (int) ((TrackPoint) corners.get(1)).getLat() + 1 - y;


        Rectangle box = new Rectangle(
                Math.abs(x),
                Math.abs(y),
                Math.abs(width),
                Math.abs(height));

        return box;
    }
}
