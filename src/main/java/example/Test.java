package example;

import core.Building;
import core.DurationAnalysis;
import core.Scene;
import core.Sun;
import gzf.gui.CameraController;
import processing.core.PApplet;
import utility.CtrlPanel;
import utility.IOHandler;
import utility.JtsRender;
import wblut.hemesh.HE_Mesh;
import wblut.processing.WB_Render;

import java.io.File;
import java.util.List;

/**
 * duration of a space point test
 *
 * @author Wu
 * @create 2021-03-08 16:47
 */

public class Test extends PApplet {
    public static void main(String[] args) {
        PApplet.main("example.Test");
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

        // initialize the sun and panel
        sun = new Sun();
        sun.setPathDiv(pathDiv);
        panel = new CtrlPanel(sun);
        // add the camera, sun and panel to the scene
        scene = new Scene(cam, sun, panel);

        // add the buildings to the scene
        // get the first .obj file from the current directory
        String objPath = System.getProperty("user.dir") + "\\";
        File directory = new File(objPath);
        String[] arr = directory.list((dir, name) -> {
            File file = new File(dir, name);
            return file.isFile() && file.getName().endsWith(".obj");
        });
        assert arr != null : "no .obj file found";
        objPath += arr[0];
        // OR
        // specify the .obj file path
//        String objPath = "src\\test\\resources\\buildings.obj";
        List<HE_Mesh> meshes = IOHandler.readFromOBJFile(objPath);
        for (HE_Mesh m : meshes)
            scene.addBuilding(new Building(IOHandler.switchObjYZ(m)));

        // initialize the analysis
        analysis = new DurationAnalysis(scene);
    }

    public void draw() {
        background(255);
        cam.drawSystem(Sun.groundRadius);
        if (sun.getPosition().zd() > 0) {
            directionalLight(240, 240, 240,
                    sun.getPosition().xf(), sun.getPosition().yf(), sun.getPosition().zf());
            lightFalloff(1, 0.0001f, 0);
            ambientLight(200, 200, 200);
        }

        scene.refresh();

        scene.displaySun(render);
        scene.displayBuildings(render);
        scene.displayAnalysis(jtsRender, this);

        for (Building b : scene.getBuildings())
            b.displayAABB(render);
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
        if (mouseButton == LEFT && alt) scene.capture3d(this);
    }

}