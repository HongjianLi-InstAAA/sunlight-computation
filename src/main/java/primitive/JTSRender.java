package primitive;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import processing.core.PApplet;
import processing.core.PConstants;

/**
 * Processing render to draw JTS geometries.
 * 
 * author: LI Biao (15/05/2015)
 * 
 * 
 * */
public class JTSRender {
	PApplet app;

	public JTSRender(PApplet app) {
		this.app = app;
	}

	public void draw(Geometry g) {
		int num = g.getNumGeometries();
		for (int i = 0; i < num; i++) {
			Geometry gg = g.getGeometryN(i);
			int dim = g.getDimension();
			if (dim == 0) {
				drawDim0(gg);
			} else if (dim == 1) {
				drawDim1(gg);
			} else if (dim == 2) {
				drawDim2(gg);
			}
		}
	}

	private void drawDim2(Geometry gg) {
		Coordinate[] coords = gg.getCoordinates();
		if (coords == null) {
			return;
		}
		Polygon pp = (Polygon) gg;

		// �������,�������������
		app.beginShape();
		for (Coordinate c : coords) {
			app.vertex((float) c.x, (float) c.y, (float) (Double.isNaN(c.z) ? 0 : c.z));
		}
		app.endShape(PConstants.CLOSE);
		// ������߲����
		Coordinate[] outers = pp.getExteriorRing().getCoordinates();
		app.beginShape();
		for (Coordinate c : outers) {
			app.vertex((float) c.x, (float) c.y, (float) (Double.isNaN(c.z) ? 0 : c.z));
		}
		app.endShape(PConstants.CLOSE);
		// ���ƶ�����ڵ�"��"
		int intNum = pp.getNumInteriorRing();
		for (int i = 0; i < intNum; i++) {
			LineString ls = pp.getInteriorRingN(i);
			Coordinate[] inner = ls.getCoordinates();
			app.beginShape();
			for (int k = 0; k < inner.length; k++) {
				for (Coordinate in : inner) {
					app.vertex((float) in.x, (float) in.y, (float) (Double.isNaN(in.z) ? 0 : in.z));
				}
			}
			app.endShape(PConstants.CLOSE);
		}

	}

	private void drawDim1(Geometry gg) {
		Coordinate[] coords = gg.getCoordinates();
		if (coords == null) {
			return;
		}

		if (coords.length == 2) {// �߶�
			app.line((float) coords[0].x, (float) coords[0].y, (float) (Double.isNaN(coords[0].z) ? 0 : coords[0].z), (float) coords[1].x, (float) coords[1].y,
					(float) (Double.isNaN(coords[1].z) ? 0 : coords[1].z));
		} else {// ������
			app.beginShape();
			for (Coordinate c : coords) {
				app.vertex((float) c.x, (float) c.y, (float) (Double.isNaN(c.z) ? 0 : c.z));
			}
			app.endShape();
		}

	}

	private void drawDim0(Geometry gg) {
		Coordinate[] coords = gg.getCoordinates();
		if (coords == null) {
			return;
		}

		app.pushMatrix();
		app.translate((float) coords[0].x, (float) coords[0].y, (float) (Double.isNaN(coords[0].z) ? 0 : coords[0].z));
		app.box(4);
		app.popMatrix();
	}
}
