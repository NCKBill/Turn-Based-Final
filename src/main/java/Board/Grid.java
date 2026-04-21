package Board;

import Unit.Unit;

import java.util.*;

public class Grid {
    private Cell[][] grid;
    private int rows;
    private int columns;
    private int[][] direction = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    public Grid(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        this.grid = new Cell[rows][columns];
        initializeGrid();
    }

    private void initializeGrid() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                grid[i][j] = new Cell(i, j, "Normal");
            }
        }
    }

    public Cell getCell(int x, int y) {
        if (isValidCoordinate(x, y)) {
            return grid[x][y];
        }
        return null;
    }

    public Cell getCell(Unit unit) {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                Cell current = grid[r][c];
                if (current != null && current.isOccupied() && current.getUnit() == unit) {
                    return current;
                }
            }
        }
        return null;
    }

    public boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < rows && y >= 0 && y < columns;
    }

    private class PathNode implements Comparable<PathNode> {
        Cell cell;
        int cost;
        PathNode parent;

        public PathNode(Cell cell, int cost, PathNode parent) {
            this.cell = cell;
            this.cost = cost;
            this.parent = parent;
        }

        @Override
        public int compareTo(PathNode other) {
            return Integer.compare(this.cost, other.cost);
        }
    }

    /**
     * Calculate the shortest path between starting cell and end cell using Dijkstra algorithm
     * @param start x and y coordinate of the current unit
     * @param end x and y coordinate of the selected cell
     * @return List of Cells representing the best path to take
     */
    public List<Cell> calculatePathDijkstra(Cell start, Cell end) {
        PriorityQueue<PathNode> queue = new PriorityQueue<>();
        Map<Cell, Integer> bestCostMap = new HashMap<>();

        PathNode startNode = new PathNode(start, 0, null);
        bestCostMap.put(start, 0);
        queue.add(startNode);

        while (!queue.isEmpty()) {
            PathNode current = queue.poll();
            Cell currentCell = current.cell;

            if (currentCell.equals(end)) {
                return pathConstruct(current);
            }

            for (int[] dir : direction) {
                int nextX = currentCell.getRow() + dir[0];
                int nextY = currentCell.getCol() + dir[1];

                if (isValidCoordinate(nextX, nextY)) {
                    Cell gridNeighbor = getCell(nextX, nextY);

                    if (gridNeighbor.isOccupied() && !gridNeighbor.equals(end)) continue;

                    int moveCost = 1;
                    int totalCost = current.cost + moveCost;

                    // Compare total cost of current path found to previously recorded path of the neighbour
                    if (totalCost < bestCostMap.getOrDefault(gridNeighbor, Integer.MAX_VALUE)) {
                        bestCostMap.put(gridNeighbor, totalCost);
                        queue.add(new PathNode(gridNeighbor, totalCost, current));
                    }
                }
            }
        }
        return new ArrayList<>();
    }

    /**
     * Turn PathNode into List of Cells to represent path
     * @param targetNode Root node
     * @return List of children Cells of the root node
     */
    private List<Cell> pathConstruct(PathNode targetNode) {
        List<Cell> path = new ArrayList<>();
        PathNode current = targetNode;

        while (current != null) {
            path.add(current.cell);
            current = current.parent;
        }

        Collections.reverse(path);
        return path;
    }

    public Cell[][] getGrid() {
        return grid;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    // for test printing grid
    public void display() {
        for (int x = 0; x < columns; x++) {
            for (int y = 0; y < rows; y++) {
                String occupant = "";
                if (grid[x][y].isOccupied()) {
                    if (grid[x][y].getUnit().isFriendly()) {
                        occupant = "O";
                    } else {
                        occupant = "X";
                    }
                }
                System.out.print("[" + occupant + "]" + " ");
            }
            System.out.println();
        }
    }

    /**
     * Calculate all reachable Cells within the unit's MP limit
     * @param startCell Origin Cell of the Unit
     * @param mpLimit MP of the Unit
     * @return a list of all Cells the unit can reach within the movement limit
     */
    public List<Cell> getReachableCells(Cell startCell, int mpLimit) {
        // Map tracks visited cells & their distance
        Map<Cell, Integer> distances = new HashMap<>();
        Queue<Cell> queue = new LinkedList<>();

        distances.put(startCell, 0);
        queue.add(startCell);

        while (!queue.isEmpty()) {
            Cell current = queue.poll();
            int currentCost = distances.get(current);

            // Stop expanding if hit the limit MP
            if (currentCost >= mpLimit) continue;

            for (int[] dir : direction) {
                int newRow = current.getRow() + dir[0];
                int newCol = current.getCol() + dir[1];
                Cell neighbor = getCell(newRow, newCol);

                // Check if valid, walkable, and not visited
                if (neighbor != null && neighbor.getUnit() == null && !distances.containsKey(neighbor)) {
                    distances.put(neighbor, currentCost + 1);
                    queue.add(neighbor);
                }
            }
        }

        // Remove the start cell to prevent unit from moving in place
        distances.remove(startCell);

        return new ArrayList<>(distances.keySet());
    }
}