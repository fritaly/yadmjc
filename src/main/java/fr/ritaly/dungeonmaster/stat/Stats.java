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
package fr.ritaly.dungeonmaster.stat;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.apache.commons.lang.Validate;

import fr.ritaly.dungeonmaster.Clock;
import fr.ritaly.dungeonmaster.ClockListener;
import fr.ritaly.dungeonmaster.Temporizer;
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.event.ChangeEvent;
import fr.ritaly.dungeonmaster.event.ChangeListener;

/**
 * Defines all the stats of a champion.<br>
 * <br>
 * Source: <a href="http://dmweb.free.fr/?q=node/691">Technical Documentation -
 * Dungeon Master and Chaos Strikes Back Skills and Statistics</a>
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public final class Stats implements ChangeListener, ClockListener {

	public static final String PROPERTY_WATER = "Water";

	public static final String PROPERTY_FOOD = "Food";

	public static final String PROPERTY_HEALTH = "Health";

	public static final String PROPERTY_STRENGTH = "Strength";

	public static final String PROPERTY_STAMINA = "Stamina";

	public static final String PROPERTY_MANA = "Mana";

	public static final String PROPERTY_DEXTERITY = "Dexterity";

	public static final String PROPERTY_WISDOM = "Wisdom";

	public static final String PROPERTY_VITALITY = "Vitality";

	public static final String PROPERTY_ANTI_FIRE = "AntiFire";

	public static final String PROPERTY_ANTI_MAGIC = "AntiMagic";

	public static final String PROPERTY_LUCK = "Luck";

	public static final String PROPERTY_MAX_LOAD_BOOST = "MaxLoadBoost";

	private final Champion champion;

	/**
	 * These two values represent how hungry and thursty a champion is. Food and
	 * Water values are decreased to regenerate Stamina and Health. When these
	 * values reach zero, the champion is starving: his stamina and health
	 * decrease until he eats, drinks or dies.
	 */
	private final Stat food;

	private final Stat water;

	/**
	 * This value represents how much damage a champion can take before dying.
	 * You can regain Health points by sleeping and drinking healing potions.
	 * Health also naturally increases over time, but slowly.
	 */
	private final Stat health;

	/**
	 * This value determines the load a champion can carry, how far items can be
	 * thrown and how much damage is done by melee attacks.
	 */
	private final Stat strength;

	/**
	 * This value represents the champion's ability to overcome fatigue. It
	 * decreases when you walk and fight and also when you are hungry or
	 * thirsty. If this value is equal to zero, any more activity will decrease
	 * health. You can regain Stamina points by sleeping and drinking Stamina
	 * potions. Stamina also naturally increases over time, but slowly.
	 */
	private final Stat stamina;

	/**
	 * This value represents the magical energy a champion has to cast spells.
	 * Each spoken symbol will consume some Mana. You can regain Mana points by
	 * sleeping and drinking Mana potions. Mana also naturally increases over
	 * time, but slowly.
	 *
	 * The speed of the increase of mana while you sleep depends on the Wisdom
	 * and the Priest and Wizard levels of the champion.
	 */
	private final Stat mana;

	/**
	 * This value determines the accuracy of missiles and the odds of hitting
	 * opponents in combat. It also helps the champion to avoid or reduce
	 * physical damage.
	 */
	private final Stat dexterity;

	/**
	 * This value is important for spellcasters as it determines their ability
	 * to master Magick. It also determines the speed of Mana recovery.
	 */
	private final Stat wisdom;

	/**
	 * This value determines how quickly a champion heals and regains stamina as
	 * well as his poison resistance. It also helps to reduce damage.
	 */
	private final Stat vitality;

	/**
	 * This value determines a champion's resistance to magic attacks.
	 */
	private final Stat antiMagic;

	/**
	 * This value is not visible through the game user interface. It is used
	 * during combat and its value is changed each time you use it. The value
	 * increases when you are unlucky and decreases when you are lucky. For
	 * example, if a champion would miss a hit, his luck can help him still
	 * succeed. In this case, the luck value is decreased. This value is
	 * modified by some items: a Rabbit's Foot will increase it by 10, while
	 * cursed items will decrease it by 3.
	 */
	private final Stat luck;

	/**
	 * This value determines a champion's resistance to fire damage.
	 */
	private final Stat antiFire;

	private final Stat maxLoadBoost;

	private boolean initialized;

	private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(
			this);

	private final Temporizer temporizer;

	public Stats(Champion champion) {
		Validate.notNull(champion, "The given champion is null");

		this.champion = champion;

		food = new Stat(champion.getName(), PROPERTY_FOOD, 1500, 1500);
		water = new Stat(champion.getName(), PROPERTY_WATER, 1500, 1500);
		health = new Stat(champion.getName(), PROPERTY_HEALTH);
		strength = new Stat(champion.getName(), PROPERTY_STRENGTH);
		stamina = new Stat(champion.getName(), PROPERTY_STAMINA);
		mana = new Stat(champion.getName(), PROPERTY_MANA);
		dexterity = new Stat(champion.getName(), PROPERTY_DEXTERITY);
		wisdom = new Stat(champion.getName(), PROPERTY_WISDOM);
		vitality = new Stat(champion.getName(), PROPERTY_VITALITY);
		antiFire = new Stat(champion.getName(), PROPERTY_ANTI_FIRE);
		antiMagic = new Stat(champion.getName(), PROPERTY_ANTI_MAGIC);
		luck = new Stat(champion.getName(), PROPERTY_LUCK);
		maxLoadBoost = new Stat(champion.getName(), PROPERTY_MAX_LOAD_BOOST);

		// Listen to the events fire by those stats
		food.addChangeListener(this);
		water.addChangeListener(this);
		health.addChangeListener(this);
		strength.addChangeListener(this);
		stamina.addChangeListener(this);
		mana.addChangeListener(this);
		dexterity.addChangeListener(this);
		wisdom.addChangeListener(this);
		vitality.addChangeListener(this);
		antiFire.addChangeListener(this);
		antiMagic.addChangeListener(this);
		luck.addChangeListener(this);
		maxLoadBoost.addChangeListener(this);

		// Stats are updated every 5 seconds
		temporizer = new Temporizer(champion.getName() + ".Stats", 5 * Clock.ONE_SECOND);
	}

	private void assertInitialized() {
		if (!initialized) {
			throw new IllegalStateException("The stats haven't been initialized");
		}
	}

	public Champion getChampion() {
		return champion;
	}

	public Stat getFood() {
		assertInitialized();

		return food;
	}

	public Stat getWater() {
		assertInitialized();

		return water;
	}

	@Override
	public void onChangeEvent(ChangeEvent event) {
		// Propagate the event as a property change event
		firePropertyChangeEvent(event.getSource());
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		changeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		changeSupport.removePropertyChangeListener(listener);
	}

	protected void firePropertyChangeEvent(Object source) {
		final Stat stat = (Stat) source;

		changeSupport.firePropertyChange(stat.getName(), stat.getPrevious(), stat.value());
	}

	public Stat getHealth() {
		assertInitialized();

		return health;
	}

	public Stat getStrength() {
		assertInitialized();

		return strength;
	}

	public Stat getStamina() {
		assertInitialized();

		return stamina;
	}

	public Stat getMana() {
		assertInitialized();

		return mana;
	}

	public void init(int health, int stamina, int mana, int luck, int strength, int dexterity, int wisdom, int vitality,
			int antiFire, int antiMagic) {

		if (initialized) {
			throw new IllegalStateException("The stats have already been initialized");
		}

		// Directly access the properties not to execute the assertions inside
		// the getters
		this.health.maxValue(health);
		this.health.value(health);

		this.stamina.maxValue(stamina);
		this.stamina.value(stamina);

		this.mana.maxValue(mana);
		this.mana.value(mana);

		this.strength.maxValue(strength);
		this.strength.value(strength);

		this.dexterity.maxValue(dexterity);
		this.dexterity.value(dexterity);

		this.wisdom.maxValue(wisdom);
		this.wisdom.value(wisdom);

		this.vitality.value(vitality);
		this.vitality.maxValue(vitality);

		this.antiFire.value(antiFire);
		this.antiFire.maxValue(antiFire);

		this.antiMagic.value(antiMagic);
		this.antiMagic.maxValue(antiMagic);

		// The luck stat isn't bounded by a max value because we need to be able
		// to increase the luck when the champion wears a rabbit foot
		this.luck.value(luck);
		this.luck.maxValue(100);

		initialized = true;
	}

	public Stat getDexterity() {
		assertInitialized();

		return dexterity;
	}

	public Stat getWisdom() {
		assertInitialized();

		return wisdom;
	}

	public Stat getVitality() {
		assertInitialized();

		return vitality;
	}

	@Override
	public boolean clockTicked() {
		assertInitialized();

		if (temporizer.trigger()) {
			// TODO When the party is sleeping, make time elapse faster (speed factor ?)

			// TODO Update the stats
			// dexterity.inc(3);
			// strength.inc(3);
			// vitality.inc(3);
			// wisdom.inc(3);

			// TODO Mana regenerates over time
			mana.inc(3);

			if (stamina.isLow() || food.isLow() || water.isLow()) {
				// If stamina, food or water is low, the health gets hit
				health.dec(5);
			} else {
				// The health regenerates over time
				health.inc(3);
			}

			if (health.actualValue() == 0) {
				// The champion just died, stop listening to clock ticks
				return false;
			}

			// TODO The stamina decreases over time (and the load too)
			stamina.dec(3);

			// Food and water decrease linearly over time
			food.dec(5);
			water.dec(5);
		}

		// Keep on listening to clock ticks until the champion dies
		return true;
	}

	public Stat getAntiMagic() {
		assertInitialized();

		return antiMagic;
	}

	public Stat getAntiFire() {
		assertInitialized();

		return antiFire;
	}

	public Stat getLuck() {
		assertInitialized();

		return luck;
	}

	public Stat getMaxLoadBoost() {
		assertInitialized();

		return maxLoadBoost;
	}

	public final float getActualMaxLoad() {
		final float baseMaxLoad = (8.0f * strength.actualValue() + 100.0f) / 10.0f;

		// The max load can be temporarily boosted
		final float actualBaseMaxLoad = baseMaxLoad + maxLoadBoost.value();

		final Integer curStamina = stamina.actualValue();
		final Integer maxStamina = stamina.actualMaxValue();

		if (curStamina >= (maxStamina / 2.0f)) {
			// Stamina is good, no penalty to the max load
			return actualBaseMaxLoad;
		} else {
			// Stamina is low, the champion's max load has a malus
			return (actualBaseMaxLoad / 2) + ((actualBaseMaxLoad * curStamina) / (maxStamina / 2.0f));
		}
	}
}