package computation;

import gzf.gui.CameraController;
import processing.core.PApplet;
import wblut.processing.WB_Render;

/**
 * sun calculator test
 *
 * @author WU
 * @create 2021-02-18 17:39
 */

public class ZTestSun extends PApplet {
    public static void main(String[] args) {
        PApplet.main("computation.ZTestSun");
    }

    CameraController cam;
    SunCalculator sun;

//    public void settings() {
//        size(1000, 800, P3D);
//    }

    public void setup() {
//        cam = new CameraController(this, 10);
        sun = new SunCalculator(-29, -66.9);
        sun.setDate(6,15);
        sun.setTime(20,20);
        sun.printInfo();

    }

//    public void draw() {
//        background(255);
//        cam.drawSystem(10);
//    }
}
