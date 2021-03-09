package core;

import org.locationtech.jts.geom.Geometry;
import processing.core.PApplet;
import processing.core.PConstants;
import utility.JtsRender;
import utility.PolyHandler;
import wblut.geom.WB_GeometryOp;
import wblut.geom.WB_Point;
import wblut.geom.WB_Triangle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * grid analysis of site sunlight hours
 *
 * @author Wu
 * @create 2021-02-27 9:50
 */

public class DurationAnalysis {
    private static final int THREAD_COUNT = 25;

    private final Shadow.Type type;
    private final Sun sun;
    private final Building[] buildings;
    private Geometry[] allDayShadow;
    private List<ArrayList<WB_Triangle>> allDayShadowTris;

    private WB_Point sample;
    private double duration; // in HOURS
    private WB_Point[][] samples;
    private double[][] durations;
    private WB_Point leftBottom, rightTop;
    private float gridWidth, gridHeight;

    public DurationAnalysis(Sun sun, Building... buildings) {
        this(Shadow.Type.FACET, sun, buildings);
    }

    public DurationAnalysis(Shadow.Type type, Sun sun, Building... buildings) {
        this.type = type;
        this.sun = sun;
        this.buildings = buildings;
        update();
    }

    public void update() {
        allDayShadow = Shadow.calAllDayShadow(type, sun, buildings);
        if (null == allDayShadow)
            return;
        allDayShadowTris = new ArrayList<>();
        for (Geometry g : allDayShadow)
            if (null != g)
                allDayShadowTris.add(PolyHandler.toWB_Triangles(g));
    }

    public void pointAnalysis(WB_Point point) {
        if (null == point)
            return;
        sample = point;
        duration = calDuration(point);
    }

    public void gridAnalysis(WB_Point leftBottom, WB_Point rightTop,
                             int row, int col) {
        this.leftBottom = leftBottom;
        this.rightTop = rightTop;
        samples = new WB_Point[row][col];
        durations = new double[row][col];

        gridWidth = (float) ((rightTop.xd() - leftBottom.xd()) / col);
        gridHeight = (float) ((rightTop.yd() - leftBottom.yd()) / row);

        long startTime = System.currentTimeMillis();
        // multi-thread
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(THREAD_COUNT);
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                int finalI = i;
                int finalJ = j;
                fixedThreadPool.execute(() -> {
                    samples[finalI][finalJ] = new WB_Point(
                            leftBottom.xd() + (finalJ + 0.5) * gridWidth,
                            leftBottom.yd() + (finalI + 0.5) * gridHeight);
                    durations[finalI][finalJ] = calDuration(samples[finalI][finalJ]);
                });
            }
        }
        fixedThreadPool.shutdown();
        try {
            if (!fixedThreadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)) {
                fixedThreadPool.shutdownNow();
                if (!fixedThreadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS))
                    System.out.println("Thread pool did not terminate.");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        System.out.printf("TOTAL TIME: %.2fs%n", (endTime - startTime) / 1e3);
    }

    private double calDuration(WB_Point point) {
        if (null == allDayShadow)
            return 0;

        int counter = 0;
        for (ArrayList<WB_Triangle> l : allDayShadowTris) {
            if (!WB_GeometryOp.contains2D(point, l))
                counter++;
        }
        duration = sun.getSunlightDuration() * counter / allDayShadow.length;
        return duration;
    }

    public void displayAllDayShadow(JtsRender jtsRender) {
        if (null == allDayShadow)
            return;
        PApplet app = jtsRender.getApp();
        app.pushStyle();
        app.noStroke();
        app.fill(0x22000000);
        for (Geometry g : allDayShadow)
            jtsRender.draw(g);
        app.popStyle();
    }

    public void displaySample(PApplet app) {
        if (null == sample)
            return;
        app.pushStyle();
        app.stroke(0x88666666);
        app.strokeWeight(10);
        app.point(sample.xf(), sample.yf(), 2);
        displayDuration(app, sample, duration);
        app.popStyle();
    }

    private void displayDuration(PApplet app, WB_Point v, double d) {
        app.pushMatrix();
        app.translate(v.xf(), v.yf());
        app.scale(1, -1, 1);
        app.pushStyle();

        app.textMode(PConstants.SHAPE);
        app.textAlign(PConstants.CENTER, PConstants.CENTER);
        app.textSize(10);
        app.fill(0);
        app.text(String.format("%.2f", d), 0, 0, 1);

        app.popStyle();
        app.popMatrix();
    }

    public void displayGrid(PApplet app) {
        app.pushStyle();
        app.rectMode(PConstants.CORNERS);
        app.noFill();
        app.stroke(0xffaa0000);
        app.strokeWeight(3);
        app.rect(leftBottom.xf(), leftBottom.yf(), rightTop.xf(), rightTop.yf());
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
                    app.noFill();
                app.rect(samples[i][j].xf(), samples[i][j].yf(), gridWidth, gridHeight);

//                displayDuration(app, samples[i][j], durations[i][j]);
            }
        }
        app.popStyle();
    }
}
