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
package fr.ritaly.dungeonmaster.audio;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.ritaly.dungeonmaster.Direction;
import fr.ritaly.dungeonmaster.Position;
import fr.ritaly.dungeonmaster.Utils;

/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public class SoundSystem {

	private static final Log log = LogFactory.getLog(SoundSystem.class);

	private static final SoundSystem INSTANCE = new SoundSystem();

	private ExecutorService executorService;

	private AudioListener listener = new AudioListener() {

		@Override
		public void setDirection(Direction direction) {
		}

		@Override
		public Direction getDirection() {
			return Direction.NORTH;
		}

		@Override
		public Position getPosition() {
			return new Position(0, 0, 0);
		}
	};

	private boolean initialized;

	private final Map<String, Sound> sounds = new HashMap<String, Sound>();

	private SoundSystem() {
	}

	public static final SoundSystem getInstance() {
		return INSTANCE;
	}

	public synchronized boolean isInitialized() {
		return initialized;
	}

	public synchronized void dispose() {
		if (log.isDebugEnabled()) {
			log.debug("Disposing sound system ...");
		}

		executorService.shutdownNow();

		try {
			executorService.awaitTermination(5, TimeUnit.SECONDS);
			executorService = null;
		} catch (InterruptedException e) {
			// Pas grave
		}

		if (log.isDebugEnabled()) {
			log.debug("Sound system disposed");
		}

		System.exit(0);
	}

	public synchronized void init(File directory) throws IOException,
			UnsupportedAudioFileException, LineUnavailableException {

		Validate.isTrue(directory != null, "The given directory is null");
		Validate.isTrue(directory.exists(), "The given directory <"
				+ directory.getAbsolutePath() + "> doesn't exist");
		Validate.isTrue(directory.isDirectory(), "The given path <"
				+ directory.getAbsolutePath() + "> doesn't denote a directory");

		if (initialized) {
			throw new IllegalStateException(
					"The sound system is already initialized");
		}

		if (log.isDebugEnabled()) {
			log.debug("Initializing sound system ...");
		}

		// Lister les fichiers du répertoire
		File[] files = directory.listFiles();

		for (File file : files) {
			final AudioInputStream audioInputStream = AudioSystem
					.getAudioInputStream(file);

			final AudioFormat format = audioInputStream.getFormat();

			final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(
					(int) file.length());

			final byte[] buffer = new byte[4096];

			int count = -1;

			while ((count = audioInputStream.read(buffer)) > 0) {
				outputStream.write(buffer, 0, count);
			}

			final Sound sound = new Sound(outputStream.toByteArray(), format);

			sounds.put(file.getName(), sound);
		}

		// On peut jouer au maximum 4 sons en même temps
		this.executorService = Executors.newFixedThreadPool(4);

		if (log.isInfoEnabled()) {
			log.info("Sound system initialized");
		}

		initialized = true;
	}

	public void play(Position position, final AudioClip clip) {
		if (!isInitialized()) {
			// On ne lève pas d'erreur pour les tests unitaires
			return;
		}
		if (position == null) {
			throw new IllegalArgumentException("The given position is null");
		}
		if (clip == null) {
			throw new IllegalArgumentException("The given audio clip is null");
		}

		final int x1 = listener.getPosition().x;
		final int y1 = listener.getPosition().y;
		final int x2 = position.x;
		final int y2 = position.y;

		double angle = Utils.angle(x1, y1, x2, y2);

		angle += Math.PI;

		switch (listener.getDirection()) {
		case NORTH:
			angle += 0;
			break;
		case SOUTH:
			angle += (Math.PI);
			break;
		case WEST:
			angle += (Math.PI / 2);
			break;
		case EAST:
			angle -= (Math.PI / 2);
			break;
		}

		play(angle, Utils.distance(x1, y1, x2, y2), clip);
	}

	private void play(final double angle, final double distance,
			final AudioClip clip) {

		if (clip == null) {
			throw new IllegalArgumentException("The given audio clip is null");
		}
		if (!isInitialized()) {
			// On ne lève pas d'erreur pour les tests unitaires
			return;
		}

		if (log.isDebugEnabled()) {
			log.debug("Submitting new task ...");
		}

		executorService.execute(new Runnable() {
			@Override
			public void run() {
				final Sound sound = sounds.get(clip.getSound());

				if (sound == null) {
					throw new IllegalArgumentException("Unsupported sound <"
							+ clip + ">");
				}

				try {
					final Clip clip2 = (Clip) AudioSystem
							.getLine(new DataLine.Info(Clip.class, sound
									.getAudioFormat()));

					clip2.addLineListener(new LineListener() {
						@Override
						public void update(LineEvent event) {
							if (event.getType().equals(LineEvent.Type.STOP)) {
								clip2.close();
							}
						}
					});
					clip2.open(sound.getAudioFormat(), sound.getData(), 0,
							sound.getData().length);

					final DecimalFormat decimalFormat = new DecimalFormat(
							"###.#");

					// Pan dans [-1, +1]
					final FloatControl pan = (FloatControl) clip2
							.getControl(FloatControl.Type.PAN);

					if (!Double.isNaN(angle)) {

						final float p = (float) Math.sin(angle);

						if ((0 <= angle) && (angle <= Math.PI)) {
							// Son sur la gauche, pan positif
							pan.setValue(p);
						} else {
							// Son sur la droite, pan négatif
							pan.setValue(p);
						}

						if (log.isDebugEnabled()) {
							log.debug("Angle = "
									+ decimalFormat
											.format((angle / Math.PI) * 180)
									+ "° / Pan = "
									+ decimalFormat.format(p * 100));
						}
					} else {
						pan.setValue(0);
					}

					final double attenuation = Utils.attenuation(distance);

					if (log.isDebugEnabled()) {
						log.debug("Distance = " + distance
								+ " / Attenuation = "
								+ decimalFormat.format((attenuation * 100))
								+ "%");
					}

					if (distance != 0) {
						// Gain dans [-30, 0]
						final FloatControl gain = (FloatControl) clip2
								.getControl(FloatControl.Type.MASTER_GAIN);

						gain.setValue((float) attenuation * -30);
					}

					clip2.loop(0);
				} catch (LineUnavailableException e) {
					throw new RuntimeException(e);
				}
			}
		});

		if (log.isDebugEnabled()) {
			log.debug("Task submitted");
		}
	}

	public final void play(AudioClip clip) {
		play(listener.getPosition(), clip);
	}

	public void setListener(AudioListener listener) {
		if (listener == null) {
			throw new IllegalArgumentException("The given listener is null");
		}

		this.listener = listener;
	}

	public static void main(String[] args) throws Exception {
		// simpleTest();
		// testClip();

		SoundSystem
				.getInstance()
				.init(
						new File(
								"src\\main\\resources\\sound"));

		for (int i = 0; i < 72; i++) {
			SoundSystem.getInstance().play((i * 5.0f / 180.0f) * Math.PI, 1,
					AudioClip.GLOUPS);

			Thread.sleep(500);
		}

		for (int i = 0; i < 10; i++) {
			SoundSystem.getInstance().play(0, i, AudioClip.GLOUPS);

			Thread.sleep(250);
		}

		// SoundSystem.getInstance().setListener(new AudioListener() {
		// @Override
		// public Position getPosition() {
		// return new Position(0, 2, 0);
		// }
		//
		// @Override
		// public Direction getDirection() {
		// return Direction.NORTH;
		// }
		//
		// @Override
		// public void setDirection(Direction direction) {
		// }
		// });
		//
		// for (int x = 0; x < 4; x++) {
		// for (int y = 0; y < 6; y++) {
		// if (((x == 0) || (x == 1))
		// && ((y == 0) || (y == 5) || (y == 4))) {
		// continue;
		// }
		//
		// if (log.isDebugEnabled()) {
		// log.debug("(x,y) = (" + x + "," + y + ")");
		// }
		//
		// SoundSystem.getInstance().play(new Position(x, y, 0),
		// AudioClip.GLOUPS);
		//
		// Thread.sleep(2000);
		// }
		// }

		// for (int i = 0; i < 10; i++) {
		// switch (RandomUtils.nextInt(3) + 1) {
		// case 1:
		// SoundSystem.getInstance().play(AudioClip.GLOUPS);
		// break;
		// case 2:
		// SoundSystem.getInstance().play(AudioClip.FIRE_BALL);
		// break;
		// case 3:
		// SoundSystem.getInstance().play(AudioClip.CHAMPION_DIED);
		// break;
		// default:
		// break;
		// }
		//
		// Thread.sleep(1000);
		// }

		while (true) {
			/* sleep for 1 second. */
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// Ignore the exception.
			}

			SoundSystem.getInstance().dispose();
		}
	}
}