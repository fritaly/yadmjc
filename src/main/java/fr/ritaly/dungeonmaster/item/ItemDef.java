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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import fr.ritaly.dungeonmaster.Skill;
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.champion.Champion.Level;
import fr.ritaly.dungeonmaster.champion.body.BodyPart;
import fr.ritaly.dungeonmaster.item.Item.AffectedStatistic;
import fr.ritaly.dungeonmaster.stat.Stats;

final class ItemDef {

	private static final Log LOG = LogFactory.getLog(ItemDef.class);

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
			final Skill skill = action.getImprovedSkill();

			// Is the champion skilled enough to use this action ?
			return (champion.getLevel(skill).compareTo(level) >= 0);
		}
	}

	private final static Map<String, ItemDef> DEFINITIONS = new LinkedHashMap<String, ItemDef>();

	static {
		try {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Loading item definitions ...");
			}

			// Parse the definitions of items from resource file "items.xml"
			final InputStream stream = ItemDef.class.getResourceAsStream("items.xml");

			final Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);

			final XPath xpath = XPathFactory.newInstance().newXPath();

			final NodeList nodes = (NodeList) xpath.evaluate("/items/item", document.getDocumentElement(), XPathConstants.NODESET);

			for (int i = 0; i < nodes.getLength(); i++) {
				final Node node = nodes.item(i);

				final ItemDef itemDef = new ItemDef();
				itemDef.id = xpath.evaluate("./@id", node);
				itemDef.weight = Float.parseFloat(xpath.evaluate("./@weight", node));

				// Locations
				final NodeList locationNodes = (NodeList) xpath.evaluate("./locations/location/@id", node, XPathConstants.NODESET);

				for (int j = 0; j < locationNodes.getLength(); j++) {
					 final Attr attr = (Attr) locationNodes.item(j);

					 itemDef.carryLocations.add(CarryLocation.valueOf(attr.getValue()));
				}

				// Activation
				final String activation = xpath.evaluate("./@activation", node);

				if (!StringUtils.isBlank(activation)) {
					itemDef.activationBodyPart = BodyPart.Type.valueOf(activation);
				}

				// Damage
				final String damage = xpath.evaluate("./@damage", node);

				if (!StringUtils.isBlank(damage)) {
					itemDef.damage = Integer.parseInt(damage);
				}

				// Shield
				final String shield = xpath.evaluate("./@shield", node);

				if (!StringUtils.isBlank(shield)) {
					itemDef.shield = Integer.parseInt(shield);
				}

				// Anti-magic
				final String antiMagic = xpath.evaluate("./@anti-magic", node);

				if (!StringUtils.isBlank(antiMagic)) {
					itemDef.antiMagic = Integer.parseInt(antiMagic);
				}

				// Delta energy
				final String deltaEnergy = xpath.evaluate("./delta-energy/text()", node);

				if (!StringUtils.isBlank(deltaEnergy)) {
					itemDef.deltaEnergy = Integer.parseInt(deltaEnergy);
				}

				// Distance
				final String distance = xpath.evaluate("./distance/text()", node);

				if (!StringUtils.isBlank(distance)) {
					itemDef.distance = Integer.parseInt(distance);
				}

				// Shoot damage
				final String shootDamage = xpath.evaluate("./shoot-damage/text()", node);

				if (!StringUtils.isBlank(shootDamage)) {
					itemDef.shootDamage = Integer.parseInt(shootDamage);
				}

				// Actions
				final NodeList actionNodes = (NodeList) xpath.evaluate("./actions/action", node, XPathConstants.NODESET);

				for (int j = 0; j < actionNodes.getLength(); j++) {
					 final Element actionNode = (Element) actionNodes.item(j);

					 final ActionDef actionDef = new ActionDef();
					 actionDef.action = Action.valueOf(actionNode.getAttribute("id"));

					 if (actionNode.hasAttribute("min-level")) {
						 actionDef.minLevel = Champion.Level.valueOf(actionNode.getAttribute("min-level"));
					 }
					 if (actionNode.hasAttribute("use-charges")) {
						 actionDef.useCharges = Boolean.parseBoolean(actionNode.getAttribute("use-charges"));
					 }

					itemDef.actions.add(actionDef);
				}

				// Effects
				final NodeList effectNodes = (NodeList) xpath.evaluate("./effects/effect", node, XPathConstants.NODESET);

				for (int j = 0; j < effectNodes.getLength(); j++) {
					final Element effectNode = (Element) effectNodes.item(j);

					final Effect effect = new Effect();
					effect.statistic = AffectedStatistic.valueOf(effectNode.getAttribute("stat"));
					effect.strength = Integer.parseInt(effectNode.getAttribute("strength"));

					itemDef.effects.add(effect);
				}

				DEFINITIONS.put(itemDef.id, itemDef);
			}

			if (LOG.isInfoEnabled()) {
				LOG.info(String.format("Loaded %d item definitions", DEFINITIONS.size()));
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
	private int deltaEnergy = -1;

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

	public int getDeltaEnergy() {
		return deltaEnergy;
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
}