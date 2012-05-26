package org.webterraingenerator;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import javax.swing.*;

import darwin.util.math.base.LineSegment;
import darwin.util.math.base.Vector;

/**
 * Hello world!
 * <p/>
 */
public class App
{

    public static void main(String[] args)
    {
        BufferedImage image = new BufferedImage(512, 512, BufferedImage.TYPE_USHORT_GRAY);
        Graphics2D g = (Graphics2D) image.getGraphics();

        Path path = new Path();
        path.addPathElement(new Vector(0, 50));
        path.addPathElement(new Vector(100, 250));
        path.addPathElement(new Vector(200, 50));
        path.addPathElement(new Vector(300, 200));
        path.addPathElement(new Vector(350, 410));
        path.addPathElement(new Vector(360, 30));

        g.setColor(Color.BLUE);

        Polygon p = new Polygon();
        for (Vector v : path.buildExtrudedPolygon(30)) {
            double[] c = v.getCoords();
            p.addPoint((int) c[0], (int) c[1]);
        }
        g.fillPolygon(p);

        g.setColor(Color.RED);

        for (LineSegment ls : path) {
            double[] start = ls.getStart().getCoords();
            double[] end = ls.getEnd().getCoords();

            g.drawLine((int) start[0], (int) start[1],
                    (int) end[0], (int) end[1]);
        }

        JFrame frame = new JFrame();
        JLabel label = new JLabel(new ImageIcon(image));
        frame.getContentPane().add(label);

        frame.setPreferredSize(new Dimension(512, 512));
        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void boxBlur(WritableRaster raster, int kernel)
    {

        int[] tmp = new int[1];
        int[][] data = new int[raster.getWidth()][raster.getHeight()];

        float divisor = 1f / (4 * kernel * kernel + 4 * kernel + 1);
        for (int x = kernel; x < data.length - kernel; x++) {
            for (int y = kernel; y < data[0].length - kernel; y++) {

                int sum = 0;
                for (int kx = -kernel; kx <= kernel; kx++) {
                    for (int ky = -kernel; ky <= kernel; ky++) {
                        raster.getPixel(x + kx, y + ky, tmp);
                        sum += tmp[0];
                    }
                }
                data[x][y] = (int) (sum * divisor);
            }
        }

        for (int x = 0; x < data.length; x++) {
            for (int y = 0; y < data[0].length; y++) {
                tmp[0] = data[x][y];
                raster.setPixel(x, y, tmp);
            }
        }

    }
}
