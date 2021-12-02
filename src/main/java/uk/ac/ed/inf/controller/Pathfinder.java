package uk.ac.ed.inf.controller;

import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

import uk.ac.ed.inf.domain.LongLat;

import java.awt.geom.Line2D;
import java.awt.geom.Path2D;

import java.util.*;
import java.util.stream.Collectors;

/*
  * This class contains the pathfinding algorithm that dictates the path of the drone for a given order.
 */
public class Pathfinder {
    private static final LongLat TOP_LEFT = new LongLat(-3.192473, 55.946233);  // FORREST HILL
    private static final LongLat TOP_RIGHT = new LongLat(-3.184319, 55.946233); // KFC
    private static final LongLat BOT_LEFT = new LongLat(-3.192473, 55.942617);  // MEADOWS
    private static final LongLat BOT_RIGHT = new LongLat(-3.184319, 55.942617); // BUS STOP

    private static final double C_AREA_LENGTH_X = Math.abs(TOP_LEFT.getLongitude() - TOP_RIGHT.getLongitude());
    private static final double C_AREA_LENGTH_Y = Math.abs(TOP_LEFT.getLatitude() - BOT_LEFT.getLatitude());
    public static final double EPSILON = LongLat.CLOSE_DISTANCE / 4; /* Determines the size of the grid cells */

    /**
     * The grid of nodes used by the Theta* algorithm.
     */
    private static List<List<GridNode>> virtualGrid;
    /**
     * The noFlyZones as a list of Polygons.
     */
    private final List<Polygon> noFlyZones;
    /**
     * The noFlyZones as a list of Line2D objects.
     */
    private final List<Line2D> noFlyZoneEdges = new ArrayList<>();
    /**
     * The noFlyZones as a list of Path2D objects.
     */
    private final List<Path2D> noFlyZonePaths = new ArrayList<>();

    public Pathfinder(List<Polygon> noFlyZones) {
        this.noFlyZones = noFlyZones;
        storeNoFlyZones(noFlyZones);
        generateGrid();
    }

    public static List<List<GridNode>> getVirtualGrid() {
        return virtualGrid;
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

        GridNode startNode = virtualGrid.get(startIndices[0]).get(startIndices[1]);
        GridNode destNode = virtualGrid.get(destIndices[0]).get(destIndices[1]);

        List<GridNode> nodeList = findPathOnGrid(startNode, destNode);
        Collections.reverse(nodeList);
        return nodeList.stream()
                .map(GridNode::getCoordinates)
                .collect(Collectors.toList());
    }

    /**
     * This method implements the Theta-star (θ*) algorithm that finds a near optimal any-angle path between two points.
     * This algorithm is a modified version of the A-star pathfinding algorithm.
     * @param start the starting node.
     * @param end the end node.
     * @return A list of nodes that form a near-optimal path.
     */
    private List<GridNode> findPathOnGrid(GridNode start, GridNode end) {
        PriorityQueue<GridNode> openQueue =
                new PriorityQueue<>(300, Comparator.comparingDouble(GridNode::getTotalScore));
        List<GridNode> closedList = new ArrayList<>();
        start.setScoreFromStart(0);
        openQueue.add(start);
        GridNode currentNode;

        while (!openQueue.isEmpty()) {
            /* Get the best node from the queue and remove it from the list */
            currentNode = openQueue.poll();

            /* if current node is the destination, generate route and return it */
            if (currentNode.equals(end)) {
                return reconstructPath(end);
            }

            /* Add currentNode to the closedList and consider its neighbours */
            closedList.add(currentNode);
            List<GridNode> neighbours = currentNode.getNeighbours();

            for (GridNode neighbour : neighbours) {

                /* If the neighbour is already in open list or closed list through a shorter path, skip it */
                double newDistFromParent = neighbour.getCoordinates().distanceTo(currentNode.getCoordinates());

                if (closedList.contains(neighbour) &&
                        neighbour.getScoreFromStart() < currentNode.getScoreFromStart() + newDistFromParent) {
                    continue;
                }
                if (openQueue.contains(neighbour) &&
                        neighbour.getScoreFromStart() < currentNode.getScoreFromStart() + newDistFromParent) {
                    continue;
                }


                /* If the neighbour has line of sight with the parent of the current node, ignore current node */
                if (currentNode.getParent() != null
                        && lineOfSight(currentNode.getParent().getCoordinates(), neighbour.getCoordinates())) {
                    neighbour.setParent(currentNode.getParent());
                } else {
                    neighbour.setParent(currentNode);
                }

                /* Update neighbour scores*/
                neighbour.calcDistanceScore(end); /* h(n), the heuristic */
                neighbour.calcScoreFromStart();   /* g(n)                */
                neighbour.calcTotalScore();       /* h(n) + g(n)         */

                /* If block has not been visited before, add it to the open queue */
                if (!openQueue.contains(neighbour) && !closedList.contains(neighbour)) {
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
    private void resetGrid() {
        for (List<GridNode> row : virtualGrid) {
            for (GridNode node : row) {
                node.setParent(null);
                node.setScoreFromStart(Double.MAX_VALUE);
            }
        }
    }

    /**
     * Converts the no-fly-zone polygons to Path2D and line2D objects and adds them to the noFlyZonePaths and
     * noFlyZoneEdges lists. There is some data duplication here, but it should not cause any memory issues due to its
     * size.
     * @param noFlyZones List of Polygon objects
     */
    private void storeNoFlyZones(List<Polygon> noFlyZones) {
        for (Polygon poly : noFlyZones) {
            List<Point> points = poly.outer().coordinates();
            Path2D path2D = new Path2D.Double();
            path2D.moveTo(points.get(0).longitude(), points.get(0).latitude());

            for (int i = 1; i < points.size(); i++) {
                path2D.lineTo(points.get(i).longitude(), points.get(i).latitude());
            }
            noFlyZonePaths.add(path2D);

            for (int j = 0; j < points.size() - 1; j++) {
                Line2D edge = new Line2D.Double(
                        points.get(j).longitude(),
                        points.get(j).latitude(),
                        points.get(j + 1).longitude(),
                        points.get(j + 1).latitude()
                );
                noFlyZoneEdges.add(edge);
            }
        }
    }

    /**
     * Given a node, reconstruct the path from the given node to the start by repeatedly moving through the parents
     * of the node.
     * @param node the GridNode where the path starts.
     * @return a list of GridNode objects that form a path from the destination to the start.
     */
    private List<GridNode> reconstructPath(GridNode node) {
        List<GridNode> path = new ArrayList<>();
        GridNode current = node;
        while (current != null) {
            if (path.contains(current)) {
                System.err.println("Found duplicate while reconstructing path (infinite loop)");
                return path;
            }
            path.add(current);
            current = current.getParent();
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
    private boolean isLongLatWalkable(LongLat longLat) {
        Objects.requireNonNull(noFlyZones);
        List<LongLat> nodeEdges;
        nodeEdges = getEdgePoints(longLat);

        boolean insideNoFlyZone = false;
        /* For each polygon, check if any of the edges of the GridNode are inside it */
        for (Path2D path2D : noFlyZonePaths) {
            for (LongLat edgePoint : nodeEdges) {
                insideNoFlyZone = insideNoFlyZone || path2D.contains(edgePoint.getLongitude(), edgePoint.getLatitude());
            }
        }

        boolean outOfBounds = longLat.getLatitude() <= LongLat.MIN_LATITUDE
                || longLat.getLatitude() >= LongLat.MAX_LATITUDE
                || longLat.getLongitude() <= LongLat.MIN_LONGITUDE
                || longLat.getLongitude() >= LongLat.MAX_LONGITUDE;

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
        double x_1, y_1, x_2, y_2;

        /* Cast a ray between the two points*/
        x_1 = a.getLongitude();
        y_1 = a.getLatitude();
        x_2 = b.getLongitude();
        y_2 = b.getLatitude();
        Line2D ray = new Line2D.Double(x_1, y_1, x_2, y_2);

        /* If the ray intersects with any no-fly-zone edge, there is no line of sight */
        for (Line2D edge : noFlyZoneEdges) {
            if (ray.intersectsLine(edge)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks whether a move from the current LongLat with the given angle is valid.
     *
     * @param currentPos the current LongLat
     * @param angle the angle of movement.
     * @return True if a move towards the given angle would be valid, False otherwise.
     */
    public boolean canMoveTowards(LongLat currentPos, int angle) {
        LongLat testPos = currentPos.nextPosition(angle);
        int[] rowCol = getRowColFromLongLat(testPos);
        boolean walkable = virtualGrid.get(rowCol[0]).get(rowCol[1]).isWalkable();
        boolean lineOfSight = lineOfSight(currentPos, testPos);

        return  walkable && lineOfSight;
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
     * Returns a list of LongLat objects representing the bounding box of the GridNode that the LongLat given
     * belongs to.
     *
     * @param longLat A LongLat object.
     * @return A list of LongLat Objects.
     */
    private static List<LongLat> getEdgePoints(LongLat longLat) {
        List<LongLat> edgePoints = new ArrayList<>();
        edgePoints.add(new LongLat(
                longLat.getLongitude() + EPSILON/2, longLat.getLatitude() + EPSILON/2));
        edgePoints.add(new LongLat(
                longLat.getLongitude() + EPSILON/2, longLat.getLatitude() - EPSILON/2));
        edgePoints.add(new LongLat(
                longLat.getLongitude() - EPSILON/2, longLat.getLatitude() + EPSILON/2));
        edgePoints.add(new LongLat(
                longLat.getLongitude() - EPSILON/2, longLat.getLatitude() - EPSILON/2));
        return edgePoints;
    }

    /**
     * Maps any LongLat position to its GridNode's coordinates inside the virtual grid.
     *
     * @param pos the LongLat position.
     * @return an array of two integers which are the row and column of the GridNode that contains the given position.
     */
    public static int[] getRowColFromLongLat(LongLat pos) {
        int row = (int) Math.round((pos.getLatitude() - LongLat.MIN_LATITUDE - EPSILON/2) / EPSILON);
        int col = (int) Math.round((pos.getLongitude() - LongLat.MIN_LONGITUDE - EPSILON/2) / EPSILON);

        return new int[]{row, col};
    }
}
