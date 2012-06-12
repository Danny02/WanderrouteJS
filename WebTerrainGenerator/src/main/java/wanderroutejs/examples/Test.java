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
import javax.imageio.*;
import javax.swing.*;
import wanderroutejs.imageprocessing.*;

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
//        new BufferedImage(200, 200, BufferedImage.TYPE_USHORT_GRAY);
//        Graphics2D g2 = img2.createGraphics();
//        g2.drawImage(img, 0, 0, 100, 100, null);

        BufferedImage normal = new BufferedImage(img2.getWidth(), img2.getHeight(), BufferedImage.TYPE_INT_RGB);
        new NormalGeneratorOp().filter(img2, normal);


        BufferedImageOp op = new RescaleOp(60, 60, null);
        BufferedImage adjustedHeight = op.createCompatibleDestImage(img2, img2.getColorModel());
        op.filter(img2, adjustedHeight);
        ImageIO.write(adjustedHeight, "png", new File("test.png"));

        ImageFrame frame = new ImageFrame(1200, 600);
        frame.addImage(normal);
        frame.addImage(adjustedHeight);

    }
}
