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

/**
 * grid analysis of sunlight hours test
 *
 * @author Wu
 * @create 2021-02-27 9:55
 */

public class GridAnalysisTest extends PApplet {
    public static void main(String[] args) {
        PApplet.main("GridAnalysisTest");
    }

    CameraController cam;
    WB_Render render;
    JtsRender jtsRender;

    Scene scene;
    Sun sun;
    CtrlPanel panel;
    int[] location, date, time;
    int pathDiv = 50;

    int buildingHeight = 30;

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
        sun.setPathDiv(pathDiv);
        panel = new CtrlPanel(sun);
        scene = new Scene(cam, sun, panel);
        scene.setShadowType(Shadow.Type.VOLUME);

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
        scene.addBuilding(building0);
        scene.addBuilding(building1);

        analysis = new DurationAnalysis(scene);
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
        for (Building building : scene.getBuildings())
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
        shadow = Shadow.calCurrentShadow(scene);
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
            scene.capture2d(this);
        }
    }

}