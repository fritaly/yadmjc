package fr.ritaly.dungeonmaster.ai;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.ritaly.dungeonmaster.Temporizer;
import fr.ritaly.dungeonmaster.Utils;

public class RandomMaterializer implements Materializer {

	private final Log log = LogFactory.getLog(this.getClass());

	private Temporizer temporizer;

	private boolean material;

	private final Creature creature;

	public RandomMaterializer(Creature creature) {
		Validate.notNull(creature, "The given creature is null");

		final int count = Utils.random(6, 6 * 3);

		this.creature = creature;
		this.temporizer = new Temporizer(creature.getId() + ".", count);

		if (log.isDebugEnabled()) {
			log.debug(creature + " is now material for " + count + " ticks");
		}
	}

	@Override
	public boolean clockTicked() {
		if (temporizer.trigger()) {
			this.material = !this.material;

			// Recycler le temporizer
			final int count = Utils.random(6, 6 * 3);

			this.temporizer = new Temporizer(creature.getId() + ".", count);

			if (log.isDebugEnabled()) {
				log.debug(creature + " is now "
						+ (isMaterial() ? "material" : "immaterial") + " for "
						+ count + " ticks");
			}
		}

		return true;
	}

	@Override
	public boolean isMaterial() {
		return material;
	}

	@Override
	public boolean isImmaterial() {
		return !isMaterial();
	}
}