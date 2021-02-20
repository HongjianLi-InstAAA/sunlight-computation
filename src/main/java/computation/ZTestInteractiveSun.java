package computation;

import controlP5.ControlP5;
import controlP5.Controller;
import controlP5.Knob;
import gzf.gui.CameraController;
import processing.core.PApplet;
import wblut.geom.WB_Vector;
import wblut.processing.WB_Render;

import java.util.ArrayList;
import java.util.List;

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
        cam = new CameraController(this, 10);
        render = new WB_Render(this);

        panel = new CtrlPanel(panelLoc);
        sun = new SunCalculator();
        init();

        location = sun.getLocation();
        date = sun.getDate();
        time = sun.getTime();
    }

    public void draw() {
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

    /**
     * inner class for control panel
     */
    private class CtrlPanel extends PApplet {
        ControlP5 cp5;
        List<Controller> controllers;

        public CtrlPanel(WB_Vector loc) {
            super();
            PApplet.runSketch(new String[]{"Panel"}, this);
            surface.setLocation(Math.round(loc.xf()), Math.round(loc.yf()));
        }

        public void settings() {
            size(240, 400);
        }

        public void setup() {
            pushStyle();
            noStroke();
            cp5 = new ControlP5(this);
            controllers = new ArrayList<>();

            controllers.add(cp5.addSlider2D("lonLat")
                            .setPosition(20, 20)
                            .setSize(200, 120)
                            .setMinMax(-180, 90, 180, -90)
                            .setValue(0, 50)
                            .setCaptionLabel("Longitude, Latitude")
//                    .disableCrosshair()
            );
            cp5.getController("lonLat").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);
            cp5.getController("lonLat").getValueLabel().setSize(10);

            controllers.add(cp5.addSlider("month")
                    .setPosition(20, 170)
                    .setWidth(200)
                    .setHeight(15)
                    .setRange(1, 12)
                    .setValue(6)
                    .setNumberOfTickMarks(12)
                    .setSliderMode(controlP5.Slider.FLEXIBLE)
            );
            cp5.getController("month").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);
            controllers.add(cp5.addSlider("day")
                    .setPosition(20, 210)
                    .setWidth(200)
                    .setHeight(15)
                    .setRange(1, 31)
                    .setValue(23)
                    .setNumberOfTickMarks(31)
                    .setSliderMode(controlP5.Slider.FLEXIBLE)
            );
            cp5.getController("day").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);

            controllers.add(cp5.addKnob("hour")
                    .setRange(0, 23)
                    .setValue(12)
                    .setPosition(20, 250)
                    .setRadius(45)
                    .setNumberOfTickMarks(23)
                    .setTickMarkLength(4)
                    .snapToTickMarks(true)
                    .setColorActive(color(80, 170, 240))
                    .setDragDirection(Knob.VERTICAL)
            );

            controllers.add(cp5.addKnob("minute")
                    .setRange(0, 59)
                    .setValue(30)
                    .setPosition(125, 250)
                    .setRadius(45)
                    .setNumberOfTickMarks(59)
                    .setTickMarkLength(4)
                    .snapToTickMarks(true)
                    .setColorActive(color(80, 170, 240))
                    .setDragDirection(Knob.VERTICAL)
            );

            popStyle();
        }

        public void draw() {
            background(120);
        }

        public int[] getLonLat() {
            if (null != cp5) {
                int lon = Math.round(cp5.getController("lonLat").getArrayValue()[0]);
                int lat = Math.round(cp5.getController("lonLat").getArrayValue()[1]);
                return new int[]{lon, lat};
            } else
                return null;
        }

        public int[] getDate() {
            if (null != cp5) {
                int month = Math.round(cp5.getController("month").getValue());
                int day = Math.round(cp5.getController("day").getValue());
                return new int[]{month, day};
            } else
                return null;
        }

        public int[] getTime() {
            if (null != cp5) {
                int hour = Math.round(cp5.getController("hour").getValue());
                int minute = Math.round(cp5.getController("minute").getValue());
                return new int[]{hour, minute};
            } else
                return null;
        }
    }
}
