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

import darwin.util.math.base.vector.*;
import darwin.util.math.composits.LineSegment;
import darwin.util.math.composits.Path;
import de.alarie.osmxmlreader.OSMFileParser;
import de.alarie.osmxmlreader.OSMWayObject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.image.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import wanderroutejs.imageprocessing.ImageUtil2;

/**
 *
 * @author daniel
 */
public class PathTestFrame extends JFrame {

    private final BufferedImage image;

    public PathTestFrame() throws HeadlessException, IOException {
        BufferedImage imageOrg = ImageUtil2.loadImage("/examples/N50E011.hgt");
        image = new BufferedImage(imageOrg.getWidth(), imageOrg.getHeight(), imageOrg.getType());
        new RescaleOp(60, 60, null).filter(imageOrg, image);

        JLabel label = new JLabel(new ImageIcon(image));
        getContentPane().add(label);

        setPreferredSize(new Dimension(512, 512));
        pack();
        setVisible(true);
        setLocationRelativeTo(null);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void drawPath(Path<Vector2> path) {
        Graphics2D g = (Graphics2D) image.getGraphics();

//        g.setColor(Color.BLUE);

//        PathTraingulator tria = new PathTraingulator();
//        Polygon p = new Polygon();
//        for (ImmutableVector<Vector2> v : tria.buildExtrudedPolygon(path, 5)) {
//            float[] c = v.getCoords();
//            p.addPoint((int) c[0], (int) c[1]);
//        }
//        g.fillPolygon(p);

        g.setColor(Color.RED);
        Vector2 imgDim = new Vector2(image.getWidth(), image.getHeight());
        for (LineSegment<Vector2> ls : path) {
            Vector2 s = ls.getStart().clone();
            s.sub(new Vector2(50, 11)).mul(imgDim);

            Vector2 e = ls.getEnd().clone();
            e.sub(new Vector2(50, 11)).mul(imgDim);
            float[] start = s.getCoords();
            float[] end = e.getCoords();

            g.drawLine((int) start[0], (int) start[1],
                    (int) end[0], (int) end[1]);
        }

    }

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        List<Path<Vector2>> paths = new ArrayList<>(10000);

        OSMFileParser parser = new OSMFileParser();
        String baseName = "/examples/roadsN50E11.osm.part";
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                InputStream input = PathTestFrame.class.getResourceAsStream(baseName + x + y);
                if (input == null) {
                    throw new IOException("Could not find: " + baseName + x + y);
                }

                Collection<OSMWayObject> objects = parser.parse(input);
                for (OSMWayObject o : objects) {
                    paths.add(o.getPath());
                }
            }
        }

        System.out.println("start drawing..");
        PathTestFrame frame = new PathTestFrame();
        for (Path<Vector2> p : paths) {
            frame.drawPath(p);
        }
        frame.repaint();
        System.out.println("drawing finished!");
    }
}
