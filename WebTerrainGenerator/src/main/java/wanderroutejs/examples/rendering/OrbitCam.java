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
package wanderroutejs.examples.rendering;

import java.util.*;

import darwin.core.controls.*;
import darwin.util.math.base.matrix.Matrix;
import darwin.util.math.base.matrix.Matrix4;
import darwin.util.math.base.vector.*;
import darwin.util.math.composits.ViewMatrix;

/**
 *
 * @author daniel
 */
public class OrbitCam implements ViewModel
{
    private final static float HORIZONTAL_MULTIPLIER = 1 / 100f * 15 * Matrix4.GRAD2RAD;//100pixels will create a 15° turn
    private final static float VERTICAL_MULTIPLIER = 1 / 100f * 15 * Matrix4.GRAD2RAD;
    private final static float ZOOM_SPEED = 1f;
    private final ViewMatrix view = new ViewMatrix();
    private final List<ViewListener> listeners = new ArrayList<>();
    private final ImmutableVector<Vector3> center;
    private final float startDistance;
    private float distance;

    public OrbitCam(ImmutableVector<Vector3> center, float distance)
    {
        startDistance = distance;
        this.center = center.clone();
        resetView();
    }

    @Override
    public ViewMatrix getView()
    {
        return view;
    }

    @Override
    public void dragged(float dx, float dy)
    {
        view.translate(0, 0, -distance);
        view.rotateEuler(dy * VERTICAL_MULTIPLIER,
                         dx * HORIZONTAL_MULTIPLIER, 0);
        view.translate(0, 0, distance);

        fireEvent();
    }

    @Override
    public void steps(int steps, boolean ctrl, boolean shift)
    {
        view.translate(0, 0, steps * ZOOM_SPEED);
    }

    @Override
    public void resetView()
    {
        distance = startDistance;
        view.loadIdentity();
        view.setWorldTranslate(center);
        view.translate(0, 0, distance);

        fireEvent();
    }

    @Override
    public void resetViewX()
    {
        //TODO y eular angle
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void resetViewY()
    {
        //TODO x eular angle
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void resetViewZ()
    {
        //TODO reset distance
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void fireEvent()
    {
        for (ViewListener l : listeners) {
            l.viewChanged(null);
        }
    }

    @Override
    public void addListener(ViewListener listener)
    {
        listeners.add(listener);
    }

    @Override
    public void removeListener(ViewListener listener)
    {
        listeners.remove(this);
    }
}
