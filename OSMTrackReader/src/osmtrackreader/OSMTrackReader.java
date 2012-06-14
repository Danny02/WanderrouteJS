/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package osmtrackreader;

import com.fmt.gps.data.GpxFileDataAccess;
import com.fmt.gps.track.Point;
import com.fmt.gps.track.TrackPoint;
import com.fmt.gps.track.TrackSegment;
import com.fmt.gps.track.Trip;
import darwin.util.math.base.Line;
import darwin.util.math.base.vector.Vector3;
import java.io.File;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author simonschmidt
 */
public class OSMTrackReader {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here

        final List points = GpxFileDataAccess.getPoints(new File("./track.gpx"));

        final Trip trip = Trip.makeTrip(1,
                new TrackSegment(points, TrackSegment.caminarType.undef));

        final List allSegments = trip.getSegments();

        final List<TrackPoint> allPoints = trip.getPoints();

        ListIterator<TrackPoint> i;
        i = allPoints.listIterator();


        // TODO: Path erstellen und dann folgende Vektoren in den Pfad aufnehmen

        while (i.hasNext()) {
            TrackPoint p = (TrackPoint) i.next();
            System.out.printf("%f, %f, %d \n", p.getLat(), p.getLon(), p.getElevation());
            Vector3 vec = new Vector3(
                    (float) p.getLat(),
                    (float) p.getLon(),
                    p.getElevation() / 100f);

        }
    }
}
