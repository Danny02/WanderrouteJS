/*
 * Copyright (C) 2012 daniel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package wanderroutejs.examples;

import java.awt.Rectangle;
import java.awt.image.*;
import java.io.*;
import javax.imageio.ImageIO;
import wanderroutejs.PathTraingulator;
import wanderroutejs.datasources.*;
import wanderroutejs.generators.*;
import wanderroutejs.heighmapgeneration.*;
import wanderroutejs.imageprocessing.*;
import wanderroutejs.io.PlainJSONModelWriter;

import darwin.geometrie.io.*;
import darwin.geometrie.unpacked.*;
import darwin.jopenctm.compression.RawEncoder;
import darwin.util.math.base.vector.*;
import darwin.util.math.composits.Path;

/**
 *
 * @author daniel
 */
public class TrackExample
{
    public static final String EXAMPLE_PATH = "/examples/untreusee-1206956.gpx";
    public static final File OUTPUT_PATH;

    static {
        String a = EXAMPLE_PATH.substring(EXAMPLE_PATH.lastIndexOf("/"));
        a = a.substring(0, a.length() - 4);
        OUTPUT_PATH = new File("./" + a + "/");

    }
    private static final int TESS_FACTOR = 100;
	
	private float heightScale;

    public void generate()
    {
		
		heightScale = 1f / 4500;
		
        InputStream in = TrackExample.class.getResourceAsStream(EXAMPLE_PATH);
        TrackGenerator trackGenerator = TrackGenerator.fromStream(in);

        Path<Vector3> path = trackGenerator.makeTrip().getTripAsPath();

        Path<Vector2> p2 = new Path<>();
        for (ImmutableVector<Vector3> v : path.getVectorIterable()) {
            p2.addPathElement(new Vector2(v.getCoords()[0], v.getCoords()[1]));
        }
        PathTestFrame frame = new PathTestFrame(new BufferedImage(1024, 1024, BufferedImage.TYPE_INT_RGB));
        frame.drawPath(p2);

        Rectangle boundingBox = trackGenerator.getTripBoundingBox();


        System.out.printf("Getting data for rectangle: (%d, %d) and (%d, %d)\n", boundingBox.x, boundingBox.y, boundingBox.width, boundingBox.height);


        SRTMGenerator srtmGenerator = new SRTMGenerator();
        try {
            System.out.println("Working on directory: " + OUTPUT_PATH);
            Iterable<File> files = srtmGenerator.loadRectangle(boundingBox).loadSRTMFiles(OUTPUT_PATH).getFiles();


            // render SRTM to heightmap, normalmap,...
            System.out.println("Generating maps.");

            generateMaps(files);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

		PathTraingulator trian = new PathTraingulator();
        {
            Mesh pathMesh = trian.buildPathMesh(path, heightScale);
            ModelWriter writerJson = new PlainJSONModelWriter();
            File pathFile2 = new File(OUTPUT_PATH, "path." + writerJson.getDefaultFileExtension());
            try (OutputStream out = new FileOutputStream(pathFile2)) {
                writerJson.writeModel(out, new Model[]{new Model(pathMesh, null)});
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        {
            Mesh pathPrisma = trian.buildExtrudedPrisma(p2, 0.0001f, 5f);

            ModelWriter writer = new CtmModelWriter(new RawEncoder());
            File pathFile = new File(OUTPUT_PATH, "path." + writer.getDefaultFileExtension());
            try (OutputStream out = new FileOutputStream(pathFile)) {
                writer.writeModel(out, new Model[]{new Model(pathPrisma, null)});
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        // render path into maps

        // get path mesh

        // get osm data

        // render osm into maps

    }

    public static void main(String[] args)
    {
        TrackExample trackExample = new TrackExample();

        trackExample.generate();
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
        BufferedImage img = ImageUtil2.loadImage(file.toURI().toURL());
        System.out.println("\tFinished loading in " + (System.currentTimeMillis() - time));

        System.out.println("Generating ambient occlusion map ...");
        time = System.currentTimeMillis();
        BufferedImage ambientOcclusionImg = this.generateAmbientOcclusionMap(img);
        System.out.println("\tFinished processing in " + (System.currentTimeMillis() - time));


        System.out.println("Generating mesh ...");
        time = System.currentTimeMillis();
        Model mesh = this.generateMesh(img, ambientOcclusionImg);
        System.out.println("\tFinished generating in " + (System.currentTimeMillis() - time));

        System.out.println("Writing mesh to file...");
        time = System.currentTimeMillis();
        try {
            saveMesh(mesh, file);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("\tFinished writing in " + (System.currentTimeMillis() - time));


        System.out.println("Generating normal map...");
        time = System.currentTimeMillis();
        this.generateNormalMap(img);
        System.out.println("\tFinished writing in " + (System.currentTimeMillis() - time));

    }

    private BufferedImage generateAmbientOcclusionMap(BufferedImage img)
    {
        int scale = 512;
        BufferedImage img2 = new BufferedImage(scale, scale, img.getType());
        BufferedImage low = ImageUtil2.getScaledImage(img, scale, scale, false);
        new GaussBlurOp(10).filter(low, img2);
        new GaussBlurOp(10).filter(img2, low);
        new GaussBlurOp(10).filter(low, img2);

        BufferedImage normal2 = new BufferedImage(img2.getWidth(), img2.getHeight(), BufferedImage.TYPE_INT_RGB);
        new NormalGeneratorOp().filter(img2, normal2);
        BufferedImage ao = new AmbientOcclusionOp(64, 16, 20).filter(img2, normal2);
        return ao;
    }

    private Model generateMesh(BufferedImage img,
                               BufferedImage ambientOcclusionImg)
    {
        HeightSource ambient = new HeightMapSource(ambientOcclusionImg, TESS_FACTOR * 3, 1f / 255);
        HeightmapGenerator generator = new GridHeightmap(TESS_FACTOR, ambient);
        HeightSource source = new HeightMapSource(img, TESS_FACTOR * 3, heightScale);

        Mesh mesh = generator.generateVertexData(source);
        Model m = new Model(mesh, null);

        return m;
    }

    private void saveMesh(Model mesh, File file) throws FileNotFoundException, IOException
    {
        ModelWriter writer = new CtmModelWriter(new RawEncoder());
        try (OutputStream out = new FileOutputStream(file.getPath() + '.' + writer.getDefaultFileExtension());) {
            writer.writeModel(out, new Model[]{mesh});
        }
    }

    private void generateNormalMap(BufferedImage img)
    {
        BufferedImage normal = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
        new NormalGeneratorOp().filter(img, normal);

        int a = 512;
        BufferedImage img2 = new BufferedImage(a, a, img.getType());
        new GaussBlurOp(5).filter(ImageUtil2.getScaledImage(img, a, a, false), img2);

        BufferedImage normal2 = new BufferedImage(img2.getWidth(), img2.getHeight(), BufferedImage.TYPE_INT_RGB);
        new NormalGeneratorOp().filter(img2, normal2);
        BufferedImage ao = new AmbientOcclusionOp(64, 16, 20).filter(img2, normal2);

        BufferedImageOp op = new RescaleOp(60, 60, null);
        BufferedImage adjustedHeight = op.createCompatibleDestImage(img, img.getColorModel());
        op.filter(img, adjustedHeight);

        try {
            ImageIO.write(normal, "png", new File(OUTPUT_PATH, "normalmap.png"));
            ImageIO.write(ao, "png", new File(OUTPUT_PATH, "ambientocclusion.png"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
