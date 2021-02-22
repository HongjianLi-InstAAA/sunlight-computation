package guo;


import gzf.gui.Vec_Guo;
import org.locationtech.jts.geom.*;

public class ShadowCalculator {
    private static final GeometryFactory gf = new GeometryFactory();

    private static Polygon toPolygon(Vec_Guo[] v) {
        int len = v.length;
        Coordinate[] coord = new Coordinate[len + 1];
        for (int i = 0; i < len; i++) {
            coord[i] = new Coordinate(v[i].x, v[i].y);
        }
        coord[len] = coord[0];

        return gf.createPolygon(coord);
    }

    /**
     * counterclockwise shape required, less accurate but more efficient
     *
     * @param light        Vec_Guo
     * @param shape        Vec_Guo[]
     * @param shapeHeight  double
     * @param groundHeight double
     * @return Geometry
     */
    public static Geometry getShadowOnGroundRough(Vec_Guo light, Vec_Guo[] shape, double shapeHeight, double groundHeight) {
        if (shapeHeight < groundHeight)
            return null;
        if (shapeHeight < 0)
            throw new IllegalArgumentException("shape height must be positive!");
        if (light.z >= 0)
            return null;

        int len = shape.length;

        double scale = (shapeHeight - groundHeight) / Math.abs(light.z);

        Vec_Guo lightGround = light.dup().z(0).mul(scale);

        Vec_Guo lightNormal = Vec_Guo.zaxis.cross(lightGround).unit();


        double maxP = Double.NEGATIVE_INFINITY;
        double minP = Double.POSITIVE_INFINITY;

        // right
        int minIndex = -1;
        // left
        int maxIndex = -1;

        for (int i = 0; i < len; i++) {
            double p = shape[i].dot(lightNormal);
            if (p > maxP) {
                maxP = p;
                maxIndex = i;
            }

            if (p < minP) {
                minP = p;
                minIndex = i;
            }
        }

        Vec_Guo[] shapeOut = new Vec_Guo[len + 2];

        int count = 0;
        boolean add = false;
        for (int i = 0; i < len; i++) {
            // start from the right
            int index = (i + minIndex) % len;
            if (index == minIndex) {
                shapeOut[count] = shape[index];
                count++;
                shapeOut[count] = shape[index].dup().add(lightGround);
                add = true;
            } else if (index == maxIndex) {
                shapeOut[count] = shape[index].dup().add(lightGround);
                count++;
                shapeOut[count] = shape[index];
                add = false;
            } else {
                if (add) {
                    shapeOut[count] = shape[index].dup().add(lightGround);
                } else {
                    shapeOut[count] = shape[index];
                }
            }
            count++;
        }

        return toPolygon(shapeOut);
    }

    /**
     * accurate but slow
     *
     * @param lig          Vec_Guo
     * @param shape        Vec_Guo[]
     * @param shapeHeight  double
     * @param groundHeight double
     * @return Geometry
     */
    public static Geometry getShadowOnGroundPrecise(Vec_Guo lig, Vec_Guo[] shape, double shapeHeight, double groundHeight) {
        Vec_Guo light = new Vec_Guo(-lig.x, -lig.y, -lig.z);
        if (shapeHeight < groundHeight)
            return null;
        if (shapeHeight < 0)
            throw new IllegalArgumentException("shape height must be positive!");
        if (light.z >= 0)
            return null;

        int len = shape.length;

        double scale = (shapeHeight - groundHeight) / Math.abs(light.z);

        Vec_Guo lightGround = light.dup().z(0).mul(scale);

        Geometry[] geos = new Geometry[len + 1];
        for (int i = 0; i < len; i++) {
            int n = (i + 1) % len;
            Vec_Guo[] shadow = new Vec_Guo[4];
            shadow[0] = shape[i];
            shadow[1] = shape[n];
            shadow[2] = shape[n].dup().add(lightGround);
            shadow[3] = shape[i].dup().add(lightGround);

            geos[i] = toPolygon(shadow);
        }

        Vec_Guo[] shadow = new Vec_Guo[len];
        for (int i = 0; i < len; i++) {
            shadow[i] = shape[i].dup().add(lightGround);
        }

        geos[len] = toPolygon(shadow);

        GeometryCollection gc = gf.createGeometryCollection(geos);

        return gc.buffer(0);
    }

}
