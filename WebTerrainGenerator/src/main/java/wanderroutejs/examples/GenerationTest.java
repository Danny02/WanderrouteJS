package wanderroutejs.examples;

import java.awt.Rectangle;
import java.awt.image.*;
import java.io.*;
import javax.imageio.ImageIO;
import wanderroutejs.PathTriangulator;
import wanderroutejs.datasources.*;
import wanderroutejs.generators.*;
import wanderroutejs.heighmapgeneration.*;
import wanderroutejs.imageprocessing.*;
import wanderroutejs.io.PlainJSONModelWriter;

import darwin.geometrie.io.*;
import darwin.geometrie.unpacked.*;
import darwin.util.math.base.vector.Vector3;
import darwin.util.math.composits.Path;

/**
 * Hello world!
 * <p/>
 */
public class GenerationTest
{
    public static final String EXAMPLE_PATH = "/examples/untreusee-1206956.gpx";
    public static final File OUTPUT_PATH;

    static {
        String a = EXAMPLE_PATH.substring(EXAMPLE_PATH.lastIndexOf('/'));
        a = a.substring(0, a.length() - 4);
        OUTPUT_PATH = new File("./" + a + "/");

    }
    private static final int TESS_FACTOR = 100;
    private static final float HEIGHT_SCALE = 1f / 4500;
    private static final float NORMAL_SCALE = 0.01f;

    public void generate()
    {
        InputStream in = GenerationTest.class.getResourceAsStream(EXAMPLE_PATH);

        TrackGenerator trackGenerator = TrackGenerator.fromStream(in);
        Rectangle boundingBox = trackGenerator.getTripBoundingBox();
        Path<Vector3> path = trackGenerator.getTripAsPath(HEIGHT_SCALE,
                                                          -boundingBox.x,
                                                          -boundingBox.y);

        System.out.println("Getting data for " + boundingBox);

        SRTMFileLocator srtmGenerator = new SRTMFileLocator();
        try {
            System.out.println("Working on directory: " + OUTPUT_PATH);
            Iterable<File> files = srtmGenerator.loadRectangle(boundingBox).loadSRTMFiles(OUTPUT_PATH).getFiles();

            // render SRTM to heightmap, normalmap,...
            System.out.println("Generating maps.");

            generateMaps(files);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        PathTriangulator trian = new PathTriangulator();
        {
            Mesh pathMesh = trian.buildPathMesh(path);
            ModelWriter writerJson = new PlainJSONModelWriter();
            File pathFile2 = new File(OUTPUT_PATH, "path." + writerJson.getDefaultFileExtension());
            try (OutputStream out = new FileOutputStream(pathFile2)) {
                writerJson.writeModel(out, new Model[]{new Model(pathMesh, null)});
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        //TODO buged floating point math
//        {
//            Mesh pathPrisma = trian.buildExtrudedPrisma(0.0001f, 5f, path);
//
//            ModelWriter writer = new CtmModelWriter();
//            File pathFile = new File(OUTPUT_PATH, "path." + writer.getDefaultFileExtension());
//            try (OutputStream out = new FileOutputStream(pathFile)) {
//                writer.writeModel(out, new Model[]{new Model(pathPrisma, null)});
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }
//        }

        // render path into maps

        // get path mesh

        // get osm data

        // render osm into maps

    }

    private void generateMaps(Iterable<File> files) throws IOException
    {
        for (File file : files) {
            this.generateMaps(file);
        }
    }

    private void generateMaps(File file) throws IOException
    {
        long time;
        System.out.println("Start loading heightmap texture for " + file + "...");
        time = System.currentTimeMillis();
        BufferedImage height = ImageUtil2.loadImage(file.toURI().toURL());
        System.out.println("\tFinished loading in " + (System.currentTimeMillis() - time));

        System.out.println("Generating ambient occlusion map ...");
        time = System.currentTimeMillis();
        BufferedImage ambientOcclusionImg = generateAmbientOcclusionMap(height);
        System.out.println("\tFinished processing in " + (System.currentTimeMillis() - time));

        System.out.println("Generating normal map...");
        time = System.currentTimeMillis();
        BufferedImage normalMap = generateNormalMap(height);
        System.out.println("\tFinished processing in " + (System.currentTimeMillis() - time));

        System.out.println("Generating mesh ...");
        time = System.currentTimeMillis();
        Model mesh = generateMesh(height, ambientOcclusionImg);
        System.out.println("\tFinished generating in " + (System.currentTimeMillis() - time));

        System.out.println("Writing mesh to file...");
        time = System.currentTimeMillis();

        saveMesh(mesh, file);

        System.out.println("\tFinished writing in " + (System.currentTimeMillis() - time));


        System.out.println("Writing texture maps...");

        ImageIO.write(normalMap, "png", new File(OUTPUT_PATH, "normalmap.png"));
        ImageIO.write(ambientOcclusionImg, "png", new File(OUTPUT_PATH, "ambientocclusion.png"));

        System.out.println("\tFinished writing in " + (System.currentTimeMillis() - time));
    }

    private BufferedImage generateAmbientOcclusionMap(BufferedImage img) throws IOException
    {
        int scale = 512;
        BufferedImage low = ImageUtil2.getScaledImage(img, scale, scale, false);

        //blur the image several times
        BufferedImageOp gauss = new GaussBlurOp(10);
        BufferedImage pingPongBuffer = new BufferedImage(scale, scale, img.getType());
        gauss.filter(low, pingPongBuffer);
        gauss.filter(pingPongBuffer, low);
        gauss.filter(low, pingPongBuffer);

        BufferedImage normal2 = new BufferedImage(scale, scale, BufferedImage.TYPE_INT_RGB);
        new NormalGeneratorOp(NORMAL_SCALE).filter(pingPongBuffer, normal2);
        BufferedImage ao = new AmbientOcclusionOp(64, 16, 20).filter(pingPongBuffer, normal2);

        BufferedImage hout = new BufferedImage(pingPongBuffer.getWidth(), pingPongBuffer.getHeight(), pingPongBuffer.getType());
        new RescaleOp(30, 30, null).filter(pingPongBuffer, hout);
        ImageIO.write(hout, "png", new File(OUTPUT_PATH, "heihgtmap.png"));
        return ao;
    }

    private Model generateMesh(BufferedImage height,
                               BufferedImage ambient)
    {
        HeightSource ambientSource = new ImageHeightSource(ambient, TESS_FACTOR * 3, 1f / 255);
        HeightmapGenerator generator = new GridHeightmapWithNormals(TESS_FACTOR, ambientSource, height);

        HeightSource source = new ImageHeightSource(height, TESS_FACTOR * 3, HEIGHT_SCALE);
        Mesh mesh = generator.generateVertexData(source);

        return new Model(mesh, null);
    }

    private void saveMesh(Model mesh, File file) throws IOException
    {
        ModelWriter writer = new CtmModelWriter();
        try (OutputStream out = new FileOutputStream(file.getPath() + '.' + writer.getDefaultFileExtension());) {
            writer.writeModel(out, new Model[]{mesh});
        }
    }

    private BufferedImage generateNormalMap(BufferedImage img)
    {
        BufferedImage normal = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
        new NormalGeneratorOp(NORMAL_SCALE).filter(img, normal);

        return normal;
    }

    public static void main(String[] args)
    {
        GenerationTest test = new GenerationTest();

        test.generate();
    }
}
