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
package org.webterraingenerator;

import java.io.*;

import darwin.geometrie.io.ModelWriter;
import darwin.geometrie.unpacked.Model;

/**
 *
 * @author daniel
 */
public class PlainJSONModelWriter implements ModelWriter
{

    @Override
    public void writeModel(OutputStream out, Model[] model) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getDefaultFileExtension()
    {
        return "json";
    }

}
