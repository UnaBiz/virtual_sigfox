import processing.core.PApplet;
import processing.serial.*;

import static processing.core.PApplet.str;

class MainController {

  private PApplet applet = null;
  private Serial arduinoPort;  // Serial port connected to Arduino debug output.
  private String line;

  MainController(PApplet applet0) {
    applet = applet0;
  }

  void settings() { //  Will be called only once.
  }

  void setup() {  //  Will be called only once.
    //  Open the serial port that connects to the Arduino device.
    String[] serialPorts = Serial.list();
    PApplet.println(prefix + "Found COM / serial ports: " + PApplet.join(serialPorts, ", "));
    if (virtual_sigfox.serialPortIndex >= serialPorts.length) {
      if (serialPorts.length == 0)
        PApplet.println("****Error: No COM / serial ports found. Check your Arduino USB connection");
      else if (virtual_sigfox.serialPortIndex > 0)
        PApplet.println("****Error: No COM / serial ports found at index " + str(virtual_sigfox.serialPortIndex) +
            ". Edit unabiz_emulator.pde, change serialPortIndex to a number between 0 and " +
            str(serialPorts.length - 1));
      applet.exit();
    }
    String portName = Serial.list()[virtual_sigfox.serialPortIndex];
    PApplet.println(prefix + "Connecting to Arduino at port " + portName + "...");
    arduinoPort = new Serial(applet, portName, 9600);
    //  Upon connection, the Arduino will automatically restart the sketch.
  }

  void draw() {  //  Will be called when screen needs to be refreshed.
    //  Receive one line at a time from the serial port, which is connected
    //  to the debug output of the Arduino.
    if (arduinoPort.available() > 0) {  // If data is available,
      line = arduinoPort.readStringUntil('\n');  // read it and store it in val
    }
    if (line != null) {
      PApplet.print(line);
      processMessage(line);  //  Send the line to UnaCloud or AWS if necessary.
    }
    line = null;
  }

  private void sendEmulatedMessage(String device, String data) {
    //  Send a message to UnaCloud or AWS to emulate a device message.
    String json = "{\"device\": \"" + device + "\", \"data\": \"" + data + "\"}";
    PApplet.println(prefix + "Emulating SIGFOX message:" + json + "...");
    for (String url: virtual_sigfox.emulateServerURLs) {
      //  For each emulate server URL, send the device and data.
      JSONRequest post = new JSONRequest(url);
      post.addHeader("Content-Type", "application/json");
      post.addJson(json);
      post.send();
      PApplet.println(prefix + "Emulate server reponse:" + post.getContent());
    }
    virtual_sigfox.view.addItem();
  }

  private void processMessage(String line) {
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
        PApplet.println(prefix + "Detected message for device=" + device + ", data=" + data);
        //  Emulate the SIGFOX message by sending to an emulation server.
        sendEmulatedMessage(device, data);
        break;
      }
      case 1: {  //  stop
        PApplet.println(prefix + "Arduino stopped: " + msg);
        applet.exit();
        break;
      }
      default: break;
    }
  }

  private static String prefix = " > ";
}
