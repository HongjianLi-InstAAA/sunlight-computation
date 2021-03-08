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
import wblut.geom.WB_Point;
import wblut.processing.WB_Render;

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

    int[] location, date, time;

    Building building;
    int buildingHeight = 15;

    Geometry shadow;

    public void settings() {
        size(1000, 800, P3D);
    }

    public void setup() {
        cam = new CameraController(this, Sun.groundRadius * 2);
        render = new WB_Render(this);
        jtsRender = new JtsRender(this);

        panel = new CtrlPanel();
        sun = new Sun();

        location = sun.getLocation();
        date = sun.getDate();
        time = sun.getTime();

        WB_Point[] shell = new WB_Point[]{
                new WB_Point(-50, -50),
                new WB_Point(50, -50),
                new WB_Point(50, 20),
                new WB_Point(0, 20),
                new WB_Point(0, 50),
                new WB_Point(-50, 50)};
        WB_Point[] hole = new WB_Point[]{
                new WB_Point(-20, 0),
                new WB_Point(10, 0),
                new WB_Point(10, -30),
                new WB_Point(-20, -30)};
        building = new Building(PolyHandler.gf.createPolygonWithHole(
                PolyHandler.reversePts(shell), PolyHandler.reversePts(hole)),
                buildingHeight);
        shadow = Shadow.calCurrentShadow(Shadow.Type.VOLUME, sun, building);
    }

    public void draw() {
        background(255);
        cam.drawSystem(Sun.groundRadius);

        sun.displayPath(render);
        sun.display(render);

        if (CtrlPanel.updateState.NONE != panel.updateInput(sun, location, date, time))
            shadow = Shadow.calCurrentShadow(Shadow.Type.VOLUME, sun, building);

        pushStyle();
        // draw shadows
        fill(0x300000ff);
        noStroke();
        jtsRender.draw(shadow);

        // draw buildings
        if (sun.getPosition().zd() <= 0)
            fill(150);
        else
            fill(255);
        stroke(0);
        strokeWeight(1);
        building.display(sun, render);
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
