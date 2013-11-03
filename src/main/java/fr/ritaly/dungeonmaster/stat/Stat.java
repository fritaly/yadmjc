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
import org.apache.commons.lang.math.RandomUtils;
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
 * <li>a base value (see {@link #value()})</li>
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
	private Integer value;

	/**
	 * The stat's previous base value.
	 */
	private Integer previous;

	/**
	 * The name of this stat. Meant for debugging. Example: "Strength". Can't be
	 * null.
	 */
	private final String name;

	/**
	 * The minimal value allowed for the base value.
	 */
	private Integer min;

	/**
	 * The maximal value allowed for the base value.
	 */
	private Integer max;

	/**
	 * The possible boost value. Can be any value (positive, negative).
	 */
	private Integer boost;

	/**
	 * The name of the stat's owner. Meant for debugging. Can't be null.
	 */
	private final String owner;

	public Stat(String owner, String name) {
		Validate.isTrue(!StringUtils.isBlank(name), String.format("The given name '%s' is blank", name));

		this.owner = owner;
		this.name = name;
		this.min = create(0.0f);
		this.max = create(Float.MAX_VALUE);
		this.boost = create(0.0f);
		this.value = create(0.0f);
		this.previous = value;
	}

	public Stat(String owner, String name, Integer initialValue) {
		Validate.isTrue(!StringUtils.isBlank(name), String.format("The given name '%s' is blank", name));
		Validate.notNull(initialValue, "The given initial value is null");

		this.owner = owner;
		this.name = name;
		this.min = create(0.0f);
		this.max = create(Float.MAX_VALUE);
		this.boost = create(0.0f);
		this.value = create(getActual(initialValue.floatValue()));
		this.previous = value;
	}

	public Stat(String owner, String name, Integer initialValue, Integer maxValue) {
		Validate.isTrue(!StringUtils.isBlank(name), String.format("The given name '%s' is blank", name));
		Validate.notNull(initialValue, "The given initial value is null");
		Validate.notNull(maxValue, "The given max value is null");
		Validate.isTrue(maxValue.floatValue() > 0.0f, String.format("The given max value %d must be positive", maxValue));

		this.owner = owner;
		this.name = name;
		this.min = create(0.0f);
		this.max = maxValue;
		this.boost = create(0.0f);
		this.value = create(getActual(initialValue.floatValue()));
		this.previous = value;
	}

	public Stat(String owner, String name, Integer initialValue, Integer minValue, Integer maxValue) {
		Validate.isTrue(!StringUtils.isBlank(name), String.format("The given name '%s' is blank", name));
		Validate.notNull(initialValue, "The given initial value is null");
		Validate.notNull(minValue, "The given min value is null");
		Validate.notNull(maxValue, "The given max value is null");
		Validate.isTrue(minValue.floatValue() <= maxValue.floatValue(),
				String.format("The given min value %d must be lesser than the given max value %d", minValue, maxValue));

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
	 * Returns the stat's base value (that is, without the possible boost).
	 *
	 * @return the stat's base value as an integer.
	 */
	public Integer value() {
		return value;
	}

	/**
	 * Returns the stat's actual value (that is, with the possible boost).
	 *
	 * @return the actual stat's value as an integer.
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
					deltaText = "+" + delta;
				} else {
					deltaText = Float.toString(delta);
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
	public Integer maxValue() {
		return max;
	}

	/**
	 * Returns the stat's actual max value (with the possible boost).
	 *
	 * @return the stat's actual max value as an integer.
	 */
	public Integer actualMaxValue() {
		if (boost.floatValue() != 0.0f) {
			return create(max.floatValue() + boost.floatValue());
		}

		return maxValue();
	}

	/**
	 * Returns the stat's boost value.
	 *
	 * @return the stat's boost value as an integer.
	 */
	public Integer boostValue() {
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
	public Integer incBoost(final Integer n) {
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
	public Integer incBoost(final Integer n, int duration) {
		if (n.floatValue() == 0) {
			// No change to the boost
			return boostValue();
		}

		final Integer oldValue = this.boost;
		final float actual = boost.floatValue() + n.floatValue();
		final float delta = actual - oldValue.floatValue();

		if (delta != 0) {
			boost = create(actual);

			log(name + ".Boost", oldValue.floatValue(), actual, delta);

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
		}

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
	public Integer inc(Integer n) {
		if (n.floatValue() == 0) {
			// No change to the boost
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

	/**
	 * Increments the max value by the given amount and returns the updated
	 * max value.
	 *
	 * @param n
	 *            the value to add to the max value.
	 * @return the updated max value.
	 */
	public Integer incMax(Integer n) {
		if (n.floatValue() == 0) {
			// No change to the max value
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

	/**
	 * Decreases the max value by the given amount and returns the updated max
	 * value.
	 *
	 * @param n
	 *            the value to remove from the max value.
	 * @return the updated max value.
	 */
	public Integer decMax(Integer n) {
		if (n.floatValue() == 0) {
			// No change to the max value
			return maxValue();
		}

		final Integer oldValue = this.max;
		final float actual = this.max.floatValue() - n.floatValue();
		final float delta = actual - oldValue.floatValue();

		if (delta != 0) {
			// TODO Decrease the actual value if above the new max value
			max = create(actual);
			previous = oldValue;

			log(name + ".Max", oldValue.floatValue(), actual, delta);

			fireChangeEvent();
		}

		return maxValue();
	}

	/**
	 * Decreases the base value by one and returns the updated base value.
	 *
	 * @return the updated base value.
	 */
	public Integer dec() {
		return dec(create(1));
	}

	/**
	 * Increases the base value by one and returns the updated base value.
	 *
	 * @return the updated base value.
	 */
	public Integer inc() {
		return inc(create(1));
	}

	/**
	 * Decreases the boost value by the given amount and returns the updated
	 * boost value.
	 *
	 * @param n
	 *            the value to remove from the boost value.
	 * @return the updated boost value.
	 */
	public Integer decBoost(final Integer n) {
		return decBoost(n, -1);
	}

	public Integer decBoost(final Integer n, int duration) {
		if (n.floatValue() == 0) {
			// No change to the boost value
			return boostValue();
		}

		final Integer oldValue = this.boost;
		final float actual = boost.floatValue() - n.floatValue();
		final float delta = actual - oldValue.floatValue();

		if (delta != 0) {
			boost = create(actual);

			log(name + ".Boost", oldValue.floatValue(), actual, delta);

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
		}

		return value();
	}

	public Integer dec(Integer n) {
		if (n.floatValue() == 0) {
			// No change to the value
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
			// Careful to always return a value within [min, max+boost]
			return max.floatValue();
		} else if (value < min.floatValue()) {
			// Careful to always return a value within [min, max+boost]
			return min.floatValue();
		}

		return value;
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
		Validate.isTrue(max.floatValue() > min.floatValue(),
				String.format("The given max value %d must be greater than the min value %d", newMax, min));

		final Integer oldMax = max;

		if (newMax.floatValue() < oldMax.floatValue()) {
			// The max value decreased
			if (value.floatValue() > newMax.floatValue()) {
				// Adjust the value first (necessary)
				final Integer oldValue = value;
				value = create(newMax.floatValue());
				previous = oldValue;

				if (log.isDebugEnabled()) {
					log(name, oldValue.floatValue(), newMax.floatValue(), newMax.floatValue() - oldValue.floatValue());
				}
			}

			max = newMax;

			if (log.isDebugEnabled()) {
				log(name + ".Max", oldMax.floatValue(), newMax.floatValue(), newMax.floatValue() - oldMax.floatValue());
			}

			fireChangeEvent();
		} else if (newMax.floatValue() > oldMax.floatValue()) {
			// The max value increased
			max = newMax;

			if (log.isDebugEnabled()) {
				log(name + ".Max", oldMax.floatValue(), newMax.floatValue(), newMax.floatValue() - oldMax.floatValue());
			}

			fireChangeEvent();
		}
	}

	protected Integer create(float value) {
		// Return a value within [Integer.MIN_VALUE,Integer.MAX_VALUE]
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

		if ((max.floatValue() != Float.MAX_VALUE) && max.floatValue() != Integer.MAX_VALUE) {
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
		// Only relevant if a max value is defined
		if (max.floatValue() == Float.MAX_VALUE) {
			return 1.0f;
		}
		if (max.floatValue() == Integer.MAX_VALUE) {
			return 1.0f;
		}

		return value().floatValue() / max.floatValue();
	}

	/**
	 * Tells whether the stat's value is low (that is lower than 20% of its max value).
	 *
	 * @return whether the stat's value is low.
	 */
	public boolean isLow() {
		return getPercent() <= 0.1f;
	}

	// TODO Rename this method
	public Integer improve(int min, int max) {
		Validate.isTrue(min >= 0);
		Validate.isTrue(max >= 0);
		Validate.isTrue(min <= max);

		// TODO The below is wrong ? should be min + RandomUtils.nextInt(max - min)
		// Pick a random value within [min, max]
		final int increment = min + RandomUtils.nextInt(max);

		// Increase the max value first
		incMax(create(maxValue().intValue() + increment));

		// Then increase the value
		return inc(create(value().intValue() + increment));
	}

	/**
	 * Tells whether the stat's actual value is boosted (positively or negatively !).
	 *
	 * @return whether the stat's actual value is boosted.
	 */
	public boolean isBoosted() {
		return boost.floatValue() != 0.0f;
	}
}