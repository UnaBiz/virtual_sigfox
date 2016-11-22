//  Read the debug output from the Arduino device to watch out for SIGFOX
//  messages sent by the device.  Send a copy of the message to UnaCloud / AWS
//  to emulate thw tansmission of the message to the cloud.
//  Based on
//  https://learn.sparkfun.com/tutorials/connecting-arduino-to-processing
//  https://github.com/sojamo/controlp5/blob/master/examples/experimental/ControlP5SliderList/ControlP5SliderList.pde
//  https://github.com/mkj-is/processing-intellij

import processing.core.PApplet;
import java.util.Map;
import controlP5.*;
import java.util.*;

import processing.core.PFont;
import processing.core.PGraphics;
import processing.serial.*;

public class virtual_sigfox extends PApplet {

  public static void main(String... args) { PApplet.main("virtual_sigfox"); }

  //  If you have multiple serial ports, change this number to select the specific port.
  //  Valid values: 0 to (total number of ports) - 1.
  static int serialPortIndex = 0;

  static String[] emulateServerURLs = {  //  URLs for the SIGFOX emulation servers.
      //  Can't use https because need to install SSL cert locally. So we use nginx to run
      //  a http proxy to https.  See nginx.conf.
      //  "https://l0043j2svc.execute-api.us-west-2.amazonaws.com/prod/ProcessSIGFOXMessage"
      "http://chendol.tp-iot.com/prod/ProcessSIGFOXMessage"  //  nginx proxy to API Gateway.
  };

  static Serial arduinoPort;  // Serial port connected to Arduino debug output.
  static String line;     // Data received from the serial port

  @Override
  public void settings() { //  Will be called only once.
    size(800, 400 ,P3D);
  }

  @Override
  public void setup() {  //  Will be called only once.
    //  Open the serial port that connects to the Arduino device.
    String[] serialPorts = Serial.list();
    println(prefix + "Found COM / serial ports: " + join(serialPorts, ", "));
    if (serialPortIndex >= serialPorts.length) {
      if (serialPorts.length == 0)
        println("****Error: No COM / serial ports found. Check your Arduino USB connection");
      else if (serialPortIndex > 0)
        println("****Error: No COM / serial ports found at index " + str(serialPortIndex) +
            ". Edit unabiz_emulator.pde, change serialPortIndex to a number between 0 and " +
            str(serialPorts.length - 1));
      exit();
    }
    String portName = Serial.list()[serialPortIndex];
    println(prefix + "Connecting to Arduino at port " + portName + "...");
    arduinoPort = new Serial(this, portName, 9600);
    //  Upon connection, the Arduino will automatically restart the sketch.
    setupUI();
  }

  @Override
  public void draw() {  //  Will be called when screen needs to be refreshed.
    //  Receive one line at a time from the serial port, which is connected
    //  to the debug output of the Arduino.
    if (arduinoPort.available() > 0) {  // If data is available,
      line = arduinoPort.readStringUntil('\n');  // read it and store it in val
    }
    if (line != null) {
      print(line);
      processMessage(line);  //  Send the line to UnaCloud or AWS if necessary.
    }
    line = null;
    drawUI();
  }

  void sendEmulatedMessage(String device, String data) {
    //  Send a message to UnaCloud or AWS to emulate a device message.
    String json = "{\"device\": \"" + device + "\", \"data\": \"" + data + "\"}";
    println(prefix + "Emulating SIGFOX message:" + json + "...");
    for (String url: emulateServerURLs) {
      //  For each emulate server URL, send the device and data.
      JSONRequest post = new JSONRequest(url);
      post.addHeader("Content-Type", "application/json");
      post.addJson(json);
      post.send();
      println(prefix + "Emulate server reponse:" + post.getContent());
    }

    m.addItem(makeItem(str(ctr) + ": tmp", (float) (32.0 + (ctr / 2.0)), 30, 40 ));
    ctr++;
  }

  int ctr = 0;

  void processMessage(String line) {
    //  Watch for messages with markers and process them.
    String[] markers = {
        "Radiocrafts.sendMessage:",  //  - Radiocrafts.sendMessage: g88pi,920e1e00b051680194597b00
        "STOPSTOPSTOP:"  //  STOPSTOPSTOP: End
    };
    if (line == null) return;
    String msg = null;  String[] msgArray = null;  int i = 0;
    //  Hunt for each marker.
    for (String marker: markers) {
      int pos = line.indexOf(marker);
      if (pos < 0) { i++; continue; }

      msg = line.substring(pos + marker.length()).trim();
      msgArray = msg.split(",");
      break;
    }
    if (msg == null) return;

    switch(i) {
      case 0: {  //  sendMessage
        String device = msgArray[0];
        String data = msgArray[1];
        println(prefix + "Detected message for device=" + device + ", data=" + data);
        //  Emulate the SIGFOX message by sending to an emulation server.
        sendEmulatedMessage(device, data);
        break;
      }
      case 1: {  //  stop
        println(prefix + "Arduino stopped: " + msg);
        exit();
        break;
      }
      default: break;
    }
  }

  static String prefix = " > ";

/////////////////////////////////////////////////////////////////////////////////

  ControlP5 cp5;

  PFont f1;
  int NUM = 100;
  float[] rotation = new float[NUM];
  SilderList m = null;

  void setupUI() {
    //  size(800, 400 ,P3D);
    f1 = createFont("Helvetica", 12);
    cp5 = new ControlP5( this );
    // create a custom SilderList with name menu, notice that function
    // menu will be called when a menu item has been clicked.
    m = new SilderList( cp5, "menu", 250, 350 );
    m.setPosition(40, 20);
    // add some items to our SilderList
  /*
  for (int i=0;i<NUM;i++) {
    m.addItem(makeItem("slider-"+i, 0, -PI, PI ));
  }
  */
  }

  // a convenience function to build a map that contains our key-value
// pairs which we will then use to render each item of the SilderList.
//
  Map<String, Object> makeItem(String theLabel, float theValue, float theMin, float theMax) {
    Map m = new HashMap<String, Object>();
    m.put("label", theLabel);
    m.put("sliderValue", theValue);
    m.put("sliderValueMin", theMin);
    m.put("sliderValueMax", theMax);
    return m;
  }

  void menu(int i) {
    println("got some slider-list event from item with index "+i);
  }

  public void controlEvent(ControlEvent theEvent) {
    if (theEvent.isFrom("menu")) {
      int index = (int) theEvent.getValue();
      Map m = ((SilderList)theEvent.getController()).getItem(index);
      println("got a slider event from item : "+m);
      rotation[index] = f(m.get("sliderValue"));
    }
  }

  void drawUI() {
    background( 220 );
    fill(0, 128, 255);
    noStroke();
    pushMatrix();
    translate(width/2, 30 );
    for (int i=0;i<NUM;i++) {
      pushMatrix();
      translate((i%10)*35, (int)(i/10)*35);
      rotate(rotation[i]);
      rect(0, 0, 20, 20);
      popMatrix();
    }
    popMatrix();
  }


// A custom Controller that implements a scrollable SilderList.
// Here the controller uses a PGraphics element to render customizable
// list items. The SilderList can be scrolled using the scroll-wheel,
// touchpad, or mouse-drag. Slider are triggered by a press or drag.
// clicking the scrollbar to the right makes the list scroll to the item
// correspoinding to the click-location.

  class SilderList extends Controller<SilderList> {

    float pos, npos;
    int itemHeight = 60;
    int scrollerLength = 40;
    int sliderWidth = 150;
    int sliderHeight = 15;
    int sliderX = 10;
    int sliderY = 25;

    int dragMode = 0;
    int dragIndex = -1;

    List< Map<String, Object>> items = new ArrayList< Map<String, Object>>();
    PGraphics menu;
    boolean updateMenu;

    SilderList(ControlP5 c, String theName, int theWidth, int theHeight) {
      super( c, theName, 0, 0, theWidth, theHeight );
      c.register( this );
      menu = createGraphics(getWidth(), getHeight());

      setView(new ControllerView<SilderList>() {

                public void display(PGraphics pg, SilderList t ) {
                  if (updateMenu) {
                    updateMenu();
                  }
                  if (inside() ) { // draw scrollbar
                    menu.beginDraw();
                    int len = -(itemHeight * items.size()) + getHeight();
                    int ty = (int)(map(pos, len, 0, getHeight() - scrollerLength - 2, 2 ) );
                    menu.fill( 128 );
                    menu.rect(getWidth()-6, ty, 4, scrollerLength );
                    menu.endDraw();
                  }
                  pg.image(menu, 0, 0);
                }
              }
      );
      updateMenu();
    }

    // only update the image buffer when necessary - to save some resources
    void updateMenu() {
      int len = -(itemHeight * items.size()) + getHeight();
      npos = constrain(npos, len, 0);
      pos += (npos - pos) * 0.1;

      /// draw the SliderList
      menu.beginDraw();
      menu.noStroke();
      menu.background(240);
      menu.textFont(cp5.getFont().getFont());
      menu.pushMatrix();
      menu.translate( 0, (int)(pos) );
      menu.pushMatrix();

      int i0 = PApplet.max( 0, (int)(map(-pos, 0, itemHeight * items.size(), 0, items.size())));
      int range = ceil(((float)(getHeight())/(float)(itemHeight))+1);
      int i1 = PApplet.min( items.size(), i0 + range );

      menu.translate(0, i0*itemHeight);

      for (int i=i0;i<i1;i++) {
        Map m = items.get(i);
        menu.noStroke();
        menu.fill(200);
        menu.rect(0, itemHeight-1, getWidth(), 1 );
        menu.fill(150);
        // uncomment the following line to use a different font than the default controlP5 font
        //menu.textFont(f1);
        String txt = String.format("%s   %.2f", m.get("label").toString().toUpperCase(), f(items.get(i).get("sliderValue")));
        menu.text(txt, 10, 20 );
        menu.fill(255);
        menu.rect(sliderX, sliderY, sliderWidth, sliderHeight);
        menu.fill(100, 230, 128);
        float min = f(items.get(i).get("sliderValueMin"));
        float max = f(items.get(i).get("sliderValueMax"));
        float val = f(items.get(i).get("sliderValue"));
        menu.rect(sliderX, sliderY, map(val, min, max, 0, sliderWidth), sliderHeight);
        menu.translate( 0, itemHeight );
      }
      menu.popMatrix();
      menu.popMatrix();
      menu.endDraw();
      updateMenu = abs(npos-pos)>0.01 ? true:false;
    }

    // when detecting a click, check if the click happend to the far right,
    // if yes, scroll to that position, otherwise do whatever this item of
    // the list is supposed to do.
    public void onClick() {
      if (getPointer().x()>getWidth()-10) {
        npos= -map(getPointer().y(), 0, getHeight(), 0, items.size()*itemHeight);
        updateMenu = true;
      }
    }


    public void onPress() {
      int x = getPointer().x();
      int y = (int)(getPointer().y()-pos)%itemHeight;
      boolean withinSlider = within(x, y, sliderX, sliderY, sliderWidth, sliderHeight);
      dragMode =  withinSlider ? 2:1;
      if (dragMode==2) {
        dragIndex = getIndex();
        float min = f(items.get(dragIndex).get("sliderValueMin"));
        float max = f(items.get(dragIndex).get("sliderValueMax"));
        float val = constrain(map(getPointer().x()-sliderX, 0, sliderWidth, min, max), min, max);
        items.get(dragIndex).put("sliderValue", val);
        setValue(dragIndex);
      }
      updateMenu = true;
    }

    public void onDrag() {
      switch(dragMode) {
        case(1): // drag and scroll the list
          npos += getPointer().dy() * 2;
          updateMenu = true;
          break;
        case(2): // drag slider
          float min = f(items.get(dragIndex).get("sliderValueMin"));
          float max = f(items.get(dragIndex).get("sliderValueMax"));
          float val = constrain(map(getPointer().x()-sliderX, 0, sliderWidth, min, max), min, max);
          items.get(dragIndex).put("sliderValue", val);
          setValue(dragIndex);
          updateMenu = true;
          break;
      }
    }

    public void onScroll(int n) {
      npos += ( n * 4 );
      updateMenu = true;
    }

    void addItem(Map<String, Object> m) {
      items.add(m);
      updateMenu = true;
    }

    Map<String, Object> getItem(int theIndex) {
      return items.get(theIndex);
    }

    private int getIndex() {
      int len = itemHeight * items.size();
      int index = (int)( map( getPointer().y() - pos, 0, len, 0, items.size() ) ) ;
      return index;
    }
  }

  public static float f( Object o ) {
    return ( o instanceof Number ) ? ( ( Number ) o ).floatValue( ) : Float.MIN_VALUE;
  }

  public static boolean within(int theX, int theY, int theX1, int theY1, int theW1, int theH1) {
    return (theX>theX1 && theX<theX1+theW1 && theY>theY1 && theY<theY1+theH1);
  }
}