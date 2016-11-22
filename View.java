import controlP5.ControlEvent;
import controlP5.ControlP5;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import java.util.HashMap;
import java.util.Map;

class View {

  private ControlP5 cp5;

  private PFont f1;
  private int NUM = 100;
  private float[] rotation = new float[NUM];
  private SliderList m = null;
  private PApplet applet = null;

  View(PApplet applet0) {  //  Construct the view.
    applet = applet0;
  }

  void settings() { //  Will be called only once.
    applet.size(800, 400, PConstants.P3D);
  }

  void setup() {
    //  size(800, 400 ,P3D);
    f1 = applet.createFont("Helvetica", 12);
    cp5 = new ControlP5(applet);
    // create a custom SilderList with name menu, notice that function
    // menu will be called when a menu item has been clicked.
    m = new SliderList(applet, cp5, "menu", 250, 350);
    m.setPosition(40, 20);
  }

  private int ctr = 0;

  void addItem(HashMap<String, Object> fields) {
    //  Add an item: m.addItem(makeItem("slider-"+i, 0, -PI, PI ));
    m.addItem(makeItem(String.valueOf(ctr) + ": tmp", (float) (32.0 + (ctr / 2.0)), 30, 40 ));
    ctr++;
  }

  // a convenience function to build a map that contains our key-value
  // pairs which we will then use to render each item of the SilderList.
  //
  private Map<String, Object> makeItem(String theLabel, float theValue, float theMin, float theMax) {
    HashMap<String, Object> m = new HashMap<>();
    m.put("label", theLabel);
    m.put("sliderValue", theValue);
    m.put("sliderValueMin", theMin);
    m.put("sliderValueMax", theMax);
    return m;
  }

  void menu(int i) {
    PApplet.println("got some slider-list event from item with index " + i);
  }

  public void controlEvent(ControlEvent theEvent) {
    if (theEvent.isFrom("menu")) {
      int index = (int) theEvent.getValue();
      Map m = ((SliderList) theEvent.getController()).getItem(index);
      PApplet.println("got a slider event from item : " + m);
      rotation[index] = f(m.get("sliderValue"));
    }
  }

  void draw() {
    applet.background(220);
    applet.fill(0, 128, 255);
    applet.noStroke();
    applet.pushMatrix();
    applet.translate(applet.width / 2, 30);
    for (int i = 0; i < NUM; i++) {
      applet.pushMatrix();
      applet.translate((i % 10) * 35, (i / 10) * 35);
      applet.rotate(rotation[i]);
      applet.rect(0, 0, 20, 20);
      applet.popMatrix();
    }
    applet.popMatrix();
  }

  private static float f(Object o) {
    return (o instanceof Number) ? ((Number) o).floatValue() : Float.MIN_VALUE;
  }

}

