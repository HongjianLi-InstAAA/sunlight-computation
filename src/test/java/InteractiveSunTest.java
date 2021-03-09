import core.Sun;
import gzf.gui.CameraController;
import processing.core.PApplet;
import utility.CtrlPanel;
import wblut.processing.WB_Render;

/**
 * interactive sun calculator test
 *
 * @author Wu
 * @create 2021-02-20 15:20
 */

public class InteractiveSunTest extends PApplet {
    public static void main(String[] args) {
        PApplet.main("InteractiveSunTest");
    }

    CameraController cam;
    WB_Render render;

    Sun sun;
    CtrlPanel panel;

    int[] location, date, time;

    public void settings() {
        size(1000, 800, P3D);
    }

    public void setup() {
//        System.out.println("outer setup-----------------------------");
        cam = new CameraController(this, Sun.groundRadius * 2);
        render = new WB_Render(this);

        panel = new CtrlPanel();
        sun = new Sun();
        sun.setPathDiv(50);

        location = sun.getLocation();
        date = sun.getDate();
        time = sun.getTime();
    }

    public void draw() {
//        System.out.println("outer draw-----------------------------");
        background(255);
        cam.drawSystem(Sun.groundRadius);

        sun.displayPath(render);
        sun.display(render);
        panel.updateInput(sun, location, date, time);
    }

}
