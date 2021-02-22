package computation;

import gzf.gui.CameraController;
import processing.core.PApplet;
import wblut.processing.WB_Render;

/**
 * sun calculator test
 *
 * @author Wu
 * @create 2021-02-8 17:39
 */

class ZTestSun extends PApplet {
    public static void main(String[] args) {
        PApplet.main("computation.ZTestSun");
    }

    CameraController cam;
    WB_Render render;

    SunCalculator sun;

    public void settings() {
        size(1000, 800, P3D);
    }

    public void setup() {
        cam = new CameraController(this, 200);
        render = new WB_Render(this);

        sun = new SunCalculator(-29, 56);
        sun.setDate(12, 22);
        sun.setTime(12, 20);
        sun.calSunPath();

        sun.printInfo();
    }

    public void draw() {
        background(255);
        cam.drawSystem(SunCalculator.groundRadius);

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
