import processing.core.PApplet;
import processing.serial.*;

import java.util.ArrayList;
import java.util.HashMap;

import static processing.core.PApplet.str;

class MainController {

  private PApplet applet = null;
  private Serial arduinoPort;  // Serial port connected to Arduino debug output.
  private String line;
  private int lineCount = 0;
  private long lastRead = 0;

  MainController(PApplet applet0) {  //  Construct the controller.
    applet = applet0;
  }

  void settings() { //  Will be called only once.
  }

  void setup() {  //  Will be called only once.
    if (virtual_sigfox.testMode) return;  //  Don't open serial port for test mode.
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
    if (virtual_sigfox.testMode) {  //  Test mode with simulated input.
      if (lineCount >= testInput.length) return;
      if (System.currentTimeMillis() - lastRead < 100) return;
      line = testInput[lineCount] + '\n';
    }
    else if (arduinoPort.available() > 0) {  // If data is available,
      line = arduinoPort.readStringUntil('\n');  // read it and store it in val
    }
    if (line != null) {
      PApplet.print(line);
      processMessage(line);  //  Send the line to UnaCloud or AWS if necessary.
      lastRead = System.currentTimeMillis();
      lineCount++;
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
    virtual_sigfox.view.addRow(fields);
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

  static final String[] testInput = {
      "Running setup...",
      " - Entering command mode...",
      " - Radiocrafts.sendBuffer: 00",
      " - Radiocrafts.enterCommandMode: OK",
      " - Radiocrafts.sendBuffer: 4d2801",
      " - Radiocrafts.sendBuffer: ff",
      " - Getting SIGFOX ID...",
      " - Radiocrafts.sendBuffer: 39",
      " - SIGFOX ID = g88pi",
      " - PAC =",
      " - Setting frequency for country -26112",
      " - Radiocrafts.setFrequencySG",
      " - Radiocrafts.sendBuffer: 4d0003",
      " - Radiocrafts.sendBuffer: ff",
      " - Set frequency result =",
      " - Getting frequency (expecting 3)...",
      " - Radiocrafts.sendBuffer: 5900",
      " - Frequency (expecting 3) =",
      "Running loop #0",
      " - Radiocrafts.sendBuffer: 55",
      " - Radiocrafts.sendBuffer: 56",
      " - Message.addField: ctr=0",
      " - Message.addField: tmp=36",
      " - Message.addField: vlt=12.30",
      " - Radiocrafts.sendMessage: g88pi,920e0000b051680194597b00",
      " - Radiocrafts.sendBuffer: 58",
      " - Radiocrafts.exitCommandMode: OK",
      " - Radiocrafts.sendBuffer: 0c920e0000b051680194597b00",
      "Running loop #1",
      " - Entering command mode...",
      " - Radiocrafts.sendBuffer: 00",
      " - Radiocrafts.enterCommandMode: OK",
      " - Radiocrafts.sendBuffer: 55",
      " - Radiocrafts.sendBuffer: 56",
      " - Message.addField: ctr=1",
      " - Message.addField: tmp=36",
      " - Message.addField: vlt=12.30",
      " - Radiocrafts.sendMessage: g88pi,920e0a00b051680194597b00",
      "Warning: Should wait 10 mins before sending the next message",
      " - Radiocrafts.sendBuffer: 58",
      " - Radiocrafts.exitCommandMode: OK",
      " - Radiocrafts.sendBuffer: 0c920e0a00b051680194597b00",
      "Running loop #2",
      " - Entering command mode...",
      " - Radiocrafts.sendBuffer: 00",
      " - Radiocrafts.enterCommandMode: OK",
      " - Radiocrafts.sendBuffer: 55",
      " - Radiocrafts.sendBuffer: 56",
      " - Message.addField: ctr=2",
      " - Message.addField: tmp=36",
      " - Message.addField: vlt=12.30",
      " - Radiocrafts.sendMessage: g88pi,920e1400b051680194597b00",
      "Warning: Should wait 10 mins before sending the next message",
      " - Radiocrafts.sendBuffer: 58",
      " - Radiocrafts.exitCommandMode: OK",
      " - Radiocrafts.sendBuffer: 0c920e1400b051680194597b00",
      "Running loop #3",
      " - Entering command mode...",
      " - Radiocrafts.sendBuffer: 00",
      " - Radiocrafts.enterCommandMode: OK",
      " - Radiocrafts.sendBuffer: 55",
      " - Radiocrafts.sendBuffer: 56",
      " - Message.addField: ctr=3",
      " - Message.addField: tmp=36",
      " - Message.addField: vlt=12.30",
      " - Radiocrafts.sendMessage: g88pi,920e1e00b051680194597b00",
      "Warning: Should wait 10 mins before sending the next message",
      " - Radiocrafts.sendBuffer: 58",
      " - Radiocrafts.exitCommandMode: OK",
      " - Radiocrafts.sendBuffer: 0c920e1e00b051680194597b00",
      "Running loop #4",
      " - Entering command mode...",
      " - Radiocrafts.sendBuffer: 00",
      " - Radiocrafts.enterCommandMode: OK",
      " - Radiocrafts.sendBuffer: 55",
      " - Radiocrafts.sendBuffer: 56",
      " - Message.addField: ctr=4",
      " - Message.addField: tmp=36",
      " - Message.addField: vlt=12.30",
      " - Radiocrafts.sendMessage: g88pi,920e2800b051680194597b00",
      "Warning: Should wait 10 mins before sending the next message",
      " - Radiocrafts.sendBuffer: 58",
      " - Radiocrafts.exitCommandMode: OK",
      " - Radiocrafts.sendBuffer: 0c920e2800b051680194597b00",
      "Running loop #5",
      " - Entering command mode...",
      " - Radiocrafts.sendBuffer: 00",
      " - Radiocrafts.enterCommandMode: OK",
      " - Radiocrafts.sendBuffer: 55",
      " - Radiocrafts.sendBuffer: 56",
      " - Message.addField: ctr=5",
      " - Message.addField: tmp=36",
      " - Message.addField: vlt=12.30",
      " - Radiocrafts.sendMessage: g88pi,920e3200b051680194597b00",
      "Warning: Should wait 10 mins before sending the next message",
      " - Radiocrafts.sendBuffer: 58",
      " - Radiocrafts.exitCommandMode: OK",
      " - Radiocrafts.sendBuffer: 0c920e3200b051680194597b00",
      "Running loop #6",
      " - Entering command mode...",
      " - Radiocrafts.sendBuffer: 00",
      " - Radiocrafts.enterCommandMode: OK",
      " - Radiocrafts.sendBuffer: 55",
      " - Radiocrafts.sendBuffer: 56",
      " - Message.addField: ctr=6",
      " - Message.addField: tmp=36",
      " - Message.addField: vlt=12.30",
      " - Radiocrafts.sendMessage: g88pi,920e3c00b051680194597b00",
      "Warning: Should wait 10 mins before sending the next message",
      " - Radiocrafts.sendBuffer: 58",
      " - Radiocrafts.exitCommandMode: OK",
      " - Radiocrafts.sendBuffer: 0c920e3c00b051680194597b00",
      "Running loop #7",
      " - Entering command mode...",
      " - Radiocrafts.sendBuffer: 00",
      " - Radiocrafts.enterCommandMode: OK",
      " - Radiocrafts.sendBuffer: 55",
      " - Radiocrafts.sendBuffer: 56",
      " - Message.addField: ctr=7",
      " - Message.addField: tmp=36",
      " - Message.addField: vlt=12.30",
      " - Radiocrafts.sendMessage: g88pi,920e4600b051680194597b00",
      "Warning: Should wait 10 mins before sending the next message",
      " - Radiocrafts.sendBuffer: 58",
      " - Radiocrafts.exitCommandMode: OK",
      " - Radiocrafts.sendBuffer: 0c920e4600b051680194597b00",
      "Running loop #8",
      " - Entering command mode...",
      " - Radiocrafts.sendBuffer: 00",
      " - Radiocrafts.enterCommandMode: OK",
      " - Radiocrafts.sendBuffer: 55",
      " - Radiocrafts.sendBuffer: 56",
      " - Message.addField: ctr=8",
      " - Message.addField: tmp=36",
      " - Message.addField: vlt=12.30",
      " - Radiocrafts.sendMessage: g88pi,920e5000b051680194597b00",
      "Warning: Should wait 10 mins before sending the next message",
      " - Radiocrafts.sendBuffer: 58",
      " - Radiocrafts.exitCommandMode: OK",
      " - Radiocrafts.sendBuffer: 0c920e5000b051680194597b00",
      "Running loop #9",
      " - Entering command mode...",
      " - Radiocrafts.sendBuffer: 00",
      " - Radiocrafts.enterCommandMode: OK",
      " - Radiocrafts.sendBuffer: 55",
      " - Radiocrafts.sendBuffer: 56",
      " - Message.addField: ctr=9",
      " - Message.addField: tmp=36",
      " - Message.addField: vlt=12.30",
      " - Radiocrafts.sendMessage: g88pi,920e5a00b051680194597b00",
      "Warning: Should wait 10 mins before sending the next message",
      " - Radiocrafts.sendBuffer: 58",
      " - Radiocrafts.exitCommandMode: OK",
      " - Radiocrafts.sendBuffer: 0c920e5a00b051680194597b00",
      "STOPSTOPSTOP: Messages sent successfully: 10, failed: 0",
  };
}
