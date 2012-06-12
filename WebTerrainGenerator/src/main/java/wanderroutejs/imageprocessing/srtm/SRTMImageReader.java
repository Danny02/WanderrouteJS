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

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

/**
 *
 * @author daniel
 */
public class SRTMImageReader extends ImageReader
{
    public enum Version
    {
        SRTM1(3601), SRTM3(1201);
        public final int gridSize;

        private Version(int gridSize)
        {
            this.gridSize = gridSize;
        }
    }
    private Version version;

    public SRTMImageReader(ImageReaderSpi spi)
    {
        super(spi);
    }

    @Override
    public int getNumImages(boolean allowSearch) throws IOException
    {
        return 1;
    }

    private void checkStuff(int index)
    {
        if (input == null) {
            throw new IllegalStateException();
        }
        if (index > 0) {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public int getWidth(int imageIndex) throws IOException
    {
        checkStuff(imageIndex);
        return getVersion().gridSize;
    }

    @Override
    public int getHeight(int imageIndex) throws IOException
    {
        checkStuff(imageIndex);
        return getVersion().gridSize;
    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IOException
    {
        checkStuff(imageIndex);
        return new Iterator<ImageTypeSpecifier>()
        {
            @Override
            public boolean hasNext()
            {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public ImageTypeSpecifier next()
            {
                ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
                int[] nBits = {16};
                return new ImageTypeSpecifier(
                        new ComponentColorModel(cs, nBits, false, true,
                                                Transparency.OPAQUE,
                                                DataBuffer.TYPE_USHORT),
                        null);
            }

            @Override
            public void remove()
            {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }

    @Override
    public IIOMetadata getStreamMetadata() throws IOException
    {
        return null;
    }

    @Override
    public IIOMetadata getImageMetadata(int imageIndex) throws IOException
    {
        return null;
    }

    @Override
    public BufferedImage read(int imageIndex, ImageReadParam param) throws IOException
    {
        ImageInputStream stream = getStream();

        int gs = getVersion().gridSize;
        BufferedImage image = new BufferedImage(gs, gs, BufferedImage.TYPE_USHORT_GRAY);

        short[] tmp = new short[gs*gs];
        for (int i = 0; i < tmp.length; i++) {
            tmp[i] = stream.readShort();
        }
        image.getRaster().setDataElements(0, 0, gs, gs, tmp);

        return image;
    }

    private Version getVersion() throws IOException
    {
        if (version == null) {

            ImageInputStream stream = getStream();

            long size = stream.length();
            if (size != -1) {
                if (size / Version.SRTM1.gridSize == Version.SRTM1.gridSize) {
                    version = Version.SRTM1;
                } else {
                    version = Version.SRTM3;
                }
            } else {
                stream.mark();
                size = Version.SRTM3.gridSize;
                stream.skipBytes(size * size * Short.SIZE);
                if (stream.read() == -1) {
                    version = Version.SRTM3;
                } else {
                    version = Version.SRTM1;
                }
                stream.reset();
            }
        }
        return version;
    }

    @Override
    public void setInput(Object input, boolean seekForwardOnly,
                         boolean ignoreMetadata)
    {
        super.setInput(input, seekForwardOnly, ignoreMetadata);
        version = null;
    }

    private ImageInputStream getStream()
    {
        if (input == null) {
            throw new IllegalStateException();
        }
        return (ImageInputStream) input;
    }
}
