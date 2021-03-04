package test;

import core.Building;
import core.DurationAnalysis;
import core.Shadow;
import core.Sun;
import gzf.gui.CameraController;
import gzf.gui.Vec_Guo;
import org.locationtech.jts.geom.Geometry;
import processing.core.PApplet;
import utility.CtrlPanel;
import utility.JtsRender;
import utility.PolyHandler;
import wblut.geom.WB_Vector;
import wblut.processing.WB_Render;

import java.util.ArrayList;
import java.util.List;

/**
 * point analysis of sunlight hours test
 *
 * @author Wu
 * @create 2021-02-27 9:55
 */

public class PointAnalysisTest extends PApplet {
    public static void main(String[] args) {
        PApplet.main("test.PointAnalysisTest");
    }

    CameraController cam;
    WB_Render render;
    JtsRender jtsRender;

    Sun sun;
    CtrlPanel panel;
    WB_Vector panelLoc = new WB_Vector(100, 100);

    int[] location, date, time;

    Building[] buildings;
    int buildingHeight = 30;

    Geometry shadow;
    WB_Vector sample = new WB_Vector(40, 30);
    DurationAnalysis analysis;

    boolean ifShowAllDayShadow;
    boolean alt;

    public void settings() {
        size(1000, 800, P3D);
    }

    public void setup() {
        cam = new CameraController(this, Sun.groundRadius * 2);
        render = new WB_Render(this);
        jtsRender = new JtsRender(this);

        panel = new CtrlPanel(panelLoc);
        sun = new Sun();
        sun.setPathDiv(30);

        location = sun.getLocation();
        date = sun.getDate();
        time = sun.getTime();

        WB_Vector[] shell = new WB_Vector[]{
                new WB_Vector(-50, -50),
                new WB_Vector(50, -50),
                new WB_Vector(50, 20),
                new WB_Vector(0, 20),
                new WB_Vector(0, 50),
                new WB_Vector(-50, 50)
        };
        WB_Vector[] hole = new WB_Vector[]{
                new WB_Vector(-20, 0),
                new WB_Vector(10, 0),
                new WB_Vector(10, -30),
                new WB_Vector(-20, -30)
        };
        WB_Vector[] poly2 = new WB_Vector[]{
                new WB_Vector(80, 40),
                new WB_Vector(120, 50),
                new WB_Vector(120, 70),
                new WB_Vector(180, 70),
                new WB_Vector(180, 150),
                new WB_Vector(140, 150),
                new WB_Vector(140, 120),
                new WB_Vector(80, 120)
        };
        Building building0 = new Building(
                PolyHandler.gf.createPolygonWithHole(
                        PolyHandler.reversePts(shell),
                        PolyHandler.reversePts(hole)),
                buildingHeight);
        Building building1 = new Building(
                PolyHandler.gf.createSimplePolygon(
                        PolyHandler.reversePts(poly2)),
                buildingHeight * 2);
        List<Building> buildingList = new ArrayList<>();
        buildingList.add(building0);
        buildingList.add(building1);

        buildings = new Building[buildingList.size()];
        buildingList.toArray(buildings);

        analysis = new DurationAnalysis(sun, buildings);
        update();

        ifShowAllDayShadow = false;
        alt = false;
    }

    public void draw() {
        background(255);
        cam.drawSystem(Sun.groundRadius);

        sun.displayPath(render);
        sun.display(render);
        if (panel.updateInput(sun, location, date, time)) {
            update();
        }

        pushStyle();
        // draw shadows
        fill(0x300000ff);
        noStroke();
        if (null != shadow)
            jtsRender.draw(shadow);

        if (ifShowAllDayShadow) {
            fill(0x10000000);
            analysis.displayAllDayShadow(jtsRender);
        }

        // draw buildings
        if (sun.getPosition().zd() <= 0 && !sun.isPolar())
            fill(150);
        else
            fill(255);
        stroke(0);
        strokeWeight(1);
        for (Building building : buildings) {
            building.display(render);
        }

        // draw samples
        stroke(0, 255, 0);
        strokeWeight(10);
        analysis.displaySample(this);
        popStyle();
    }

    private void update() {
        shadow = Shadow.calCurrentShadow(sun, buildings);
        analysis.update();
        analysis.pointAnalysis(sample);
    }

    public void keyPressed() {
        if (key == 't' || key == 'T')
            cam.top();
        if (key == 'f' || key == 'F')
            cam.front();
        if (key == 'p' || key == 'P')
            cam.perspective();

        if (keyCode == ALT)
            alt = true;

        if (key == 'a' || key == 'A')
            ifShowAllDayShadow = !ifShowAllDayShadow;
    }

    public void keyReleased(){
        if (keyCode == ALT)
            alt = false;
    }

    public void mouseReleased() {
        if (mouseButton == LEFT && alt) {
            Vec_Guo pick = cam.pick3dXYPlane(mouseX, mouseY);
            sample.set(pick.x, pick.y);
            analysis.pointAnalysis(sample);
        }
    }

}
