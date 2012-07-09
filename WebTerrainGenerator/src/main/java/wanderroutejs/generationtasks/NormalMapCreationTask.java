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

import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;
import wanderroutejs.imageprocessing.NormalGeneratorOp;

/**
 *
 * @author daniel
 */
public class NormalMapCreationTask implements Callable<BufferedImage>
{
    private final BufferedImage height;
    private final float normalScale;

    public NormalMapCreationTask(BufferedImage height, float normalScale)
    {
        this.height = height;
        this.normalScale = normalScale;
    }

    @Override
    public BufferedImage call() throws Exception
    {
        BufferedImage normal = new BufferedImage(height.getWidth(), height.getHeight(), BufferedImage.TYPE_INT_RGB);
        new NormalGeneratorOp(normalScale).filter(height, normal);
        return normal;
    }
}
