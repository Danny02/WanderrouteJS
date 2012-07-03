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

import java.io.*;

import darwin.annotations.ServiceProvider;
import darwin.geometrie.io.*;
import darwin.geometrie.unpacked.Model;

/**
 *
 * @author daniel
 */
@ServiceProvider(ModelReader.class)
public class PlainJSONModelReader implements ModelReader
{
    public static final String FILE_EXTENSION = "json";

    @Override
    public Model[] readModel(InputStream source) throws IOException, WrongFileTypeException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isSupported(String fileExtension)
    {
        return FILE_EXTENSION.equals(fileExtension.toLowerCase());
    }

}
