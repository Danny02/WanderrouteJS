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

import java.util.concurrent.Callable;

/**
 *
 * @author daniel
 */
public class TimedCallable<E> implements Callable<E>
{
    private final String taskName;
    private final Callable<E> callable;

    public TimedCallable(Callable<E> callable)
    {
        this(callable.getClass().getSimpleName(), callable);
    }

    public TimedCallable(String taskName,
                         Callable<E> callable)
    {
        this.taskName = taskName;
        this.callable = callable;
    }

    @Override
    public E call() throws Exception
    {
        System.out.println("Starting task \"" + taskName + "\"...");
        long time = System.currentTimeMillis();
        E res = callable.call();
        time = System.currentTimeMillis() - time;
        System.out.println("Finished task \"" + taskName + "\" in: " + time);
        return res;
    }
}
