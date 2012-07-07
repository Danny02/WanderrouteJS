package wanderroutejs.generators;

import javax.media.opengl.GL;
import wanderroutejs.datasources.HeightSource;

import darwin.geometrie.data.*;
import darwin.geometrie.unpacked.Mesh;

@Deprecated
public class TriangleStripGenerator implements HeightmapGenerator
{
    private final int tessfactor;

    public TriangleStripGenerator(int tessfactor)
    {
        this.tessfactor = tessfactor;
    }

    @Override
    public Mesh generateVertexData(HeightSource source)
    {
        float tf = 1f / tessfactor;

        Element position = new Element(new GenericVector(DataType.FLOAT, 3), "Position");
        VertexBuffer vbuffer = new VertexBuffer(position, tessfactor * tessfactor * 2);

        for (int z = 0; z < tessfactor - 1; ++z) {
            boolean dir = z % 2 == 1;
            for (int x = 0; x < tessfactor; ++x) {
                int xPos = dir ? z : tessfactor - 1 - z;

                Vertex oben = vbuffer.newVertex();
                Vertex unten = vbuffer.newVertex();

                if (dir) {
                    Vertex t = unten;
                    unten = oben;
                    oben = t;
                }

                float xf = xPos*tf;
                float zf = z*tf;
                float zof = (z+1)*tf;

                oben.setAttribute(position, xf, source.getHeightValue(xf, zof), -zof);
                unten.setAttribute(position, xf, source.getHeightValue(xf, zf), -zf);
            }

        }

        return new Mesh(null, vbuffer, GL.GL_TRIANGLE_STRIP);
    }
}
