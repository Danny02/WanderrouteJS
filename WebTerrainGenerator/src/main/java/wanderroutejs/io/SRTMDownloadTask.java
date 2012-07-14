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
package wanderroutejs.io;

import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Callable;
import java.util.zip.ZipInputStream;

/**
 *
 * @author daniel
 */
public class SRTMDownloadTask implements Callable<Path> {

    private final URL tileURL;
    private final Path outDir;

    public SRTMDownloadTask(URL tileURL, Path outDir) {
        this.tileURL = tileURL;
        this.outDir = outDir;
    }

    @Override
    public Path call() throws Exception {
        ZipInputStream container = new ZipInputStream(tileURL.openStream());

        String entityName = container.getNextEntry().getName();
        Path tileFile = outDir.resolve(entityName);
        if (Files.notExists(tileFile)) {
            Files.createFile(tileFile);
        }
        FileChannel a = FileChannel.open(tileFile, StandardOpenOption.WRITE);
        a.transferFrom(Channels.newChannel(container), 0, Long.MAX_VALUE);
        return tileFile;
    }
}
