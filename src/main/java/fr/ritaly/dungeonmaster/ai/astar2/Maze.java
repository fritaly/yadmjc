package fr.ritaly.dungeonmaster.ai.astar2;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Maze {

	private int rows;
	private int columns;
	private Square[][] elements;
	private Square goal;
	private static final String CLOSED_TOP = "+ - ";
	private static final String OPEN_TOP = "+   ";
	private static final String CLOSED_LEFT = "|   ";
	private static final String CLOSED_LEFT_PATH = "| . ";
	private static final String CLOSED_LEFT_START = "| S ";
	private static final String CLOSED_LEFT_GOAL = "| G ";
	private static final String OPEN_LEFT = "    ";
	private static final String OPEN_LEFT_PATH = "  . ";
	private static final String OPEN_LEFT_START = "  S ";
	private static final String OPEN_LEFT_GOAL = "  G ";

	private List<Square> opened = new ArrayList<Square>();
	private List<Square> closed = new ArrayList<Square>();
	private List<Square> bestList = new ArrayList<Square>();

	public Maze(int rows, int columns) {

		this.rows = rows;
		this.columns = columns;
		elements = new Square[rows][columns];
		init();
	}

	private void init() {

		createSquares();
		setStartAndGoal();
		generateAdjacenies();
	}

	public int getRows() {

		return rows;
	}

	public int getColumns() {

		return columns;
	}

	private void setStartAndGoal() {

		elements[0][0].setStart(true);
		Random random = new Random();
		int goalX = 0, goalY = 0;
		while (goalX == 0 && goalY == 0) {
			goalX = random.nextInt(rows);
			goalY = random.nextInt(columns);
		}
		goal = elements[goalX][goalY];
		goal.setEnd(true);
	}

	private void generateAdjacenies() {

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				elements[i][j].calculateAdjacencies();
			}
		}
	}

	private void createSquares() {

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				elements[i][j] = new Square(i, j, this);
			}
		}
	}

	public Square getSquare(int x, int y) {

		return elements[x][y];
	}

	public void setSquare(Square square) {

		elements[square.getX()][square.getY()] = square;
	}

	public void draw() {

		System.out.println("Drawing maze");
		drawContents();
		drawBorder();
	}

	private void drawContents() {

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				Square square = elements[i][j];
				drawTop(square);
			}
			System.out.println("+");

			for (int j = 0; j < columns; j++) {
				Square square = elements[i][j];
				drawLeft(square);
			}
			System.out.println("|");
		}
	}

	private void drawLeft(Square square) {

		int x = square.getX();
		int y = square.getY();

		if (y - 1 < 0) {
			if (square.isStart()) {
				System.out.print(CLOSED_LEFT_START);
				return;
			}

			if (square.isEnd()) {
				System.out.print(CLOSED_LEFT_GOAL);
				return;
			}

			if (bestList.contains(square)) {
				System.out.print(CLOSED_LEFT_PATH);
				return;
			}
			System.out.print(CLOSED_LEFT);
			return;
		}

		for (Square neighbor : square.getAdjacencies()) {
			if (neighbor.getX() == x && neighbor.getY() == y - 1) {
				if (square.isEnd()) {
					System.out.print(OPEN_LEFT_GOAL);
					return;
				}
				if (bestList.contains(square)) {
					System.out.print(OPEN_LEFT_PATH);
					return;
				}
				System.out.print(OPEN_LEFT);
				return;
			}
		}

		if (square.isEnd()) {
			System.out.print(CLOSED_LEFT_GOAL);
			return;
		}

		if (bestList.contains(square)) {
			System.out.print(CLOSED_LEFT_PATH);
			return;
		}
		System.out.print(CLOSED_LEFT);

	}

	private void drawTop(Square square) {

		int x = square.getX();
		int y = square.getY();

		if (x == 0) {
			System.out.print(CLOSED_TOP);
			return;
		}

		for (Square neighbor : square.getAdjacencies()) {
			if (neighbor.getX() == x - 1 && neighbor.getY() == y) {
				System.out.print(OPEN_TOP);
				return;
			}
		}

		System.out.print(CLOSED_TOP);
	}

	private void drawBorder() {

		for (int i = 0; i < columns; i++) {
			System.out.print(CLOSED_TOP);
		}
		System.out.println("+");
	}

	public void findBestPath() {

		System.out.println("Calculating best path...");
		Set<Square> adjacencies = elements[0][0].getAdjacencies();
		for (Square adjacency : adjacencies) {
			adjacency.setParent(elements[0][0]);
			if (adjacency.isStart() == false) {
				opened.add(adjacency);
			}
		}

		while (opened.size() > 0) {
			Square best = findBestPassThrough();
			opened.remove(best);
			closed.add(best);
			if (best.isEnd()) {
				System.out.println("Found Goal");
				populateBestList(goal);
				draw();
				return;
			} else {
				Set<Square> neighbors = best.getAdjacencies();
				for (Square neighbor : neighbors) {
					if (opened.contains(neighbor)) {
						Square tmpSquare = new Square(neighbor.getX(),
								neighbor.getY(), this);
						tmpSquare.setParent(best);
						if (tmpSquare.getPassThrough(goal) >= neighbor
								.getPassThrough(goal)) {
							continue;
						}
					}

					if (closed.contains(neighbor)) {
						Square tmpSquare = new Square(neighbor.getX(),
								neighbor.getY(), this);
						tmpSquare.setParent(best);
						if (tmpSquare.getPassThrough(goal) >= neighbor
								.getPassThrough(goal)) {
							continue;
						}
					}
					
					
					neighbor.setParent(best);

					opened.remove(neighbor);
					closed.remove(neighbor);
					opened.add(0, neighbor);
				}
			}
		}

		System.out.println("No Path to goal");
	}

	private void populateBestList(Square square) {

		bestList.add(square);
		if (square.getParent().isStart() == false) {
			populateBestList(square.getParent());
		}

		return;
	}

	private Square findBestPassThrough() {

		Square best = null;
		for (Square square : opened) {
			if (best == null
					|| square.getPassThrough(goal) < best.getPassThrough(goal)) {
				best = square;
			}
		}

		return best;
	}

}
