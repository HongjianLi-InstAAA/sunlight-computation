package guo;

import gzf.gui.CameraController;
import gzf.gui.Vec_Guo;
import org.locationtech.jts.geom.Geometry;
import processing.core.PApplet;

public class Example extends PApplet {

	public static void main(String[] args) {
		PApplet.main("guo.Example");
	}

	CameraController cam;
	SolarCalculator cal = new SolarCalculator(30);
	JTSRender render = new JTSRender(this);

	Vec_Guo[] shape = new Vec_Guo[] { new Vec_Guo(-5, -5), new Vec_Guo(5, -5), new Vec_Guo(5, 2), new Vec_Guo(0, 2), new Vec_Guo(0, 5), new Vec_Guo(-5, 5) };
	Vec_Guo[] shape2 = new Vec_Guo[] { new Vec_Guo(5, 5), new Vec_Guo(15, 5), new Vec_Guo(15, 12), new Vec_Guo(10, 12), new Vec_Guo(10, 15), new Vec_Guo(5, 15) };

	float shapeHeight = 10;

	public void settings() {
		size(800, 800, P3D);
	}
	public void setup() {
		cam = new CameraController(this, 10);
		cal.setDate(12, 22);
	}

	public void draw() {
		background(255);
		cam.drawSystem(10);
		cal.setDate((double) mouseX / width);
		/*
		 * draw solar position
		 */
		Vec_Guo pos = cal.getSolarPosition((double) mouseY / height);
		pos.mul(20);
		if (pos.z > 0)
			stroke(255, 0, 0);
		else
			stroke(0, 0, 0);
		strokeWeight(15);
		point((float) pos.x, (float) pos.y, (float) pos.z);

		/*
		 * draw shadow
		 */
		Geometry shadow = ShadowCalculator.getShadowOnGroundPrecise(pos, shape, shapeHeight, 0);
		Geometry shadow2 = ShadowCalculator.getShadowOnGroundPrecise(pos, shape2, shapeHeight, 0);
		if (shadow != null) {
			fill(0x30000000);
			noStroke();
			render.draw(shadow);
		}
		
		if (shadow2 != null) {
			fill(0x30000000);
			noStroke();
			render.draw(shadow2);
		}

		/*
		 * draw building
		 */
		fill(255);
		stroke(0);
		strokeWeight(1);
		drawShape(shape);

		drawShape(shape2);
	}

	private void drawShape(Vec_Guo[] shape) {
		int len = shape.length;
		for (int i = 0; i < len; i++) {
			int n = (i + 1) % len;
			beginShape();
			vertex((float) shape[i].x, (float) shape[i].y);
			vertex((float) shape[i].x, (float) shape[i].y, shapeHeight);
			vertex((float) shape[n].x, (float) shape[n].y, shapeHeight);
			vertex((float) shape[n].x, (float) shape[n].y);
			endShape(CLOSE);
		}

		beginShape();
		for (Vec_Guo vec_guo : shape) {
			vertex((float) vec_guo.x, (float) vec_guo.y, shapeHeight);
		}
		endShape(CLOSE);
	}
}
