/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.alarie.osmxmlreader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;

/**
 *
 * @author simonschmidt
 */
public class OSMFileParser {

    public Collection<OSMWayObject> parse(InputStream file) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        SAXParser parser = parserFactory.newSAXParser();

        OsmTypesHandler handler = new OsmTypesHandler();
        parser.parse(file, handler);

        return handler.getOsmObjects();
    }
}
