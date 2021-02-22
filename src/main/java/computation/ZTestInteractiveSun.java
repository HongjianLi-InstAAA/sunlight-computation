package computation;

import gzf.gui.CameraController;
import processing.core.PApplet;
import wblut.geom.WB_Vector;
import wblut.processing.WB_Render;

/**
 * interactive sun calculator test
 *
 * @author Wu
 * @create 2021-02-20 15:20
 */

public class ZTestInteractiveSun extends PApplet {
    public static void main(String[] args) {
        PApplet.main("computation.ZTestInteractiveSun");
    }

    CameraController cam;
    WB_Render render;

    SunCalculator sun;
    CtrlPanel panel;
    WB_Vector panelLoc = new WB_Vector(100, 100);

    int[] location, date, time;

    public void settings() {
        size(1000, 800, P3D);
    }

    public void setup() {
//        System.out.println("outer setup-----------------------------");
        cam = new CameraController(this, SunCalculator.groundRadius * 2);
        render = new WB_Render(this);

        panel = new CtrlPanel(panelLoc);
        sun = new SunCalculator();
        sun.setPathDiv(50);
        sun.calSunPath();

        location = sun.getLocation();
        date = sun.getDate();
        time = sun.getTime();
    }

    public void draw() {
//        System.out.println("outer draw-----------------------------");
        background(255);
        cam.drawSystem(SunCalculator.groundRadius);

        sun.displayPath(render);
        sun.display(render);
        updateInput();
    }

    public void init() {
        sun.calSunPath();
        sun.printInfo();
    }

    public void updateInput() {
        boolean isUpdate = false;

        int[] inputLocation = panel.getLonLat();
        if (null != inputLocation && isUpdate(location, inputLocation)) {
            location = inputLocation;
            sun.setLocalPosition(location[0], location[1]);
            isUpdate = true;
        }
        int[] inputDate = panel.getDate();
        if (null != inputDate && isUpdate(date, inputDate)) {
            date = inputDate;
            sun.setDate(date[0], date[1]);
            isUpdate = true;
        }
        int[] inputTime = panel.getTime();
        if (null != inputTime && isUpdate(time, inputTime)) {
            time = inputTime;
            sun.setTime(time[0], time[1]);
            isUpdate = true;
        }

        if (isUpdate) {
            init();
            System.out.println("/////////////////UPDATE//////////////");
        }
    }

    public boolean isUpdate(int[] a, int[] b) {
        if (null != a && null != b)
            for (int i = 0; i < a.length; i++) {
                if (a[i] != b[i])
                    return true;
            }
        return false;
    }


}
