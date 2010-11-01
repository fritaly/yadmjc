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
 * Un groupe de {@link Champion}s.
 *
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public class Party implements ChangeEventSource, ClockListener, AudioListener,
		ChangeListener {

	private final Log log = LogFactory.getLog(Party.class);

	/**
	 * {@link Map} contenant les {@link Champion}s selon leur emplacement dans
	 * le groupe.
	 */
	private final Map<Location, Champion> champions = new HashMap<Location, Champion>();

	/**
	 * Pool contenant les couleurs restantes que l'on peut assigner aux
	 * {@link Champion}s qui rejoignent le groupe.
	 */
	private final Set<Color> colors = EnumSet.allOf(Color.class);

	private final ChangeEventSupport eventSupport = new ChangeEventSupport();

	/**
	 * {@link List} des {@link DirectionChangeListener} que l'on doit notifier
	 * quand le groupe change de direction.
	 */
	private final List<DirectionChangeListener> directionChangeListeners = new ArrayList<DirectionChangeListener>();

	/**
	 * La {@link Direction} dans laquelle le groupe regarde.
	 */
	private Direction lookDirection = Direction.NORTH;

	/**
	 * La {@link Position} actuelle du groupe.
	 */
	private Position position;

	/**
	 * Le {@link Champion} désigné comme le leader du groupe.
	 */
	private Champion leader;

	/**
	 * L'éventuel objet actuellement porté par le leader du groupe.
	 */
	private Item item;

	/**
	 * Le donjon où le groupe de {@link Champion}s est actuellement situé.
	 */
	private Dungeon dungeon;

	/**
	 * Indique si le groupe est en train de dormir.
	 */
	private boolean sleeping;

	private final PartySpells spells = new PartySpells(this);

	// FIXME Implémenter Serialization !!

	public Party() {
	}

	public Party(Champion... champions) {
		// Le paramètre champions peut être null
		if (champions != null) {
			Validate.isTrue(champions.length <= 4,
					"The given array of champions is too long (4 champions max)");

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
		if (color == null) {
			throw new IllegalArgumentException("The given color is null");
		}
		if (colors.contains(color)) {
			throw new IllegalStateException("The given color <" + color
					+ "> is already in the color set");
		}

		colors.add(color);
	}

	/**
	 * Retourne les champions du groupe sous forme de {@link List}.
	 * 
	 * @param all
	 *            indique si tous les champions doivent être retournés dans la
	 *            liste, c'est-à-dire ceux qui sont vivants ET morts.
	 * @return une List&lt;Champion&gt;. Ne retourne jamais null.
	 */
	public List<Champion> getChampions(boolean all) {
		final ArrayList<Champion> result = new ArrayList<Champion>(
				champions.values());

		if (!all) {
			// Filtrer les champions morts
			for (Iterator<Champion> it = result.iterator(); it.hasNext();) {
				Champion champion = it.next();

				if (champion.isDead()) {
					it.remove();
				}
			}
		}

		return result;
	}
	
	/**
	 * Retourne la taille du groupe.
	 * 
	 * @param all
	 *            indique si tous les champions doivent être comptés,
	 *            c'est-à-dire ceux qui sont vivants ET morts.
	 * @return un entier positif ou nul
	 */
	public int getSize(boolean all) {
		int size = champions.size();

		for (Champion champion : champions.values()) {
			if (champion.isDead()) {
				size--;
			}
		}

		return size;
	}

	/**
	 * Indique si le groupe est plein, c'est-à-dire qu'il compte 4
	 * {@link Champion}s.
	 * 
	 * @return si le groupe est plein, c'est-à-dire qu'il compte 4
	 *         {@link Champion}s.
	 */
	public boolean isFull() {
		return (champions.size() == 4);
	}

	public boolean isEmpty(boolean all) {
		return getChampions(all).isEmpty();
	}
	
	public Set<Champion> getChampions(Side side, boolean all) {
		Validate.notNull(side, "The given side is null");
		
		final Iterator<Location> iterator = side.getLocations().iterator();
		
		final Location location1 = iterator.next();
		final Location location2 = iterator.next();
		
		final Set<Champion> set = new HashSet<Champion>();
		
		final Champion champion1 = getChampion(location1);
		
		if (champion1 != null) {
			if (champion1.isAlive() || all) {
				set.add(champion1);
			}
		}
		
		final Champion champion2 = getChampion(location2);
		
		if (champion2 != null) {
			if (champion2.isAlive() || all) {
				set.add(champion2);
			}
		}
		
		return set;
	}
	
	public Champion getChampion(Location location) {
		if (location == null) {
			throw new IllegalArgumentException("The given location is null");
		}

		return champions.get(location);
	}

	public Location addChampion(Champion champion) {
		if (champion == null) {
			throw new IllegalArgumentException("The given champion is null");
		}
		if (isFull()) {
			throw new IllegalStateException("The party is already full");
		}
		if (champions.containsValue(champion)) {
			throw new IllegalArgumentException(
					"The given champion has already joined the party");
		}

		if (log.isDebugEnabled()) {
			log.debug(champion.getName() + " is joigning the party ...");
		}

		final boolean wasEmpty = isEmpty(true);

		// Emplacements déjà occupés
		final EnumSet<Location> busy;

		if (champions.isEmpty()) {
			busy = EnumSet.noneOf(Location.class);
		} else {
			busy = EnumSet.copyOf(champions.keySet());
		}

		// if (log.isDebugEnabled()) {
		// log.debug("Busy locations = " + busy);
		// }

		// Emplacements libres
		final EnumSet<Location> free = EnumSet.complementOf(busy);

		if (log.isDebugEnabled()) {
			log.debug("Free locations = " + free);
		}

		// Ne peut être null
		final Location location = free.iterator().next();

		champions.put(location, champion);

		// Ecouter les évènements levés par le champion
		champion.addChangeListener(this);

		if (log.isDebugEnabled()) {
			log.debug(champion.getName() + ".Location: " + location);
		}

		final Color color = pickColor();

		if (log.isDebugEnabled()) {
			log.debug(champion.getName() + ".Color: " + color);
		}

		champion.setColor(color);
		champion.setParty(this);

		if (wasEmpty) {
			// Elire le nouveau leader
			setLeader(champion);
		}

		fireChangeEvent();

		if (log.isInfoEnabled()) {
			log.info(champion.getName() + " joined the party");
		}

		return location;
	}

	public Location removeChampion(Champion champion) {
		if (champion == null) {
			throw new IllegalArgumentException("The given champion is null");
		}
		if (!champions.containsValue(champion)) {
			throw new IllegalArgumentException("The given champion <"
					+ champion.getName() + "> hasn't joined this party");
		}

		if (log.isDebugEnabled()) {
			log.debug(champion.getName() + " is leaving the party ...");
		}

		// Recherche du champion dans le groupe
		for (Location location : Location.values()) {
			if (champions.get(location) == champion) {
				if (log.isDebugEnabled()) {
					log.debug("Found " + champion.getName() + " at location "
							+ location);
				}

				if (champion.isLeader()) {
					if (getSize(false) >= 2) {
						// Elire un nouveau leader avant de retirer ce champion
						// du groupe
						final Iterator<Champion> iterator = getChampions(false)
								.iterator();

						Champion newLeader = champion;

						while (iterator.hasNext() && (newLeader == champion)) {
							newLeader = iterator.next();
						}

						setLeader(newLeader);
					} else {
						setLeader(null, false);
					}
				}

				// On peut maitenant retirer le champion du groupe
				champions.remove(location);

				// Ne plus écouter les évènements levés par le champion
				champion.removeChangeListener(this);

				releaseColor(champion.getColor());
				champion.setParty(null);
				champion.setColor(null);

				fireChangeEvent();

				if (log.isInfoEnabled()) {
					log.info(champion.getName() + " left the party");
				}

				return location;
			}
		}

		// Champion non trouvé
		return null;
	}

	/**
	 * Définit le nouveau {@link Champion} du groupe.
	 * 
	 * @param champion
	 *            le {@link Champion} qui doit être élu leader du groupe.
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
			throw new IllegalArgumentException(
					"The given champion hasn't joined this party");
		}

		final Champion previousLeader = this.leader;

		this.leader = champion;

		if (previousLeader != champion) {
			if (previousLeader != null) {
				if (leader != null) {
					if (log.isDebugEnabled()) {
						log.debug(previousLeader.getName()
								+ " lost the party leadership. New leader is "
								+ leader.getName());
					}
				} else {
					if (log.isDebugEnabled()) {
						log.debug(previousLeader.getName()
								+ " lost the party leadership");
					}
				}
			} else {
				if (log.isDebugEnabled()) {
					log.debug(leader.getName() + " gained the party leadership");
				}
			}

			if (item != null) {
				// Lever un évènement sur chaque champion (l'objet change de
				// main)
				previousLeader.fireChangeEvent();
				champion.fireChangeEvent();
			}

			// Ne lever un évènement que si strictement nécessaire
			fireChangeEvent();
		}
	}

	/**
	 * Echange les {@link Champion}s situés aux deux emplacements donnés.
	 * 
	 * @param location1
	 *            une instance de {@link Location} représentant le premier
	 *            emplacement à échanger.
	 * @param location2
	 *            une instance de {@link Location} représentant le second
	 *            emplacement à échanger.
	 */
	public void swap(Location location1, Location location2) {
		if (location1 == null) {
			throw new IllegalArgumentException(
					"The given first location is null");
		}
		if (location2 == null) {
			throw new IllegalArgumentException(
					"The given second location is null");
		}

		// Retirer les deux champions
		final Champion champion1 = champions.remove(location1);
		final Champion champion2 = champions.remove(location2);

		// Replacer les deux champions
		if (champion2 != null) {
			champions.put(location1, champion2);
		}
		if (champion1 != null) {
			champions.put(location2, champion1);
		}

		if ((champion1 != null) || (champion2 != null)) {
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

	public void teleport(Position position, boolean silent) {
		// Conserver la direction de regard actuelle
		teleport(position, this.lookDirection, silent);
	}

	public void teleport(Position position, Direction direction, boolean silent) {
		Validate.isTrue(position != null, "The given position is null");
		Validate.isTrue(direction != null, "The given direction is null");

		boolean notify = false;

		if (!position.equals(this.position)) {
			// Déplacer le groupe
			setPosition(position, false);

			notify = true;
		}

		if (!direction.equals(this.lookDirection)) {
			// Le faire changer de direction
			setLookDirection(direction, false);

			notify = true;
		}

		if (!silent) {
			// Jouer le son de la téléportation
			SoundSystem.getInstance().play(getPosition(), AudioClip.TELEPORT);
		}

		if (notify) {
			fireChangeEvent();
		}
	}

	public void move(Direction direction) {
		if (direction == null) {
			throw new IllegalArgumentException("The given direction is null");
		}

		// Jouer le son des pas
		SoundSystem.getInstance().play(getPosition(), AudioClip.STEP);

		if (log.isDebugEnabled()) {
			log.debug("Moving party towards " + direction + " ...");
		}

		final Position newPosition = direction.change(this.position);

		setPosition(newPosition, true);

		if (log.isInfoEnabled()) {
			log.info("Party moved to " + newPosition);
		}
	}

	public void turn(Move move) {
		if (move == null) {
			throw new IllegalArgumentException("The given move is null");
		}

		if (move.changesDirection()) {
			// Le mouvement fait tourner le groupe
			setLookDirection(move.changeDirection(this.lookDirection), true);
		}
	}

	@Override
	public void setDirection(Direction direction) {
		setLookDirection(direction);
	}

	public void setLookDirection(Direction direction) {
		setLookDirection(direction, true);
	}

	private void setLookDirection(Direction direction, boolean notify) {
		if (direction == null) {
			throw new IllegalArgumentException("The given direction is null");
		}

		if (this.lookDirection != direction) {
			final Direction previousDirection = this.lookDirection;

			this.lookDirection = direction;

			if (log.isDebugEnabled()) {
				log.debug("Party.LookDirection: " + previousDirection + " -> "
						+ direction);
			}

			if (notify) {
				fireChangeEvent();

				// Déclencher un évènement spécifique pour notifier du
				// changement de direction
				fireDirectionChangeEvent();
			}
		}
	}

	private void setPosition(Position position, boolean notify) {
		if (position == null) {
			throw new IllegalArgumentException("The given position is null");
		}

		final Position oldPosition = this.position;

		if (!position.equals(this.position)) {
			this.position = position;

			if (log.isDebugEnabled()) {
				log.debug("Party.Position: " + oldPosition + " -> " + position);
			}

			if (notify) {
				fireChangeEvent();
			}
		}
	}

	public void setPosition(Position position) {
		setPosition(position, true);
	}

	public Champion getLeader() {
		return leader;
	}

	private Champion selectNewLeader() {
		if (isEmpty(true)) {
			throw new IllegalStateException("The party is empty");
		}
		if (getSize(false) == 1) {
			// Un seul champion vivant dans le groupe
			if (getChampions(false).iterator().next() != this.leader) {
				// Le seul champion vivant du groupe n'est pas l'actuel leader.
				// Cas où le leader vient de mourir et qu'il faut en désigner
				// un nouveau à sa place !
			} else {
				// Le seul champion vivant du groupe est déjà l'actuel leader
				return this.leader;
			}
		} else if (getSize(false) == 0) {
			// Tous les champions sont morts
			setLeader(null, false);
		}

		if (log.isDebugEnabled()) {
			log.debug("Selecting new leader ...");
		}

		// Identifier les candidats au poste
		final List<Champion> candidates = new ArrayList<Champion>();

		// Ne conserver que les champions vivants qui ne sont pas déjà leader
		for (Champion champion : champions.values()) {
			if (champion.isAlive() && (champion != this.leader)) {
				candidates.add(champion);
			}
		}

		// Choisir un leader au hasard
		final Champion newLeader = candidates.get(RandomUtils
				.nextInt(candidates.size()));

		setLeader(newLeader);

		if (log.isInfoEnabled()) {
			log.info(newLeader.getName() + " was elected leader");
		}

		return newLeader;
	}

	/**
	 * Retourne la vitesse de déplacement du groupe sous forme d'une instance de
	 * {@link Speed}.
	 * 
	 * @return une instance de {@link Speed}. Ne retourne jamais null.
	 */
	public Speed getMoveSpeed() {
		// Vitesse de déplacement non définie
		Speed speed = Speed.UNDEFINED;

		// C'est le champion le plus lent qui détermine la vitesse du groupe
		for (Champion champion : champions.values()) {
			if (champion.isDead()) {
				// Il est mort, ne pas le prendre en compte
				continue;
			}

			if (champion.getMoveSpeed().getValue() > speed.getValue()) {
				speed = champion.getMoveSpeed();
			}
		}

		return speed;
	}

	/**
	 * Place l'objet donné dans la main du leader du groupe et retourne l'objet
	 * qu'il portait précédemment (s'il y en avait un) ou null.
	 * 
	 * @param item
	 *            un {@link Item} représentant l'objet que le leader doit
	 *            saisir.
	 * @return une instance de {@link Item} ou null.
	 */
	public Item grab(Item item) {
		if (item == null) {
			throw new IllegalArgumentException("The given item is null");
		}
		if (isEmpty(false)) {
			throw new IllegalStateException(
					"Unable to grab item with an empty party");
		}
		if (leader == null) {
			throw new IllegalStateException(
					"Unable to grab item for there is no leader");
		}

		final Item removed = this.item;

		this.item = item;

		if (removed != item) {
			if (removed != null) {
				if (removed instanceof DirectionChangeListener) {
					// Supprimer le listener
					removeDirectionChangeListener((DirectionChangeListener) this);
				}
			}
			if (item != null) {
				if (item instanceof DirectionChangeListener) {
					// Enregistrer le listener
					addDirectionChangeListener((DirectionChangeListener) this);
				}
			}

			// Lever un évènement sur le leader du groupe
			leader.fireChangeEvent();

			if (removed != null) {
				if (log.isDebugEnabled()) {
					log.debug(leader.getName() + " released " + removed
							+ " and grabbed " + this.item);
				}
			} else {
				if (log.isDebugEnabled()) {
					log.debug(leader.getName() + " grabbed " + this.item);
				}
			}

			fireChangeEvent();
		}

		return removed;
	}

	/**
	 * Indique si le leader du groupe porte un objet en main.
	 * 
	 * @return si le leader du groupe porte un objet en main.
	 */
	public boolean hasItem() {
		return (item != null);
	}

	public Item getItem() {
		return item;
	}

	/**
	 * Libère l'objet que le leader du groupe porte (s'il y en a un) et le
	 * retourne.
	 * 
	 * @return une instance de {@link Item} ou null.
	 */
	public Item release() {
		if (isEmpty(false)) {
			throw new IllegalStateException(
					"Unable to release an item for an empty party");
		}

		final Item removed = this.item;

		if (removed != null) {
			if (removed instanceof DirectionChangeListener) {
				// Supprimer le listener
				removeDirectionChangeListener((DirectionChangeListener) this);
			}
		}

		this.item = null;

		if (removed != item) {
			// Lever un évènement sur le leader du groupe
			leader.fireChangeEvent();

			if (log.isDebugEnabled()) {
				log.debug(leader.getName() + " released item " + removed);
			}

			fireChangeEvent();
		}

		return removed;
	}

	@Override
	public boolean clockTicked() {
		// Pas besoin de dispatcher l'appel aux champions car ils se sont déjà
		// enregistrés de leur côté

		// Traiter les sorts du groupe
		spells.clockTicked();

		// TODO Le groupe dort ?

		// Animer le groupe tant qu'il est rattaché à un donjon
		return (dungeon != null);
	}

	public Dungeon getDungeon() {
		return dungeon;
	}

	public Element getElement() {
		return (dungeon != null) ? dungeon.getElement(getPosition()) : null;
	}

	public void setDungeon(Dungeon dungeon) {
		// dungeon peut être null
		if ((this.dungeon != null) && (dungeon != null)) {
			// On ne peut positionner deux fois de suite le donjon
			throw new IllegalArgumentException("The dungeon is already set");
		}

		this.dungeon = dungeon;

		if (this.dungeon != null) {
			// Enregistrer le groupe
			Clock.getInstance().register(this);
		}
	}

	@Override
	public String toString() {
		return Party.class.getSimpleName() + "[position=" + position + "]";
	}

	public boolean isSleeping() {
		return sleeping;
	}

	public void sleep() {
		setSleeping(true);
	}

	public void awake() {
		setSleeping(false);
	}

	private void setSleeping(boolean sleeping) {
		final boolean wasSleeping = this.sleeping;

		this.sleeping = sleeping;

		if (!wasSleeping && sleeping) {
			// Le groupe vient de s'endormir
			if (log.isDebugEnabled()) {
				log.debug("Party has just fallen asleep");
			}

			fireChangeEvent();
		} else if (wasSleeping && !sleeping) {
			// Le groupe vient de se réveiller
			if (log.isDebugEnabled()) {
				log.debug("Party has just waken up");
			}

			fireChangeEvent();
		}
	}

	public PartySpells getSpells() {
		return spells;
	}

	/**
	 * Retourne la luminosité générée par les champions du groupe.
	 * 
	 * @return un entier positif ou null représentant la luminosité générée par
	 *         les champions dans l'intervalle [0-255].
	 */
	public int getLight() {
		int light = 0;

		// Prendre en compte la contribution de chaque champion
		for (Champion champion : getChampions(false)) {
			light += champion.getLight();
		}

		// Retourner une valeur dans l'intervalle [0-255]
		return Utils.bind(light, 0, Constants.MAX_LIGHT);
	}

	public boolean allChampionsDead() {
		boolean dead = true;

		for (Champion champion : getChampions(true)) {
			if (champion.isAlive()) {
				dead = false;
				break;
			}
		}

		return dead;
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

	protected final void fireDirectionChangeEvent() {
		final DirectionChangeEvent event = new DirectionChangeEvent(this);

		for (DirectionChangeListener listener : directionChangeListeners) {
			listener.directionChanged(event);
		}
	}

	/**
	 * Indique si le groupe est invisible (du fait d'un sort).
	 * 
	 * @return si le groupe est invisible.
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
	 * Retourne l'identifiant du dernier tic d'horloge pendant lequel le groupe
	 * a été attaqué.
	 * 
	 * @return un entier positif ou nul ou -1 si le groupe n'a jamais été
	 *         attaqué.
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
	 * Retourne l'emplacement auquel est situé le {@link Champion} donné ou null
	 * si celui-ci n'appartient pas au groupe.
	 * 
	 * @param champion
	 *            un {@link Champion} dont l'emplacement dans le groupe est
	 *            demandé.
	 * @return une instance de {@link Location} ou null.
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
			// La source est l'un des champions du groupe
			final Champion champion = (Champion) event.getSource();

			if (champion.isDead()) {
				// Le champion est mort. Sélectionner un nouveau leader s'il
				// reste des champions vivants ! FIXME Game Over
				if (getChampions(false).isEmpty()) {
					// Game over. Tous les champions sont morts
				} else {
					// Il reste au moins un champion vivant
					selectNewLeader();
				}
			}
		}
	}
}