package core;

import processing.core.PGraphics;
import utility.PolyWithNormal;
import utility.PolyHandler;
import wblut.geom.*;
import wblut.hemesh.HEC_Polygon;
import wblut.hemesh.HE_Face;
import wblut.hemesh.HE_Mesh;
import wblut.processing.WB_Render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * simple building for sunlight computation
 *
 * @author Wu
 * @create 2021-02-27 9:52
 */

public class Building {
    private final WB_Polygon base;
    private final double height;

    private List<PolyWithNormal> pns;
    private List<WB_Triangle> tris;
    private WB_AABB aabb;

    public Building(WB_Polygon base, double height) {
        this.base = base;
        this.height = height;
        HEC_Polygon creator = new HEC_Polygon(base, height);
        HE_Mesh mesh = new HE_Mesh(creator);
        init(mesh);

        pns = new ArrayList<>();
        for (HE_Face f : mesh.getFaces())
            pns.add(new PolyWithNormal(f.getPolygon()));
    }

    public Building(HE_Mesh mesh) {
        this.base = null;
        this.height = 0;
        init(mesh);

        pns = new ArrayList<>();
        for (WB_Triangle tri : tris)
            pns.add(new PolyWithNormal(tri));
    }

    private void init(HE_Mesh mesh) {
        this.tris = PolyHandler.mesh2tris(mesh);
        List<WB_Coord> pts = new ArrayList<>();
        for (WB_Triangle tri : tris) {
            pts.add(tri.getPoint(0));
            pts.add(tri.getPoint(1));
            pts.add(tri.getPoint(2));
        }

        aabb = new WB_AABB(pts);
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

    public Map<WB_Point, WB_Coord> getFacetCenters() {
        if (null == tris)
            return null;
        Map<WB_Point, WB_Coord> facetCenters = new HashMap<>();
        for (WB_Triangle tri :
                tris) {
            WB_Plane p = tri.getPlane();
            if (null != p) {
                facetCenters.put(tri.getCenter(), p.getNormal());
            }
        }
        return facetCenters;
    }

    public WB_AABB getAABB() {
        return aabb;
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
        for (PolyWithNormal p : pns)
            p.draw(render);
//        for (WB_Triangle t : tris)
//            render.drawTriangle(t);
        app.popStyle();
    }
}
