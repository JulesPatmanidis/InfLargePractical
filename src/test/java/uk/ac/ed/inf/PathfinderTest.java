package uk.ac.ed.inf;

import org.junit.Test;
import uk.ac.ed.inf.Clients.DatabaseClient;
import uk.ac.ed.inf.Pathfinding.GridNode;
import uk.ac.ed.inf.Pathfinding.Pathfinder;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class PathfinderTest {

    private static final String TEST_DATE_1 = "2022-01-02";
    private static final String TEST_DATE_2 = "2023-09-10";
    double EPSILON = Pathfinder.EPSILON;
    LongLat AT = new LongLat(-3.186874, 55.944494); // Appleton Tower

    @Test
    public void testFindPathOnGrid() {
        List<GridNode> path;
        Pathfinder pathfinder = new Pathfinder();
        GridNode rightDownCorner = Pathfinder.virtualGrid.get(0).get(Pathfinder.virtualGrid.get(0).size()-1);
        GridNode leftUpCorner = Pathfinder.virtualGrid.get(Pathfinder.virtualGrid.size()-1).get(0);

        path = pathfinder.findPathOnGrid(rightDownCorner, leftUpCorner);

        for (GridNode node : path) {
            assertTrue(node.isWalkable);
            if (node != rightDownCorner) {
                assertNotNull(node.parent);
                assertTrue(pathfinder.lineOfSight(node.coordinates, node.parent.coordinates));
            }
        }
    }

    @Test
    public void testFindPath() {
        List<LongLat> path;
        Pathfinder pathfinder = new Pathfinder();

        path = pathfinder.findPath(AT, Pathfinder.CONFINEMENT_AREA.get(0));
        for (LongLat point : path) {
            assertTrue(point.isConfined());

        }
        assertTrue(path.get(path.size() - 1).closeTo(Pathfinder.CONFINEMENT_AREA.get(0)));
    }

}
