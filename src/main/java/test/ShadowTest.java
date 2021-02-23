package test;

import core.Shadow;
import core.Sun;
import gzf.gui.CameraController;
import org.locationtech.jts.geom.Geometry;
import processing.core.PApplet;
import utility.CtrlPanel;
import utility.JtsRender;
import utility.PolyAnalysis;
import utility.PolyHandler;
import wblut.geom.WB_Polygon;
import wblut.geom.WB_Vector;
import wblut.hemesh.HEC_Polygon;
import wblut.hemesh.HE_Face;
import wblut.hemesh.HE_Mesh;
import wblut.processing.WB_Render;

import java.util.ArrayList;
import java.util.List;

/**
 * shadow calculator test
 *
 * @author Wu
 * @create 2021-02-22 17:46
 */

public class ShadowTest extends PApplet {
    public static void main(String[] args) {
        PApplet.main("test.ShadowTest");
    }

    CameraController cam;
    WB_Render render;
    JtsRender jtsRender;

    Sun sun;
    CtrlPanel panel;
    WB_Vector panelLoc = new WB_Vector(100, 100);

    int[] location, date, time;

    WB_Vector[] shape = new WB_Vector[]{
            new WB_Vector(-50, -50),
            new WB_Vector(50, -50),
            new WB_Vector(50, 20),
            new WB_Vector(0, 20),
            new WB_Vector(0, 50),
            new WB_Vector(-50, 50)};
    WB_Vector[] hole = new WB_Vector[]{
            new WB_Vector(-20, 0),
            new WB_Vector(10, 0),
            new WB_Vector(10, -30),
            new WB_Vector(-20, -30)};
    WB_Polygon base;
    HE_Mesh mesh;
    private List<PolyAnalysis> pas;

    int shapeHeight = 15;
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

        base = PolyHandler.gf.createPolygonWithHole(
                PolyHandler.reversePts(shape),
                PolyHandler.reversePts(hole));
//        base = PolyHandler.gf.createSimplePolygon(Shadow.reversePts(shape));
        HEC_Polygon creator = new HEC_Polygon(base, shapeHeight);
        mesh = new HE_Mesh(creator);
        pas = new ArrayList<>();
        for (HE_Face f : mesh.getFaces()) {
            pas.add(new PolyAnalysis(f.getPolygon()));
        }
    }

    public void draw() {
        background(255);
        cam.drawSystem(Sun.groundRadius);

        shadow = Shadow.calShadow(sun,
                PolyHandler.gf.createPolygonWithHole(shape, hole), shapeHeight);

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
        for(PolyAnalysis p:pas)
            p.draw(render);
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
