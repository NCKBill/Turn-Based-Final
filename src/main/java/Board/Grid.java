package Board;

import Unit.Unit;

import java.util.*;

public class Grid {
    private final Cell[][] grid;
    private final int rows;
    private final int columns;
    private final int[][] direction = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    public Grid(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        this.grid = new Cell[rows][columns];
        initializeGrid();
    }

    private void initializeGrid() {
        Random random = new Random();
        int rand = random.nextInt(4); // Preset 4 maps in Maps.java, numbered from 0 to 3
        
        Maps map = new Maps(rand, this.rows, this.columns);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                grid[i][j] = new Cell(i, j, map.getMap()[i][j]);
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

    // Wrapper class
    private static class PathNode implements Comparable<PathNode> {
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
     *
     * @param start x and y coordinate of the current unit
     * @param end   x and y coordinate of the selected cell
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
                Cell gridNeighbor = getCell(nextX, nextY);

                if (gridNeighbor != null) {
                    if (gridNeighbor.isOccupied() && !gridNeighbor.equals(end)) continue;
                    
                    int moveCost = gridNeighbor.getTerrainCost();
                    // Prevent entering impassable terrain (Wall)
                    if (moveCost == Integer.MAX_VALUE) continue;

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
     *
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

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    /**
     * Calculate all reachable Cells within the unit's MP limit
     *
     * @param startCell Origin cell of the Unit
     * @param mpLimit   MP of the Unit
     * @return a list of all Cells the unit can reach within the movement limit
     */
    public List<Cell> getReachableCells(Cell startCell, int mpLimit) {
        // Map tracks visited cells & their distance
        Map<Cell, Integer> bestCosts = new HashMap<>();
        PriorityQueue<PathNode> queue = new PriorityQueue<>();

        bestCosts.put(startCell, 0);
        queue.add(new PathNode(startCell, 0, null));

        while (!queue.isEmpty()) {
            PathNode current = queue.poll();
            Cell currentCell = current.cell;

            for (int[] dir : direction) {
                int newRow = currentCell.getRow() + dir[0];
                int newCol = currentCell.getCol() + dir[1];
                Cell neighbor = getCell(newRow, newCol);

                // Check if valid and walkable
                if (neighbor != null && neighbor.getUnit() == null) {
                    int moveCost = neighbor.getTerrainCost();
                    // Prevent entering impassable terrain (Wall)
                    if (moveCost == Integer.MAX_VALUE) continue;

                    int totalCost = current.cost + moveCost;

                    if (totalCost <= mpLimit && totalCost < bestCosts.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                        bestCosts.put(neighbor, totalCost);
                        queue.add(new PathNode(neighbor, totalCost, current));
                    }
                }
            }
        }

        // Remove start cell to prevent unit from moving in place
        bestCosts.remove(startCell);

        return new ArrayList<>(bestCosts.keySet());

    }
}
