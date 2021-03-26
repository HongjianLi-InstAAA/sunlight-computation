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

    private WB_Point point;
    private WB_Coord normal;
    private WB_Point pointAbovePlane;
    private WB_Vector axis;
    private float angle;
    private double duration; // in HOURS

    public SamplingPoint(WB_Point point) {
        this(point, WB_Vector.Z());
    }

    public SamplingPoint(WB_Point point, WB_Coord normal) {
        setPoint(point, normal);
    }

    public void setPoint(WB_Point point) {
        if (null == normal)
            setPoint(point, WB_Vector.Z());
        else
            setPoint(point, normal);
    }

    public void setPoint(WB_Point point, WB_Coord normal) {
        this.point = point;
        this.normal = normal;
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
