package fr.ritaly.dungeonmaster.ai.astar;

/*    
 * A* algorithm implementation.
 * Copyright (C) 2007, 2009 Giuseppe Scrivano <gscrivano@gnu.org>

 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses/>.
 */

import java.util.*;

public class PathFinder extends AStar<PathFinder.Node> {
	private int[][] map;

	public static class Node {
		public int x;
		public int y;

		Node(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public String toString() {
			return "(" + x + ", " + y + ") ";
		}
	}

	public PathFinder(int[][] map) {
		this.map = map;
	}

	protected boolean isGoal(Node node) {
		return (node.x == map[0].length - 1) && (node.y == map.length - 1);
	}

	protected Double g(Node from, Node to) {

		if (from.x == to.x && from.y == to.y)
			return 0.0;

		if (map[to.y][to.x] == 1)
			return 1.0;

		return Double.MAX_VALUE;
	}

	protected Double h(Node from, Node to) {
		/* Use the Manhattan distance heuristic. */
		return new Double(Math.abs(map[0].length - 1 - to.x)
				+ Math.abs(map.length - 1 - to.y));
	}

	protected List<Node> generateSuccessors(Node node) {
		List<Node> ret = new LinkedList<Node>();
		int x = node.x;
		int y = node.y;
		if (y < map.length - 1 && map[y + 1][x] == 1)
			ret.add(new Node(x, y + 1));

		if (x < map[0].length - 1 && map[y][x + 1] == 1)
			ret.add(new Node(x + 1, y));

		return ret;
	}

	public static void main(String[] args) {
		int[][] map = new int[][] { { 1, 1, 1, 1, 1, 1, 1, 1, 1 },
				{ 0, 0, 1, 0, 0, 0, 0, 0, 1 }, { 1, 1, 1, 1, 0, 1, 1, 0, 1 },
				{ 1, 1, 1, 1, 1, 1, 1, 0, 0 }, { 1, 0, 0, 0, 0, 1, 0, 0, 0 },
				{ 1, 1, 1, 1, 0, 1, 1, 1, 1 }, { 1, 1, 1, 1, 0, 1, 0, 0, 1 },
				{ 1, 1, 1, 1, 0, 1, 0, 0, 1 }, { 1, 1, 1, 1, 0, 1, 0, 0, 1 },
				{ 1, 1, 1, 1, 0, 1, 0, 0, 0 }, { 1, 1, 1, 1, 0, 1, 1, 1, 1 }, };
		PathFinder pf = new PathFinder(map);

		System.out
				.println("Find a path from the top left corner to the right bottom one.");

		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[0].length; j++)
				System.out.print(map[i][j] + " ");
			System.out.println();
		}

		long begin = System.currentTimeMillis();

		List<Node> nodes = pf.compute(new PathFinder.Node(0, 0));

		long end = System.currentTimeMillis();

		System.out.println("Time = " + (end - begin) + " ms");
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