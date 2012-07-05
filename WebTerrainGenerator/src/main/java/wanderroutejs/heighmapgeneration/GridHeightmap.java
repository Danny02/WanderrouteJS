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
        final Element position = new Element(new GenericVector(DataType.FLOAT, 3), "Position");
        final Element ambient = new Element(new GenericVector(DataType.FLOAT, 3), "Normal");
        VertexBuffer buffer = new VertexBuffer(new DataLayout(position, ambient), factory.getVertexCount());

        factory.fillVBufferPerVertex(buffer, new PerVertexFiller()
        {
            @Override
            public void fill(Vertex vertex, Tupel2 pos)
            {
                float x = pos.getX();
                float y = pos.getY();

                float h = image.getHeightValue(x, y);
                vertex.setAttribute(position, x-0.5, h, y-0.5);

                float a = ao.getHeightValue(x, y);
                vertex.setAttribute(ambient, a, a, a);
            }
        });

        Cell c = factory.getCells()[0];
        return new Mesh(c.getTriangles(), buffer, GL.GL_TRIANGLES);
    }
}
