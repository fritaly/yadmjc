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
package fr.ritaly.dungeonmaster.champion;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.ritaly.dungeonmaster.Clock;
import fr.ritaly.dungeonmaster.ClockListener;
import fr.ritaly.dungeonmaster.Constants;
import fr.ritaly.dungeonmaster.Direction;
import fr.ritaly.dungeonmaster.Location;
import fr.ritaly.dungeonmaster.Move;
import fr.ritaly.dungeonmaster.Position;
import fr.ritaly.dungeonmaster.Side;
import fr.ritaly.dungeonmaster.Speed;
import fr.ritaly.dungeonmaster.Utils;
import fr.ritaly.dungeonmaster.audio.AudioClip;
import fr.ritaly.dungeonmaster.audio.AudioListener;
import fr.ritaly.dungeonmaster.audio.SoundSystem;
import fr.ritaly.dungeonmaster.champion.Champion.Color;
import fr.ritaly.dungeonmaster.event.ChangeEvent;
import fr.ritaly.dungeonmaster.event.ChangeEventSource;
import fr.ritaly.dungeonmaster.event.ChangeEventSupport;
import fr.ritaly.dungeonmaster.event.ChangeListener;
import fr.ritaly.dungeonmaster.event.DirectionChangeEvent;
import fr.ritaly.dungeonmaster.event.DirectionChangeListener;
import fr.ritaly.dungeonmaster.item.Item;
import fr.ritaly.dungeonmaster.map.Dungeon;
import fr.ritaly.dungeonmaster.map.Element;

/**
 * A party of champions. A party has at least one champion and up to 4 champions.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public class Party implements ChangeEventSource, ClockListener, AudioListener, ChangeListener {

	/**
	 * The possible states of a party. TODO Elaborate on why this is needed
	 *
	 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
	 */
	public static enum State {
		NORMAL,
		CLIMBING_DOWN;
	}

	private final Log log = LogFactory.getLog(Party.class);

	/**
	 * Map storing the champions by their location inside the party.
	 */
	private final Map<Location, Champion> champions = new HashMap<Location, Champion>();

	/**
	 * Set used for assigning a color to champions added to a party.
	 */
	private final Set<Color> colors = EnumSet.allOf(Color.class);

	/**
	 * Support class used for firing change events.
	 */
	private final ChangeEventSupport eventSupport = new ChangeEventSupport();

	/**
	 * The listeners to notify when the party's direction changes.
	 */
	private final List<DirectionChangeListener> directionChangeListeners = new ArrayList<DirectionChangeListener>();

	/**
	 * The direction the party is currently looking into.
	 */
	private Direction lookDirection = Direction.NORTH;

	/**
	 * The party's current position.
	 */
	private Position position;

	/**
	 * The party's current leader. The leader is the champion that grabs the
	 * items in the user interface.
	 */
	private Champion leader;

	/**
	 * The possible item currently held by the party's leader.
	 */
	private Item item;

	/**
	 * The dungeon where the party is crawling.
	 */
	private Dungeon dungeon;

	/**
	 * Whether the party is currently sleeping.
	 */
	private boolean sleeping;

	/**
	 * The spells currently acting on the party.
	 */
	private final PartySpells spells = new PartySpells(this);

	// FIXME Implement serialization

	/**
	 * The party's current state. Never null.
	 */
	private State state = State.NORMAL;

	public Party() {
	}

	public Party(Champion... champions) {
		// The champions array can be null
		if (champions != null) {
			Validate.isTrue(champions.length <= 4, "The given array of champions is too long (4 champions max)");

			for (Champion champion : champions) {
				addChampion(champion);
			}
		}
	}

	private Color pickColor() {
		if (colors.isEmpty()) {
			throw new IllegalStateException("The set of colors is empty");
		}

		final Iterator<Color> iterator = colors.iterator();

		final Color color = iterator.next();

		iterator.remove();

		return color;
	}

	private void releaseColor(Color color) {
		Validate.notNull(color, "The given color is null");
		if (colors.contains(color)) {
			throw new IllegalStateException("The given color " + color + " is already in the color set");
		}

		colors.add(color);
	}

	/**
	 * Returns the champions in the party as a list. The given boolean
	 * determines whether only living (false) or living and dead (true)
	 * champions are returned.
	 *
	 * @param all
	 *            whether dead champions are to be returned too.
	 * @return a list of champions. Never returns null.
	 */
	public List<Champion> getChampions(boolean all) {
		final ArrayList<Champion> result = new ArrayList<Champion>(champions.values());

		if (!all) {
			// Filter out the dead champions
			for (final Iterator<Champion> it = result.iterator(); it.hasNext();) {
				if (it.next().isDead()) {
					it.remove();
				}
			}
		}

		return result;
	}

	/**
	 * Returns the party's size (that is, the number of champions in the party).
	 *
	 * @param all
	 *            whether dead champions are to be counted too.
	 * @return an integer representing a number of champions.
	 */
	public int getSize(boolean all) {
		int size = champions.size();

		if (!all) {
			for (Champion champion : champions.values()) {
				if (champion.isDead()) {
					size--;
				}
			}
		}

		return size;
	}

	/**
	 * Tells whether the party is full (that is it contains 4 champions).
	 *
	 * @return whether the party is full.
	 */
	public boolean isFull() {
		return (champions.size() == 4);
	}

	/**
	 * Tells whether the party is empty (that is it contains no champion).
	 *
	 * @param all
	 *            whether dead champions are to be counted too.
	 * @return whether the party is empty.
	 */
	public boolean isEmpty(boolean all) {
		return getChampions(all).isEmpty();
	}

	/**
	 * Returns the champions located on the given side. The given boolean
	 * determines whether only living (false) or living and dead (true)
	 * champions are considered.
	 *
	 * @param side
	 *            the side where requested champions are located. Can't be null.
	 * @param all
	 *            whether dead champions are to be counted too.
	 * @return a set of champions. Never returns null.
	 */
	public Set<Champion> getChampions(Side side, boolean all) {
		Validate.notNull(side, "The given side is null");

		// A side corresponds to 2 locations
		final Iterator<Location> iterator = side.getLocations().iterator();

		final Location location1 = iterator.next();
		final Location location2 = iterator.next();

		final Set<Champion> result = new HashSet<Champion>();

		final Champion champion1 = getChampion(location1);

		if (champion1 != null) {
			if (champion1.isAlive() || all) {
				result.add(champion1);
			}
		}

		final Champion champion2 = getChampion(location2);

		if (champion2 != null) {
			if (champion2.isAlive() || all) {
				result.add(champion2);
			}
		}

		return result;
	}

	/**
	 * Returns the champion at the given location.
	 *
	 * @param location
	 *            the location where the requested champion is. Can't be null.
	 * @return a champion or null if there's no champion at this location.
	 */
	public Champion getChampion(final Location location) {
		Validate.notNull(location, "The given location is null");

		return champions.get(location);
	}

	/**
	 * Tries adding the given champion to the party and if the operation
	 * succeeded returns the location where the champion was added.
	 *
	 * @param champion
	 *            the champion to add to the party. Can't be null.
	 * @return a location representing where the champion was successfully
	 *         added.
	 */
	public Location addChampion(Champion champion) {
		Validate.notNull(champion, "The given champion is null");
		Validate.isTrue(!champions.containsValue(champion), "The given champion has already joined the party");
		if (isFull()) {
			throw new IllegalStateException("The party is already full");
		}

		if (log.isDebugEnabled()) {
			log.debug(String.format("%s is joigning the party ...", champion.getName()));
		}

		final boolean wasEmpty = isEmpty(true);

		// Which locations are already occupied ?
		final EnumSet<Location> occupied;

		if (champions.isEmpty()) {
			occupied = EnumSet.noneOf(Location.class);
		} else {
			occupied = EnumSet.copyOf(champions.keySet());
		}

		// Free locations ?
		final EnumSet<Location> free = EnumSet.complementOf(occupied);

		if (log.isDebugEnabled()) {
			log.debug("Free locations = " + free);
		}

		// Get the first free location
		final Location location = free.iterator().next();

		champions.put(location, champion);

		// Listen to the events fired by the champion
		champion.addChangeListener(this);

		if (log.isDebugEnabled()) {
			log.debug(String.format("%s.Location: %s", champion.getName(), location));
		}

		// Assign a color to this champion
		final Color color = pickColor();

		if (log.isDebugEnabled()) {
			log.debug(String.format("%s.Color: %s", champion.getName(), color));
		}

		champion.setColor(color);
		champion.setParty(this);

		if (wasEmpty) {
			// This champion become the new leader
			setLeader(champion);
		}

		fireChangeEvent();

		if (log.isInfoEnabled()) {
			log.info(String.format("%s joined the party", champion.getName()));
		}

		return location;
	}

	/**
	 * Removes the given champion for this party and (if the operation
	 * succeeded) returns the location where the champion was at.
	 *
	 * @param champion
	 *            the champion to remove. Can't be null.
	 * @return the removed champion or null if it wasn't in this party.
	 */
	public Location removeChampion(Champion champion) {
		Validate.notNull(champion, "The given champion is null");
		Validate.isTrue(champions.containsValue(champion), String.format("The given champion %s hasn't joined this party", champion.getName()));

		if (log.isDebugEnabled()) {
			log.debug(String.format("%s is leaving the party ...", champion.getName()));
		}

		// Where is the champion inside the party ?
		for (Location location : Location.values()) {
			if (champions.get(location) == champion) {
				if (log.isDebugEnabled()) {
					log.debug(String.format("Found %s at location %s", champion.getName(), location));
				}

				if (champion.isLeader()) {
					if (getSize(false) >= 2) {
						// Choose another leader before removing this champion
						final Iterator<Champion> iterator = getChampions(false).iterator();

						Champion newLeader = champion;

						while (iterator.hasNext() && (newLeader == champion)) {
							newLeader = iterator.next();
						}

						setLeader(newLeader);
					} else {
						// The party will be empty, unset the leader before
						// removing the champion
						setLeader(null, false);
					}
				}

				// Remove the champion
				champions.remove(location);

				// Stop listening to the champion's event
				champion.removeChangeListener(this);

				releaseColor(champion.getColor());
				champion.setParty(null);
				champion.setColor(null);

				fireChangeEvent();

				if (log.isInfoEnabled()) {
					log.info(String.format("%s left the party", champion.getName()));
				}

				return location;
			}
		}

		// The champion couldn't be found
		return null;
	}

	/**
	 * Sets the party's new leader to the given champion.
	 *
	 * @param champion
	 *            the champion to define as new leader. Can't be null.
	 */
	public void setLeader(Champion champion) {
		setLeader(champion, true);
	}

	private void setLeader(Champion champion, boolean check) {
		if (champion == null) {
			if (check) {
				throw new IllegalArgumentException("The given champion is null");
			}
		}
		if (!champions.containsValue(champion) && check) {
			throw new IllegalArgumentException("The given champion hasn't joined this party");
		}

		final Champion previousLeader = this.leader;

		this.leader = champion;

		if (previousLeader != champion) {
			if (previousLeader != null) {
				if (leader != null) {
					if (log.isDebugEnabled()) {
						log.debug(String.format("%s lost the leadership. New leader is %s", previousLeader.getName(),
								leader.getName()));
					}
				} else {
					if (log.isDebugEnabled()) {
						log.debug(String.format("%s lost the leadership", previousLeader.getName()));
					}
				}
			} else {
				if (log.isDebugEnabled()) {
					log.debug(String.format("%s gained the leadership", leader.getName()));
				}
			}

			if (item != null) {
				// Fire a change event for the 2 champions (the item held was
				// passed between 2 champions)
				previousLeader.fireChangeEvent();
				champion.fireChangeEvent();
			}

			// Fire an event to notify a change of leader
			fireChangeEvent();
		}
	}

	/**
	 * Swaps the champions at the 2 given locations.
	 *
	 * @param location1
	 *            the first location to swap. Can't be null.
	 * @param location2
	 *            the second location to swap. Can't be null.
	 */
	public void swap(Location location1, Location location2) {
		Validate.notNull(location1, "The given first location is null");
		Validate.notNull(location2, "The given second location is null");
		Validate.isTrue(location1 != location2, "The two given locations are equal");

		// Remove the 2 champions (can be null)
		final Champion champion1 = champions.remove(location1);
		final Champion champion2 = champions.remove(location2);

		// Add them back at the new location
		if (champion2 != null) {
			champions.put(location1, champion2);
		}
		if (champion1 != null) {
			champions.put(location2, champion1);
		}

		if ((champion1 != null) || (champion2 != null)) {
			// Notify the change
			fireChangeEvent();
		}
	}

	private void fireChangeEvent() {
		eventSupport.fireChangeEvent(new ChangeEvent(this));
	}

	@Override
	public void addChangeListener(ChangeListener listener) {
		eventSupport.addChangeListener(listener);
	}

	@Override
	public void removeChangeListener(ChangeListener listener) {
		eventSupport.removeChangeListener(listener);
	}

	/**
	 * Returns the direction where this party is looking at.
	 *
	 * @return a direction representing where the party is looking at.
	 */
	public Direction getLookDirection() {
		return lookDirection;
	}

	@Override
	public Direction getDirection() {
		return getLookDirection();
	}

	@Override
	public Position getPosition() {
		return position;
	}

	/**
	 * Returns the {@link Position} next to the party's current position and in
	 * its look {@link Direction}.
	 *
	 * @return a {@link Position} or null.
	 */
	public Position getFacingPosition() {
		// TODO Rename this method
		if (position != null) {
			// The look direction is never null
			return position.towards(getLookDirection());
		}

		return null;
	}

	/**
	 * Teleports the party to the given target position.
	 *
	 * @param position
	 *            the target position where to teleport the party. Can't be
	 *            null.
	 * @param silent
	 *            if the teleport is silent. When false a specific sound will be
	 *            played to hint a teleport occurred.
	 */
	public void teleport(Position position, boolean silent) {
		// Keep the current look direction
		teleport(position, this.lookDirection, silent);
	}

	/**
	 * Teleports the party to the given target position, possibly also changing
	 * the party's direction.
	 *
	 * @param position
	 *            the target position where to teleport the party. Can't be
	 *            null.
	 * @param direction
	 *            the new look direction. Can't be null.
	 * @param silent
	 *            if the teleport is silent. When false a specific sound will be
	 *            played to hint a teleport occurred.
	 */
	public void teleport(final Position position, final Direction direction, final boolean silent) {
		Validate.notNull(position, "The given position is null");
		Validate.notNull(direction, "The given direction is null");

		boolean notify = false;

		if (!position.equals(this.position)) {
			// Move the party
			setPosition(position, false);

			notify = true;
		}

		if (!direction.equals(this.lookDirection)) {
			// Change the direction
			setLookDirection(direction, false);

			notify = true;
		}

		if (!silent) {
			// Play the teleport sound
			SoundSystem.getInstance().play(getPosition(), AudioClip.TELEPORT);
		}

		if (notify) {
			// Notify the change
			fireChangeEvent();
		}
	}

//	public void move(Direction direction) {
//		Validate.notNull(direction, "The given direction is null");
//
//		// Jouer le son des pas
//		SoundSystem.getInstance().play(getPosition(), AudioClip.STEP);
//
//		if (log.isDebugEnabled()) {
//			log.debug("Moving party towards " + direction + " ...");
//		}
//
//		final Position newPosition = direction.change(this.position);
//
//		setPosition(newPosition, true);
//
//		if (log.isInfoEnabled()) {
//			log.info("Party moved to " + newPosition);
//		}
//	}

	public void turn(final Move move) {
		Validate.notNull(move, "The given move is null");

		if (move.changesDirection()) {
			// This move changes the look direction
			setLookDirection(move.changeDirection(this.lookDirection), true);
		}
	}

	@Override
	public void setDirection(Direction direction) {
		setLookDirection(direction);
	}

	/**
	 * Sets the party's look direction to the given value.
	 *
	 * @param direction
	 *            the look direction to set. Can't be null.
	 */
	public void setLookDirection(Direction direction) {
		setLookDirection(direction, true);
	}

	private void setLookDirection(Direction direction, boolean notify) {
		Validate.notNull(direction, "The given direction is null");

		if (this.lookDirection != direction) {
			final Direction previousDirection = this.lookDirection;

			this.lookDirection = direction;

			if (log.isDebugEnabled()) {
				log.debug(String.format("Party.LookDirection: %s -> %s", previousDirection, direction));
			}

			if (notify) {
				fireChangeEvent();

				// Fire a specific event to notify the change
				fireDirectionChangeEvent();
			}
		}
	}

	private void setPosition(final Position position, final boolean notify) {
		Validate.notNull(position, "The given position is null");

		final Position oldPosition = this.position;

		if (!position.equals(this.position)) {
			this.position = position;

			if (log.isDebugEnabled()) {
				log.debug(String.format("Party.Position: %s -> %s", oldPosition, position));
			}

			if (notify) {
				fireChangeEvent();
			}
		}
	}

	/**
	 * Sets the party's position to the given value.
	 *
	 * @param position the new party's position to set. Can't be null.
	 */
	public void setPosition(Position position) {
		setPosition(position, true);
	}

	/**
	 * Returns the party's current leader (that is, the one holding the possible item).
	 *
	 * @return a champion or null if the party has no current leader.
	 */
	public Champion getLeader() {
		return leader;
	}

	private Champion selectNewLeader() {
		if (isEmpty(true)) {
			throw new IllegalStateException("The party is empty");
		}
		if (getSize(false) == 1) {
			// Only one living champion in the party
			if (getChampions(false).iterator().next() != this.leader) {
				// The only living champion is not the current leader. This happens
				// when the leader just died and a new one has to be chosen
			} else {
				// The only living champion is also the current leader
				return this.leader;
			}
		} else if (getSize(false) == 0) {
			// No living champion left
			setLeader(null, false);
		}

		if (log.isDebugEnabled()) {
			log.debug("Selecting new leader ...");
		}

		// Identify possible leaders
		final List<Champion> candidates = new ArrayList<Champion>();

		// Only consider the living champions that aren't the current leader
		for (Champion champion : champions.values()) {
			if (champion.isAlive() && (champion != this.leader)) {
				candidates.add(champion);
			}
		}

		// Randomly chose a new leader
		final Champion newLeader = candidates.get(RandomUtils.nextInt(candidates.size()));

		setLeader(newLeader);

		if (log.isInfoEnabled()) {
			log.info(String.format("%s was promoted leader", newLeader.getName()));
		}

		return newLeader;
	}

	/**
	 * Returns the party's move speed. The overall speed is the speed of the
	 * slowest champion !
	 *
	 * @return the party's speed. Never returns null.
	 */
	public Speed getMoveSpeed() {
		Speed speed = Speed.UNDEFINED;

		// The slowest champion determines the party's speed
		for (Champion champion : champions.values()) {
			if (champion.isDead()) {
				// This one is dead, skip it
				continue;
			}

			// The slowest move speed is the one with the highest value
			if (champion.getMoveSpeed().getValue() > speed.getValue()) {
				speed = champion.getMoveSpeed();
			}
		}

		return speed;
	}

	/**
	 * Puts the given item into the (third) hand of the party's current leader
	 * and returns the possible previously held item.
	 *
	 * @param item
	 *            the item to put into the leader's (third) hand. Can't be null.
	 * @return the previously held item or null if there was none.
	 */
	public Item grab(final Item item) {
		Validate.notNull(item, "The given item is null");
		if (isEmpty(false)) {
			throw new IllegalStateException("Unable to grab item with an empty party");
		}
		if (leader == null) {
			throw new IllegalStateException("Unable to grab item for there is no leader");
		}

		final Item removed = this.item;

		this.item = item;

		if (removed != item) {
			if (removed != null) {
				if (removed instanceof DirectionChangeListener) {
					// Unregister as a listener
					removeDirectionChangeListener((DirectionChangeListener) this);
				}
			}
			if (item != null) {
				if (item instanceof DirectionChangeListener) {
					// Register as a listener
					addDirectionChangeListener((DirectionChangeListener) this);
				}
			}

			// Have the leader fire a change event
			leader.fireChangeEvent();

			if (removed != null) {
				if (log.isDebugEnabled()) {
					log.debug(String.format("%d released %s and grabbed %s", leader.getName(), removed, this.item));
				}
			} else {
				if (log.isDebugEnabled()) {
					log.debug(String.format("%s grabbed %s", leader.getName(), this.item));
				}
			}

			fireChangeEvent();
		}

		return removed;
	}

	/**
	 * Tells whether the party's current leader is holding an item.
	 *
	 * @return whether the party's current leader is holding an item.
	 */
	public boolean hasItem() {
		return (item != null);
	}

	/**
	 * Returns the item currently held by the party's leader (if any).
	 *
	 * @return an item or null if the party's current leader isn't holding an
	 *         item.
	 */
	public Item getItem() {
		return item;
	}

	/**
	 * Make the party's current leader drop the item currently held (if any) and returns it.
	 *
	 * @return the dropped item or null if the leader isn't holding an item.
	 */
	public Item release() {
		if (isEmpty(false)) {
			throw new IllegalStateException("Unable to release an item for an empty party");
		}

		final Item removed = this.item;

		if (removed != null) {
			if (removed instanceof DirectionChangeListener) {
				// Unregister as a listener
				removeDirectionChangeListener((DirectionChangeListener) this);
			}
		}

		this.item = null;

		if (removed != item) {
			// Have the leader fire a change event
			leader.fireChangeEvent();

			if (log.isDebugEnabled()) {
				log.debug(String.format("%s released item %s", leader.getName(), removed));
			}

			fireChangeEvent();
		}

		return removed;
	}

	@Override
	public boolean clockTicked() {
		// No need to dispatch the call to the champions as they're already
		// listening to clock ticks on their own

		// Propagate the call to the party's spells
		spells.clockTicked();

		// TODO Is the party sleeping ?

		// Keep listening as long as the party's inside a dungeon
		return (dungeon != null);
	}

	/**
	 * Returns the dungeon this party is inside.
	 *
	 * @return a dungeon or null if the party isn't inside a dungeon.
	 */
	public Dungeon getDungeon() {
		return dungeon;
	}

	/**
	 * Returns the current element where this party is at.
	 *
	 * @return an element or null if the party isn't inside a dungeon.
	 */
	public Element getElement() {
		return (dungeon != null) ? dungeon.getElement(getPosition()) : null;
	}

	/**
	 * Sets the dungeon inside which the party is.
	 *
	 * @param dungeon
	 *            the dungeon where to "install" the party. Can be null to unset
	 *            the dungeon.
	 */
	public void setDungeon(Dungeon dungeon) {
		// The dungeon can be null
		if ((this.dungeon != null) && (dungeon != null)) {
			// Not supposed to happen
			throw new IllegalArgumentException("The dungeon is already set");
		}

		this.dungeon = dungeon;

		if (this.dungeon != null) {
			// Start listening to clock ticks
			Clock.getInstance().register(this);
		}
	}

	@Override
	public String toString() {
		return String.format("%s[position=%s]", Party.class.getSimpleName(), position);
	}

	/**
	 * Tells whether the party is currently sleeping.
	 *
	 * @return whether the party is currently sleeping.
	 */
	public boolean isSleeping() {
		return sleeping;
	}

	/**
	 * Makes the party sleep.
	 */
	public void sleep() {
		setSleeping(true);
	}

	/**
	 * Wakes up the party if sleeping.
	 */
	public void awake() {
		setSleeping(false);
	}

	private void setSleeping(boolean sleeping) {
		final boolean wasSleeping = this.sleeping;

		this.sleeping = sleeping;

		if (!wasSleeping && sleeping) {
			if (log.isDebugEnabled()) {
				log.debug("Party has just fallen asleep");
			}

			fireChangeEvent();
		} else if (wasSleeping && !sleeping) {
			if (log.isDebugEnabled()) {
				log.debug("Party has just waken up");
			}

			fireChangeEvent();
		}
	}

	/**
	 * Returns the spells acting on this party.
	 *
	 * @return
	 */
	public PartySpells getSpells() {
		return spells;
	}

	/**
	 * Returns the light generated by this party as an integer within [0,255].
	 * The value returned takes into account the the light generated by each
	 * living champion (FUL spells, illimulets, torches).
	 *
	 * @return an integer within [0,255] representing the light generated by
	 *         this party.
	 */
	public int getLight() {
		int light = 0;

		// Contribution for this (living) champion ?
		for (Champion champion : getChampions(false)) {
			light += champion.getLight();
		}

		// Ensure the final result is within [0,255]
		return Utils.bind(light, 0, Constants.MAX_LIGHT);
	}

	/**
	 * Tells whether all champions in the party are dead. This means a
	 * "Game Over". Returns false if the party has no champions.
	 *
	 * @return whether the party has some champions and all champions in the
	 *         party are dead.
	 */
	public boolean allChampionsDead() {
		if (champions.isEmpty()) {
			// The champions can't be dead since there are none !
			return false;
		}

		for (Champion champion : getChampions(true)) {
			if (champion.isAlive()) {
				return false;
			}
		}

		return true;
	}

	public void addDirectionChangeListener(DirectionChangeListener listener) {
		if (listener != null) {
			directionChangeListeners.add(listener);
		}
	}

	public void removeDirectionChangeListener(DirectionChangeListener listener) {
		if (listener != null) {
			directionChangeListeners.remove(listener);
		}
	}

	/**
	 * Fires an event to notify of a direction change.
	 */
	protected final void fireDirectionChangeEvent() {
		// Reuse the immutable event among listeners
		final DirectionChangeEvent event = new DirectionChangeEvent(this);

		for (DirectionChangeListener listener : directionChangeListeners) {
			listener.directionChanged(event);
		}
	}

	/**
	 * Tells whether the party is invisible (because of the "Invisibility" spell).
	 *
	 * @return whether the party is invisible.
	 */
	public final boolean isInvisible() {
		return getSpells().isInvisibilityActive();
	}

	/**
	 * Tells whether the party can dispell illusions.
	 *
	 * @return whether the party can dispell illusions.
	 */
	public final boolean dispellsIllusions() {
		return getSpells().isDispellIllusionActive();
	}

	/**
	 * Tells whether the party can see through walls.
	 *
	 * @return whether the party can see through walls.
	 */
	public final boolean seesThroughWalls() {
		return getSpells().isSeeThroughWallsActive();
	}

	/**
	 * Returns the id of the last clock tick during which the party was
	 * attacked.
	 *
	 * @return a positive integer identifying a clock tick id or -1 if the party
	 *         has never been attacked.
	 */
	public int getLastAttackTick() {
		int result = -1;

		for (Champion champion : champions.values()) {
			if (champion.isAlive()) {
				result = Math.max(result, champion.getLastAttackTick());
			}
		}

		return result;
	}

	/**
	 * Returns the location of the given champion inside the party.
	 *
	 * @param champion
	 *            the champion whose location is requested. Can't be null.
	 * @return the location where this champion is inside the party or null if
	 *         the given champion couldn't be found.
	 */
	Location getLocation(Champion champion) {
		Validate.notNull(champion, "The given champion is null");

		for (Location location : champions.keySet()) {
			if (champions.get(location) == champion) {
				return location;
			}
		}

		return null;
	}

	@Override
	public void onChangeEvent(ChangeEvent event) {
		if (champions.containsValue(event.getSource())) {
			// The event source is one of our champions
			final Champion champion = (Champion) event.getSource();

			if (champion.isDead()) {
				// The champion just died. Select a new leader if there are
				// living champions left
				if (getChampions(false).isEmpty()) {
					// All champions are dead. That's a "Game Over"
					fireChangeEvent();
				} else {
					// At least one champion still living
					selectNewLeader();
				}
			}
		}
	}

	/**
	 * Returns the party's current state.
	 *
	 * @return the party state.
	 */
	public State getState() {
		return state;
	}

	public void setState(State state) {
		Validate.notNull(state, "The given state is null");

		if (!this.state.equals(state)) {
			if (log.isDebugEnabled()) {
				log.debug(String.format("Party.State: %s -> %s", this.state, state));
			}

			// All state transitions are valid
			this.state = state;
		}
	}
}