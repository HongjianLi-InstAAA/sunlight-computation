package core;

import org.locationtech.jts.geom.*;
import utility.PolyHandler;
import wblut.geom.WB_Coord;
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
     * building shadow vector
     *
     * @param sun specified sun
     * @param H   building height
     * @return WB_Vector
     */
    private static WB_Vector calShadowVector(Sun sun, int H) {
        double alpha = sun.getElevation();
        double shadowLength = H / Math.tan(alpha);
        WB_Vector shadowVec = sun.getPosition().mul(-1);

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
     * @param sun     specified sun
     * @param coords  points of a polygon
     * @param startID index of the starting points
     * @param endID   index of the ending points
     * @return Geometry
     */
    private static Geometry calEdgeShadow(Sun sun, WB_Coord[] coords, int startID, int endID, int H) {
        WB_Vector shadowVec = calShadowVector(sun, H);
        Geometry[] geos = new Geometry[endID - startID + 1];

        int edgeNums = endID - startID + 1;

        for (int i = startID; i < endID + 1; i++) {
            WB_Coord p0 = coords[i];
            WB_Coord p1 = coords[(i - startID + 1) % edgeNums + startID];
            WB_Coord p2 = WB_Vector.add(p1, shadowVec);
            WB_Coord p3 = WB_Vector.add(p0, shadowVec);

            geos[i - startID] = PolyHandler.createPolygon(p0, p1, p2, p3);
        }

        GeometryCollection gc = PolyHandler.JTSgf.createGeometryCollection(geos);
        return gc.buffer(0);
    }

    /**
     * shadow of a building, allowing holes
     *
     * @param sun  specified sun
     * @param base building base
     * @param H    building height
     * @return Geometry
     */
    public static Geometry calShadow(Sun sun, WB_Polygon base, int H) {
        if (sun.getPosition().zd() <= 0)
            return null;
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
            shadows[i] = calEdgeShadow(sun, coords, startID, endID, H);

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

        GeometryCollection gc = PolyHandler.JTSgf.createGeometryCollection(shadows);
        return gc.buffer(0);
    }
}
