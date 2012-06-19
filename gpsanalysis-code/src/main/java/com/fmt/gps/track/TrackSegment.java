package com.fmt.gps.track;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnore;

import com.fmt.gps.Travelog;

@JsonAutoDetect
public class TrackSegment {
	/** Number of points to skip for analysis. **/
	public static final int STEP= 6;
	/** margin Seconds used to determine is speed chnage occurred. **/
	public static final int SECONDS_MARGIN= 3;
	
	/** Speed in Store. **/
	public static final double STORE_SPEED_FPS= 2.0;
	/** Speed walking. **/
	public static final double WALK_SPEED_FPS= 4.0;
	/** Speed running. **/
	public static final double RUN_SPEED_FPS= 8.0;
	/** Speed bicycling. **/
	public static final double BIKE_SPEED_FPS= 20.0;
	/** speed in car in city. **/
	public static final double VEHICLE_CITY_SPEED_FPS= 30.0;
	/** speed on highway. **/
	public static final double HIGHWAY_SPEED_FPS= 45.0;
	/** speed flying. **/
	public static final double AIR_SPEED_FPS= 150.0;
	/** Minimum Track Segment size when splitting up points and segments. **/
	public static final int    MIN_SEGMENT_SIZE= 10;
	
	/** Type movement of Segment. **/
	public static enum caminarType{mullStop, pauseIndoor, vehicle, bike, run, walk, highway, flight, store, inside, carSet, car, photo, speedChange, undef};
	//public static final double BIKE_SPEED_FPS= 9.39;

	//public static final double GPS_SPEED_FPS= 0.002724;
	//public static final double MPH= 3447.136563877;
	//public static final double MPH= 3600.0;

	/** Difference in speeds tolerated between segments. **/
	public static final double TOLERANCE_MERGE= 0.15;
	/** Milliseconds in one second. **/
	public static final int LMILLIS= 1000;
	/** seconds in a minute. **/
	public static final int LMINUTE= 60;
	/** Milliseconds in one second. **/
	public static final double FMILLIS= 1000.0;
	/** seconds in a minute. **/
	public static final double FMINUTE= 60.0;
	@JsonIgnore
	/** all points in Segment. **/
	public final List<TrackPoint> points;
	/** start and stop times of this segment. **/
	public final Date start, stop;
	/** distance covered in this segment. **/
	public final double distance;

	private double calories= 0.0;	//TODO
	private caminarType type;

	/**
	 * Constructor.  Populates member variables based on points list.
	 * @param points list of points contained in this segment
	 **/
	public TrackSegment(List<TrackPoint> points, caminarType type) {
		this.points= populatePoints(points);
		this.distance= getTotalDistance(points);
		this.type= type; //setCaminarType(this.distance, points);

		this.start= (points.size() > 0) ? points.get(0).getTime() : null;
		this.stop= (points.size() > 0) ? points.get(points.size()- 1).getTime() : null;
	}
	
	/**
	 * Should this segment be split apart.
	 * @return if this segment can be analyzed and possibly split further
	 **/
	public boolean isSplitable() {
		return (type != caminarType.pauseIndoor && type != caminarType.car && 
				type != caminarType.carSet && type != caminarType.speedChange &&
				type != caminarType.mullStop &&
				points.size() > MIN_SEGMENT_SIZE);
	}
	
	/**
	 * Returns caminar type depending on speed of points.
	 * @param points list of all TrackPoints in segment
	 * @return caminar type
	 **/
	public static caminarType setCaminarType(double distance, List<TrackPoint> points) {
		if(points.size() < 3)	{
			if(distance > Trip.MULL_FEET_TOLERANCE) {
				return caminarType.inside;
			} else {
				return caminarType.pauseIndoor;
			}
		}
		
		double avgSpeed= getAvgFps(points);
		
		if(avgSpeed > AIR_SPEED_FPS)	return caminarType.flight;
		if(avgSpeed > HIGHWAY_SPEED_FPS)	return caminarType.highway;
		if(avgSpeed > VEHICLE_CITY_SPEED_FPS)	return caminarType.vehicle;
		if(avgSpeed > BIKE_SPEED_FPS)	return caminarType.bike;
		if(avgSpeed > RUN_SPEED_FPS)	return caminarType.run;
		if(avgSpeed > WALK_SPEED_FPS)	return caminarType.walk;
		if(avgSpeed > STORE_SPEED_FPS)	return caminarType.store;
		if(avgSpeed > 0.0)	return caminarType.mullStop;
		
		return caminarType.undef;
	}
	
	public static double getTotalDistance(List<TrackPoint> points) {
		double totalDistanceTravelled= 0.0;
		if(points.size() > 0) {
			totalDistanceTravelled= (points.get(0).isDistanceSet()) ? points.get(0).getDistance().getFeetFromPrevious() : 0.0;
		}
		
		for(TrackPoint pt: points) {
			if(pt.isDistanceSet())	totalDistanceTravelled+= pt.getDistance().getFeetToNext();
		}
		
		return totalDistanceTravelled;
	}

	/**
	 * Populated distance member of TrackPoints in list.
	 * @param dbPoints list of TrackPoints without distance filled in.
	 * @return list of TrackPoints with distance populated
	 **/
	public static List<TrackPoint> populatePoints(List<TrackPoint> dbPoints) {
		List<TrackPoint> popPoints= new ArrayList<TrackPoint>();
		
		if(dbPoints.size() == 0) return popPoints;
		
		TrackPoint pPrevious= dbPoints.get(0);
		TrackPoint pNext= dbPoints.get(0);
		TrackPoint pCurrent= dbPoints.get(0);
		popPoints.add(dbPoints.get(0));
		
		
		for(int i= 1; i < (dbPoints.size()- 1); i+=1 ) {
			pCurrent= dbPoints.get(i);
			pPrevious= dbPoints.get(i- 1);
			pNext= dbPoints.get(i+ 1);
			Date dCurr= pCurrent.getTime();
			Date dNext= pNext.getTime();
			Date dPrevious= pPrevious.getTime();
			
			Distance pDistance= new Distance();
			pDistance.setFeetFromPrevious(Distance.getFeet(pCurrent, pPrevious));
			pDistance.setFeetToNext(Distance.getFeet(pCurrent, pNext));
			
			pDistance.setSecondsFromPrevious(Distance.getSecondsDiff(dCurr, dPrevious));
			pDistance.setSecondsToNext(Distance.getSecondsDiff(dNext, dCurr));
			/*if(pDistance.feetFromPrevious > 0.0 && pDistance.secondsFromPrevious > 0L) {
				pDistance.fps= pDistance.feetFromPrevious/((double)pDistance.secondsFromPrevious);
			} else {
				pDistance.fps= 0.0;
			}*/
			pDistance.setFpsFromPrevious(Distance.getFps(pPrevious, pCurrent));
			pDistance.setFpsToNext(Distance.getFps(pCurrent, pNext));
			
			pCurrent.setDistance(pDistance);
			pCurrent.setDistanceSet(true);
			popPoints.add(pCurrent);
			//Trip.log(String.format(("%.4f - %d = %f\n", pCurrent.distance.distanceFromPrevious, pCurrent.distance.timeGapFromPrevious, pCurrent.distance.speed);

			
		}
		
		popPoints.add(dbPoints.get(dbPoints.size()- 1));
		
		return popPoints;
	}
	
	/**
	 * TODO: use getTimeSpan()
	 * Gets average speed across track.
	 * @param seg segment to get average speed of
	 * @return average speed of all points in segment
	 **/
	@Deprecated
	public static double getAvgFps(List<TrackPoint> points) {
		double totalSpeed= 0.0;
		double avgSpeed= 0.0;
		double totalDistance= 0.0;
		int numPoints= 0;
		
		TrackPoint prev= null;
		for(TrackPoint pt: points) {
			if(pt.isDistanceSet()) {
				if(null != prev){
					totalSpeed+= pt.getDistance().getFpsFromPrevious();
					totalDistance+= Distance.getFeet(prev, pt);
					avgSpeed+= Distance.getFps(prev, pt);	//TODO: err?
					numPoints++;
				}
			} else {
				if(null != prev) {
					totalDistance+= Distance.getFeet(prev, pt);
					//Trip.log("no distance");
					//numPoints++;
				}
			}
			prev= pt;
		}
		
		//Trip.log(String.format(("points: %d time: %d avg: %f tot: %f tot: %f\n", points.size(), getTimespan(points), avgSpeed, totalSpeed, totalDistance);
		
		//return totalDistance/((double)getTimespan(points));
		return totalSpeed/((double)numPoints);
	}
	
	/**
	 * returns seconds from start to end of this list of points.
	 * @param points list of points
	 * @return time from begining to end of list
	 */
	public static long getSeconds(List<TrackPoint> points) {
		if(points.size() == 0) return 0L;
		return (points.get(points.size()- 1).getTime().getTime()- points.get(0).getTime().getTime())/LMILLIS;
	}
	
	/**
	 * Gets list of places where one may have stopped (as if held by an anchor).
	 * @param seg segment to get achors from
	 * @return all anchors in segment
	 **/
	public static List<TrackPoint> getAnchors(TrackSegment seg) {
		List<TrackPoint> anchors= new ArrayList<TrackPoint>();
		
		for(TrackPoint pt: seg.points) {
			//if(isThisPointAnAnchorInThisList(pt, seg.points)) {
				anchors.add(pt);
			//}
		}
		
		return anchors;
	}
	
	/**
	 * returns avg speed over time.
	 * @param pts points to look at speed at
	 * @param timespan timespan between points
	 * @return average speed over the giver timespan
	 **/
	public static double speedOverTime(List<TrackPoint> pts, long startOffset, long timespan) {
		TrackPoint start, end;
		int startPos= 0;
		int endPos;
		
		final long startTime= pts.get(0).getTime().getTime()+ startOffset;
		final long endTime= startTime+ timespan;
		
		//Trip.log(String.format(("pts: %d\n", pts.size());
		//Trip.log(String.format(("TimeSpan start:%s end:%s\n", Travelog.TIME.format(new Date(startTime)), Travelog.TIME.format(new Date(endTime)));
		
		int i= 0;
		start= pts.get(i);
		while(start.getTime().getTime() < startTime) {
			start= pts.get(i++);
			startPos= i;
		}
		
		endPos= startPos;
		TrackPoint next= pts.get(i);
		while(next.getTime().getTime() < endTime) {
			next= pts.get(i++);
			endPos= i;
		}
		
		return getAvgFps(pts.subList(startPos, endPos));
	}
	
	/**
	 * Splits this TrackSegment into two.
	 * @return two TrackSegments or just one if no split is appropriate
	 **/
	public static TrackSegment[] Xsplit(List<TrackPoint> popPoints) {
		TrackSegment[] segments= new TrackSegment[2];
		
		int i= 0;
		for(TrackPoint curr: popPoints) {
			i++;
			if(null != curr.getDistance()) { 
				
				if(curr.getDistance().getSecondsFromPrevious() > Trip.TIME_PAUSE_SECONDS) {
					//Trip.log(String.format(("TPause at %s %f%s", Travelog.TIME.format(curr.getTime()), curr.distance.feetFromPrevious, Travelog.CR);
				} else {
					//Trip.log(String.format(("dist at %f, ", curr.distance.distanceFromPrevious);
				}
				if(curr.getDistance().getFeetFromPrevious() > Trip.STOP_PLACE) {
					//Trip.log(String.format(("DStop at %s %f%s", Travelog.TIME.format(curr.getTime()), curr.distance.fpsFromPrevious, Travelog.CR);
				} else {
					//Trip.log(String.format(("dist at %f, ", curr.distance.distanceFromPrevious);
				}
			}
		}
		
		//check 3 minute blocks
		final long fiveMinutes= 3* TrackSegment.LMILLIS* TrackSegment.LMINUTE;
		long startT= 0L;
		for(long s= startT; s < fiveMinutes* 15; s+= fiveMinutes) {
			double speed= TrackSegment.speedOverTime(popPoints, s, fiveMinutes);
			//Trip.log(String.format(("timespan: %d speed: %f\n", s/TrackSegment.MINUTE/TrackSegment.MILLIS, speed* TrackSegment.MPH);
		}
		
		return segments;
	}
	
	/**
	 * Split list of points into time slices
	 * @param points list of points
	 * @param timespan length of each time-slice
	 * @return list of slices of points
	 **/
	public static List<TrackSegment> splitIntoSegments(List<TrackPoint> points, long timespan) {
		List<TrackSegment> allSegs= new ArrayList<TrackSegment>();
		List<TrackPoint> remaining= points.subList(0, points.size());
		List<TrackPoint> slice= points.subList(0, points.size());
		
		long startTime= points.get(0).getTime().getTime();
		long nextSplit= startTime+ timespan;
		
		int pos= getListTimePos(points, nextSplit);
		while(-1 != pos) {
			//Trip.log(String.format(("remain: %d\n", remaining.size());
			slice= remaining.subList(0, pos);
			List<TrackPoint> full2= remaining.subList(pos, remaining.size());
			remaining= full2;
			allSegs.add(new TrackSegment(slice, caminarType.undef));
			
			startTime= remaining.get(0).getTime().getTime();
			nextSplit= startTime+ timespan;
			//Trip.log(String.format(("slice: %d remain: %d\n", slice.size(), remaining.size());
			pos= getListTimePos(remaining, nextSplit);
		}
		
		allSegs.add(new TrackSegment(remaining, caminarType.undef));
		
		return allSegs;
	}
	
	/**
	 * Gets position in list of time
	 * @param points list
	 * @param time time to look for
	 * @return position in list closest to time
	 **/
	public static int getListTimePos(List<TrackPoint> points, long time) {
		int i= 0;
		for(TrackPoint pt: points) {
			if(pt.getTime().getTime() > time) {
				return i;
			}
			i++;
		}
		
		return -1;
	}
	
	/**
	 * find gaps in space or time
	 * describe pause
	 * append to end of trkseg
	 * print out list of trksegs and the pauses inbetween
	 **/
	/**
	 * shifts points between segments depending on each segments avg speed.
	 * @param prev first segment
	 * @param middle middle segment
	 * @param next last segment
	 **/
	public static void shift(TrackSegment prev, TrackSegment middle, TrackSegment next) {
		//Trip.log(String.format(("sz->3: %d\n", prev.points.size()+ middle.points.size()+ next.points.size());
		//Trip.log(String.format(("sz->3: %d %d %d\n", prev.points.size(), middle.points.size(), next.points.size());
		
		double prevSpeed= getAvgFps(prev.points);	
		double middleSpeed= getAvgFps(middle.points);	
		double nextSpeed= getAvgFps(next.points);
		
		int trend= 0;
		TrackPoint pt;
		for(int i= 0; i < middle.points.size(); i++) {
			pt= middle.points.get(i);
			if(getDiffPercent(pt.getDistance().getFpsFromPrevious(), middleSpeed) > Trip.STOP_PLACE) {
				trend++;
			} else {
				trend= 0;
			}
		}
		
		//Trip.log(String.format(("trend: %d\n", trend);
		
		shift(middle, next, trend);
		
		for(int i= middle.points.size()- 1; i >= 0; i-= 1) {
			pt= middle.points.get(i);
			if(getDiffPercent(pt.getDistance().getFpsFromPrevious(), middleSpeed) > Trip.STOP_PLACE) {
				trend++;
			} else {
				trend= 0;
			}
		}
		
		Trip.log(String.format("trend: %d\n", trend));
		
		shift(trend, prev, middle);
		
		//Trip.log(String.format(("sz<-3: %d\n", prev.points.size()+ middle.points.size()+ next.points.size());
		//Trip.log(String.format(("sz<-3: %d %d %d\n---\n", prev.points.size(), middle.points.size(), next.points.size());
	}
	
	/**
	 * Shifts points from the end of one segment to beginning of another.
	 * @param middle segment to shift from end of
	 * @param next segment to shift points to
	 * @param fromEnd how many points to take off of end of middle segment
	 **/
	public static void shift(final TrackSegment middle, final TrackSegment next, int fromEnd) {
		if(fromEnd > 0) {
			List<TrackPoint> toShift= middle.points.subList(middle.points.size()- fromEnd, middle.points.size());
			next.points.addAll(0, toShift);
			middle.points.removeAll(toShift);
		}
	}
	/**
	 * Shifts points from the beginning of one segment to end of another.
	 * @param middle segment to shift from end of
	 * @param next segment to shift points to
	 * @param fromEnd how many points to take off of beginning of middle segment
	 **/
	public static void shift(int fromStart, final TrackSegment prev, final TrackSegment middle) {
		if(fromStart > 0) {
			List<TrackPoint> toShift= middle.points.subList(0, fromStart);
			prev.points.addAll(toShift);
			middle.points.removeAll(toShift);
		}
	}
	
	/**
	 * gives percent difference between two numbers.
	 * @param one a number
	 * @param two another number
	 * @return percent the two numbers differ
	 **/
	public static double getDiffPercent(double one, double two) {
		return (one- two)/ two;
	}

	/**
	 * Given a list of track segments, merge adjacent segments that are similar enough.
	 * @param allSegs all track segments
	 * @return reduced list of track segments
	 **/
	public static List<TrackSegment> mergeLikeSegments(List<TrackSegment> allSegs) {
		List<TrackSegment> lessSegs= new ArrayList<TrackSegment>();
		boolean setMergedOnLastLoop= false;
		
		for(int i= 1; i < allSegs.size(); i++) {
			setMergedOnLastLoop= false;
			TrackSegment prev= allSegs.get(i- 1);
			TrackSegment curr= allSegs.get(i);
			
			if(getDiffPercent(getAvgFps(prev.points), getAvgFps(curr.points)) < TOLERANCE_MERGE) {
				prev.points.addAll(curr.points);
				i++;
				if(i < allSegs.size()) setMergedOnLastLoop= true;
			}
			
			lessSegs.add(prev);
		}
		
		if(setMergedOnLastLoop) {
			lessSegs.add(allSegs.get(allSegs.size()- 1));
		}
		
		return lessSegs;
	}
	
	/**
	 * Displays all times and the seconds between them.
	 * @param points points to see
	 **/
	public static void seeTimes(List<TrackPoint> points) {
		Date previousTime= points.get(0).getTime();
		
		for(TrackPoint pt: points) {
			long diff= pt.getTime().getTime()- previousTime.getTime();
			//Trip.log(String.format(("%s: %d\n", Travelog.TTIME.format(pt.getTime()), diff/FMILLIS);
			previousTime= pt.getTime();
		}
	}
	
	/**
	 * Displays all places and distances between them.
	 * @param points points to see
	 **/
	public static void seeDistances(List<TrackPoint> points) {
		TrackPoint previousPlace= points.get(0);
		
		for(TrackPoint pt: points) {
			double diff= Distance.getFeet(previousPlace, pt);
			//Trip.log(String.format(("%s\t%f: %f\n", Travelog.TTIME.format(pt.getTime()), diff, Distance.getFps(previousPlace, pt));
			previousPlace= pt;
		}
	}
	
	/**
	 * Displays all places and distances and times between them.
	 * @param points points to see
	 **/
	public static void seeDistancesAndTimes(List<TrackPoint> points) {
		TrackPoint previousPlace= points.get(0);
		Date previousTime= points.get(0).getTime();
		//Trip.log(String.format(("%8s\t%8s\t%8s\t%s\t%8s\n", "feet", "FPS", "avgSpeed", "seconds", "time");

		for(TrackPoint pt: points) {
			double ddiff= Distance.getFeet(previousPlace, pt);
			long tdiff= pt.getTime().getTime()- previousTime.getTime();
			Trip.log(String.format("%f\t%f\t%f\t%d\t%s\n", 
					ddiff, 
					(Distance.getFps(previousPlace, pt)), 
					(getMovingAverage(3, pt.getPosition(), points)),
					tdiff/LMILLIS,
					Travelog.TTIME.format(pt.getTime())));
			previousPlace= pt;
			previousTime= pt.getTime();
		}
	}
	
	/**
	 * Get average over a longer period of time.
	 * @param secondsMargin how many seconds to look on either side of a point
	 * @param pos which position in the list to center on
	 * @param points all points
	 * @return average speed over timespan x 2
	 **/
	public static double getMovingAverage(long secondsMargin, int pos, List<TrackPoint> points) {
		List<TrackPoint> timespan= getTimespan(pos, secondsMargin, points);
		
		double fps= 0;
		int numPoints= 0;
		
		for(TrackPoint pt: timespan) {
			if(pt.isDistanceSet()) {
				fps+= pt.getDistance().getFpsFromPrevious();
				numPoints++;
			}
		}
		
		//Trip.log(String.format(("fps:%f / %d\n", fps, numPoints);
		return fps/((double)numPoints);
	}
	
	/**
	 * Returns position in list when gps points were not recorded, or -1 if none occurred
	 * @param points all points
	 * @param secondsTolerance how long a time gap to look for
	 * @return position in list when positions were not recorded for timeTolerance time, or -1 if no gaps
	 **/
	public static int didTimePauseOccur(int fromPos, List<TrackPoint> points, int secondsTolerance) {
		int i= fromPos;
		
		List<TrackPoint> pts= points.subList(fromPos, points.size());
		
		for(TrackPoint pt: pts) {
			if(pt.isDistanceSet()) {
				//Trip.log(String.format(("%d: %d--%d\n", secondsTolerance, pt.distance.secondsFromPrevious, pt.distance.secondsToNext);
				if(/*pt.distance.secondsFromPrevious > secondsTolerance || */pt.getDistance().getSecondsToNext() > secondsTolerance) {
					return i;
				}
			}
			i++;
		}
		
		return -1;
	}
	
	/**
	 * Returns position in list when speed changed dramatically, or -1 if none occurred
	 * @param points all points
	 * @param pos from which point in list to start checking
	 * @param fpsTolerance speed change tolerance
	 * @return position in list when speed changed or -1 if none occurred
	 **/
	public static int didSpeedChangeOccur(int pos, List<TrackPoint> points, double fpsTolerance) {
		//check moving average at step=moving average-> if change, then check more closely
		
		//Trip.log(String.format(("params: %d, %d\n", pos, points.size());
		
		if(points.size() < (STEP*2))	return -1;
		
		int whenDidSpeedProbablyChange= 0;
		int whenDidSpeedExactlyChange= 0;
		
		List<Double> avgSpeed= new ArrayList<Double>();
		
		//add all average moving speeds to a list
		for(int i= pos; i < points.size(); i++) {
			avgSpeed.add(getMovingAverage(SECONDS_MARGIN, i, points));
		}
		
		//not enough points
		if(avgSpeed.size() < (STEP*2))	return -1;
		
		//if speed change is above tolerance, then tag it
		for(int i= STEP* 2; i < avgSpeed.size(); i+= STEP) {
			//Trip.log(String.format(("%d chg: %f = %f - %f\n", avgSpeed.size(), Math.abs(avgSpeed.get(i- STEP)- avgSpeed.get(i)), avgSpeed.get(i- STEP), avgSpeed.get(i));
			if(Math.abs(avgSpeed.get(i- STEP)- avgSpeed.get(i)) > fpsTolerance) {
				whenDidSpeedProbablyChange= pos+ i- STEP/2;
				//Trip.log(String.format(("whenDidSpeedProbablyChange %d\n", whenDidSpeedProbablyChange);
				break;
			}
		}
		
		//if speed change occurred
		if(whenDidSpeedProbablyChange != 0 && whenDidSpeedProbablyChange != (points.size()- 1)) {
			double maxSpeedChange= 0.0;
			//get list of points around change point
			List<TrackPoint> pointsAroundChange= points.subList(Math.max(0, whenDidSpeedProbablyChange- STEP), Math.min(points.size()- 1, whenDidSpeedProbablyChange+ STEP));
			//whenDidSpeedProbablyChange= Math.max(0, whenDidSpeedProbablyChange- STEP);
			
			//get point that is most changing
			for(int i= 0; i < pointsAroundChange.size(); i++) {
				TrackPoint pt= pointsAroundChange.get(i);
				if(pt.isDistanceSet()) {
					double speedDiff=  Math.abs(pt.getDistance().getFpsFromPrevious()- pt.getDistance().getFpsToNext());
					if(speedDiff > maxSpeedChange) {
						maxSpeedChange= speedDiff;
						whenDidSpeedExactlyChange= i;
					}
				}
			}
			
			whenDidSpeedProbablyChange= Math.max(0, whenDidSpeedProbablyChange- STEP);
			//Trip.log(String.format(("chgPos %d\t%d\t%d\n", whenDidSpeedProbablyChange+ whenDidSpeedExactlyChange, whenDidSpeedExactlyChange, points.size());
			
			//return position in original list where speed changed to most
			return whenDidSpeedProbablyChange+ whenDidSpeedExactlyChange; //- STEP + pos
		}
		
		//return 0 if not change was extreme enough
		return -1;
	}
	
	/**
	 * returns whether I was mulling about in the same area for a period of time.
	 * @param pos from which point to start looking for mulling
	 * @param points all points
	 * @param feetTolerance maximum amount of feet mulling can occur in
	 * @param secondsTolerance minimum amount of time when mulling occurred
	 * @return whether mulling occurred at this position
	 */
	public static boolean amIMullingAbout(int pos, List<TrackPoint> points, double feetTolerance, double secondsTolerance) {
		//anchor at point, then see if I move feetTolerance away from that point for secondsTolerance
		final TrackPoint anchor= points.get(pos);
		
		List<TrackPoint> adjacentPoints= getDistanceSpan(pos, feetTolerance, points);
		long timespan= getSeconds(adjacentPoints);
		
		boolean mulling= (timespan > secondsTolerance);

		if(mulling) Trip.log(String.format("AdjacentPts: %d %d\t%s\t%s\n", adjacentPoints.size(), timespan, mulling ? "y" : "n", Travelog.TTIME.format(anchor.getTime())));
		
		return mulling;
	}
	
	/**
	 * Returns a list of points surrounding one at pos depending on how far apart they occurred.
	 * @param pos which point to center on
	 * @param secondsMargin margin of seconds on either side of point at pos
	 * @param pts all points
	 * @return a list of points that fall within secondsMargin of central point
	 **/
	public static List<TrackPoint> getTimespan(int pos, long secondsMargin, List<TrackPoint> pts) {
		TrackPoint start, end;
		int startPos= 0;
		int endPos;
		
		List<TrackPoint> timepoints= new ArrayList<TrackPoint>();
		
		final long startTime= pts.get(pos).getTime().getTime()- secondsMargin* LMILLIS;
		final long endTime= pts.get(pos).getTime().getTime()+ secondsMargin* LMILLIS;
		
		for(TrackPoint pt: pts) {
			if(pt.getTime().getTime() > startTime && pt.getTime().getTime() < endTime) {
				timepoints.add(pt);
			}
		}
		
		//Trip.log(String.format(("timespan %d from %s: %d\n", secondsMargin, Travelog.TIME.format(pts.get(pos).getTime()), timepoints.size());
		return timepoints;
	}
	
	/**
	 * Returns a list of points surrounding one at pos depending on whether they are with fettMargin.
	 * @param pos which point to center on
	 * @param feetMargin margin of feet on either side of point at pos
	 * @param pts all points
	 * @return a list of points that fall within feetMargin of central point
	 **/
	public static List<TrackPoint> getDistanceSpan(int pos, double feetMargin, List<TrackPoint> pts) {
		TrackPoint start, end;
		int startPos= 0;
		int endPos;
		
		List<TrackPoint> spacePoints= new ArrayList<TrackPoint>();
		
		final TrackPoint anchor= pts.get(pos);
		//final long startTime= pts.get(pos).getTime().getTime()- secondsMargin;
		//final long endTime= pts.get(pos).getTime().getTime()+ secondsMargin;
		
		/*for(TrackPoint pt: pts) {
			if(Distance.getFeet(anchor, pt) < feetMargin) {
				spacePoints.add(pt);
			}
		}*/
		
		//add anchor point
		spacePoints.add(pts.get(pos));
		int previous= pos- 1;
		int next= pos+ 1;
		boolean stillAdjacentPrevious= true;
		boolean stillAdjacentNext= true;
		
		while(stillAdjacentPrevious || stillAdjacentNext) {
			if(previous < 0 || !stillAdjacentPrevious) {
				stillAdjacentPrevious= false;
			} else {
				if(Distance.getFeet(anchor, pts.get(previous)) < feetMargin) {
					//add previous points
					spacePoints.add(0, pts.get(previous));
				} else {
					stillAdjacentPrevious= false;
				}
				previous--;
			}
			if(next >= pts.size() || !stillAdjacentNext) {
				stillAdjacentNext= false;
			} else {
				if(Distance.getFeet(anchor, pts.get(next)) < feetMargin) {
					//add subsequent points
					spacePoints.add(pts.get(next));
				} else {
					stillAdjacentNext= false;
				}
				next++;
			}
		}
		
		return spacePoints;
	}
	
	/**
	 * Returns description of all Pauses/Stops/GPS point time gap.
	 * @param points all points
	 * @return text description of all Pauses
	 **/
	public static String getAllPauses(List<TrackPoint> points) {
		String narr= "Went Indoors:\n";
		int curr= 0;
		int next= didTimePauseOccur(curr, points, 5);
		
		while(-1 != next) {
			narr+= "Indoors at "+ Travelog.TTIME.format(points.get(curr).getTime())+ Travelog.CR;
			next= didTimePauseOccur(curr, points, 10);
			curr= next+ 1;
		}
		
		return narr;
	}
	
	/**
	 * Returns description of all HangOut/Mulls.
	 * @param points all points
	 * @return text description of all Mulls
	 **/
	public static String getAllHangOuts(List<TrackPoint> points) {
		String narr= "HangOuts:\n";
		for(int curr= 0; curr < points.size(); curr++) {
			
			if(amIMullingAbout(curr, points, 100, 60)) {
				narr+= String.format("hung at %s\n", Travelog.TTIME.format(points.get(curr).getTime()));
			}
		}
		
		return narr;
	}
	
	/**
	 * Returns description of all Speed Changes.
	 * @param points all points
	 * @return text description of all speed changes
	 **/
	public static String getAllSpeedChanges(List<TrackPoint> points, double fpsChangeTolerance) {
		String narr= "Speed Changes:\n";
		int curr= 0;
		int next= didSpeedChangeOccur(curr, points, fpsChangeTolerance);
		
		while(-1 != next && 0 != next && next != (points.size()-1)) {
			narr+= "Speed Changed at "+ Travelog.TTIME.format(points.get(curr).getTime())+ Travelog.CR;
			//Trip.log(narr);
			next= didSpeedChangeOccur(curr, points, fpsChangeTolerance);
			curr= next+ 1;
		}
		
		return narr;
	}
	
	/**
	 * Splits list of TrackPoints into a list of 3 TrackSegments or 0 TrackSegments if no mulling about in one place occurred.
	 * The 3 items in the list returned are: 0) TrackSegment of points before mulling about in one place 1) TrackSegment of two points where the mulling about in one place occurred 2) TrackSegment of points after the mulling about in one place.
	 * @param points all points
	 * @return list of TrackSegments split based upon where mulling about in one place occurred, or a 0-item list if no split occurred
	 **/
	public static List<TrackSegment> splitListByMull(List<TrackPoint> pts, double feetTolerance, long secondsTolerance) {
		boolean splitList= false;
		List<TrackSegment> lists= new ArrayList<TrackSegment>();
		List<TrackPoint> points= pts.subList(0, pts.size());
		
		//Trip.log(String.format(("getAllHangOutLists: %d\n", pts.size());
		
		for(int curr= 0; curr < points.size(); curr++) {
			
			if(amIMullingAbout(curr, points, feetTolerance, secondsTolerance)) {
				List<TrackPoint> adjacentPoints= getDistanceSpan(curr, feetTolerance, points);
				//List<TrackPoint> debugList= getDistanceSpan(0, feetTolerance, adjacentPoints);
				//Travelog.seeList(debugList, "getAllHangOutLists::debugList");
				//Travelog.seeList(adjacentPoints, "getAllHangOutLists::adjacentPoints");
				if(adjacentPoints.size() != points.size()) {
					
					
					TrackSegment mullSeg= new TrackSegment(adjacentPoints, caminarType.mullStop);
					/*lists.add(mullSeg);
					points.removeAll(adjacentPoints);
					lists.add(new TrackSegment(points, caminarType.undef));
					splitList= true;
					*/
					
					List<TrackPoint> firstList= points.subList(0, curr);
					List<TrackPoint> pauseList= points.subList(curr, curr+ mullSeg.points.size());
					List<TrackPoint> secondList= points.subList(curr+ mullSeg.points.size(), points.size());
					lists.add(new TrackSegment(firstList, caminarType.undef));
					lists.add(new TrackSegment(pauseList, caminarType.mullStop));
					lists.add(new TrackSegment(secondList, caminarType.undef));

				}
				break;
			}
		}
		
		return lists;
	}
	
	/**
	 * Splits list of TrackPoints into a list of 3 TrackSegments or 0 TrackSegments if no speed change occurred.
	 * The 3 items in the list returned are: 0) TrackSegment of points before speed change 1) TrackSegment of two points where the speed change occurred 2) TrackSegment of points after speed change.
	 * @param points all points
	 * @return list of TrackSegments split based upon speed changes, or a 0-item list if no split occurred
	 **/
	public static List<TrackSegment> splitListBySpeedChange(List<TrackPoint> points, double fpsChangeTolerance) {
		int curr= 0;
		List<TrackSegment> lists= new ArrayList<TrackSegment>();
		
		int next= didSpeedChangeOccur(curr, points, fpsChangeTolerance);
		if(-1 != next && 1 < next && next != (points.size()- 2)) {
			/*Trip.log(String.format(("next: %d, size: %d\n", next, points.size());
			List<TrackPoint> firstList= points.subList(0, next- 1);
			List<TrackPoint> secondList= points.subList(next, points.size()- 1);
			lists.add(new TrackSegment(firstList, caminarType.undef));
			lists.add(new TrackSegment(secondList, caminarType.undef));*/
			//Trip.log(String.format(("next: %d, size: %d\n", next, points.size());
			List<TrackPoint> firstList= points.subList(0, next);
			List<TrackPoint> pauseList= points.subList(next, next+2);
			List<TrackPoint> secondList= points.subList(next+ 2, points.size());
			lists.add(new TrackSegment(firstList, caminarType.undef));
			lists.add(new TrackSegment(pauseList, caminarType.speedChange));
			lists.add(new TrackSegment(secondList, caminarType.undef));
		}
		
		return lists;
	}

	/**
	 * Splits list of TrackPoints into a list of 3 TrackSegments or 0 TrackSegments if no interruptions in time occurred.
	 * The 3 items in the list returned are: 0) TrackSegment of points before time interruption 1) TrackSegment of two points where the time interruption occurred 2) TrackSegment of points after time interruption.
	 * @param points all points
	 * @return list of TrackSegments split based upon time interruptions, or a 0-item list if no split occurred
	 **/
	public static List<TrackSegment> splitListByTimeInterruption(List<TrackPoint> points, int secondsGapTolerance) {
		int curr= 0;
		List<TrackSegment> lists= new ArrayList<TrackSegment>();
		
		int next= didTimePauseOccur(curr, points, secondsGapTolerance);
		//Trip.log(String.format(("nnext: %d, size: %d\n", next, points.size());
		if(-1 != next && 1 < next && next != (points.size()- 2)) {
			//Trip.log(String.format(("next: %d, size: %d\n", next, points.size());
			List<TrackPoint> firstList= points.subList(0, next);
			List<TrackPoint> pauseList= points.subList(next, next+2);
			List<TrackPoint> secondList= points.subList(next+ 2, points.size());
			lists.add(new TrackSegment(firstList, caminarType.undef));
			lists.add(new TrackSegment(pauseList, caminarType.pauseIndoor));
			lists.add(new TrackSegment(secondList, caminarType.undef));
		}
		
		return lists;
	}
	
	/**
	 * formats timespan of TrackFormatinto readble format.
	 * @param ts TrackFormatinto
	 * @return string describing time
	 **/
	private static String formatTime(TrackSegment ts) {
		final long timespan= TrackSegment.getSeconds(ts.points);
		if(timespan < TrackSegment.LMINUTE)	return "for less than a minute";
		if(timespan < TrackSegment.LMINUTE*TrackSegment.LMINUTE)	return String.format("for %d minutes", timespan/TrackSegment.LMINUTE);
		return "until "+ Travelog.TIME.format(ts.stop);
	}

	/**
	 * transform type of segment into readble string.
	 * @param type type of segment
	 * @return readble version of type
	 **/
	private static String getMovementDesc(caminarType type) {
		switch(type) {
		case walk:
			return "Walked";
		case run:
			return "Ran";
		case vehicle:
			return "Drove";
		case pauseIndoor:
			return "Stoppped";
		case highway:
			return "Drove on Highway";
		case inside:
			return "Went inside";
		case flight:
			return "Flew";
		case bike:
			return "Bicycled";
		case store:
			return "Shopped";
		case car:
		case carSet:
			return "Parked my Car";
		case mullStop:
			return "Hung Out";
		case speedChange:
			return "chnaged speeds";
		case photo:
			return "took a photo";
		case undef: 
		default:
			return "Did nothing for";
		}
	}

	/**
	 * formats distance into readable string.
	 * @param totalDistance distance to format
	 * @return readable string
	 **/
	public static String formatDistance(double totalDistance) {
		if(totalDistance < 400.0)	return String.format("%.3f %s", totalDistance, Trip.USE_KM ? "meters" : "feet");
		return String.format("%.3f %s", totalDistance/(Trip.USE_KM ? Distance.M_PER_KM : Distance.FEET_PER_MILE), Trip.USE_KM ? "km" : "miles");
	}
	
	/**
	 * returns central point of all points in segment.
	 * @param pts all points
	 * @return central point of segment, or null if not mulling about
	 **/
	public static TrackPoint areAllPointsTheSamePlace(List<TrackPoint> pts) {
		if(amIMullingAbout(0, pts, Trip.MULL_FEET_TOLERANCE, Trip.MULL_SECONDS_TOLERANCE)) {
			return getCentralPoint(pts);
		}
		
		return null;
	}
	
	/**
	 * Gets central point of list of points.
	 * @param pts all points
	 * @return central point in list of points
	 **/
	public static TrackPoint getCentralPoint(List<TrackPoint> pts) {
		double allLats= 0.0;
		double allLons= 0.0;
		
		for(TrackPoint pt : pts) {
			allLats+= pt.getLat();
			allLons+= pt.getLon();
		}
		
		TrackPoint centralPt= new TrackPoint();
		centralPt.setLat(allLats/((double)pts.size()));
		centralPt.setLat(allLons/((double)pts.size()));
		
		return centralPt;
	}
	
	/**
	 * @return the calories
	 */
	public double getCalories() {
		return calories;
	}

	/**
	 * @param calories the calories to set
	 */
	public void setCalories(double calories) {
		this.calories = calories;
	}

	/**
	 * @return the type
	 */
	public caminarType getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(caminarType type) {
		this.type = type;
	}
}

/*Yahoo GeoCode API result:
<ResultSet version="1.0">
<Error>0</Error>
<ErrorMessage>No error</ErrorMessage>
<Locale>us_US</Locale>
<Quality>99</Quality>
<Found>1</Found>
<Result>
<quality>99</quality>
<latitude>38.898717</latitude>
<longitude>-77.035974</longitude>
<offsetlat>38.898717</offsetlat>
<offsetlon>-77.035974</offsetlon>
<radius>500</radius>
<name>38.898717, -77.035974</name>
<line1>1600 Pennsylvania Ave NW</line1>
<line2>Washington, DC  20006</line2>
<line3/>
<line4>United States</line4>
<house>1600</house>
<street>Pennsylvania Ave NW</street>
<xstreet/>
<unittype/>
<unit/>
<postal>20006</postal>
<neighborhood/>
<city>Washington</city>
<county>District of Columbia</county>
<state>District of Columbia</state>
<country>United States</country>
<countrycode>US</countrycode>
<statecode>DC</statecode>
<countycode>DC</countycode>
<hash/>
<woeid>12765843</woeid>
<woetype>11</woetype>
<uzip>20006</uzip>
</Result>
</ResultSet>
*/