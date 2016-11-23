import controlP5.ControlEvent;
import controlP5.ControlP5;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;

import java.util.HashMap;
import java.util.Map;

class View {

  static final int dataCols = 3;  //  Number of data columns.
  static final int displayRows = 4, displayCols = 9, width = 40, height = 20;  //  Grid display.
  static final int colGroups = displayCols / dataCols;  //  How many groups of columns.
  static final int sliderWidth = 250, sliderHeight = 350, sliderLeft = 40, sliderTop = 20;
  static final int[][] columnColors = {
      {0, 128, 255},
      {255, 0, 128},
      {128, 200, 0}
  };

  private ControlP5 cp5;
  private PFont font;
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
    font = applet.createFont("Helvetica", 12);
    sliderList = new SliderList(applet, cp5, "sliderList", sliderWidth, sliderHeight, font);
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
    applet.textFont(font);
    applet.background(220);
    applet.noStroke();
    applet.pushMatrix();
    applet.translate(sliderWidth + sliderLeft + 20, sliderTop);
    for (int row = 0; row < sliderList.rowCount; row++) {
      //  Wrap each row to the next column group.
      int displayRow = row % displayRows;
      for (int col = 0; col < dataCols; col++) {
        //  Display in groups of dataCols: col 0, 1, 2, 0, 1, 2, ...
        int displayCol = dataCols * (row / displayRows) + col;
        float val = sliderList.getValue(row, col);
        float x = displayCol * width * 1.2f;
        float y = (displayRow + 1) * height * 1.2f;

        float range = sliderList.colMax[col] - sliderList.colMin[col];
        float normalisedVal = 1;
        if (range > 0) normalisedVal = (val - sliderList.colMin[col]) / range;

        applet.pushMatrix();
        applet.translate(x, y);
        applet.fill(200, 200, 200);
        applet.rect(0, 0, width, height);
        applet.fill(columnColors[col][0], columnColors[col][1], columnColors[col][2]);
        applet.rect(0, 0, (int) (width * normalisedVal), height);
        applet.fill(0);
        applet.text("" + val, 5, 15);
        applet.popMatrix();
      }
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

