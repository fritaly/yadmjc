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

import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javanet.staxutils.IndentingXMLStreamWriter;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import fr.ritaly.dungeonmaster.Skill;
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.Champion.Level;
import fr.ritaly.dungeonmaster.champion.body.BodyPart;
import fr.ritaly.dungeonmaster.item.Item.AffectedStatistic;
import fr.ritaly.dungeonmaster.item.Item.Type;
import fr.ritaly.dungeonmaster.stat.Stats;

/**
 * A definition of item.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
final class ItemDef {

	/**
	 * SAX handler to parse the resource file 'items.xml' defining items.
	 *
	 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
	 */
	private static final class ItemDefParser extends DefaultHandler {

		private final Log log = LogFactory.getLog(this.getClass());

		private final List<ItemDef> definitions = new ArrayList<ItemDef>();

		private ItemDef definition;

		private ItemDefParser() {
		}

		@Override
		public void startDocument() throws SAXException {
			this.definitions.clear();

			if (log.isDebugEnabled()) {
				log.debug("Parsing item definitions ...");
			}
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			final String elementName = qName;

			if ("items".equals(elementName) || "locations".equals(elementName) || "actions".equals(elementName) || "effects".equals(elementName)) {
				// Do nothing
			} else if ("item".equals(elementName)) {
				this.definition = new ItemDef();
				this.definition.id = attributes.getValue("id");
				this.definition.weight = Float.parseFloat(attributes.getValue("weight"));

				if (attributes.getValue("damage") != null) {
					this.definition.damage = Integer.parseInt(attributes.getValue("damage"));
				}
				if (attributes.getValue("activation") != null) {
					this.definition.activationBodyPart = BodyPart.Type.valueOf(attributes.getValue("activation"));
				}
				if (attributes.getValue("shield") != null) {
					this.definition.shield = Integer.parseInt(attributes.getValue("shield"));
				}
				if (attributes.getValue("anti-magic") != null) {
					this.definition.antiMagic = Integer.parseInt(attributes.getValue("anti-magic"));
				}
				if (attributes.getValue("decay-rate") != null) {
					this.definition.decayRate = Integer.parseInt(attributes.getValue("decay-rate"));
				}
				if (attributes.getValue("distance") != null) {
					this.definition.distance = Integer.parseInt(attributes.getValue("distance"));
				}
				if (attributes.getValue("shoot-damage") != null) {
					this.definition.shootDamage = Integer.parseInt(attributes.getValue("shoot-damage"));
				}
			} else if ("location".equals(elementName)) {
				this.definition.carryLocations.add(CarryLocation.valueOf(attributes.getValue("id")));
			} else if ("action".equals(elementName)) {
				final ActionDef actionDef = new ActionDef();
				actionDef.action = Action.valueOf(attributes.getValue("id"));

				if (attributes.getValue("min-level") != null) {
					actionDef.minLevel = Champion.Level.valueOf(attributes.getValue("min-level"));
				}
				if (attributes.getValue("use-charges") != null) {
					actionDef.useCharges = Boolean.valueOf(attributes.getValue("use-charges"));
				}

				this.definition.actions.add(actionDef);
			} else if ("effect".equals(elementName)) {
				final Effect effect = new Effect();
				effect.statistic = AffectedStatistic.valueOf(attributes.getValue("stat"));
				effect.strength = Integer.valueOf(attributes.getValue("strength"));

				this.definition.effects.add(effect);
			} else {
				throw new SAXException(String.format("Unexpected element name '%s'", elementName));
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			final String elementName = qName;

			if ("items".equals(elementName) || "locations".equals(elementName) || "actions".equals(elementName) || "effects".equals(elementName)) {
				// Do nothing
			} else if ("item".equals(elementName)) {
				this.definitions.add(definition);

				this.definition = null;
			} else if ("location".equals(elementName)) {
				// Do nothing
			} else if ("action".equals(elementName)) {
				// Do nothing
			} else if ("effect".equals(elementName)) {
				// Do nothing
			} else {
				throw new SAXException(String.format("Unexpected element name '%s'", elementName));
			}
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
		}

		@Override
		public void endDocument() throws SAXException {
			if (log.isInfoEnabled()) {
				log.info(String.format("Parsed %d item definitions", definitions.size()));
			}
		}
	}

	/**
	 * An item effect can provide a bonus or a malus to a champion's stat.
	 *
	 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
	 */
	public static final class Effect {

		/**
		 * The statistic affected by this effect.
		 */
		private AffectedStatistic statistic;

		/**
		 * The strength of this effect.
		 */
		private int strength;

		private Effect() {
		}

		/**
		 * Returns the statistic affected by this effect.
		 *
		 * @return the affected statistic. Never returns null.
		 */
		public AffectedStatistic getStatistic() {
			return statistic;
		}

		/**
		 * Returns the strength of this effect as an integer.
		 *
		 * @return an integer value representing the effect's strength. Can be
		 *         positive or negative.
		 */
		public int getStrength() {
			return strength;
		}

		/**
		 * Applies the effect to the given champion
		 *
		 * @param champion
		 *            the champion to who apply the effect. Can't be null.
		 */
		public void affect(Champion champion) {
			Validate.notNull(champion, "The given champion is null");

			final Stats stats = champion.getStats();

			switch (statistic) {
			case DEXTERITY:
				stats.getDexterity().incMax(strength);
				stats.getDexterity().inc(strength);
				break;
			case MANA:
				stats.getMana().incMax(strength);
				stats.getMana().inc(strength);
				break;
			case STRENGTH:
				stats.getStrength().incMax(strength);
				stats.getStrength().inc(strength);
				break;
			case MAX_LOAD:
				// MaxLoad is a special stat for which the boost is handled
				// separately from the base value (this value is computed
				// dynamically)
				stats.getMaxLoadBoost().inc(strength);
				break;
			case WISDOM:
				stats.getWisdom().incMax(strength);
				stats.getWisdom().inc(strength);
				break;
			case ANTI_FIRE:
				stats.getAntiFire().incMax(strength);
				stats.getAntiFire().inc(strength);
				break;
			case ANTI_MAGIC:
				stats.getAntiMagic().incMax(strength);
				stats.getAntiMagic().inc(strength);
				break;
			case LUCK:
				stats.getLuck().incMax(strength);
				stats.getLuck().inc(strength);
				break;
			case WIZARD_LEVEL:
				champion.getExperience(Skill.WIZARD).incBoost(strength);
				break;
			case HEAL_SKILL:
				champion.getExperience(Skill.HEAL).incBoost(strength);
				break;
			case DEFEND_SKILL:
				champion.getExperience(Skill.DEFEND).incBoost(strength);
				break;
			case INFLUENCE_SKILL:
				champion.getExperience(Skill.INFLUENCE).incBoost(strength);
				break;
			case ALL_SKILLS:
				for (Skill skill : Skill.values()) {
					champion.getExperience(skill).incBoost(strength);
				}
				break;
			default:
				throw new UnsupportedOperationException("Unsupported statistic " + statistic);
			}
		}

		/**
		 * Neutralizes the effect for the given champion.
		 *
		 * @param champion
		 *            the champion to who neutralize the effect. Can't be null.
		 */
		public void unaffect(Champion champion) {
			Validate.notNull(champion, "The given champion is null");

			final Stats stats = champion.getStats();

			switch (statistic) {
			case DEXTERITY:
				stats.getDexterity().dec(strength);
				stats.getDexterity().decMax(strength);
				break;
			case MANA:
				stats.getMana().dec(strength);
				stats.getMana().decMax(strength);
				break;
			case STRENGTH:
				stats.getStrength().dec(strength);
				stats.getStrength().decMax(strength);
				break;
			case MAX_LOAD:
				// MaxLoad is a special stat for which the boost is handled
				// separately from the base value (this value is computed
				// dynamically)
				stats.getMaxLoadBoost().dec(strength);
				break;
			case WISDOM:
				stats.getWisdom().dec(strength);
				stats.getWisdom().decMax(strength);
				break;
			case ANTI_FIRE:
				stats.getAntiFire().dec(strength);
				stats.getAntiFire().decMax(strength);
				break;
			case ANTI_MAGIC:
				stats.getAntiMagic().dec(strength);
				stats.getAntiMagic().decMax(strength);
				break;
			case LUCK:
				stats.getLuck().dec(strength);
				stats.getLuck().decMax(strength);
				break;
			case WIZARD_LEVEL:
				champion.getExperience(Skill.WIZARD).decBoost(strength);
				break;
			case HEAL_SKILL:
				champion.getExperience(Skill.HEAL).decBoost(strength);
				break;
			case DEFEND_SKILL:
				champion.getExperience(Skill.DEFEND).decBoost(strength);
				break;
			case INFLUENCE_SKILL:
				champion.getExperience(Skill.INFLUENCE).decBoost(strength);
				break;
			case ALL_SKILLS:
				for (Skill skill : Skill.values()) {
					champion.getExperience(skill).decBoost(strength);
				}
				break;
			default:
				throw new UnsupportedOperationException("Unsupported statistic " + statistic);
			}
		}
	}

	public static final class ActionDef {

		private Action action;

		/**
		 * The minimum skill level necessary for using this action. The default
		 * value is hard-coded here
		 */
		private Champion.Level minLevel = Champion.Level.NONE;

		/**
		 * Whether the use of this action is limited by a number of available
		 * charges. The default value is hard-coded here
		 */
		private boolean useCharges = false;

		// FIXME Add a damage property ?
		private ActionDef() {
		}

		public Action getAction() {
			return action;
		}

		public Champion.Level getMinLevel() {
			return minLevel;
		}

		public boolean isUseCharges() {
			return useCharges;
		}

		/**
		 * Tells whether the given champion can use this action.
		 *
		 * @param champion
		 *            the champion to test. Can't be null.
		 * @return whether the given champion can use this action.
		 */
		public boolean isUsable(Champion champion) {
			Validate.notNull(champion, "The given champion is null");

			// What's the minimal level required to use this skill ?
			final Level level = getMinLevel();

			if (Level.NONE.equals(level)) {
				// In most cases, there's no min skill level required
				return true;
			}

			// What's the skill involved ?
			final Skill skill = action.getSkill();

			// Is the champion skilled enough to use this action ?
			return (champion.getLevel(skill).compareTo(level) >= 0);
		}
	}

	private final static Map<String, ItemDef> DEFINITIONS = new LinkedHashMap<String, ItemDef>();

	static {
		try {
			// Parse the definitions of items from resource file "items.xml"
			final InputStream stream = ItemDef.class.getResourceAsStream("items.xml");

			final ItemDefParser parser = new ItemDefParser();

			SAXParserFactory.newInstance().newSAXParser().parse(stream, parser);

			for (ItemDef definition : parser.definitions) {
				DEFINITIONS.put(definition.id, definition);
			}
		} catch (Exception e) {
			throw new RuntimeException("Error when parsing item definitions", e);
		}
	}

	private String id;

	private int shield, antiMagic;

	/**
	 * The amount of energy lost by the projectile every time it moves.
	 */
	private int decayRate = -1;

	/**
	 * The amount of damage associated with fired projectiles. Value within
	 * [0,255].
	 */
	private int shootDamage = -1;

	/**
	 * This value determines how far the item will go when thrown. If a weapon
	 * is used to "Shoot", this will be a part of how far the item being shot
	 * will travel. The farther the projectile goes, the more damage it does
	 * (Damage is decreased as it flies). Valeur dans l'intervalle [0-255].
	 */
	private int distance = -1;

	/**
	 * The base value used for computing the damages caused by an attack with
	 * this item. Relevant only for a weapon item for which the value is
	 * positive. For other items, the value is set to -1.
	 */
	private int damage = -1;

	/**
	 * TODO Ensure the weight is >= 0.
 	 * The item's weight (in Kg) as a float.
	 */
	private float weight;

	/**
	 * The carry locations where this item can be stored.
	 */
	private final Set<CarryLocation> carryLocations = new HashSet<CarryLocation>();

	private BodyPart.Type activationBodyPart;

	private List<ItemDef.ActionDef> actions = new ArrayList<ItemDef.ActionDef>();

	private List<ItemDef.Effect> effects = new ArrayList<ItemDef.Effect>();

	ItemDef() {
	}

	public int getDamage() {
		return damage;
	}

	/**
	 * Returns the effects bestowed by this item to a champion when activated.
	 *
	 * @return a list of effects. Never returns null.
	 */
	public List<ItemDef.Effect> getEffects() {
		return Collections.unmodifiableList(effects);
	}

	/**
	 * Returns the actions associated to this item.
	 *
	 * @return a list of actions. Never returns null.
	 */
	public List<ItemDef.ActionDef> getActions() {
		return Collections.unmodifiableList(actions);
	}

	public int getDistance() {
		return distance;
	}

	public int getShootDamage() {
		return shootDamage;
	}

	public int getDecayRate() {
		return decayRate;
	}

	/**
	 * Returns the fire resistance bonus bestowed by this item when activated.
	 *
	 * @return an integer representing a fire resistance bonus. Returns zero if
	 *         the item doesn't bestow any bonus.
	 */
	public int getAntiMagic() {
		return antiMagic;
	}

	/**
	 * Returns the shield bonus bestowed by this item when activated.
	 *
	 * @return an integer representing a shield bonus. Returns zero if the item
	 *         doesn't bestow any bonus.
	 */
	public int getShield() {
		return shield;
	}

	/**
	 * Returns the body part that activates this item (if any). Returns null if
	 * the item can't be activated. When activated an item provides (in general)
	 * a bonus to the champion (be it a defense bonus, a stat bonus, etc).
	 * Examples: This method returns {@link BodyPart.Type#NECK} for an amulet,
	 * {@link BodyPart.Type#WEAPON_HAND} for a weapon item.
	 *
	 * @return the body part which activates this item (if any) or null.
	 */
	public BodyPart.Type getActivationBodyPart() {
		return activationBodyPart;
	}

	/**
	 * Returns the item's weight as a float.
	 *
	 * @return a float representing a weight in Kg.
	 */
	public float getWeight() {
		return weight;
	}

	public String getId() {
		return id;
	}

	/**
	 * Returns the carry locations where this item can be stored.
	 *
	 * @return a set of carry locations. Never returns null.
	 */
	public Set<CarryLocation> getCarryLocations() {
		return Collections.unmodifiableSet(carryLocations);
	}

	public static List<ItemDef> getAllDefinitions() {
		return new ArrayList<ItemDef>(DEFINITIONS.values());
	}

	public static ItemDef getDefinition(Item.Type type) {
		Validate.notNull(type, "The given item type is null");

		return DEFINITIONS.get(type.name());
	}

	public static void main(String[] args) throws Exception {
		final List<Item.Type> types = Arrays.asList(Item.Type.values());

		Collections.sort(types, new Comparator<Item.Type>() {
			@Override
			public int compare(Type o1, Type o2) {
				return o1.name().compareTo(o2.name());
			}
		});

		final StringWriter stringWriter = new StringWriter(32000);

		final XMLStreamWriter writer = new IndentingXMLStreamWriter(XMLOutputFactory.newFactory().createXMLStreamWriter(
				stringWriter));

		writer.writeStartDocument();
		writer.writeStartElement("items");
		writer.writeDefaultNamespace("yadmjc:items:1.0");

		for (Item.Type type : types) {
			writer.writeStartElement("item");
			writer.writeAttribute("id", type.name());
			writer.writeAttribute("weight", Float.toString(type.getWeight()));

			if (type.getDamage() != -1) {
				writer.writeAttribute("damage", Integer.toString(type.getDamage()));
			}

			if (type.getActivationBodyPart() != null) {
				writer.writeAttribute("activation", type.getActivationBodyPart().name());
			}

			if (type.getShield() != 0) {
				writer.writeAttribute("shield", Integer.toString(type.getShield()));
			}
			if (type.getAntiMagic() != 0) {
				writer.writeAttribute("anti-magic", Integer.toString(type.getAntiMagic()));
			}

			if (type.getDecayRate() != -1) {
				writer.writeAttribute("decay-rate", Integer.toString(type.getDecayRate()));
			}

			if (type.getDistance() != -1) {
				writer.writeAttribute("distance", Integer.toString(type.getDistance()));
			}

			if (type.getShootDamage() != -1) {
				writer.writeAttribute("shoot-damage", Integer.toString(type.getShootDamage()));
			}

			if (!type.getCarryLocations().isEmpty()) {
				writer.writeStartElement("locations");

				// Sort the locations to ensure they're always serialized in a consistent way
				for (CarryLocation location : new TreeSet<CarryLocation>(type.getCarryLocations())) {
					writer.writeEmptyElement("location");
					writer.writeAttribute("id", location.name());
				}
				writer.writeEndElement();
			}

			if (!type.getActions().isEmpty()) {
				writer.writeStartElement("actions");
				for (ActionDef actionDef : type.getActions()) {
					writer.writeEmptyElement("action");
					writer.writeAttribute("id", actionDef.getAction().name());

					if (actionDef.getMinLevel() != Champion.Level.NONE) {
						writer.writeAttribute("min-level", actionDef.getMinLevel().name());
					}

					if (actionDef.isUseCharges()) {
						writer.writeAttribute("use-charges", Boolean.toString(actionDef.isUseCharges()));
					}
				}
				writer.writeEndElement(); // </actions>
			}

			if (!type.getEffects().isEmpty()) {
				writer.writeStartElement("effects");
				for (Effect effect : type.getEffects()) {
					writer.writeEmptyElement("effect");
					writer.writeAttribute("stat", effect.getStatistic().name());
					writer.writeAttribute("strength", String.format("%+d", effect.getStrength()));
				}
				writer.writeEndElement(); // </effects>
			}

			writer.writeEndElement(); // </item>
		}

		writer.writeEndElement(); // </items>
		writer.writeEndDocument();

		System.out.println(stringWriter);
	}
}