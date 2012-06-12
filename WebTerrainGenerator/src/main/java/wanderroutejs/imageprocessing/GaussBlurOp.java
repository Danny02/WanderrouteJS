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
public class GaussBlurOp implements BufferedImageOp
{
    private final ConvolveOp gaussX, gaussY;

    public GaussBlurOp(int kernelRadius)
    {
        assert kernelRadius >= 0;
        int size = kernelRadius*2+1;
        float[] kernel = calculateKernel(kernelRadius);

        gaussX = new ConvolveOp(new Kernel(size, 1, kernel));
        gaussY = new ConvolveOp(new Kernel(1, size, kernel));
    }

    @Override
    public BufferedImage filter(BufferedImage in, BufferedImage out)
    {
        BufferedImage tmp = new BufferedImage(out.getWidth(), out.getHeight(), out.getType());
        gaussX.filter(in, tmp);
        gaussY.filter(tmp, out);
        return out;
    }

    @Override
    public Rectangle2D getBounds2D(BufferedImage bi)
    {
        return gaussX.getBounds2D(bi).createIntersection(gaussY.getBounds2D(bi));
    }

    @Override
    public BufferedImage createCompatibleDestImage(BufferedImage bi,
                                                   ColorModel cm)
    {
        return gaussX.createCompatibleDestImage(bi, cm);
    }

    @Override
    public Point2D getPoint2D(Point2D pd, Point2D pd1)
    {
        return gaussX.getPoint2D(pd, pd1);
    }

    @Override
    public RenderingHints getRenderingHints()
    {
        return gaussX.getRenderingHints();
    }

    private float[] calculateKernel(int kernelRadius)
    {
        float[] a = new float[]{1};
        for (int i = 0; i < kernelRadius*2; i++) {
            float[] b = new float[a.length+1];
            b[0] = 1;
            b[a.length] = 1;
            for (int j = 1; j < a.length; j++) {
                b[j] = a[j-1] + a[j];
            }
            a = b;
        }

        float sum = 0;
        for(float b : a)
            sum += b;

        for (int i = 0; i < a.length; i++) {
            a[i] /= sum;
        }
        return a;
    }
}
