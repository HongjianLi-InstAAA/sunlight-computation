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

    private final List<PolyWithNormal> pns;
    private final List<WB_Triangle> tris;
    private List<WB_Point> triCenters;
    private Map<WB_Point, WB_Coord> facetCenters;

    public Building(WB_Polygon base, double height) {
        this.base = base;
        this.height = height;
        HEC_Polygon creator = new HEC_Polygon(base, height);
        HE_Mesh mesh = new HE_Mesh(creator);
        pns = new ArrayList<>();
        for (HE_Face f : mesh.getFaces()) {
            pns.add(new PolyWithNormal(f.getPolygon()));
        }

        tris = PolyHandler.mesh2tris(mesh);
    }

    public Building(List<WB_Triangle> tris) {
        this.base = null;
        this.height = 0;
        this.tris = tris;
        pns = new ArrayList<>();
        List<WB_Coord> pts = new ArrayList<>();
        for (WB_Triangle tri : tris) {
            pns.add(new PolyWithNormal(tri));
            pts.add(tri.getPoint(0));
            pts.add(tri.getPoint(1));
            pts.add(tri.getPoint(2));
        }

        WB_AABB aabb = new WB_AABB(pts);
    }

    public Building(HE_Mesh mesh){
        this(PolyHandler.mesh2tris(mesh));
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

    public List<WB_Point> getTriCenters() {
        if (null == tris)
            return null;
        triCenters = new ArrayList<>();
        for (WB_Triangle tri : tris) {
            WB_Plane p = tri.getPlane();
            if (null != p)
                triCenters.add(tri.getCenter().add(p.getNormal().mul(1)));
        }
        return triCenters;
    }

    public Map<WB_Point, WB_Coord> getFacetCenters() {
        if (null == tris)
            return null;
        facetCenters = new HashMap<>();
        for (WB_Triangle tri :
                tris) {
            WB_Plane p = tri.getPlane();
            if (null != p) {
                facetCenters.put(
                        tri.getCenter().add(p.getNormal().mul(1)),
                        p.getNormal()
                );
            }
        }
        return facetCenters;
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
