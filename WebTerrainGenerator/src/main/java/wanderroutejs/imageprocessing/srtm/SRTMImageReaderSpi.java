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

import java.io.IOException;
import java.util.Locale;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import darwin.annotations.ServiceProvider;

/**
 *
 * @author daniel
 */
@ServiceProvider(ImageReaderSpi.class)
public class SRTMImageReaderSpi extends ImageReaderSpi
{

    public SRTMImageReaderSpi()
    {
        inputTypes = new Class[]{ImageInputStream.class};
    }

    @Override
    public String[] getFileSuffixes()
    {
        return new String[]{"hgt","HGT"};
    }

    @Override
    public boolean canDecodeInput(Object source) throws IOException
    {
        if (source == null) {
            throw new IllegalArgumentException();
        }

        if (source instanceof ImageInputStream) {
            ImageInputStream stream = (ImageInputStream) source;
        }
        return false;
    }

    @Override
    public ImageReader createReaderInstance(Object extension) throws IOException
    {
        return new SRTMImageReader(this);
    }

    @Override
    public String getDescription(Locale locale)
    {
        return "Reads SRTM files(*.hgt), which encode a heightmap of a specific region of the world.";
    }
}
