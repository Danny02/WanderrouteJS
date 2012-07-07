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

import java.util.concurrent.Phaser;

/**
 *
 * @author daniel
 */
public class PhasedRunnable implements Runnable
{
    private final Runnable runnable;
    private final Phaser phaser;

    public PhasedRunnable(Runnable runnable, Phaser phaser)
    {
        this.runnable = runnable;
        this.phaser = phaser;
    }

    @Override
    public void run()
    {
        runnable.run();
        phaser.arrive();
    }

}
