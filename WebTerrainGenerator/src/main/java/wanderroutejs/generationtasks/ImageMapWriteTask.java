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
import java.io.*;
import java.nio.file.Path;
import java.util.concurrent.*;
import javax.imageio.ImageIO;

/**
 *
 * @author daniel
 */
public class ImageMapWriteTask implements Runnable {

    private final Future<BufferedImage> image;
    private final String imageName;
    private final Path outPath;

    public ImageMapWriteTask(Future<BufferedImage> image, String imageName,
            Path outPath) {
        this.image = image;
        this.imageName = imageName;
        this.outPath = outPath;
    }

    @Override
    public void run() {
        try {
            ImageIO.write(image.get(), "png", outPath.resolve(imageName + ".png").toFile());
        } catch (InterruptedException | ExecutionException | IOException ex) {
            ex.printStackTrace();
        }
    }
}
