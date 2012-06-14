/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wanderroutejs;

import java.util.*;

import darwin.util.math.base.*;
import darwin.util.math.base.vector.*;

/**
 *
 * @author daniel
 */
public class Path implements Iterable<LineSegment>
{
    private final List<ImmutableVector<Vector2>> positions = new ArrayList<>(1 << 10);

    public void addPathElement(ImmutableVector<Vector2> position)
    {
        positions.add(position.clone());
    }

    public Collection<ImmutableVector<Vector2>> buildExtrudedPolygon(float extrude)
    {
        if (positions.size() < 2) {
            throw new RuntimeException("The path is not complete, at least to elements have to exist!");
        }
        Deque<ImmutableVector<Vector2>> poly = new  LinkedList<>();
        Iterator<ImmutableVector<Vector2>> iter = positions.iterator();

        ImmutableVector<Vector2> prev = iter.next();
        ImmutableVector<Vector2> next = iter.next();

        //Start poly punkte erstellen
        ImmutableVector<Vector2>[] p = generateExtrudedEndPoints(prev, next, extrude);
        poly.addFirst(p[0]);
        poly.addLast(p[1]);

        while (iter.hasNext()) {
            ImmutableVector<Vector2> tmp = iter.next();
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

    private ImmutableVector<Vector2>[] generateExtrudedEndPoints(
            ImmutableVector<Vector2> end, ImmutableVector<Vector2> other, float extrude)
    {
        Vector2 dir = other.clone().sub(end);
        Vector2 ex = rotateCCW(dir).normalize();
        return new Vector2[]{
                    ex.mul(extrude).add(end),
                    ex.mul(-extrude).add(end)
                };
    }

    private Vector2[] generateExtrudedPoints(ImmutableVector<Vector2> first,
                                            ImmutableVector<Vector2> mid,
                                            ImmutableVector<Vector2> last,
                                            float extrude)
    {
        Vector2 dir1 = mid.clone().sub(first);
        Vector2 dir2 = last.clone().sub(mid);

        Vector2 left = rotateCCW(dir1).normalize().mul(extrude);
        Vector2 right = left.clone().mul(-1);

        if (dir1.isParrallelTo(dir2)) {
            return new Vector2[]{mid.clone().add(left), mid.clone().add(right)};
        }

        Line firstLeft = Line.fromPoints(first.clone().add(left), mid.clone().add(left));
        Line firstRight = Line.fromPoints(first.clone().add(right), mid.clone().add(right));

        left = rotateCCW(dir2).normalize().mul(extrude);
        right = left.clone().mul(-1);

        Line secondLeft = Line.fromPoints(last.clone().add(left), mid.clone().add(left));
        Line secondRight = Line.fromPoints(last.clone().add(right), mid.clone().add(right));


        return new Vector2[]{
                    new Vector2(firstLeft.getIntersection(secondLeft)),
                    new Vector2(firstRight.getIntersection(secondRight))
                };
    }

    private Vector2 rotateCW(ImmutableVector<Vector2> v)
    {
        float[] d = v.getCoords();
        return new Vector2(-d[1], d[0]);
    }

    private Vector2 rotateCCW(ImmutableVector<Vector2> v)
    {
        float[] d = v.getCoords();
        return new Vector2(d[1], -d[0]);
    }

    @Override
    public Iterator<LineSegment> iterator()
    {
        return new Iterator<LineSegment>()
        {
            Iterator<ImmutableVector<Vector2>> iter = positions.iterator();
            ImmutableVector<Vector2> prev, next = iter.next();

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
