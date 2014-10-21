package com.horowitz.mickey;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.Format;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import com.horowitz.mickey.data.DataStore;
import com.horowitz.mickey.data.Mission;
import com.horowitz.mickey.data.Objective;

public final class ContractorPanel extends JPanel implements PropertyChangeListener {
  private JToolBar _toolbar;
  private CCanvas  _canvas;

  private String   _contractor;
  private Mission  _mission;
  private JPanel   _objectivesPanel;

  public ContractorPanel(String contractor) {
    super();
    _contractor = contractor;
    _mission = null;
    initToolbar();
    initForm();
  }

  public String getContractor() {
    return _contractor;
  }

  public Mission getMission() {
    return _mission;
  }

  public void setMission(Mission mission) {
    _mission = mission;
    updateView();
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
                  scanContractorFromImage();
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

    setLayout(new BorderLayout());
    add(_toolbar, BorderLayout.NORTH);
  }

  private void initForm() {
    JPanel formPanel = new JPanel(new BorderLayout());
    _canvas = new CCanvas();
    formPanel.add(_canvas, BorderLayout.WEST);
    _objectivesPanel = new JPanel();
    formPanel.add(new JScrollPane(_objectivesPanel));

    
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
    
    Box saveBox = Box.createHorizontalBox();
    
    saveBox.add(Box.createHorizontalGlue());
    saveBox.add(saveButton);
    saveBox.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    formPanel.add(saveBox, BorderLayout.SOUTH);

    add(formPanel);// to the center of the main panel
    updateView();
  }
  
  

  public void scanContractorFromImage() {
    try {
      File f = new File(_contractor + "_missionNumber.bmp");
      BufferedImage image = ImageIO.read(f);

      MissionScanner mscanner = new MissionScanner();
      int[] numbers = mscanner.scanMissionNumbersDirect(image);
      if (numbers != null) {
        Mission[] missions = new DataStore().readMissions(_contractor);
        for (Mission mission : missions) {
          if (mission.getNumber() == numbers[0]) {
            updateImage();
            _mission = mission;
            mscanner.scanCurrentMissionDirect(_canvas._image, _mission);
            updateView();
          }
        }
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  private void save() {
    if (_mission != null) {
      try {
        DataStore dataStore = new DataStore();
        dataStore.writeCurrentMission(_mission.copy());
        dataStore.writeMission(_mission.copyNeeded());
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
  
  private void updateImage() throws IOException {
    File f = new File(_contractor + "_objectives.bmp");
    BufferedImage image = ImageIO.read(f);
    _canvas._image = image;
    _canvas.revalidate();
  }

  private void updateView() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        if (_mission != null) {
          try {
            clear();

            updateImage();

            _objectivesPanel.setLayout(new GridBagLayout());
            List<Objective> objectives = _mission.getObjectives();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.insets = new Insets(10, 10, 5, 5);
            NumberFormat nf = NumberFormat.getIntegerInstance();

            for (Objective objective : objectives) {
              String mat = objective.getMaterial().toLowerCase();
              System.err.println(mat);
              ImageIcon icon = ImageManager.getImage("contracts/" + mat + "24.png");
              if (icon == null) {
                icon = ImageManager.getImage("contracts/" + "unknown" + "24.png");
              }
              JLabel objLabel = new JLabel(icon);
              JLabel objLabel2 = new JLabel(" / ");
              objLabel2.setFont(objLabel2.getFont().deriveFont(15f));

              FocusTextField tf1 = new FocusTextField(nf);
              tf1.setMargin(new Insets(1, 1, 1, 4));
              tf1.setColumns(7);
              tf1.setName(mat + "_currentAmount");
              tf1.setHorizontalAlignment(JTextField.RIGHT);
              tf1.setValue(objective.getCurrentAmount());
              tf1.addPropertyChangeListener("value", ContractorPanel.this);

              FocusTextField tf2 = new FocusTextField(nf);
              tf2.setMargin(new Insets(1, 1, 1, 4));
              tf2.setColumns(7);
              tf2.setName(mat + "_neededAmount");
              tf2.setHorizontalAlignment(JTextField.RIGHT);
              tf2.setValue(objective.getNeededAmount());
              tf2.addPropertyChangeListener("value", ContractorPanel.this);

              Box box = Box.createHorizontalBox();

              box.add(objLabel);
              box.add(Box.createHorizontalStrut(5));
              box.add(tf1);
              box.add(objLabel2);
              box.add(tf2);

              _objectivesPanel.add(box, gbc);

              gbc.gridy++;
              gbc.insets = new Insets(5, 10, 5, 5);
            }

            gbc.gridwidth = 2;
            gbc.weightx = 1;
            gbc.weighty = 1;

            JLabel fake = new JLabel(" ");
            _objectivesPanel.add(fake, gbc);

            _objectivesPanel.revalidate();

          } catch (IOException e) {
            e.printStackTrace();
          }
        } else {
          clear();
        }
      }

      private void clear() {

        _canvas._image = null;
        _canvas.repaint();

        Component[] components = _objectivesPanel.getComponents();
        for (Component c : components) {
          if (c instanceof JFormattedTextField) {
            c.removePropertyChangeListener("value", ContractorPanel.this);
          }
        }
        _objectivesPanel.removeAll();
      }
    });

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

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    JFormattedTextField tf = (JFormattedTextField) evt.getSource();
    List<Objective> objectives = _mission.getObjectives();
    for (Objective o : objectives) {
      if (tf.getName().startsWith(o.getMaterial().toLowerCase())) {
        String[] ss = tf.getName().split("_");
        if (ss[1].equals("neededAmount")) {
          o.setNeededAmount((Long) tf.getValue());
        }
        if (ss[1].equals("currentAmount")) {
          o.setCurrentAmount((Long) tf.getValue());
        }
        System.err.println(o);
      }
    }
  
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

  static class FocusTextField extends JFormattedTextField {
  
    public FocusTextField(Format format) {
      super(format);
      addFocusListener(new FocusListener() {
  
        @Override
        public void focusGained(FocusEvent e) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              FocusTextField.this.selectAll();
            }
          });
          
        }
  
        @Override
        public void focusLost(FocusEvent e) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              FocusTextField.this.select(0, 0);
            }
          });
        }
      });
    }
  
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame("Contractor Assistant");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.getContentPane().add(new ContractorPanel("mahatma"), BorderLayout.CENTER);
  
    frame.setSize(new Dimension(600, 350));
  
    frame.setLocationRelativeTo(null);
  
    frame.setVisible(true);
  
  }
}
