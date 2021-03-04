package utility;

import org.locationtech.jts.geom.*;
import wblut.geom.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * this class is made to convert between
 * JTS Polygon and HE_Mesh WB_Polygon
 *
 * @author FreeMan
 * @modifier Wu
 */

public class PolyHandler {
    public static final WB_GeometryFactory gf = WB_GeometryFactory.instance();
    public static final GeometryFactory JTSgf = new GeometryFactory();

    public static Coordinate toCoordinate(WB_Vector v) {
        return new Coordinate(v.xd(), v.yf(), v.zf());
    }

    /**
     * @param coords JTS coordinates
     * @return Coordinate[]
     */
    public static Coordinate[] addFirst2Last(Coordinate... coords) {
        Coordinate[] cs = new Coordinate[coords.length + 1];
        int i = 0;
        for (; i < coords.length; i++) {
            cs[i] = coords[i];
        }
        cs[i] = coords[0];
        return cs;
    }

    /**
     * Polygon.getCoordinates包括首尾重合的点 ，用此方法去重
     *
     * @param coords JTS coordinates
     * @return Coordinate[]
     */
    public static Coordinate[] subLast(Coordinate... coords) {
        Coordinate[] cs = new Coordinate[coords.length - 1];
        for (int i = 0; i < coords.length - 1; i++) {
            cs[i] = coords[i];
            cs[i].z = 0;
        }
        return cs;
    }

    /**
     * get shell points of WB_Polygon in AntiClockWise
     *
     * @param poly WB_Polygon
     * @return WB_Coord[]
     */
    public static WB_Coord[] getShellPts(WB_Polygon poly) {
        if (poly.getNumberOfContours() == 1)
            return poly.getPoints().toArray();
        int numOut = poly.getNumberOfShellPoints();
        WB_Point[] out = new WB_Point[numOut];
        for (int i = 0; i < numOut; i++) {
            out[i] = poly.getPoint(i);
        }
        return out;
    }

    /**
     * get inner points of WB_Polygon in ClockWise
     *
     * @param poly WB_Polygon
     * @return WB_Point[][]
     */
    public static WB_Point[][] getInnerPts(WB_Polygon poly) {
        if (poly.getNumberOfContours() == 1)
            return null;
        WB_Point[][] in = new WB_Point[poly.getNumberOfHoles()][];
        int[] num = poly.getNumberOfPointsPerContour();// 从外开始
        int count = num[0];
        for (int i = 0; i < in.length; i++) {
            WB_Point[] pts = new WB_Point[num[i + 1]];
            for (int j = 0; j < pts.length; j++) {
                pts[j] = poly.getPoint(count + j);
            }
            in[i] = pts;
            count += pts.length;
        }
        return in;
    }

    /**
     * out points in AntiClockWise
     * inner points in ClockWise
     *
     * @param out shell polygon
     * @param in  hole polygon
     * @return WB_Polygon
     */
    public static WB_Polygon createWB_PolyWithHole(WB_Polygon out, WB_Polygon... in) {
        WB_Coord[] outPts = getShellPts(out);
        WB_Coord[][] ptsIn = new WB_Point[in.length][];

        for (int i = 0; i < in.length; i++) {
            List<WB_Coord> pts = in[i].getPoints().toList();
            ptsIn[i] = new WB_Point[pts.size()];
            for (int j = 0; j < pts.size(); j++) {
                ptsIn[i][j] = pts.get(pts.size() - 1 - j);
            }
        }
        return new WB_Polygon(outPts, ptsIn);
    }

    /**
     * reverse the order of points in an array
     *
     * @param pts origin points
     * @return WB_Coord[]
     */
    public static WB_Coord[] reversePts(WB_Coord[] pts) {
        WB_Coord[] newPts = new WB_Coord[pts.length];
        for (int i = 0; i < pts.length; i++) {
            newPts[i] = pts[pts.length - 1 - i];
        }
        return newPts;
    }

    public static WB_Polygon reversePtsSimple(WB_Polygon poly) {
        return new WB_Polygon(reversePts(getShellPts(poly)));
    }

    /**
     * create a JTS Polygon from WB_Coord[]
     *
     * @param coords points of a polygon
     * @return JTS Polygon
     */
    public static Polygon createPolygon(WB_Coord... coords) {
        Coordinate[] polyCoords = new Coordinate[coords.length + 1];
        for (int i = 0; i < coords.length; i++) {
            polyCoords[i] = new Coordinate(coords[i].xd(),
                    coords[i].yd(), coords[i].zd());
        }
        polyCoords[coords.length] = polyCoords[0];
        return JTSgf.createPolygon(polyCoords);
    }

    public static Coordinate[] createLinearRingCoordinates(WB_Coord[] coords) {
        Coordinate[] coordinates = new Coordinate[coords.length + 1];
        for (int i = 0; i < coords.length; i++) {
            coordinates[i] = new Coordinate(coords[i].xd(),
                    coords[i].yd(), coords[i].zd());
        }
        coordinates[coordinates.length - 1] = coordinates[0];
        return coordinates;
    }

    /**
     * to convert a JTS Polygon to WB_Polygon
     *
     * @param g Geometry
     * @return WB_Polygon
     */
    public static WB_Polygon toWB_Polygon(Geometry g) {
        if (null != g && g.getGeometryType().equalsIgnoreCase("Polygon")) {
            Polygon p = (Polygon) g;
            Coordinate[] coordOut = p.getExteriorRing().getCoordinates();
            coordOut = subLast(coordOut);
            WB_Point[] outPt = new WB_Point[coordOut.length];
            for (int i = 0; i < coordOut.length; i++) {
                outPt[i] = new WB_Point(coordOut[i].x, coordOut[i].y, coordOut[i].z);
//                System.out.printf("coord: %.2f, %.2f\n", coordOut[i].x, coordOut[i].y);
            }
            int num = p.getNumInteriorRing();

            if (num == 0) {
                return new WB_Polygon(outPt);
            } else {
                WB_Point[][] ptsIn = new WB_Point[num][];
                for (int i = 0; i < num; i++) {
                    Coordinate[] coords = p.getInteriorRingN(i).getCoordinates();
                    // LineString 也需sublast
                    coords = subLast(coords);
                    // System.out.println(coords[0]+" &&
                    // "+coords[coords.length-1]);/
                    WB_Point[] pts = new WB_Point[coords.length];
                    for (int j = 0; j < coords.length; j++) {
                        pts[j] = new WB_Point(coords[j].x, coords[j].y, coords[j].z);
                    }
                    ptsIn[i] = pts;
                }
                return new WB_Polygon(outPt, ptsIn);
            }
        } else {
//            System.out.println("this Geometry is not a Polygon!");
            return null;
        }
    }

    public static ArrayList<WB_Triangle> toWB_Triangles(Geometry g) {
        if (null == g)
            return null;
        ArrayList<WB_Triangle> tris = new ArrayList<>();
        List<WB_Polygon> polys = new ArrayList<>();

        String type = g.getGeometryType();
        if (type.equalsIgnoreCase("Polygon")) {
            polys.add(toWB_Polygon(g));
        } else if (type.equalsIgnoreCase("MultiPolygon")) {
            int geoNum = g.getNumGeometries();
            for (int i = 0; i < geoNum; i++) {
                Geometry subGeo = g.getGeometryN(i);
                polys.add(toWB_Polygon(subGeo));
            }
        } else {
            return null;
        }

        for (WB_Polygon p : polys) {
            tris.addAll(poly2tris(p));
        }
        return tris;
    }

    public static List<WB_Triangle> poly2tris(WB_Polygon poly) {
        if (null == poly)
            return null;
        List<WB_Triangle> tris = new ArrayList<>();
        final int[] triID = poly.getTriangles();
        for (int i = 0; i < triID.length; i += 3) {
            tris.add(gf.createTriangle(
                    poly.getPoint(triID[i]),
                    poly.getPoint(triID[i + 1]),
                    poly.getPoint(triID[i + 2])
            ));
        }
        return tris;
    }

    /**
     * WB_Polygon的创建原理
     *
     * @param poly WB_Polygon
     * @return WB_Polygon
     */
    public WB_Polygon polyDup(WB_Polygon poly) {
        WB_Coord[] out = getShellPts(poly);
        WB_Coord[][] in = getInnerPts(poly);
        return new WB_Polygon(out, in);
    }

    /**
     * convert simple WB_Polygon to JTS Polygon
     * to be finished if there is a inner ring
     *
     * @param poly WB_Polygon
     * @return JTS Polygon
     */
    public static Polygon toJTSPolygonSimple(WB_Polygon poly) {
        Coordinate[] coord = new Coordinate[poly.getNumberOfPoints()];
        for (int i = 0; i < poly.getNumberOfPoints(); i++) {
            WB_Point p = poly.getPoint(i);
            Coordinate c = new Coordinate(p.xd(), p.yd(), p.zd());
            coord[i] = c;
        }
        LinearRing ring = JTSgf.createLinearRing(addFirst2Last(coord));
        return JTSgf.createPolygon(ring);
    }

    public static Polygon toJTSPolygon(WB_Polygon poly) {
        int num = poly.getNumberOfContours();
        if (num == 1)
            return toJTSPolygonSimple(poly);

        int numOut = poly.getNumberOfShellPoints();
        Coordinate[] outPts = new Coordinate[numOut];

        for (int i = 0; i < numOut; i++) {
            WB_Point wbPt = poly.getPoint(i);
            outPts[i] = new Coordinate(wbPt.xd(), wbPt.yd(), wbPt.zd());
        }

        outPts = addFirst2Last(outPts);
        LinearRing outRing = JTSgf.createLinearRing(outPts);

        LinearRing[] holeRings = new LinearRing[poly.getNumberOfHoles()];

        int[] ptsNumPerHole = poly.getNumberOfPointsPerContour();// 从外开始
        int count = ptsNumPerHole[0];

        for (int i = 0; i < holeRings.length; i++) {
            Coordinate[] pts = new Coordinate[ptsNumPerHole[i + 1]];
            for (int j = 0; j < pts.length; j++) {
                WB_Point wbPt = poly.getPoint(count + j);
                pts[j] = new Coordinate(wbPt.xd(), wbPt.yd(), wbPt.zd());
            }
            pts = addFirst2Last(pts);
            holeRings[i] = JTSgf.createLinearRing(pts);
            count += pts.length;
        }
        return JTSgf.createPolygon(outRing, holeRings);
    }

    public static WB_Polygon applyToSimple(WB_Polygon poly, WB_Transform3D T) {
        WB_Coord[] pts = applyToPts(poly.getPoints().toArray(), T);
        return new WB_Polygon(pts);
    }

    /**
     * apply Transform to HE_Mesh polygon
     *
     * @param poly WB_Polygon
     * @param T    WB_Transform3D
     * @return WB_Polygon
     */
    public static WB_Polygon apply(WB_Polygon poly, WB_Transform3D T) {
        if (poly.getNumberOfContours() == 1)
            return applyToSimple(poly, T);
        WB_Coord[] out = applyToPts(getShellPts(poly), T);
        WB_Coord[][] in = applyToPts(getInnerPts(poly), T);
        return new WB_Polygon(out, in);
    }

    public static WB_Coord[] applyToPts(List<WB_Coord> pts, WB_Transform3D T) {
        WB_Point[] ptsT = new WB_Point[pts.size()];
        for (int i = 0; i < pts.size(); i++) {
            ptsT[i] = new WB_Point(pts.get(i)).apply(T);
        }
        return ptsT;
    }

    public static WB_Coord[] applyToPts(WB_Coord[] pts, WB_Transform3D T) {
        return applyToPts(Arrays.asList(pts), T);
    }

    public static WB_Coord[][] applyToPts(WB_Coord[][] pts, WB_Transform3D T) {
        WB_Coord[][] ptsT = new WB_Point[pts.length][];
        for (int i = 0; i < pts.length; i++) {
            ptsT[i] = applyToPts(pts[i], T);
        }
        return ptsT;
    }

    public static List<WB_Polygon> getTrimmed(WB_Polygon trimPoly, List<WB_Polygon> polysToTrim) {
        ArrayList<WB_Polygon> polys = new ArrayList<>();
        Polygon trimJTS = toJTSPolygon(trimPoly);

        for (WB_Polygon poly : polysToTrim) {
            Polygon pp = toJTSPolygon(poly);
            if (trimJTS.overlaps(pp)) {
                try {
                    Geometry geo = pp.difference(pp.difference(trimJTS)).getGeometryN(0).buffer(-1).getGeometryN(0);
                    if (geo.getArea() > 250) {
                        WB_Polygon polyTrim = toWB_Polygon(geo);

                        polys.add(polyTrim);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return polys;
    }

}
