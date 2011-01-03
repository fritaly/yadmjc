package fr.ritaly.dungeonmaster;

/**
 * Enumération des "matérialités" supportées dans le jeu.
 * 
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
public enum Materiality {
	/**
	 * Définit une entité matérielle (la plupart des objets dans le jeu).
	 */
	MATERIAL,

	/**
	 * Définit une entité immatérielle (les créatures de type GHOST, ZYTAZ, etc
	 * qui peuvent passer à travers les murs, portes)
	 */
	IMMATERIAL;
}
