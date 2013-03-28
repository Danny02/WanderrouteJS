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
package wanderroutejs.examples;

import wanderroutejs.MightyGenerat0r;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import javax.swing.*;
import wanderroutejs.generators.PathTriangulator;
import wanderroutejs.generators.TrackGenerator;

import darwin.util.math.base.vector.*;
import darwin.util.math.composits.*;

/**
 *
 * @author daniel
 */
public class PathTestFrame extends JFrame
{
    private final BufferedImage image;

    public PathTestFrame(int scale)
    {
        image = new BufferedImage(scale, scale, BufferedImage.TYPE_INT_RGB);

        JLabel label = new JLabel(new ImageIcon(image));
        getContentPane().add(label);

        setPreferredSize(new Dimension(scale + 50, scale + 50));
        pack();
        setVisible(true);
        setLocationRelativeTo(null);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void drawPath(Path<Vector3> path)
    {
        Graphics2D g = (Graphics2D) image.getGraphics();

        g.setColor(Color.RED);
        ImmutableVector<Vector3> imgDim = new Vector3(image.getWidth(), 0, image.getHeight());
        ImmutableVector<Vector3> imgOfset = new Vector3(100, 0, 400);
        for (LineSegment<Vector3> ls : path.getLineSegmentIterable()) {
            float scale = 4;
            float[] start = ls.getStart().clone().mul(imgDim).sub(imgOfset).mul(scale).getCoords();
            float[] end = ls.getEnd().clone().mul(imgDim).sub(imgOfset).mul(scale).getCoords();

            g.drawLine((int) start[0], (int) start[2],
                       (int) end[0], (int) end[2]);
        }

    }

    public static void main(String[] args)
    {
        InputStream in = MightyGenerat0r.class.getResourceAsStream("/examples/untreusee-1206956.gpx");
        TrackGenerator trackGenerator = TrackGenerator.fromStream(in);
        Rectangle boundingBox = trackGenerator.getTripBoundingBox();
        Path<Vector3> path = trackGenerator.getTripAsPath(4500,
                                                          -boundingBox.x,
                                                          -boundingBox.y);

        PathTestFrame frame = new PathTestFrame(512);
        PathTriangulator trian = new PathTriangulator();
        path = trian.simplify(path, 2.7);
        frame.drawPath(path);
    }
}
