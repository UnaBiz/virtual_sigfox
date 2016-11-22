//  Read the debug output from the Arduino device to watch out for SIGFOX
//  messages sent by the device.  Send a copy of the message to UnaCloud / AWS
//  to emulate thw tansmission of the message to the cloud.
//  Based on
//  https://learn.sparkfun.com/tutorials/connecting-arduino-to-processing
//  https://github.com/sojamo/controlp5/blob/master/examples/experimental/ControlP5SliderList/ControlP5SliderList.pde
//  https://github.com/mkj-is/processing-intellij

//  This stub is needed to compile the Processing code.
//  The actual code is in virtual_sigfox.java.

import processing.serial.*;
import controlP5.*;
import http.requests.*;