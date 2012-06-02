package org.webterraingenerator;

import java.io.*;

import darwin.geometrie.io.*;
import darwin.geometrie.unpacked.*;
import darwin.jopenctm.compression.*;

/**
 * Hello world!
 * <p/>
 */
public class App
{
    public static void main(String[] args) throws IOException
    {
        HeightmapGenerator generator = new GridHeightmap(256);
        Mesh mesh = generator.generateVertexData(new TestImage());
        Model m = new Model(mesh, null);

        try (OutputStream out = new FileOutputStream("test2.ctm");) {
            ModelWriter writer = new CtmModelWriter(new MG2Encoder());
            writer.writeModel(out, new Model[]{m});
        }
    }
}
