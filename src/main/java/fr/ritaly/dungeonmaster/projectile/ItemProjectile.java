package fr.ritaly.dungeonmaster.projectile;

import org.apache.commons.lang.Validate;

import fr.ritaly.dungeonmaster.Direction;
import fr.ritaly.dungeonmaster.Position;
import fr.ritaly.dungeonmaster.SubCell;
import fr.ritaly.dungeonmaster.item.Item;
import fr.ritaly.dungeonmaster.map.Dungeon;

public class ItemProjectile extends AbstractProjectile {
	
	private final Item item;

	public ItemProjectile(Item item, Dungeon dungeon, Position position,
			Direction direction, SubCell subCell, int range) {
		
		super(dungeon, position, direction, subCell, range);
		
		Validate.notNull(item);

		this.item = item;
	}

	@Override
	protected void projectileDied() {
		// TODO Jouer le son de l'objet qui tombe à terre (dépend de l'objet !)
		// SoundSystem.getInstance().play(clip);

		// Déposer l'objet au sol (TODO Attention au sens pour les flèches !!)
		dungeon.getElement(getPosition()).itemDroppedDown(item, getSubCell());
	}
	
	// TODO La portée du projectile dépend du type de l'objet
}