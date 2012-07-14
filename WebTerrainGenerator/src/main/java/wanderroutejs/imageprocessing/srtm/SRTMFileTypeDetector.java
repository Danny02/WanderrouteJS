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
package wanderroutejs.imageprocessing.srtm;

import darwin.annotations.ServiceProvider;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.spi.FileTypeDetector;

/**
 *
 * @author daniel
 */
@ServiceProvider(FileTypeDetector.class)
public class SRTMFileTypeDetector extends FileTypeDetector {

    @Override
    public String probeContentType(Path path) throws IOException {
        String fileName = path.toString();
        int suffixPos = fileName.lastIndexOf('.');
        if (suffixPos == -1 || suffixPos == fileName.length() - 1) {
            return null;
        }

        String fileSuffix = fileName.substring(suffixPos + 1);

        SRTMImageReaderSpi spi = new SRTMImageReaderSpi();
        for (String suf : spi.getFileSuffixes()) {
            if (suf.equals(fileSuffix)) {
                return spi.getMIMETypes()[0];
            }
        }
        return null;
    }
}
