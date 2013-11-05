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
package fr.ritaly.dungeonmaster.item.drink;

import org.apache.commons.lang.Validate;

import fr.ritaly.dungeonmaster.audio.AudioClip;
import fr.ritaly.dungeonmaster.audio.SoundSystem;
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.body.BodyPart;
import fr.ritaly.dungeonmaster.item.Item;

/**
 * A water skin contains 0 up to 3 doses of water.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public class WaterSkin extends Item {

	/**
	 * The maximum number of water doses in the water skin.
	 */
	private static final int MAX_DOSES = 3;

	/**
	 * The remaining water doses in the water skin. Within range [0,3].
	 */
	private int doses;

	/**
	 * Creates a new full water skin.
	 */
	public WaterSkin() {
		super(Type.WATER_SKIN);
	}

	@Override
	public int getShield() {
		return 0;
	}

	@Override
	public int getAntiMagic() {
		return 0;
	}

	@Override
	public BodyPart.Type getActivationBodyPart() {
		return null;
	}

	@Override
	public float getWeight() {
		return 0.3f + (doses * 0.3f);
	}

	/**
	 * Tells whether the water skin is empty.
	 *
	 * @return whether the water skin is empty.
	 */
	public boolean isEmpty() {
		return (doses == 0);
	}

	/**
	 * Tells whether the water skin is full.
	 *
	 * @return whether the water skin is full.
	 */
	public boolean isFull() {
		return (doses == MAX_DOSES);
	}

	/**
	 * Drinks one dose of water from the water skin.
	 */
	public void drink() {
		if (isEmpty()) {
			throw new IllegalStateException("The water skin is empty");
		}

		// Play the associated sound
		SoundSystem.getInstance().play(AudioClip.GLOUPS);

		doses--;

		fireChangeEvent();
	}

	/**
	 * Fills the water skin. After this call, the water skin has
	 * {@value #MAX_DOSES} doses. Won't fail if the water skin is already full.
	 */
	public void fill() {
		// Play the associated sound
		SoundSystem.getInstance().play(AudioClip.REFILL);

		doses = MAX_DOSES;

		fireChangeEvent();
	}

	@Override
	protected Item consume(Champion champion) {
		Validate.notNull(champion, "The given champion is null");

		if (!isEmpty()) {
			// The champion drinks
			drink();

			// The champion's water stat increases by 150 points
			champion.getStats().getWater().inc(150);
		}

		// The item can be refilled contrary to a water flask
		return this;
	}
}