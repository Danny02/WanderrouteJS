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
package wanderroutejs.examples;

import com.fmt.gps.data.GpxFileDataAccess;
import com.fmt.gps.track.*;
import java.io.InputStream;
import java.util.List;

import darwin.util.math.base.vector.Vector3;
import darwin.util.math.composits.Path;

/**
 *
 * @author daniel
 */
public class TrackExample
{
    public static void main(String[] args)
    {
        // TODO code application logic here
        InputStream in = TrackExample.class.getResourceAsStream("/examples/untreusee-1206956.gpx");
        final List points = GpxFileDataAccess.getPoints(in);

        final Trip trip = Trip.makeTrip(1, new TrackSegment(points, TrackSegment.caminarType.undef));

//        final List allSegments = trip.getSegments();

        Path<Vector3> path = new Path<>();

        for (TrackPoint p : trip.getPoints()) {
            System.out.printf("%f, %f, %d \n", p.getLat(), p.getLon(), p.getElevation());
            path.addPathElement(new Vector3(
                    (float) p.getLat(),
                    (float) p.getLon(),
                    p.getElevation() / 100f));

        }
    }
}
