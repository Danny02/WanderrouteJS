package de.alarie.osmxmlreader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.SortedSet;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import static org.apache.commons.cli.OptionBuilder.withArgName;
import org.xml.sax.SAXException;
/**
 * Hello world!
 *
 */
public class App 
{
    
    public static final Options options = new Options();
    static {
        options.addOption(withArgName("file").hasArg().isRequired().
                          withDescription("The output file to write to").
                          withLongOpt("file-path").create("f"));

        options.addOption(withArgName("type-file").hasArg().
                          withDescription("The output file to write the tag types to").
                          withLongOpt("file-path").create("t"));

        options.addOption("h", "help", false, null);
    }
    
    public void App() {
    }
    
    public static void main( String[] args ) throws Exception {
        CommandLineParser parser = new PosixParser();
        App app = new App();
        PrintStream outStream;
        try {
            CommandLine line =  parser.parse(options, args);
            if(line.hasOption('h'))
            {
               // printUsage();
                System.exit(0);
            }

            File file = new File(line.getOptionValue('f'));
            

            try {
                app.downloadSample(file);
            }
            catch (IOException ex) {
                System.out.println("Error reading/writing osm file");
            }
			
			String outFile = line.getOptionValue('t');
            
            
            outStream = (outFile != null) ? new PrintStream(new File(outFile)) : System.out;
				
            OsmTypesHandler handler = app.createTypeList(file);
            app.printData(handler.getTypes(), outStream);
			/*
			outStream = (outFile != null) ? new PrintStream(new File("highways." + outFile)) : System.out;
			app.printData(handler.getHighwayTypes(), outStream);
			
			outStream = (outFile != null) ? new PrintStream(new File("landuse." + outFile)) : System.out;
			app.printData(handler.getLanduseTypes(), outStream);
			
			outStream = (outFile != null) ? new PrintStream(new File("natural." + outFile)) : System.out;
			app.printData(handler.getNaturalTypes(), outStream);
			
			outStream = (outFile != null) ? new PrintStream(new File("waterway." + outFile)) : System.out;
			app.printData(handler.getWaterwayTypes(), outStream);
			*/
			outStream = (outFile != null) ? new PrintStream(new File("objects." + outFile)) : System.out;
			ArrayList<OSMObject> objects = handler.getObjects();
			for (OSMObject obj : objects) {
				outStream.println(obj.toString());
			}

        } catch (ParseException ex) {
            System.out.println(ex.getLocalizedMessage());
            //printUsage();
            System.exit(1);
        }
        
    }
    
    
    public OsmTypesHandler createTypeList(File file) throws ParserConfigurationException, SAXException, IOException {
        return new OSMFileParser().parse(file);
    }
        
    public void downloadSample(File file) throws IOException {    
        URL downloadUrl = new URL(
            "http://api.openstreetmap.org/api/0.6/map?bbox=11.90969,50.31386,12.0000,50.41386");

        InputStream in = downloadUrl.openStream();
        OutputStream out = new FileOutputStream(file);

        byte[] buffer = new byte[10000];
        try {
            int len = in.read(buffer);
            while (len > 0) {
                out.write(buffer, 0, len);
                len = in.read(buffer);
            }
        } finally {
            out.close();
            in.close();
        }       
    }
    
    
    private void printData(SortedSet<String> nameSet, PrintStream out) {
        for (String name : nameSet) {
            out.println(name);
        }
    }
}
