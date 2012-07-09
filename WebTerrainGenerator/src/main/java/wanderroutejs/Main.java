package wanderroutejs;

import java.io.*;
import org.apache.commons.cli.*;

import static org.apache.commons.cli.OptionBuilder.withArgName;

public class Main
{
    public static final Options options = new Options();

    static {
        options.addOption(withArgName("track").hasArg().
                isRequired().
                withDescription("The file path to the gps track(*.gpx), for which render resource should get created.").
                withLongOpt("gps-track-path").
                create("t"));

        options.addOption(withArgName("output").hasArg().
                withDescription("The output path in which the resource should get created.").
                withLongOpt("output-path").
                create("o"));

        options.addOption(withArgName("tessfactor").hasArg().
                withDescription("The amount of subdivitions of the terrain grid.").
                withLongOpt("tesselation-factor").
                create("tf"));

        options.addOption(withArgName("heihgtscale").hasArg().
                withDescription("The amount the terrain height get scaled.").
                withLongOpt("terrain-height-scale").
                create("hs"));

        options.addOption(withArgName("normalscale").hasArg().
                withDescription("The 'strongness' factor of the terrain normal map(value < 1. means stronger).").
                withLongOpt("terrain-normal-power").
                create("ns"));

        options.addOption("h", "help", false, null);
    }

    public static void main(String[] args) throws IOException
    {
        CommandLineParser parser = new PosixParser();
        try {
            CommandLine line = parser.parse(options, args);
            if (line.hasOption('h')) {
                printUsage();
                System.exit(0);
            }

            String trackFile = line.getOptionValue('t');
            String outputDir = line.getOptionValue('o', "./");

            int tessfactor;
            try {
                tessfactor = Integer.parseInt(line.getOptionValue("tf", "" + MightyGenerat0r.DEFAULT_TESS_FACTOR));
            } catch (NumberFormatException ex) {
                System.out.println("Error while parsing the terrain tesselation factor option, using the default '"
                        + MightyGenerat0r.DEFAULT_TESS_FACTOR + "'!");
                tessfactor = MightyGenerat0r.DEFAULT_TESS_FACTOR;
            }

            float heightScale;
            try {
                heightScale = Float.parseFloat(line.getOptionValue("hs", "" + MightyGenerat0r.DEFAULT_HEIGHT_SCALE));
            } catch (NumberFormatException ex) {
                System.out.println("Error while parsing the terrain height scale option, using the default '"
                        + MightyGenerat0r.DEFAULT_HEIGHT_SCALE + "'!");
                heightScale = MightyGenerat0r.DEFAULT_HEIGHT_SCALE;
            }

            float normalScale;
            try {
                normalScale = Float.parseFloat(line.getOptionValue("ns", "" + MightyGenerat0r.DEFAULT_NORMAL_SCALE));
            } catch (NumberFormatException ex) {
                System.out.println("Error while parsing the terrain normal scale option, using the default '"
                        + MightyGenerat0r.DEFAULT_NORMAL_SCALE + "'!");
                normalScale = MightyGenerat0r.DEFAULT_NORMAL_SCALE;
            }

            if(line.hasOption('h'))
                printUsage();

            File out = new File(outputDir);
            MightyGenerat0r generator = new MightyGenerat0r(heightScale, normalScale, tessfactor, out);
            generator.generate(trackFile);
        } catch (ParseException ex) {
            System.out.println(ex.getLocalizedMessage());
            printUsage();
            System.exit(1);
        }
    }

    private static void printUsage()
    {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("--gps-track-path [-o path -tf factor -hs scale -ns scale]", options);
    }
}
