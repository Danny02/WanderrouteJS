package wanderroutejs;

import wanderroutejs.threading.TimedRunnable;
import wanderroutejs.threading.TimedCallable;
import java.awt.Rectangle;
import java.io.*;
import java.util.concurrent.*;
import wanderroutejs.generationtasks.PathCreationTask;
import wanderroutejs.generationtasks.TerrainCreationTask;
import wanderroutejs.generators.*;
import wanderroutejs.threading.*;


/**
 * Hello world!
 * <p/>
 */
public class MightyGenerat0r
{
    public static final float NORMAL_SCALE = 0.01f;
    public static final float HEIGHT_SCALE = 1f / 4500;
    public static final String EXAMPLE_PATH = "/examples/untreusee-1206956.gpx";
    public static final File OUTPUT_PATH;

    static {
        String a = EXAMPLE_PATH.substring(EXAMPLE_PATH.lastIndexOf('/'));
        a = a.substring(0, a.length() - 4);
        OUTPUT_PATH = new File("./" + a + "/");
    }
    private ExecutorService threadPool;
    private final Phaser phaser = new Phaser();

    public synchronized void generate()
    {

        threadPool = Executors.newCachedThreadPool();
        phaser.register();

        long time = System.currentTimeMillis();
        InputStream in = MightyGenerat0r.class.getResourceAsStream(EXAMPLE_PATH);

        final TrackGenerator trackGenerator = TrackGenerator.fromStream(in);
        final Rectangle boundingBox = trackGenerator.getTripBoundingBox();

        System.out.println("Generating data for " + boundingBox);
        
        submitTask("terrain creation", new TerrainCreationTask(boundingBox, this));
        submitTask("path creation", new PathCreationTask(trackGenerator, boundingBox, this));

        // render path into maps

        // render osm into maps

        phaser.arriveAndAwaitAdvance();
        System.out.println("Total generation time: " + (System.currentTimeMillis() - time));

        threadPool.shutdown();
    }

    public <E> Future<E> submitTask(String taskName, Callable<E> callable)
    {
        return threadPool.submit(new TimedCallable<E>(taskName, callable));
    }

    public void submitTask(String taskName, Runnable runnable)
    {
        phaser.register();
        threadPool.submit(new PhasedRunnable(new TimedRunnable(taskName, runnable), phaser));
    }

    public static void main(String[] args)
    {
        MightyGenerat0r test = new MightyGenerat0r();

        test.generate();
    }
}
