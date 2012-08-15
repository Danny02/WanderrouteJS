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

import darwin.annotations.ServiceProvider;
import darwin.geometrie.data.*;
import darwin.geometrie.io.ModelWriter;
import darwin.geometrie.unpacked.*;
import java.io.*;

/**
 *
 * @author daniel
 */
@ServiceProvider(ModelWriter.class)
//TODO mit ner json lib ersetzten
public class PlainJSONModelWriter implements ModelWriter {

    @Override
    public void writeModel(OutputStream out, Model[] model) throws IOException {
        if (model.length != 1) {
            throw new IllegalArgumentException("Can only export a single Model!");
        }
        StringBuilder builder = new StringBuilder().append('{');

        Mesh m = model[0].getMesh();
        int[] indice = m.getIndicies();
        if (indice != null) {
            builder.append("\"index\":");
            builder.append("[");
            for (int i = 0; i < indice.length; i++) {
                builder.append(indice[i]);
                if (i < indice.length - 1) {
                    builder.append(",");
                }
            }
            builder.append("];\n");
        }

        VertexBuffer vbuffer = m.getVertices();
        for (Element ele : vbuffer.layout.getElements()) {
            builder.append("\"")
					.append(ele.getBezeichnung())
					.append("\":");

            builder.append("[");
            for (Vertex v : vbuffer) {
                builder.append("[");
                Number[] data = v.getAttribute(ele);
                for (int i = 0; i < data.length; i++) {
                    builder.append(data[i]);
                    if (i < data.length - 1) {
                        builder.append(",");
                    }
                }
                builder.append("],");
            }
			builder.deleteCharAt(builder.length() - 1);
            builder.append("]");
        }
        builder.append('}');
        out.write(builder.toString().getBytes());
    }

    @Override
    public String getDefaultFileExtension() {
        return "json";//PlainJSONModelReader.FILE_EXTENSION;
    }
}
