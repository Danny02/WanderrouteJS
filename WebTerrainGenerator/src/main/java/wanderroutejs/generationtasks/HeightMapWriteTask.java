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
import java.nio.file.Path;
import javax.imageio.ImageIO;

/**
 *
 * @author daniel
 */
public class HeightMapWriteTask implements Runnable {

    private final BufferedImage height;
    private final Path outPath;

    public HeightMapWriteTask(BufferedImage height, Path outPath) {
        this.height = height;
        this.outPath = outPath;
    }

    @Override
    public void run() {
        try {
            BufferedImage hout = new BufferedImage(height.getWidth(), height.getHeight(), height.getType());
            new RescaleOp(30, 30, null).filter(height, hout);
            ImageIO.write(hout, "png", outPath.resolve("heihgtmap.png").toFile());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
