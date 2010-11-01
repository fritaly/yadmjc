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
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public class Clock {

	/**
	 * Constante définissant le nombre de tics d'horloge équivalant à 1 seconde
	 * de temps de jeu.
	 */
	public static final int ONE_SECOND = 6;

	/**
	 * Constante définissant le nombre de tics d'horloge équivalant à 1 minute
	 * de temps de jeu.
	 */
	public static final int ONE_MINUTE = ONE_SECOND * 60;

	/**
	 * La période de temps (en millisecondes) utilisée comme unité de base dans
	 * le jeu (1/6 de seconde dans Dungeon Master).
	 */
	private static final int DEFAULT_PERIOD = 166;

	private int tickCount = 1;

	private final class Task implements Runnable {

		private volatile boolean paused;

		@Override
		public void run() {
			while (!Thread.interrupted()) {
				// Calculer l'horodatage du prochian tick
				final long nextTick = System.currentTimeMillis() + period;

				_tick();

				while (paused) {
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
					// Calculer le temps à attendre avant le prochain tick afin
					// de "lisser" l'exécution
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
					// Arrêt demandé
					break;
				}

				while (paused) {
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

		private synchronized void pause() {
			if (paused) {
				throw new IllegalStateException("The timer is already paused");
			}

			paused = true;

			notifyAll();
		}

		private synchronized void resume() {
			if (!paused) {
				throw new IllegalStateException("The timer must be paused");
			}

			paused = false;

			notifyAll();
		}
	}

	private static enum State {
		STOPPED,
		STARTED,
		PAUSED;
	}

	private final Log log = LogFactory.getLog(Clock.class);

	private static final Clock INSTANCE = new Clock();

	// Ne pas stocker deux fois la même instance -> Set
	private final Set<ClockListener> listeners = new HashSet<ClockListener>();

	/**
	 * Buffer de stockage des instances de {@link ClockListener} entre deux
	 * ticks (permet d'éviter les modifications concurrentes lors de l'itération
	 * sur le membre {@link #listeners}).
	 */
	private final Set<ClockListener> buffer = Collections
			.synchronizedSet(new HashSet<ClockListener>());
	
	/**
	 * Buffer de stockage des instances de {@link ClockListener} entre deux
	 * ticks (permet d'éviter les modifications concurrentes lors de l'itération
	 * sur le membre {@link #listeners}).
	 */
	private final Set<ClockListener> trash = Collections
			.synchronizedSet(new HashSet<ClockListener>());

	private Thread thread;

	private final Task task = new Task();

	private State state = State.STOPPED;

	/**
	 * Période de temps entre deux tics d'horloge.
	 */
	private long period = DEFAULT_PERIOD;

	private Clock() {
	}

	public static Clock getInstance() {
		return INSTANCE;
	}

	public void register(ClockListener listener) {
		Validate.notNull(listener, "The given clock listener is null");

		buffer.add(listener);

		if (log.isDebugEnabled()) {
			log.debug("Registered " + listener);
		}
	}
	
	public void unregister(ClockListener listener) {
		Validate.notNull(listener, "The given clock listener is null");

		trash.add(listener);

		if (log.isDebugEnabled()) {
			log.debug("Unregistered " + listener);
		}
	}

	public synchronized void pause() {
		if (!isStarted()) {
			throw new IllegalStateException("The clock isn't started");
		}

		// Mettre le thread en pause
		task.pause();

		this.state = State.PAUSED;

		if (log.isInfoEnabled()) {
			log.info("Clock paused");
		}
	}

	public synchronized boolean isStarted() {
		return State.STARTED.equals(state);
	}

	public synchronized boolean isStopped() {
		return State.STOPPED.equals(state);
	}

	public synchronized void resume() {
		if (!isPaused()) {
			throw new IllegalStateException("The clock must be paused");
		}

		// Relancer le thread
		task.resume();

		this.state = State.STARTED;

		if (log.isInfoEnabled()) {
			log.info("Clock resumed");
		}
	}

	public synchronized boolean isPaused() {
		return State.PAUSED.equals(state);
	}

	public synchronized void start() {
		if (isStarted()) {
			throw new IllegalStateException("The clock is already started");
		}

		this.state = State.STARTED;

		// Démarrer thread
		this.thread = new Thread(task);
		this.thread.start();

		if (log.isInfoEnabled()) {
			log.info("Clock started");
		}
	}

	public synchronized void stop() {
		if (isStopped()) {
			throw new IllegalStateException("The clock is already stopped");
		}

		// Arrêter thread
		thread.interrupt();
		thread = null;

		this.state = State.STOPPED;

		if (log.isInfoEnabled()) {
			log.info("Clock stopped");
		}
	}

	private void _tick() {
		if (log.isDebugEnabled()) {
			log.debug("[----------- Tick #" + Integer.toString(tickCount)
					+ " -----------]");
		}

		// Attention à l'ordre de prise en compte des deux Set<ClockListenr> !
		if (!buffer.isEmpty()) {
			// Déplacer les objets du buffer vers le set des listeners
			listeners.addAll(buffer);
			buffer.clear();
		}
		if (!trash.isEmpty()) {
			listeners.removeAll(trash);
			trash.clear();
		}

		if (!listeners.isEmpty()) {
			if (log.isDebugEnabled()) {
				log.debug("Clock is notifying " + listeners.size()
						+ " listener(s) ...");
			}

			for (Iterator<ClockListener> it = listeners.iterator(); it
					.hasNext();) {

				final ClockListener listener = it.next();

				if (!listener.clockTicked()) {
					// L'animation est terminée, supprimer cette entrée du Set
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
		Validate.isTrue(period > 0, "The given period <" + period
				+ "> must be positive");

		this.period = period;
	}

	public void tick() {
		tick(1);
	}

	public void tick(int count) {
		if (count <= 0) {
			throw new IllegalArgumentException("The given tick count <" + count
					+ "> must be positive");
		}
		if (isStarted()) {
			throw new IllegalStateException("The clock is started");
		}

		if (log.isDebugEnabled()) {
			log.debug("Ticking " + count + " time(s) ...");
		}

		for (int i = 0; i < count; i++) {
			_tick();
		}

		if (log.isDebugEnabled()) {
			log.debug("Ticked " + count + " time(s)");
		}
	}

	/**
	 * Retourne l'identifiant du numéro de tic d'horloge.
	 * 
	 * @return un entier positif ou nul.
	 */
	public int getTickId() {
		return tickCount;
	}
}