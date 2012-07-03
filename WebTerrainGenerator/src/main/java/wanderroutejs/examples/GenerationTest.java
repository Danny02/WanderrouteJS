package wanderroutejs.examples;

import java.awt.image.BufferedImage;
import java.io.*;
import wanderroutejs.datasources.*;
import wanderroutejs.heighmapgeneration.*;
import wanderroutejs.imageprocessing.*;

import darwin.geometrie.io.*;
import darwin.geometrie.unpacked.*;
import darwin.jopenctm.compression.RawEncoder;

/**
 * Hello world!
 * <p/>
 */
public class GenerationTest
{
    public static void main(String[] args) throws IOException
    {
        int tessFactor = 100;


        System.out.println("Start loading heightmap texture ...");
        long time = System.currentTimeMillis();
        BufferedImage img = ImageUtil2.loadImage(GenerationTest.class.getResource("/examples/N50E011.hgt"));
        System.out.println("\tFinished loading in " + (System.currentTimeMillis() - time));


        System.out.println("Generating ambient occlusion map ...");
        time = System.currentTimeMillis();
        int scale = 512;
        BufferedImage img2 = new BufferedImage(scale, scale, img.getType());
        BufferedImage low = ImageUtil2.getScaledImage(img, scale, scale, false);
        new GaussBlurOp(10).filter(low, img2);
        new GaussBlurOp(10).filter(img2, low);
        new GaussBlurOp(10).filter(low, img2);

        BufferedImage normal2 = new BufferedImage(img2.getWidth(), img2.getHeight(), BufferedImage.TYPE_INT_RGB);
        new NormalGeneratorOp().filter(img2, normal2);
        BufferedImage ao = new AmbientOcclusionOp(64, 16, 20).filter(img2, normal2);
        HeightSource ambient = new HeightMapSource(ao, tessFactor * 3, 1f / 255);
        System.out.println("\tFinished processing in " + (System.currentTimeMillis() - time));


        System.out.println("Generating mesh ...");
        time = System.currentTimeMillis();
        HeightmapGenerator generator = new GridHeightmap(tessFactor, ambient);
        HeightSource source = new HeightMapSource(img, tessFactor * 3, 1f / 4500);

        Mesh mesh = generator.generateVertexData(source);
        Model m = new Model(mesh, null);
        System.out.println("\tFinished generating in " + (System.currentTimeMillis() - time));



        System.out.println("Writing mesh to file...");
        time = System.currentTimeMillis();
        try (OutputStream out = new FileOutputStream("map1.ctm");) {
            ModelWriter writer = new CtmModelWriter(new RawEncoder());
            writer.writeModel(out, new Model[]{m});
        }
        System.out.println("\tFinished writing in " + (System.currentTimeMillis() - time));
    }
}
