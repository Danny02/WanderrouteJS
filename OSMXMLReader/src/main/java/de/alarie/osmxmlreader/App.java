package de.alarie.osmxmlreader;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import static org.apache.commons.cli.OptionBuilder.withArgName;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

/**
 * Hello world!
 *
 */
public class App {

    public static final Options options = new Options();

    static {
        options.addOption(withArgName("file").hasArg().isRequired().withDescription("The output file to write to").withLongOpt("file-path").create("f"));
        options.addOption(withArgName("box").hasArg().isRequired().withDescription("The bounding Box").withLongOpt("bounding-box").create("b"));

        options.addOption("h", "help", false, null);
    }

    public static void main(String[] args) throws Exception {
        CommandLineParser parser = new PosixParser();
        CommandLine line = parser.parse(options, args);
        if (line.hasOption('h')) {
            printUsage();
            System.exit(0);
        }

        File file = new File(line.getOptionValue('f'));

        String box = line.getOptionValue('b');
        String[] coords = box.split(",");
        assert coords.length == 4;

        Rectangle2D boundingBox = new Rectangle2D.Float(Float.parseFloat(coords[0]),
                Float.parseFloat(coords[1]),
                Float.parseFloat(coords[2]),
                Float.parseFloat(coords[3]));

        new App().downloadSample(file, boundingBox);
    }

    private void downloadSample(File file, Rectangle2D bbox) throws IOException {        
        final double max = 0.25;
        int xMul = (int) Math.ceil(bbox.getWidth() / max);
        int yMul = (int) Math.ceil(bbox.getHeight() / max);
        for (int x = 0; x < xMul; x++) {
            for (int y = 0; y < yMul; y++) {
                File f = new File(file.getAbsolutePath() + ".part" + x + y);

                double xPos = bbox.getX() + x * max;
                double yPos = bbox.getY() + y * max;
                String b = "" + xPos;
                b += "," + yPos;
                b += "," + (xPos + Math.min(max, bbox.getWidth() - max * x));
                b += "," + (yPos + Math.min(max, bbox.getHeight() - max * y));

                URL downloadUrl = new URL("http://api.openstreetmap.org/api/0.6/map?bbox=" + b);

                byte[] buffer = new byte[4096];
                try (InputStream in = downloadUrl.openStream();
                        OutputStream out = new FileOutputStream(f);) {
                    int len = 0;
                    do {
                        out.write(buffer, 0, len);
                        len = in.read(buffer);
                    } while (len > 0);
                }
            }
        }
    }

    private static void printUsage() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("--file [OPTIONS]", options);
    }
}
