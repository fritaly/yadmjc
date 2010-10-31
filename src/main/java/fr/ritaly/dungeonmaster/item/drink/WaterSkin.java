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

import fr.ritaly.dungeonmaster.audio.AudioClip;
import fr.ritaly.dungeonmaster.audio.SoundSystem;
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.body.BodyPart;
import fr.ritaly.dungeonmaster.item.Item;

/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public class WaterSkin extends Item {

	private static final int MAX_COUNT = 3;

	private int count;

	public WaterSkin() {
		super(Type.WATER_SKIN);
	}

	@Override
	public int getShield() {
		return 0;
	}

	@Override
	public int getFireShield() {
		return 0;
	}

	@Override
	public BodyPart.Type getActivationBodyPart() {
		return null;
	}

	@Override
	public float getWeight() {
		return 0.3f + (count * 0.3f);
	}

	public boolean isEmpty() {
		return (count == 0);
	}

	public boolean isFull() {
		return (count == MAX_COUNT);
	}

	public void drink() {
		if (isEmpty()) {
			throw new IllegalStateException("The water skin is empty");
		}

		// Jouer le son "gloups"
		SoundSystem.getInstance().play(AudioClip.GLOUPS);
		
		count--;

		fireChangeEvent();
	}

	public void fill() {
		// On peut reremplir une outre déjà pleine

		// Jouer le son dédié
		SoundSystem.getInstance().play(AudioClip.REFILL);
		
		count = MAX_COUNT;

		fireChangeEvent();
	}

	@Override
	protected Item consume(Champion champion) {
		if (!isEmpty()) {
			// Le héros boit une dose de l'outre
			drink();
			
			champion.getStats().getWater().inc(150);
		}
		
		// L'objet peut être recyclé
		return this;
	}
}