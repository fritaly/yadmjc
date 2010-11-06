package fr.ritaly.dungeonmaster;

import fr.ritaly.dungeonmaster.map.Dungeon;

/**
 * A factory of {@link Projectile}s.
 * 
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public interface ProjectileFactory {

	/**
	 * Creates and returns a new {@link Projectile} from given parameters.
	 * 
	 * @param dungeon
	 *            the {@link Dungeon} where the projectile will be created.
	 * @param position
	 *            the {@link Position} where the projectile will be created.
	 * @param direction
	 *            the {@link Direction} the created projectile will be pointing
	 *            to.
	 * @param subCell
	 *            the {@link SubCell} where the projectile will be created.
	 * @return a new instance of {@link Projectile}.
	 */
	public Projectile createProjectile(Dungeon dungeon, Position position,
			Direction direction, SubCell subCell);
}
