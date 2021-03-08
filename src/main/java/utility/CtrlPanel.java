package utility;

import core.Sun;
import controlP5.*;
import processing.core.PApplet;
import wblut.geom.WB_Vector;


/**
 * control panel as the second PApplet window
 * input location, date and local time for sun calculator
 *
 * @author Wu
 * @create 2021-02-22 8:23
 */

public class CtrlPanel extends PApplet {

    private static final int bgColor = 120;
    private static final int lineSpacing = 13;

    private static boolean isUpdate(int[] a, int[] b) {
        if (null != a && null != b) {
            for (int i = 0; i < a.length; i++)
                if (a[i] != b[i])
                    return true;
        }
        return false;
    }

    private static void updateArray(int[] origin, int[] target) {
        if (null != origin && null != target) {
            System.arraycopy(target, 0, origin, 0, origin.length);
        }
    }

    private ControlP5 cp5;

    public CtrlPanel(WB_Vector loc) {
        super();
        PApplet.runSketch(new String[]{"Panel"}, this);
        surface.setLocation(Math.round(loc.xf()), Math.round(loc.yf()));
    }

    public void settings() {
        size(240, 460);
    }

    public void setup() {
//            System.out.println("inner setup-----------------------------");
        cp5 = new ControlP5(this);

        int topMargin = 30;
        int leftMargin = 20;

        pushStyle();
        noStroke();

        Slider2D lonLatCtrl = cp5.addSlider2D("lonLat")
                .setPosition(leftMargin, topMargin)
                .setSize(200, 120)
                .setMinMax(-180, 90, 180, -90)
                .setValue(Sun.Nanjing[0], Sun.Nanjing[1])
                .setCaptionLabel("Longitude, Latitude")
//                    .disableCrosshair()
                ;
        lonLatCtrl.getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);
        lonLatCtrl.getValueLabel().setSize(10);

        Slider monthCtrl = cp5.addSlider("month")
                .setPosition(leftMargin, topMargin + 150)
                .setWidth(200)
                .setHeight(15)
                .setRange(1, 12)
                .setValue(Sun.summerSolstice[0])
                .setDecimalPrecision(0)
                .setNumberOfTickMarks(12)
                .setSliderMode(controlP5.Slider.FLEXIBLE);
        monthCtrl.getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);

        Slider dayCtrl = cp5.addSlider("day")
                .setPosition(leftMargin, topMargin + 190)
                .setWidth(200)
                .setHeight(15)
                .setRange(1, 31)
                .setValue(Sun.summerSolstice[1] + 1)
                .setDecimalPrecision(0)
                .setNumberOfTickMarks(31)
                .setSliderMode(controlP5.Slider.FLEXIBLE);
        dayCtrl.getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);

        cp5.addTextlabel("time")
                .setText("LOCAL TIME")
                .setPosition(leftMargin/*+65*/, topMargin + 230)
                .setColorValue(0xffffffff)
        ;

        cp5.addKnob("hour")
                .setPosition(leftMargin, topMargin + 250)
                .setRadius(45)
                .setRange(0, 23)
                .setValue(Sun.noon[0])
                .setDecimalPrecision(0)
                .setNumberOfTickMarks(23)
                .setTickMarkLength(4)
                .snapToTickMarks(true)
                .setColorActive(color(80, 170, 240))
                .setDragDirection(Knob.VERTICAL);

        cp5.addKnob("minute")
                .setPosition(leftMargin + 105, topMargin + 250)
                .setRadius(45)
                .setRange(0, 59)
                .setValue(Sun.noon[1])
                .setDecimalPrecision(0)
                .setNumberOfTickMarks(59)
                .setTickMarkLength(4)
                .snapToTickMarks(true)
                .setColorActive(color(80, 170, 240))
                .setDragDirection(Knob.VERTICAL);

        cp5.addTextlabel("instruction0")
                .setText("select a point: ALT + left-click")
                .setPosition(leftMargin, topMargin + 370)
                .setColorValue(0xffffffff)
        ;
        cp5.addTextlabel("instruction1")
                .setText("shadow : S")
                .setPosition(leftMargin, topMargin + 370 + lineSpacing)
                .setColorValue(0xffffffff)
        ;
        cp5.addTextlabel("instruction2")
                .setText("all day shadow : A")
                .setPosition(leftMargin, topMargin + 370 + lineSpacing * 2)
                .setColorValue(0xffffffff)
        ;
        cp5.addTextlabel("instruction3")
                .setText("grid analysis : G")
                .setPosition(leftMargin, topMargin + 370 + lineSpacing * 3)
                .setColorValue(0xffffffff)
        ;

        popStyle();
    }

    public void draw() {
//            System.out.println("inner draw-----------------------------");
        background(bgColor);
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

    public enum updateState {
        UPDATE_PATH, UPDATE_TIME, NONE
    }

    public updateState updateInput(Sun sun, int[] location, int[] date, int[] time) {
        updateState state = updateState.NONE;
        boolean isUpdate = false;

        int[] inputLocation = getLonLat();
        if (null != inputLocation && isUpdate(location, inputLocation)) {
            updateArray(location, inputLocation);
            sun.setLocalPosition(location[0], location[1]);
            sun.calSunPath();
            isUpdate = true;
            System.out.println("///////////// location update /////////////");
            state = updateState.UPDATE_PATH;
        }
        int[] inputDate = getDate();
        if (null != inputDate && isUpdate(date, inputDate)) {
            updateArray(date, inputDate);
            sun.setDate(date[0], date[1]);
            sun.calSunPath();
            isUpdate = true;
            System.out.println("///////////// date update /////////////");
            state = updateState.UPDATE_PATH;
        }


        int[] inputTime = getTime();
        if (null != inputTime && isUpdate(time, inputTime)) {
            updateArray(time, inputTime);
            sun.setTime(time[0], time[1]);
            isUpdate = true;
            System.out.println("///////////// time update /////////////");
            state = updateState.UPDATE_TIME;
        }

        if (isUpdate) {
            sun.printInfo();
//            System.out.println(state);
        }
        return state;
    }

}
