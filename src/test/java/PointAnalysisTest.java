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
import wblut.geom.WB_Point;
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
        PApplet.main("PointAnalysisTest");
    }

    CameraController cam;
    WB_Render render;
    JtsRender jtsRender;

    Sun sun;
    CtrlPanel panel;
    int pathDiv = 50;

    int[] location, date, time;

    Building[] buildings;
    int buildingHeight = 30;

    Geometry shadow;
    WB_Point sample;
    DurationAnalysis analysis;

    boolean ifShowShadow, ifShowAllDayShadow;
    boolean alt;

    public void settings() {
        size(1000, 800, P3D);
    }

    public void setup() {
        cam = new CameraController(this, Sun.groundRadius * 2);
        render = new WB_Render(this);
        jtsRender = new JtsRender(this);

        panel = new CtrlPanel();
        sun = new Sun();
        sun.setPathDiv(pathDiv);

        location = sun.getLocation();
        date = sun.getDate();
        time = sun.getTime();

        WB_Point[] shell = new WB_Point[]{
                new WB_Point(-50, -50),
                new WB_Point(50, -50),
                new WB_Point(50, 20),
                new WB_Point(0, 20),
                new WB_Point(0, 50),
                new WB_Point(-50, 50)
        };
        WB_Point[] hole = new WB_Point[]{
                new WB_Point(-20, 0),
                new WB_Point(10, 0),
                new WB_Point(10, -30),
                new WB_Point(-20, -30)
        };
        WB_Point[] poly2 = new WB_Point[]{
                new WB_Point(80, 40),
                new WB_Point(120, 50),
                new WB_Point(120, 70),
                new WB_Point(180, 70),
                new WB_Point(180, 150),
                new WB_Point(140, 150),
                new WB_Point(140, 120),
                new WB_Point(80, 120)
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
        sample = PolyHandler.ORIGIN;
        ifShowShadow = true;
        ifShowAllDayShadow = false;
        alt = false;
    }

    public void draw() {
        background(255);
        cam.drawSystem(Sun.groundRadius);

        sun.displayPath(render);
        sun.display(render);

        CtrlPanel.updateState state = panel.updateInput(sun, location, date, time);
        if (CtrlPanel.updateState.NONE != state) {
            update();
            if (CtrlPanel.updateState.UPDATE_PATH == state)
                analysis.update();
        }

        // draw buildings
        for (Building building : buildings)
            building.display(sun, render);

        // draw shadows
        if (ifShowShadow)
            Shadow.displayShadow(shadow, jtsRender);
        // draw all day shadow
        if (ifShowAllDayShadow)
            analysis.displayAllDayShadow(jtsRender);

        // draw samples
        analysis.displaySample(this);
    }

    private void update() {
        shadow = Shadow.calCurrentShadow(Shadow.Type.VOLUME, sun, buildings);
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

        if (key == 's' || key == 'S')
            ifShowShadow = !ifShowShadow;
        if (key == 'a' || key == 'A')
            ifShowAllDayShadow = !ifShowAllDayShadow;
    }

    public void keyReleased() {
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