package uk.ac.ed.inf.Pathfinding;

import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

import uk.ac.ed.inf.LongLat;
import uk.ac.ed.inf.Parser;
import uk.ac.ed.inf.Utils.Utils;

import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.*;
import java.util.stream.Collectors;


/*
  * This class contains the pathfinding algorithm that dictates the path of the drone for a given order.
 */
public class Pathfinder {
    public static final List<LongLat> CONFINEMENT_AREA = new ArrayList<>(List.of(
            new LongLat(-3.192473, 55.946233), // FORREST HILL TOP LEFT
            new LongLat(-3.184319, 55.946233), // KFC TOP RIGHT
            new LongLat(-3.192473, 55.942617), // MEADOWS BOT LEFT
            new LongLat(-3.184319, 55.942617))); // BUS STOP BOT RIGHT
    private static final double C_AREA_LENGTH_X =
            Math.abs(CONFINEMENT_AREA.get(0).longitude - CONFINEMENT_AREA.get(1).longitude);
    private static final double C_AREA_LENGTH_Y =
            Math.abs(CONFINEMENT_AREA.get(0).latitude - CONFINEMENT_AREA.get(2).latitude);
    public static final double EPSILON = LongLat.CLOSE_DISTANCE / 5 ;

    public static List<List<GridNode>> virtualGrid;
    public List<Polygon> noFlyZones;

    public Pathfinder() {
        this.noFlyZones = Parser.getNoFlyZones();
        generateGrid();
    }

    /**
     * Form a path that avoids no-fly-zones given a start and a destination.
     *
     * @param start the starting coordinates.
     * @param dest the destination coordinates.
     * @return a list of LongLat objects.
     */
    public List<LongLat> findPath(LongLat start, LongLat dest) {
        resetGrid();
        int[] startIndices = getRowColFromLongLat(start);
        int[] destIndices = getRowColFromLongLat(dest);
        //System.out.println(Arrays.toString(startIndices));
        GridNode startNode = virtualGrid.get(startIndices[0]).get(startIndices[1]);
        GridNode destNode = virtualGrid.get(destIndices[0]).get(destIndices[1]);
        if (!destNode.coordinates.closeTo(dest)) {
            System.out.println("problem!");
        }
        List<GridNode> nodeList = findPathOnGrid(startNode, destNode);
        Collections.reverse(nodeList);
        return nodeList.stream()
                .map(node -> node.coordinates)
                .collect(Collectors.toList());
    }

    /**
     * This method implements the Theta-star (θ*) algorithm that finds a near optimal any-angle path between two points.
     * This algorithm is a modified version of the A-star pathfinding algorithm.
     * @param start the starting node.
     * @param end the end node.
     * @return A list of nodes that form a near-optimal path.
     */
    public List<GridNode> findPathOnGrid(GridNode start, GridNode end) {
        PriorityQueue<GridNode> openQueue =
                new PriorityQueue<>(300, Comparator.comparingDouble(node -> node.totalScore));
        List<GridNode> closedList = new ArrayList<>();
        start.scoreFromStart = 0;
        openQueue.add(start);
        GridNode currentNode;

        while (!openQueue.isEmpty()) {
            /* Get the head of the queue and remove it from the list */
            currentNode = openQueue.poll();

            /* if current node is the destination, generate route and return it */
            if (currentNode.equals(end)) {
                return reconstructPath(end);
            }

            /* Add currentNode to the closedList and consider its neighbours */
            closedList.add(currentNode);
            List<GridNode> neighbours = currentNode.getNeighbours();

            /* For every neighbour,  */
            for (GridNode neighbour : neighbours) {

                /* If the neighbour is already in open list or closed list through a shorter path, skip it */
                double newDistFromParent = neighbour.coordinates.distanceTo(currentNode.coordinates);

                if (closedList.contains(neighbour) &&
                        neighbour.scoreFromStart < currentNode.scoreFromStart + newDistFromParent) {
                    continue;
                }
                if (Utils.queueContains(openQueue, neighbour) &&
                        neighbour.scoreFromStart < currentNode.scoreFromStart + newDistFromParent) {
                    continue;
                }

                /* If the neighbour has line of sight with the parent of the current node, ignore current node */
                if ( currentNode.parent != null && lineOfSight(currentNode.parent.coordinates, neighbour.coordinates)) {
                    neighbour.parent = currentNode.parent;
                    //System.out.printf("Skipped: (%d, %d) -> (%d, %d)\n", currentNode.row, currentNode.col, neighbour.row, neighbour.col);
                } else {
                    neighbour.parent = currentNode;
                    //System.out.printf("Not skipped: (%d, %d) -> (%d, %d)\n", currentNode.row, currentNode.col, neighbour.row, neighbour.col);
                }

                /* Update neighbour scores*/
                neighbour.calcDistanceScore(end); // h(n), the heuristic
                neighbour.calcScoreFromStart(); // g(n)
                neighbour.calcTotalScore(); // h(n) + g(n)

                /* If block has not been visited before, add it to the open queue */
                if (!Utils.queueContains(openQueue, neighbour) && !closedList.contains(neighbour)) {
                    openQueue.add(neighbour);
                }
            }
        }
        System.err.println("Pathfinder could not find path");
        return List.of(end);
    }

    /**
     * Populates the virtual grid with initialised GridNode objects.
     */
    private void generateGrid() {
        virtualGrid = new ArrayList<>();
        int gridSizeY = (int) Math.round(C_AREA_LENGTH_Y / EPSILON);
        int gridSizeX = (int) Math.round(C_AREA_LENGTH_X / EPSILON);

        for (int row = 0; row < gridSizeY; row++) {
            virtualGrid.add(new ArrayList<>());
            for (int col = 0; col < gridSizeX; col++) {
                virtualGrid.get(row).add(createNode(row, col));
            }
        }
    }

    /**
     * Resets the parent and scoreFromStart of every node back to default values. This function must be called every
     * time the Pathfinder must find a new path.
     */
    public void resetGrid() {
        for (List<GridNode> row : virtualGrid) {
            for (GridNode node : row) {
                node.parent = null;
                node.scoreFromStart = Double.MAX_VALUE;
            }
        }
    }

    /**
     * Given a node, reconstruct the path from the given node to the start by repeatedly moving through the parents
     * of the node.
     * @param node the GridNode where the path starts.
     * @return a list of GridNode objects that form a path from the destination to the start.
     */
    public List<GridNode> reconstructPath(GridNode node) {
        List<GridNode> path = new ArrayList<>();
        GridNode current = node;
        while (current != null) {
            if (path.contains(current)) {
                System.out.println("Found duplicate while reconstructing path (infinite loop)");
                return path;
            }
            path.add(current);
            current = current.parent;
        }
        return path;
    }

    /**
     * Determines if a given position is walkable by taking into account the No-Fly-Zones found on the server. A
     * position is not walkable if it is inside a No-Fly_Zone or if it is outside the drone confinement area.
     * To determine whether a LongLat is walkable, the java.awt.Path2D class is used to model the no-fly-zone polygons,
     * and the built-in contains method is used to check if any edges of the GridNode that surrounds the point is
     * inside any no-fly-zone polygon.
     * @param longLat the position to be tested.
     * @return True if the drone can move to the GridNode at the given position, false otherwise.
     */
    public boolean isLongLatWalkable(LongLat longLat) {
        Objects.requireNonNull(noFlyZones);
        List<LongLat> nodeEdges;
        nodeEdges = getEdgePoints(longLat);

        boolean insideNoFlyZone = false;

        /* For each polygon, check if any of the edges of the GridNode are inside it */
        for (Polygon polygon : noFlyZones) {
            List<Point> pts = polygon.outer().coordinates();
            Path2D path2D = new Path2D.Double();
            path2D.moveTo(pts.get(0).longitude(), pts.get(0).latitude());
            for (int i = 1; i < pts.size(); i++) {
                path2D.lineTo(pts.get(i).longitude(), pts.get(i).latitude());
            }

            for (LongLat edgePoint : nodeEdges) {
                insideNoFlyZone = insideNoFlyZone || path2D.contains(edgePoint.longitude, edgePoint.latitude);
            }
        }

        boolean outOfBounds = longLat.latitude <= LongLat.MIN_LATITUDE
                || longLat.latitude >= LongLat.MAX_LATITUDE
                || longLat.longitude <= LongLat.MIN_LONGITUDE
                || longLat.longitude >= LongLat.MAX_LONGITUDE;

        return !insideNoFlyZone && !outOfBounds;
    }

    /**
     * Checks whether there is line of sight between two LongLat points. Line of sight can be interrupted by noFlyZones.
     * Line of sight is checked by considering if all corners of the GridNode which surrounds LongLat "a" have line
     * of sight with each corresponding corner of the GridNode surrounding LongLat "b".
     * @param a the LongLat representing the start of the segment.
     * @param b the LongLat representing the end of the segment.
     * @return True if there is line of sight between the start and end.
     */
    public boolean lineOfSight(LongLat a, LongLat b) {
        Objects.requireNonNull(noFlyZones);
        List<LongLat> edgePointsA;
        List<LongLat> edgePointsB;
        edgePointsA = getEdgePoints(a);
        edgePointsB = getEdgePoints(b);
        double x_1, y_1, x_2, y_2;

        for (int i = 0; i < edgePointsA.size(); i++) {
            x_1 = edgePointsA.get(i).longitude;
            y_1 = edgePointsA.get(i).latitude;
            x_2 = edgePointsB.get(i).longitude;
            y_2 = edgePointsB.get(i).latitude;
            Line2D ray = new Line2D.Double(x_1, y_1, x_2, y_2);

            for (Polygon poly : noFlyZones) {
                List<Point> points = poly.outer().coordinates();
                for (int j = 0; j < points.size() - 1; j++) {
                    Line2D edge = new Line2D.Double(
                            points.get(j).longitude(),
                            points.get(j).latitude(),
                            points.get(j + 1).longitude(),
                            points.get(j + 1).latitude()
                    );

                    if (ray.intersectsLine(edge)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Create a new GridNode and initialise its instance variables.
     *
     * @param row the row of the GridNode on the virtual grid.
     * @param col the column of the GridNode on the virtual grid.
     * @return the newly created GridNode.
     */
    private GridNode createNode(int row, int col) {
        LongLat longLat = createNodeLongLat(row, col);
        boolean isWalkable = isLongLatWalkable(longLat);
        return new GridNode(row, col, longLat, isWalkable);
    }

    /**
     * Creates and returns a LongLat object that represents the node of the given row and column. This is essentially
     * where the virtual Grid is mapped to  real world coordinates.
     *
     * @param row the column of the node on the virtual grid.
     * @param col the row of the node on the virtual grid.
     * @return a LongLat object that represents the node with the given row/column.
     */
    private LongLat createNodeLongLat(int row, int col) {
        double latitude = LongLat.MIN_LATITUDE + row * EPSILON + (EPSILON / 2);
        double longitude = LongLat.MIN_LONGITUDE + col * EPSILON + (EPSILON / 2);
        return new LongLat(longitude, latitude);
    }

    /**
     * Maps any LongLat position to its GridNode's coordinates inside the virtual grid.
     *
     * @param pos the LongLat position.
     * @return an array of two integers which are the row and column of the GridNode that contains the given position.
     */
    public static int[] getRowColFromLongLat(LongLat pos) {
        int row = (int) Math.round((pos.latitude - LongLat.MIN_LATITUDE - EPSILON/2) / EPSILON);
        int col = (int) Math.round((pos.longitude - LongLat.MIN_LONGITUDE - EPSILON/2) / EPSILON);
        //System.out.printf("got row,col: %d,%d\n", row, col);
        return new int[]{row, col};
    }

    /**
     * Returns a list of LongLat objects representing the bounding box of the GridNode that the LongLat given
     * belongs to.
     *
     * @param longLat A LongLat object.
     * @return A list of LongLat Objects.
     */
    public List<LongLat> getEdgePoints(LongLat longLat) {
        List<LongLat> edgePoints = new ArrayList<>();
        edgePoints.add(new LongLat(longLat.longitude + EPSILON/2, longLat.latitude + EPSILON/2));
        edgePoints.add(new LongLat(longLat.longitude + EPSILON/2, longLat.latitude - EPSILON/2));
        edgePoints.add(new LongLat(longLat.longitude - EPSILON/2, longLat.latitude + EPSILON/2));
        edgePoints.add(new LongLat(longLat.longitude - EPSILON/2, longLat.latitude - EPSILON/2));
        return edgePoints;
    }
}
