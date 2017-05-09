package com.horowitz.mickey.scanner;

import java.awt.AWTException;
import java.awt.Robot;

public class KeyboardRobot {
  private Robot robot;
  
  public KeyboardRobot() throws AWTException {
    super();
    robot = new Robot();
  }

  public void enterText(String s) {
    s = s.toUpperCase();
    if (s != null) {
      for (int i = 0; i < s.length(); i++){
        char ch = s.charAt(i);
        robot.keyPress(ch);
        robot.keyRelease(ch);
        robot.delay(500);
      }
    }
  }
}