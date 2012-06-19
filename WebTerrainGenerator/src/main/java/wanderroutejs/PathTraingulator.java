/*
 * Copyright (C) 2012 some
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * (version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/> 
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301  USA.
 */
package wanderroutejs;

import darwin.geometrie.data.DataType;
import darwin.geometrie.data.Element;
import darwin.geometrie.data.GenericVector;
import darwin.geometrie.data.Vertex;
import darwin.geometrie.data.VertexBuffer;
import darwin.geometrie.unpacked.Mesh;
import darwin.util.math.base.Line;
import darwin.util.math.base.vector.ImmutableVector;
import darwin.util.math.base.vector.Vector;
import darwin.util.math.base.vector.Vector2;
import darwin.util.math.base.vector.Vector3;
import darwin.util.math.composits.LineSegment;
import darwin.util.math.composits.Path;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import javax.media.opengl.GL;
import wanderroutejs.datasources.HeightSource;

/**
 *
 * @author some
 */
public class PathTraingulator {

    public Mesh buildPathMesh(Path<Vector2> path, HeightSource height) {
        Element pos = new Element(new GenericVector(DataType.FLOAT, 3), "Position");
        VertexBuffer vb = new VertexBuffer(pos, path.size());
        vb.fullyInitialize();

        Iterator<ImmutableVector<Vector2>> iter = path.getVectorIterator();
        for (Vertex v : vb) {
            float[] vec = iter.next().getCoords();
            v.setAttribute(pos, vec[0], vec[1], height.getHeightValue(vec[0], vec[1]));
        }
        return new Mesh(null, vb, GL.GL_LINE_STRIP);
    }

    public <E extends Vector<E>> Mesh buildExtrudedPrisma(Path<E> path, float extrude, float height) {
        Deque<ImmutableVector<E>> poly = new LinkedList<>(buildExtrudedPolygon(path, extrude));
        Mesh m = new Mesh(null, null, GL.GL_TRIANGLES);
        return m;
    }

    public <E extends Vector<E>> Collection<ImmutableVector<E>> buildExtrudedPolygon(Path<E> path, float extrude) {
        if (path.size() < 2) {
            throw new RuntimeException("The path is not complete, at least to elements have to exist!");
        }
        Deque<ImmutableVector<E>> poly = new LinkedList<>();
        Iterator<LineSegment<E>> iter = path.iterator();

        LineSegment<E> accLine = iter.next();

        //Start poly punkte erstellen
        ImmutableVector[] p = generateExtrudedEndPoints(accLine.getStart(),
                accLine.getEnd(), extrude);

        poly.addFirst(p[0]);
        poly.addLast(p[1]);

        while (iter.hasNext()) {
            LineSegment<E> newLine = iter.next();
            p = generateExtrudedPoints(accLine.getStart(),
                    accLine.getEnd(), newLine.getEnd(), extrude);
            poly.addFirst(p[0]);
            poly.addLast(p[1]);
            accLine = newLine;
        }

        //end poly punkte erstellen
        p = generateExtrudedEndPoints(accLine.getEnd(), accLine.getStart(), extrude);
        poly.addFirst(p[1]);
        poly.addLast(p[0]);

        return poly;
    }

    private <E extends Vector<E>> Vector<E>[] generateExtrudedEndPoints(
            ImmutableVector<E> end, ImmutableVector<E> other, float extrude) {
        Vector<E> dir = other.clone().sub(end);
        Vector<E> ex = rotateCCW(dir).normalize();
        return new Vector[]{
                    ex.mul(extrude).add(end),
                    ex.mul(-extrude).add(end)
                };
    }

    private <E extends Vector<E>> Vector3[] generateExtrudedPoints(ImmutableVector<E> first,
            ImmutableVector<E> mid,
            ImmutableVector<E> last,
            float extrude) {
        ImmutableVector<E> dir1 = mid.clone().sub(first);
        ImmutableVector<E> dir2 = last.clone().sub(mid);

        Vector<E> left = rotateCCW(dir1.clone()).normalize().mul(extrude);
        Vector<E> right = left.clone().mul(-1);

        if (dir1.isParrallelTo(dir2)) {
            return new Vector3[]{mid.clone().add(left).toVector3(),
                        mid.clone().add(right).toVector3()};
        }

        Line<E> firstLeft = Line.fromPoints(first.clone().add(left), mid.clone().add(left));
        Line<E> firstRight = Line.fromPoints(first.clone().add(right), mid.clone().add(right));

        left = rotateCCW(dir2.clone()).normalize().mul(extrude);
        right = left.clone().mul(-1);

        Line<E> secondLeft = Line.fromPoints(last.clone().add(left), mid.clone().add(left));
        Line<E> secondRight = Line.fromPoints(last.clone().add(right), mid.clone().add(right));


        return new Vector3[]{
                    firstLeft.getIntersection(secondLeft),
                    firstRight.getIntersection(secondRight)
                };
    }

    private <E extends Vector<E>> Vector<E> rotateCCW(Vector<E> v) {
        float[] d = v.getCoords();
        float tmp = d[1];
        d[1] = -d[0];
        d[0] = tmp;
        return v;
    }
}
