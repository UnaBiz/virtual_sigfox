import processing.core.PApplet;
import processing.serial.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static processing.core.PApplet.str;

class MainController {

  private PApplet applet = null;
  private Serial arduinoPort;  // Serial port connected to Arduino debug output.
  private String line;

  MainController(PApplet applet0) {  //  Construct the controller.
    applet = applet0;
  }

  void settings() { //  Will be called only once.
  }

  void setup() {  //  Will be called only once.
    //  Open the serial port that connects to the Arduino device.
    String[] serialPorts = Serial.list();
    //  Exclude Bluetooth ports on Mac.
    ArrayList<String> serialPortsFiltered = new ArrayList<>();
    for (String port: serialPorts) {
      if (port.toLowerCase().contains("bluetooth")) continue;
      serialPortsFiltered.add(port);
    }

    PApplet.println(prefix + "Found COM / serial ports: ");
    PApplet.printArray(serialPortsFiltered);
    if (virtual_sigfox.serialPortIndex >= serialPortsFiltered.size()) {
      if (serialPortsFiltered.size() == 0)
        PApplet.println("****Error: No COM / serial ports found. Check your Arduino USB connection");
      else if (virtual_sigfox.serialPortIndex > 0)
        PApplet.println("****Error: No COM / serial ports found at index " + str(virtual_sigfox.serialPortIndex) +
            ". Edit virtual_sigfox.java, change serialPortIndex to a number between 0 and " +
            str(serialPortsFiltered.size() - 1));
      applet.exit();
    }
    String portName = serialPortsFiltered.get(virtual_sigfox.serialPortIndex);
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
      PApplet.println(prefix + "Emulate server response:" + post.getContent());
    }
    //  Decode the fields in the message.
    HashMap<String, Object> fields = Message.decodeMessage(data);
    //  Display the fields.
    virtual_sigfox.view.addItem(fields);
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
        //  Emulate the SIGFOX message by sending to an emulation server.
        String device = msgArray[0];
        String data = msgArray[1];
        PApplet.println(prefix + "Detected message for device=" + device + ", data=" + data);
        sendEmulatedMessage(device, data);
        break;
      }
      case 1: {  //  stop
        //  Stop the program.
        PApplet.println(prefix + "Arduino stopped: " + msg);
        applet.exit();
        break;
      }
      default: break;
    }
  }

  private static String prefix = " > ";
}
