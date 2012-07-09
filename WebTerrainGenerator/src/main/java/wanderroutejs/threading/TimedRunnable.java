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
package wanderroutejs.threading;

import org.slf4j.*;

/**
 *
 * @author daniel
 */
public class TimedRunnable implements Runnable
{
    private final static Logger logger = LoggerFactory.getLogger(TimedRunnable.class);
    private final String taskName;
    private final Runnable runnable;

    public TimedRunnable(Runnable runnable)
    {
        this(null, runnable);
    }

    public TimedRunnable(String taskName, Runnable runnable)
    {
        if (taskName == null) {
            taskName = runnable.getClass().getSimpleName();
        }
        this.taskName = taskName;
        this.runnable = runnable;
    }

    @Override
    public void run()
    {
        logger.info("Starting task \"" + taskName + "\"...");
        long time = System.currentTimeMillis();
        runnable.run();
        time = System.currentTimeMillis() - time;
        logger.info("Finished task \"" + taskName + "\" in: " + time);
    }
}
