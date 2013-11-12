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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.ritaly.dungeonmaster.Clock;
import fr.ritaly.dungeonmaster.DeferredCommand;
import fr.ritaly.dungeonmaster.event.ChangeEvent;
import fr.ritaly.dungeonmaster.event.ChangeEventSource;
import fr.ritaly.dungeonmaster.event.ChangeEventSupport;
import fr.ritaly.dungeonmaster.event.ChangeListener;

/**
 * A {@link Stat} represents a champion's feature. A stat has 4 features:
 * <ul>
 * <li>a base value (see {@link #baseValue()})</li>
 * <li>a minimal value (see {@link #min})</li>
 * <li>a maximal value (see {@link #max})</li>
 * <li>a boost value (see {@link #boost})</li>
 * </ul>
 *
 * The min and max value define a range within which the base value is bounded.<br>
 * <br>
 * The boost applies to the base value and also the max value. So when there's a
 * positive boost, the stat's actual value is the base value plus the boost.
 * Since the max value is also boosted, this allows the stat to have a value
 * higher than the regular max value. If the boost is zero, then the actual
 * value is simply the base value.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public final class Stat implements ChangeEventSource {

	private final ChangeEventSupport eventSupport = new ChangeEventSupport();

	protected final Log log = LogFactory.getLog(this.getClass());

	/**
	 * The stat's base value. Always within [min,max].
	 */
	private int value;

	/**
	 * The stat's previous base value.
	 */
	private int previous;

	/**
	 * The name of this stat. Meant for debugging. Example: "Strength". Can't be
	 * null.
	 */
	private final String name;

	/**
	 * The minimal value allowed for the base value.
	 */
	private int min;

	/**
	 * The maximal value allowed for the base value.
	 */
	private int max;

	/**
	 * The possible boost value. Can be any value (positive, negative).
	 */
	private int boost;

	/**
	 * The name of the stat's owner. Meant for debugging. Can't be null.
	 */
	private final String owner;

	public Stat(String owner, String name) {
		Validate.isTrue(!StringUtils.isBlank(name), String.format("The given name '%s' is blank", name));

		this.owner = owner;
		this.name = name;
		this.min = 0;
		this.max = Integer.MAX_VALUE;
		this.boost = 0;
		this.value = 0;
		this.previous = value;
	}

	public Stat(String owner, String name, int initialValue) {
		Validate.isTrue(!StringUtils.isBlank(name), String.format("The given name '%s' is blank", name));

		this.owner = owner;
		this.name = name;
		this.min = 0;
		this.max = Integer.MAX_VALUE;
		this.boost = 0;
		this.value = bindBaseValue(initialValue);
		this.previous = value;
	}

	public Stat(String owner, String name, int initialValue, int maxValue) {
		Validate.isTrue(!StringUtils.isBlank(name), String.format("The given name '%s' is blank", name));
		Validate.isTrue(maxValue > 0, String.format("The given max value %d must be positive", maxValue));

		this.owner = owner;
		this.name = name;
		this.min = 0;
		this.max = maxValue;
		this.boost = 0;
		this.value = bindBaseValue(initialValue);
		this.previous = value;
	}

	public Stat(String owner, String name, int initialValue, int minValue, int maxValue) {
		Validate.isTrue(!StringUtils.isBlank(name), String.format("The given name '%s' is blank", name));
		Validate.isTrue(minValue <= maxValue,
				String.format("The given min value %d must be lesser than the given max value %d", minValue, maxValue));

		this.owner = owner;
		this.name = name;
		this.min = minValue;
		this.max = maxValue;
		this.boost = 0;
		this.value = bindBaseValue(initialValue);
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

	private void fireChangeEvent() {
		eventSupport.fireChangeEvent(new ChangeEvent(this));
	}

	/**
	 * Returns the stat's base value (that is, without the possible boost).
	 *
	 * @return the stat's base value as an integer.
	 */
	public int baseValue() {
		return value;
	}

	/**
	 * Returns the stat's actual value (that is, with the possible boost).
	 *
	 * @return the actual stat's value as an integer.
	 */
	public int value() {
		return (boost != 0) ? value + boost : value;
	}

	private void log(String name, int oldValue, int newValue, int delta) {
		if (log.isDebugEnabled()) {
			final String oldValueText;
			final boolean oldValueMax;

			if (oldValue == Integer.MAX_VALUE) {
				oldValueText = "MAX_VALUE";
				oldValueMax = true;
			} else {
				oldValueText = Integer.toString(oldValue);
				oldValueMax = false;
			}

			final String newValueText;
			final boolean newValueMax;

			if (newValue == Integer.MAX_VALUE) {
				newValueText = "MAX_VALUE";
				newValueMax = true;
			} else {
				newValueText = Integer.toString(newValue);
				newValueMax = false;
			}

			final String deltaText;

			if (!oldValueMax && !newValueMax) {
				if (delta > 0) {
					deltaText = "+" + delta;
				} else {
					deltaText = Integer.toString(delta);
				}
			} else {
				deltaText = "N/A";
			}

			if (owner != null) {
				log.debug(String.format("%s.%s: %s -> %s [%s]", owner, name, oldValueText, newValueText, deltaText));
			} else {
				log.debug(String.format("%s: %s -> %s [%s]", name, oldValueText, newValueText, deltaText));
			}
		}
	}

	/**
	 * Returns the stat's base max value (without the possible boost).
	 *
	 * @return the stat's base max value as an integer.
	 */
	public int baseMaxValue() {
		return max;
	}

	/**
	 * Returns the stat's actual max value (with the possible boost).
	 *
	 * @return the stat's actual max value as an integer.
	 */
	public int maxValue() {
		return (boost != 0) ? max + boost : max;
	}

	/**
	 * Returns the stat's boost value.
	 *
	 * @return the stat's boost value as an integer.
	 */
	public int boostValue() {
		return this.boost;
	}

	/**
	 * Increments the boost value by the given amount for an unlimited duration
	 * and returns the updated value.
	 *
	 * @param n
	 *            the value to add to the boost value.
	 * @return the updated boost value.
	 */
	public int incBoost(final int n) {
		return incBoost(n, -1);
	}

	/**
	 * Increments the boost value by the given amount and for the given duration
	 * and returns the updated value.
	 *
	 * @param n
	 *            the value to add to the boost value.
	 * @param duration
	 *            a duration in number of clock ticks. Passed this duration, the
	 *            added value will be removed from the boost. Any negative or
	 *            zero duration is considered a boost for ever. The duration is
	 *            convenient for providing a limited boost to the stat.
	 * @return the updated boost value.
	 */
	public int incBoost(final int n, int duration) {
		if (n == 0) {
			// No change to the boost
			return boostValue();
		}

		final int oldValue = this.boost;
		final int newValue = this.boost + n;

		boost = newValue;

		log(name + ".Boost", oldValue, newValue, n);

		if (duration > 0) {
			// Create a DeferredCommand to reset the boost after the given
			// duration
			Clock.getInstance().register(new DeferredCommand(name + ".Boost.DeferredCommand", duration) {

				@Override
				protected void run() {
					decBoost(n);
				}

				@Override
				public String toString() {
					return name + ".Boost.DeferredCommand";
				}
			});
		}

		fireChangeEvent();

		return boostValue();
	}

	/**
	 * Increments the base value by the given amount and returns the updated
	 * base value.
	 *
	 * @param n
	 *            the value to add to the base value.
	 * @return the updated base value.
	 */
	public int inc(int n) {
		if (n == 0) {
			// No change to the boost
			return baseValue();
		}

		return baseValue(this.value + n);
	}

	/**
	 * Increments the max value by the given amount and returns the updated
	 * max value.
	 *
	 * @param n
	 *            the value to add to the max value.
	 * @return the updated max value.
	 */
	public int incMax(int n) {
		if (n == 0) {
			// No change to the max value
			return baseMaxValue();
		}

		final int oldValue = this.max;
		final int newValue = this.max + n;

		max = newValue;
		previous = oldValue;

		log(name + ".Max", oldValue, newValue, n);

		fireChangeEvent();

		return baseMaxValue();
	}

	/**
	 * Decreases the max value by the given amount and returns the updated max
	 * value.
	 *
	 * @param n
	 *            the value to remove from the max value.
	 * @return the updated max value.
	 */
	public int decMax(int n) {
		if (n == 0) {
			// No change to the max value
			return baseMaxValue();
		}

		final int oldValue = this.max;
		final int newValue = this.max - n;

		// TODO Decrease the actual value if above the new max value
		max = newValue;
		previous = oldValue;

		log(name + ".Max", oldValue, newValue, n);

		fireChangeEvent();

		return baseMaxValue();
	}

	/**
	 * Decreases the boost value by the given amount and returns the updated
	 * boost value.
	 *
	 * @param n
	 *            the value to remove from the boost value.
	 * @return the updated boost value.
	 */
	public int decBoost(final int n) {
		return decBoost(n, -1);
	}

	public int decBoost(final int n, int duration) {
		if (n == 0) {
			// No change to the boost value
			return boostValue();
		}

		final int oldValue = this.boost;
		final int newValue = this.boost - n;

		boost = newValue;

		log(name + ".Boost", oldValue, newValue, -n);

		if (duration > 0) {
			// Create a DeferredCommand to reset the boost after the given
			// duration
			Clock.getInstance().register(new DeferredCommand(name + ".Boost.DeferredCommand", duration) {
				@Override
				protected void run() {
					incBoost(n);
				}

				@Override
				public String toString() {
					return name + ".Boost.DeferredCommand";
				}
			});
		}

		fireChangeEvent();

		return baseValue();
	}

	public int dec(int n) {
		if (n == 0) {
			// No change to the value
			return baseValue();
		}

		return baseValue(this.value - n);
	}

	private int bindBaseValue(final int value) {
		if (value > max) {
			// Careful to always return a value within [min, max]
			return max;
		}
		if (value < min) {
			// Careful to always return a value within [min, max]
			return min;
		}

		return value;
	}

	public int baseValue(int n) {
		final int oldValue = value;
		final int newValue = bindBaseValue(n);
		final int delta = newValue - oldValue;

		if (delta != 0) {
			value = newValue;
			previous = oldValue;

			log(name, oldValue, newValue, delta);

			fireChangeEvent();
		}

		return value;
	}

	public void baseMaxValue(int newMax) {
		Validate.isTrue(max > min,
				String.format("The given max value %d must be greater than the min value %d", newMax, min));

		final int oldMax = max;

		if (newMax < oldMax) {
			// The max value decreased
			if (value > newMax) {
				// Adjust the value first (necessary)
				final int oldValue = value;
				value = newMax;
				previous = oldValue;

				if (log.isDebugEnabled()) {
					log(name, oldValue, newMax, newMax - oldValue);
				}
			}

			max = newMax;

			if (log.isDebugEnabled()) {
				log(name + ".Max", oldMax, newMax, newMax - oldMax);
			}

			fireChangeEvent();
		} else if (newMax > oldMax) {
			// The max value increased
			max = newMax;

			if (log.isDebugEnabled()) {
				log(name + ".Max", oldMax, newMax, newMax - oldMax);
			}

			fireChangeEvent();
		}
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

		if (min != 0) {
			builder.append(", min=");
			builder.append(min);
		}

		if (max != Integer.MAX_VALUE) {
			builder.append(", max=");
			builder.append(max);
		}

		if (boost != 0) {
			builder.append(", boost=");
			builder.append(boost);
		}

		builder.append("]");

		return builder.toString();
	}

	// Visibility package protected on purpose
	int getPrevious() {
		return previous;
	}

	private float getPercent() {
		// Only relevant if a max value is defined
		if (max == Integer.MAX_VALUE) {
			return 1.0f;
		}

		return (float) baseValue() / max;
	}

	/**
	 * Tells whether the stat's value is low (that is lower than 20% of its max value).
	 *
	 * @return whether the stat's value is low.
	 */
	public boolean isLow() {
		return getPercent() <= 0.1f;
	}

	/**
	 * Tells whether the stat's actual value is boosted (positively or negatively !).
	 *
	 * @return whether the stat's actual value is boosted.
	 */
	public boolean isBoosted() {
		return (boost != 0);
	}
}