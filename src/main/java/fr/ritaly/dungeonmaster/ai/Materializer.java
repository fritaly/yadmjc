package fr.ritaly.dungeonmaster.ai;

import fr.ritaly.dungeonmaster.ClockListener;

public interface Materializer extends ClockListener {

	public boolean isMaterial();
	
	public boolean isImmaterial();
}