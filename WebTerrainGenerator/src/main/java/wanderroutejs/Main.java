package wanderroutejs;

import java.awt.image.BufferedImage;
import java.io.*;
import org.apache.commons.cli.*;
import wanderroutejs.datasources.*;
import wanderroutejs.heighmapgeneration.*;
import wanderroutejs.imageprocessing.ImageUtil2;
import wanderroutejs.io.PlainJSONModelWriter;

import darwin.geometrie.io.*;
import darwin.geometrie.unpacked.*;

import static org.apache.commons.cli.OptionBuilder.withArgName;

public class Main
{
    private static final int TESS_DEFAULT = 10;
    public static final Options options = new Options();

    static {
        options.addOption(withArgName("file").hasArg().isRequired().withDescription("The SRTM file to convert to a triangle Mesh").withLongOpt("file-path").create("f"));

        options.addOption(withArgName("type").hasArg().withDescription("The OpenGL type of the mesh [TRIANGLE_STRIP, TRINAGLES]. Default is TRIANGLES").withLongOpt("mesh-type").create("t"));

        options.addOption(withArgName("name").hasArg().withDescription("The JSON variable name to use for the data array.").withLongOpt("jsonVariableName").create("v"));

        options.addOption(withArgName("tessfactor").hasArg().withDescription("The amount of tesselation default is " + TESS_DEFAULT).withLongOpt("tesselation-factor").create("tf"));

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

            String fileName = line.getOptionValue('f');
            String variableName = line.getOptionValue('v', "vertices");
            String type = line.getOptionValue('t', "TRIANGLES");
            int tessfactor;
            try {
                tessfactor = Integer.parseInt(line.getOptionValue("tf", "" + TESS_DEFAULT));
            } catch (NumberFormatException ex) {
                System.out.println("Error while parsing the mesh-precision option, using the default '" + TESS_DEFAULT + "'!");
                tessfactor = TESS_DEFAULT;
            }

            convertToMesh(fileName, tessfactor, variableName, type);

        } catch (ParseException ex) {
            System.out.println(ex.getLocalizedMessage());
            printUsage();
            System.exit(1);
        }
    }

    public static void convertToMesh(String fileName, int tesselation,
                                     String varName,
                                     String type) throws IOException
    {
        System.out.println("Reading file: " + fileName);

        BufferedImage image = ImageUtil2.loadImage(new File(fileName).toURI().toURL());
        HeightSource source = new ImageHeightSource(image, tesselation, 1f / 6000);

        String newFileName;
        HeightmapGenerator generator;

        ModelWriter writer;
        if ("TRIANGLE_STRIP".equals(type)) {
            generator = new TriangleStripGenerator(tesselation);
            newFileName = fileName.replaceAll(".hgt", ".TRIANGLE_STRIP.");
            writer = new PlainJSONModelWriter();
        } else {
            generator = new TrianglesGenerator(tesselation);
            newFileName = fileName.replaceAll(".hgt", ".TRIANGLES.");
            writer = new CtmModelWriter();
        }

        Mesh mesh = generator.generateVertexData(source);

        newFileName += writer.getDefaultFileExtension();

        try (OutputStream out = new FileOutputStream(newFileName)) {
            writer.writeModel(out, new Model[]{new Model(mesh, null)});
        }
    }

    private static void printUsage()
    {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("--file-path [OPTIONS]", options);
    }
}
