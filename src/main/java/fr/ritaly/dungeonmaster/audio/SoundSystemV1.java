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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.ritaly.dungeonmaster.HasPosition;
import fr.ritaly.dungeonmaster.Position;

/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public class SoundSystemV1 {

	private final Log log = LogFactory.getLog(this.getClass());

	private static final SoundSystemV1 INSTANCE = new SoundSystemV1();

	private ExecutorService executorService;

	private HasPosition listener = new HasPosition() {
		@Override
		public Position getPosition() {
			return new Position(0, 0, 0);
		}
	};

	private boolean initialized;

	private final Map<String, Sound> sounds = new HashMap<String, Sound>();

	private SoundSystemV1() {
	}

	public static final SoundSystemV1 getInstance() {
		return INSTANCE;
	}

	public synchronized boolean isInitialized() {
		return initialized;
	}

	public void close() {
		if (log.isDebugEnabled()) {
			log.debug("Closing sound system ...");
		}

		executorService.shutdownNow();

		try {
			executorService.awaitTermination(5, TimeUnit.SECONDS);
			executorService = null;
		} catch (InterruptedException e) {
			// Pas grave
		}

		if (log.isDebugEnabled()) {
			log.debug("Sound system closed");
		}

		System.exit(0);
	}

	public synchronized void init(final File directory) throws IOException,
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

		this.executorService = Executors.newFixedThreadPool(4);

		if (log.isInfoEnabled()) {
			log.info("Sound system initialized");
		}

		initialized = true;
	}

	public void play(Position position, final AudioClip clip) {
		if (position == null) {
			throw new IllegalArgumentException("The given position is null");
		}
		if (clip == null) {
			throw new IllegalArgumentException("The given audio clip is null");
		}
		if (!isInitialized()) {
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

				final DataLine.Info info = new DataLine.Info(
						SourceDataLine.class, sound.getAudioFormat());

				try {
					final SourceDataLine line = (SourceDataLine) AudioSystem
							.getLine(info);

					line.open(sound.getAudioFormat());
					line.start();
					line.write(sound.getData(), 0, sound.getData().length);
					line.drain();
					line.close();
				} catch (LineUnavailableException e) {
					throw new RuntimeException(e);
				}
			}
		});

		if (log.isDebugEnabled()) {
			log.debug("Task submitted");
		}

		// TODO Calculer distance, pan, atténuation ...
	}

	public final void play(AudioClip clip) {
		play(listener.getPosition(), clip);
	}

	public void setListener(HasPosition listener) {
		if (listener == null) {
			throw new IllegalArgumentException("The given listener is null");
		}

		this.listener = listener;
	}

	public static void main(String[] args) throws Exception {
		// simpleTest();
		// testClip();

		SoundSystemV1
				.getInstance()
				.init(
						new File(
								"c:\\Users\\Francois\\workspace\\Dungeon Master\\sound"));

		for (int i = 0; i < 10; i++) {
			switch (RandomUtils.nextInt(4)) {
			case 0:
				SoundSystemV1.getInstance().play(AudioClip.DOOR_BROKEN);
				break;
			case 1:
				SoundSystemV1.getInstance().play(AudioClip.GLOUPS);
				break;
			case 2:
				SoundSystemV1.getInstance().play(AudioClip.FIRE_BALL);
				break;
			case 3:
				SoundSystemV1.getInstance().play(AudioClip.CHAMPION_DIED);
				break;
			default:
				break;
			}

			Thread.sleep(300);
		}

		while (true) {
			/* sleep for 1 second. */
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// Ignore the exception.
			}

			SoundSystemV1.getInstance().close();
		}
	}
}