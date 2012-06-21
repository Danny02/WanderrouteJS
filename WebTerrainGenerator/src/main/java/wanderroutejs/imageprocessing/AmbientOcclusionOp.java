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
package wanderroutejs.imageprocessing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import wanderroutejs.examples.ImageFrame;

import darwin.util.math.base.tupel.Tupel2;
import darwin.util.math.base.vector.*;

import static java.lang.Math.*;

/**
 *
 * @author daniel
 */
public class AmbientOcclusionOp
{
    private final Tupel2[] samplePositions;

    public AmbientOcclusionOp(int sampleCount, int sampleOffset,
                              int sampleRadius)
    {
        samplePositions = new Tupel2[sampleCount];

        for (int i = 1; i < sampleCount + 1; i++) {
            float s = i / (float) sampleCount;
            double a = sqrt(s * sampleCount * sampleOffset);
            double b = sqrt(s);
            double x = sin(a) * b;
            double y = cos(a) * b;

            samplePositions[i - 1] = new Vector2((float) x, (float) y).mul(sampleRadius);
        }
    }

    public BufferedImage filter(BufferedImage heightMap, BufferedImage normalMap)
    {
        BufferedImage dest = new BufferedImage(heightMap.getWidth(),
                                               heightMap.getHeight(),
                                               BufferedImage.TYPE_BYTE_GRAY);

        int[] tmp = new int[4];
        for (int x = 0; x < heightMap.getWidth(); ++x) {
            for (int y = 0; y < heightMap.getHeight(); ++y) {
                normalMap.getRaster().getPixel(x, y, tmp);
                ImmutableVector<Vector3> normal = new Vector3(tmp[0] / 255f * 2f - 1f,
                                                              tmp[1] / 255f * 2f - 1f,
                                                              tmp[2] / 255f).normalize();

                heightMap.getRaster().getPixel(x, y, tmp);
                ImmutableVector<Vector3> position = new Vector3(x, y, tmp[0]);

                int count = samplePositions.length;
                double sumOcclusion = 0;
                for (Tupel2 t : samplePositions) {
                    int sx = round(x + t.getX());
                    int sy = round(y + t.getY());

                    if (sx < 0 || sy < 0 || sx >= heightMap.getWidth()
                            || sy >= heightMap.getHeight()) {
                        --count;
                        continue;
                    }

                    heightMap.getRaster().getPixel(sx, sy, tmp);
                    Vector3 dir = new Vector3(sx, sy, tmp[0]).sub(position);
                    double lenSqrt = sqrt(dir.length());
                    double cosa = dir.normalize().dot(normal);
                    if (cosa > 0) {
                        sumOcclusion += cosa / lenSqrt;
                    } else {
                        sumOcclusion += (1 - cosa) / lenSqrt;
                    }
                }
                sumOcclusion /= count;

                dest.getRaster().setPixel(x, y, new int[]{(int) (sumOcclusion * 255)});
            }
        }
        return dest;
    }

    public static void main(String[] args)
    {
        AmbientOcclusionOp a = new AmbientOcclusionOp(16, 16, 5);

        int s = 50;
        BufferedImage image = new BufferedImage(s, s, BufferedImage.TYPE_INT_RGB);

        Graphics2D g = image.createGraphics();
        g.setColor(Color.RED);
        g.drawRect(256, 256, 1, 1);
        for (Tupel2 t : a.samplePositions) {
            g.drawRect(s/2 + round(t.getX()), s/2 + round(t.getY()), 1, 1);
        }

        ImageFrame frame = new ImageFrame(600, 600);
        frame.addImage(image);

    }
}
