package uk.ac.ed.inf.Pathfinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import uk.ac.ed.inf.LongLat;

/**
 * This class represents each node on the virtual grid needed for the pathfinding algorithm. The virtual grid is
 * conceptually placed over the drone confinement area, and the granularity of the grid is defined as EPSILON.
 * Each GridNode holds all information required by the Theta-star pathfinding algorithm, as well as some utility
 * methods.
 */

public class GridNode {

    private static final double HEURISTIC_BIAS = 1;
    public static final List<List<Integer>> DISPLACEMENT_MATRIX = List.of(
            List.of(-1, 0),
            List.of(0, -1),
            List.of(1, 0),
            List.of(0, 1),
            List.of(1, -1),
            List.of(-1, 1),
            List.of(1, 1),
            List.of(-1, -1)
    );

    public final int row;
    public final int col;
    public final LongLat coordinates;
    public GridNode parent;
    public double totalScore;
    public double distanceScore;    /* heuristic -> Distance from target */
    public double scoreFromStart;   /* g_score -> Distance so far */
    public boolean isWalkable;

    public GridNode(int row, int col, LongLat coordinates, boolean isWalkable) {
        this.row = row;
        this.col = col;
        this.coordinates = coordinates;
        this.isWalkable = isWalkable;
        this.scoreFromStart = Double.MAX_VALUE;
    }

    /**
     * Updates the Distance score (heuristic) of the GridNode. The distance score is measured by the distance from the
     * current node to the destination node.
     * @param end the destination node.
     */
    public void calcDistanceScore(GridNode end) {
        this.distanceScore = coordinates.distanceTo(end.coordinates);
    }

    /**
     * Updates the Score from start (g score) of the GridNode. The score from start for any node is its distance from
     * its parent added with the parent's score from start.
     */
    public void calcScoreFromStart() {
        if (parent == null) {
            scoreFromStart = 0;
            throw new NullPointerException("Error: Score for node without parent was attempted to be calculated.");
        } else {
            scoreFromStart = parent.scoreFromStart + this.coordinates.distanceTo(parent.coordinates);
        }
    }

    /**
     * Updates the total score of the GridNode. The total score is the sum of the score from start and the biased
     * distance score. The higher the bias the more the Algorithm acts like the Best First Search greedy algorithm, and
     * as the bias approaches zero, it behaves more and more like Dijkstra's algorithm for pathfinding.
     */
    public void calcTotalScore() {
        totalScore = (distanceScore * HEURISTIC_BIAS) + scoreFromStart;
    }

    /**
     * Provides a list of GridNodes that are adjacent to the parent node. Each node has 8 adjacent nodes unless it is
     * at the boundaries of the grid.
     * @return a list of GridNode Objects that are adjacent to the parent node.
     */
    public List<GridNode> getNeighbours() {
        List<GridNode> neighbours = new ArrayList<>();
        int row = this.row;
        int column = this.col;

        for (List<Integer> vector : DISPLACEMENT_MATRIX) {
            int tempRow = vector.get(0);
            int tempColumn = vector.get(1);

            if (isValidNeighbour(row + tempRow, column + tempColumn)) {
                GridNode currentNode = Pathfinder.virtualGrid.get(row + tempRow).get(column + tempColumn);
                if (currentNode.isWalkable) {
                    neighbours.add(currentNode);
                }
            }
        }
        return neighbours;
    }

    /**
     * Returns true if the given row and column pair are inside the virtual grid bounds.
     * @param row the row number to be checked.
     * @param column the column number to be checked.
     * @return true if given row/column pair is valid, false otherwise.
     */
    private boolean isValidNeighbour(int row, int column) {
        return ((row >= 0 && row < Pathfinder.virtualGrid.size())
                && (column >= 0 && column < Pathfinder.virtualGrid.get(0).size()));
    }

    @Override
    public String toString() {
        int parentRow;
        int parentCol;
        if (parent == null) {
            parentRow = -1;
            parentCol = -1;
        } else {
            parentRow = parent.row;
            parentCol = parent. col;
        }
        return "GridNode{" +
                "row=" + row +
                ", col=" + col +
                //", coordinates=" + coordinates +
                ", parent (row, col)= (" + parentRow + ", " + parentCol + ")" +
                //", visited=" + visited +
                //", totalScore=" + totalScore +
                ", isWalkable=" + isWalkable +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GridNode gridNode = (GridNode) o;
        return row == gridNode.row && col == gridNode.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
}