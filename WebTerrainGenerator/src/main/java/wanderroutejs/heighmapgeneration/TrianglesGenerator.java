package wanderroutejs.heighmapgeneration;

import javax.media.opengl.GL;
import wanderroutejs.datasources.HeightSource;

import darwin.geometrie.data.*;
import darwin.geometrie.unpacked.Mesh;

@Deprecated
public class TrianglesGenerator implements HeightmapGenerator
{
    private final int tessfactor;

    public TrianglesGenerator(int tessfactor)
    {
        this.tessfactor = tessfactor;
    }

    @Override
    public Mesh generateVertexData(HeightSource source)
    {
        int xHalf = tessfactor / 2;
        int zHalf = tessfactor / 2;
        float tf = 1f / tessfactor;

        Element position = new Element(new GenericVector(DataType.FLOAT, 3), "Position");
        VertexBuffer vbuffer = new VertexBuffer(position, tessfactor * tessfactor * 6);

        for (int z = 0; z < tessfactor - 1; ++z) {
            for (int x = 0; x < tessfactor; ++x) {
                int xPos = x;
                int zPos = z;

                float xf = (xPos - xHalf) * tf;
                float zf = (zPos - zHalf) * tf;

                Float[] data2 = new Float[]{xf, source.getHeightValue(xPos, z), zf};
                Float[] data3 = new Float[]{xf + tf, source.getHeightValue(xPos + 1, z + 1), zf + tf};

                vbuffer.newVertex().setAttribute(position, xf,
                                                 source.getHeightValue(xPos, z + 1), zf + tf);
                vbuffer.newVertex().setAttribute(position, data2);
                vbuffer.newVertex().setAttribute(position, data3);

                vbuffer.newVertex().setAttribute(position, data3);
                vbuffer.newVertex().setAttribute(position, data2);
                vbuffer.newVertex().setAttribute(position, xf + tf,
                                                 source.getHeightValue(xPos + 1, z), zf);
            }

        }

        return new Mesh(null, vbuffer, GL.GL_TRIANGLES);
    }
}
