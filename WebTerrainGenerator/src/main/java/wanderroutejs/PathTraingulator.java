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
import wanderroutejs.io.PlainJSONModelWriter;

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

        int polyVertCount = poly.size();
        int pathVertCount = path.size();

        Element pos = new Element(new GenericVector(DataType.FLOAT, 3), "Position");
        VertexBuffer vb = new VertexBuffer(pos, polyVertCount * 2);

        //create side-wall
        for (ImmutableVector<E> vector : poly) {
            float[] v = vector.getCoords();
            vb.newVertex().setAttribute(pos, v[0], v[1], height);
            vb.newVertex().setAttribute(pos, v[0], v[1], -height);
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
                System.arraycopy(tmp, 0, indices, offset+6, 6);

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


        return new Vector3[]{
                    firstLeft.getIntersection(secondLeft),
                    firstRight.getIntersection(secondRight)
                };
    }

    public static void main(String[] args) throws IOException
    {
        Path<Vector2> path = new Path<>();
        path.addPathElement(new Vector2(0.1f, 0.1f));
        path.addPathElement(new Vector2(0.5f, 0.15f));
        path.addPathElement(new Vector2(0.55f, 0.25f));
        path.addPathElement(new Vector2(0.55f, 0.45f));
        path.addPathElement(new Vector2(0.25f, 0.55f));
        path.addPathElement(new Vector2(0.27f, 0.65f));
        path.addPathElement(new Vector2(0.87f, 0.95f));

        PathTraingulator trian = new PathTraingulator();

        int size = 500;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        g2.setColor(Color.RED);

        Polygon poly = new Polygon();
        for (ImmutableVector<Vector2> v : trian.buildExtrudedPolygon(path, 0.01f)) {
            int x = (int) (v.getCoords()[0] * size);
            int y = (int) (v.getCoords()[1] * size);
            poly.addPoint(x, y);
        }
        g2.fillPolygon(poly);

        ImageFrame frame = new ImageFrame(550, 550);
        frame.addImage(image);
//
        Mesh m = trian.buildExtrudedPrisma(path, 0.01f, 5);

        try (FileOutputStream out = new FileOutputStream("path.ctm")) {
            ModelWriter writer = new CtmModelWriter(new RawEncoder());
            writer.writeModel(out, new Model[]{new Model(m, null)});
        }


        Element pos = new Element(new GenericVector(DataType.FLOAT, 3), "Position");
        VertexBuffer vb = new VertexBuffer(pos, path.size());
        for(ImmutableVector<Vector2> v:path.getVectorIterable())
        {
            float[] c = v.getCoords();
            vb.newVertex().setAttribute(pos, c[0], c[1], 0f);
        }

        Mesh m2 = new Mesh(null, vb, -1);
        try (FileOutputStream out = new FileOutputStream("path.json")) {
            ModelWriter writer = new PlainJSONModelWriter();
            writer.writeModel(out, new Model[]{new Model(m2, null)});
        }
    }
}
