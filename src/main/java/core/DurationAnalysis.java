package core;

import org.locationtech.jts.geom.Geometry;
import processing.core.PApplet;
import processing.core.PConstants;
import utility.JtsRender;
import utility.PolyHandler;
import wblut.geom.*;

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

    private SamplingPoint sample;
    private List<SamplingPoint> samples;
    private SamplingPoint[][] grids;
    private WB_Point leftBottom, rightTop;
    private float gridWidth, gridHeight;

    public DurationAnalysis(Scene scene) {
        this.type = scene.getType();
        this.sun = scene.getSun();
        buildings = scene.getBuildings().toArray(new Building[0]);
        scene.setAnalysis(this);

        updateAllDayShadow();
        scene.init();
    }

    public DurationAnalysis(Sun sun, Building... buildings) {
        this(Shadow.Type.FACET, sun, buildings);
    }

    public DurationAnalysis(Shadow.Type type, Sun sun, Building... buildings) {
        this.type = type;
        this.sun = sun;
        this.buildings = buildings;
        updateAllDayShadow();
    }

    public void updateAllDayShadow() {
        allDayShadow = Shadow.calAllDayShadow(type, sun, buildings);
        if (null == allDayShadow)
            return;
        allDayShadowTris = new ArrayList<>();
        for (Geometry g : allDayShadow)
            if (null != g)
                allDayShadowTris.add(PolyHandler.toWB_Triangles(g));
    }

//    public SamplingPoint pointAnalysis(WB_Point point) {
//        if (null == point)
//            return null;
//        sample = new SamplingPoint(point);
//        calDuration3D(sample);
//        System.out.printf("sample point duration = %.2f hours\n",
//                sample.getDuration());
//        return sample;
//    }

    public void pointAnalysis(SamplingPoint sp) {
        sample = sp;
        if (null == sp)
            return;
        calDuration3D(sample);
        System.out.printf("sample point duration = %.2f hours\n",
                sample.getDuration());
    }

    public void pointsAnalysis(List<SamplingPoint> sps) {
        samples = sps;
        if (null == sps)
            return;
        for (SamplingPoint sp : sps)
            calDuration3D(sp);
    }

    public void gridAnalysis(WB_Point leftBottom, WB_Point rightTop,
                             int row, int col) {
        this.leftBottom = leftBottom;
        this.rightTop = rightTop;
        grids = new SamplingPoint[row][col];

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
                    grids[finalI][finalJ] = new SamplingPoint(new WB_Point(
                            leftBottom.xd() + (finalJ + 0.5) * gridWidth,
                            leftBottom.yd() + (finalI + 0.5) * gridHeight));
                    calDuration(grids[finalI][finalJ]);
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

    private void calDuration(SamplingPoint sp) {
        if (null == allDayShadow)
            return;

        int counter = 0;
        for (ArrayList<WB_Triangle> l : allDayShadowTris) {
            if (!WB_GeometryOp.contains2D(sp.getPointAbovePlane(), l))
                counter++;
        }

        sp.setDuration(sun.getSunlightDuration() * counter / allDayShadow.length);
    }

    private void calDuration3D(SamplingPoint sp) {
        WB_PolyLine path = sun.getPath();
        if (null == path)
            return;
        WB_Point[] sunPositions = new WB_Point[sun.getPathDiv() - 2];
        int counter = sunPositions.length;
        WB_Point samplingPoint = sp.getPointAbovePlane();
        for (int i = 0; i < sunPositions.length; i++) {
            sunPositions[i] = path.getPoint(i + 1);
            WB_Ray ray = PolyHandler.gf.createRayThroughPoints(
                    samplingPoint, samplingPoint.add(sunPositions[i]));
            shield:
            for (Building b : buildings) {
                List<WB_Triangle> tris = b.getTris();
                for (WB_Triangle tri : tris) {
                    if (PolyHandler.checkRayTriIntersection(ray, tri)) {
                        counter--;
                        break shield;
                    }
                }
            }
        }
        sp.setDuration(sun.getSunlightDuration() * counter / sunPositions.length);
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
        displaySamplingPoint(app, sample);

        if (null == samples)
            return;
        displaySamplingPoint(app, samples.toArray(new SamplingPoint[0]));
    }

    private void displaySamplingPoint(PApplet app, SamplingPoint... sps) {
        if (null == sps)
            return;
        app.pushStyle();

        for (SamplingPoint sp :
                sps) {
            if (sp.getDuration() == 0)
                app.stroke(0xbb000000);
            else if (sp.getDuration() < 2)
                app.stroke(0xbbff0000);
            else if (sp.getDuration() < 4)
                app.stroke(0xbbffff00);
            else if (sp.getDuration() < 6)
                app.stroke(0xbb00ff00);
            else if (sp.getDuration() < 8)
                app.stroke(0xbb00ffff);
            else if (sp.getDuration() < 10)
                app.stroke(0xbb0000ff);
            else if (sp.getDuration() < 12)
                app.stroke(0xbbff00ff);
            else
                app.stroke(0xbb999999);

            app.strokeWeight(10);
            app.point(sp.getPointAbovePlane().xf(), sp.getPointAbovePlane().yf(), sp.getPointAbovePlane().zf());
            displayDuration(app, sp);
        }

        app.popStyle();
    }

    private void displayDuration(PApplet app, SamplingPoint sp) {
        app.pushMatrix();
        app.translate(sp.getPointAbovePlane().xf(), sp.getPointAbovePlane().yf(), sp.getPointAbovePlane().zf());
        app.rotate(-sp.getAngle(), sp.getAxis().xf(), sp.getAxis().yf(), sp.getAxis().zf());
        app.scale(1, -1, 1);

        app.pushStyle();

        app.textMode(PConstants.SHAPE);
        app.textAlign(PConstants.CENTER, PConstants.CENTER);
        app.textSize(10);
        app.fill(0);
        app.text(String.format("%.2f", sp.getDuration()), 0, 0, 0);

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
        for (SamplingPoint[] pointAnalyses : grids) {
            for (SamplingPoint grid : pointAnalyses) {
                double d = grid.getDuration();
                if (d == 0)
                    app.fill(0x88000000);
                else if (d < 2)
                    app.fill(0x88ff0000);
                else if (d < 4)
                    app.fill(0x88ffff00);
                else if (d < 6)
                    app.fill(0x8800ff00);
                else if (d < 8)
                    app.fill(0x8800ffff);
                else if (d < 10)
                    app.fill(0x880000ff);
                else if (d < 12)
                    app.fill(0x88ff00ff);
                else
                    app.noFill();
                app.rect(grid.getPoint().xf(), grid.getPoint().yf(), gridWidth, gridHeight);

//                displayDuration(app, samples[i][j], durations[i][j]);
            }
        }
        app.popStyle();
    }
}
