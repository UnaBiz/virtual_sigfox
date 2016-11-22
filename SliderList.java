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
import processing.core.PGraphics;
import java.util.HashMap;
import java.util.Map;

class SliderList extends Controller<SliderList> {

  private float pos, npos;
  private int itemHeight = 60;
  private int scrollerLength = 40;
  private int sliderWidth = 50;
  private int sliderHeight = 15;
  private int sliderX = 10;
  private int sliderY = 25;

  private int dragMode = 0;
  private int dragIndex = -1;
  private int rowCount = 0, colCount = 0;  //  Number of rows and cols.

  private HashMap<String, Map<String, Object>> items2 = new HashMap<>();
  private PGraphics menu;
  private boolean updateMenu;

  SliderList(PApplet applet, ControlP5 c, String theName, int theWidth, int theHeight) {
    super(c, theName, 0, 0, theWidth, theHeight);
    c.register(this);
    menu = applet.createGraphics(getWidth(), getHeight());

    setView(new ControllerView<SliderList>() {

      public void display(PGraphics pg, SliderList t) {
        if (updateMenu) {
          updateMenu();
        }
        if (inside()) { // draw scrollbar
          menu.beginDraw();
          int len = -(itemHeight * rowCount) + getHeight();
          int ty = (int) (PApplet.map(pos, len, 0, getHeight() - scrollerLength - 2, 2));
          menu.fill(128);
          menu.rect(getWidth() - 6, ty, 4, scrollerLength);
          menu.endDraw();
        }
        pg.image(menu, 0, 0);
      }
    });
    updateMenu();
  }

  // only update the image buffer when necessary - to save some resources
  private void updateMenu() {
    int len = -(itemHeight * rowCount) + getHeight();
    npos = PApplet.constrain(npos, len, 0);
    pos += (npos - pos) * 0.1;

    /// draw the SliderList
    menu.beginDraw();
    menu.noStroke();
    menu.background(240);
    menu.textFont(cp5.getFont().getFont());
    menu.pushMatrix();
    menu.translate(0, (int) (pos));
    menu.pushMatrix();

    if (rowCount > 0) renderMenu();
    menu.popMatrix();
    menu.popMatrix();
    menu.endDraw();
    if (rowCount == 0) {
      updateMenu = false;
    } else {
      updateMenu = PApplet.abs(npos - pos) > 0.01;
    }
  }

  private void renderMenu() {
    //  Render each row of the menu.
    int i0 = PApplet.max(0, (int) (PApplet.map(-pos, 0, itemHeight * rowCount,
        0, rowCount)));
    int range = PApplet.ceil(((float) (getHeight()) / (float) (itemHeight)) + 1);
    int i1 = PApplet.min(rowCount, i0 + range);
    menu.translate(0, i0 * itemHeight);
    for (int i = i0; i < i1; i++) {
      renderRow(i);
    }
  }

  private void renderRow(int row) {
    //  Render one row of the menu.
    menu.noStroke();
    menu.fill(200);
    menu.rect(0, itemHeight - 1, getWidth(), 1);
    // uncomment the following line to use a different font than the default controlP5 font
    //menu.textFont(f1);

    for (int col = 0; col < 4; col++) {
      //  Render each column.
      Map m = getItem(row, col);
      if (m == null) continue;

      float min = f(m.get("sliderValueMin"));
      float max = f(m.get("sliderValueMax"));
      Object val = f(m.get("sliderValue"));
      String label = m.get("label").toString().toUpperCase();

      String txt;
      float val2 = 0;
      if (val instanceof Float) {
        val2 = (float) val;
        txt = String.format("%s   %.2f", label, val2);
      }
      else txt = String.format("%s   %s", label, val);

      int left = (int) (col * (sliderWidth * 1.1));
      menu.fill(150);
      menu.text(txt, left + 10, 20);
      menu.fill(255);
      menu.rect(left + sliderX, sliderY, sliderWidth, sliderHeight);
      menu.fill(100, 230, 128);
      menu.rect(left + sliderX, sliderY, PApplet.map(val2, min, max, 0, sliderWidth), sliderHeight);
    }
    menu.translate(0, itemHeight);
  }

  // when detecting a click, check if the click happened to the far right,
  // if yes, scroll to that position, otherwise do whatever this item of
  // the list is supposed to do.
  public void onClick() {
    if (getPointer().x() > getWidth() - 10) {
      npos = -PApplet.map(getPointer().y(), 0, getHeight(), 0, rowCount * itemHeight);
      updateMenu = true;
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
    updateMenu = true;
  }

  public void onDrag() {
    switch (dragMode) {
      case (1): // drag and scroll the list
        npos += getPointer().dy() * 2;
        updateMenu = true;
        break;
      case (2): // drag slider
        /*  TODO
        float min = f(items.get(dragIndex).get("sliderValueMin"));
        float max = f(items.get(dragIndex).get("sliderValueMax"));
        float val = PApplet.constrain(PApplet.map(getPointer().x() - sliderX, 0, sliderWidth, min, max), min, max);
        items.get(dragIndex).put("sliderValue", val);
        setValue(dragIndex);
        updateMenu = true;
        */
        break;
    }
  }

  public void onScroll(int n) {
    npos += (n * 4);
    updateMenu = true;
  }

  void addItem(int row, int col, Map<String, Object> item) {
    rowCount = Math.max(rowCount, row + 1);
    colCount = Math.max(colCount, col + 1);
    String key = "" + row + "|" + col;
    items2.put(key, item);
    updateMenu = true;
  }

  Map<String, Object> getItem(int row, int col) {
    String key = "" + row + "|" + col;
    return items2.get(key);
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
