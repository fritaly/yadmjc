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
package fr.ritaly.dungeonmaster.item;

import org.apache.commons.lang.Validate;

import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.body.BodyPart;

/**
 * Some food. Food is useful for fighting hunger.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
final class Food extends Item {

	/**
	 * Creates a new food item with the given type.
	 *
	 * @param type
	 *            the type of food to create. Can't be null.
	 */
	Food(Type type) {
		super(type);

		Validate.isTrue(Item.Category.getFoodItems().contains(type));
	}

	@Override
	public final int getShield() {
		return 0;
	}

	@Override
	public final int getAntiMagic() {
		return 0;
	}

	@Override
	public final BodyPart.Type getActivationBodyPart() {
		return null;
	}

	/**
	 * Returns the "nutritive value" of this food as an integer. The higher the
	 * value, the more nutritive.
	 *
	 * @return a positive integer representing the "nutritive value" of this
	 *         food.
	 */
	private int getFoodValue() {
		switch (getType()) {
		case APPLE:
			return 500;
		case CORN:
			return 600;
		case BREAD:
			return 650;
		case CHEESE:
			return 820;
		case SCREAMER_SLICE:
			return 550;
		case WORM_ROUND:
			return 350;
		case DRUMSTICK:
			return 990;
		case DRAGON_STEAK:
			return 1400;
		default:
			throw new UnsupportedOperationException("Unsupported food item " + getType());
		}
	}

	@Override
	protected final Item consume(Champion champion) {
		champion.getStats().getFood().inc(getFoodValue());

		if (isPoisonous()) {
			// The food is poisoned
			champion.poison(getPoisonStrength());
		}

		// The item is consumed after use (return nothing)
		return null;
	}
}
