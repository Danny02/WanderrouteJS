package com.fmt.gps.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.fmt.gps.track.Distance;
import com.fmt.gps.track.TrackPoint;
import com.fmt.gps.track.TrackSegment;
import com.fmt.gps.track.Trip;

public class GpxFileDataAccess {
	/** default elevation.  Elevatoin not currently read. **/
	private static final int ELEVATION= 30;
	

	/**
	 * Given Trip, return GPX Xml as a string.
	 * @param trip entire trip
	 * @param diary always false: used to augment data
	 * @return GPX XML String
	 **/
	public static String makeGpxXml(Trip trip, boolean diary) {
		StringBuffer buf= new StringBuffer();
		buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
		buf.append("<?xml-stylesheet type=\"text/xsl\" href=\"details.xsl\"?>\n");
		buf.append("<gpx version=\"1.1\" creator=\"GpsDiary\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.topografix.com/GPX/1/1\" xmlns:topografix=\"http://www.topografix.com/GPX/Private/TopoGrafix/0/1\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd http://www.topografix.com/GPX/Private/TopoGrafix/0/1 http://www.topografix.com/GPX/Private/TopoGrafix/0/1/topografix.xsd\">\n");
		buf.append("<trk>\n");
		buf.append("<desc><![CDATA[]]></desc>\n");
		buf.append("<number>"+trip.getNumberOfPoints()+"</number>\n");
		buf.append("<extensions><topografix:color>c0c0c0</topografix:color></extensions>\n");
		for(TrackSegment seg: trip.getSegments()) {
			buf.append("\t<trkseg>\n");
			if(diary) { 
				buf.append("\t<type>"+seg.getType().name()+"</type>\n");
				
			}
			for(TrackPoint pt: seg.points) {
				buf.append("\t\t<trkpt lat=\""+pt.getLat()+"\" lon=\""+pt.getLon()+"\">\n");
				buf.append("\t\t<ele>"+ELEVATION+"</ele>\n");
				buf.append("\t\t<time>"+Distance.getGpxDateFormatter().format(pt.getTime())+"</time>\n");
				buf.append("\t\t</trkpt>\n");
			}
			buf.append("\t</trkseg>\n");
		}
		buf.append("</trk>\n");
		buf.append("</gpx>\n");
		
		return buf.toString();
	}
	
	
	/**
	 * Convenience method.  Gets FileOutputStream given a File.
	 * @param gpxFile which file
	 * @return FileOutputStream to file
	 **/
	public static FileOutputStream getFileOutputStream(File gpxFile) {
		try {
			FileOutputStream fos = new FileOutputStream(gpxFile);
			return fos;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Returns a list of all TrackPoints from file
	 * @param is InputStream to GPX data
	 * @return List of all TrackPoints in file
	 **/
	public static List<TrackPoint> getPoints(InputStream is) {
		List<TrackPoint> points= null;

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			Document dom = builder.parse(is);
			Element root = dom.getDocumentElement();
			NodeList items = root.getElementsByTagName("trkpt");

		    //System.out.println(items.getLength());
		    
		    points= new ArrayList<TrackPoint>();

			//extract data into Point
			for(int j= 0; j < items.getLength(); j+= 1) {
				final Node item = items.item(j);
				NamedNodeMap attrs= item.getAttributes();
				final NodeList props= item.getChildNodes();

				final TrackPoint pt= new TrackPoint();
				pt.setPosition(j);
				//get lat and long
				pt.setLat(Double.parseDouble(attrs.getNamedItem("lat").getTextContent()));
				pt.setLon(Double.parseDouble(attrs.getNamedItem("lon").getTextContent()));

				//get year
				for(int k= 0; k < props.getLength(); k++) {
					Node item2= props.item(k);
					String name = item2.getNodeName();
					if (name.equalsIgnoreCase("time")){
						try {
							pt.setTime(Distance.getGpxDateFormatter().parse(item2.getFirstChild().getNodeValue()));
						} catch (ParseException ex) {
							ex.printStackTrace();
						}
					}
                                        
                                        
                                        // @author Simon Schmidt
                                        // read elevation if available and append it to the point
                                        if (name.equalsIgnoreCase("ele")){
                                            double elevationInMeters = Double.valueOf(item2.getFirstChild().getNodeValue());
                                            pt.setElevation((int) (elevationInMeters * 100));
					}
				}

				points.add(pt);
			}

			is.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			//} catch(final XPathExpressionException ex) {
		} catch(final ParserConfigurationException ex) {
		} catch(final SAXException ex) {}
		
		return points;
	}
	
	/**
	 * Returns a list of all TrackPoints from file
	 * @param gpxFile which file
	 * @return List of all TrackPoints in file
	 **/
	public static List<TrackPoint> getPoints(File gpxFile) {
		List<TrackPoint> points= null;

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();


			FileInputStream fis = new FileInputStream(gpxFile);
			Document dom = builder.parse(fis);
			Element root = dom.getDocumentElement();
			NodeList items = root.getElementsByTagName("trkpt");

		    //System.out.println(items.getLength());
		    
		    points= new ArrayList<TrackPoint>();

			//extract data into Point
			for(int j= 0; j < items.getLength(); j+= 1) {
				final Node item = items.item(j);
				NamedNodeMap attrs= item.getAttributes();
				final NodeList props= item.getChildNodes();

				final TrackPoint pt= new TrackPoint();
				pt.setPosition(j);
				//get lat and long
				pt.setLat(Double.parseDouble(attrs.getNamedItem("lat").getTextContent()));
				pt.setLon(Double.parseDouble(attrs.getNamedItem("lon").getTextContent()));

				//get year
				for(int k= 0; k < props.getLength(); k++) {
					Node item2= props.item(k);
					String name = item2.getNodeName();
					if (name.equalsIgnoreCase("time")){
						try {
							pt.setTime(Distance.getGpxDateFormatter().parse(item2.getFirstChild().getNodeValue()));
						} catch (ParseException ex) {
							ex.printStackTrace();
						}
					}
                                        
                                        // @author Simon Schmidt
                                        // read elevation if available and append it to the point
                                        if (name.equalsIgnoreCase("ele")){
                                            double elevationInMeters = Double.valueOf(item2.getFirstChild().getNodeValue());
                                            pt.setElevation((int) (elevationInMeters * 100));
					}
				}

				points.add(pt);
			}

			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			//} catch(final XPathExpressionException ex) {
		} catch(final ParserConfigurationException ex) {
		} catch(final SAXException ex) {}
		
		return points;
	}
	
	/**
	 * Returns diary from xml file.
	 * @param gpxFile xml file
	 * @return populated Trip object
	 **/
	public static Trip getDiary(File gpxFile) {
		Trip trip= new Trip(-1);
		int position= 0;

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();


			FileInputStream fis = new FileInputStream(gpxFile);
			Document dom = builder.parse(fis);
			Element root = dom.getDocumentElement();
			//<trkseg>
			NodeList xmlTrksegs = root.getElementsByTagName("trkseg");

		    //System.out.println(items.getLength());

			//extract data into Point
			//<trkseg>
			for(int j= 0; j < xmlTrksegs.getLength(); j+= 1) {
				//<trkseg>
				final Node xmlSeg = xmlTrksegs.item(j);
				final NamedNodeMap attrs= xmlSeg.getAttributes();
				final NodeList trksegChildren= xmlSeg.getChildNodes();
				final List<TrackPoint> pts= new ArrayList<TrackPoint>();
				TrackSegment.caminarType segType= TrackSegment.caminarType.undef;
				String textDescription= null;

//				<trkseg>
//				<trkpt lat="42.12489" lon="-71.06225">
//				<ele>30</ele>
//				<time>2011-08-29T15:14:55Z</time>
//				</trkpt>
				//
				for(int k= 0; k < trksegChildren.getLength(); k++) {
					final Node xmlTrkPt= trksegChildren.item(k);
					final String trkptName = xmlTrkPt.getNodeName();
					//System.out.println("nn:"+ trkptName);
					//<trkpt>
					if (trkptName.equalsIgnoreCase("trkpt")) {
						final TrackPoint pt= new TrackPoint();
						pt.setPosition(position++);
						final NamedNodeMap trkptAttrs= xmlTrkPt.getAttributes();
						pt.setLat(Double.parseDouble(trkptAttrs.getNamedItem("lat").getTextContent()));
						pt.setLon(Double.parseDouble(trkptAttrs.getNamedItem("lon").getTextContent()));
						
						final NodeList trkptChildren= xmlTrkPt.getChildNodes();
						for(int m= 0; m < trkptChildren.getLength(); m++) {
							Node trkptChild= trkptChildren.item(m);
							String trkptChildName = trkptChild.getNodeName();
							//System.out.println("n:"+ trkptChildName);
							if (trkptChildName.equalsIgnoreCase("time")) {
								try {
									//System.out.println("v:"+ trkptChild.getTextContent());
									pt.setTime(Distance.getGpxDateFormatter().parse(trkptChild.getTextContent()));
								} catch (ParseException e) {
									e.printStackTrace();
								}
							}
						}
						pts.add(pt);
					}
					if(trkptName.equalsIgnoreCase("type")) {
						segType= TrackSegment.caminarType.valueOf(xmlTrkPt.getTextContent());
					}
					if(trkptName.equalsIgnoreCase("text-description")) {
						textDescription= xmlTrkPt.getTextContent();
					}
				}

				final TrackSegment seg= new TrackSegment(pts, segType);
				
				trip.getSegments().add(seg);
			}

			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			//} catch(final XPathExpressionException ex) {
		} catch(final ParserConfigurationException ex) {
		} catch(final SAXException ex) {}
		
		return trip;
	}
}
