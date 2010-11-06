package fr.ritaly.dungeonmaster.projectile;

import org.apache.commons.lang.Validate;

import fr.ritaly.dungeonmaster.Direction;
import fr.ritaly.dungeonmaster.Position;
import fr.ritaly.dungeonmaster.SubCell;
import fr.ritaly.dungeonmaster.magic.Spell;
import fr.ritaly.dungeonmaster.map.Dungeon;

public class SpellProjectileFactory implements ProjectileFactory {

	private final Spell spell;

	public SpellProjectileFactory(Spell spell) {
		Validate.notNull(spell, "The given spell is null");
		Validate.isTrue(spell.getType().isProjectile(), "The given spell "
				+ spell.getName() + " isn't a projectile spell");

		this.spell = spell;
	}

	@Override
	public Projectile createProjectile(Dungeon dungeon, Position position,
			Direction direction, SubCell subCell) {

		// Contrôles effectués par le constructeur de SpellProjectile
		return new SpellProjectile(spell, dungeon, position, direction, subCell);
	}
}