package utility;

import Jama.Matrix;
import gzf.gui.Vec_Guo;
import org.locationtech.jts.geom.*;
import wblut.geom.*;
import wblut.hemesh.HET_Import;
import wblut.hemesh.HE_Mesh;

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

    public static final WB_Point ORIGIN = new WB_Point(0, 0, 0);
    public static final WB_Point X_NORMAL = new WB_Point(1, 0, 0);
    public static final WB_Point Y_NORMAL = new WB_Point(0, 1, 0);
    public static final WB_Triangle XY_PLANE = gf.createTriangle(ORIGIN, X_NORMAL, Y_NORMAL);

    public static Coordinate toCoordinate(WB_Vector v) {
        return new Coordinate(v.xd(), v.yf(), v.zf());
    }

    public static WB_Coord vec2coord(Vec_Guo v) {
        return new WB_Point(v.x(), v.y(), v.z());
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
        if (coords.length < 2)
            return null;
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
     * @return WB_Point[]
     */
    public static WB_Point[] getShellPts(WB_Polygon poly) {
        if (poly.getNumberOfContours() == 1)
            return (WB_Point[]) poly.getPoints().toArray();
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
        WB_Point[] outPts = getShellPts(out);
        WB_Point[][] ptsIn = new WB_Point[in.length][];

        for (int i = 0; i < in.length; i++) {
            List<WB_Coord> pts = in[i].getPoints().toList();
            ptsIn[i] = new WB_Point[pts.size()];
            for (int j = 0; j < pts.size(); j++) {
                ptsIn[i][j] = (WB_Point) pts.get(pts.size() - 1 - j);
            }
        }
        return new WB_Polygon(outPts, ptsIn);
    }

    /**
     * reverse the order of points in an array
     *
     * @param pts origin points
     * @return WB_Point[]
     */
    public static WB_Point[] reversePts(WB_Point[] pts) {
        WB_Point[] newPts = new WB_Point[pts.length];
        for (int i = 0; i < pts.length; i++) {
            newPts[i] = pts[pts.length - 1 - i];
        }
        return newPts;
    }

    public static WB_Polygon reversePtsSimple(WB_Polygon poly) {
        return new WB_Polygon(reversePts(getShellPts(poly)));
    }

    /**
     * create a JTS Polygon from WB_Point[]
     *
     * @param points points of a polygon
     * @return JTS Polygon
     */
    public static Polygon createPolygon(WB_Point... points) {
        Coordinate[] polyCoords = new Coordinate[points.length + 1];
        for (int i = 0; i < points.length; i++) {
            polyCoords[i] = new Coordinate(points[i].xd(),
                    points[i].yd(), points[i].zd());
        }
        polyCoords[points.length] = polyCoords[0];
        return JTSgf.createPolygon(polyCoords);
    }

    public static Coordinate[] createLinearRingCoordinates(WB_Coord[] points) {
        Coordinate[] coordinates = new Coordinate[points.length + 1];
        for (int i = 0; i < points.length; i++) {
            coordinates[i] = new Coordinate(points[i].xd(),
                    points[i].yd(), points[i].zd());
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
            if (null == coordOut)
                return null;
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
            if (null != p)
                tris.addAll(poly2tris(p));
        }
        return tris;
    }

    public static List<WB_Triangle> poly2tris(WB_Polygon poly) {
        if (null == poly)
            return null;
        List<WB_Triangle> tris = new ArrayList<>();
        final int[] triID = poly.getTriangles();
        for (int i = 0; i < triID.length - 2; i += 3) {
            tris.add(gf.createTriangle(
                    poly.getPoint(triID[i]),
                    poly.getPoint(triID[i + 1]),
                    poly.getPoint(triID[i + 2])
            ));
        }
        return tris;
    }

    public static HE_Mesh reverseObj(String path) {
        HE_Mesh mesh = HET_Import.readFromObjFile(path);
        WB_CoordCollection coords = mesh.getPoints();
        int[][] faces = mesh.getFacesAsInt();
        List<WB_Point> pts = new ArrayList<>();
        for (int i = 0; i < coords.size(); i++) {
            WB_Point pt = new WB_Point(coords.get(i));
            pt = new WB_Point(pt.xd(), -pt.zd(), pt.yd());
            pts.add(pt);
        }

        return new HE_Mesh(gf.createMesh(pts, faces));
    }

    public static List<WB_Triangle> mesh2tris(HE_Mesh mesh) {
        if (null == mesh)
            return null;
        mesh.triangulate();
        List<WB_Triangle> tris = new ArrayList<>();
        int[][] triID = mesh.getFacesAsInt();
        for (int[] ints : triID) {
            WB_Triangle tri = gf.createTriangle(
                    mesh.getVertex(ints[0]),
                    mesh.getVertex(ints[1]),
                    mesh.getVertex(ints[2]));
            tris.add(tri);
        }
        return tris;
    }

    /**
     * get Euler Angles of rotation from (0, 0, 1) to target normal
     *
     * @param origin target point
     * @param normal normal of the target plane
     * @return WB_Vector
     */
    public static WB_Vector getEuler(WB_Point origin, WB_Coord normal) {
        WB_Vector normalVec = new WB_Vector(normal);
        normalVec.normalizeSelf();

        WB_Transform3D tran = new WB_Transform3D();
        tran.addFromWorldToCS(new WB_CoordinateSystem(new WB_Plane(origin, normalVec)));
        tran.applyInvAsVector(WB_Vector.Z());
        return tran.getEulerAnglesXYZ();
    }

    /**
     * ray-plane intersection point
     *
     * @param ray   ray
     * @param plane WB_Triangle
     * @return WB_Point intersection point
     */
    public static WB_Point rayPlaneIntersection(WB_Ray ray, WB_Triangle plane) {
        Matrix m = rayIntersectPlane(ray, plane);
        if (null == m)
            return null;
        double t = m.get(0, 0);
        return pointInRay(ray, t);
    }

    public static double rayTriIntersectionT(WB_Ray ray, WB_Triangle plane) {
        Matrix m = rayIntersectPlane(ray, plane);
        if (null == m)
            return Double.MAX_VALUE;
        double u = m.get(1, 0);
        double v = m.get(2, 0);
        if (u + v <= 1 && u >= 0 && u <= 1 && v >= 0 && v <= 1)
            return m.get(0, 0);
        return Double.MAX_VALUE;
    }

    public static WB_Point pointInRay(WB_Ray ray, double t) {
        WB_Point la = ray.getPoint(0);
        WB_Point lb = ray.getPoint(1);
        return la.add(lb.sub(la).mul(t));
    }

    /**
     * check if a ray intersects a triangle
     *
     * @param ray ray
     * @param tri WB_Triangle
     * @return boolean
     */
    public static boolean checkRayTriIntersection(WB_Ray ray, WB_Triangle tri) {
        Matrix m = rayIntersectPlane(ray, tri);
        if (null == m)
            return false;
        double u = m.get(1, 0);
        double v = m.get(2, 0);
        return u + v <= 1 && u >= 0 && u <= 1 && v >= 0 && v <= 1;
    }

    /**
     * ray-plane intersection matrix
     *
     * @param ray   ray
     * @param plane WB_Triangle
     * @return Matrix [{t}, {u}, {v}]
     */
    private static Matrix rayIntersectPlane(WB_Ray ray, WB_Triangle plane) {
        WB_Point la = ray.getPoint(0);
        WB_Point lb = ray.getPoint(1);

        WB_Coord p0 = plane.getPoint(0);
        WB_Coord p1 = plane.getPoint(1);
        WB_Coord p2 = plane.getPoint(2);
        Matrix m = new Matrix(new double[][]{
                {la.xd() - lb.xd(), p1.xd() - p0.xd(), p2.xd() - p0.xd()},
                {la.yd() - lb.yd(), p1.yd() - p0.yd(), p2.yd() - p0.yd()},
                {la.zd() - lb.zd(), p1.zd() - p0.zd(), p2.zd() - p0.zd()}
        });
        if (m.det() == 0)
            return null;
        m = m.inverse();

        Matrix n = new Matrix(new double[][]{
                {la.xd() - p0.xd()},
                {la.yd() - p0.yd()},
                {la.zd() - p0.zd()}
        });

        Matrix intersection = m.times(n);
        if (null != intersection && intersection.get(0, 0) >= 0)
            return intersection;
        return null;
    }

    /**
     * WB_Polygon的创建原理
     *
     * @param poly WB_Polygon
     * @return WB_Polygon
     */
    public static WB_Polygon polyDup(WB_Polygon poly) {
        WB_Point[] out = getShellPts(poly);
        WB_Point[][] in = getInnerPts(poly);
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
        WB_Point[] pts = applyToPts((WB_Point[]) poly.getPoints().toArray(), T);
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
        WB_Point[] out = applyToPts(getShellPts(poly), T);
        WB_Point[][] in = applyToPts(getInnerPts(poly), T);
        return new WB_Polygon(out, in);
    }

    public static WB_Point[] applyToPts(List<WB_Point> pts, WB_Transform3D T) {
        WB_Point[] ptsT = new WB_Point[pts.size()];
        for (int i = 0; i < pts.size(); i++) {
            ptsT[i] = new WB_Point(pts.get(i)).apply(T);
        }
        return ptsT;
    }

    public static WB_Point[] applyToPts(WB_Point[] pts, WB_Transform3D T) {
        return applyToPts(Arrays.asList(pts), T);
    }

    public static WB_Point[][] applyToPts(WB_Point[][] pts, WB_Transform3D T) {
        WB_Point[][] ptsT = new WB_Point[pts.length][];
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
