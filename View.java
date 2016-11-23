import controlP5.ControlEvent;
import controlP5.ControlP5;
import processing.core.PApplet;
import processing.core.PConstants;

import java.util.HashMap;
import java.util.Map;

class View {

  private ControlP5 cp5;
  static final int rows = 10, cols = 9, width = 40, height = 20, dataCols = 3;  //  Number of data columns.
  static final int sliderWidth = 250, sliderHeight = 350, sliderLeft = 40, sliderTop = 20;
  static final int[][] columnColors = {
      {0, 128, 255},
      {255, 0, 128},
      {128, 200, 0}
  };
  private SliderList sliderList = null;
  private PApplet applet = null;

  View(PApplet applet0) {  //  Construct the view.
    applet = applet0;
  }

  void settings() { //  Will be called only once.
    applet.size(800, 400, PConstants.P3D);
  }

  void setup() {
    cp5 = new ControlP5(applet);
    // create a custom SilderList with name menu, notice that function
    // menu will be called when a menu item has been clicked.
    sliderList = new SliderList(applet, cp5, "sliderList", sliderWidth, sliderHeight);
    sliderList.setPosition(sliderLeft, sliderTop);
  }

  private int row = 0;

  void addRow(HashMap<String, Object> fields) {
    //  Add an item: sliderList.addRow(makeItem("slider-"+i, 0, -PI, PI ));
    String prefix = String.valueOf(row + 1) + ": ";
    int col = 0;
    for (String key: fields.keySet()) {
      Object val = fields.get(key);
      Map<String, Object> item;
      if (val instanceof Float) {
        item = makeItem(prefix + key, val);
      } else {
        item = makeItem(prefix + key, val);
      }
      sliderList.addItem(row, col, item);
      col++;
      prefix = "";
    }
    row++;
  }

  // a convenience function to build a map that contains our key-value
  // pairs which we will then use to render each item of the SilderList.
  //
  private Map<String, Object> makeItem(String theLabel, Object theValue) {
    HashMap<String, Object> m = new HashMap<>();
    m.put("label", theLabel);
    m.put("sliderValue", theValue);
    return m;
  }

  void draw() {
    applet.background(220);
    applet.noStroke();
    applet.pushMatrix();
    applet.translate(sliderWidth + sliderLeft + 20, sliderTop);
    for (int i = 0; i < rows * cols; i++) {
      float val = sliderList.values[i];
      if (val == 0) continue;
      int col = i % cols;
      int dataCol = col % dataCols;
      float x = col * width * (float) 1.2;
      float y = ((i / cols) + 1) * height * (float) 1.2;

      float range = sliderList.colMax[dataCol] - sliderList.colMin[dataCol];
      float normalisedVal = 1;
      if (range > 0) normalisedVal = (val - sliderList.colMin[dataCol]) / range;

      applet.pushMatrix();
      applet.translate(x, y);
      applet.fill(200, 200, 200);
      applet.rect(0, 0, width, height);
      applet.fill(columnColors[dataCol][0], columnColors[dataCol][1], columnColors[dataCol][2]);
      applet.rect(0, 0, (int) (width * normalisedVal), height);
      applet.popMatrix();
    }
    applet.popMatrix();
  }

  // void menu(int i) { PApplet.println("got some slider-list event from item with index " + i); }

  public void controlEvent(ControlEvent theEvent) {
    /* TODO
    if (theEvent.isFrom("menu")) {
      int index = (int) theEvent.getValue();
      Map sliderList = ((SliderList) theEvent.getController()).getItem(index);
      PApplet.println("got a slider event from item : " + sliderList);
      rotation[index] = f(sliderList.get("sliderValue"));
    }
    */
  }

}

