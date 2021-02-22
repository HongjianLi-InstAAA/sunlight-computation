package computation;

import gzf.gui.CameraController;
import org.locationtech.jts.geom.Geometry;
import processing.core.PApplet;
import wblut.geom.WB_GeometryFactory;
import wblut.geom.WB_Polygon;
import wblut.geom.WB_Vector;
import wblut.hemesh.HEC_Polygon;
import wblut.hemesh.HE_Mesh;
import wblut.processing.WB_Render;

/**
 * @author Wu
 * @create 2021-02-22 17:46
 */

public class ZTestShadow extends PApplet {
    public static void main(String[] args) {
        PApplet.main("computation.ZTestShadow");
    }

    static final WB_GeometryFactory gf = new WB_GeometryFactory();

    CameraController cam;
    WB_Render render;
    JtsRender jtsRender;

    Sun sun;
    CtrlPanel panel;
    WB_Vector panelLoc = new WB_Vector(100, 100);

    int[] location, date, time;

    WB_Vector[] shape = new WB_Vector[]{
            new WB_Vector(-500, -500),
            new WB_Vector(500, -500),
            new WB_Vector(500, 200),
            new WB_Vector(0, 200),
            new WB_Vector(0, 500),
            new WB_Vector(-500, 500)};
    WB_Vector[] hole = new WB_Vector[]{
            new WB_Vector(-200, 0),
            new WB_Vector(100, 0),
            new WB_Vector(100, -300),
            new WB_Vector(-200, -300)};
    WB_Polygon base;
    HE_Mesh mesh;

    int shapeHeight = 30;
    Geometry shadow;

    public void settings() {
        size(1000, 800, P3D);
    }

    public void setup() {
        cam = new CameraController(this, Sun.groundRadius * 2);
        render = new WB_Render(this);
        jtsRender = new JtsRender(this);

        panel = new CtrlPanel(panelLoc);
        sun = new Sun();
        sun.calSunPath();

        location = sun.getLocation();
        date = sun.getDate();
        time = sun.getTime();

        base = gf.createPolygonWithHole(Shadow.reversePts(shape),
                Shadow.reversePts(hole));
        HEC_Polygon creator = new HEC_Polygon(base, shapeHeight);
        mesh = new HE_Mesh(creator);
    }

    public void draw() {
        background(255);
        cam.drawSystem(Sun.groundRadius);

        shadow = Shadow.calShadow(sun, gf.createPolygonWithHole(shape, hole), shapeHeight);

        sun.displayPath(render);
        sun.display(render);
        panel.updateInput(sun, location, date, time);

        pushStyle();
        fill(0x30000000);
        noStroke();
        if (null != shadow)
            jtsRender.draw(shadow);

        if (sun.getPosition().zd() > 0)
            fill(255);
        else
            fill(150);
        stroke(0);
        strokeWeight(1);
        render.drawMeshEdges(mesh.toFacelistMesh());
        popStyle();
    }

    public void keyPressed() {
        if (key == 't' || key == 'T')
            cam.top();
        if (key == 'f' || key == 'F')
            cam.front();
        if (key == 'p' || key == 'P')
            cam.perspective();
    }

}
