package core;

import utility.PolyAnalysis;
import wblut.geom.WB_Polygon;
import wblut.hemesh.HEC_Polygon;
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

    public Building(WB_Polygon base, double height){
        this.base=base;
        this.height=height;
        HEC_Polygon creator = new HEC_Polygon(base, height);
        HE_Mesh mesh = new HE_Mesh(creator);
        pas = new ArrayList<>();
        for (HE_Face f : mesh.getFaces()) {
            pas.add(new PolyAnalysis(f.getPolygon()));
        }
    }

    public WB_Polygon getBase() {
        return base;
    }

    public double getHeight() {
        return height;
    }

    public void display(WB_Render render){
        for(PolyAnalysis p:pas)
            p.draw(render);
    }
}
