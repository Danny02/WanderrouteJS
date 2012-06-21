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
package wanderroutejs.heighmapgeneration;

import java.awt.Graphics2D;
import java.awt.image.*;
import java.io.*;
import java.util.logging.*;
import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.xml.transform.Source;
import wanderroutejs.datasources.HeightSource;
import wanderroutejs.examples.ImageFrame;
import wanderroutejs.imageprocessing.*;

import darwin.geometrie.data.*;
import darwin.geometrie.factorys.PerVertexFiller;
import darwin.geometrie.factorys.grids.*;
import darwin.geometrie.unpacked.Mesh;
import darwin.util.math.base.tupel.Tupel2;
import darwin.util.math.base.vector.Vector2;

import static java.lang.Math.*;

/**
 *
 * @author daniel
 */
@Deprecated
public class GridWithNormalGenerator implements HeightmapGenerator
{
    private final CellFactory factory;
    private final BufferedImage normal;

    public GridWithNormalGenerator(int tessFactor, BufferedImage heightMap)
    {
        factory = new CellFactory(tessFactor);
        factory.createCell(new Vector2());

        normal = new BufferedImage(heightMap.getWidth(), heightMap.getHeight(), BufferedImage.TYPE_INT_RGB);
        new NormalGeneratorOp().filter(heightMap, normal);
    }

    @Override
    public Mesh generateVertexData(final HeightSource source)
    {
        final Element position = new Element(new GenericVector(DataType.FLOAT, 3), "Position");
        final Element normalElement = new Element(new GenericVector(DataType.FLOAT, 3), "Normal");
        final Element texcoord = new Element(new GenericVector(DataType.FLOAT, 2), "TexCoord");
        VertexBuffer buffer = new VertexBuffer(new DataLayout(position, texcoord, normalElement), factory.getVertexCount());

        factory.fillVBufferPerVertex(buffer, new PerVertexFiller()
        {
            @Override
            public void fill(Vertex vertex, Tupel2 pos)
            {
                float h = source.getHeightValue(pos.getX(), pos.getY());
                vertex.setAttribute(position, pos.getX(), pos.getY(), h);
                vertex.setAttribute(texcoord, pos.getX(), pos.getY());
                float[] n = getNormalValue(pos.getX(), pos.getY());
                vertex.setAttribute(normalElement, n[0], n[1], n[2]);
            }
        });

        Cell c = factory.getCells()[0];
        return new Mesh(c.getTriangles(), buffer, GL.GL_TRIANGLES);
    }

    private float[] getNormalValue(float x, float y)
    {
//        if (x < 0 || x > 1 || y < 0 || y > 1) {
//            System.err.println("A requested texturecoordinate is out of range! It got clamped. x:" + x + " y:" + y);
//        }
        x = max(0, min(x, 1));
        y = max(0, min(y, 1));

        int[] values = new int[3];
        normal.getRaster().getPixel((int) (x * (normal.getWidth() - 1)),
                                    (int) (y * (normal.getHeight() - 1)),
                                    values);
        return new float[]{values[0] / 255f * 2 - 1, values[1] / 255f * 2 - 1, values[2] / 255f};
    }
}
