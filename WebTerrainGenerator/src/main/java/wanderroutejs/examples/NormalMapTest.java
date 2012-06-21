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

import java.awt.image.*;
import java.io.IOException;
import wanderroutejs.imageprocessing.*;

/**
 *
 * @author daniel
 */
public class NormalMapTest
{
    public static void main(String[] args) throws IOException
    {
//        IIORegistry.getDefaultInstance().registerApplicationClasspathSpis();


        BufferedImage img = ImageUtil2.loadImage("/examples/N50E11.hgt");
        img = ImageUtil2.getScaledImage(img, 512, 512, false);

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
//        ImageIO.write(normal, "png", new File("test.png"));

//todo buggy
//        Path p = new Path();
//        p.addPathElement(new Vector2(200, 200));
//        p.addPathElement(new Vector2(600, 800));
//        p.addPathElement(new Vector2(800, 200));
//        p.addPathElement(new Vector2(400, 400));
//
//        Graphics2D g = normal.createGraphics();
//        g.setColor(Color.YELLOW);
//        Polygon poly = new Polygon();
//        for (ImmutableVector<Vector2> v : p.buildExtrudedPolygon(10)) {
//            System.out.println((int) v.getCoords()[0]);
//            poly.addPoint((int) v.getCoords()[0], (int) v.getCoords()[1]);
//        }
//        g.fillPolygon(poly);


        ImageFrame frame = new ImageFrame(1200, 600);
        frame.addImage(normal);
        frame.addImage(adjustedHeight);
        frame.addImage(ao);

    }
}
