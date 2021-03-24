package core;

import gzf.gui.CameraController;
import gzf.gui.Vec_Guo;
import org.locationtech.jts.geom.Geometry;
import processing.core.PApplet;
import utility.CtrlPanel;
import utility.JtsRender;
import utility.PolyHandler;
import wblut.geom.*;
import wblut.processing.WB_Render;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Wu
 * @create 2021-03-23 16:48
 */

public class Scene {
    private final CameraController cam;
    private final Sun sun;
    private final CtrlPanel panel;

    private final int[] location;
    private final int[] date;
    private final int[] time;

    private final List<Building> buildings;
    private Shadow.Type type = Shadow.Type.FACET;
    private Geometry shadow;
    private SamplingPoint sample;
    private List<SamplingPoint> samples;
    private DurationAnalysis analysis;
    private int gridSubdiv = 100;

    private boolean ifShowShadow = false;
    private boolean ifShowAllDayShadow = false;
    private boolean ifShowGrid = true;

    public Scene(CameraController cam, Sun sun, CtrlPanel panel) {
        this.cam = cam;
        this.sun = sun;
        this.panel = panel;

        location = sun.getLocation();
        date = sun.getDate();
        time = sun.getTime();
        buildings = new ArrayList<>();

        sample = new SamplingPoint(PolyHandler.ORIGIN, WB_Vector.Z());
    }

    public void setAnalysis(DurationAnalysis analysis) {
        this.analysis = analysis;
    }

    public void setSamples(List<SamplingPoint> samples) {
        this.samples = samples;
        analysis.pointsAnalysis(samples);
    }

    public void setShadowType(Shadow.Type t) {
        this.type = t;
    }

    public void setGridSubdiv(int gridSubdiv) {
        this.gridSubdiv = gridSubdiv;
    }

    public void setIfShowShadow(boolean ifShowShadow) {
        this.ifShowShadow = ifShowShadow;
    }

    public void setIfShowAllDayShadow(boolean ifShowAllDayShadow) {
        this.ifShowAllDayShadow = ifShowAllDayShadow;
    }

    public void setIfShowGrid(boolean ifShowGrid) {
        this.ifShowGrid = ifShowGrid;
    }

    public void reverseShowShadow() {
        ifShowShadow = !ifShowShadow;
    }

    public void reverseShowAllDayShadow() {
        ifShowAllDayShadow = !ifShowAllDayShadow;
    }

    public void reverseShowGrid() {
        ifShowGrid = !ifShowGrid;
    }

    public void addBuilding(Building b) {
        buildings.add(b);
    }

    public Sun getSun() {
        return sun;
    }

    public Shadow.Type getType() {
        return type;
    }

    public List<Building> getBuildings() {
        return buildings;
    }

    public SamplingPoint getSample() {
        return sample;
    }

    public List<SamplingPoint> getSamples() {
        return samples;
    }

    public boolean refresh() {
        CtrlPanel.updateState state = panel.updateInput(location, date, time);
        if (CtrlPanel.updateState.NONE != state) {
            updateCurrentShadow();
            if (state == CtrlPanel.updateState.UPDATE_PATH) {
                updatePath();
                return true;
            }
        }
        return false;
    }

    public void init() {
        updateCurrentShadow();
        updatePath();
    }

    public void displaySun(WB_Render render) {
        sun.displayPath(render);
        sun.display(render);
    }

    public void displayBuildings(WB_Render render) {
        for (Building building : getBuildings())
            building.display(sun, render);
    }

    public void displayAnalysis(JtsRender jtsRender, PApplet app) {
        if (ifShowShadow)
            Shadow.displayShadow(shadow, jtsRender);
        if (ifShowAllDayShadow)
            analysis.displayAllDayShadow(jtsRender);

        analysis.displaySample(app);
        if (ifShowGrid)
            analysis.displayGrid(app);
    }

    private void updateCurrentShadow() {
        shadow = Shadow.calCurrentShadow(this);
    }

    private void updatePath() {
        analysis.updateAllDayShadow();
        analysis.pointAnalysis(sample);
        if (null != samples)
            analysis.pointsAnalysis(samples);
        if (ifShowGrid)
            analysis.gridAnalysis(
                    new WB_Point(-Sun.groundRadius, -Sun.groundRadius),
                    new WB_Point(Sun.groundRadius, Sun.groundRadius),
                    gridSubdiv, gridSubdiv
            );
    }

    public void capture3d(PApplet app) {
        Vec_Guo[] pick3d = cam.pick3d(app.mouseX, app.mouseY);
        WB_Coord origin = PolyHandler.vec2coord(pick3d[0]);
        WB_Coord dir = PolyHandler.vec2coord(pick3d[1]);
        WB_Ray ray = PolyHandler.gf.createRayThroughPoints(origin, WB_Vector.add(origin, dir));

        List<Building> captureBuildings = new ArrayList<>();
        for (Building b : buildings) {
            if (WB_GeometryOp.checkIntersection3D(ray, b.getAABB()))
                captureBuildings.add(b);
        }

        double t = Double.MAX_VALUE;
        SamplingPoint sp = null;
        for (Building b : captureBuildings) {
            for (WB_Triangle tri : b.getTris()) {
                double tempT = PolyHandler.rayTriIntersectionT(ray, tri);
                if (tempT < t) {
                    t = tempT;
                    WB_Point capture = PolyHandler.pointInRay(ray, t);
                    sp = new SamplingPoint(capture, tri.getPlane().getNormal());
                }
            }
        }

        if (null == sp) {
            Vec_Guo pick = cam.pick3dXYPlane(app.mouseX, app.mouseY);
            sp = new SamplingPoint(new WB_Point(pick.x(), pick.y()));
        }
        sample = sp;
        analysis.pointAnalysis(sample);
    }
}
