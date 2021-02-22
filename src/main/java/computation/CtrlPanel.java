package computation;

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
    private ControlP5 cp5;

    public CtrlPanel(WB_Vector loc) {
        super();
        PApplet.runSketch(new String[]{"Panel"}, this);
        surface.setLocation(Math.round(loc.xf()), Math.round(loc.yf()));
    }

    public void settings() {
        size(240, 400);
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
                .setValue(SunCalculator.Nanjing[0], SunCalculator.Nanjing[1])
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
                .setValue(SunCalculator.summerSolstice[0])
                .setDecimalPrecision(0)
                .setNumberOfTickMarks(12)
                .setSliderMode(controlP5.Slider.FLEXIBLE);
        monthCtrl.getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP_OUTSIDE).setPaddingX(0);

        Slider dayCtrl = cp5.addSlider("day")
                .setPosition(leftMargin, topMargin + 190)
                .setWidth(200)
                .setHeight(15)
                .setRange(1, 31)
                .setValue(SunCalculator.summerSolstice[1])
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
                .setValue(SunCalculator.highNoon[0])
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
                .setValue(SunCalculator.highNoon[1])
                .setDecimalPrecision(0)
                .setNumberOfTickMarks(59)
                .setTickMarkLength(4)
                .snapToTickMarks(true)
                .setColorActive(color(80, 170, 240))
                .setDragDirection(Knob.VERTICAL);

        popStyle();
    }

    public void draw() {
//            System.out.println("inner draw-----------------------------");
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