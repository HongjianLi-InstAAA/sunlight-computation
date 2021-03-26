import core.*;
import gzf.gui.CameraController;
import gzf.gui.Vec_Guo;
import org.locationtech.jts.geom.Geometry;
import processing.core.PApplet;
import utility.CtrlPanel;
import utility.JtsRender;
import utility.PolyHandler;
import wblut.geom.WB_Point;
import wblut.geom.WB_Vector;
import wblut.processing.WB_Render;

import java.util.ArrayList;
import java.util.List;

/**
 * facet shadow test
 *
 * @author Wu
 * @create 2021-03-08 16:47
 */

public class FacetTest extends PApplet {
    public static void main(String[] args) {
        PApplet.main("FacetTest");
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
    SamplingPoint sample;
    DurationAnalysis analysis;
    int gridSubdiv = 100;

    boolean ifShowShadow = false;
    boolean ifShowAllDayShadow = false;
    boolean ifShowGrid = true;
    boolean alt = false;

    public void settings() {
        size(1000, 800, P3D);
    }

    public void setup() {
        cam = new CameraController(this, Sun.groundRadius * 2);
        render = new WB_Render(this);
        jtsRender = new JtsRender(this);

        sun = new Sun();
        panel = new CtrlPanel(sun);
        sun.setPathDiv(pathDiv);

        location = sun.getLocation();
        date = sun.getDate();
        time = sun.getTime();

        String objPath = "src\\test\\resources\\buildings.obj";
        Building building = new Building(PolyHandler.reverseObj(objPath));
        List<Building> buildingList = new ArrayList<>();
        buildingList.add(building);

        buildings = new Building[buildingList.size()];
        buildingList.toArray(buildings);

        analysis = new DurationAnalysis(type, sun, buildings);
        sample = new SamplingPoint(PolyHandler.ORIGIN, WB_Vector.Z());
        update();
        updateGrid();
    }

    public void draw() {
        background(255);
        cam.drawSystem(Sun.groundRadius);

        sun.displayPath(render);
        sun.display(render);

        CtrlPanel.updateState state = panel.updateInput(location, date, time);
        if (CtrlPanel.updateState.NONE != state) {
            update();
            if (state == CtrlPanel.updateState.UPDATE_PATH) {
                analysis.updateAllDayShadow();
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
        if (key == 't' || key == 'T') cam.top();
        if (key == 'f' || key == 'F') cam.front();
        if (key == 'p' || key == 'P') cam.perspective();

        if (keyCode == ALT) alt = true;

        if (key == 's' || key == 'S') ifShowShadow = !ifShowShadow;
        if (key == 'a' || key == 'A') ifShowAllDayShadow = !ifShowAllDayShadow;
        if (key == 'g' || key == 'G') ifShowGrid = !ifShowGrid;
    }

    public void keyReleased() {
        if (keyCode == ALT) alt = false;
    }

    public void mouseReleased() {
        if (mouseButton == LEFT && alt) {
            Vec_Guo pick = cam.pick3dXYPlane(mouseX, mouseY);
            sample.getPoint().set(pick.x, pick.y);
            analysis.pointAnalysis(sample);
        }
    }

}