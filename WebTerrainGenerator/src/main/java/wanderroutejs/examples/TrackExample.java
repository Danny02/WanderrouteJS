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
import java.util.ArrayList;
import wanderroutejs.datasources.*;
import wanderroutejs.generators.*;
import wanderroutejs.heighmapgeneration.*;
import wanderroutejs.imageprocessing.*;

import darwin.geometrie.io.*;
import darwin.geometrie.unpacked.*;
import darwin.jopenctm.compression.RawEncoder;
import darwin.util.math.composits.Path;

/**
 *
 * @author daniel
 */
public class TrackExample
{
    private TrackGenerator trackGenerator;

    private SRTMGenerator srtmGenerator;

    private int tessFactor = 100;

    public TrackExample(int tessFactor) {
        this.tessFactor = tessFactor;



    }

    public void generate () {
        trackGenerator = TrackGenerator.fromFile(new File("/examples/untreusee-1206956.gpx"));

        Path path = trackGenerator.makeTrip()
                .getTripAsPath();

        Rectangle boundingBox = trackGenerator.getTripBoundingBox();




        srtmGenerator = new SRTMGenerator();

        ArrayList<String> files = srtmGenerator.loadRectangle(boundingBox)
                .loadSRTMFiles("srtm/")
                .getFiles();



        // render SRTM to heightmap, normalmap,...
        try {
            this.generateMaps(files);
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }

        // render path into maps

        // get path mesh

        // get osm data

        // render osm into maps

    }

    public static void main(String[] args)
    {
        TrackExample trackExample = new TrackExample(100);

        trackExample.generate();
    }

    private void generateMaps(ArrayList<String> files) throws IOException {
        for(String file : files) {
            this.generateMaps(file);
        }
    }

    private void generateMaps(String file) throws IOException {
        long time;

        System.out.println("Start loading heightmap texture ...");
        time = System.currentTimeMillis();
        BufferedImage img = ImageUtil2.loadImage(file);
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
            this.saveMesh(mesh, file);
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("\tFinished writing in " + (System.currentTimeMillis() - time));


        System.out.println("Generating normal map...");
        time = System.currentTimeMillis();
        this.generateNormalMap(img);
        System.out.println("\tFinished writing in " + (System.currentTimeMillis() - time));

    }

    private BufferedImage generateAmbientOcclusionMap(BufferedImage img) {
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


    private Model generateMesh(BufferedImage img, BufferedImage ambientOcclusionImg){
        HeightSource ambient = new HeightMapSource(ambientOcclusionImg, tessFactor * 3, 1f / 255);
        HeightmapGenerator generator = new GridHeightmap(tessFactor, ambient);
        HeightSource source = new HeightMapSource(img, tessFactor * 3, 1f / 4500);

        Mesh mesh = generator.generateVertexData(source);
        Model m = new Model(mesh, null);

        return m;
    }

    private void saveMesh(Model mesh, String fileName) throws FileNotFoundException, IOException {
        try (OutputStream out = new FileOutputStream(fileName + ".ctm");) {
            ModelWriter writer = new CtmModelWriter(new RawEncoder());
            writer.writeModel(out, new Model[]{mesh});
        }
    }

    private void generateNormalMap(BufferedImage img) {
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

        ImageFrame frame = new ImageFrame(1200, 600);
        frame.addImage(normal);
        frame.addImage(adjustedHeight);
        frame.addImage(ao);
    }


}
