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
package wanderroutejs.datasources;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

import darwin.util.image.GaussBlurOp;
import darwin.util.image.ImageUtil2;
import static java.lang.Math.*;

/**
 *
 * @author daniel
 */
public class ImageHeightSource implements HeightSource
{
    private final BufferedImage heightMap;
    private final float scaleFactor;

    public ImageHeightSource(BufferedImage image, float scaleFactor)
    {
        heightMap = image;//new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        new GaussBlurOp(5).filter(image, heightMap);
        this.scaleFactor = scaleFactor;

//        ImageFrame frame = new ImageFrame(600, 600);
//        frame.addImage(image);;
    }

    public ImageHeightSource(BufferedImage image, int size, float scaleFactor)
    {
        this(ImageUtil2.getScaledImage(image, size, size, false), scaleFactor);
    }

    @Override
    public float getHeightValue(float x, float y)
    {
        if (x < 0 || x > 1 || y < 0 || y > 1) {
            System.out.println(x + "  " + y);
        }
        x = max(0, min(x, 1));
        y = max(0, min(y, 1));

        int[] values = new int[4];
        heightMap.getRaster().getPixel((int) (x * (heightMap.getWidth() - 1)),
                                       (int) (y * (heightMap.getHeight() - 1)),
                                       values);
        return values[0] * scaleFactor;
    }

    @Override
    public Dimension getDataDimension()
    {
        return new Dimension(heightMap.getWidth(), heightMap.getHeight());
    }
}
