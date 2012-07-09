package wanderroutejs;

import java.awt.Rectangle;
import java.io.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.logging.Level;
import org.slf4j.*;
import wanderroutejs.generationtasks.*;
import wanderroutejs.generators.TrackGenerator;
import wanderroutejs.threading.*;

import darwin.geometrie.io.ResourcesLoader;

/**
 * Hello world!
 * <p/>
 */
public class MightyGenerat0r
{
    //Defaults
    public static final Logger logger = LoggerFactory.getLogger(MightyGenerat0r.class);
    public static final float DEFAULT_HEIGHT_SCALE = 1f / 4500;
    public static final float DEFAULT_NORMAL_SCALE = 0.01f;
    public static final int DEFAULT_TESS_FACTOR = 100;
    //Parameter
    private final float heightScale, normalScale;
    private final int tessFactor;
    private final File outputDir;
    //Threading fields
    private ExecutorService threadPool;
    private final Phaser phaser = new Phaser();
    private final Lock lock = new ReentrantLock();

    public MightyGenerat0r()
    {
        this(DEFAULT_HEIGHT_SCALE, DEFAULT_NORMAL_SCALE, DEFAULT_TESS_FACTOR, new File("./"));
    }

    public MightyGenerat0r(float heightScale, float normalScale, int tessFactor,
                           File outputDir)
    {
        this.heightScale = heightScale;
        this.normalScale = normalScale;
        this.tessFactor = tessFactor;
        this.outputDir = outputDir;
    }

//TODO problematisch wenn generate von mehreren Threads genutzt wird. Wahrscheinliches verhalten ist,
// dass alle Afruhe der Methode erst beenden wenn auch der letzte Aufruf fertig ist(Wegen dem Phaser,
// jeder aufruf bräuchte nen eigenen Phaser. Versteh die klasse kaum weswegen es auch andere möglichkeiten geben könnte)
    public synchronized void generate(String gpsPathFile) throws IOException
    {
        phaser.register();
        long time = System.currentTimeMillis();


        File test = new File(gpsPathFile);
        if (!test.canRead()) {
            logger.error("Can not read File: "+test.getAbsolutePath());
            throw new IOException("Could not read File");
        }

        InputStream in = new FileInputStream(test);

        final TrackGenerator trackGenerator = TrackGenerator.fromStream(in);
        final Rectangle boundingBox = trackGenerator.getTripBoundingBox();

        logger.info("Generating data for " + boundingBox);

        File outPath = getOutputPath(gpsPathFile);

        if (!outPath.exists()) {
            outPath.mkdirs();
        }

        submitTask("terrain creation", new TerrainCreationTask(boundingBox, this, outPath,
                                                               normalScale, heightScale, tessFactor));
        submitTask("path creation", new PathCreationTask(trackGenerator, boundingBox,
                                                         this, outPath, heightScale));

        // render path into maps

        // render osm into maps

        phaser.arriveAndAwaitAdvance();
        logger.info("Total generation time: " + (System.currentTimeMillis() - time));
    }

    public <E> Future<E> submitTask(String taskName, Callable<E> callable)
    {
        return getExecutor().submit(new TimedCallable<>(taskName, callable));
    }

    public void submitTask(String taskName, Runnable runnable)
    {
        phaser.register();
        getExecutor().submit(new PhasedRunnable(new TimedRunnable(taskName, runnable), phaser));
    }

    private ExecutorService getExecutor()
    {
        lock.lock();
        if (threadPool == null) {
            threadPool = Executors.newCachedThreadPool();
        }
        lock.unlock();
        return threadPool;
    }

    private File getOutputPath(String gpsTrackPath)
    {
        File outPath;
        int pos = gpsTrackPath.lastIndexOf('/');
        String a = gpsTrackPath;
        if (pos != -1) {
            a = gpsTrackPath.substring(pos);
        }
        a = a.substring(0, a.length() - 4);
        outPath = new File(outputDir.getAbsolutePath() + "/" + a + "/");
        return outPath;
    }

    public void shutdown()
    {
        if (threadPool != null) {
            threadPool.shutdown();
            threadPool = null;
        }
    }

    public static void main(String[] args)
    {
        MightyGenerat0r test = new MightyGenerat0r();
        try {

            test.generate("untreusee-1206956.gpx");
        } catch (Throwable t) {
        } finally {
            test.shutdown();
        }
    }
}
