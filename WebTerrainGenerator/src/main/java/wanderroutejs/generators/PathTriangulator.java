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
package wanderroutejs.generators;

import java.util.*;
import javax.media.opengl.GL;
import wanderroutejs.datasources.HeightSource;

import darwin.geometrie.data.GenericVector;
import darwin.geometrie.data.*;
import darwin.geometrie.unpacked.Mesh;
import darwin.util.math.base.Line;
import darwin.util.math.base.vector.Vector;
import darwin.util.math.base.vector.*;
import darwin.util.math.composits.*;

/**
 *
 * @author some
 */
public class PathTriangulator
{
    public Mesh buildPathMesh(Path<Vector2> path, HeightSource height)
    {
        Element pos = new Element(new GenericVector(DataType.FLOAT, 3), "Position");
        VertexBuffer vb = new VertexBuffer(pos, path.size());
        vb.fullyInitialize();
        Iterator<ImmutableVector<Vector2>> iter = path.getVectorIterator();
        for (Vertex v : vb) {
            float[] vec = iter.next().getCoords();
            float z = 0f;
            if (height != null) {
                z = height.getHeightValue(vec[0], vec[1]);
            }
            v.setAttribute(pos, vec[0], z, vec[1]);
        }
        return new Mesh(null, vb, GL.GL_LINE_STRIP);
    }

    public Mesh buildPathMesh(Path<Vector3> path)
    {
        Element pos = new Element(new GenericVector(DataType.FLOAT, 3), "Position");
        VertexBuffer vb = new VertexBuffer(pos, path.size());
        vb.fullyInitialize();

        Iterator<ImmutableVector<Vector3>> iter = path.getVectorIterator();
        for (Vertex v : vb) {
            float[] vec = iter.next().getCoords();
            v.setAttribute(pos, vec[0], vec[1], vec[2]);
        }
        return new Mesh(null, vb, GL.GL_LINE_STRIP);
    }

    public Mesh buildExtrudedPrisma(float extrude, float height,
                                    Path<Vector3> path)
    {
        Path<Vector2> p2 = new Path<>();
        for (ImmutableVector<Vector3> v : path.getVectorIterable()) {
            float[] c = v.getCoords();
            p2.addPathElement(new Vector2(c[0], c[2]));
        }

        return buildExtrudedPrisma(p2, extrude, height);
    }

    public Mesh buildExtrudedPrisma(Path<Vector2> path, float extrude,
                                    float height)
    {
        Collection<ImmutableVector<Vector2>> poly = buildExtrudedPolygon(path, extrude);

        int polyVertCount = poly.size();
        int pathVertCount = path.size();

        Element pos = new Element(new GenericVector(DataType.FLOAT, 3), "Position");
        VertexBuffer vb = new VertexBuffer(pos, polyVertCount * 2);

        for (ImmutableVector<Vector2> vector : poly) {
            float[] v = vector.getCoords();
            vb.newVertex().setAttribute(pos, v[0], height, v[1]);
            vb.newVertex().setAttribute(pos, v[0], -height, v[1]);
        }

        int[] indices = new int[(polyVertCount * 2 + (pathVertCount - 1) * 4) * 3];

        //side-wall  0,1,2  3,2,1  2,3,4  5,4,3
        for (int i = 0; i < polyVertCount - 1; i++) {
            int t = i * 2;
            int[] tmp = triangulate(t, t + 1, t + 2, t + 3);
            System.arraycopy(tmp, 0, indices, i * 6, 6);
        }
        //connect last to first path node
        int t = polyVertCount * 2;
        int[] tmp = triangulate(t - 2, t - 1, 0, 1);
        System.arraycopy(tmp, 0, indices, (polyVertCount - 1) * 6, 6);

        int wallOffset = polyVertCount * 6;
//        top - bottom
        for (int i = 0; i < pathVertCount - 1; i++) {
            int br = pathVertCount + i;
            int tr = pathVertCount + i + 1;
            int bl = pathVertCount - i - 1;
            int tl = pathVertCount - i - 2;
            int offset = wallOffset + i * 12;
            tmp = triangulate(tl * 2, bl * 2, tr * 2, br * 2);
            System.arraycopy(tmp, 0, indices, offset, 6);

            tmp = triangulate(tl * 2 + 1, bl * 2 + 1, tr * 2 + 1, br * 2 + 1);
            System.arraycopy(tmp, 0, indices, offset + 6, 6);

        }

        return new Mesh(indices, vb, GL.GL_TRIANGLES);
    }

    public <E extends Vector<E>> Path<E> simplify(Path<E> path, double threshold)
    {
        if (path.size() < 3) {
            return path;
        }

        Path<E> newPath = new Path<>();
        Iterator<LineSegment<E>> segments = path.iterator();

        int discardCount = 0;

        LineSegment<E> last = segments.next();
        while (true) {
            ImmutableVector<E> start = last.getStart();

            newPath.addPathElement(last.getStart());
            last = segments.next();

            E dir1 = start.clone().sub(last.getStart());
            E dir2 = start.clone().sub(last.getEnd());
            E dir3 = last.getStart().clone().sub(last.getEnd());

            double a = dir1.length();
            double b = dir2.length();
            double c = dir3.length();
            double s = (a+b+c) * 0.5;
            double area = Math.sqrt(s*(s-a)*(s-b)*(s-c));

            if (area < threshold) {
                discardCount++;
                if (!segments.hasNext()) {
                    break;
                }
                last = segments.next();
            }

            if (!segments.hasNext()) {
                newPath.addPathElement(last.getStart());
                break;
            }
        }

        System.out.println("discarded: " + discardCount + '(' + (discardCount / (float) path.size() * 100) + "%)");

        newPath.addPathElement(last.getEnd());
        return newPath;
    }

    private int[] triangulate(int a, int b, int c, int d)
    {
        return new int[]{a, b, c,
                         d, c, b};
    }

    public <E extends Vector<E>> Collection<ImmutableVector<E>> buildExtrudedPolygon(
            Path<E> path, float extrude)
    {
        if (path.size() < 2) {
            throw new RuntimeException("The path is not complete, at least two elements have to exist!");
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
            ImmutableVector<E> end, ImmutableVector<E> other, float extrude)
    {
        Vector<E> dir = other.clone().sub(end);
        ImmutableVector<E> ex = dir.clone().rotateCCW(2).normalize();
        return new Vector[]{
                    ex.clone().mul(extrude).add(end),
                    ex.clone().mul(-extrude).add(end)
                };
    }

    private <E extends Vector<E>> Vector3[] generateExtrudedPoints(
            ImmutableVector<E> first,
            ImmutableVector<E> mid,
            ImmutableVector<E> last,
            float extrude)
    {
        ImmutableVector<E> dir1 = mid.clone().sub(first);
        ImmutableVector<E> dir2 = last.clone().sub(mid);

        ImmutableVector<E> left = dir1.clone().rotateCCW(2).normalize().mul(extrude);
        ImmutableVector<E> right = left.clone().mul(-1);

        if (dir1.isParrallelTo(dir2)) {
            return new Vector3[]{mid.clone().add(left).toVector3(),
                                 mid.clone().add(right).toVector3()};
        }

        Line<E> firstLeft = Line.fromPoints(first.clone().add(left), mid.clone().add(left));
        Line<E> firstRight = Line.fromPoints(first.clone().add(right), mid.clone().add(right));

        left = dir2.clone().rotateCCW(2).normalize().mul(extrude);
        right = left.clone().mul(-1);

        Line<E> secondLeft = Line.fromPoints(last.clone().add(left), mid.clone().add(left));
        Line<E> secondRight = Line.fromPoints(last.clone().add(right), mid.clone().add(right));

        try {
            Vector3 leftResult = firstLeft.getIntersection(secondLeft);
            Vector3 rightResult = firstRight.getIntersection(secondRight);
            return new Vector3[]{leftResult, rightResult};
        } catch (Throwable t) {
            System.out.println(t);
            dir1.isParrallelTo(dir2);
            throw t;
        }
    }
}
