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

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.ImageIO;
import wanderroutejs.imageprocessing.*;

import darwin.util.math.base.vector.*;

/**
 *
 * @author daniel
 */
public class Test
{
    public static void main(String[] args) throws IOException
    {
//        IIORegistry.getDefaultInstance().registerApplicationClasspathSpis();


        BufferedImage img = ImageUtil2.loadImage("examples/N50E011.hgt");
        BufferedImage img2 = img;

        BufferedImage normal = new BufferedImage(img2.getWidth(), img2.getHeight(), BufferedImage.TYPE_INT_RGB);
        new NormalGeneratorOp().filter(img2, normal);


        BufferedImageOp op = new RescaleOp(60, 60, null);
        BufferedImage adjustedHeight = op.createCompatibleDestImage(img2, img2.getColorModel());
        op.filter(img2, adjustedHeight);
        ImageIO.write(normal, "png", new File("test.png"));

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

    }
}
