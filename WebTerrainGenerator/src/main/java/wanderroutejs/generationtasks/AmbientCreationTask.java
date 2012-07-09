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
package wanderroutejs.generationtasks;

import java.awt.image.*;
import java.util.concurrent.Callable;
import wanderroutejs.imageprocessing.*;

/**
 *
 * @author daniel
 */
public class AmbientCreationTask implements Callable<BufferedImage>
{
    private final BufferedImage height;
    private final float normalScale;

    public AmbientCreationTask(BufferedImage height, float normalScale)
    {
        this.height = height;
        this.normalScale = normalScale;
    }

    @Override
    public BufferedImage call() throws Exception
    {
        int scale = 512;
        BufferedImage low = ImageUtil2.getScaledImage(height, scale, scale, false);
        //blur the image several times
        BufferedImageOp gauss = new GaussBlurOp(10);
        BufferedImage pingPongBuffer = new BufferedImage(scale, scale, height.getType());
        gauss.filter(low, pingPongBuffer);
        gauss.filter(pingPongBuffer, low);
        gauss.filter(low, pingPongBuffer);
        BufferedImage normal2 = new BufferedImage(scale, scale, BufferedImage.TYPE_INT_RGB);
        new NormalGeneratorOp(normalScale).filter(pingPongBuffer, normal2);
        BufferedImage ao = new AmbientOcclusionOp(64, 16, 20).filter(pingPongBuffer, normal2);
        return ao;
    }
}
