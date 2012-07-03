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

import de.alarie.osmxmlreader.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import sun.net.util.URLUtil;
import wanderroutejs.PathTraingulator;
import wanderroutejs.datasources.*;
import wanderroutejs.imageprocessing.ImageUtil2;
import wanderroutejs.io.PlainJSONModelWriter;

import darwin.geometrie.io.ModelWriter;
import darwin.geometrie.unpacked.Model;
import darwin.util.math.base.vector.Vector2;
import darwin.util.math.composits.*;

/**
 *
 * @author daniel
 */
public class PathTestFrame extends JFrame {

    private final BufferedImage image;

    public PathTestFrame(BufferedImage imageOrg) {
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
        BufferedImage imageOrg = ImageUtil2.loadImage(PathTestFrame.class.getResource("/examples/N50E011.hgt"));

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
        PathTestFrame frame = new PathTestFrame(imageOrg);
        for (Path<Vector2> p : paths) {
            frame.drawPath(p);
        }
        frame.repaint();
        System.out.println("drawing finished!");

        PathTraingulator tria = new PathTraingulator();
        HeightSource source = new HeightMapSource(imageOrg, 1f / 6000);

        System.out.println("start exporting mesh..");
        ModelWriter writer = new PlainJSONModelWriter();
        try (OutputStream out = new FileOutputStream("test.json")) {
            for (Path<Vector2> p : paths) {
                Model[] models = new Model[]{new Model(tria.buildPathMesh(p, source), null)};
                writer.writeModel(out, models);
            }
        }
        System.out.println("exporting finished!");
    }
}
