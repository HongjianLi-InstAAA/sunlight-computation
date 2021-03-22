package core;

import gzf.gui.CameraController;
import gzf.gui.Vec_Guo;
import org.locationtech.jts.geom.Geometry;
import processing.core.PApplet;
import utility.CtrlPanel;
import utility.JtsRender;
import utility.PolyHandler;
import wblut.geom.WB_Point;
import wblut.processing.WB_Render;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * facet shadow test
 *
 * @author Wu
 * @create 2021-03-08 16:47
 */

public class Test extends PApplet {
    public static void main(String[] args) {
        PApplet.main("core.Test");
    }

    CameraController cam;
    WB_Render render;
    JtsRender jtsRender;

    Sun sun;
    CtrlPanel panel;
    int[] location, date, time;
    int pathDiv = 50;

    Building[] buildings;

    Shadow.Type type = Shadow.Type.FACET;
    Geometry shadow;
    WB_Point sample;
    DurationAnalysis analysis;
    int gridSubdiv = 100;

    boolean ifShowShadow, ifShowAllDayShadow, ifShowGrid;
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

        String objPath;
        String curDir = System.getProperty("user.dir") + "\\";
        File directory = new File(curDir);
        String[] arr = directory.list((dir, name) -> {
            File file = new File(dir, name);
            return file.isFile() && file.getName().endsWith(".obj");
        });
        objPath = curDir + arr[0];

        Building building = new Building(PolyHandler.obj2tris(objPath));
        List<Building> buildingList = new ArrayList<>();
        buildingList.add(building);

        buildings = new Building[buildingList.size()];
        buildingList.toArray(buildings);

        analysis = new DurationAnalysis(type, sun, buildings);
        sample = PolyHandler.ORIGIN;
        update();
        updateGrid();

        ifShowShadow = false;
        ifShowAllDayShadow = false;
        ifShowGrid = true;
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
            if (state == CtrlPanel.updateState.UPDATE_PATH) {
                analysis.update();
                if (ifShowGrid)
                    updateGrid();
            }
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
        // draw grid
        if (ifShowGrid)
            analysis.displayGrid(this);
    }

    private void update() {
        shadow = Shadow.calCurrentShadow(type, sun, buildings);
        analysis.pointAnalysis(sample);

    }

    private void updateGrid() {
        analysis.gridAnalysis(
                new WB_Point(-Sun.groundRadius, -Sun.groundRadius),
                new WB_Point(Sun.groundRadius, Sun.groundRadius),
                gridSubdiv, gridSubdiv
        );
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
        if (key == 'g' || key == 'G')
            ifShowGrid = !ifShowGrid;
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