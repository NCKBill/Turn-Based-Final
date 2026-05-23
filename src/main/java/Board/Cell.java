package Board;
import Unit.Unit;

/**
 * Represents a single square in the game
 * Each Cell is capable of holding a Unit/Terrain
 */
public class Cell {
    private Unit occupant;
    private int row;
    private int col;
    private int terrainType;

    public Cell(int row, int col, int terrainType) {
        this.row = row;
        this.col = col;
        this.terrainType = terrainType; // Terrain types: 0 (normal/grass), 1 (water), 2 (trees/woods), 3 (mountain/wall)
        this.occupant = null;
    }

    //Checks if the cell currently holds a unit
    public boolean isOccupied() {
        return occupant != null;
    }

    public Unit getUnit() {
        return occupant;
    }

    public void setUnit(Unit occupant) {
        this.occupant = occupant;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getTerrainType() {
        return terrainType;
    }

    public int getTerrainCost() {
        // Terrain types: 0 (normal/grass), 1 (water), 2 (trees/woods), 3 (mountain/wall)
        int cost = 0;
        switch (this.terrainType) {
            case 0: cost = 1; break;
            case 1: cost = 2; break;
            case 2: cost = 2; break;
            case 3: cost = Integer.MAX_VALUE; break;
        }

        return cost;
    }
}
