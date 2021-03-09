package core;

import processing.core.PGraphics;
import utility.PolyAnalysis;
import utility.PolyHandler;
import wblut.geom.WB_Polygon;
import wblut.geom.WB_Triangle;
import wblut.hemesh.HEC_Polygon;
import wblut.hemesh.HET_Import;
import wblut.hemesh.HE_Face;
import wblut.hemesh.HE_Mesh;
import wblut.processing.WB_Render;

import java.util.ArrayList;
import java.util.List;

/**
 * simple building for sunlight computation
 *
 * @author Wu
 * @create 2021-02-27 9:52
 */

public class Building {
    private final WB_Polygon base;
    private final double height;

    private final List<PolyAnalysis> pas;
    private final List<WB_Triangle> tris;

    public Building(WB_Polygon base, double height) {
        this.base = base;
        this.height = height;
        HEC_Polygon creator = new HEC_Polygon(base, height);
        HE_Mesh mesh = new HE_Mesh(creator);
        pas = new ArrayList<>();
        for (HE_Face f : mesh.getFaces()) {
            pas.add(new PolyAnalysis(f.getPolygon()));
        }

        tris = PolyHandler.mesh2tris(mesh);
    }

    public Building(List<WB_Triangle> tris) {
        this.base = null;
        this.height = 0;
        this.tris = tris;
        pas = new ArrayList<>();
        for (WB_Triangle tri : tris)
            pas.add(new PolyAnalysis(tri));
    }

    public WB_Polygon getBase() {
        return base;
    }

    public double getHeight() {
        return height;
    }

    public List<WB_Triangle> getTris() {
        return tris;
    }

    public void display(Sun sun, WB_Render render) {
        PGraphics app = render.getHome();
        app.pushStyle();
        if (sun.getPosition().zd() <= 0)
            app.fill(150);
        else
            app.fill(255);
        app.stroke(0);
        app.strokeWeight(1);
        for (PolyAnalysis p : pas)
            p.draw(render);
//        for (WB_Triangle t : tris)
//            render.drawTriangle(t);
        app.popStyle();
    }
}
