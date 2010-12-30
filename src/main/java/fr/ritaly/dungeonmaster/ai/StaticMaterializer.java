package fr.ritaly.dungeonmaster.ai;

public class StaticMaterializer implements Materializer {

	private final boolean material;

	public StaticMaterializer(boolean material) {
		this.material = material;
	}

	@Override
	public boolean clockTicked() {
		// Donnée statique non fonction du temps
		return false;
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