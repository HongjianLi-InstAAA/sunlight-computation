import core.*;
import gzf.gui.CameraController;
import processing.core.PApplet;
import utility.CtrlPanel;
import utility.JtsRender;
import utility.PolyHandler;
import wblut.processing.WB_Render;

/**
 * duration of a space point test
 *
 * @author Wu
 * @create 2021-03-08 16:47
 */

public class SpacePointTest extends PApplet {
    public static void main(String[] args) {
        PApplet.main("SpacePointTest");
    }

    CameraController cam;
    WB_Render render;
    JtsRender jtsRender;

    Scene scene;

    Sun sun;
    CtrlPanel panel;
    int pathDiv = 50;

    DurationAnalysis analysis;
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

//        String objPath;
//        String curDir = System.getProperty("user.dir") + "\\";
//        File directory = new File(curDir);
//        String[] arr = directory.list((dir, name) -> {
//            File file = new File(dir, name);
//            return file.isFile() && file.getName().endsWith(".obj");
//        });
//        objPath = curDir + arr[0];

        scene = new Scene(cam, sun, panel);

        String objPath = "src\\test\\resources\\buildings.obj";
        Building building = new Building(PolyHandler.reverseObj(objPath));
        scene.addBuilding(building);

        analysis = new DurationAnalysis(scene);
        alt = false;
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
        if (key == 't' || key == 'T')
            cam.top();
        if (key == 'f' || key == 'F')
            cam.front();
        if (key == 'p' || key == 'P')
            cam.perspective();

        if (keyCode == ALT)
            alt = true;

        if (key == 's' || key == 'S')
            scene.reverseShowShadow();
        if (key == 'a' || key == 'A')
            scene.reverseShowAllDayShadow();
        if (key == 'g' || key == 'G')
            scene.reverseShowGrid();
    }

    public void keyReleased() {
        if (keyCode == ALT)
            alt = false;
    }

    public void mouseReleased() {
        if (mouseButton == LEFT && alt)
            scene.capture3d(this);
    }

}