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
package org.webterraingenerator;

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

import darwin.util.math.base.LineSegment;
import darwin.util.math.base.vector.*;

/**
 *
 * @author daniel
 */
public class PathTestFrame extends JFrame
{
    public PathTestFrame(Path path) throws HeadlessException
    {
        BufferedImage image = new BufferedImage(512, 512, BufferedImage.TYPE_USHORT_GRAY);
        Graphics2D g = (Graphics2D) image.getGraphics();

        g.setColor(Color.BLUE);

        Polygon p = new Polygon();
        for (ImmutableVector<Vector2> v : path.buildExtrudedPolygon(30)) {
            float[] c = v.getCoords();
            p.addPoint((int) c[0], (int) c[1]);
        }
        g.fillPolygon(p);

        g.setColor(Color.RED);

        for (LineSegment ls : path) {
            float[] start = ls.getStart().getCoords();
            float[] end = ls.getEnd().getCoords();

            g.drawLine((int) start[0], (int) start[1],
                       (int) end[0], (int) end[1]);
        }

        JLabel label = new JLabel(new ImageIcon(image));
        getContentPane().add(label);

        setPreferredSize(new Dimension(512, 512));
        pack();
        setVisible(true);
        setLocationRelativeTo(null);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void boxBlur(WritableRaster raster, int kernel)
    {

        int[] tmp = new int[1];
        int[][] data = new int[raster.getWidth()][raster.getHeight()];

        float divisor = 1f / (4 * kernel * kernel + 4 * kernel + 1);
        for (int x = kernel; x < data.length - kernel; x++) {
            for (int y = kernel; y < data[0].length - kernel; y++) {

                int sum = 0;
                for (int kx = -kernel; kx <= kernel; kx++) {
                    for (int ky = -kernel; ky <= kernel; ky++) {
                        raster.getPixel(x + kx, y + ky, tmp);
                        sum += tmp[0];
                    }
                }
                data[x][y] = (int) (sum * divisor);
            }
        }

        for (int x = 0; x < data.length; x++) {
            for (int y = 0; y < data[0].length; y++) {
                tmp[0] = data[x][y];
                raster.setPixel(x, y, tmp);
            }
        }

    }
}
