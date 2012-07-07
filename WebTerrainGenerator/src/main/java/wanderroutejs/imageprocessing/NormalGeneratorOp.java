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
package wanderroutejs.imageprocessing;

import java.awt.RenderingHints;
import java.awt.geom.*;
import java.awt.image.*;

/**
 *
 * @author daniel
 */
public class NormalGeneratorOp implements BufferedImageOp
{
    private final float zScale;

    public NormalGeneratorOp(float zScale)
    {
        this.zScale = zScale;
    }

    public NormalGeneratorOp()
    {
        zScale = 1;
    }

    @Override
    public BufferedImage filter(BufferedImage src, BufferedImage dest)
    {
        int[] tmp = new int[4];
        for (int x = 0; x < src.getWidth(); ++x) {
            for (int y = 0; y < src.getHeight(); y++) {
                int[][] data = new int[3][3];
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        if (i == 1 && j == 1) {
                            continue;
                        }
                        int cx = x + i - 1;
                        int cy = y + j - 1;
                        int value;
                        if (cx < 0 || cx >= src.getWidth() || cy < 0 || cy >= src.getHeight()) {
                            value = 0;
                        } else {
                            src.getRaster().getPixel(cx, cy, tmp);
                            value = tmp[0];
                        }
                        data[i][j] = value;
                    }
                }
                int vx = data[0][0] - data[2][0] + data[0][1] * 2 - data[2][1] * 2 + data[0][2] - data[2][2];
                int vy = data[0][0] + 2 * data[1][0] + data[2][0] - data[0][2] - 2 * data[1][2] - data[2][2];


                double diag = Math.sqrt(vx * vx + vy * vy);

                double r =  Short.MAX_VALUE;
                double vz = zScale * Math.sqrt(r * r - diag * diag);

                double nlength = Math.sqrt(vx * vx + vy * vy + vz * vz);

                double nx, ny, nz = 0;
                if (nlength != 0) {
                    nx = vx / nlength * 0.5 + 0.5;
                    ny = vy / nlength * 0.5 + 0.5;
                    nz = vz / nlength;
                } else {
                    nx = ny = 0.5;
                }

                dest.getRaster().setPixel(x, y, new int[]{(int) (nx * 255), (int) (ny * 255), (int) (nz * 255)});
            }
        }
        return dest;
    }

    @Override
    public Rectangle2D getBounds2D(BufferedImage bi)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BufferedImage createCompatibleDestImage(BufferedImage bi,
                                                   ColorModel cm)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Point2D getPoint2D(Point2D pd, Point2D pd1)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public RenderingHints getRenderingHints()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
