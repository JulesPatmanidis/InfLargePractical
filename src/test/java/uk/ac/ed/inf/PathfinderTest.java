package uk.ac.ed.inf;

import org.junit.Ignore;
import uk.ac.ed.inf.domain.LongLat;

@Ignore
public class PathfinderTest {

    private static final String TEST_DATE_1 = "2022-01-02";
    private static final String TEST_DATE_2 = "2023-09-10";
    LongLat AT = new LongLat(-3.186874, 55.944494); // Appleton Tower

//    @Test
//    public void testFindPathOnGrid() {
//        WebServerClient webServerClient = new WebServerClient("9898");
//        List<GridNode> path;
//        Pathfinder pathfinder = new Pathfinder(webServerClient.getNoFlyZones());
//        GridNode rightDownCorner = Pathfinder.getVirtualGrid().get(0).get(Pathfinder.getVirtualGrid().get(0).size()-1);
//        GridNode leftUpCorner = Pathfinder.getVirtualGrid().get(Pathfinder.getVirtualGrid().size()-1).get(0);
//
//        path = pathfinder.findPathOnGrid(rightDownCorner, leftUpCorner);
//
//        for (GridNode node : path) {
//            assertTrue(node.isWalkable());
//            if (node != rightDownCorner) {
//                assertNotNull(node.getParent());
//                assertTrue(pathfinder.lineOfSight(node.getCoordinates(), node.getParent().getCoordinates()));
//            }
//        }
//    }

//    @Test
//    public void testFindPath() {
//        WebServerClient webServerClient = new WebServerClient("9898");
//        Pathfinder pathfinder = new Pathfinder(webServerClient.getNoFlyZones());
//        List<LongLat> path;
//
//        path = pathfinder.findPath(AT, Pathfinder.CONFINEMENT_AREA.get(0));
//        for (LongLat point : path) {
//            assertTrue(point.isConfined());
//        }
//        assertTrue(path.get(path.size() - 1).closeTo(Pathfinder.CONFINEMENT_AREA.get(0)));
//    }

}
