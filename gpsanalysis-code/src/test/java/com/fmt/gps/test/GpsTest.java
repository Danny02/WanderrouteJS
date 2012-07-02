package com.fmt.gps.test;

import java.io.File;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

import junit.framework.JUnit4TestAdapter;

import org.junit.Before;
import org.junit.Test;

import com.fmt.gps.Travelog;
import com.fmt.gps.data.GpxFileDataAccess;
import com.fmt.gps.track.Distance;
import com.fmt.gps.track.TrackPoint;
import com.fmt.gps.track.TrackSegment;
import com.fmt.gps.track.Trip;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class GpsTest extends junit.framework.TestCase {
	private static final File TEST_GPX_FILE= new File("./track.gpx");
	private List<TrackPoint> points= null;
	private StringBuffer logBuf;
	private Formatter log;
	
	public GpsTest(String testName) {
		super(testName);
	}
	
	@Before 
	protected void setUp() throws FileNotFoundException {
		points= GpxFileDataAccess.getPoints(new FileInputStream(TEST_GPX_FILE));
		
		logBuf= new StringBuffer();
		log = new Formatter(logBuf, Locale.US);
	}
	
	//@Test public void testPointsTotal() {
		//assertEquals(2272, points.size());
	//}
	
	@Test
	public void testAvgSpeeds() {
			log.format("Number of Points: %d%s",points.size(), Travelog.CR);
			log.format("Trip on %s from %s to %s%s", Travelog.DAY.format(points.get(0).getTime()), Travelog.TIME.format(points.get(0).getTime()).toLowerCase(), Travelog.TIME.format(points.get(points.size()- 1).getTime()).toLowerCase(), Travelog.CR);

			//assertEquals("Number of Points: 2272\nTrip on August 29 from 5:49pm to 7:16pm\n", logBuf.toString());
	}
	
	@Test
	public void testTrip() throws FileNotFoundException {
		final List<TrackPoint> points= GpxFileDataAccess.getPoints(new FileInputStream(Travelog.GPX_FILE));
		
		Trip trip= Trip.makeTrip(9, new TrackSegment(points, TrackSegment.caminarType.undef));
		//log(String.format("numOfSegs: %d\n", trip.getSegments().size()));
	}
	
	@Test
	public void testTravelog() throws FileNotFoundException {
		final List<TrackPoint> points= GpxFileDataAccess.getPoints(new FileInputStream(TEST_GPX_FILE));
		
		Trip.log("\n"+ Travelog.makeNarrative(points, false));
	}
	
	@Test
	public void testGpxFileDataAccess() throws FileNotFoundException {
		final List<TrackPoint> points= GpxFileDataAccess.getPoints(new FileInputStream(TEST_GPX_FILE));
		
		//System.out.println("pts: "+ points.size());
		final Trip trip= GpxFileDataAccess.getDiary(new FileInputStream(TEST_GPX_FILE));
		
		//System.out.println("pts: "+ trip.getNumberOfPoints());
	}
	
	@Test
	public void testDistance() {
		TrackPoint phone= new TrackPoint();
		phone.setLat(42.124890); phone.setLon(-71.062250);
		TrackPoint southie= new TrackPoint();
		southie.setLat(42.124891); southie.setLon(-71.062251);
		
		//haversine_km2
		double dist= Distance.getFeet(phone.getLat(), phone.getLon(), southie.getLat(), southie.getLon());
		Trip.log(dist+"");
		//haversine_km2
		dist= Distance.getFeet(phone.getLat(), phone.getLon(), southie.getLat(), southie.getLon());
		Trip.log(dist+"");
		
		//double angle = Math.atan2(p1.y - p0.y, p1.x - p0.x);
		
		//double tcl= mod(atan2(sin(lon2-lon1)*cos(lat2),cos(lat1)*sin(lat2)-sin(lat1)*cos(lat2)*cos(lon2-lon1)),2*pi)

		/*double tcl= -1.0;
		double  dlat = lat2 - lat1
				  dlon = lon2 - lon1
				  y = sin(lon2-lon1)*cos(lat2)
				  x = cos(lat1)*sin(lat2)-sin(lat1)*cos(lat2)*cos(lon2-lon1)
				  if y > 0 then
				    if x > 0 then tc1 = arctan(y/x)
				    if x < 0 then tc1 = 180 - arctan(-y/x)
				    if x = 0 then tc1 = 90
				  if y < 0 then
				    if x > 0 then tc1 = -arctan(-y/x)
				    if x < 0 then tc1 = arctan(y/x)-180
				    if x = 0 then tc1 = 270
				  if y = 0 then
				    if x > 0 then tc1 = 0
				    if x < 0 then tc1 = 180
				    if x = 0 then [the 2 points are the same]
*/
	}
	
	/**
	 * Return test suite.
	 * @return test suite
	 **/
	public static junit.framework.Test suite()  {
		return new JUnit4TestAdapter(GpsTest.class);
	}
	
	/**
	 * main().  To run test from command line.
	 * @param args
	 **/
	public static void main(String[] args) {
		//JUnitCore.runClasses(GpsTest.class);
		junit.textui.TestRunner.run(GpsTest.class);
	}
}
