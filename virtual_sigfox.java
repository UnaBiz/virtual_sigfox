//  Read the debug output from the Arduino device to watch out for SIGFOX
//  messages sent by the device.  Send a copy of the message to UnaCloud / AWS
//  to emulate thw transmission of the message to the cloud.
//  Based on
//  https://learn.sparkfun.com/tutorials/connecting-arduino-to-processing
//  https://github.com/sojamo/controlp5/blob/master/examples/experimental/ControlP5SliderList/ControlP5SliderList.pde
//  https://github.com/mkj-is/processing-intellij

import processing.core.PApplet;

public class virtual_sigfox extends PApplet {

  //  If you have multiple serial ports, change this number to select the specific port.
  //  Valid values: 0 to (total number of ports) - 1.
  public final static int serialPortIndex = 0;

  public final static String[] emulateServerURLs = {  //  URLs for the SIGFOX emulation servers.
      //  Can't use https because need to install SSL cert locally. So we use nginx to run
      //  a http proxy to https.  See nginx.conf.
      //  "https://l0043j2svc.execute-api.us-west-2.amazonaws.com/prod/ProcessSIGFOXMessage"
      "http://chendol.tp-iot.com/prod/ProcessSIGFOXMessage"  //  nginx proxy to API Gateway.
  };

  public final static boolean testMode = true;

  public static MainController controller = null;  //  Controller to drive the main logic.
  public static View view = null;  //  View for displaying the UI.

  //  Main function to start the app.
  public static void main(String args[]) { PApplet.main("virtual_sigfox"); }

  public virtual_sigfox() {  //  Construct the Processing app instance.
    //  Create the controller and view.
    controller = new MainController(this);
    view = new View(this);
  }

  @Override
  public void settings() { //  Will be called only once.
    //  Delegate to the controller and view.
    controller.settings();
    view.settings();
  }

  @Override
  public void setup() {  //  Will be called only once.
    //  Delegate to the controller and view.
    controller.setup();
    view.setup();
  }

  @Override
  public void draw() {  //  Will be called when screen needs to be refreshed.
    //  Delegate to the controller and view.
    controller.draw();
    view.draw();
  }

}