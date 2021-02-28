package test;

import core.Sun;
import gzf.gui.CameraController;
import processing.core.PApplet;
import wblut.processing.WB_Render;

/**
 * sun calculator test
 *
 * @author Wu
 * @create 2021-02-8 17:39
 */

public class SunTest extends PApplet {
    public static void main(String[] args) {
        PApplet.main("test.SunTest");
    }

    CameraController cam;
    WB_Render render;

    Sun sun;

    public void settings() {
        size(1000, 800, P3D);
    }

    public void setup() {
        cam = new CameraController(this, Sun.groundRadius * 2);
        render = new WB_Render(this);

        sun = new Sun(-29, 56);
        sun.setDate(12, 22);
        sun.setTime(12, 20);

        sun.printInfo();
    }

    public void draw() {
        background(255);
        cam.drawSystem(Sun.groundRadius);

        sun.displayPath(render);
        sun.display(render);
    }

    public void keyPressed(){
        if (key == 't' || key == 'T')
            cam.top();
        if (key == 'p' || key == 'P')
            cam.perspective();
    }
}
