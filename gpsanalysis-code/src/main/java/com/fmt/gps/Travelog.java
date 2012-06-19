package com.fmt.gps;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

import com.fmt.gps.track.Distance;
import com.fmt.gps.track.TrackPoint;
import com.fmt.gps.track.TrackSegment;
import com.fmt.gps.track.TrackSegment.caminarType;
import com.fmt.gps.track.Trip;

public class Travelog {

	/** Sample input file. **/
	//public static final File GPX_FILE= new File("/home/ftaylor92/tmp/test-speed-change.gpx");
	//public static final File GPX_FILE= new File("/home/ftaylor92/tmp/test-mull.gpx");
	//public static final File GPX_FILE= new File("/home/ftaylor92/tmp/Ri.gpx");
	//public static final File GPX_FILE= new File("/home/ftaylor92/tmp/test-split-time.gpx");
	//public static final File GPX_FILE= new File("/home/ftaylor92/tmp/test-time.gpx");
	//public static final File GPX_FILE= new File("/home/ftaylor92/tmp/70mph.gpx");
	//public static final File GPX_FILE= new File("/home/ftaylor92/tmp/runcar.gpx");
	//public static final File GPX_FILE= new File("/home/ftaylor92/tmp/fritters.gpx");
	//public static final File GPX_FILE= new File("/home/ftaylor92/tmp/Home.gpx");
	public static final File GPX_FILE= new File("./track.gpx");
	//public static final File GPX_FILE= new File("/home/ftaylor92/tmp/70twice.gpx");


	/** line-feed character.  Can be &lt;br/&gt; if web output. **/
	public static final String CR= "\n";
	
	/** Date formatter: MMMMM d. **/
	public static final SimpleDateFormat DAY= new SimpleDateFormat("MMMMM d");
	/** Date formatter: dd-MMMMM-yyyy'.gpx'. **/
	public static final SimpleDateFormat DFILE= new SimpleDateFormat("dd-MMMMM-yyyy'.gpx'");
	/** Date formatter: dd-MMMMM-yyyy'.tvl. **/
	public static final SimpleDateFormat DTFILE= new SimpleDateFormat("dd-MMMMM-yyyy'.tvl'");
	/** Date formatter: h:mma. **/
	public static final SimpleDateFormat TIME= new SimpleDateFormat("h:mma");
	/** Date formatter: h:mm:ss. **/
	public static final SimpleDateFormat TTIME= new SimpleDateFormat("h:mm:ss");

	/**
	 * Makes a Narrative out of GPS points
	 * @param points GPS points
	 * @param html whether test should be in HTML format
	 * @return narrative text
	 **/
	public static String makeNarrative(List<TrackPoint> points, boolean html) {
		StringBuffer logBuf= new StringBuffer();
		Formatter log = new Formatter(logBuf, Locale.US);
		
		TrackSegment trkseg= new TrackSegment(points, caminarType.undef);
		
		//print all time diffs
		//TrackSegment.seeTimes(trkseg.points);
		//TrackSegment.seeDistances(trkseg.points);
		TrackSegment.seeDistancesAndTimes(trkseg.points);
		Trip.log("-------");
		//print all speeds
		for(TrackPoint tp: trkseg.points) {
//			Trip.log(String.format(("speed: %f\t%s%s", tp.distance.speed* TrackSegment.MPH, TIME.format(tp.getTime()), CR);
		}
		
		//getASegmentFromList();
		//take an average from the middle, then expand it if possible
		final int middle= trkseg.points.size()/2- 300;
		final int RANGE= 20;
		final long SUSTAIN= 30;
		final long STEP= 3;
		List<TrackPoint> seg= trkseg.points.subList(middle- RANGE, middle+ RANGE);
		final double avgSpeed= TrackSegment.getAvgFps(seg);
		Trip.log(String.format("Avg: %f%s", avgSpeed, CR));
		int i;
		TrackPoint prev= seg.get(0);
		for(i= 1; i < (seg.size()-1); i+= STEP) {
			//Trip.log(seg.get(i).toString());
			TrackPoint curr= seg.get(i);
			double diffSpeed= avgSpeed- Distance.getFps(prev, curr);
//			Trip.log(String.format(("diffFromAvg: %f %.4f%s", diffSpeed, (diffSpeed/avgSpeed)*100.0, CR);
			prev= seg.get(i);
		}
		
		List<TrackPoint> seg2= trkseg.points.subList(100, 1000);
		for(int t= 10; t < 1000; t+=10) {
			//double speed= TrackSegment.speedOverTime(seg2, 0, t* 1000);
			//Trip.log(String.format(("timespan: %d speed: %f\n", t, speed);
		}
		
		//check 3 minute blocks
		final long fiveMinutes= 3* TrackSegment.LMILLIS* TrackSegment.LMINUTE;
		long startT= 0L;
		for(long s= startT; s < fiveMinutes* 15; s+= fiveMinutes) {
			double speed= TrackSegment.speedOverTime(trkseg.points, s, fiveMinutes);
			//Trip.log(String.format(("timespan: %d speed: %f\n", s/TrackSegment.MINUTE/TrackSegment.MILLIS, speed* TrackSegment.MPH);
		}
		
		//----------------
		
		List<TrackSegment> allSegs= TrackSegment.splitIntoSegments(trkseg.points, fiveMinutes);
		int sz= 0;
		for(TrackSegment aSeg: allSegs) {
			//Trip.log(String.format(("sz: %d sz: %d\n", aSeg.points.size(), allSegs.size());
			sz+= aSeg.points.size();
		}
		
		for(int g= 1; g < (allSegs.size()- 1); g++) {
			TrackSegment.shift(allSegs.get(g- 1), allSegs.get(g), allSegs.get(g+ 1));
		}
		
		sz= 0;
		sz+= allSegs.get(0).points.size();
		sz+= allSegs.get(allSegs.size()-1).points.size();
		Trip.log(String.format("segs: %d\n", allSegs.size()));
		for(int g= 1; g < (allSegs.size()- 1); g++) {
			sz+= allSegs.get(g).points.size();
			Trip.log(String.format("avgSpeed: %f diff: %f\n", 
					TrackSegment.getAvgFps(allSegs.get(g).points),
					TrackSegment.getDiffPercent(TrackSegment.getAvgFps(allSegs.get(g).points), TrackSegment.getAvgFps(allSegs.get(g- 1).points))));
		}
		
		Trip.log(String.format("sz: %d size: %d\n", sz, trkseg.points.size()));
		
		//TODO: merge like segments
		List<TrackSegment> lessSegs= TrackSegment.mergeLikeSegments(allSegs);
//		List<TrackSegment> lessSegs2= TrackSegment.mergeLikeSegments(lessSegs);
//		lessSegs= TrackSegment.mergeLikeSegments(lessSegs2);
		//lessSegs2= TrackSegment.mergeLikeSegments(lessSegs);
		//lessSegs= TrackSegment.mergeLikeSegments(lessSegs2);
		//lessSegs2= TrackSegment.mergeLikeSegments(lessSegs);
		//lessSegs= TrackSegment.mergeLikeSegments(lessSegs2);
		
		Trip.log(String.format("segs: %d\n", lessSegs.size()));
		sz= 0;
		sz+= lessSegs.get(0).points.size();
		sz+= lessSegs.get(lessSegs.size()-1).points.size();
		for(int g= 1; g < (lessSegs.size()- 1); g++) {
			Trip.log(String.format("avgSpeed: %f diff: %f\n", 
					TrackSegment.getAvgFps(lessSegs.get(g).points),
					TrackSegment.getDiffPercent(TrackSegment.getAvgFps(lessSegs.get(g).points), TrackSegment.getAvgFps(lessSegs.get(g- 1).points))));
			sz+= lessSegs.get(g).points.size();
		}
		
		Trip.log(String.format("sz: %d size: %d\n", sz, trkseg.points.size()));
		
		Trip.log(TrackSegment.getAllPauses(trkseg.points));
		Trip.log(TrackSegment.getAllHangOuts(trkseg.points));
		Trip.log(TrackSegment.getAllSpeedChanges(trkseg.points, Trip.FPS_SPEED_CHANGE_TOLERANCE));
		
		Trip.log("---");
		final Trip tripSegs= Trip.makeTrip(-1, trkseg);
		//-----------------
		Trip.log("---Display Trip---");
		
		int episode= 0;		
		
		return logBuf.toString();
	}
	
	/**
	 * for debug: outputs list descriptions.
	 * @param pts list of points
	 * @param name name of list
	 **/
	public static void seeList(List<TrackPoint> pts, String name) {
		System.out.print(name+ " "+ pts.size()+ ": ");
		for(TrackPoint tp: pts) {
			System.out.print(tp.getPosition()+", ");
		}
		System.out.print("\n");
	}
}
