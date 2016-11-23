// A custom Controller that implements a scrollable SilderList.
// Here the controller uses a PGraphics element to render customizable
// list items. The SilderList can be scrolled using the scroll-wheel,
// touchpad, or mouse-drag. Slider are triggered by a press or drag.
// clicking the scrollbar to the right makes the list scroll to the item
// corresponding to the click-location.

import controlP5.ControlP5;
import controlP5.Controller;
import controlP5.ControllerView;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import java.util.HashMap;
import java.util.Map;

class SliderList extends Controller<SliderList> {

  //  For faster rendering of the grid, we cache the values into this matrix.
  final static int valuesRows = View.displayRows * View.colGroups;
  float[][] values = new float[valuesRows][View.dataCols];
  float[] colMin = new float[View.dataCols];  //  Min value of each col.
  float[] colMax = new float[View.dataCols];  //  Max value of each col.

  private PFont font;
  private float pos, npos;
  private int itemHeight = 60;
  private int scrollerLength = 40;
  private int sliderWidth = 70;
  private int sliderHeight = 15;
  private int sliderX = 10;
  private int sliderY = 25;

  private int dragMode = 0;
  private int dragIndex = -1;
  public int rowCount = 0;
  private int colCount = 0;  //  Number of displayRows and displayCols.

  private HashMap<String, Map<String, Object>> items2 = new HashMap<>();
  private PGraphics sliderList;
  private boolean updateDisplay;

  SliderList(PApplet applet, ControlP5 c, String theName, int theWidth, int theHeight,
             PFont font0) {
    super(c, theName, 0, 0, theWidth, theHeight);
    font = font0;
    c.register(this);
    sliderList = applet.createGraphics(getWidth(), getHeight());

    setView(new ControllerView<SliderList>() {

      public void display(PGraphics pg, SliderList t) {
        if (updateDisplay) {
          refreshDisplay();
        }
        if (inside()) { // draw scrollbar
          sliderList.beginDraw();
          int len = -(itemHeight * rowCount) + getHeight();
          int ty = (int) (PApplet.map(pos, len, 0, getHeight() - scrollerLength - 2, 2));
          sliderList.fill(128);
          sliderList.rect(getWidth() - 6, ty, 4, scrollerLength);
          sliderList.endDraw();
        }
        pg.image(sliderList, 0, 0);
      }
    });
    refreshDisplay();
  }

  // only update the image buffer when necessary - to save some resources
  private void refreshDisplay() {
    int len = -(itemHeight * rowCount) + getHeight();
    npos = PApplet.constrain(npos, len, 0);
    pos += (npos - pos) * 0.1;

    /// draw the SliderList
    sliderList.beginDraw();
    sliderList.noStroke();
    sliderList.background(240);
    sliderList.textFont(cp5.getFont().getFont());
    sliderList.pushMatrix();
    sliderList.translate(0, (int) (pos));
    sliderList.pushMatrix();

    if (rowCount > 0) render();
    sliderList.popMatrix();
    sliderList.popMatrix();
    sliderList.endDraw();
    if (rowCount == 0) {
      updateDisplay = false;
    } else {
      if (itemHeight * rowCount < getHeight()) npos = 0;
      updateDisplay = PApplet.abs(npos - pos) > 0.01;
    }
  }

  private void render() {
    //  Render each row of the sliderList.
    int i0 = PApplet.max(0, (int) (PApplet.map(-pos, 0, itemHeight * rowCount,
        0, rowCount)));
    int range = PApplet.ceil(((float) (getHeight()) / (float) (itemHeight)) + 1);
    int i1 = PApplet.min(rowCount, i0 + range);
    sliderList.translate(0, i0 * itemHeight);
    for (int i = i0; i < i1; i++) {
      renderRow(i);
    }
  }

  private void renderRow(int row) {
    //  Render one row of the sliderList.
    sliderList.noStroke();
    sliderList.fill(200);
    sliderList.rect(0, itemHeight - 1, getWidth(), 1);
    sliderList.textFont(font);
    for (int col = 0; col < 4; col++) {
      //  Render each column.
      renderCell(row, col);
    }
    sliderList.translate(0, itemHeight);
  }

  private void renderCell(int row, int col) {
    //  Render the cell at (row,col).
    Map m = getItem(row, col);
    if (m == null) return;

    Object val = f(m.get("sliderValue"));
    Object display = f(m.get("display"));
    String label = m.get("label").toString(), txt = "";
    float val2 = 0, min = colMin[col], max = colMax[col];

    //  Format for display.
    if (val instanceof Float) {
      val2 = (float) val;
      txt = String.format("%s   %.2f", label, val2);
    }
    else txt = String.format("%s   %s", label, val);

    int left = (int) (col * (sliderWidth * 1.1));
    sliderList.fill(150);
    sliderList.text(txt, left + 15, 20);
    if (col == 0) {
      //  Show row header.
      sliderList.text("" + row + ":", left, 20);
    }
    sliderList.fill(255);
    sliderList.rect(left + sliderX, sliderY, sliderWidth, sliderHeight);
    sliderList.fill(View.columnColors[col][0], View.columnColors[col][1], View.columnColors[col][2]);
    if (min != max) {
      sliderList.rect(left + sliderX, sliderY, PApplet.map(val2, min, max, 0, sliderWidth),
          sliderHeight);
    }
  }

  // when detecting a click, check if the click happened to the far right,
  // if yes, scroll to that position, otherwise do whatever this item of
  // the list is supposed to do.
  public void onClick() {
    if (getPointer().x() > getWidth() - 10) {
      npos = -PApplet.map(getPointer().y(), 0, getHeight(), 0, rowCount * itemHeight);
      updateDisplay = true;
    }
  }

  public void onPress() {
    int x = getPointer().x();
    int y = (int) (getPointer().y() - pos) % itemHeight;
    boolean withinSlider = within(x, y, sliderX, sliderY, sliderWidth, sliderHeight);
    dragMode = withinSlider ? 2 : 1;
    if (dragMode == 2) {
      /* TODO
      dragIndex = getIndex();
      float min = f(items.get(dragIndex).get("sliderValueMin"));
      float max = f(items.get(dragIndex).get("sliderValueMax"));
      float val = PApplet.constrain(PApplet.map(getPointer().x() - sliderX, 0, sliderWidth, min, max), min, max);
      items.get(dragIndex).put("sliderValue", val);
      setValue(dragIndex);
      */
    }
    updateDisplay = true;
  }

  public void onDrag() {
    switch (dragMode) {
      case (1): // drag and scroll the list
        npos += getPointer().dy() * 2;
        updateDisplay = true;
        break;
      case (2): // drag slider
        /*  TODO
        float min = f(items.get(dragIndex).get("sliderValueMin"));
        float max = f(items.get(dragIndex).get("sliderValueMax"));
        float val = PApplet.constrain(PApplet.map(getPointer().x() - sliderX, 0, sliderWidth, min, max), min, max);
        items.get(dragIndex).put("sliderValue", val);
        setValue(dragIndex);
        updateDisplay = true;
        */
        break;
    }
  }

  public void onScroll(int n) {
    npos += (n * 4);
    updateDisplay = true;
  }

  void addItem(int row, int col, Map<String, Object> item) {
    rowCount = Math.max(rowCount, row + 1);
    colCount = Math.max(colCount, col + 1);
    String key = "" + row + "|" + col;
    items2.put(key, item);
    updateDisplay = true;

    Object val = f(item.get("sliderValue"));
    String label = item.get("label").toString(), display = "";
    float val2 = 0.001f, min = 0, max = 1;

    //  Compute the min max.
    if (val instanceof Float) {
      val2 = (float) val;
      display = String.format("%s   %.2f", label, val2);
      if (row == 0) {
        colMin[col] = val2;
        colMax[col] = val2;
      }
      min = Math.min(colMin[col], val2);
      max = Math.max(colMax[col], val2);
    }
    else display = String.format("%s   %s", label, val);
    item.put("display", display);

    //  Save into the values array.
    values[row % valuesRows][col] = val2;
    colMin[col] = min;
    colMax[col] = max;

    //  TODO: Compare with previous min-max.  If changed, update the min-max for each row.
    for (int r = 0; r < row; r++) {
      renderCell(r, col);
    }
  }

  Map<String, Object> getItem(int row, int col) {
    String key = "" + row + "|" + col;
    if (!items2.containsKey(key)) return null;
    return items2.get(key);
  }

  float getValue(int row, int col) {
    return values[row % valuesRows][col];
  }

  private int getIndex() {
    int len = itemHeight * rowCount;
    int index = (int) (PApplet.map(getPointer().y() - pos, 0, len, 0, rowCount));
    return index;
  }

  private static float f(Object o) {
    return (o instanceof Number) ? ((Number) o).floatValue() : Float.MIN_VALUE;
  }

  private static boolean within(int theX, int theY, int theX1, int theY1, int theW1, int theH1) {
    return (theX > theX1 && theX < theX1 + theW1 && theY > theY1 && theY < theY1 + theH1);
  }
}
