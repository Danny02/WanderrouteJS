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

import darwin.core.gui.*;
import darwin.renderer.BasicScene;
import darwin.renderer.dependencies.RendererModul;
import darwin.renderer.geometrie.packed.RenderModel.RenderModelFactory;
import darwin.renderer.geometrie.packed.Shaded;

/**
 *
 * @author daniel
 */
public class RenderTest
{
    private final ClientWindow window;

    @Inject
    public RenderTest(Client client, RenderModelFactory rm, BasicScene scene)
    {

        window = new ClientWindow(800, 600, false, client);

        client.addGLEventListener(scene);

        Shaded s = rm.create(null, null);

        scene.addSceneObject(s);
    }

    public void start() throws InstantiationException
    {
        window.startUp();
    }

    public static void main(String[] args) throws InstantiationException
    {
        Injector injector = Guice.createInjector(new RendererModul());
        RenderTest test = injector.getInstance(RenderTest.class);
        test.start();
    }
}
