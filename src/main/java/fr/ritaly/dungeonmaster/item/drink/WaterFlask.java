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
import fr.ritaly.dungeonmaster.item.EmptyFlask;
import fr.ritaly.dungeonmaster.item.Item;

/**
 * A flask of water. Contains one dose of water.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public class WaterFlask extends Item {

	/**
	 * Creates a new water flask.
	 */
	public WaterFlask() {
		super(Type.WATER_FLASK);
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

	/**
	 * Drinks the water from this flask.
	 */
	public void drink() {
		// Play the associated sound
		SoundSystem.getInstance().play(AudioClip.GLOUPS);

		fireChangeEvent();
	}

	@Override
	protected Item consume(Champion champion) {
		Validate.notNull(champion, "The given champion is null");

		// The champion drinks
		drink();

		// The champion's water stat increases by 150 points
		champion.getStats().getWater().inc(150);

		// The water flask turns into an empty flask
		return new EmptyFlask();
	}
}