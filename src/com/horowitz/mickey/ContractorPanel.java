package com.horowitz.mickey;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.horowitz.mickey.data.Contractor;
import com.horowitz.mickey.data.DataStore;
import com.horowitz.mickey.data.Material;
import com.horowitz.mickey.data.Mission;
import com.horowitz.mickey.data.Objective;

public final class ContractorPanel extends JPanel implements PropertyChangeListener {
  private JPanel  _toolbar;
  private CCanvas _canvas;

  private String  _contractorName;

  public String getContractorName() {
    return _contractorName;
  }

  private Contractor _contractor;
  private Mission    _currentMission;
  private Mission    _missionDB;
  private JPanel     _objectivesPanel;
  private JTextField _missionNumberTF;
  private JLabel     _missionNumberLabel;

  public ContractorPanel(String contractorName) {
    super();
    _contractorName = contractorName;

    _contractor = null;
    _currentMission = null;
    _missionDB = null;

    initToolbar();
    initForm();
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame("Contractor Assistant");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    ContractorPanel cp = new ContractorPanel("George");

    frame.getContentPane().add(cp, BorderLayout.CENTER);

    frame.setSize(new Dimension(600, 350));

    frame.setLocationRelativeTo(null);

    frame.setVisible(true);

  }

  private void initToolbar() {
    setLayout(new BorderLayout());
    _toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));

    // _toolbar.add(new JLabel("Mission: "));
    _missionNumberLabel = new JLabel();
    _toolbar.add(_missionNumberLabel);

    _missionNumberTF = new JTextField(2);
    // _missionNumberTF.setPreferredSize(new Dimension(30,30));
    _missionNumberTF.setFont(_missionNumberTF.getFont().deriveFont(15f));
    // _missionNumberTF.setForeground(Color.RED);
    // _missionNumberTF.setMinimumSize(new Dimension(20, 20));
    _toolbar.add(_missionNumberTF);

    JButton requestScan = new JButton(new AbstractAction("Request scan") {

      @Override
      public void actionPerformed(ActionEvent e) {
        if (_contractor != null && !isRunning("CSTHREAD")) {
          final Settings commands = Settings.createCommands();
          commands.setProperty("contractor", _contractor.getName());
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
                  rescan();
                }
                now = System.currentTimeMillis();
              } while (!weredone && now - start < 1000 * 120); // 2 minutes
            }
          }, "CSTHREAD");
          csThread.start();
        }
      }
    });
    // _toolbar.add(requestScan);

    JButton reloadButton = new JButton(new AbstractAction("Reload") {

      @Override
      public void actionPerformed(ActionEvent e) {
        Thread t = new Thread(new Runnable() {
          public void run() {
            reload();
          }
        });
        t.start();
      }
    });

    // _toolbar.add(Box.createHorizontalStrut(4));
    _toolbar.add(reloadButton);

    JButton rescanButton = new JButton(new AbstractAction("Rescan") {

      @Override
      public void actionPerformed(ActionEvent e) {
        Thread t = new Thread(new Runnable() {
          public void run() {
            rescan();
          }
        });
        t.start();
      }
    });

    // _toolbar.add(Box.createHorizontalStrut(4));
    _toolbar.add(rescanButton);

    // _toolbar.add(Box.createGlue());

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

  private void updateImage() throws IOException {
    File f = new File("data/" + _contractor.getName() + "_objectives.bmp");
    BufferedImage image = ImageIO.read(f);
    _canvas._image = image;
    _canvas.revalidate();
  }

  public void updateView() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        if (_contractor != null) {
          try {
            File f = new File("data/" + _contractor.getName() + "_missionNumber.bmp");
            if (f.exists()) {
              BufferedImage image = ImageIO.read(f);
              Icon icon = new ImageIcon(image);
              _missionNumberLabel.setIcon(icon);
              // _missionNumberLabel.setPreferredSize(new Dimension(100,22));
              _missionNumberLabel.repaint();

              _missionNumberTF.setText("" + _contractor.getCurrentMissionNumber());
            }
          } catch (IOException e1) {
            e1.printStackTrace();
          }

        }

        if (_currentMission != null && _missionDB != null) {
          try {
            clear();

            updateImage();

            _objectivesPanel.setLayout(new GridBagLayout());
            List<Objective> objectivesC = _currentMission.getObjectives();
            List<Objective> objectivesDB = _missionDB.getObjectives();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.insets = new Insets(10, 10, 5, 5);
            NumberFormat nf = NumberFormat.getIntegerInstance();

            for (Objective objectiveC : objectivesC) {
              Objective objectiveDBFound = null;
              for (Objective objectiveDB : objectivesDB) {
                if (objectiveDB.getMaterial().equals(objectiveC.getMaterial())) {
                  objectiveDBFound = objectiveDB;
                  break;
                }
              }

              String mat = objectiveC.getMaterial();
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
              tf1.setValue(objectiveC.getCurrentAmount());
              tf1.addPropertyChangeListener("value", ContractorPanel.this);

              FocusTextField tf2 = new FocusTextField(nf);
              tf2.setMargin(new Insets(1, 1, 1, 4));
              tf2.setColumns(7);
              tf2.setName(mat + "_neededAmount");
              tf2.setHorizontalAlignment(JTextField.RIGHT);
              tf2.setValue(objectiveDBFound.getNeededAmount());
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

            _objectivesPanel.invalidate();

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
        _objectivesPanel.repaint();
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
    String[] ss = tf.getName().split("_");

    if (ss[1].equals("currentAmount")) {
      List<Objective> objectives = _currentMission.getObjectives();
      for (Objective o : objectives) {
        if (tf.getName().startsWith(o.getMaterial())) {
          o.setCurrentAmount((Long) tf.getValue());
        }
      }
    } else {
      // rarely but sometimes DB needs to be amended
      List<Objective> objectives = _missionDB.getObjectives();
      for (Objective o : objectives) {
        if (tf.getName().startsWith(o.getMaterial())) {
          o.setNeededAmount((Long) tf.getValue());
        }
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

  public void reload() {
    try {
      DataStore ds = new DataStore();
      _contractor = ds.getContractor(_contractorName);
      int mni = _contractor.getCurrentMissionNumber();
      String mn = _missionNumberTF.getText();
      if (mn != null && mn.length() > 0) {
        mni = Integer.parseInt(mn);
      }
      _currentMission = ds.getCurrentMission(_contractorName, mni);
      _missionDB = ds.getMission(_contractorName, mni);

      if (_currentMission != null && _missionDB != null && _currentMission.getNumber() != _missionDB.getNumber()) {
        _currentMission = _missionDB.copy();
      }
      updateView();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void rescan() {
    try {
      DataStore ds = new DataStore();
      _contractor = ds.getContractor(_contractorName);
      if (_contractor != null) {
        String cname = _contractor.getName();
        File f = new File("data/" + cname + "_missionNumber.bmp");
        BufferedImage image = ImageIO.read(f);

        MissionScanner mscanner = new MissionScanner();
        Integer number = mscanner.scanMissionNumber(image);

        if (number == null) {
          _missionNumberTF.setForeground(Color.RED);
          // _missionNumberTF.setText("" + _contractor.getCurrentMissionNumber());
          try {
            number = Integer.parseInt(_missionNumberTF.getText());
          } catch (NumberFormatException e) {
          }
        } else {
          _missionNumberTF.setForeground(Color.BLACK);
        }

        if (number != null) {
          _contractor.setCurrentMissionNumber(number);

          _missionDB = ds.getMission(cname, number);
          if (_missionDB != null) {
            _currentMission = _missionDB.copy();
            updateImage();
            mscanner.scanCurrentMissionDirect(_canvas._image, _currentMission);
          }
          MaterialsScanner matscanner = new MaterialsScanner();
          // mscanner.scanMaterials(materialsImage, materials)
          f = new File("data/" + cname + "_materials.bmp");
          image = ImageIO.read(f);
          Material[] materials = matscanner.scanMaterials(image, Locations.MATERIALS_1);
          _contractor.setMaterials(materials);

          f = new File("data/" + cname + "_materials2.bmp");
          if (f.exists()) {
            image = ImageIO.read(f);
            Material[] materials2 = matscanner.scanMaterials(image, Locations.MATERIALS_2);
            List<Material> old = new ArrayList<>();
            for (Material material : materials2) {
              old.add(material);
            }
            _contractor.setMaterials(old.toArray(new Material[0]));
          }

          save();

          updateView();

        } else {
          // failed to scan mission number
          _missionDB = null;
          _currentMission = null;
          updateView();
        }
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  private void save() {
    DataStore dataStore = new DataStore();
    try {
      if (_contractor != null)
        dataStore.saveContractor(_contractor);
      if (_currentMission != null && _missionDB != null) {
        dataStore.writeCurrentMission(_currentMission);
        dataStore.writeMission(_missionDB);
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public Contractor getContractor() {
    return _contractor;
  }

  public Mission getCurrentMission() {
    return _currentMission;
  }

  public Mission getMissionDB() {
    return _missionDB;
  }

}
