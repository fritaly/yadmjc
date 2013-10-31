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

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.ritaly.dungeonmaster.Clock;
import fr.ritaly.dungeonmaster.DeferredCommand;
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.event.ChangeEvent;
import fr.ritaly.dungeonmaster.event.ChangeEventSource;
import fr.ritaly.dungeonmaster.event.ChangeEventSupport;
import fr.ritaly.dungeonmaster.event.ChangeListener;

/**
 * Une {@link Stat} repr�sente une caract�ristique de {@link Champion}. Une
 * {@link Stat} est caract�ris�e par 4 valeurs:
 * <ul>
 * <li>La valeur de base: cf {@link #value}</li>
 * <li>La valeur minimale: cf {@link #min}</li>
 * <li>La valeur maximale: cf {@link #max}</li>
 * <li>La valeur du boost: cf {@link #boost}</li>
 * </ul>
 * La valeur r�elle de la {@link Stat} est la somme de la valeur de base et du
 * boost. Si le boost vaut 0 alors valeur r�elle = valeur de base. Cela
 * s'applique aussi � la valeur max: la valeur r�elle maximale de la
 * {@link Stat} est la somme de la valeur maximale et du boost.<br>
 * <br>
 * La valeur de base est toujours dans l'intervalle [min, max]. Cette contrainte
 * ne s'applique pas au boost ou � la valeur r�elle.
 * 
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public final class Stat implements ChangeEventSource {

	private final ChangeEventSupport eventSupport = new ChangeEventSupport();

	protected final Log log = LogFactory.getLog(this.getClass());

	/**
	 * La valeur de base de la statistique.
	 */
	private Integer value;

	private Integer previous;

	/**
	 * Le nom de la statistique. Permet de g�n�rer des logs parlantes. Exemple:
	 * "Strength". Ne peut �tre null.
	 */
	private final String name;

	/**
	 * La valeur minimale que peut prendre la valeur de base.
	 */
	private Integer min;

	/**
	 * La valeur maximale que peut prendre la valeur de base.
	 */
	private Integer max;

	/**
	 * La valeur de boost.
	 */
	private Integer boost;

	/**
	 * Le propri�taire de la statistique. Permet de g�n�rer des logs parlantes.
	 * Peut �tre null.
	 */
	private final String owner;

	public Stat(String owner, String name) {
		// owner peut �tre null
		if ((name == null) || (name.trim().length() == 0)) {
			throw new IllegalArgumentException("The given name <" + name
					+ "> is blank");
		}

		this.owner = owner;
		this.name = name;
		this.min = create(0.0f);
		this.max = create(Float.MAX_VALUE);
		this.boost = create(0.0f);
		this.value = create(0.0f);
		this.previous = value;
	}

	public Stat(String owner, String name, Integer initialValue) {
		// owner peut �tre null
		if ((name == null) || (name.trim().length() == 0)) {
			throw new IllegalArgumentException("The given name <" + name
					+ "> is blank");
		}
		if (initialValue == null) {
			throw new IllegalArgumentException(
					"The given initial value is null");
		}

		this.owner = owner;
		this.name = name;
		this.min = create(0.0f);
		this.max = create(Float.MAX_VALUE);
		this.boost = create(0.0f);
		this.value = create(getActual(initialValue.floatValue()));
		this.previous = value;
	}

	public Stat(String owner, String name, Integer initialValue, Integer maxValue) {
		// owner peut �tre null
		if ((name == null) || (name.trim().length() == 0)) {
			throw new IllegalArgumentException("The given name <" + name
					+ "> is blank");
		}
		if (initialValue == null) {
			throw new IllegalArgumentException(
					"The given initial value is null");
		}
		if (maxValue == null) {
			throw new IllegalArgumentException("The given max value is null");
		}
		if (maxValue.floatValue() <= 0.0f) {
			throw new IllegalArgumentException("The given max value <"
					+ maxValue + "> must be positive");
		}

		this.owner = owner;
		this.name = name;
		this.min = create(0.0f);
		this.max = maxValue;
		this.boost = create(0.0f);
		this.value = create(getActual(initialValue.floatValue()));
		this.previous = value;
	}

	public Stat(String owner, String name, Integer initialValue, Integer minValue,
			Integer maxValue) {

		// owner peut �tre null
		if ((name == null) || (name.trim().length() == 0)) {
			throw new IllegalArgumentException("The given name <" + name
					+ "> is blank");
		}
		if (initialValue == null) {
			throw new IllegalArgumentException(
					"The given initial value is null");
		}
		if (minValue == null) {
			throw new IllegalArgumentException("The given min value is null");
		}
		if (maxValue == null) {
			throw new IllegalArgumentException("The given max value is null");
		}
		if (maxValue.floatValue() <= minValue.floatValue()) {
			throw new IllegalArgumentException("The given max value <"
					+ maxValue + "> must be greater than the min value <"
					+ minValue + ">");
		}

		this.owner = owner;
		this.name = name;
		this.min = minValue;
		this.max = maxValue;
		this.boost = create(0.0f);
		this.value = create(getActual(initialValue.floatValue()));
		this.previous = value;
	}

	@Override
	public void addChangeListener(ChangeListener listener) {
		eventSupport.addChangeListener(listener);
	}

	@Override
	public void removeChangeListener(ChangeListener listener) {
		eventSupport.removeChangeListener(listener);
	}

	protected void fireChangeEvent() {
		eventSupport.fireChangeEvent(new ChangeEvent(this));
	}

	/**
	 * Retourne la valeur de base (c'est-�-dire non boost�e) de la statistique.
	 * 
	 * @return une instance de Integer.
	 */
	public Integer value() {
		return value;
	}

	/**
	 * Retourne la valeur r�elle (c'est-�-dire avec l'�ventuel boost) de la
	 * statistique.
	 * 
	 * @return une instance de Integer.
	 */
	public Integer actualValue() {
		if (boost.floatValue() != 0.0f) {
			return create(value.floatValue() + boost.floatValue());
		}

		return value();
	}

	private void log(String name, float oldValue, float newValue, float delta) {
		if (log.isDebugEnabled()) {
			final String oldValueText;
			final boolean oldValueMax;
			
			if (oldValue == Integer.MAX_VALUE) {
				oldValueText = "MAX_VALUE";
				oldValueMax = true;
			} else {
				oldValueText = Float.toString(oldValue);
				oldValueMax = false;
			}
			
			final String newValueText;
			final boolean newValueMax;
			
			if (newValue == Integer.MAX_VALUE) {
				newValueText = "MAX_VALUE";
				newValueMax = true;
			} else {
				newValueText = Float.toString(newValue);
				newValueMax = false;
			}
			
			final String deltaText;
			
			if (!oldValueMax && !newValueMax) {
				if (delta > 0) {
					deltaText = "[+" + delta + "]";
				} else {
					deltaText = "[" + delta + "]";
				}				
			} else {
				deltaText = "[N/A]";
			}
			
			if (owner != null) {
				log.debug(owner
						+ "."
						+ name
						+ ": "
						+ oldValueText
						+ " -> "
						+ newValueText
						+ " "
						+ deltaText);
			} else {
				log.debug(name
						+ ": "
						+ oldValueText
						+ " -> "
						+ newValueText
						+ " "
						+ deltaText);
			}
		}
	}

	/**
	 * Retourne la valeur maximale de la valeur de base.
	 * 
	 * @return une instance de Integer.
	 */
	public Integer maxValue() {
		return max;
	}

	/**
	 * Retourne la valeur r�elle maximale (c'est-�-dire avec l'�ventuel boost)
	 * de la statistique.
	 * 
	 * @return une instance de Integer.
	 */
	public Integer actualMaxValue() {
		if (boost.floatValue() != 0.0f) {
			return create(max.floatValue() + boost.floatValue());
		}

		return maxValue();
	}

	/**
	 * Retourne la valeur du boost.
	 * 
	 * @return une instance de Integer.
	 */
	public Integer boostValue() {
		return this.boost;
	}

	/**
	 * Incr�mente la valeur du boost de la quantit� donn�e de mani�re illimit�e
	 * dans le temps et retourne la valeur r�sultante.
	 * 
	 * @param n
	 *            la quantit� dont le boost doit �tre incr�ment�.
	 * @return la nouvelle valeur du boost.
	 */
	public Integer incBoost(final Integer n) {
		return incBoost(n, -1);
	}

	/**
	 * Incr�mente la valeur du boost de la quantit� donn�e et pour le nombre de
	 * tics d'horloge donn�. Enfin retourne la valeur r�sultante.
	 * 
	 * @param n
	 *            la quantit� dont le boost doit �tre incr�ment�.
	 * @param duration
	 *            un entier repr�sentant un nombre de tics d'horloge pendant
	 *            lequel le boost doit �tre incr�ment�. Pass� ce d�lai, le boost
	 *            revient � sa valeur initiale. Une valeur n�gative ou nulle
	 *            rend la modification "�ternelle".
	 * @return la nouvelle valeur du boost.
	 */
	public Integer incBoost(final Integer n, int duration) {
		if (n.floatValue() == 0) {
			// On ne fait rien si param�tre � 0
			return boostValue();
		}

		final Integer oldValue = this.boost;
		final float actual = boost.floatValue() + n.floatValue();
		final float delta = actual - oldValue.floatValue();

		if (delta != 0) {
			boost = create(actual);

			log(name + ".Boost", oldValue.floatValue(), actual, delta);

			if (duration > 0) {
				// Cr�ation d'une DeferredCommand pour modifier le boost apr�s
				// duration tics d'horloge
				Clock.getInstance().register(
						new DeferredCommand(name + ".Boost.DeferredCommand",
								duration) {

							@Override
							protected void run() {
								// FIXME D�croissance en fonction du temps ou
								// d'un seul coup ??
								decBoost(n);
							}

							@Override
							public String toString() {
								return name + ".Boost.DeferredCommand";
							}
						});
			}

			fireChangeEvent();
		}

		return boostValue();
	}

	public Integer inc(Integer n) {
		if (n.floatValue() == 0) {
			// On ne fait rien si param�tre � 0
			return value();
		}

		final Integer oldValue = this.value;
		final float actual = getActual(value.floatValue() + n.floatValue());
		final float delta = actual - oldValue.floatValue();

		if (delta != 0) {
			value = create(actual);
			previous = oldValue;

			log(name, oldValue.floatValue(), actual, delta);

			fireChangeEvent();
		}

		return value();
	}

	public Integer incMax(Integer n) {
		if (n.floatValue() == 0) {
			// On ne fait rien si param�tre � 0
			return maxValue();
		}

		final Integer oldValue = this.max;
		final float actual = this.max.floatValue() + n.floatValue();
		final float delta = actual - oldValue.floatValue();

		if (delta != 0) {
			max = create(actual);
			previous = oldValue;

			log(name + ".Max", oldValue.floatValue(), actual, delta);

			fireChangeEvent();
		}

		return maxValue();
	}

	public Integer decMax(Integer n) {
		if (n.floatValue() == 0) {
			// On ne fait rien si param�tre � 0
			return maxValue();
		}

		final Integer oldValue = this.max;
		final float actual = this.max.floatValue() - n.floatValue();
		final float delta = actual - oldValue.floatValue();

		if (delta != 0) {
			// TODO Diminuer la valeur actuelle si sup�rieure au niveau max
			max = create(actual);
			previous = oldValue;

			log(name + ".Max", oldValue.floatValue(), actual, delta);

			fireChangeEvent();
		}

		return maxValue();
	}

	public Integer dec() {
		return dec(create(1));
	}

	public Integer inc() {
		return inc(create(1));
	}

	public Integer decBoost(final Integer n) {
		return decBoost(n, -1);
	}

	public Integer decBoost(final Integer n, int duration) {
		if (n.floatValue() == 0) {
			// On ne fait rien si param�tre � 0
			return boostValue();
		}

		final Integer oldValue = this.boost;
		final float actual = boost.floatValue() - n.floatValue();
		final float delta = actual - oldValue.floatValue();

		if (delta != 0) {
			boost = create(actual);

			log(name + ".Boost", oldValue.floatValue(), actual, delta);

			if (duration > 0) {
				// Cr�ation d'une DeferredCommand pour modifier le boost apr�s
				// duration tics d'horloge
				Clock.getInstance().register(
						new DeferredCommand(name + ".Boost.DeferredCommand",
								duration) {

							@Override
							protected void run() {
								// FIXME D�croissance en fonction du temps ou
								// d'un seul coup ??
								incBoost(n);
							}

							@Override
							public String toString() {
								return name + ".Boost.DeferredCommand";
							}
						});
			}

			fireChangeEvent();
		}

		return value();
	}

	public Integer dec(Integer n) {
		if (n.floatValue() == 0) {
			// On ne fait rien si param�tre � 0
			return value();
		}

		final Integer oldValue = this.value;
		final float actual = getActual(value.floatValue() - n.floatValue());
		final float delta = actual - oldValue.floatValue();

		if (delta != 0) {
			value = create(actual);
			previous = oldValue;

			log(name, oldValue.floatValue(), actual, delta);

			fireChangeEvent();
		}

		return value();
	}

	private float getActual(final float value) {
		if (value > max.floatValue()) {
			// Attention de ne pas d�passer la valeur max autoris�e
			return max.floatValue();
		} else if (value < min.floatValue()) {
			// Attention de ne pas d�passer la valeur min autoris�e
			return min.floatValue();
		} else {
			// Valeur dans l'intervalle de validit�
			return value;
		}
	}

	public void value(Integer n) {
		final Integer oldValue = value;
		final float actual = getActual(n.floatValue());
		final float delta = actual - oldValue.floatValue();

		if (delta != 0) {
			value = create(actual);
			previous = oldValue;

			log(name, oldValue.floatValue(), actual, delta);

			fireChangeEvent();
		}
	}

	public void maxValue(Integer newMax) {
		if (max.floatValue() <= min.floatValue()) {
			throw new IllegalArgumentException("The given max value <" + newMax
					+ "> must be greater than the min value <"
					+ min.floatValue() + ">");
		}

		final Integer oldMax = max;

		if (newMax.floatValue() < oldMax.floatValue()) {
			// La valeur max a diminu�
			if (value.floatValue() > newMax.floatValue()) {
				// Diminuer la valeur d'abord
				final Integer oldValue = value;
				value = create(newMax.floatValue());
				previous = oldValue;

				if (log.isDebugEnabled()) {
					final float delta = newMax.floatValue()
							- oldValue.floatValue();

					log(name, oldValue.floatValue(), newMax.floatValue(), delta);
				}
			}

			max = newMax;

			if (log.isDebugEnabled()) {
				final float delta = newMax.floatValue() - oldMax.floatValue();

				log(name + ".Max", oldMax.floatValue(), newMax.floatValue(),
						delta);
			}

			fireChangeEvent();
		} else if (newMax.floatValue() > oldMax.floatValue()) {
			// La valeur max a augment�
			max = newMax;

			if (log.isDebugEnabled()) {
				final float delta = newMax.floatValue() - oldMax.floatValue();

				log(name + ".Max", oldMax.floatValue(), newMax.floatValue(),
						delta);
			}

			fireChangeEvent();
		}
	}

	protected Integer create(float value) {
		// Ne pas g�n�rer de d�passement de capacit�
		if (value >= Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		}
		if (value <= Integer.MIN_VALUE) {
			return Integer.MIN_VALUE;
		}

		return Integer.valueOf((int) value);
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder(64);
		builder.append("Stat[name=");
		builder.append(name);
		builder.append(", value=");
		builder.append(value);

		if (min.floatValue() != 0.0f) {
			builder.append(", min=");
			builder.append(min);
		}

		if ((max.floatValue() != Float.MAX_VALUE)
				&& max.floatValue() != Integer.MAX_VALUE) {
			builder.append(", max=");
			builder.append(max);
		}

		if (boost.floatValue() != 0.0f) {
			builder.append(", boost=");
			builder.append(boost);
		}

		builder.append("]");

		return builder.toString();
	}

	public Integer getPrevious() {
		return previous;
	}

	public float getPercent() {
		// N'a de sens que si une valeur max est d�finie
		if (max.floatValue() == Float.MAX_VALUE) {
			return 1.0f;
		}
		if (max.floatValue() == Integer.MAX_VALUE) {
			return 1.0f;
		}

		return value().floatValue() / max.floatValue();
	}

	/**
	 * La stat est faible si elle est inf�rieure � 20% de sa valeur max.
	 * 
	 * @return un boolean.
	 */
	public boolean isLow() {
		return getPercent() <= 0.1f;
	}

	// FIXME M�thode � renommer
	public Integer improve(int min, int max) {
		Validate.isTrue(min >= 0);
		Validate.isTrue(max >= 0);
		Validate.isTrue(min <= max);

		final int increment = min + RandomUtils.nextInt(max);

		// Augmenter la valeur max
		incMax(create(maxValue().intValue() + increment));

		// Augmenter la valeur courante
		return inc(create(value().intValue() + increment));
	}
	
	/**
	 * Indique si la statistique est boost�e (positivement ou n�gativement).
	 * 
	 * @return si la statistique est boost�e.
	 */
	public boolean isBoosted() {
		return boost.floatValue() != 0.0f;
	}
}