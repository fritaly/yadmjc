package fr.ritaly.dungeonmaster;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Annotation used to tag a property that can never be null.
 * 
 * @author <a href="mailto:francois.ritaly@free.fr">Francois RITALY</a>
 */
@Target(ElementType.FIELD)
public @interface NeverNull {

}