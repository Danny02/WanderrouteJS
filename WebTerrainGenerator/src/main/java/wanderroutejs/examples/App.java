package wanderroutejs.examples;

import java.awt.image.BufferedImage;
import java.io.*;
import wanderroutejs.datasources.HeightMapSource;
import wanderroutejs.heighmapgeneration.GridWithNormalGenerator;
import wanderroutejs.imageprocessing.ImageUtil2;

import darwin.geometrie.io.*;
import darwin.geometrie.unpacked.*;
import darwin.jopenctm.compression.RawEncoder;

/**
 * Hello world!
 * <p/>
 */
public class App
{
    public static void main(String[] args) throws IOException
    {
        BufferedImage img = ImageUtil2.loadImage("examples/N50E011.hgt");

        int tessFactor = 100;
//        HeightmapGenerator generator = new GridHeightmap(tessFactor);
//        HeightSource source = new HeightMapSource(img, tessFactor, 1f / 6000);
//        Mesh mesh = generator.generateVertexData(source);
        Mesh mesh = new GridWithNormalGenerator(tessFactor, img).generateVertexData(new HeightMapSource(img, tessFactor, 1f / 6000));
        Model m = new Model(mesh, null);

        try (OutputStream out = new FileOutputStream("test2.ctm");) {
            ModelWriter writer = new CtmModelWriter(new RawEncoder());
            writer.writeModel(out, new Model[]{m});
        }
    }
}
