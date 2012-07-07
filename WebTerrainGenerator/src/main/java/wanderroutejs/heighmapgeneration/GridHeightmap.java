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

import javax.media.opengl.GL;
import wanderroutejs.datasources.HeightSource;

import darwin.geometrie.data.*;
import darwin.geometrie.factorys.PerVertexFiller;
import darwin.geometrie.factorys.grids.*;
import darwin.geometrie.unpacked.Mesh;
import darwin.util.math.base.tupel.Tupel2;
import darwin.util.math.base.vector.Vector2;

/**
 *
 * @author daniel
 */
public class GridHeightmap implements HeightmapGenerator
{
    private static final Element POSITION = new Element(new GenericVector(DataType.FLOAT, 3), "Position");
    private static final Element AMBIENT = new Element(new GenericVector(DataType.FLOAT, 2), "TexCoord");
    private final CellFactory factory;
    private final HeightSource ao;

    public GridHeightmap(int tessFactor, HeightSource ao)
    {
        factory = new CellFactory(tessFactor);
        factory.createCell(new Vector2());
        this.ao = ao;
    }

    @Override
    public Mesh generateVertexData(final HeightSource image)
    {
        VertexBuffer buffer = new VertexBuffer(new DataLayout(getElements()), factory.getVertexCount());

        factory.fillVBufferPerVertex(buffer, new PerVertexFiller()
        {
            @Override
            public void fill(Vertex vertex, Tupel2 pos)
            {
                fillVertex(pos.getX(), pos.getY(), vertex, image);
            }
        });

        Cell c = factory.getCells()[0];
        return new Mesh(c.getTriangles(), buffer, GL.GL_TRIANGLES);
    }

    protected void fillVertex(float x, float y, Vertex vertex,
                              HeightSource source)
    {
        //x,y reversed to flip triangle order(front back face)
        float h = source.getHeightValue(y, x);
        vertex.setAttribute(POSITION, y - 0.5, h, x - 0.5);

        float a = ao.getHeightValue(y, x);
        vertex.setAttribute(AMBIENT, a, a);
    }

    protected Element[] getElements()
    {
        return new Element[]{POSITION, AMBIENT};
    }
}
