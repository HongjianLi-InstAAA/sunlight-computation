package core;

import org.locationtech.jts.geom.Geometry;
import processing.core.PApplet;
import processing.core.PConstants;
import utility.JtsRender;
import utility.PolyHandler;
import wblut.geom.WB_GeometryOp;
import wblut.geom.WB_Vector;

/**
 * grid analysis of site sunlight hours
 *
 * @author Wu
 * @create 2021-02-27 9:50
 */

public class DurationAnalysis {
    private final Sun sun;
    private final Building[] buildings;
    private Geometry[] allDayShadow;

    private WB_Vector sample;
    private double duration; // in HOURS
    private WB_Vector[][] samples;
    private double[][] durations;
    private float gridWidth, gridHeight;

    public DurationAnalysis(Sun sun, Building... buildings) {
        this.sun = sun;
        this.buildings = buildings;
        update();
    }

    public void update() {
        allDayShadow = Shadow.calAllDayShadow(sun, buildings);
    }

    public void pointAnalysis(WB_Vector point) {
        sample = point;
        duration = calDuration(point);
    }

    public void gridAnalysis(WB_Vector leftBottom, WB_Vector rightTop,
                             int row, int col) {
        samples = new WB_Vector[row][col];
        durations = new double[row][col];

        gridWidth = (float) ((rightTop.xd() - leftBottom.xd()) / col);
        gridHeight = (float) ((rightTop.yd() - leftBottom.yd()) / row);
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                samples[i][j] = new WB_Vector(
                        leftBottom.xd() + (j + 0.5) * gridWidth,
                        leftBottom.yd() + (i + 0.5) * gridHeight);
                durations[i][j] = calDuration(samples[i][j]);
            }
        }
    }

    private double calDuration(WB_Vector point) {
        if (null == allDayShadow)
            return 24;
        int counter = 0;
        for (Geometry g : allDayShadow) {
            if (null != g && WB_GeometryOp.contains2D(point,
                    PolyHandler.toWB_Triangles(g)));
            else
                counter++;
        }
        duration = sun.getSunlightDuration() * counter / allDayShadow.length;
        return duration;
    }

    public void displayAllDayShadow(JtsRender jtsRender) {
        for (Geometry g : allDayShadow)
            jtsRender.draw(g);
    }

    public void displaySample(PApplet app) {
        app.point(sample.xf(), sample.yf());
        displayDuration(app, sample, duration);

    }

    public void displayGrid(PApplet app) {
        app.pushStyle();
        app.rectMode(PConstants.CENTER);
        app.noStroke();
        for (int i = 0; i < samples.length; i++) {
            for (int j = 0; j < samples[0].length; j++) {
                if (durations[i][j] == 0) {
                    app.fill(0xff000000);
                    app.rect(samples[i][j].xf(), samples[i][j].yf(), gridWidth, gridHeight);
                    continue;
                } else if (durations[i][j] < 2)
                    app.fill(0xffff0000);
                else if (durations[i][j] < 4)
                    app.fill(0xffffff00);
                else if (durations[i][j] < 6)
                    app.fill(0xff00ff00);
                else if (durations[i][j] < 8)
                    app.fill(0xff00ffff);
                else if (durations[i][j] < 10)
                    app.fill(0xff0000ff);
                else if (durations[i][j] < 12)
                    app.fill(0xffff00ff);
                else
                    app.fill(0xffffffff);
                app.rect(samples[i][j].xf(), samples[i][j].yf(), gridWidth, gridHeight);

                displayDuration(app, samples[i][j], durations[i][j]);
            }
        }
        app.popStyle();
    }

    private void displayDuration(PApplet app, WB_Vector v, double d) {
        app.pushMatrix();
        app.translate(v.xf(), v.yf());
        app.scale(1, -1, 1);
        app.pushStyle();

        app.textMode(PConstants.SHAPE);
        app.textAlign(PConstants.CENTER, PConstants.CENTER);
        app.textSize(2);
        app.fill(0);
        app.text(String.format("%.2f", d), 0, 0, 1);

        app.popStyle();
        app.popMatrix();
    }
}
