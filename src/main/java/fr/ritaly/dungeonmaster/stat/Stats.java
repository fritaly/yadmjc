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

import fr.ritaly.dungeonmaster.Clock;
import fr.ritaly.dungeonmaster.ClockListener;
import fr.ritaly.dungeonmaster.Temporizer;
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.event.ChangeEvent;
import fr.ritaly.dungeonmaster.event.ChangeListener;

/**
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public class Stats implements ChangeListener, ClockListener {

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

	private final Champion champion;

	/**
	 * These two values represent how hungry and thursty a champion is. Food and
	 * Water values are decreased to regenerate Stamina and Health. When these
	 * values reach zero, the champion is starving: his stamina and health
	 * decrease until he eats, drinks or dies.
	 */
	private final IntStat food;

	private final IntStat water;

	/**
	 * This value represents how much damage a champion can take before dying.
	 * You can regain Health points by sleeping and drinking healing potions.
	 * Health also naturally increases over time, but slowly.
	 */
	private final IntStat health;

	/**
	 * This value determines the load a champion can carry, how far items can be
	 * thrown and how much damage is done by melee attacks.
	 */
	private final IntStat strength;

	/**
	 * This value represents the champion's ability to overcome fatigue. It
	 * decreases when you walk and fight and also when you are hungry or
	 * thirsty. If this value is equal to zero, any more activity will decrease
	 * health. You can regain Stamina points by sleeping and drinking Stamina
	 * potions. Stamina also naturally increases over time, but slowly.
	 */
	private final IntStat stamina;

	/**
	 * This value represents the magical energy a champion has to cast spells.
	 * Each spoken symbol will consume some Mana. You can regain Mana points by
	 * sleeping and drinking Mana potions. Mana also naturally increases over
	 * time, but slowly.
	 * 
	 * The speed of the increase of mana while you sleep depends on the Wisdom
	 * and the Priest and Wizard levels of the champion.
	 */
	private final IntStat mana;

	/**
	 * This value determines the accuracy of missiles and the odds of hitting
	 * opponents in combat. It also helps the champion to avoid or reduce
	 * physical damage.
	 */
	private final IntStat dexterity;

	/**
	 * This value is important for spellcasters as it determines their ability
	 * to master Magick. It also determines the speed of Mana recovery.
	 */
	private final IntStat wisdom;

	/**
	 * This value determines how quickly a champion heals and regains stamina as
	 * well as his poison resistance. It also helps to reduce damage.
	 */
	private final IntStat vitality;

	/**
	 * This value determines a champion's resistance to magic attacks.
	 */
	private final IntStat antiMagic;

	/**
	 * This value is not visible through the game user interface. It is used
	 * during combat and its value is changed each time you use it. The value
	 * increases when you are unlucky and decreases when you are lucky. For
	 * example, if a champion would miss a hit, his luck can help him still
	 * succeed. In this case, the luck value is decreased. This value is
	 * modified by some items: a Rabbit's Foot will increase it by 10, while
	 * cursed items will decrease it by 3.
	 */
	private final IntStat luck;

	/**
	 * This value determines a champion's resistance to fire damage.
	 */
	private final IntStat antiFire;

	private boolean initialized;

	private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(
			this);

	private final Temporizer temporizer;

	public Stats(Champion champion) {
		if (champion == null) {
			throw new IllegalArgumentException("The given champion is null");
		}

		this.champion = champion;

		// Instancier ces membres depuis le constructeur permet de passer aux
		// instances de IntStat le nom de son "propriétaire"
		food = new IntStat(champion.getName(), PROPERTY_FOOD, 1500, 1500);
		water = new IntStat(champion.getName(), PROPERTY_WATER, 1500, 1500);
		health = new IntStat(champion.getName(), PROPERTY_HEALTH);
		strength = new IntStat(champion.getName(), PROPERTY_STRENGTH);
		stamina = new IntStat(champion.getName(), PROPERTY_STAMINA);
		mana = new IntStat(champion.getName(), PROPERTY_MANA);
		dexterity = new IntStat(champion.getName(), PROPERTY_DEXTERITY);
		wisdom = new IntStat(champion.getName(), PROPERTY_WISDOM);
		vitality = new IntStat(champion.getName(), PROPERTY_VITALITY);
		antiFire = new IntStat(champion.getName(), PROPERTY_ANTI_FIRE);
		antiMagic = new IntStat(champion.getName(), PROPERTY_ANTI_MAGIC);
		luck = new IntStat(champion.getName(), PROPERTY_LUCK);

		// Ecouter les évènements levés par les stats
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

		// 5 secondes
		temporizer = new Temporizer(champion.getName() + ".Stats",
				5 * Clock.ONE_SECOND);
	}

	private void assertInitialized() {
		if (!initialized) {
			throw new IllegalStateException(
					"The stats haven't been initialized");
		}
	}

	public Champion getChampion() {
		return champion;
	}

	public IntStat getFood() {
		assertInitialized();

		return food;
	}

	public IntStat getWater() {
		assertInitialized();

		return water;
	}

	@Override
	public void onChangeEvent(ChangeEvent event) {
		// Convertir l'évènement de ChangeEvent en PropertyChangeEvent
		firePropertyChangeEvent(event.getSource());
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		changeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		changeSupport.removePropertyChangeListener(listener);
	}

	protected void firePropertyChangeEvent(Object source) {
		final Stat<Number> stat = (Stat<Number>) source;

		changeSupport.firePropertyChange(stat.getName(), stat.getPrevious(),
				stat.value());
	}

	public IntStat getHealth() {
		assertInitialized();

		return health;
	}

	public IntStat getStrength() {
		assertInitialized();

		return strength;
	}

	public IntStat getStamina() {
		assertInitialized();

		return stamina;
	}

	public IntStat getMana() {
		assertInitialized();

		return mana;
	}

	public void init(int health, int stamina, int mana, int luck, int strength,
			int dexterity, int wisdom, int vitality, int antiFire, int antiMagic) {

		if (initialized) {
			throw new IllegalStateException(
					"The stats have already been initialized");
		}

		// On accède directement aux membres ce qui permet de ne pas passer par
		// les assertions sur le flag initialized
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

		// Il n'y a pas de bornes à la statistique "Luck" afin de pouvoir
		// l'augmenter quand le champion porte une patte de lapin
		this.luck.value(luck);
		this.luck.maxValue(100);

		initialized = true;
	}

	public IntStat getDexterity() {
		assertInitialized();

		return dexterity;
	}

	public IntStat getWisdom() {
		assertInitialized();

		return wisdom;
	}

	public IntStat getVitality() {
		assertInitialized();

		return vitality;
	}

	@Override
	public boolean clockTicked() {
		assertInitialized();

		if (temporizer.trigger()) {
			// TODO Créer facteur quand mode sleeping

			// TODO Mise à jour des stats ?
			// dexterity.inc(3);
			// strength.inc(3);
			// vitality.inc(3);
			// wisdom.inc(3);

			// TODO La mana se restaure avec le temps
			mana.inc(3);

			if (stamina.isLow() || food.isLow() || water.isLow()) {
				// La santé décroît si la stamina est faible, le champion a faim
				// ou soif
				health.dec(5);
			} else {
				// La santé se régénère
				health.inc(3);
			}

			if (health.actualValue() == 0) {
				// Si le héros vient de mourir, on retourne de suite
				return false;
			}

			// TODO La stamina décroît avec le temps (et la charge portée !)
			stamina.dec(3);

			// Décroissance constante avec le temps
			food.dec(5);
			water.dec(5);
		}

		// On n'arrête jamais l'animation des stats
		return true;
	}

	public IntStat getAntiMagic() {
		assertInitialized();

		return antiMagic;
	}

	public IntStat getAntiFire() {
		assertInitialized();

		return antiFire;
	}

	public IntStat getLuck() {
		assertInitialized();

		return luck;
	}

	public final float getMaxLoad() {
		final float baseMaxLoad = (8.0f * strength.actualValue() + 100.0f) / 10.0f;

		final Integer curStamina = stamina.actualValue();
		final Integer maxStamina = stamina.actualMaxValue();

		if (curStamina >= (maxStamina / 2.0f)) {
			return baseMaxLoad;
		} else {
			// Champion à la peine
			return (baseMaxLoad / 2)
					+ ((baseMaxLoad * curStamina) / (maxStamina / 2.0f));
		}
	}
}