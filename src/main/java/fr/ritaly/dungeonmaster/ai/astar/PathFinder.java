package fr.ritaly.dungeonmaster.ai.astar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang.Validate;

import fr.ritaly.dungeonmaster.Materiality;
import fr.ritaly.dungeonmaster.map.Dungeon;
import fr.ritaly.dungeonmaster.map.Element;
import fr.ritaly.dungeonmaster.map.Floor;
import fr.ritaly.dungeonmaster.map.Level;

/**
 * See http://memoization.com/2008/11/30/a-star-algorithm-in-java/ for the web
 * page where the original source code comes from. The code was refactored so as 
 * to make the maze stateless.
 */
public class PathFinder {

	private final Level level;

	private final Materiality materiality;

	private final List<Element> opened = new ArrayList<Element>();

	private final List<Element> closed = new ArrayList<Element>();

	private final List<Element> bestList = new ArrayList<Element>();

	private final Map<Element, Element> parents = new LinkedHashMap<Element, Element>();

	public PathFinder(Level level, Materiality materiality) {
		Validate.notNull(level);
		Validate.notNull(materiality);

		this.level = level;
		this.materiality = materiality;
	}

	public List<Element> findBestPath(int startX, int startY, int endX, int endY) {
		final Element start = level.getElement(startX, startY);
		final Element goal = level.getElement(endX, endY);

		System.out.println("Calculating best path...");

		for (Element adjacency : start.getReachableElements()) {
			if (Materiality.IMMATERIAL.equals(materiality)) {
				// Tous les noeuds peuvent être traversés
			} else {
				// Seuls les noeuds "non concrets" peuvent être traversés
				if (adjacency.isConcrete()) {
					// Ignorer ce noeud
					continue;
				}
			}

			parents.put(adjacency, start);

			if (!((adjacency.getPosition().x == startX) && (adjacency
					.getPosition().y == startY))) {
				opened.add(adjacency);
			}
		}

		while (opened.size() > 0) {
			final Element best = findBestPassThrough(goal, startX, startY);

			opened.remove(best);
			closed.add(best);

			if ((best.getPosition().x == endX)
					&& (best.getPosition().y == endY)) {
				System.out.println("Found Goal");
				populateBestList(goal, startX, startY);

				// Rajouter la position de départ à la solution
				bestList.add(start);

				// Inverser la liste pour aller de la position de départ au but
				Collections.reverse(bestList);

				System.out.println(level.draw(bestList));

				return bestList;
			} else {
				final List<Element> neighbors = best.getReachableElements();

				for (Element neighbor : neighbors) {
					if (Materiality.IMMATERIAL.equals(materiality)) {
						// Tous les noeuds peuvent être traversés
					} else {
						// Seuls les noeuds "non concrets" peuvent être
						// traversés
						if (neighbor.isConcrete()) {
							// Ignorer ce noeud
							continue;
						}
					}

					if (opened.contains(neighbor)) {
						final Floor temp = new Floor();
						temp.setPosition(neighbor.getPosition());
						temp.setLevel(level);

						parents.put(temp, best);

						if (getPassThrough(temp, goal, startX, startY) >= getPassThrough(
								neighbor, goal, startX, startY)) {

							continue;
						}
					}

					if (closed.contains(neighbor)) {
						final Floor temp = new Floor();
						temp.setPosition(neighbor.getPosition());
						temp.setLevel(level);

						parents.put(temp, best);

						if (getPassThrough(temp, goal, startX, startY) >= getPassThrough(
								neighbor, goal, startX, startY)) {

							continue;
						}
					}

					parents.put(neighbor, best);

					opened.remove(neighbor);
					closed.remove(neighbor);
					opened.add(0, neighbor);
				}
			}
		}

		return null;
	}

	private Element findBestPassThrough(Element goal, int startX, int startY) {
		Element best = null;
		for (Element square : opened) {
			if (best == null
					|| getPassThrough(square, goal, startX, startY) < getPassThrough(
							best, goal, startX, startY)) {
				best = square;
			}
		}

		return best;
	}

	private void populateBestList(Element square, int startX, int startY) {
		bestList.add(square);

		final Element parent = parents.get(square);

		if (!((parent.getPosition().x == startX) && (parent.getPosition().y == startY))) {
			populateBestList(parents.get(square), startX, startY);
		}
	}

	private double getPassThrough(Element square, Element goal, int x, int y) {
		if ((square.getPosition().x == x) && (square.getPosition().y == y)) {
			return 0.0;
		}

		return getLocalCost(square, goal, x, y) + getParentCost(square, x, y);
	}

	private double getLocalCost(Element square, Element goal, int x, int y) {
		if ((square.getPosition().x == x) && (square.getPosition().y == y)) {
			return 0.0;
		}

		// cost of getting from this square to goal
		return 1.0 * (Math.abs(x - goal.getPosition().x) + Math.abs(y
				- goal.getPosition().y));
	}

	private double getParentCost(Element square, int x, int y) {
		if ((square.getPosition().x == x) && (square.getPosition().y == y)) {
			return 0.0;
		}

		return 1.0 + .5 * (getParentCost(parents.get(square), x, y) - 1.0);
	}

	public static void main(String[] args) {
		int startX = 0, startY = 0;
		int goalX = 0, goalY = 0;

		List<Element> path = null;

		do {
			Dungeon dungeon = new Dungeon();
			Level level = new Level(dungeon, 1, 10, 10);

			Random random = new Random();

			startX = random.nextInt(10);
			startY = random.nextInt(10);

			do {
				goalX = random.nextInt(10);
				goalY = random.nextInt(10);
			} while (goalX == startX && goalY == startY);

			path = new PathFinder(level, Materiality.MATERIAL).findBestPath(
					startX, startY, goalX, goalY);
		} while (path == null);
	}
}