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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import javax.media.opengl.GL;
import wanderroutejs.datasources.HeightSource;
import wanderroutejs.examples.ImageFrame;

import darwin.geometrie.data.GenericVector;
import darwin.geometrie.data.*;
import darwin.geometrie.io.*;
import darwin.geometrie.unpacked.*;
import darwin.jopenctm.compression.RawEncoder;
import darwin.util.math.base.Line;
import darwin.util.math.base.vector.Vector;
import darwin.util.math.base.vector.*;
import darwin.util.math.composits.*;

/**
 *
 * @author some
 */
public class PathTraingulator
{
    public Mesh buildPathMesh(Path<Vector2> path, HeightSource height)
    {
        Element pos = new Element(new GenericVector(DataType.FLOAT, 3), "Position");
        VertexBuffer vb = new VertexBuffer(pos, path.size());
        vb.fullyInitialize();
        Iterator<ImmutableVector<Vector2>> iter = path.getVectorIterator();
        for (Vertex v : vb) {
            float[] vec = iter.next().getCoords();
            v.setAttribute(pos, vec[0], height.getHeightValue(vec[0], vec[1]), vec[1]);
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

    public <E extends Vector<E>> Mesh buildExtrudedPrisma(Path<E> path,
                                                          float extrude,
                                                          float height)
    {
        Collection<ImmutableVector<E>> poly = buildExtrudedPolygon(path, extrude);
        int poSz = poly.size();

        Element pos = new Element(new GenericVector(DataType.FLOAT, 3), "Position");
        VertexBuffer vb = new VertexBuffer(pos, poSz * 2);

        //create side-wall
        for (ImmutableVector<E> vector : poly) {
            float[] v = vector.getCoords();
            vb.newVertex().setAttribute(pos, v[0], height, v[1]);
            vb.newVertex().setAttribute(pos, v[0], -height, v[1]);
        }

        int[] indices = new int[((path.size() - 1) * 2 * 2 + poSz * 2) * 3];

        //side-wall  0,1,2  3,2,1  2,3,4  5,4,3
        for (int i = 0; i < poSz - 1; i++) {
            int t = i * 2;
            int[] tmp = triangulate(t, t + 1, t + 2, t + 3);
            System.arraycopy(tmp, 0, indices, i * 6, 6);
        }
        //connect last to first path node
        int t = poSz * 2;
        int[] tmp = triangulate(t - 4, t - 3, t - 2, t - 1);
        System.arraycopy(tmp, 0, indices, poSz * 6, 6);

        int paSz = path.size();
        //top - bottom
        for (int i = 0; i < paSz - 1; i++) {
            int br = paSz + i;
            int tr = paSz + i + 1;
            int bl = paSz - i - 1;
            int tl = paSz - i - 2;
            int offset = (poSz + i) * 6;
            for (int j = 0; j < 2; j++) {
                tmp = triangulate(tl * 2 + j, bl * 2 + j, tr * 2 + j, br * 2 + j);
                System.arraycopy(tmp, 0, indices, offset, 6);
                offset += paSz;
            }
        }

        return new Mesh(indices, vb, GL.GL_TRIANGLES);
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
        Vector<E> ex = rotateCCW(dir).normalize();
        return new Vector[]{
                    ex.mul(extrude).add(end),
                    ex.mul(-extrude).add(end)
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

    private <E extends Vector<E>> Vector<E> rotateCCW(Vector<E> v)
    {
        float[] d = v.getCoords();
        float tmp = d[1];
        d[1] = -d[0];
        d[0] = tmp;
        return v;
    }

    public static void main(String[] args) throws IOException
    {
        Path<Vector2> path = new Path<>();
        path.addPathElement(new Vector2(30, 30));
        path.addPathElement(new Vector2(90, 31));
        path.addPathElement(new Vector2(90, 70));

        PathTraingulator trian = new PathTraingulator();

        BufferedImage image = new BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        g2.setColor(Color.RED);

        Polygon poly = new Polygon();
        for (ImmutableVector<Vector2> v : trian.buildExtrudedPolygon(path, 5)) {
            int x = (int)v.getCoords()[0];
            int y = (int)v.getCoords()[1];
            poly.addPoint(x, y);
            System.out.println(x+"  "+y);
        }
        g2.fillPolygon(poly);

        ImageFrame frame = new ImageFrame(550, 550);
        frame.addImage(image);

        Mesh m = trian.buildExtrudedPrisma(path, 5f, 5);

        try (FileOutputStream out = new FileOutputStream("path.ctm")) {
            ModelWriter writer = new CtmModelWriter(new RawEncoder());
            writer.writeModel(out, new Model[]{new Model(m, null)});
        }
    }
}
