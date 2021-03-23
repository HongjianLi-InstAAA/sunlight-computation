package core;

import wblut.geom.WB_Coord;
import wblut.geom.WB_Point;
import wblut.geom.WB_Vector;

/**
 * sampling point for duration calculation
 *
 * @author Wu
 * @create 2021-03-23 15:23
 */

public class SamplingPoint {

    private final WB_Point point;
    private final WB_Point pointAbovePlane;
    private final WB_Vector axis;
    private final float angle;
    private double duration; // in HOURS

    public SamplingPoint(WB_Point point) {
        this(point, WB_Vector.Z());
    }

    public SamplingPoint(WB_Point point, WB_Coord normal) {
        this.point = point;
        axis = WB_Vector.cross(normal, WB_Vector.Z());
        angle = (float) WB_Vector.getAngle(axis, WB_Vector.Z());
        pointAbovePlane = point.add(WB_Vector.mul(normal, 1));
    }

    public void setDuration(double d) {
        this.duration = d;
    }

    public WB_Point getPoint() {
        return point;
    }

    public WB_Point getPointAbovePlane() {
        return pointAbovePlane;
    }

    public WB_Vector getAxis() {
        return axis;
    }

    public float getAngle() {
        return angle;
    }

    public double getDuration() {
        return duration;
    }
}
