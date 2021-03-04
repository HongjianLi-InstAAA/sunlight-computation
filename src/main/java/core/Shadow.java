package core;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LinearRing;
import utility.PolyHandler;
import wblut.geom.WB_Coord;
import wblut.geom.WB_PolyLine;
import wblut.geom.WB_Polygon;
import wblut.geom.WB_Vector;

/**
 * shadow calculator for the sun at a given position
 *
 * @author Wu
 * @create 2021-02-22 16:11
 */

public class Shadow {
    /**
     * shadow at the current time
     *
     * @param sun       sun
     * @param buildings buildings
     * @return Geometry
     */
    public static Geometry calCurrentShadow(Sun sun, Building... buildings) {
        if (sun.getPosition().zd() <= 0)
            return null;
        Geometry[] geos = new Geometry[buildings.length];
        for (int i = 0; i < geos.length; i++) {
            geos[i] = calShadow(sun.getPosition(), sun.getElevation(), buildings[i]);
        }
        return unionShadow(geos);
    }

    /**
     * shadow of the all day
     *
     * @param sun       sun
     * @param buildings buildings
     * @return Geometry[]
     */
    public static Geometry[] calAllDayShadow(Sun sun, Building... buildings) {
        WB_PolyLine path = sun.getPath();
        if (null == path)
            return null;
        Geometry[] allDayShadow = new Geometry[sun.getPathDiv() - 2];
        double[] pathElevation = sun.getPathElevation();
        for (int i = 0; i < allDayShadow.length; i++) {
            Geometry[] timeShadow = new Geometry[buildings.length];
            for (int j = 0; j < timeShadow.length; j++) {
                timeShadow[j] = calShadow(path.getPoint(i + 1),
                        pathElevation[i + 1], buildings[j]);
            }

            allDayShadow[i] = unionShadow(timeShadow);
        }
        return allDayShadow;
    }

    /**
     * building shadow vector
     *
     * @param pos    position of the sun
     * @param alpha  elevation of the sun
     * @param height building height in METERS
     * @return WB_Vector
     */
    private static WB_Vector calShadowVector(WB_Vector pos, double alpha, double height) {
        double shadowLength = height / Math.tan(alpha);
        WB_Vector shadowVec = pos.mul(-1);

        shadowVec.setZ(0);
        shadowVec.normalizeSelf();
        shadowVec.mulSelf(shadowLength);
        return shadowVec;
    }

    /**
     * sum of the first N elements in an array
     *
     * @param array int[]
     * @param n     first N elements
     * @return integer
     */
    private static int sumFirstNElements(int[] array, int n) {
        int sum = 0;
        if (n > array.length - 1)
            throw new IllegalArgumentException("N should not be larger than array.length: "
                    + n + "/" + array.length);
        else if (n < 0)
            throw new IllegalArgumentException("N should not be less than 0: " + n);
        else if (n > 0) {
            for (int i = 0; i < n; i++)
                sum += array[i];
        }
        return sum;
    }

    /**
     * shadow of each edge, then union
     *
     * @param pos      position of the sun
     * @param alpha    elevation of the sun
     * @param building building
     * @param startID  index of the starting points
     * @param endID    index of the ending points
     * @return Geometry
     */
    private static Geometry calEdgeShadow(WB_Vector pos, double alpha, Building building, int startID, int endID) {
        WB_Vector shadowVec = calShadowVector(pos, alpha, building.getHeight());
        Geometry[] geos = new Geometry[endID - startID + 1];

        WB_Coord[] coords = building.getBase().getPoints().toArray();
        int edgeNums = endID - startID + 1;

        for (int i = startID; i < endID + 1; i++) {
            WB_Coord p0 = coords[i];
            WB_Coord p1 = coords[(i - startID + 1) % edgeNums + startID];
            WB_Coord p2 = WB_Vector.add(p1, shadowVec);
            WB_Coord p3 = WB_Vector.add(p0, shadowVec);

            geos[i - startID] = PolyHandler.createPolygon(p0, p1, p2, p3);
        }

        return unionShadow(geos);
    }

    /**
     * shadow of a building, allowing holes
     *
     * @param pos      position of the sun
     * @param alpha    elevation of the sun
     * @param building simple building
     * @return Geometry
     */
    private static Geometry calShadow(WB_Vector pos, double alpha, Building building) {
        if (pos.zd() <= 0)
            return null;
        WB_Polygon base = building.getBase();
        WB_Coord[] coords = base.getPoints().toArray();
        int[] ptsPerContour = base.getNumberOfPointsPerContour();
        Geometry[] shadows = new Geometry[ptsPerContour.length + 1];

        // holes LinearRing[], if any
        LinearRing[] holes = null;
        if (ptsPerContour.length > 1)
            holes = new LinearRing[ptsPerContour.length - 1];
        // edge shadow of each LinearRing
        for (int i = 0; i < ptsPerContour.length; i++) {
            int startID = sumFirstNElements(ptsPerContour, i);
            int endID = startID + ptsPerContour[i] - 1;
            shadows[i] = calEdgeShadow(pos, alpha, building, startID, endID);

            if (i > 0) {
                WB_Coord[] holeCoords = new WB_Coord[ptsPerContour[i]];
                System.arraycopy(coords, startID, holeCoords, 0, holeCoords.length);
                holes[i - 1] = PolyHandler.JTSgf.createLinearRing(
                        PolyHandler.createLinearRingCoordinates(holeCoords));
            }
        }

        // shell LinearRing
        WB_Coord[] shellCoords = new WB_Coord[ptsPerContour[0]];
        System.arraycopy(coords, 0, shellCoords, 0, shellCoords.length);
        LinearRing shell = PolyHandler.JTSgf.createLinearRing(
                PolyHandler.createLinearRingCoordinates(shellCoords));

        // building base to union with edge shadows
        if (ptsPerContour.length > 1)
            shadows[shadows.length - 1] = PolyHandler.JTSgf.createPolygon(shell, holes);
        else
            shadows[shadows.length - 1] = PolyHandler.JTSgf.createPolygon(shell);

        return unionShadow(shadows);
    }

    private static Geometry unionShadow(Geometry[] geos) {
        if (geos.length == 1)
            return geos[0];
        for (Geometry g : geos) {
            if (null == g)
                return null;
        }
        GeometryCollection gc = PolyHandler.JTSgf.createGeometryCollection(geos);
        return gc.buffer(0);
    }
}
