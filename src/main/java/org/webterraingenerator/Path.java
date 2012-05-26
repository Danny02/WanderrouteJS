/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.webterraingenerator;

import java.util.*;

import darwin.util.math.base.Line;
import darwin.util.math.base.LineSegment;
import darwin.util.math.base.Vector;

/**
 *
 * @author daniel
 */
public class Path implements Iterable<LineSegment>
{

    private final List<Vector> positions = new ArrayList<>(1 << 10);

    public void addPathElement(Vector position)
    {
        positions.add(position);
    }

    public List<Vector> buildExtrudedPolygon(float extrude)
    {
        if (positions.size() < 2) {
            throw new RuntimeException("The path is not complete, at least to elements have to exist!");
        }
        LinkedList<Vector> poly = new LinkedList<>();
        Iterator<Vector> iter = positions.iterator();

        Vector prev = iter.next();
        Vector next = iter.next();

        //Start poly punkte erstellen
        Vector[] p = generateExtrudedEndPoints(prev, next, extrude);
        poly.addFirst(p[0]);
        poly.addLast(p[1]);

        while (iter.hasNext()) {
            Vector tmp = iter.next();
            p = generateExtrudedPoints(prev, next, tmp, extrude);
            poly.addFirst(p[0]);
            poly.addLast(p[1]);
            prev = next;
            next = tmp;
        }

        //end poly punkte erstellen
        p = generateExtrudedEndPoints(next, prev, extrude);
        poly.addFirst(p[1]);
        poly.addLast(p[0]);

        return poly;
    }

    private Vector[] generateExtrudedEndPoints(Vector end, Vector other,
            float extrude)
    {
        Vector dir = other.sub(end);
        Vector ex = rotateCCW(dir).normalize();
        return new Vector[]{
                    ex.mult(extrude).add(end),
                    ex.mult(-extrude).add(end)
                };
    }

    private Vector[] generateExtrudedPoints(Vector first, Vector mid, Vector last,
            float extrude)
    {
        Vector dir1 = mid.sub(first);
        Vector dir2 = last.sub(mid);

        Vector left = rotateCCW(dir1).normalize().mult(extrude);
        Vector right = left.mult(-1);

        if (dir1.isParrallelTo(dir2)) {
            return new Vector[]{mid.add(left), mid.add(right)};
        }

        Line firstLeft = Line.fromPoints(first.add(left), mid.add(left));
        Line firstRight = Line.fromPoints(first.add(right), mid.add(right));

        left = rotateCCW(dir2).normalize().mult(extrude);
        right = left.mult(-1);

        Line secondLeft = Line.fromPoints(last.add(left), mid.add(left));
        Line secondRight = Line.fromPoints(last.add(right), mid.add(right));


        return new Vector[]{
                    firstLeft.getIntersection(secondLeft),
                    firstRight.getIntersection(secondRight)
                };
    }

    private Vector rotateCW(Vector v)
    {
        float[] d = v.getCoordsF();
        return new Vector(-d[1], d[0]);
    }

    private Vector rotateCCW(Vector v)
    {
        float[] d = v.getCoordsF();
        return new Vector(d[1], -d[0]);
    }

    @Override
    public Iterator<LineSegment> iterator()
    {
        return new Iterator<LineSegment>()
        {

            Iterator<Vector> iter = positions.iterator();
            Vector prev, next = iter.next();

            @Override
            public boolean hasNext()
            {
                return iter.hasNext();
            }

            @Override
            public LineSegment next()
            {
                prev = next;
                next = iter.next();
                return new LineSegment(prev, next);
            }

            @Override
            public void remove()
            {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }
}
