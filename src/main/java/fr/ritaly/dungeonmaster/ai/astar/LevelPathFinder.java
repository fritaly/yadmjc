package fr.ritaly.dungeonmaster.ai.astar;

import java.util.LinkedList;
import java.util.List;

import fr.ritaly.dungeonmaster.map.Dungeon;
import fr.ritaly.dungeonmaster.map.Element;
import fr.ritaly.dungeonmaster.map.Level;

public class LevelPathFinder extends AStar<LevelPathFinder.Node> {
	private final Level level;

	private final int targetX, targetY;

	public static class Node {
		public int x;
		public int y;

		public Node(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public String toString() {
			return "(" + x + ", " + y + ") ";
		}
	}

	public LevelPathFinder(Level level, int x, int y) {
		this.level = level;
		this.targetX = x;
		this.targetY = y;
	}

	protected boolean isGoal(Node node) {
		return (node.x == targetX) && (node.y == targetY);
	}

	protected Double g(Node from, Node to) {
		if (from.x == to.x && from.y == to.y) {
			// But atteint
			return 0.0;
		}

		// FIXME Améliorer la prise en compte du côté traversable des éléments
		if (!level.getElement(to.x, to.y).isConcrete()) {
			// Element franchissable
			return 1.0;
		}

		// Element non franchissable
		return Double.MAX_VALUE;
	}

	protected Double h(Node from, Node to) {
		/* Use the Manhattan distance heuristic. */
		return new Double(Math.abs(targetX - to.x) + Math.abs(targetY - to.y));
	}

	protected List<Node> generateSuccessors(Node node) {
		// On recherche dans les 4 directions possibles de déplacement
		final List<Node> nodes = new LinkedList<Node>();
		final int x = node.x;
		final int y = node.y;

		// FIXME Améliorer la prise en compte du côté traversable des éléments
		{
			final Element element = level.getElement(x, y + 1, false);
			
			if ((element != null) && !element.isConcrete()) {
				nodes.add(new Node(x, y + 1));
			}
		}
		{
			final Element element = level.getElement(x, y - 1, false);
			
			if ((element != null) && !element.isConcrete()) {
				nodes.add(new Node(x, y - 1));
			}
		}

		// FIXME Améliorer la prise en compte du côté traversable des éléments
		{
			final Element element = level.getElement(x + 1, y, false);
			
			if ((element != null) && !element.isConcrete()) {
				nodes.add(new Node(x + 1, y));
			}
		}
		{
			final Element element = level.getElement(x - 1, y, false);
			
			if ((element != null) && !element.isConcrete()) {
				nodes.add(new Node(x - 1, y));
			}
		}

		return nodes;
	}

	public static void main(String[] args) {
		final Level level = new Dungeon().createLevel(1, 10, 10);

		LevelPathFinder pf = new LevelPathFinder(level, 7, 8);

		long begin = System.nanoTime();

		List<Node> nodes = pf.compute(new LevelPathFinder.Node(1, 1));

		System.out.println("Time = " + ((System.nanoTime() - begin) / 1000000)
				+ " ms");
		System.out.println("Expanded = " + pf.getExpandedCounter());
		System.out.println("Cost = " + pf.getCost());

		if (nodes == null)
			System.out.println("No path");
		else {
			System.out.print("Path = ");
			for (Node n : nodes)
				System.out.print(n);
			System.out.println();
		}
	}

}