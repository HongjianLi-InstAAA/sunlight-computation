import igeo.*;
import processing.core.PApplet;

/**
 * @author Wu
 * @create 2021-03-16 16:32
 */

public class IGeoTest extends PApplet {
    public static void main(String[] args) {
        PApplet.main("IGeoTest");
    }

    public void settings() {
        size(1000, 800, IG.GL);
    }

    public void setup() {
        IG.init();
    }

    public void draw() {

    }

}
