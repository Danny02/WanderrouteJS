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
package wanderroutejs.generators;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import wanderroutejs.datasources.HeightSource;
import wanderroutejs.imageprocessing.*;

import darwin.geometrie.data.*;

/**
 *
 * @author daniel
 */
public class GridHeightmapWithNormals extends GridHeightmap
{
    private static final Element NORMAL = new Element(new GenericVector(DataType.FLOAT, 3), "Normal");
    private final BufferedImage normal;

    public GridHeightmapWithNormals(int tessFactor, HeightSource ao,
                                    BufferedImage heightMap)
    {
        super(tessFactor, ao);

        BufferedImage sh = ImageUtil2.getScaledImage(heightMap, tessFactor, tessFactor, false);
        normal = new BufferedImage(sh.getWidth(), sh.getHeight(), BufferedImage.TYPE_INT_RGB);
        new NormalGeneratorOp(0.05f).filter(sh, normal);
    }

    @Override
    protected void fillVertex(float x, float y, Vertex vertex,
                              HeightSource source)
    {
        super.fillVertex(x, y, vertex, source);
        vertex.setAttribute(NORMAL, getNormalValue(y, x));
    }

    @Override
    protected Element[] getElements()
    {
        Element[] old = super.getElements();
        Element[] n = Arrays.copyOf(old, old.length + 1);
        n[old.length] = NORMAL;
        return n;
    }

    private Float[] getNormalValue(float x, float y)
    {
        x = Math.max(0, Math.min(x, 1));
        y = Math.max(0, Math.min(y, 1));

        int[] values = new int[3];
        normal.getRaster().getPixel((int) (x * (normal.getWidth() - 1)),
                                    (int) (y * (normal.getHeight() - 1)),
                                    values);

        return new Float[]{values[0] / 255f * 2 - 1, values[2] / 255f, values[1] / 255f * 2 - 1};
    }
}
