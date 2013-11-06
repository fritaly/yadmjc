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
package fr.ritaly.dungeonmaster.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.ritaly.dungeonmaster.Clock;
import fr.ritaly.dungeonmaster.Direction;
import fr.ritaly.dungeonmaster.HasPosition;
import fr.ritaly.dungeonmaster.Position;
import fr.ritaly.dungeonmaster.Sector;
import fr.ritaly.dungeonmaster.Teleport;
import fr.ritaly.dungeonmaster.ai.Creature;
import fr.ritaly.dungeonmaster.champion.Party;
import fr.ritaly.dungeonmaster.event.ChangeEvent;
import fr.ritaly.dungeonmaster.event.ChangeEventSource;
import fr.ritaly.dungeonmaster.event.ChangeEventSupport;
import fr.ritaly.dungeonmaster.event.ChangeListener;
import fr.ritaly.dungeonmaster.item.Item;
import fr.ritaly.dungeonmaster.projectile.Projectile;

/**
 * Abstract class used for defining elements, that is, the building blocks for
 * creating levels.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
public abstract class Element implements ChangeEventSource, HasPosition {

	protected final Log log = LogFactory.getLog(this.getClass());

	/**
	 * Enumerates the possible element types.
	 *
	 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
	 */
	public static enum Type {
		/**
		 * A regular floor tile.
		 */
		FLOOR,

		/**
		 * A floor tile triggering a switch.
		 */
		FLOOR_SWITCH,

		/**
		 * A wall switch.
		 */
		WALL_SWITCH,

		/**
		 * A wall with a lock.
		 */
		WALL_LOCK,

		/**
		 * A wall with a slot (for coins).
		 */
		WALL_SLOT,

		/**
		 * A regular wall.
		 */
		WALL,

		/**
		 * A wall that can be retracted.
		 */
		RETRACTABLE_WALL,

		/**
		 * An invisible wall.
		 */
		INVISIBLE_WALL,

		/**
		 * A fake wall that can be traversed.
		 */
		FAKE_WALL,

		/**
		 * A pillar.
		 */
		PILLAR,

		/**
		 * A door.
		 */
		DOOR,

		/**
		 * A pit.
		 */
		PIT,

		/**
		 * A teleporter.
		 */
		TELEPORTER,

		/**
		 * Some stairs.
		 */
		STAIRS,

		/**
		 * A wall whose 4 sides feature an alcove.
		 */
		FOUR_SIDE_ALCOVE,

		/**
		 * A wall whose one side features an alcove.
		 */
		ALCOVE,

		/**
		 * A fontain.
		 */
		FOUNTAIN,

		/**
		 * A lever on a wall.
		 */
		LEVER,

		/**
		 * A wall with a torch holder.
		 */
		TORCH_WALL,

		/**
		 * An altar to resurrect champions.
		 */
		ALTAR,

		/**
		 * A wall with a writing.
		 */
		TEXT_WALL,

		/**
		 * A wall with a champion portrait (ro reincarnate champions).
		 */
		PORTRAIT,

		/**
		 * A decorated wall.
		 */
		DECORATED_WALL,

		/**
		 * A decorated floor tile.
		 */
		DECORATED_FLOOR,

		/**
		 * A generator of creatures.
		 */
		GENERATOR,

		/**
		 * A wall generating (item or spell) projectiles.
		 */
		PROJECTILE_LAUNCHER;

		/**
		 * Tells whether this type of element is "concrete". Concrete elements
		 * can be used as external walls to delimit a level. A regular wall is
		 * concrete but a fake or invisible wall isn't concrete.
		 *
		 * @return whether this type of element is "concrete".
		 */
		public boolean isConcrete() {
			switch (this) {
			case ALCOVE:
			case FOUNTAIN:
			case FOUR_SIDE_ALCOVE:
			case LEVER:
			case TORCH_WALL:
			case WALL:
			case WALL_LOCK:
			case WALL_SLOT:
			case WALL_SWITCH:
			case ALTAR:
			case DECORATED_WALL:
			case TEXT_WALL:
			case PORTRAIT:
			case PROJECTILE_LAUNCHER:
				return true;

			case FAKE_WALL:
			case DOOR:
			case FLOOR:
			case FLOOR_SWITCH:
			case INVISIBLE_WALL:
			case PILLAR:
			case RETRACTABLE_WALL:
			case PIT:
			case STAIRS:
			case TELEPORTER:
			case DECORATED_FLOOR:
			case GENERATOR:
				return false;

			default:
				throw new UnsupportedOperationException("Method unsupported for type " + this);
			}
		}
	}

	/**
	 * The level this element belong to.
	 */
	private Level level;

	/**
	 * The element's position inside the level.
	 */
	private Position position;

	/**
	 * The element type.
	 */
	private final Type type;

	/**
	 * The party occupying this element (if any). Populated when the party steps
	 * into the element and reset when the party steps off.
	 */
	private Party party;

	// FIXME Only instantiate a creature manager if the element can be occupied by creatures
	/**
	 * The object responsible for managing the presence of creatures on this element.
	 */
	private final CreatureManager creatureManager = new CreatureManager(this);

	/**
	 * The possible projectiles currently on this element. Can be null.
	 */
	private Map<Sector, Projectile> projectiles;

	/**
	 * The possible poison clouds currently on this element. Can be null.
	 */
	private List<PoisonCloud> poisonClouds;

	/**
	 * The possible flux cage on this element. Can be null. There can be only
	 * one flux cage per element.
	 */
	private FluxCage fluxCage;

	/**
	 * Stores the items for this element.
	 */
	private final ItemManager itemManager = new ItemManager(this);

	/**
	 * Support class used for firing change events.
	 */
	private final ChangeEventSupport eventSupport = new ChangeEventSupport();

	protected Element(Type type) {
		Validate.notNull(type, "The given type is null");

		this.type = type;
	}

	/**
	 * Drops the given into onto the given sector.
	 *
	 * @param item
	 *            the item to drop. Can't be null.
	 * @param sector
	 *            the sector where to drop the item. Can't be null.
	 */
	public void itemDropped(Item item, Sector sector) {
		itemManager.itemDropped(item, sector);
	}

	public final boolean itemPicked(Item item) {
		return pickItem(getSector(item)) == item;
	}

	/**
	 * Picks the first item (if any) at the given sector.
	 *
	 * @param sector
	 *            the sector where the item to pick is. Can't be null.
	 * @return the picked item or null if there was no item at the given
	 *         sector.
	 */
	public Item pickItem(Sector sector) {
		return itemManager.pickItem(sector);
	}

	public final Type getType() {
		return type;
	}

	public Level getLevel() {
		return level;
	}

	// FIXME Protect the call of this method with an aspect
	// This method should only be called from the Level class. However we can't
	// declare it package protected because we need to call it from the A*
	// algorithm when searching paths...
	public void setLevel(Level level) {
		// The level argument can be null (when the element is detached from its
		// parent level)
		this.level = level;
	}

	@Override
	public Position getPosition() {
		return position;
	}

	// FIXME Protect the call of this method with an aspect
	// This method should only be called from the Level class. However we can't
	// declare it package protected because we need to call it from the A*
	// algorithm when searching paths...
	public void setPosition(Position position) {
		// The position argument can be null (when the element is detached from
		// its parent level)
		this.position = position;
	}

	/**
	 * Tells whether this element can be traversed by the given party.
	 *
	 * @param party
	 *            the party of champions. Can't be null.
	 * @return whether this element can be traversed by the given party.
	 */
	public abstract boolean isTraversable(Party party);

	/**
	 * Tells whether this element can be traversed by the given creature. The
	 * returned value depends on:
	 * <ul>
	 * <li>the type of the element: a fake wall can always be traversed, a
	 * regular wall can't.</li>
	 * <li>the materiality of the creature: a ghost can traverse a regular wall,
	 * a mummy can't.</li>
	 * <li>the state of this element: a door can be traversed depending on the
	 * creature's height and its aperture (open, 3/4 open, 1/2 open, 1/4 open,
	 * closed).</li>
	 * </ul>
	 *
	 * @param creature
	 *            the creature to test. Can't be null.
	 * @return whether this element can be traversed by the given creature.
	 */
	public abstract boolean isTraversable(Creature creature);

	/**
	 * Tells whether this element can be traversed projectiles.
	 *
	 * @return whether this element can be traversed projectiles.
	 */
	public abstract boolean isTraversableByProjectile();

	/**
	 * Callback method invoked after an item has been dropped onto this element.
	 *
	 * @param item
	 *            the dropped item. Can't be null.
	 * @param sector
	 *            the sector where the item has been dropped. Can't be null.
	 */
	protected void afterItemDropped(Item item, Sector sector) {
	}

	/**
	 * Callback method invoked after an item has been picked from this element.
	 *
	 * @param item
	 *            the picked item. Can't be null.
	 * @param sector
	 *            the sector where the item has been picked. Can't be null.
	 */
	protected void afterItemPicked(Item item, Sector sector) {
	}

	// TODO This method isn't called except from tests. ???
	final void creatureSteppedOn(Creature creature, Sector sector) {
		creatureManager.creatureSteppedOn(creature, sector);
	}

	public final void projectileArrived(Projectile projectile, Sector sector) {
		Validate.notNull(projectile, "The given projectile is null");
		Validate.notNull(sector, "The given sector is null");

		if (!isTraversableByProjectile() && !Type.DOOR.equals(getType())) {
			// Une porte peut accueillir un projectile m�me si celle-ci est
			// ferm�e afin qu'il puisse exploser
			throw new UnsupportedOperationException(
					"The projectile can't arrive on " + getId());
		}

		if (log.isDebugEnabled()) {
			log.debug(projectile.getId() + " arrived on " + getId()
					+ " (sector: " + sector + ")");
		}

		if (projectiles == null) {
			// Cr�er la Map � la vol�e
			projectiles = new EnumMap<Sector, Projectile>(Sector.class);
		}

		// L'emplacement doit initialement �tre vide
		if (projectiles.get(sector) != null) {
			throw new IllegalArgumentException("The cell " + sector
					+ " of element " + getId()
					+ " is already occupied by a projectile ("
					+ projectiles.get(sector) + ")");
		}

		// M�moriser le projectile
		projectiles.put(sector, projectile);

		afterProjectileArrived(projectile);
	}

	public final void projectileLeft(Projectile projectile, Sector sector) {
		if (projectile == null) {
			throw new IllegalArgumentException("The given projectile is null");
		}
		if (sector == null) {
			throw new IllegalArgumentException("The given sector is null");
		}
		if (!isTraversableByProjectile() && !Type.DOOR.equals(getType())) {
			// Une porte peut accueillir un projectile m�me si celle-ci est
			// ferm�e afin qu'il puisse exploser
			throw new UnsupportedOperationException("The projectile "
					+ projectile.getId() + " can't leave " + getId());
		}

		if (log.isDebugEnabled()) {
			log.debug(projectile.getId() + " left " + getId() + " (sector: "
					+ sector + ")");
		}

		final Projectile removed = projectiles.remove(sector);

		if (removed != projectile) {
			throw new IllegalArgumentException("Removed: " + removed
					+ " / Projectile: " + projectile + " / Sector: " + sector);
		}

		if (projectiles.isEmpty()) {
			// Purger la Map � la vol�e
			projectiles = null;
		}

		afterProjectileLeft(projectile);
	}

	protected void afterProjectileLeft(Projectile projectile) {
	}

	protected void afterProjectileArrived(Projectile projectile) {
	}

	protected void afterCreatureSteppedOn(Creature creature) {
	}

	protected void afterCreatureSteppedOff(Creature creature) {
	}

	public final Sector getSector(Creature creature) {
		return creatureManager.getSector(creature);
	}

	public final Sector getSector(Item item) {
		return itemManager.getSector(item);
	}

	public final void creatureSteppedOff(Creature creature, Sector sector) {
		creatureManager.creatureSteppedOff(creature, sector);
	}

	/**
	 * Notifie l'�l�ment que le groupe de champions vient d'arriver sur sa
	 * position.
	 *
	 * @param party
	 *            une instance de {@link Party} repr�sentant le groupe de
	 *            champions.
	 */
	public final void partySteppedOn(Party party) {
		if (party == null) {
			throw new IllegalArgumentException("The given party is null");
		}
		if (!isTraversable(party)) {
			throw new UnsupportedOperationException(
					"The party can't step on element " + getId());
		}

		if (log.isDebugEnabled()) {
			log.debug("Party stepped on " + getId());
		}

		// M�moriser la r�f�rence
		this.party = party;

		afterPartySteppedOn();
	}

	protected void afterPartySteppedOn() {
	}

	protected void afterPartySteppedOff(Party party) {
	}

	/**
	 * Notifie l'�l�ment que le groupe de champions vient de tourner sur place.
	 * Note: Cette m�thode permet � un �lement de type STAIRS de d�placer le
	 * groupe de champions quand celui-ci tourne sur lui-m�me.
	 */
	public void partyTurned() {
		if (party == null) {
			throw new IllegalStateException("The party isn't on " + getId());
		}

		if (log.isDebugEnabled()) {
			log.debug("Party turned on " + getId());
		}
	}

	/**
	 * Notifie l'�l�ment que le groupe de champions vient de quitter sa
	 * position.
	 */
	public final void partySteppedOff() {
		if (this.party == null) {
			throw new IllegalStateException("The party isn't located on this "
					+ getId());
		}
		if (!isTraversable(party)) {
			throw new UnsupportedOperationException(
					"The party can't step off element " + type);
		}

		// R�initialiser la r�f�rence
		final Party backup = this.party;
		this.party = null;

		if (log.isDebugEnabled()) {
			log.debug("Party stepped off " + getId());
		}

		afterPartySteppedOff(backup);
	}

	/**
	 * Indique si le groupe de champions occupe cette position.
	 *
	 * @return si le groupe de champions occupe cette position.
	 */
	public boolean hasParty() {
		return (party != null);
	}

	/**
	 * Indique si l'�l�ment est occup� par au moins une cr�ature.
	 *
	 * @return si l'�l�ment est occup� par au moins une cr�ature.
	 */
	public boolean hasCreatures() {
		return creatureManager.hasCreatures();
	}

	/**
	 * Indique si l'�l�ment est occup� par au moins un projectile.
	 *
	 * @return si l'�l�ment est occup� par au moins un projectile.
	 */
	public boolean hasProjectiles() {
		return (projectiles != null) && !projectiles.isEmpty();
	}

	/**
	 * Indique si l'�l�ment est vide, c'est-�-dire non occup� par des cr�atures,
	 * par le groupe de champions ou tout autre chose qui emp�cherait de s'y
	 * placer.
	 *
	 * @return si l'�l�ment est vide.
	 */
	public boolean isEmpty() {
		return !hasParty() && !hasCreatures();
	}

	@Override
	public final String toString() {
		if (position != null) {
			return this.type.name() + position;
		} else {
			return this.type.name() + "[?:?,?]";
		}
	}

	/**
	 * Retourne le groupe de champions s'il occupe cette position ou null.
	 *
	 * @return une instance de {@link Party} ou null.
	 */
	public final Party getParty() {
		return party;
	}

	/**
	 * Retourne les cr�atures occupant cet �l�ment sous forme de Map.
	 *
	 * @return une Map&lt;Sector, Creature&gt. Cette m�thode ne retourne jamais
	 *         null.
	 */
	public final Map<Sector, Creature> getCreatureMap() {
		// Ne pas utiliser en dehors des tests unitaires (acc�s trop bas niveau)
		// Utiliser getCreatures() � la place
		return creatureManager.getCreatureMap();
	}

	/**
	 * Retourne les cr�atures occupant cet �l�ment sous forme de {@link List}.
	 *
	 * @return une Set&lt;Creature&gt. Cette m�thode ne retourne jamais null.
	 */
	public final Set<Creature> getCreatures() {
		return creatureManager.getCreatures();
	}

	public final Map<Sector, Projectile> getProjectiles() {
		if (projectiles == null) {
			return Collections.emptyMap();
		}

		// Recopie d�fensive
		return Collections.unmodifiableMap(projectiles);
	}

	/**
	 * Retourne la cr�ature occupant l'emplacement donn� s'il y a lieu.
	 *
	 * @param sector
	 *            l'emplacement sur lequel rechercher la cr�ature.
	 * @return une instance de {@link Creature} ou null s'il n'y en a aucune �
	 *         cet emplacement.
	 */
	public final Creature getCreature(Sector sector) {
		return creatureManager.getCreature(sector);
	}

	@Override
	public final void addChangeListener(ChangeListener listener) {
		eventSupport.addChangeListener(listener);
	}

	@Override
	public final void removeChangeListener(ChangeListener listener) {
		eventSupport.addChangeListener(listener);
	}

	protected final void fireChangeEvent() {
		eventSupport.fireChangeEvent(new ChangeEvent(this));
	}

	/**
	 * Indique si l'�l�ment est "en dur". C'est le cas d'un mur au send large
	 * (mur simple, mur d�cor�) mais pas d'un mur invisible ou d'un faux mur.
	 * Permet de d�terminer si un �l�ment peut �tre utilis� en bordure de
	 * niveau.
	 *
	 * @return si l'�l�ment est "en dur".
	 */
	public final boolean isConcrete() {
		return type.isConcrete();
	}

	/**
	 * Retourne l'identifiant de cet �l�ment sous forme de {@link String}.
	 *
	 * @return un {@link String} identifiant cet �l�ment.
	 */
	public abstract String getCaption();

	public final String getId() {
		if (position != null) {
			return this.type.name() + position;
		} else {
			return this.type.name() + "[?:?,?]";
		}
	}

	/**
	 * Retourne tous les objets au sol sur cet �l�ment.
	 *
	 * @return une List&lt;Item&gt;. Cette m�thode ne retourne jamais null.
	 */
	public final List<Item> getItems() {
		return itemManager.getItems();
	}

	public final int getItemCount() {
		return itemManager.getItemCount();
	}

	public final int getCreatureCount() {
		return creatureManager.getCreatureCount();
	}

	public final int getItemCount(Sector sector) {
		return itemManager.getItemCount(sector);
	}

	/**
	 * Retourne les objets situ�s � l'emplacement donn� s'il y a lieu.
	 *
	 * @param sector
	 *            l'emplacement o� sont situ�s les objets recherch�s.
	 * @return une List&lt;Item&gt; contenant les objets trouv�s. Cette m�thode
	 *         ne retourne jamais null.
	 */
	public List<Item> getItems(Sector sector) {
		return itemManager.getItems(sector);
	}

	/**
	 * Indique si l'�l�ment comporte des objets.
	 *
	 * @return si l'�l�ment comporte des objets.
	 */
	public boolean hasItems() {
		return itemManager.hasItems();
	}

	/**
	 * Calcule et retourne une instance de {@link Teleport} indiquant comment
	 * d�placer un groupe de champions se d�pla�ant dans la direction donn�e.
	 * Dans la majorit� des cas, le groupe se retrouvera sur l'�l�ment situ�
	 * dans la direction donn�e mais pour certains �l�ments (t�l�porteurs,
	 * escaliers) le d�placement du groupe n'est pas aussi simple.
	 *
	 * @param direction
	 *            la {@link Direction} dans laquelle se d�place le groupe de
	 *            champions.
	 * @return une instance de {@link Teleport} indiquant comment d�placer le
	 *         groupe de champions.
	 */
	public Teleport getTeleport(Direction direction) {
		Validate.notNull(direction, "The given direction is null");

		if (!hasParty()) {
			throw new IllegalStateException("The party isn't on this element");
		}

		// Dans le cas g�n�ral, l'�l�ment ne modifie pas la position finale
		return new Teleport(getPosition().towards(direction), getParty()
				.getLookDirection());
	}

	// /**
	// * Notifie l'�l�ment que le groupe de champions qui l'occupe est sur le
	// * point de bouger dans la direction donn�e et retourne la position finale
	// * du groupe. Cette m�thode est sp�cialement con�ue pour la classe
	// * {@link Stairs} qui a un mode de fonctionnement un peu particulier.
	// *
	// * @param direction
	// * la {@link Direction} de d�placement du groupe.
	// * @return la {@link Position} finale apr�s d�placement du groupe.
	// */
	// public Position computeTargetPosition(Direction direction) {
	// Validate.notNull(direction, "The given direction is null");
	// if (!hasParty()) {
	// throw new IllegalStateException("The party isn't on this element");
	// }
	//
	// // Dans le cas g�n�ral, l'�l�ment ne modifie pas la position finale
	// return getPosition().towards(direction);
	// }
	//
	// public Direction computeTargetDirection(Direction direction) {
	// Validate.notNull(direction, "The given direction is null");
	// if (!hasParty()) {
	// throw new IllegalStateException("The party isn't on this element");
	// }
	//
	// // Dans le cas g�n�ral, l'�l�ment ne modifie pas la direction du groupe
	// return getParty().getLookDirection();
	// }

	/**
	 * Calcule et retourne la place libre restante pour accueillir de nouvelles
	 * {@link Creature}s sous forme d'un entier (repr�sentant un nombre de
	 * {@link Sector}s).
	 *
	 * @return un entier dans l'intervalle [0-4] repr�sentant le nombre de
	 *         {@link Sector}s libres.
	 */
	public int getFreeRoom() {
		return creatureManager.getFreeRoom();
	}

	/**
	 * Retourne les {@link Sector}s occup�es par les {@link Creature}s
	 * pr�sentes sur cet {@link Element}.
	 *
	 * @return un EnumSet&lt;Sector&gt;. Ne retourne jamais null.
	 */
	public EnumSet<Sector> getOccupiedSectors() {
		return creatureManager.getOccupiedSectors();
	}

	/**
	 * Retourne les {@link Sector}s libres de cet {@link Element}.
	 *
	 * @return un EnumSet&lt;Sector&gt;. Ne retourne jamais null.
	 */
	public EnumSet<Sector> getFreeSectors() {
		return creatureManager.getFreeSectors();
	}

	/**
	 * Indique si cet {@link Element} peut accueillir la {@link Creature} donn�e
	 * compte tenu de sa taille et de la place restante.
	 *
	 * @param creature
	 *            une {@link Creature}.
	 * @return si cet {@link Element} peut accueillir la {@link Creature} donn�e
	 *         compte tenu de sa taille et de la place restante.
	 */
	public boolean canHost(Creature creature) {
		Validate.notNull(creature);

		final int room = getFreeRoom();
		final int creatureSize = creature.getSize().value();

		if (creatureSize > room) {
			// Plus assez de place pour accueillir la cr�ature
			return false;
		}

		// Dans le cas o� la place restante est de 2 et la taille de la cr�ature
		// �galement de 2, il faut s'assurer qu'il s'agit de Sector voisines
		// qui permettent r�ellement d'accueillir la cr�ature !
		if ((room == 2) && (creatureSize == 2)) {
			final Iterator<Sector> iterator = getFreeSectors().iterator();

			final Sector cell1 = iterator.next();
			final Sector cell2 = iterator.next();

			if (!cell1.isNeighbourOf(cell2)) {
				return false;
			}
		}

		return true;
	}

	public abstract void validate() throws ValidationException;

	// FIXME Cr�er m�thode Element.setVisited(boolean) pour magic footprints

	protected final Position getPartyPosition() {
		final Level level = getLevel();

		if (level != null) {
			final Dungeon dungeon = level.getDungeon();

			if (dungeon != null) {
				final Party party = dungeon.getParty();

				if (party != null) {
					return party.getPosition();
				}
			}
		}

		return null;
	}

	public final void creatureSteppedOn(Creature creature) {
		creatureManager.creatureSteppedOn(creature);
	}

	public boolean hasCreature(Creature creature) {
		return creatureManager.hasCreature(creature);
	}

	public final void creatureSteppedOff(Creature creature) {
		creatureManager.creatureSteppedOff(creature);
	}

	public final void creatureSteppedOn(Creature creature, Direction direction) {
		creatureManager.creatureSteppedOn(creature, direction);
	}

	public final void creatureSteppedOff(Creature creature, Direction direction) {
		creatureManager.creatureSteppedOff(creature, direction);
	}

	protected final CreatureManager getCreatureManager() {
		return creatureManager;
	}

	public Object removeCreature(Creature creature) {
		return creatureManager.removeCreature(creature);
	}

	public void addCreature(Creature creature, Object location) {
		creatureManager.addCreature(creature, location);
	}

	public void addCreature(Creature creature) {
		creatureManager.addCreature(creature);
	}

	public boolean hasPoisonClouds() {
		return (poisonClouds != null) && !poisonClouds.isEmpty();
	}

	public int getPoisonCloudCount() {
		if (poisonClouds != null) {
			return poisonClouds.size();
		}

		return 0;
	}

	// TODO Prendre en compte la force du nuage de poison en param�tre
	public void createPoisonCloud() {
		if (this.poisonClouds == null) {
			this.poisonClouds = new ArrayList<PoisonCloud>();
		}

//		if (log.isDebugEnabled()) {
//			log.debug("Creating new poison cloud on " + this + " ...");
//		}

		final PoisonCloud poisonCloud = new PoisonCloud(this);

		// S'enregistrer pour savoir quand le nuage dispara�t
		poisonCloud.addChangeListener(new ChangeListener() {
			@Override
			public void onChangeEvent(ChangeEvent event) {
				if (log.isDebugEnabled()) {
					log.debug(event.getSource() + " vanished into thin air");
				}

				poisonClouds.remove(event.getSource());

				if (poisonClouds.isEmpty()) {
					poisonClouds = null;
				}
			}
		});

		// M�moriser le nuage
		this.poisonClouds.add(poisonCloud);

		if (log.isDebugEnabled()) {
			log.debug("Created a new poison cloud on " + this);
		}

		// Enregistrer ce nuage
		Clock.getInstance().register(poisonCloud);
	}

	public boolean hasFluxCage() {
		return (fluxCage != null);
	}

	public void createFluxCage() {
		// On ne peut cr�er une cage s'il y en a d�j� une en place
		if (hasFluxCage()) {
			// TODO G�rer le cas d'une seconde cage qui renforce la premi�re ?
			throw new IllegalStateException("There is already a flux cage on "
					+ this);
		}

//		if (log.isDebugEnabled()) {
//			log.debug("Creating new flux cage on " + this + " ...");
//		}

		final FluxCage fluxCage = new FluxCage(this);

		// S'enregistrer pour savoir quand la cage dispara�t
		fluxCage.addChangeListener(new ChangeListener() {
			@Override
			public void onChangeEvent(ChangeEvent event) {
				if (log.isDebugEnabled()) {
					log.debug(event.getSource() + " vanished into thin air");
				}

				Element.this.fluxCage = null;
			}
		});

		// M�moriser la cage
		this.fluxCage = fluxCage;

		if (log.isDebugEnabled()) {
			log.debug("Created a flux cage on " + this);
		}

		// Enregistrer la cage
		Clock.getInstance().register(fluxCage);
	}

	public List<Element> getSurroundingElements() {
		final List<Element> elements = new ArrayList<Element>();

		for (Position position : getPosition().getSurroundingPositions()) {
			if (!getLevel().contains(position)) {
				// Position situ�e en dehors des limites du niveau
				continue;
			}

			elements.add(getLevel().getElement(position.x, position.y));
		}

		return elements;
	}

	public List<Element> getReachableElements() {
		// 4 positions atteignables au mieux
		final List<Element> elements = new ArrayList<Element>(4);

		for (Position position : getPosition().getAttackablePositions()) {
			if (!getLevel().contains(position)) {
				// Position situ�e en dehors des limites du niveau
				continue;
			}

			elements.add(getLevel().getElement(position.x, position.y));
		}

		return elements;
	}
}