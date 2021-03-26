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

    Scene scene;
    Sun sun;
    CtrlPanel panel;
    int pathDiv = 50;

    DurationAnalysis analysis;
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

        String objPath = "src\\test\\resources\\buildings.obj";
        Building building = new Building(PolyHandler.reverseObj(objPath));
        scene.addBuilding(building);

        analysis = new DurationAnalysis(scene);
    }

    public void draw() {
        background(255);
        cam.drawSystem(Sun.groundRadius);

        scene.refresh();

        scene.displaySun(render);
        scene.displayBuildings(render);
        scene.displayAnalysis(jtsRender, this);
    }

    public void keyPressed() {
        if (key == 't' || key == 'T') cam.top();
        if (key == 'f' || key == 'F') cam.front();
        if (key == 'p' || key == 'P') cam.perspective();

        if (keyCode == ALT) alt = true;

        if (key == 's' || key == 'S') scene.reverseShowShadow();
        if (key == 'a' || key == 'A') scene.reverseShowAllDayShadow();
        if (key == 'g' || key == 'G') scene.reverseShowGrid();
    }

    public void keyReleased() {
        if (keyCode == ALT) alt = false;
    }

    public void mouseReleased() {
        if (mouseButton == LEFT && alt)
            scene.capture2d(this);
    }

}