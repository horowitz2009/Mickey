package com.horowitz.mickey;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import com.horowitz.commons.DateUtils;
import com.horowitz.commons.MouseRobot;
import com.horowitz.commons.RobotInterruptedException;
import com.horowitz.mickey.common.MyImageIO;
import com.horowitz.mickey.service.Service;

public class DropboxRemote extends JFrame {
  public static void main(String[] args) {
    DropboxRemote dr = new DropboxRemote();
    dr.runListener();
  }

  private int              turn    = 0;
  private MouseRobot       _mouse;
  private Service          service = new Service("-tiktak");
  private static JTextArea outputConsole;
  private JToggleButton    _pingToggle;

  public DropboxRemote() {
    super();
    setTitle("DropboxRemote 928");
    setSize(350, 150);
    outputConsole = new JTextArea(8, 14);
    JToolBar toolbar = new JToolBar();
    _pingToggle = new JToggleButton("Ping");
    toolbar.add(_pingToggle);
    toolbar.setFloatable(false);

    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(toolbar, BorderLayout.NORTH);
    getContentPane().add(new JScrollPane(outputConsole), BorderLayout.CENTER);
    setAlwaysOnTop(true);
    setVisible(true);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    try {
      _mouse = new MouseRobot();

      File d = new File("requests");
      publish("Listening to " + d.getAbsolutePath());

    } catch (AWTException e) {
      e.printStackTrace();
    }
  }

  private void processRequests() throws RobotInterruptedException {
    publish("" + ++turn);
    String[] requests = service.getActiveRequests();
    for (String r : requests) {
      if (r.startsWith("c")) {
        System.out.println("captured a request: " + r);
        publish("captured a request: " + r);

        service.inProgress(r);
        processClick(r);
      } else if (r.startsWith("p")) {
        captureScreen();
        _mouse.delay(100, false);
      }
    }

    service.purgeOld(1000 * 60 * 60);// 1 hour old
  }

  private void processClick(String r) {
    try {
      captureScreen();
      _mouse.delay(100, false);
      String[] ss = r.split("[-_.]");
      int x = Integer.parseInt(ss[1]);
      int y = Integer.parseInt(ss[2]);
      _mouse.click(x, y);
      System.out.println("click " + x + ", " + y);
      publish("click " + x + ", " + y);
      _mouse.delay(1000, false);
      captureScreen();
      _mouse.delay(10000, false);
      captureScreen();
    } catch (Throwable e) {
      e.printStackTrace();
      publish("ERROR " + e.getMessage());
    } finally {
      System.out.println("done");
      publish("done");
      service.done(r);
    }
  }
  
  private void deleteOlder(String prefix, int amountFiles) {
    File f = new File(".");
    File[] files = f.listFiles();
    List<File> targetFiles = new ArrayList<File>(6);
    int cnt = 0;
    for (File file : files) {
      if (!file.isDirectory() && file.getName().startsWith(prefix)) {
        targetFiles.add(file);
        cnt++;
      }
    }

    if (cnt > amountFiles) {
      // delete some files
      Collections.sort(targetFiles, new Comparator<File>() {
        public int compare(File o1, File o2) {
          if (o1.lastModified() > o2.lastModified())
            return 1;
          else if (o1.lastModified() < o2.lastModified())
            return -1;
          return 0;
        };
      });

      int c = cnt - 5;
      for (int i = 0; i < c; i++) {
        File fd = targetFiles.get(i);
        fd.delete();
      }
    }
  }

  private void captureScreen() {
    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    writeImage(new Rectangle(0, 0, screenSize.width, screenSize.height), "ping " + DateUtils.formatDateForFile(System.currentTimeMillis()) + ".png");
    publish("ping");
    deleteOlder("ping", 12);
  }

  public void writeImage(Rectangle rect, String filename) {
    try {
      writeImage(new Robot().createScreenCapture(rect), filename);
    } catch (AWTException e) {
      e.printStackTrace();
    }
  }

  public void writeImage(BufferedImage image, String filename) {

    try {
      int ind = filename.lastIndexOf("/");
      if (ind > 0) {
        String path = filename.substring(0, ind);
        File f = new File(path);
        f.mkdirs();
      }
      File file = new File(filename);
      MyImageIO.write(image, filename.substring(filename.length() - 3).toUpperCase(), file);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void runListener() {
    Thread settingsThread = new Thread(new Runnable() {
      public void run() {
        boolean stop = false;
        long start = System.currentTimeMillis();
        do {
          try {
            long now = System.currentTimeMillis();
            if (_pingToggle.isSelected()) {
              if (now - start > 1 * 60 * 1000) {
                start = now;
                captureScreen();
              }
            
            }
            processRequests();
          } catch (Throwable t) {
            publish("-= ERROR =-");
            publish(t.getMessage());
          }

          try {
            Thread.sleep(5000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }

        } while (!stop);
      }
    }, "SETTINGS");

    settingsThread.start();

  }

  public void publish(String msg) {
    String text = outputConsole.getText();
    if (text.length() > 3000) {
      outputConsole.setText("");
    }
    outputConsole.append(msg);
    outputConsole.append("\n");
    outputConsole.setCaretPosition(outputConsole.getDocument().getLength());
    // outputConsole.repaint();
  }

}
