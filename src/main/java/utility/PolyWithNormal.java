package utility;

import processing.opengl.PGraphicsOpenGL;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;
import wblut.geom.WB_Segment;
import wblut.geom.WB_Triangle;
import wblut.processing.WB_Render3D;

/**
 * display WB_Polygon with its normal
 */

public class PolyWithNormal {
    WB_Polygon poly;
    WB_Point pt;
    WB_Segment seg;
    static final int LEN = 10;

    public PolyWithNormal(WB_Polygon poly) {
        this.poly = poly;
        set();
    }

    public PolyWithNormal(WB_Triangle tri) {
        this(PolyHandler.gf.createSimplePolygon(
                tri.getPoint(0),
                tri.getPoint(1),
                tri.getPoint(2)));
    }

    private void set() {
        WB_Point center = poly.getCenter();
        pt = center.add(poly.getNormal().mul(LEN));
        seg = new WB_Segment(center, pt);
    }

    public void draw(WB_Render3D render) {
        PGraphicsOpenGL app = render.getHome();
        app.pushStyle();
        app.noStroke();
        render.drawPolygon(poly);

        app.stroke(0x22000000);
        app.noFill();
        render.drawPolygonEdges(poly);

//        render.drawSegment(seg);
//        app.fill(255);
//        render.drawPoint(pt, 1);
        app.popStyle();
    }
}