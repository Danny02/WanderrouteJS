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

import wanderroutejs.datasources.HeightSource;
import javax.media.opengl.GL;

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
public class GridHeightmap implements HeightmapGenerator
{
    private final CellFactory factory;

    public GridHeightmap(int tessFactor)
    {
        factory = new CellFactory(tessFactor);
        factory.createCell(new Vector2());
    }

    @Override
    public Mesh generateVertexData(final HeightSource image)
    {
        final Element position = new Element(new GenericVector(DataType.FLOAT, 3), "Position");
        VertexBuffer buffer = new VertexBuffer(position, factory.getVertexCount());

        factory.fillVBufferPerVertex(buffer, new PerVertexFiller()
        {
            @Override
            public void fill(Vertex vertex, Tupel2 pos)
            {
                float h = image.getHeightValue(pos.getX(), pos.getY());
                vertex.setAttribute(position, pos.getX(), pos.getY(), h);
            }
        });

        Cell c = factory.getCells()[0];
        return new Mesh(c.getTriangles(), buffer, GL.GL_TRIANGLES);
    }
}
