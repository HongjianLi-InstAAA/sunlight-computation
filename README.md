# sunlight computation
[![semantic-release](https://img.shields.io/badge/%20%20%F0%9F%93%A6%F0%9F%9A%80-semantic--release-e10079.svg)](https://github.com/semantic-release/semantic-release)

Sunlight-computation is a Java library for building sunlight analysis which supports external OBJ files as input. It implements real-time and all-day shadow computation, and provides point analysis of sunlight duration.
## Dependencies
- [Processing-3.3.7](https://github.com/processing/processing/releases/tag/processing-0264-3.3.7)
- [HE_Mesh](https://github.com/wblut/HE_Mesh)
- [ControlP5](https://github.com/sojamo/controlp5)
- [Jama-1.0.3](https://math.nist.gov/javanumerics/jama/)
- Guo_Cam
## Core
core primitive and calculator
### Sun
sun calculator for any location, date or time with algorithm accuracy < 1°
### Shadow
shadow calculator for the sun at a given position
- calculate shadow at the current time
- calculate all-day shadow on a given date
### Building
building for sunlight computation
- create from a base polygon and height
- create from a HE_Mesh
### Scene
scene with a camera, sun, control panel and buildings
### DurationAnalysis
analysis of sunlight duration at a given point in the scene
- point analysis
- grid analysis
## Utility
toolkit for geometric operation and rendering
### CtrlPanel
the second PApplet window for controllers
### JtsRender
renderer for JTS primitives
### PolyWithNormal
polygon with normal to check if the front is outward
### PolyHandler
convert between JTS Polygon and HE_Mesh WB_Polygon
### IOHandler
handle input/output issues
## Example
```java
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
 * example program for sunlight-computation library
 *
 * @author Wu
 * @create 2021-03-24 16:47
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
        scene.addMeshesAsBuildings(meshes, true);

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
        if (mouseButton == LEFT && alt)
            scene.capture3d(this);
    }

}
```