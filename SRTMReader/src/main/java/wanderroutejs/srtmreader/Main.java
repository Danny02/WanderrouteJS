package wanderroutejs.srtmreader;

import java.awt.Rectangle;
import java.io.*;
import org.apache.commons.cli.*;

import static org.apache.commons.cli.OptionBuilder.withArgName;

public class Main
{
    private final Rectangle terrainBounds;

    public Main(Rectangle terrainBounds)
    {
        this.terrainBounds = terrainBounds;
    }

    public void convertToMesh(File file, int precision, String varName, String type)
    {
        String path = file.getAbsolutePath();
        System.out.println("Reading file: " + path);

        SRTMReader reader = new SRTMReader(SRTMReader.Version.SRTM3);

        int[][] heights;
        try {
            heights = reader.parseFile(file);

            String meshType;
            Generator generator;
            if("TRIANGLE_STRIP".equals(type)){
                generator = new TriangleStripGenerator();
                meshType = type;
            }else{
                generator = new TrianglesGenerator();
                meshType = "TRIANGLES";
            }

            generator.generateMesh(heights,
                                    precision,
                                    terrainBounds.y,
                                    terrainBounds.y + terrainBounds.height,
                                    terrainBounds.x,
                                    terrainBounds.x + terrainBounds.width);
//            String json = generator.toJSON(varName);

            FileWriter writer = new FileWriter(path.replaceAll(".hgt", "." + meshType + ".json"));
//            writer.write(json);
            writer.close();
            System.out.println("JSON file created");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static final Options options = new Options();
    static {
        options.addOption(withArgName("file").hasArg().isRequired().
                          withDescription("The SRTM file to convert to a triangle Mesh").
                          withLongOpt("file-path").create("f"));

        options.addOption(withArgName("type").hasArg().
                          withDescription("The OpenGL type of the mesh [TRIANGLE_STRIP, TRINAGLES]. Default is TRIANGLES").
                          withLongOpt("mesh-type").create("t"));

        options.addOption(withArgName("name").hasArg().
                          withDescription("The JSON variable name to use for the data array.").
                          withLongOpt("jsonVariableName").create("v"));

        options.addOption(OptionBuilder.hasArg().
                          withDescription("The precision of ???, default-value is 3").
                          withLongOpt("mesh-precision").create("p"));

        options.addOption("h", "help", false, null);
    }

    public static void main(String[] args)
    {
        CommandLineParser parser = new PosixParser();
        try {
            CommandLine line =  parser.parse(options, args);
            if(line.hasOption('h'))
            {
                printUsage();
                System.exit(0);
            }

            File file = new File(line.getOptionValue('f'));
            String variableName = line.getOptionValue('v', "vertices");
            String type = line.getOptionValue('t', "TRIANGLES");
            int precision;
            try{
                precision = Integer.parseInt(line.getOptionValue('p', "3"));
            }catch(NumberFormatException ex)
            {
                System.out.println("Error while parsing the mesh-precision option, using the default '3'!");
                precision = 3;
            }

            Rectangle terrainBounds = new Rectangle(300, 450, 720, 720);
            new Main(terrainBounds).convertToMesh(file, precision, variableName, type);

        } catch (ParseException ex) {
            System.out.println(ex.getLocalizedMessage());
            printUsage();
            System.exit(1);
        }
    }

    private static void printUsage() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("--file-path [OPTIONS]", options);
    }
}
