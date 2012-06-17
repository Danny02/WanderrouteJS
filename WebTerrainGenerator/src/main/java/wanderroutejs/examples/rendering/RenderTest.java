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
package wanderroutejs.examples.rendering;

import com.google.inject.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import wanderroutejs.datasources.HeightMapSource;
import wanderroutejs.heighmapgeneration.GridWithNormalGenerator;
import wanderroutejs.imageprocessing.ImageUtil2;

import darwin.core.controls.*;
import darwin.core.gui.*;
import darwin.geometrie.unpacked.*;
import darwin.renderer.BasicScene;
import darwin.renderer.dependencies.RendererModul;
import darwin.renderer.geometrie.packed.RenderModel.RenderModelFactory;
import darwin.renderer.geometrie.packed.*;
import darwin.renderer.shader.Shader;
import darwin.resourcehandling.resmanagment.ResourcesLoader;
import darwin.util.math.base.vector.Vector3;

/**
 *
 * @author daniel
 */
public class RenderTest
{
    private final ClientWindow window;
    private final AsyncIni ini;

    @Inject
    public RenderTest(Client client, final RenderModelFactory rm,
                      final BasicScene scene, final ResourcesLoader loader)
    {
        window = new ClientWindow(800, 600, false, client);

        client.addGLEventListener(scene);

        ViewModel view = new OrbitCam(new Vector3(), 10);
        scene.setViewMatrix(view.getView());

        InputController controller = new InputController(view, null, null);
        client.addMouseListener(controller);

        ini = new AsyncIni()
        {
            @Override
            public void ini()
            {

                try {
                    BufferedImage img = ImageUtil2.loadImage("examples/N50E011.hgt");

                    int tessFactor = 100;
                    img = ImageUtil2.getScaledImage(img, tessFactor, tessFactor, false);
                    Mesh mesh = new GridWithNormalGenerator(tessFactor, img).generateVertexData(new HeightMapSource(img, 1f / 4000));
                    Model m = new Model(mesh, null);

                    Shader s = loader.getShader("simpleLambert", false);

                    Shaded obj = rm.create(m, s);

                    scene.addSceneObject(obj);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        };
    }

    public void start() throws InstantiationException
    {
        window.startUp();
        ini.ini();
    }

    public static void main(String[] args) throws InstantiationException
    {
        Injector injector = Guice.createInjector(new RendererModul());
        RenderTest test = injector.getInstance(RenderTest.class);
        test.start();
    }
}
