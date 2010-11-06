package fr.ritaly.dungeonmaster.projectile;

import org.apache.commons.lang.Validate;

import fr.ritaly.dungeonmaster.Direction;
import fr.ritaly.dungeonmaster.Position;
import fr.ritaly.dungeonmaster.SubCell;
import fr.ritaly.dungeonmaster.item.Item;
import fr.ritaly.dungeonmaster.item.ItemFactory;
import fr.ritaly.dungeonmaster.map.Dungeon;

public class ItemProjectileFactory implements ProjectileFactory {

	private final ItemFactory factory;

	private final Item.Type type;

	public ItemProjectileFactory(ItemFactory factory, Item.Type type) {
		Validate.notNull(factory, "The given item factory is null");
		Validate.notNull(type, "The given item type is null");

		this.factory = factory;
		this.type = type;
	}

	@Override
	public Projectile createProjectile(Dungeon dungeon, Position position,
			Direction direction, SubCell subCell) {

		// TODO Calculer la distance de jet de l'objet (fonction du champion si
		// besoin)

		// Créer un nouvel objet à chaque appel
		final Item item = factory.newItem(type);

		// Contrôles effectués par le constructeur de ItemProjectile
		return new ItemProjectile(item, dungeon, position, direction, subCell,
				30);
	}
}