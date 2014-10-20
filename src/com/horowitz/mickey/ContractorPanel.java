package com.horowitz.mickey;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import com.horowitz.mickey.data.DataStore;
import com.horowitz.mickey.data.Mission;
import com.horowitz.mickey.data.Objective;

public class ContractorPanel extends JPanel {
  private JToolBar  _toolbar;
  private CCanvas   _canvas;

  private String    _contractor;
  private Mission   _mission;
  private JPanel _objectivesPanel;

  public ContractorPanel(Mission mission) {
    super();
    _contractor = mission.getContractor().toLowerCase();
    _mission = mission;
    initToolbar();
    initForm();
  }

  private boolean isRunning(String threadName) {
    boolean isRunning = false;
    Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
    for (Iterator<Thread> it = threadSet.iterator(); it.hasNext();) {
      Thread thread = it.next();
      if (thread.getName().equals(threadName)) {
        isRunning = true;
        break;
      }
    }
    return isRunning;
  }

  private void initToolbar() {
    _toolbar = new JToolBar();

    JButton scanNow = new JButton(new AbstractAction("Scan now") {

      @Override
      public void actionPerformed(ActionEvent e) {
        if (!isRunning("CSTHREAD")) {
          final Settings commands = Settings.createCommands();
          commands.setProperty("contractor", _contractor);
          commands.setProperty("command", "contractor");
          Thread csThread = new Thread(new Runnable() {

            public void run() {
              long start = System.currentTimeMillis();
              long now;
              boolean weredone = false;
              do {
                try {
                  Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                String status = commands.getProperty("contractor.scan");
                if ("done".equals(status)) {
                  // we're done
                  weredone = true;
                  updateView();
                }
                now = System.currentTimeMillis();
              } while (!weredone && now - start < 1000 * 120); // 2 minutes
            }
          }, "CSTHREAD");
          csThread.start();
        }
      }
    });
    _toolbar.add(scanNow);

    JButton scanOffline = new JButton(new AbstractAction("Scan offline") {

      @Override
      public void actionPerformed(ActionEvent e) {
        Thread t = new Thread(new Runnable() {
          public void run() {
            scanContractorFromImage();
          }
        });
        t.start();
      }
    });
    _toolbar.add(scanOffline);

    JButton saveButton = new JButton(new AbstractAction("Save") {

      @Override
      public void actionPerformed(ActionEvent e) {
        Thread t = new Thread(new Runnable() {
          public void run() {
            save();
          }
        });
        t.start();
      }
    });
    _toolbar.add(saveButton);

    setLayout(new BorderLayout());
    add(_toolbar, BorderLayout.NORTH);
  }
  
  private void initForm() {
    JPanel formPanel = new JPanel(new BorderLayout());
    _canvas = new CCanvas();
    formPanel.add(_canvas, BorderLayout.WEST);
    _objectivesPanel = new JPanel();
    formPanel.add(_objectivesPanel);
    add(formPanel);// to the center of the main panel
    
    updateView();
  }

  private void scanContractorFromImage() {
    // TODO
  }

  private void save() {
    // TODO
  }

  protected void updateView() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        try {
          File f = new File(_contractor + "_objectives.bmp");
          BufferedImage image = ImageIO.read(f);
          _canvas._image = image;

          _canvas.revalidate();
          
          _objectivesPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
          List<Objective> objectives = _mission.getObjectives();
          for (Objective objective : objectives) {
            String mat = objective.getMaterial().toLowerCase();
            System.err.println("contracts/"+mat+"24.bmp");
            ImageIcon icon = ImageManager.getImage("contracts/"+mat+"24.png");
            System.err.println(icon);
            JLabel objLabel = new JLabel(icon);
            objLabel.setText("" + objective.getCurrentAmount() + " / " + objective.getNeededAmount());
            _objectivesPanel.add(objLabel);
            _objectivesPanel.revalidate();
          }
          
          
          
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
  }

  class CCanvas extends JPanel {

    public CCanvas() {
      setMaximumSize(new Dimension(1000, 150));
    }

    BufferedImage _image = null;

    public void paint(Graphics g) {
      super.paint(g);
      if (_image != null) {
        g.drawImage(_image, 0, 0, null);
      }
    }

    @Override
    public Dimension getPreferredSize() {
      Dimension ps = super.getPreferredSize();
      if (_image != null) {
        ps = new Dimension(_image.getWidth(), _image.getHeight());
      }
      return ps;
    }
  }

  public static void main(String[] args) {
    try {
      JFrame frame = new JFrame("Contractor Assistant");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      Mission[] missions = new DataStore().readMissions("george");
      for (Mission mission : missions) {
        if (mission.getNumber() == 30) {
          frame.getContentPane().add(new ContractorPanel(mission), BorderLayout.CENTER);
          break;
        }
      }
      
      frame.pack();
      frame.setSize(new Dimension(frame.getSize().width + 3, frame.getSize().height + 2));

      frame.setLocationRelativeTo(null);

      frame.setVisible(true);
    } catch (HeadlessException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }
}
