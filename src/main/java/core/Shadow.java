package core;

import Jama.Matrix;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LinearRing;
import processing.core.PApplet;
import utility.JtsRender;
import utility.PolyHandler;
import wblut.geom.*;

import java.util.List;

/**
 * shadow calculator for the sun at a given position
 *
 * @author Wu
 * @create 2021-02-22 16:11
 */

public class Shadow {
    public enum Type {VOLUME, FACET}

    /**
     * shadow at the current time
     *
     * @param sun       sun
     * @param buildings buildings
     * @return Geometry
     */
    public static Geometry calCurrentShadow(Type type, Sun sun, Building... buildings) {
        if (sun.getPosition().zd() <= 0)
            return null;
        Geometry[] geos = new Geometry[buildings.length];
        for (int i = 0; i < geos.length; i++) {
            switch (type) {
                case VOLUME:
                    geos[i] = calShadowByVolume(
                            sun.getPosition(), sun.getElevation(), buildings[i]);
                    break;
                case FACET:
                    geos[i] = calShadowByFacet(sun.getPosition(), buildings[i]);
                    break;
                default:
                    break;
            }
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
    public static Geometry[] calAllDayShadow(Type type, Sun sun, Building... buildings) {
        WB_PolyLine path = sun.getPath();
        if (null == path)
            return null;

        Geometry[] allDayShadow = new Geometry[sun.getPathDiv() - 2];
        double[] pathElevation = sun.getPathElevation();

        for (int i = 0; i < allDayShadow.length; i++) {
            Geometry[] timeShadow = new Geometry[buildings.length];
            for (int j = 0; j < timeShadow.length; j++) {
                switch (type) {
                    case VOLUME:
                        timeShadow[j] = calShadowByVolume(path.getPoint(i + 1),
                                pathElevation[i + 1], buildings[j]);
                        break;
                    case FACET:
                        timeShadow[j] = calShadowByFacet(path.getPoint(i + 1), buildings[j]);
                        break;
                    default:
                        break;
                }
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
    private static WB_Point calShadowVector(WB_Point pos, double alpha, double height) {
        double shadowLength = height / Math.tan(alpha);
        WB_Point shadowVec = pos.mul(-1);

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
    private static Geometry calEdgeShadow(WB_Point pos, double alpha, Building building, int startID, int endID) {
        WB_Point shadowVec = calShadowVector(pos, alpha, building.getHeight());
        Geometry[] geos = new Geometry[endID - startID + 1];

        WB_Coord[] points = building.getBase().getPoints().toArray();
        int edgeNums = endID - startID + 1;

        for (int i = startID; i < endID + 1; i++) {
            WB_Point p0 = (WB_Point) points[i];
            WB_Point p1 = (WB_Point) points[(i - startID + 1) % edgeNums + startID];
            WB_Point p2 = p1.add(shadowVec);
            WB_Point p3 = p0.add(shadowVec);

            geos[i - startID] = PolyHandler.createPolygon(p0, p1, p2, p3);
        }

        return unionShadow(geos);
    }

    /**
     * line-plane intersection
     *
     * @param line  line
     * @param plane WB_Triangle
     * @return WB_Point intersection point
     */
    private static WB_Point lineIntersectPlane(WB_Line line, WB_Triangle plane) {
        WB_Point la = line.getPoint(0);
        WB_Point lb = line.getPoint(1);

        WB_Point p0 = (WB_Point) plane.getPoint(0);
        WB_Point p1 = (WB_Point) plane.getPoint(1);
        WB_Point p2 = (WB_Point) plane.getPoint(2);
        Matrix m = new Matrix(new double[][]{
                {la.xd() - lb.xd(), p1.xd() - p0.xd(), p2.xd() - p0.xd()},
                {la.yd() - lb.yd(), p1.yd() - p0.yd(), p2.yd() - p0.yd()},
                {la.zd() - lb.zd(), p1.zd() - p0.zd(), p2.zd() - p0.zd()}
        }).inverse();
        Matrix n = new Matrix(new double[][]{
                {la.xd() - p0.xd()},
                {la.yd() - p0.yd()},
                {la.zd() - p0.zd()}
        });

        double t = m.times(n).get(0, 0);
        return la.add(lb.sub(la).mul(t));
    }

    /**
     * shadow of a triangular facet
     *
     * @param pos position of the sun
     * @param tri triangular facet
     * @return Geometry
     */
    private static Geometry calFacetShadow(WB_Point pos, WB_Triangle tri) {
        WB_Point sunlight = pos.mul(-1);
        WB_Point[] shadowPoints = new WB_Point[3];
        for (int i = 0; i < shadowPoints.length; i++) {
            WB_Coord origin = tri.getPoint(i);
            WB_Line line = PolyHandler.gf.createLineThroughPoints(
                    origin, WB_Point.add(origin, sunlight));
            shadowPoints[i] = lineIntersectPlane(line, PolyHandler.XY_PLANE);
        }

        return PolyHandler.createPolygon(
                shadowPoints[0], shadowPoints[1], shadowPoints[2]);
    }

    /**
     * shadow of a extruded building, allowing holes
     *
     * @param pos      position of the sun
     * @param alpha    elevation of the sun
     * @param building simple building in volume
     * @return Geometry
     */
    private static Geometry calShadowByVolume(WB_Point pos, double alpha, Building building) {
        if (pos.zd() <= 0)
            return null;
        WB_Polygon base = building.getBase();
        WB_Coord[] points = base.getPoints().toArray();
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
                WB_Coord[] holePoints = new WB_Point[ptsPerContour[i]];
                System.arraycopy(points, startID, holePoints, 0, holePoints.length);
                holes[i - 1] = PolyHandler.JTSgf.createLinearRing(
                        PolyHandler.createLinearRingCoordinates(holePoints));
            }
        }

        // shell LinearRing
        WB_Coord[] shellPoints = new WB_Point[ptsPerContour[0]];
        System.arraycopy(points, 0, shellPoints, 0, shellPoints.length);
        LinearRing shell = PolyHandler.JTSgf.createLinearRing(
                PolyHandler.createLinearRingCoordinates(shellPoints));

        // building base to union with edge shadows
        if (ptsPerContour.length > 1)
            shadows[shadows.length - 1] = PolyHandler.JTSgf.createPolygon(shell, holes);
        else
            shadows[shadows.length - 1] = PolyHandler.JTSgf.createPolygon(shell);

        return unionShadow(shadows);
    }

    /**
     * shadow of a building in facets
     *
     * @param pos      position of the sun
     * @param building complex building in facets
     * @return Geometry
     */
    private static Geometry calShadowByFacet(WB_Point pos, Building building) {
        if (pos.zd() <= 0)
            return null;
        List<WB_Triangle> tri = building.getTris();
        Geometry[] geos = new Geometry[tri.size()];
        for (int i = 0; i < geos.length; i++) {
            geos[i] = calFacetShadow(pos, tri.get(i));
        }
        return unionShadow(geos);
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

    public static void displayShadow(Geometry shadow, JtsRender jtsRender) {
        if (null == shadow)
            return;
        PApplet app = jtsRender.getApp();
        app.pushStyle();
        app.fill(0x300000ff);
        app.noStroke();
        jtsRender.draw(shadow);
        app.popStyle();
    }
}
