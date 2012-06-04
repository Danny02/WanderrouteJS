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

import com.sun.imageio.plugins.bmp.BMPImageWriterSpi;
import com.sun.imageio.plugins.jpeg.JPEGImageWriterSpi;
import com.sun.imageio.plugins.png.PNGImageWriterSpi;
import java.awt.*;
import java.awt.image.BufferedImage;
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
        IIORegistry.getDefaultInstance().registerApplicationClasspathSpis();

        BufferedImage img = null;
        for (Iterator<ImageReader> it = ImageIO.getImageReadersBySuffix("hgt"); it.hasNext();) {
            ImageReader reader = it.next();

            reader.setInput(ImageIO.createImageInputStream(new File("N50E011.hgt")));
            img = reader.read(0);
        }

        boolean r = ImageIO.write(img, "png", new File("test.png"));
        System.out.println(r);

        if (img != null) {
            System.out.println(img.getHeight());
        }

        JFrame frame = new JFrame();
        JLabel label = new JLabel(new ImageIcon(img));
        frame.getContentPane().add(label);

        frame.setPreferredSize(new Dimension(1201, 1201));
        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
