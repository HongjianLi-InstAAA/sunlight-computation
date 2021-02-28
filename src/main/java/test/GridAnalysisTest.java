package test;

import core.Building;
import core.Shadow;
import core.Sun;
import gzf.gui.CameraController;
import org.locationtech.jts.geom.Geometry;
import processing.core.PApplet;
import utility.CtrlPanel;
import utility.JtsRender;
import utility.PolyHandler;
import wblut.geom.WB_GeometryOp;
import wblut.geom.WB_Vector;
import wblut.processing.WB_Render;

import java.util.Objects;

/**
 * grid analysis of sunlight hours test
 *
 * @author Wu
 * @create 2021-02-27 9:55
 */

public class GridAnalysisTest extends PApplet {
    public static void main(String[] args) {
        PApplet.main("test.GridAnalysisTest");
    }

    CameraController cam;
    WB_Render render;
    JtsRender jtsRender;

    Sun sun;
    CtrlPanel panel;
    WB_Vector panelLoc = new WB_Vector(100, 100);

    int[] location, date, time;

    Building building;
    int buildingHeight = 15;

    Geometry shadow;
    WB_Vector sample = new WB_Vector(40, 30);

    public void settings() {
        size(1000, 800, P3D);
    }

    public void setup() {
        cam = new CameraController(this, Sun.groundRadius * 2);
        render = new WB_Render(this);
        jtsRender = new JtsRender(this);

        panel = new CtrlPanel(panelLoc);
        sun = new Sun();
        sun.setPathDiv(25);

        location = sun.getLocation();
        date = sun.getDate();
        time = sun.getTime();

        WB_Vector[] shell = new WB_Vector[]{
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
        building = new Building(PolyHandler.gf.createPolygonWithHole(
                PolyHandler.reversePts(shell), PolyHandler.reversePts(hole)),
                buildingHeight);
        shadow = Shadow.calCurrentShadow(sun, building);

        Geometry[] allDayShadow = Shadow.calAllDayShadow(sun, building);
        int counter = 0;
        for (Geometry g : allDayShadow) {
            if (null == g || !WB_GeometryOp.contains2D(sample,
                    Objects.requireNonNull(PolyHandler.toWB_Polygon(g))))
                counter++;
        }
        System.out.printf("counter: %d, duration: %.2f hours\n", counter,
                sun.getSunlightDuration() * counter / sun.getPathDiv());
    }

    public void draw() {
        background(255);
        cam.drawSystem(Sun.groundRadius);

        sun.displayPath(render);
        sun.display(render);
        if (panel.updateInput(sun, location, date, time)) {
            shadow = Shadow.calCurrentShadow(sun, building);
        }

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
        building.display(render);

        stroke(0, 255, 0);
        strokeWeight(10);
        point(sample.xf(), sample.yf());
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
