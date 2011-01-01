package fr.ritaly.dungeonmaster.utils;

import java.util.ArrayList;
import java.util.Collection;

public class DMJArrayList<E> extends ArrayList<E> {

	private static final long serialVersionUID = 7161275216822263406L;

	public DMJArrayList() {
	}

	public DMJArrayList(Collection<? extends E> c) {
		super(c);
	}

	public DMJArrayList(int initialCapacity) {
		super(initialCapacity);
	}

	public boolean addNotNull(E e) {
		// Filtrer les valeurs nulles
		if (e == null) {
			return false;
		}

		return super.add(e);
	}
}