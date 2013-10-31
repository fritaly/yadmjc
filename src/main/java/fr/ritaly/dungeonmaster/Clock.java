/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package fr.ritaly.dungeonmaster;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The clock is the source of time ticks and broadcasts ticks to listening
 * objects inside the game. The clock runs in a separate thread and can be
 * paused / resumed.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public class Clock {

	/**
	 * The number of clock ticks within a second. This constant defines the
	 * speed of the clock.
	 *
	 * @see #DEFAULT_PERIOD
	 */
	public static final int ONE_SECOND = 6;

	/**
	 * The number of clock ticks within a minute.
	 */
	public static final int ONE_MINUTE = ONE_SECOND * 60;

	/**
	 * The period (in milliseconds) between 2 clock ticks. This constant defines
	 * the speed of the clock. In the original Dungeon Master game, the clock
	 * ticked 6 times per second hence a period of roughly 166 ms.
	 *
	 * @See {@link #ONE_SECOND}
	 */
	private static final int DEFAULT_PERIOD = 166;

	/**
	 * Sequence representing the number of ticks since the clock started or was
	 * reset.
	 */
	private int tickCount = 1;

	private final class Task implements Runnable {

		/**
		 * Whether the clock is paused.
		 */
		private volatile boolean paused;

		@Override
		public void run() {
			while (!Thread.interrupted()) {
				// When should the next tick occur ?
				final long nextTick = System.currentTimeMillis() + period;

				_tick();

				while (paused) {
					// The clock has been paused, wait for the 'resume' signal
					synchronized (this) {
						try {
							if (log.isDebugEnabled()) {
								log.debug("Pausing clock ...");
							}

							wait();

							if (log.isDebugEnabled()) {
								log.debug("Pause interrupted");
							}
						} catch (InterruptedException e) {
						}
					}
				}

				try {
					// How long should we wait before the next tick ?
					final long duration = nextTick - System.currentTimeMillis();

					if (duration > 0) {
						if (log.isDebugEnabled()) {
							log.debug("Waiting for " + duration + " ms");
						}

						Thread.sleep(duration);
					} else {
						log.warn("Missed tick by " + duration + " ms");
					}
				} catch (InterruptedException e) {
					// Stop requested
					break;
				}

				while (paused) {
					// The clock has been paused, wait for the 'resume' signal
					synchronized (this) {
						try {
							if (log.isDebugEnabled()) {
								log.debug("Pausing clock ...");
							}

							wait();

							if (log.isDebugEnabled()) {
								log.debug("Pause interrupted");
							}
						} catch (InterruptedException e) {
						}
					}
				}
			}
		}

		/**
		 * Pauses the clock.
		 */
		private synchronized void pause() {
			if (paused) {
				throw new IllegalStateException("The timer is already paused");
			}

			paused = true;

			notifyAll();
		}

		/**
		 * Resumes the clock.
		 */
		private synchronized void resume() {
			if (!paused) {
				throw new IllegalStateException("The timer must be paused");
			}

			paused = false;

			notifyAll();
		}
	}

	/**
	 * Enumerates the possible states of the clock.
	 *
	 * @author francois_ritaly
	 */
	private static enum State {
		STOPPED,
		STARTED,
		PAUSED;
	}

	private final Log log = LogFactory.getLog(Clock.class);

	/**
	 * There's only one instance of clock allowed in the game.
	 */
	private static final Clock INSTANCE = new Clock();

	/**
	 * Set containing the listeners to be notified of clock ticks.
	 */
	private final Set<ClockListener> listeners = new HashSet<ClockListener>();

	/**
	 * Buffer set used for storing the clock listeners to be registered at the
	 * next clock tick. Necessary to avoid concurrent modification exceptions
	 * when notifying listeners.
	 */
	private final Set<ClockListener> buffer = Collections.synchronizedSet(new HashSet<ClockListener>());

	/**
	 * Buffer set used for storing the clock listeners to be unregistered at the
	 * next clock tick. Necessary to avoid concurrent modification exceptions
	 * when notifying listeners.
	 */
	private final Set<ClockListener> trash = Collections.synchronizedSet(new HashSet<ClockListener>());

	/**
	 * The clock's thread.
	 */
	private Thread thread;

	/**
	 * The task responsible for pausing / resuming the clock.
	 */
	private final Task task = new Task();

	/**
	 * The clock's state. Should never be null.
	 */
	private State state = State.STOPPED;

	/**
	 * Period (in milliseconds) between 2 clock ticks.
	 */
	private long period = DEFAULT_PERIOD;

	private Clock() {
	}

	/**
	 * Returns the unique instance of {@link Clock}.
	 *
	 * @return the unique instance of {@link Clock}.
	 */
	public static Clock getInstance() {
		return INSTANCE;
	}

	/**
	 * Registers the given clock listener. The listener will be registered at
	 * the next tick.
	 *
	 * @param listener
	 *            an instance of {@link ClockListener} to register. Can't be
	 *            null.
	 */
	public void register(ClockListener listener) {
		Validate.notNull(listener, "The given clock listener is null");

		buffer.add(listener);

		if (log.isDebugEnabled()) {
			log.debug("Registered " + listener);
		}
	}

	/**
	 * Unregisters the given clock listener. The listener will be unregistered
	 * at the next tick.
	 *
	 * @param listener
	 *            an instance of {@link ClockListener} to unregister. Can't be
	 *            null.
	 */
	public void unregister(ClockListener listener) {
		Validate.notNull(listener, "The given clock listener is null");

		trash.add(listener);

		if (log.isDebugEnabled()) {
			log.debug("Unregistered " + listener);
		}
	}

	/**
	 * Pauses the clock.
	 */
	public synchronized void pause() {
		if (!isStarted()) {
			throw new IllegalStateException("The clock isn't started");
		}

		// Pause the thread
		task.pause();

		this.state = State.PAUSED;

		if (log.isInfoEnabled()) {
			log.info("Clock paused");
		}
	}

	/**
	 * Tells whether the clock is started.
	 *
	 * @return whether the clock is started.
	 */
	public synchronized boolean isStarted() {
		return State.STARTED.equals(state);
	}

	/**
	 * Tells whether the clock is stopped.
	 *
	 * @return whether the clock is stopped.
	 */
	public synchronized boolean isStopped() {
		return State.STOPPED.equals(state);
	}

	/**
	 * Resumes the clock.
	 */
	public synchronized void resume() {
		if (!isPaused()) {
			throw new IllegalStateException("The clock must be paused");
		}

		// Resume the thread
		task.resume();

		this.state = State.STARTED;

		if (log.isInfoEnabled()) {
			log.info("Clock resumed");
		}
	}

	/**
	 * Tells whether the clock is paused.
	 *
	 * @return whether the clock is paused.
	 */
	public synchronized boolean isPaused() {
		return State.PAUSED.equals(state);
	}

	/**
	 * Starts the clock.
	 */
	public synchronized void start() {
		if (isStarted()) {
			throw new IllegalStateException("The clock is already started");
		}

		this.state = State.STARTED;

		// Start the clock's thread
		this.thread = new Thread(task);
		this.thread.start();

		if (log.isInfoEnabled()) {
			log.info("Clock started");
		}
	}

	/**
	 * Stops the clock.
	 */
	public synchronized void stop() {
		if (isStopped()) {
			throw new IllegalStateException("The clock is already stopped");
		}

		// Stop the thread
		thread.interrupt();
		thread = null;

		this.state = State.STOPPED;

		if (log.isInfoEnabled()) {
			log.info("Clock stopped");
		}
	}

	private void _tick() {
		if (log.isDebugEnabled()) {
			log.debug(String.format("[----------- Tick #%d -----------]", tickCount));
		}

		// Careful with the order when adding / removing listeners
		if (!buffer.isEmpty()) {
			// There are listeners pending for registration, add them to the live set
			listeners.addAll(buffer);
			buffer.clear();
		}
		if (!trash.isEmpty()) {
			// There are listeners pending for unregistration, remove them from the live set
			listeners.removeAll(trash);
			trash.clear();
		}

		if (!listeners.isEmpty()) {
			if (log.isDebugEnabled()) {
				log.debug(String.format("Clock is notifying %d listener(s) ...", listeners.size()));
			}

			for (final Iterator<ClockListener> it = listeners.iterator(); it.hasNext();) {
				final ClockListener listener = it.next();

				if (!listener.clockTicked()) {
					// The listener is not interested any more in tick events,
					// unregister it right away
					it.remove();

					if (log.isDebugEnabled()) {
						log.debug("Unregistered " + listener);
					}
				}
			}
		}

		tickCount++;
	}

	public synchronized long getPeriod() {
		return period;
	}

	public synchronized void setPeriod(long period) {
		Validate.isTrue(period > 0, String.format("The given period %d must be positive", period));

		this.period = period;
	}

	/**
	 * Make the clock tick. Useful for manually controlling the clock (typically
	 * for tests).
	 */
	public void tick() {
		tick(1);
	}

	/**
	 * Make the clock tick n times. Useful for manually controlling the clock
	 * (typically for tests).
	 *
	 * @param n
	 *            the number of times the clock must tick. Must be positive.
	 */
	public void tick(final int n) {
		Validate.isTrue(n > 0, String.format("The given tick count %d must be positive", n));

		if (isStarted()) {
			throw new IllegalStateException("The clock is started");
		}

		if (log.isDebugEnabled()) {
			log.debug(String.format("Ticking %d time(s) ...", n));
		}

		for (int i = 0; i < n; i++) {
			_tick();
		}

		if (log.isDebugEnabled()) {
			log.debug(String.format("Ticked %d time(s)", n));
		}
	}

	/**
	 * Returns the number identifying the current clock tick.
	 *
	 * @return an integer identifying the clock tick.
	 */
	public int getTickId() {
		return tickCount;
	}

	/**
	 * Resets the clock as if it had just been instantiated.
	 */
	public synchronized void reset() {
		listeners.clear();
		buffer.clear();
		trash.clear();

		tickCount = 1;

		if (log.isInfoEnabled()) {
			log.info("Clock reset");
		}
	}
}