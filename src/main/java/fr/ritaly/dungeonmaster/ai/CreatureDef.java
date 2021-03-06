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
package fr.ritaly.dungeonmaster.ai;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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

import fr.ritaly.dungeonmaster.Utils;
import fr.ritaly.dungeonmaster.ai.Creature.Height;
import fr.ritaly.dungeonmaster.ai.Creature.Size;
import fr.ritaly.dungeonmaster.champion.Champion;
import fr.ritaly.dungeonmaster.item.Item;
import fr.ritaly.dungeonmaster.item.ItemFactory;
import fr.ritaly.dungeonmaster.magic.PowerRune;
import fr.ritaly.dungeonmaster.magic.Spell;

/**
 * A definition of creature.
 *
 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
 */
final class CreatureDef {

	/**
	 * SAX handler to parse the resource file 'creatures.xml' defining items.
	 *
	 * @author <a href="mailto:francois.ritaly@gmail.com">Francois RITALY</a>
	 */
	private static final class CreatureDefParser extends DefaultHandler {

		private final Log log = LogFactory.getLog(this.getClass());

		private final List<CreatureDef> definitions = new ArrayList<CreatureDef>();

		private CreatureDef definition;

		private CreatureDefParser() {
		}

		@Override
		public void startDocument() throws SAXException {
			this.definitions.clear();

			if (log.isDebugEnabled()) {
				log.debug("Parsing creature definitions ...");
			}
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			final String elementName = qName;

			if ("creatures".equals(elementName) || "weaknesses".equals(elementName) || "spells".equals(elementName) || "items".equals(elementName)) {
				// Do nothing
			} else if ("creature".equals(elementName)) {
				this.definition = new CreatureDef();
				this.definition.id = attributes.getValue("id");
				this.definition.baseHealth = Integer.valueOf(attributes.getValue("base-health"));
				this.definition.height = Height.valueOf(attributes.getValue("height"));
				this.definition.size = Size.valueOf(attributes.getValue("size"));
				this.definition.awareness = Integer.valueOf(attributes.getValue("awareness"));
				this.definition.bravery = Integer.valueOf(attributes.getValue("bravery"));
				this.definition.experienceMultiplier = Integer.valueOf(attributes.getValue("experience-multiplier"));
				this.definition.moveDuration = Integer.valueOf(attributes.getValue("move-duration"));
				this.definition.sightRange = Integer.valueOf(attributes.getValue("sight-range"));
				this.definition.absorbsItems = Boolean.valueOf(attributes.getValue("absorbs-items"));
				this.definition.levitates = Boolean.valueOf(attributes.getValue("levitates"));
				this.definition.archenemy = Boolean.valueOf(attributes.getValue("archenemy"));
				this.definition.nightVision = Boolean.valueOf(attributes.getValue("night-vision"));
				this.definition.seesInvisible = Boolean.valueOf(attributes.getValue("sees-invisible"));
			} else if ("defense".equals(elementName)) {
				this.definition.antiMagic = Integer.valueOf(attributes.getValue("anti-magic"));
				this.definition.armor = Integer.valueOf(attributes.getValue("armor"));
				this.definition.shield = Integer.valueOf(attributes.getValue("shield"));
				this.definition.poisonResistance = Integer.parseInt(attributes.getValue("poison"));
			} else if ("attack".equals(elementName)) {
				this.definition.attackSkill = Champion.Level.valueOf(attributes.getValue("skill"));
				this.definition.attackAnimationDuration = Integer.parseInt(attributes.getValue("animation-duration"));
				this.definition.attackDuration = Integer.parseInt(attributes.getValue("duration"));
				this.definition.attackPower = Integer.parseInt(attributes.getValue("power"));
				this.definition.attackType = AttackType.valueOf(attributes.getValue("type"));
				this.definition.attackRange = Integer.parseInt(attributes.getValue("range"));
				this.definition.attackProbability = Integer.parseInt(attributes.getValue("probability"));
				this.definition.sideAttack = Boolean.valueOf(attributes.getValue("side-attack"));
			} else if ("poison".equals(elementName)) {
				if (attributes.getValue("strength") != null) {
					this.definition.poison = Integer.parseInt(attributes.getValue("strength"));
				}
			} else if ("spell".equals(elementName)) {
				this.definition.spells.add(Spell.Type.valueOf(attributes.getValue("id")));
			} else if ("weakness".equals(elementName)) {
				this.definition.weaknesses.add(Weakness.valueOf(attributes.getValue("id")));
			} else if ("item".equals(elementName)) {
				final ItemDef def = new ItemDef();
				def.type = Item.Type.valueOf(attributes.getValue("type"));
				def.min = Integer.parseInt(attributes.getValue("min"));
				def.max = Integer.parseInt(attributes.getValue("max"));

				if (attributes.getValue("curse") != null) {
					def.curse = PowerRune.valueOf(attributes.getValue("curse"));
				}

				this.definition.itemDefs.add(def);
			} else {
				throw new SAXException(String.format("Unexpected element name '%s'", elementName));
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			final String elementName = qName;

			if ("creatures".equals(elementName) || "weaknesses".equals(elementName) || "spells".equals(elementName) || "items".equals(elementName)) {
				// Do nothing
			} else if ("creature".equals(elementName)) {
				this.definitions.add(definition);

				this.definition = null;
			} else if ("defense".equals(elementName)) {
				// Do nothing
			} else if ("attack".equals(elementName)) {
				// Do nothing
			} else if ("poison".equals(elementName)) {
				// Do nothing
			} else if ("weakness".equals(elementName)) {
				// Do nothing
			} else if ("spell".equals(elementName)) {
				// Do nothing
			} else if ("item".equals(elementName)) {
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
				log.info(String.format("Parsed %d creature definitions", definitions.size()));
			}
		}
	}

	public static final class ItemDef {

		private Item.Type type;

		private int min;

		private int max;

		private PowerRune curse;

		private ItemDef() {
		}

		public List<Item> getItems() {
			final List<Item> list = new ArrayList<Item>();

			for (int i = 0; i < Utils.random(min, max); i++) {
				final Item item = ItemFactory.getFactory().newItem(type);

				if (curse != null) {
					item.curse(curse);
				}

				list.add(item);
			}

			return list;
		}
	}

	private final static Map<String, CreatureDef> DEFINITIONS = new LinkedHashMap<String, CreatureDef>();

	static {
		try {
			// Parse the definitions of creatures from resource file "creatures.xml"
			final InputStream stream = CreatureDef.class.getResourceAsStream("creatures.xml");

			final CreatureDefParser parser = new CreatureDefParser();

			SAXParserFactory.newInstance().newSAXParser().parse(stream, parser);

			for (CreatureDef definition : parser.definitions) {
				DEFINITIONS.put(definition.id, definition);
			}
		} catch (Exception e) {
			throw new RuntimeException("Error when parsing creature definitions", e);
		}
	}

	private String id;

	/**
	 * These two bits define the height of the creature. It is used to check if
	 * missiles can fly over the creatures (for example Fireballs can fly over
	 * small creatures). This value is also used to define how to animate a door
	 * that is closed upon the creature:
	 * <ul>
	 * <li>'0': the door is not animated and stays fully open. The creature
	 * still takes damage.</li>
	 * <li>'1': the door is animated from the top to 1/4th of its size. This
	 * applies to tall creatures like Mummies.</li>
	 * <li>'2': the door is animated between 1/4th of its size to half of its
	 * size. This applies to medium sized creatures like Screamers.</li>
	 * <li>'3': the door is animated from half of its size to 3/4th of its size.
	 * This applies to small creatures like the Worm.</li>
	 * Note: This value is ignored for non material creatures and the door
	 * always closes normally without causing any damage to such creatures.
	 */
	private Height height;

	private Creature.Size size;

	/**
	 * The base health is used to calculate the health of creatures
	 * generated during the game.
	 */
	private int baseHealth;

	/**
	 * Maximum number of tiles between a creature and party needed to detect and
	 * "turn" towards the party, perhaps to shoot a projectile. This applies
	 * even if the creature is not facing the party. Value within [0,15] (aka
	 * "Detection range").
	 */
	private int awareness;

	/**
	 * Resistance to War Cry, Calm, Brandish and Blow Horn (maybe also
	 * Confuse). Value within [0,15]. The special value 15 means the
	 * creature can't be frightened.
	 */
	private int bravery;

	/**
	 * This value is used as a multiplier to compute the experience earned
	 * by a champion killing this creature
	 */
	private int experienceMultiplier;

	/**
	 * Defines how long it takes for the creature to move from one position
	 * to another (in number of clock ticks). Value within [0,255]. The
	 * special value 255 means that the creature can't move.
	 */
	private int moveDuration;

	/**
	 * Maximum number of tiles between creature and party needed to see the
	 * party. This applies only if the creature is facing the party. This value
	 * is affected by the current light level in the dungeon (the value is
	 * halved for each level of darkness). Value within [0,15].
	 */
	private int sightRange;

	/**
	 * When this bit is set to '1', the creature can absorb some items when they
	 * are thrown at the creature (like the Mummy). The list of items that can
	 * be absorbed is hard coded in the program (Arrow, Slayer, Poison Dart,
	 * Throwing Star and Dagger). If a thrown item is not absorbed by the
	 * creature, it falls on the floor (it is never destroyed). This is not
	 * linked to the ability of the Giggler to steal items in champion hands
	 * which is hard coded. See
	 * http://www.gamefaqs.com/snes/588299-dungeon-master/faqs/33244
	 */
	private boolean absorbsItems;

	/**
	 * cf http://www.gamefaqs.com/snes/588299-dungeon-master/faqs/33244. If this
	 * bit is set to '1', the creature can pass over pits without falling
	 */
	private boolean levitates;

	/**
	 * When this bit is set, the creature never takes any damage (health is not
	 * decreased), it can teleport up to two tiles away and it cannot move to a
	 * tile containing a flux cage.
	 */
	private boolean archenemy;

	/**
	 * When this bit is set to '1', the creature can see the party in darkness
	 * because it ignores the sight range reduction caused by low light levels
	 * in the dungeon.
	 */
	private boolean nightVision;

	/**
	 * When this bit is set to '1', the creature can see the party even if it is
	 * under the effect of the 'Invisibility' spell.
	 */
	private boolean seesInvisible;

	/**
	 * Resistance to magical spells like Fireball. Value within [0,15]. The
	 * special value 15 means the creature is immune to magic attacks. aka
	 * "Fire resistance".
	 */
	private int antiMagic;

	/**
	 * This is the resistance to damage including Dispell on non material
	 * creatures. The armor value is always within range [0,255]. The special
	 * value 255 means that the creature is invincible.
	 */
	private int armor;

	/**
	 * Value within [0,255]. The special value 255 means the creature is
	 * untouchable. This value represents the difficulty for champions to
	 * hit the creature
	 */
	private int shield;

	private Champion.Level attackSkill;

	/**
	 * The number of clock ticks during which the animation of an attacking
	 * creature should be displayed. Value within [0,255].
	 */
	private int attackAnimationDuration;

	/**
	 * This is the number of clock ticks per attack, defining the attack
	 * speed of the creature. This is the minimum amount of time required
	 * between two attacks. Value within [0,255].
	 */
	private int attackDuration;

	/**
	 * The base value for computing how much damage a creature's attack will
	 * inflict.
	 */
	private int attackPower;

	/**
	 * This "number" is used to determine what kind of attack the creature
	 * executes. Changing this value will result in a different "protection"
	 * to be used when calculating the damage:
	 *
	 * 1: Use Anti-Fire to determine damage 2: Half the hero's armor and do
	 * physical damage 3: Unknown 4: Deal physical piercing damage 5: Use
	 * Anti-Magic to determine damage 6: Use Wisdom to determine damage
	 */
	private AttackType attackType;

	/**
	 * Maximum number of tiles between creature and party needed to perform a
	 * distance attack (cast a spell). Valeur dans l'intervalle [0-15]. aka
	 * "Spell casting range".
	 */
	private int attackRange;

	/**
	 * The odds of hitting a {@link Champion}. Value within [0,255].
	 */
	private int attackProbability;

	/**
	 * The amount of poison inflicted when the creature successfully hits a
	 * character.Value within [0,255].
	 */
	private int poison;

	/**
	 * Resistance to magical spells involving poison. Value within [0,15].
	 * The special value 15 means the creature is immune to poison attacks.
	 */
	private int poisonResistance;

	/**
	 * The creature does not need to face the party to attack. This flag is set
	 * only for creatures that have the same image for all sides. It affects
	 * their attack frequency because they don't need to turn to face the party
	 * before attacking.
	 */
	private boolean sideAttack;

	private final Set<Weakness> weaknesses = new TreeSet<Weakness>();

	private final Set<Spell.Type> spells = new TreeSet<Spell.Type>();

	private final List<ItemDef> itemDefs = new ArrayList<CreatureDef.ItemDef>();

	CreatureDef() {
	}

	public boolean isSeesInvisible() {
		return seesInvisible;
	}

	public boolean isNightVision() {
		return nightVision;
	}

	public boolean isArchenemy() {
		return archenemy;
	}

	public boolean isLevitates() {
		return levitates;
	}

	public boolean isAbsorbsItems() {
		return absorbsItems;
	}

	public boolean isSideAttack() {
		return sideAttack;
	}

	public int getAttackProbability() {
		return attackProbability;
	}

	public int getBaseHealth() {
		return baseHealth;
	}

	public int getAntiMagic() {
		return antiMagic;
	}

	public int getArmor() {
		return armor;
	}

	public int getAttackAnimationDuration() {
		return attackAnimationDuration;
	}

	public int getAttackDuration() {
		return attackDuration;
	}

	public int getAttackPower() {
		return attackPower;
	}

	public int getAttackRange() {
		return attackRange;
	}

	public AttackType getAttackType() {
		return attackType;
	}

	public int getAwareness() {
		return awareness;
	}

	public int getBravery() {
		return bravery;
	}

	public int getExperienceMultiplier() {
		return experienceMultiplier;
	}

	public Height getHeight() {
		return height;
	}

	public String getId() {
		return id;
	}

	public int getMoveDuration() {
		return moveDuration;
	}

	public int getPoison() {
		return poison;
	}

	public int getPoisonResistance() {
		return poisonResistance;
	}

	public int getShield() {
		return shield;
	}

	public int getSightRange() {
		return sightRange;
	}

	public Creature.Size getSize() {
		return size;
	}

	public Champion.Level getAttackSkill() {
		return attackSkill;
	}

	public Set<Spell.Type> getSpells() {
		return Collections.unmodifiableSet(spells);
	}

	public Set<Weakness> getWeaknesses() {
		return Collections.unmodifiableSet(weaknesses);
	}

	public List<ItemDef> getItemDefs() {
		return Collections.unmodifiableList(itemDefs);
	}

	public static Map<String, CreatureDef> getDefinitions() {
		return DEFINITIONS;
	}

	public static List<CreatureDef> getAllDefinitions() {
		return new ArrayList<CreatureDef>(DEFINITIONS.values());
	}

	public static CreatureDef getDefinition(Creature.Type type) {
		Validate.notNull(type, "The given creature type is null");

		return DEFINITIONS.get(type.name());
	}

	/**
	 * Generates and returns a list of items corresponding to the items
	 * dropped by the creature when killed.
	 *
	 * @return a list of items. Never returns null.
	 */
	public List<Item> getItems() {
		if (itemDefs.isEmpty()) {
			return Collections.emptyList();
		}

		final List<Item> list = new ArrayList<Item>();

		for (ItemDef def : itemDefs) {
			list.addAll(def.getItems());
		}

		return list;
	}

	public static void main(String[] args) throws Exception {
		final List<Creature.Type> types = Arrays.asList(Creature.Type.values());

		Collections.sort(types, new Comparator<Creature.Type>() {
			@Override
			public int compare(Creature.Type o1, Creature.Type o2) {
				return o1.name().compareTo(o2.name());
			}
		});

		final StringWriter stringWriter = new StringWriter(32000);

		final XMLStreamWriter writer = new IndentingXMLStreamWriter(XMLOutputFactory.newFactory().createXMLStreamWriter(
				stringWriter));

		writer.writeStartDocument();
		writer.writeStartElement("creatures");
		writer.writeDefaultNamespace("yadmjc:creatures:1.0");

		for (Creature.Type type : types) {
			writer.writeStartElement("creature");
			writer.writeAttribute("id", type.name());
			writer.writeAttribute("base-health", Integer.toString(type.getBaseHealth()));
			writer.writeAttribute("height", type.getHeight().name());
			writer.writeAttribute("size", type.getSize().name());
			writer.writeAttribute("awareness", Integer.toString(type.getAwareness()));
			writer.writeAttribute("bravery", Integer.toString(type.getBravery()));
			writer.writeAttribute("experience-multiplier", Integer.toString(type.getExperienceMultiplier()));
			writer.writeAttribute("move-duration", Integer.toString(type.getMoveDuration()));
			writer.writeAttribute("sight-range", Integer.toString(type.getSightRange()));
			writer.writeAttribute("absorbs-items", Boolean.toString(type.isAbsorbsItems()));
			writer.writeAttribute("levitates", Boolean.toString(type.levitates()));
			writer.writeAttribute("archenemy", Boolean.toString(type.isArchenemy()));
			writer.writeAttribute("night-vision", Boolean.toString(type.isNightVision()));
			writer.writeAttribute("sees-invisible", Boolean.toString(type.isSeesInvisible()));

			writer.writeEmptyElement("defense");
			writer.writeAttribute("anti-magic", Integer.toString(type.getAntiMagic()));
			writer.writeAttribute("armor", Integer.toString(type.getArmor()));
			writer.writeAttribute("shield", Integer.toString(type.getShield()));
			writer.writeAttribute("poison", Integer.toString(type.getPoisonResistance()));

			writer.writeEmptyElement("attack");
			writer.writeAttribute("skill", type.getAttackSkill().name());
			writer.writeAttribute("animation-duration", Integer.toString(type.getAttackAnimationDuration()));
			writer.writeAttribute("duration", Integer.toString(type.getAttackDuration()));
			writer.writeAttribute("power", Integer.toString(type.getAttackPower()));
			writer.writeAttribute("type", type.getAttackType().name());
			writer.writeAttribute("range", Integer.toString(type.getAttackRange()));
			writer.writeAttribute("probability", Integer.toString(type.getAttackProbability()));
			writer.writeAttribute("side-attack", Boolean.toString(type.isSideAttackAllowed()));

			writer.writeEmptyElement("poison");
			if (type.getPoison() != 0) {
				writer.writeAttribute("strength", Integer.toString(type.getPoison()));
			}

			if (!type.getSpells().isEmpty()) {
				writer.writeStartElement("spells");

				// Sort the spells to ensure they're always serialized in a
				// consistent way
				for (Spell.Type spell : new TreeSet<Spell.Type>(type.getSpells())) {
					writer.writeEmptyElement("spell");
					writer.writeAttribute("id", spell.name());
				}

				writer.writeEndElement(); // </spells>
			}

			if (!type.getWeaknesses().isEmpty()) {
				writer.writeStartElement("weaknesses");

				// Sort the weaknesses to ensure they're always serialized in a
				// consistent way
				for (Weakness weakness : new TreeSet<Weakness>(type.getWeaknesses())) {
					writer.writeEmptyElement("weakness");
					writer.writeAttribute("id", weakness.name());
				}

				writer.writeEndElement(); // </weaknesses>
			}

			if (!type.getDefinition().getItemDefs().isEmpty()) {
				writer.writeStartElement("items");

				for (ItemDef itemDef : type.getDefinition().getItemDefs()) {
					writer.writeEmptyElement("item");
					writer.writeAttribute("type", itemDef.type.name());
					writer.writeAttribute("min", Integer.toString(itemDef.min));
					writer.writeAttribute("max", Integer.toString(itemDef.max));

					if (itemDef.curse != null) {
						writer.writeAttribute("curse", itemDef.curse.name());
					}
				}

				writer.writeEndElement(); // </items>
			}

			writer.writeEndElement(); // </creature>
		}

		writer.writeEndElement(); // </creatures>
		writer.writeEndDocument();

		System.out.println(stringWriter);
	}
}