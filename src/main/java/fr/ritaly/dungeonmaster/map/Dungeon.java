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
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.ritaly.dungeonmaster.Clock;
import fr.ritaly.dungeonmaster.ClockListener;
import fr.ritaly.dungeonmaster.DeferredCommand;
import fr.ritaly.dungeonmaster.Direction;
import fr.ritaly.dungeonmaster.Move;
import fr.ritaly.dungeonmaster.Position;
import fr.ritaly.dungeonmaster.Side;
import fr.ritaly.dungeonmaster.Teleport;
import fr.ritaly.dungeonmaster.Utils;
import fr.ritaly.dungeonmaster.audio.AudioClip;
import fr.ritaly.dungeonmaster.audio.SoundSystem;
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.Party;

/**
 * A dungeon. A {@link Dungeon} is made of one to several {@link Level}s.
 * 
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public class Dungeon implements ClockListener {

	/**
	 * Creates a new empty dungeon.
	 */
	public Dungeon() {
		Clock.getInstance().register(this);
	}

	private final Log log = LogFactory.getLog(Dungeon.class);

	/**
	 * The dungeon levels stored by level number.
	 */
	private final Map<Integer, Level> levels = new HashMap<Integer, Level>();

	/**
	 * The {@link Party} of champions inside the dungeon.
	 */
	private Party party;

	private final LinkedList<DeferredCommand> partyMoves = new LinkedList<DeferredCommand>();

	/**
	 * Returns the number of levels composing this dungeon.
	 * 
	 * @return an int.
	 */
	public int getLevelCount() {
		return levels.size();
	}

	/**
	 * Returns the {@link Level} where the party is currently located or null if
	 * there is no defined party.
	 * 
	 * @return a {@link Level} or null.
	 */
	public Level getCurrentLevel() {
		if (hasParty()) {
			return getLevel(getParty().getPosition().z);
		}

		return null;
	}

	/**
	 * Returns the dungeon's levels.
	 * 
	 * @return a {@link List} of {@link Level}s. Never returns null.
	 */
	public List<Level> getLevels() {
		final ArrayList<Level> list = new ArrayList<Level>(levels.values());

		Collections.sort(list, new Comparator<Level>() {
			@Override
			public int compare(Level level1, Level level2) {
				return level1.getLevel() - level2.getLevel();
			}
		});

		return list;
	}

	/**
	 * Returns the dungeon element where the party is currently located or null
	 * if there is no defined party.
	 * 
	 * @return an {@link Element} or null.
	 */
	public Element getCurrentElement() {
		if (hasParty()) {
			return getElement(getParty().getPosition());
		}

		return null;
	}

	/**
	 * Returns the {@link Level} with given number.
	 * 
	 * @param level
	 *            an int identifying the requested {@link Level}.
	 * @return a {@link Level} or null.
	 */
	public Level getLevel(int level) {
		Validate.isTrue((level >= 0), "The given level number " + level
				+ " must be positive or zero");

		return levels.get(level);
	}

	/**
	 * Returns the {@link Party} inside this dungeon (if any).
	 * 
	 * @return a {@link Party} or null.
	 */
	public Party getParty() {
		return party;
	}

	/**
	 * Tells whether there is a {@link Party} inside this dungeon.
	 * 
	 * @return whether there is a {@link Party} inside this dungeon.
	 */
	public boolean hasParty() {
		return (party != null);
	}

	/**
	 * Sets this dungeon's party and installs it to the given {@link Position}.
	 * 
	 * @param position
	 *            a {@link Position} where the {@link Party} will be installed.
	 * @param party
	 *            the {@link Party} to install.
	 */
	public void setParty(Position position, Party party) {
		Validate.notNull(position, "The given position is null");
		Validate.notNull(party, "The given party is null");

		setParty(position.x, position.y, position.z, party);
	}

	public void setParty(int x, int y, int z, Party party) {
		if (this.party != null) {
			throw new IllegalStateException("There is already a party set");
		}
		Validate.notNull(party, "The given party is null");

		final Position position = new Position(x, y, z);

		if (log.isDebugEnabled()) {
			log.debug("Installing party at " + position + " ...");
		}

		// Récupérer l'élément en premier (permet de valider le triplet (x,y,z))
		final Element element = getElement(x, y, z);

		// Vérifier que le groupe peut s'y placer !
		if (!element.isTraversable(party)) {
			throw new IllegalArgumentException("The " + element.getType()
					+ " element at " + position
					+ " can't be occupied by the party");
		}

		this.party = party;
		this.party.setPosition(position);
		this.party.setDungeon(this);

		// Initialiser le listener audio
		SoundSystem.getInstance().setListener(party);

		// "Placer" le groupe sur l'endroit cible (le faire marcher dessus)
		element.partySteppedOn(party);

		if (log.isInfoEnabled()) {
			log.info("Party installed at " + position);
		}
	}

	/**
	 * Returns the dungeon element with given position.
	 * 
	 * @param position
	 *            the {@link Position} of the requested element. Can't be null.
	 * @return an {@link Element} or null.
	 */
	public Element getElement(Position position) {
		Validate.notNull(position, "The given position is null");

		return getElement(position.x, position.y, position.z);
	}

	public void setElement(int x, int y, int z, Element element) {
		Validate.notNull(element, "The given element is null");

		final Level level = getLevel(z);

		if (level == null) {
			throw new IllegalArgumentException("There is no level <" + z + ">");
		}

		level.setElement(x, y, element);
	}

	public void setElement(Position position, Element element) {
		Validate.notNull(position, "The given position is null");
		Validate.notNull(element, "The given element is null");

		setElement(position.x, position.y, position.z, element);
	}

	public Element getElement(int x, int y, int z) {
		final Level level = getLevel(z);

		if (level == null) {
			throw new IllegalArgumentException("There is no level <" + z + ">");
		}

		// Rechercher l'élément cible
		final Element element = level.getElement(x, y);

		if (element == null) {
			throw new IllegalArgumentException("There is no element at [" + z
					+ ":" + x + "," + y + "]");
		}

		return element;
	}

	public Level createLevel(int number, int height, int width) {
		Validate.isTrue(number > 0, "The level number <" + number
				+ "> must be positive");
		Validate.isTrue(height > 0, "The given height <" + height
				+ "> must be positive");
		Validate.isTrue(width > 0, "The given width <" + width
				+ "> must be positive");

		if (levels.containsKey(number)) {
			throw new IllegalArgumentException(
					"There is already a level with number <" + number + ">");
		}

		final Level level = new Level(this, number, height, width);

		levels.put(number, level);

		return level;
	}

	public void setLevel(int number, Level level) {
		Validate.isTrue(number > 0, "The level number <" + number
				+ "> must be positive");
		Validate.notNull(level, "The given level is null");

		levels.put(number, level);
	}

	@Override
	public boolean clockTicked() {
		if (!partyMoves.isEmpty()) {
			final DeferredCommand command = partyMoves.getFirst();

			if (!command.clockTicked()) {
				// L'action s'est déclenchée. La supprimer l'entrée de la liste
				partyMoves.removeFirst();
			}
		}

		return true;
	}

	public boolean moveParty(final Move move, boolean now, final AudioClip clip) {
		// Le clip ne peut être nul si on appelle cette méthode
		Validate.notNull(clip, "The given clip is null");

		return doMoveParty(move, now, clip);
	}

	public boolean moveParty(final Move move, boolean now) {
		// Déplacer sans jouer de son
		return doMoveParty(move, now, null);
	}

	private boolean doMoveParty(final Move move, boolean now,
			final AudioClip clip) {

		// Le paramètre clip peut être null ou non selon la méthode appelante
		Validate.notNull(move, "The given move is null");
		if (party == null) {
			throw new IllegalStateException("The party isn't defined");
		}

		if (now) {
			return movePartyNow(move, clip);
		} else {
			// Mettre en file d'attente la commande
			final int delay = party.getMoveSpeed().getValue();

			partyMoves.add(new DeferredCommand("Dungeon.PartyMover", delay) {

				@Override
				protected void run() {
					if (log.isDebugEnabled()) {
						log.debug("Running delayed command. Moving party ...");
					}

					movePartyNow(move, clip);

					if (log.isDebugEnabled()) {
						log.debug("Ran delayed command");
					}
				}
			});

			if (log.isDebugEnabled()) {
				log.debug("Queued party move <" + move + "> [delay=" + delay
						+ " ticks]");
			}

			return true;
		}
	}

	private boolean movePartyNow(Move move, AudioClip clip) {
		// Le paramètre clip peut être null ou non selon la méthode appelante
		Validate.notNull(move, "The given move is null");
		if (this.party == null) {
			throw new IllegalStateException("There is no party set");
		}

		// Note: Un déplacement ne peut modifier la direction de regard et la
		// position en même temps (seule une téléportation le peut)

		if (log.isDebugEnabled()) {
			log.debug("Moving party [" + move + "] ...");
		}

		if (!move.changesPosition()) {
			// Pas de changement de position, c'est sa direction de regard qui
			// change
			party.turn(move);

			if (log.isInfoEnabled()) {
				log.info("Party remains still");
			}

			final Element element = getElement(getParty().getPosition());

			// Notification (pour les escaliers entre autres)
			element.partyTurned();

			return true;
		}

		// Le groupe va changer de position

		// Emplacement initial ?
		final Element sourceElement = getElement(party.getPosition());

		if (log.isDebugEnabled()) {
			log.debug("Source element = " + sourceElement);
		}

		// Direction de déplacement ?
		final Direction moveDirection = move.getMoveDirection(party
				.getLookDirection());

		if (log.isDebugEnabled()) {
			log.debug("Move direction = " + moveDirection);
		}

		// Position finale ? Le demander à l'élément source (pour les escaliers
		// entre autres)
		final Teleport teleport = sourceElement.getTeleport(moveDirection);

		if (log.isDebugEnabled()) {
			log.debug("Teleport = " + teleport);
		}

		// Emplacement final ?
		final Element destinationElement = getElement(teleport.getPosition());

		if (log.isDebugEnabled()) {
			log.debug("Destination element = " + destinationElement);
		}

		// Position traversable ?
		if (!destinationElement.isTraversable(party)) {
			// Le groupe se cogne dans le mur

			SoundSystem.getInstance().play(AudioClip.BONG);

			// Appliquer des dégâts aux champions. Champions concernés ?
			final Set<Champion> champions;

			switch (move) {
			case FORWARD:
				champions = getParty().getChampions(Side.FRONT, false);
				break;
			case BACKWARD:
				champions = getParty().getChampions(Side.REAR, false);
				break;
			case LEFT:
				champions = getParty().getChampions(Side.LEFT, false);
				break;
			case RIGHT:
				champions = getParty().getChampions(Side.RIGHT, false);
				break;
			default:
				// Dans les autres cas, les champions ne peuvent se blesser !!
				champions = null;
				break;
			}

			if (champions != null) {
				for (Champion champion : champions) {
					// Blesser chaque champion
					champion.hit(Utils.random(10, 30));
				}
			}

			if (log.isInfoEnabled()) {
				log.info("Party bumped into " + destinationElement.getType());
			}

			return false;
		}

		// Position occupée ?
		if (!destinationElement.isEmpty()) {
			// Oui, le groupe ne peut bouger
			if (log.isInfoEnabled()) {
				log.info("Party can't move since destination isn't free");
			}

			return false;
		}

		// Quitter la position actuelle
		sourceElement.partySteppedOff();

		// Changer la direction du groupe (si nécessaire)
		getParty().setLookDirection(teleport.getDirection());

		// Déplacement du groupe
		party.setPosition(teleport.getPosition());

		// Jouer le son demandé
		SoundSystem.getInstance().play(clip);

		// Occuper la position cible
		destinationElement.partySteppedOn(party);

		if (log.isInfoEnabled()) {
			log.info("Party moved");
		}

		return true;
	}

	public boolean teleportParty(final Teleport teleport, final boolean silent) {
		Validate.notNull(teleport, "The given teleport is null");

		return teleportParty(teleport.getPosition(), teleport.getDirection(),
				silent);
	}

	public boolean teleportParty(final Position destination,
			final Direction direction, final boolean silent) {

		Validate.notNull(destination, "The given destination is null");
		Validate.notNull(direction, "The given direction is null");

		if (this.party == null) {
			throw new IllegalStateException("There is no party set");
		}
		if (destination.equals(this.party.getPosition())) {
			throw new IllegalArgumentException(
					"The party is already at the given destication "
							+ destination);
		}

		if (log.isDebugEnabled()) {
			log.debug("Teleporting party to " + destination + " ...");
		}

		// Le groupe va changer de position

		// Emplacement initial ?
		final Element sourceElement = getElement(party.getPosition());

		if (log.isDebugEnabled()) {
			// log.debug("Source position = " + party.getPosition());
			log.debug("Source element = " + sourceElement.getId());
		}

		// Emplacement final ?
		final Element destinationElement = getElement(destination);

		if (log.isDebugEnabled()) {
			log.debug("Destination element = " + destinationElement.getId());
		}

		// Position traversable ?
		if (!destinationElement.isTraversable(party)) {
			// Ne doit pas arriver, c'est qu'il y a un défaut de conception du
			// niveau
			throw new IllegalArgumentException("The given destination "
					+ destination + " can't be traversed by the party");
		}

		// Position occupée ?
		if (!destinationElement.isEmpty()) {
			// TODO Tuer les monstre sur la destination ?

			// Oui, le groupe ne peut bouger
			if (log.isInfoEnabled()) {
				log.info("Party can't move since destination " + destination
						+ " isn't free");
			}

			return false;
		}

		// Quitter la position actuelle
		sourceElement.partySteppedOff();

		// Faire tourner le groupe
		party.setLookDirection(direction);

		// Déplacement du groupe
		party.setPosition(destination);

		if (!silent) {
			// Jouer le son demandé
			SoundSystem.getInstance().play(AudioClip.TELEPORT);
		}

		// Occuper la position cible
		destinationElement.partySteppedOn(party);

		if (log.isInfoEnabled()) {
			log.info("Teleported party");
		}

		return true;
	}

	public void validate() throws ValidationException {
		for (Level level : levels.values()) {
			level.validate();
		}
	}

	/**
	 * Retourne la luminosité totale en prennant en compte celle générée par les
	 * {@link Champion}s (amulettes, torches, sorts...) et celle naturelle du
	 * niveau sur lequel sont situés les {@link Champion}s.
	 * 
	 * @return un entier positif ou nul dans l'intervalle [0-255].
	 */
	public int getActualLight() {
		// Il vaut mieux que cette méthode soit sur la classe Dungeon (seule
		// cette classe sait sur quel niveau se situent les champions)
		int light = 0;

		// Contribution des champions ?
		final Party party = getParty();

		if (party != null) {
			light += party.getLight();
		}

		// Lumière naturelle du niveau ?
		light += getLevel(party.getPosition().z).getAmbiantLight();
		
		// TODO Prendre en compte les sorts de type Darkness !!

		// TODO 7 niveaux de lumière possibles (enum)
		return Utils.bind(light, 0, 255);
	}
}