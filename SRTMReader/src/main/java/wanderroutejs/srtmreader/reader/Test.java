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
package wanderroutejs.srtmreader.reader;

import wanderroutejs.srtmreader.NormalGeneratorOp;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.Iterator;
import javax.imageio.*;
import javax.imageio.spi.IIORegistry;
import javax.swing.*;

/**
 *
 * @author daniel
 */
public class Test
{
    public static void main(String[] args) throws IOException
    {
//        IIORegistry.getDefaultInstance().registerApplicationClasspathSpis();

        BufferedImage img = null;
        for (Iterator<ImageReader> it = ImageIO.getImageReadersBySuffix("hgt"); it.hasNext();) {
            ImageReader reader = it.next();

            reader.setInput(ImageIO.createImageInputStream(new File("N50E011.hgt")));
            img = reader.read(0);
        }

//        float[] kernelX = new float[]{1,0,-1,2,0,-2,1,0,-1};
//        float[] kernelY = new float[]{1,2,1,0,0,0,-1,-2,-1};
//        BufferedImageOp sobelX = new ConvolveOp(new Kernel(3, 3, kernelX));
//        BufferedImageOp sobelY = new ConvolveOp(new Kernel(3, 3, kernelY));
//
//        BufferedImage gradX = sobelX.createCompatibleDestImage(img, img.getColorModel());
//        sobelX.filter(img, gradX);
//
//        BufferedImage gradY = sobelY.createCompatibleDestImage(img, img.getColorModel());
//        sobelY.filter(img, gradY);
        

        BufferedImage dest = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
        new NormalGeneratorOp().filter(img, dest);

        JFrame frame = new JFrame();

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        frame.getContentPane().add(panel);

        JLabel normal = new JLabel(new ImageIcon(dest));
        BufferedImageOp op = new RescaleOp(60, 60, null);
        BufferedImage disp = op.createCompatibleDestImage(img, img.getColorModel());
        JLabel height = new JLabel(new ImageIcon(op.filter(img, disp)));

        normal.setPreferredSize(new Dimension(400, 400));
        height.setPreferredSize(new Dimension(400, 400));

        panel.add(height);
        panel.add(normal);

        frame.setPreferredSize(new Dimension(800, 400));
        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
