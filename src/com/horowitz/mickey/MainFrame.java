/**
 * 
 */
package com.horowitz.mickey;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Insets;
import java.awt.KeyEventDispatcher;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.horowitz.mickey.common.MyImageIO;
import com.horowitz.mickey.data.Contractor;
import com.horowitz.mickey.data.DataStore;
import com.horowitz.mickey.data.Material;
import com.horowitz.mickey.service.Service;
import com.horowitz.mickey.trainScanner.TrainManagementWindow;
import com.horowitz.mickey.trainScanner.TrainScanner;

/**
 * @author zhristov
 * 
 */
public final class MainFrame extends JFrame {

  private final static Logger LOGGER              = Logger.getLogger(MainFrame.class.getName());

  private static final String APP_TITLE           = "v0.934";

  private boolean             _devMode            = false;

  private ScreenScanner       _scanner;
  private MouseRobot          _mouse;
  private boolean             _stopThread         = false;
  private Statistics          _stats;
  private JLabel              _trainsNumberLabel;
  private JLabel              _trainsNumberLabelA;
  private JLabel              _freightTrainsNumberLabel;
  private JLabel              _freightTrainsNumberLabelA;
  private JLabel              _expressTrainsNumberLabel;
  private JLabel              _expressTrainsNumberLabelA;
  private JLabel              _refreshNumberLabel;
  private JLabel              _refreshMNumberLabel;
  private JLabel              _refreshTNumberLabel;
  private JLabel              _refreshSNumberLabel;
  private JLabel              _refreshNumberLabelA;
  private JLabel              _lastActivityLabel;
  private JLabel              _startedLabel;

  private JButton             _locateAction;
  private JButton             _resetAction;
  private JButton             _doMagicAction;

  private Location            _freeTime           = Locations.LOC_6MIN;
  private Location            _xpTime             = Locations.LOC_6MIN;
  private Location            _freightTime        = Locations.LOC_10MIN;
  private Location            _expressTime        = Locations.LOC_30MIN;

  private Long                _lastTime           = 0l;
  private Queue<Integer>      _lastDiffs          = new ArrayBlockingQueue<Integer>(3);

  private JToggleButton       _oneClick;

  private Settings            _settings;
  private Settings            _commands;

  private JToggleButton       _refreshClick;

  private JToggleButton       _pingClick;
  private long                _lastPingTime;

  private JToggleButton       _resumeClick;

  private JToolBar            _frToolbar1;
  private JToolBar            _freeToolbar1;
  private JToolBar            _freeToolbar2;

  private JToolBar            _frToolbar2;

  private JToolBar            _exToolbar1;
  private JToolBar            _xpToolbar1;

  private JToolBar            _exToolbar2;

  private List<String>        _captureContractors = new ArrayList<String>();
  private boolean             _captureHome        = false;
  private boolean             _scanMaterials2     = false;
  private boolean             _homeClicked        = false;

  private List<BufferedImage> _lastImageList      = new ArrayList<>();

  private JToggleButton       _sendInternational;

  private boolean isOneClick() {
    return _oneClick.isSelected();
  }

  public MainFrame(Boolean refresh, Boolean ping) throws HeadlessException {
    super();
    _settings = Settings.createDefaultSettings();
    _commands = Settings.createCommands();
    _stats = new Statistics();

    setupLogger();

    init();

    // KEYS TURNED OFF. NO NEED AT THIS POINT
    // KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new MyKeyEventDispatcher());

    runSettingsListener();
  }

  private void setupLogger() {
    try {
      try {
        MyLogger.setup();
      } catch (IOException e) {
        e.printStackTrace();
        throw new RuntimeException("Problems with creating the log files");
      }

      _scanner = new ScreenScanner(_settings);
      _mouse = new MouseRobot();
    } catch (AWTException e2) {
      LOGGER.log(Level.SEVERE, e2.getMessage());
      System.exit(ERROR);
    }
  }

  private final class MyKeyEventDispatcher implements KeyEventDispatcher {

    public boolean dispatchKeyEvent(KeyEvent e) {
      if (!e.isConsumed()) {

        if (e.getKeyCode() == 90 || e.getKeyCode() == 16) {// Z or Shift
          massClick(1, true);
        }
        if (e.getKeyCode() == 88) {// X
          massClick(1, (int) (_scanner.getXOffset() * 1.6), true);
        }
        if (e.getKeyCode() == 67) {// C
          massClick(1, (int) (_scanner.getXOffset() * 3), true);
        }

        if (e.getKeyCode() == 65 || e.getKeyCode() == 18) {// A or Alt
          massClick(2, true);
        }
        if (e.getKeyCode() == 83) {// S
          massClick(2, (int) (_scanner.getXOffset() * 1.6), true);
        }
        if (e.getKeyCode() == 68) {// D
          massClick(2, (int) (_scanner.getXOffset() * 3), true);
        }

        if (e.getKeyCode() == 81 || e.getKeyCode() == 32) {// Q or Space
          massClick(4, true);
        }
        if (e.getKeyCode() == 87) {// W
          massClick(4, (int) (_scanner.getXOffset() * 1.6), true);
        }
        if (e.getKeyCode() == 69) {// E
          massClick(4, (int) (_scanner.getXOffset() * 3), true);
        }

        if (e.getKeyCode() == 77) {// M for MAILS
          massClick(1, (int) (_scanner.getXOffset() / 2), true);
        }

        LOGGER.fine("key pressed: " + e.getExtendedKeyCode() + " >>> " + e.getKeyCode());
        e.consume();
      }
      return false;
    }
  }

  class MyCanvas extends JPanel {

    public MyCanvas() {
      setMaximumSize(new Dimension(1000, 150));
    }

    Image image = null;

    public void paint(Graphics g) {
      super.paint(g);
      if (image != null) {
        g.drawImage(image, 0, 0, null);
      }
    }

  }

  private void init() {
    setTitle(APP_TITLE);

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setAlwaysOnTop(true);
    JPanel rootPanel = new JPanel(new BorderLayout());
    getContentPane().add(rootPanel, BorderLayout.CENTER);

    final JTextArea outputConsole = new JTextArea(8, 14);

    rootPanel.add(new JScrollPane(outputConsole), BorderLayout.CENTER);

    Handler handler = new Handler() {

      @Override
      public void publish(LogRecord record) {
        String text = outputConsole.getText();
        if (text.length() > 3000) {
          outputConsole.setText("");
        }
        outputConsole.append(record.getMessage());
        outputConsole.append("\n");
        outputConsole.setCaretPosition(outputConsole.getDocument().getLength());
        // outputConsole.repaint();
      }

      @Override
      public void flush() {
        outputConsole.repaint();
      }

      @Override
      public void close() throws SecurityException {
        // do nothing

      }
    };
    LOGGER.addHandler(handler);
    _scanner.addHandler(handler);

    _refreshClick = new JToggleButton("Auto refresh");
    _refreshClick.setSelected(Boolean.parseBoolean(_settings.getProperty("refresh", "false")));

    JToolBar mainToolbar1 = new JToolBar();
    JToolBar mainToolbar2 = new JToolBar();

    _frToolbar1 = new JToolBar();
    _freeToolbar1 = new JToolBar();
    _freeToolbar2 = new JToolBar();
    _frToolbar2 = new JToolBar();
    JLabel frLabel = new JLabel("Freight     ");
    _frToolbar1.add(frLabel);
    _frToolbar2.add(new JLabel("                  "));
    JLabel freeLabel = new JLabel("Free          ");
    _freeToolbar1.add(freeLabel);
    _freeToolbar2.add(new JLabel("                  "));
    _exToolbar1 = new JToolBar();
    _xpToolbar1 = new JToolBar();
    _exToolbar2 = new JToolBar();
    JLabel exLabel = new JLabel("Express  ");
    _exToolbar1.add(exLabel);
    JLabel exLabel2 = new JLabel("         ");
    _exToolbar2.add(exLabel2);

    JLabel xpLabel = new JLabel("XP  ");
    _xpToolbar1.add(xpLabel);

    Dimension d = new Dimension(55, 20);
    frLabel.setPreferredSize(d);
    freeLabel.setPreferredSize(d);
    exLabel.setPreferredSize(d);
    exLabel2.setPreferredSize(d);
    frLabel.setMinimumSize(d);
    freeLabel.setMinimumSize(d);
    exLabel.setMinimumSize(d);
    exLabel2.setMinimumSize(d);
    frLabel.setMaximumSize(d);
    freeLabel.setMaximumSize(d);
    exLabel.setMaximumSize(d);
    exLabel2.setMaximumSize(d);
    xpLabel.setMaximumSize(d);

    JPanel toolbars = new JPanel(new GridLayout(9, 1));
    toolbars.add(mainToolbar1);
    toolbars.add(mainToolbar2);
    mainToolbar1.setFloatable(false);
    mainToolbar2.setFloatable(false);
    _frToolbar1.setFloatable(false);
    _freeToolbar1.setFloatable(false);
    _freeToolbar2.setFloatable(false);
    _frToolbar2.setFloatable(false);
    _exToolbar1.setFloatable(false);
    _exToolbar2.setFloatable(false);
    _xpToolbar1.setFloatable(false);
    _frToolbar1.setBackground(new Color(201, 177, 133));
    _freeToolbar1.setBackground(new Color(201, 177, 183));// TODO
    _freeToolbar2.setBackground(new Color(201, 177, 183));// TODO
    _frToolbar2.setBackground(new Color(201, 177, 133));
    _exToolbar1.setBackground(new Color(153, 173, 209));
    _exToolbar2.setBackground(new Color(153, 173, 209));
    _xpToolbar1.setBackground(new Color(233, 193, 189));
    toolbars.add(_frToolbar1);
    toolbars.add(_frToolbar2);
    toolbars.add(_freeToolbar1);
    toolbars.add(_freeToolbar2);
    toolbars.add(_exToolbar1);
    toolbars.add(_exToolbar2);
    toolbars.add(_xpToolbar1);
    Box north = Box.createVerticalBox();
    north.add(toolbars);

    JLabel trainsNumberLabel = new JLabel("T:");
    trainsNumberLabel.setForeground(Color.GRAY);
    _trainsNumberLabel = new JLabel("888");
    _trainsNumberLabelA = new JLabel("888");
    _trainsNumberLabelA.setForeground(Color.GRAY);
    trainsNumberLabel.setFont(trainsNumberLabel.getFont().deriveFont(14.0f));
    _trainsNumberLabel.setFont(_trainsNumberLabel.getFont().deriveFont(14.0f));
    _trainsNumberLabelA.setFont(_trainsNumberLabelA.getFont().deriveFont(10.0f));

    JLabel freightTrainsNumberLabel = new JLabel("F:");
    freightTrainsNumberLabel.setForeground(Color.GRAY);
    _freightTrainsNumberLabel = new JLabel("888");
    _freightTrainsNumberLabelA = new JLabel("888");
    _freightTrainsNumberLabelA.setForeground(Color.GRAY);
    freightTrainsNumberLabel.setFont(freightTrainsNumberLabel.getFont().deriveFont(14.0f));
    _freightTrainsNumberLabel.setFont(_freightTrainsNumberLabel.getFont().deriveFont(14.0f));
    _freightTrainsNumberLabelA.setFont(_freightTrainsNumberLabelA.getFont().deriveFont(10.0f));

    JLabel expressTrainsNumberLabel = new JLabel("E:");
    expressTrainsNumberLabel.setForeground(Color.GRAY);
    _expressTrainsNumberLabel = new JLabel("888");
    _expressTrainsNumberLabelA = new JLabel("888");
    _expressTrainsNumberLabelA.setForeground(Color.GRAY);
    expressTrainsNumberLabel.setFont(expressTrainsNumberLabel.getFont().deriveFont(14.0f));
    _expressTrainsNumberLabel.setFont(_expressTrainsNumberLabel.getFont().deriveFont(14.0f));
    _expressTrainsNumberLabelA.setFont(_expressTrainsNumberLabelA.getFont().deriveFont(10.0f));

    JLabel refreshNumberLabel = new JLabel("R:");
    refreshNumberLabel.setForeground(Color.GRAY);
    refreshNumberLabel.setFont(refreshNumberLabel.getFont().deriveFont(14.0f));
    _refreshNumberLabel = new JLabel("" + (_refreshClick.isSelected() ? "88" : "off"));
    _refreshMNumberLabel = new JLabel("" + (_refreshClick.isSelected() ? "88" : "off"));
    _refreshTNumberLabel = new JLabel("" + (_refreshClick.isSelected() ? "88" : "off"));
    _refreshSNumberLabel = new JLabel("" + (_refreshClick.isSelected() ? "88" : "off"));
    _refreshNumberLabelA = new JLabel("88");
    _refreshNumberLabelA.setForeground(Color.GRAY);
    _refreshNumberLabel.setFont(_refreshNumberLabel.getFont().deriveFont(14.0f));
    _refreshMNumberLabel.setFont(_refreshMNumberLabel.getFont().deriveFont(14.0f));
    _refreshTNumberLabel.setFont(_refreshTNumberLabel.getFont().deriveFont(14.0f));
    _refreshSNumberLabel.setFont(_refreshSNumberLabel.getFont().deriveFont(14.0f));
    _refreshNumberLabelA.setFont(_refreshNumberLabelA.getFont().deriveFont(10.0f));

    JLabel lastActivityLabel = new JLabel("L:");
    lastActivityLabel.setForeground(Color.GRAY);
    lastActivityLabel.setFont(lastActivityLabel.getFont().deriveFont(14.0f));
    _lastActivityLabel = new JLabel(" 88:88 ");
    _lastActivityLabel.setFont(_lastActivityLabel.getFont().deriveFont(14.0f));

    JLabel startedLabel = new JLabel("S:");
    startedLabel.setForeground(Color.GRAY);
    startedLabel.setFont(startedLabel.getFont().deriveFont(14.0f));
    _startedLabel = new JLabel(" 88:88 ");
    _startedLabel.setFont(_startedLabel.getFont().deriveFont(14.0f));

    JPanel labelsBox = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.ipadx = 5;
    gbc.ipady = 5;
    gbc.insets = new Insets(0, 4, 0, 0);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.LAST_LINE_START;
    gbc.weightx = 0.0;
    labelsBox.add(trainsNumberLabel, gbc);

    gbc.insets = new Insets(0, 0, 0, 0);
    gbc.gridx++;
    labelsBox.add(_trainsNumberLabel, gbc);

    gbc.insets = new Insets(0, 3, 0, 0);
    gbc.gridx++;
    labelsBox.add(_trainsNumberLabelA, gbc);

    gbc.insets = new Insets(0, 7, 0, 0);
    gbc.gridx++;
    labelsBox.add(freightTrainsNumberLabel, gbc);

    gbc.insets = new Insets(0, 0, 0, 0);
    gbc.gridx++;
    labelsBox.add(_freightTrainsNumberLabel, gbc);

    gbc.insets = new Insets(0, 3, 0, 0);
    gbc.gridx++;
    labelsBox.add(_freightTrainsNumberLabelA, gbc);

    gbc.insets = new Insets(0, 7, 0, 0);
    gbc.gridx++;
    labelsBox.add(expressTrainsNumberLabel, gbc);

    gbc.insets = new Insets(0, 0, 0, 0);
    gbc.gridx++;
    labelsBox.add(_expressTrainsNumberLabel, gbc);

    gbc.insets = new Insets(0, 3, 0, 0);
    gbc.gridx++;
    labelsBox.add(_expressTrainsNumberLabelA, gbc);

    gbc.insets = new Insets(0, 0, 0, 0);
    gbc.gridx++;
    gbc.weightx = 1.0;
    labelsBox.add(new JLabel(" "), gbc);

    // second row
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.insets = new Insets(0, 4, 0, 0);
    gbc.weightx = 0.0;
    labelsBox.add(refreshNumberLabel, gbc);

    gbc.insets = new Insets(0, 0, 0, 0);
    gbc.gridx++;
    labelsBox.add(_refreshNumberLabel, gbc);

    gbc.insets = new Insets(0, 3, 0, 0);
    gbc.gridx++;
    labelsBox.add(_refreshNumberLabelA, gbc);

    gbc.insets = new Insets(0, 7, 0, 0);
    gbc.gridx++;
    labelsBox.add(lastActivityLabel, gbc);

    gbc.insets = new Insets(0, 0, 0, 0);
    gbc.gridx++;
    gbc.gridwidth = 2;
    labelsBox.add(_lastActivityLabel, gbc);

    gbc.insets = new Insets(0, 7, 0, 0);
    gbc.gridx += 2;
    gbc.gridwidth = 1;
    labelsBox.add(startedLabel, gbc);

    gbc.insets = new Insets(0, 0, 0, 0);
    gbc.gridx++;
    gbc.gridwidth = 2;
    labelsBox.add(_startedLabel, gbc);

    // //////////////////////////

    gbc.gridy++;
    gbc.gridx = 0;
    gbc.insets = new Insets(0, 4, 0, 0);
    gbc.weightx = 0.0;
    labelsBox.add(new JLabel("M:"), gbc);

    gbc.insets = new Insets(0, 0, 0, 0);
    gbc.gridx++;
    labelsBox.add(_refreshMNumberLabel, gbc);

    gbc.insets = new Insets(0, 3, 0, 0);
    gbc.gridx++;
    labelsBox.add(new JLabel(" "), gbc);

    gbc.insets = new Insets(0, 7, 0, 0);
    gbc.gridx++;
    labelsBox.add(new JLabel("T:"), gbc);

    gbc.insets = new Insets(0, 0, 0, 0);
    gbc.gridx++;
    gbc.gridwidth = 2;
    labelsBox.add(_refreshTNumberLabel, gbc);

    gbc.insets = new Insets(0, 7, 0, 0);
    gbc.gridx += 2;
    gbc.gridwidth = 1;
    labelsBox.add(new JLabel("S:"), gbc);

    gbc.insets = new Insets(0, 0, 0, 0);
    gbc.gridx++;
    gbc.gridwidth = 2;
    labelsBox.add(_refreshSNumberLabel, gbc);

    // //////////////////////////

    north.add(labelsBox);
    rootPanel.add(north, BorderLayout.NORTH);

    // SCAN
    AbstractAction scanAction = new AbstractAction("Scan") {
      public void actionPerformed(ActionEvent e) {
        Thread myThread = new Thread(new Runnable() {
          @Override
          public void run() {
            scan();
          }
        });

        myThread.start();
      }
    };
    mainToolbar1.add(scanAction);

    // DO MAGIC
    {
      _doMagicAction = new JButton(new AbstractAction("DO MAGIC") {
        public void actionPerformed(ActionEvent e) {
          runMagic();
        }
      });
      mainToolbar1.add(_doMagicAction);
    }

    // REFRESH
    AbstractAction runAction = new AbstractAction("Refresh") {
      public void actionPerformed(ActionEvent e) {
        Thread myThread = new Thread(new Runnable() {
          @Override
          public void run() {
            try {
              refresh(false);
              runMagic();
            } catch (RobotInterruptedException e) {
              LOGGER.log(Level.SEVERE, "Interrupted by user6", e);
            }
          }
        });
        myThread.start();
      }
    };
    mainToolbar1.add(runAction);

    // LOCATE
    {
      _locateAction = new JButton(new AbstractAction("L") {
        public void actionPerformed(ActionEvent e) {
          Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
              try {
                try {
                  locate();
                } catch (RobotInterruptedException e) {
                  LOGGER.log(Level.SEVERE, "Interrupted by user7", e);
                  e.printStackTrace();
                  _stopThread = true;
                }
              } catch (Exception e1) {
                LOGGER.log(Level.WARNING, e1.getMessage());
                e1.printStackTrace();
              }
            }

          });
          myThread.start();
        }
      });
      mainToolbar1.add(_locateAction);
    }

    // Train Sender
    {
      JButton b1 = new JButton(new AbstractAction("TS") {
        public void actionPerformed(ActionEvent e) {
          Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
              scanTrains();
            }
          });
          myThread.start();
        }
      });
      mainToolbar1.add(b1);
    }
    // CAPTURE CONTRACTS 1
    {
      JButton b1 = new JButton(new AbstractAction("C1") {
        public void actionPerformed(ActionEvent e) {
          Thread myThread = new Thread(new Runnable() {

            @Override
            public void run() {
              try {
                resetContractors();
                _scanMaterials2 = true;
                if (!isRunning("MAGIC")) {
                  runMagic();
                }
              } catch (Exception e1) {
                LOGGER.log(Level.WARNING, e1.getMessage());
                e1.printStackTrace();
              }
            }

          });
          myThread.start();
        }
      });
      mainToolbar1.add(b1);
    }

    // // CAPTURE CONTRACTS 2
    // {
    // JButton b1 = new JButton(new AbstractAction("C2") {
    // public void actionPerformed(ActionEvent e) {
    // Thread myThread = new Thread(new Runnable() {
    //
    // @Override
    // public void run() {
    // try {
    // resetContractors();
    // _scanMaterials2 = false;
    // if (!isRunning("MAGIC")) {
    // runMagic();
    // }
    // } catch (Exception e1) {
    // LOGGER.log(Level.WARNING, e1.getMessage());
    // e1.printStackTrace();
    // }
    // }
    //
    // });
    // myThread.start();
    // }
    // });
    // mainToolbar1.add(b1);
    // }
    // RESET
    {
      _resetAction = new JButton(new AbstractAction("Reset") {
        public void actionPerformed(ActionEvent e) {
          Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
              _stats.reset();
              updateLabels();
            }

          });
          myThread.start();
        }
      });
      mainToolbar2.add(_resetAction);
    }
    // OC
    {
      _oneClick = new JToggleButton("OC");
      _oneClick.setSelected(true);
      // mainToolbar2.add(_oneClick);
    }

    // Refresh
    {
      mainToolbar2.add(_refreshClick);
    }

    // Ping
    {
      _pingClick = new JToggleButton("Ping");
      _pingClick.setSelected(Boolean.parseBoolean(_settings.getProperty("ping", "true")));
      mainToolbar2.add(_pingClick);
      _pingClick.addChangeListener(new ChangeListener() {

        @Override
        public void stateChanged(ChangeEvent e) {
          _commands.setProperty("ping", "" + _pingClick.isSelected());
          _commands.saveSettingsSorted();
        }
      });
    }
    {
      _sendInternational = new JToggleButton("SI");
      _sendInternational.setSelected(true);
      mainToolbar2.add(_sendInternational);

      _sendInternational.addChangeListener(new ChangeListener() {

        @Override
        public void stateChanged(ChangeEvent e) {
          _commands.setProperty("sendInternational", "" + _sendInternational.isSelected());
          _commands.saveSettingsSorted();
        }
      });
    }
    // Resume
    {
      _resumeClick = new JToggleButton("Resume");
      _resumeClick.setSelected(Boolean.parseBoolean(_settings.getProperty("resume", "false")));
      _resumeClick.addChangeListener(new ChangeListener() {

        @Override
        public void stateChanged(ChangeEvent e) {
          _commands.setProperty("resume", "" + _resumeClick.isSelected());
          _commands.saveSettingsSorted();
        }
      });
      mainToolbar2.add(_resumeClick);
    }

    //freight
    ButtonGroup bgFr = new ButtonGroup();
    createButtons(_frToolbar1, bgFr, Locations.LOC_PAGE_F1, "freight");
    createButtons(_frToolbar2, bgFr, Locations.LOC_PAGE_F2, "freight");
    
    //free
    ButtonGroup bgFree = new ButtonGroup();
    createButtons(_freeToolbar1, bgFree, Locations.LOC_PAGE_F1, "free");
    createButtons(_freeToolbar2, bgFree, Locations.LOC_PAGE_F2, "free");

    //express
    ButtonGroup bgEx = new ButtonGroup();
    createButtons(_exToolbar1, bgEx, Locations.LOC_PAGE_E1, "express");
    createButtons(_exToolbar2, bgEx, Locations.LOC_PAGE_E2, "express");
    // createButtons(_exToolbar2, bgEx, Locations.LOC_PAGE3, false);
    
    //XP
    ButtonGroup bgXP = new ButtonGroup();
    createButtons(_xpToolbar1, bgXP, Locations.LOC_PAGE_F1, "xp");

    ((JToggleButton) _frToolbar1.getComponent(3)).setSelected(true);
    ((JToggleButton) _freeToolbar1.getComponent(2)).setSelected(true);
    ((JToggleButton) _exToolbar1.getComponent(4)).setSelected(true);
    ((JToggleButton) _xpToolbar1.getComponent(2)).setSelected(true);

    /*
     * JToggleButton timeButton1 = new JToggleButton(new AbstractAction(" 6m ") {
     * 
     * public void actionPerformed(ActionEvent e) { _freightTime = Locations.LOC_6MIN; _expressTime = Locations.LOC_6MIN; } }); bg.add(timeButton1);
     * toolbar2.add(timeButton1);
     * 
     * JToggleButton timeButton2 = new JToggleButton(new AbstractAction(" 10m ") {
     * 
     * public void actionPerformed(ActionEvent e) { _freightTime = Locations.LOC_10MIN; _expressTime = Locations.LOC_10MIN;
     * 
     * } }); bg.add(timeButton2); toolbar2.add(timeButton2);
     * 
     * JToggleButton timeButton3 = new JToggleButton(new AbstractAction(" 30m ") {
     * 
     * public void actionPerformed(ActionEvent e) { _freightTime = Locations.LOC_30MIN; _expressTime = Locations.LOC_30MIN;
     * 
     * } }); bg.add(timeButton3); toolbar2.add(timeButton3);
     * 
     * JToggleButton timeButton4 = new JToggleButton(new AbstractAction(" 1h ") {
     * 
     * public void actionPerformed(ActionEvent e) { _freightTime = Locations.LOC_1HOUR; _expressTime = Locations.LOC_1HOUR;
     * 
     * } }); bg.add(timeButton4); toolbar2.add(timeButton4);
     * 
     * JToggleButton timeButton5 = new JToggleButton(new AbstractAction(" 2h ") {
     * 
     * public void actionPerformed(ActionEvent e) { _freightTime = Locations.LOC_2HOURS; _expressTime = Locations.LOC_2HOURS; } });
     * bg.add(timeButton5); toolbar2.add(timeButton5);
     * 
     * { JToggleButton timeButton6 = new JToggleButton(new AbstractAction(" 3h ") {
     * 
     * public void actionPerformed(ActionEvent e) { _freightTime = Locations.LOC_3HOURS; _expressTime = Locations.LOC_3HOURS; } });
     * bg.add(timeButton6); toolbar2.add(timeButton6); } { JToggleButton timeButton6 = new JToggleButton(new AbstractAction(" 4h ") {
     * 
     * public void actionPerformed(ActionEvent e) { _freightTime = Locations.LOC_4HOURS; _expressTime = Locations.LOC_4HOURS; } });
     * bg.add(timeButton6); toolbar3.add(timeButton6); } { JToggleButton timeButton6 = new JToggleButton(new AbstractAction(" 6h ") {
     * 
     * public void actionPerformed(ActionEvent e) { _freightTime = Locations.LOC_6HOURS; _expressTime = Locations.LOC_6HOURS; } });
     * bg.add(timeButton6); toolbar3.add(timeButton6); } { JToggleButton timeButton6 = new JToggleButton(new AbstractAction(" 8h ") {
     * 
     * public void actionPerformed(ActionEvent e) { _freightTime = Locations.LOC_8HOURS; _expressTime = Locations.LOC_8HOURS; } });
     * bg.add(timeButton6); toolbar3.add(timeButton6); } { JToggleButton timeButton6 = new JToggleButton(new AbstractAction(" 10h ") {
     * 
     * public void actionPerformed(ActionEvent e) { _freightTime = Locations.LOC_10HOURS; _expressTime = Locations.LOC_10HOURS; } });
     * bg.add(timeButton6); toolbar3.add(timeButton6); } { JToggleButton timeButton6 = new JToggleButton(new AbstractAction(" 1d ") {
     * 
     * public void actionPerformed(ActionEvent e) { _freightTime = Locations.LOC_1DAY; _expressTime = Locations.LOC_1DAY; } }); bg.add(timeButton6);
     * toolbar3.add(timeButton6); }
     */
    // timeButton2.setSelected(true);

    LOGGER.info("mandatory refresh time set to " + _settings.getInt("mandatoryRefresh.time") + " minutes.");
    LOGGER.info("ping time set to " + _settings.getInt("ping.time") + " minutes.");

  }

  private void runSettingsListener() {
    Thread settingsThread = new Thread(new Runnable() {
      public void run() {
        new Service().purgeAll();
        boolean stop = false;
        do {
          _settings.loadSettings();
          _commands.loadSettings();
          if ("close".equals(_commands.getProperty("command"))) {
            LOGGER.info("stop everything");
            stop = true;
          } else {
            // if (!isRunning("MAGIC") && _scanner.isOptimized()) {
            // try {
            // checkIsStuck();
            // } catch (GameStuckException e1) {
            // try {
            // refresh();
            // runMagic();
            // } catch (RobotInterruptedException e) {
            // }
            // }
            // }
            try {
              reapplySettings();
              //processCommands();
              processRequests();
            } catch (Throwable t) {
              //hmm
            }
            // if (System.currentTimeMillis() - _lastTime > 60 * 1000) {
            // final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            // _scanner.writeImage(new Rectangle(0, 0, screenSize.width, screenSize.height),
            // "screenshot_" + DateUtils.formatDateForFile2(System.currentTimeMillis()) + ".png");
            // }

            try {
              Thread.sleep(20000);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
            deleteOlder("screenshot", 5);

          }
        } while (!stop);
      }
    }, "SETTINGS");

    settingsThread.start();

  }

  private void reapplySettings() {
    LOGGER.info(".................");
    int free = _commands.getInt("free", -1);
    int freight = _commands.getInt("freight", -1);
    int express = _commands.getInt("express", -1);
    int xp = _commands.getInt("xp", -1);

    if (free >= 0) {
      reapplyTimes(free, _freeToolbar1.getComponents(), _freeToolbar2.getComponents());
    }

    if (freight >= 0) {
      reapplyTimes(freight, _frToolbar1.getComponents(), _frToolbar2.getComponents());
    }

    if (express >= 0) {
      reapplyTimes(express, _exToolbar1.getComponents(), _exToolbar2.getComponents());
    }

    if (xp >= 0) {
      reapplyTimes(xp, _xpToolbar1.getComponents(), null);
    }

    boolean ping = "true".equalsIgnoreCase(_commands.getProperty("ping"));
    boolean resume = "true".equalsIgnoreCase(_commands.getProperty("resume"));
    boolean sendInternational = "true".equalsIgnoreCase(_commands.getProperty("sendInternational"));

    if (ping != _pingClick.isSelected()) {
      _pingClick.setSelected(ping);
    }

    if (resume != _resumeClick.isSelected()) {
      _resumeClick.setSelected(resume);
    }

    if (sendInternational != _sendInternational.isSelected()) {
      _sendInternational.setSelected(sendInternational);
    }
  }

  private void reapplyTimes(int time, Component[] components1, Component[] components2) {
    boolean found = false;
    for (int i = 0; i < components1.length && !found; i++) {
      if (components1[i] instanceof LocationToggleButton) {
        LocationToggleButton b = (LocationToggleButton) components1[i];
        if (b.getTrainLocation().getTime() == time) {
          if (!b.isSelected()) {
            b.doClick();
            b.invalidate();
          }
          found = true;
        }
      }
    }
    if (!found && components2 != null) {
      for (int i = 0; i < components2.length && !found; i++) {
        if (components2[i] instanceof LocationToggleButton) {
          LocationToggleButton b = (LocationToggleButton) components2[i];
          if (b.getTrainLocation().getTime() == time) {
            if (!b.isSelected()) {
              b.doClick();
              b.invalidate();
            }
            found = true;
          }
        }
      }
    }
  }

  private void processRequests() {
    Service service = new Service();

    String[] requests = service.getActiveRequests();
    for (String r : requests) {
      
      if (r.startsWith("capture")) {
        service.inProgress(r);
        String[] ss = r.split("_");
        if (ss.length > 2) {
          // _captureContractors.clear();
          _captureContractors.add(ss[1]);
          hook(r);
        } else {
          if (r.startsWith("captureAll")) {
            _scanMaterials2 = true;
            resetContractors();
            hook(r);
          } else if (r.startsWith("captureHome")) {
            _captureHome = true;
            hookHome(r);
          }

        }
      } else if (r.startsWith("click")) {
        service.inProgress(r);
        processClick(r);
      } else if (r.startsWith("refresh")||r.startsWith("r")) {
        service.inProgress(r);
        String[] ss = r.split("_");
        Boolean bookmark = ss.length > 1 ? Boolean.parseBoolean(ss[1]) : false;
        try {
          stopMagic();
          refresh(bookmark);
          runMagic();
        } catch (RobotInterruptedException e) {
        }
      } else if (r.startsWith("reload")) {
        service.inProgress(r);
        reload(r);
      } else if (r.startsWith("reset")) {
        service.inProgress(r);
        _stats.reset();
        updateLabels();
      }
    }

    service.purgeOld(1000 * 60 * 60);// 1 hour old
  }

  private void reload(String r) {
    reload();
  }

  private void reload() {
    if (_trainManagementWindow == null) {
      TrainScanner tscanner = new TrainScanner(_scanner, LOGGER, _settings.getInt("IntTrains.takeABreakAfter", 3));
      _trainManagementWindow = new TrainManagementWindow(null, tscanner);
    } else
      _trainManagementWindow.reload();
  }

  private void processClick(String r) {
    try {
      String[] ss = r.split("_");
      int x = Integer.parseInt(ss[1]);
      int y = Integer.parseInt(ss[2]);
      _mouse.click(x, y);
      try {
        _mouse.delay(1000);
      } catch (RobotInterruptedException e) {
      }
    } finally {
      new Service().done(r);
    }
  }

  private void hook(final String request) {
    Thread hookThread = new Thread(new Runnable() {
      public void run() {
        int n = 0;
        boolean done = false;
        do {
          n++;
          String[] ss = request.split("_");
          String c = null;
          if (ss.length > 2) {
            c = ss[1];
            LOGGER.info("checking (" + n + ") " + c);
          } else {
            c = ss[0];
          }
          if ((ss.length > 2 && !_captureContractors.contains(c)) || (ss.length <= 2 && _captureContractors.size() == 0)) {
            new Service().done(request);
            done = true;
            LOGGER.info(c + " is DONE!!!");
          }
          try {
            Thread.sleep(4000);
          } catch (InterruptedException e) {
          }
        } while (!done && n < 5 * 20);
        if (!done) {
          new Service().purgeAll();
        }
      }
    });
    hookThread.start();
  }

  private void hookHome(final String request) {
    Thread hookThread = new Thread(new Runnable() {
      public void run() {
        int n = 0;
        boolean done = false;
        do {
          n++;
          String[] ss = request.split("_");
          String c = null;
          if (ss.length > 2) {
            c = ss[1];
            LOGGER.info("checking (" + n + ") " + c);
          } else {
            c = ss[0];
          }
          if (!_captureHome) {
            new Service().done(request);
            done = true;
            LOGGER.info(c + " is DONE!!!");
          }
          try {
            Thread.sleep(4000);
          } catch (InterruptedException e) {
          }
        } while (!done && n < 5 * 20);
        if (!done) {
          new Service().purgeAll();
        }
      }
    });
    hookThread.start();
  }

  private void processCommands() {
    String command = _commands.getProperty("command");
    if ("refresh".equals(command)) {
      // try {
      // Thread.sleep(10000);
      // } catch (InterruptedException e1) {
      // }
      _commands.setProperty("command", command + "_ok");
      _commands.saveSettingsSorted();

      stopMagic();
      try {
        refresh(false);
        runMagic();
      } catch (RobotInterruptedException e) {
        e.printStackTrace();
      }

      _commands.setProperty("command.done", "" + DateUtils.formatDateForFile2(System.currentTimeMillis()));
      _commands.saveSettingsSorted();
    } else if ("start".equals(command)) {
      _commands.setProperty("command", command + "_ok");
      _commands.saveSettingsSorted();

      stopMagic();
      runMagic();

      _commands.setProperty("command.done", "" + DateUtils.formatDateForFile2(System.currentTimeMillis()));
      _commands.saveSettingsSorted();
    } else if ("stop".equals(command)) {
      _commands.setProperty("command", command + "_ok");
      _commands.saveSettingsSorted();

      stopMagic();

      _commands.setProperty("command.done", "" + DateUtils.formatDateForFile2(System.currentTimeMillis()));
      _commands.saveSettingsSorted();
    } else if ("contracts".equals(command)) {
      _commands.setProperty("command", command + "_ok");
      // _commands.saveSettingsSorted();
      _scanMaterials2 = true;
      resetContractors();
      if (!isRunning("MAGIC")) {
        runMagic();
      }
      _commands.setProperty("command.done", "" + DateUtils.formatDateForFile2(System.currentTimeMillis()));
      _commands.saveSettingsSorted();
    } else if ("contractor".equals(command)) {
      _scanMaterials2 = "true".equals(_settings.getProperty("contractors.withMaterials"));
      _commands.setProperty("command", command + "_ok");
      String contractor = _commands.getProperty("contractor");
      rescanContractor(contractor);
      if (!isRunning("MAGIC")) {
        runMagic();
      }
      _commands.setProperty("command.done", "" + DateUtils.formatDateForFile2(System.currentTimeMillis()));
      _commands.saveSettingsSorted();
    }
  }

  private void resetContractors() {
    try {
      _captureHome = true;
      _captureContractors = new DataStore().getContractorNamesForScan();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void stopMagic() {
    _stopThread = true;
    int tries = 10;
    boolean stillRunning = true;
    for (int i = 0; i < tries && stillRunning; ++i) {
      stillRunning = isRunning("MAGIC");
      if (stillRunning) {
        LOGGER.info("Magic still working...");
        try {
          Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
      }
    }
    _stopThread = false;
  }

  private void createButtons(final JToolBar toolbar, final ButtonGroup bg, final Location[] locations, final String type) {
    for (int i = 0; i < locations.length; i++) {
      final Location l = locations[i];
      LocationToggleButton button = new LocationToggleButton(l, new AbstractAction(l.getName()) {

        public void actionPerformed(ActionEvent e) {
          if (type.equals("free")) {
            _freeTime = l;
          } else if (type.equals("freight")) {
            _freightTime = l;
            
          } else if (type.equals("xp")) {
            _xpTime = l;
          } else {
            _expressTime = l;
          }
          LOGGER.info("selected " + type + ": " + l.getName());
          _commands.setProperty(type, "" + l.getTime());
          _commands.saveSettingsSorted();
        }
      });
      bg.add(button);
      toolbar.add(button);

    }
  }

  private class LocationToggleButton extends JToggleButton {
    private Location _location;

    public LocationToggleButton(Location location, Action a) {
      super(a);
      _location = location;
    }

    public Location getTrainLocation() {
      return _location;
    }

  }

  private void updateLabels() {
    _stats.updateTime();
    _trainsNumberLabel.setText("" + _stats.getTotalTrainCount());
    _freightTrainsNumberLabel.setText("" + _stats.getFreightTrainCount());
    _expressTrainsNumberLabel.setText("" + _stats.getExpressTrainCount());
    _refreshNumberLabel.setText("" + _stats.getRefreshCount());
    _refreshMNumberLabel.setText("" + _stats.getRefreshMandatoryCount());
    _refreshTNumberLabel.setText("" + _stats.getRefreshTimeoutCount());
    _refreshSNumberLabel.setText("" + _stats.getRefreshStuckCount());
    _lastActivityLabel.setText("" + _stats.getLastActivityTimeAsString());
    _startedLabel.setText("" + _stats.getStartedTimeAsString());

    _trainsNumberLabelA.setText(_stats.getAverageTrainTimeAsString());
    _freightTrainsNumberLabelA.setText(_stats.getAverageFreightTimeAsString());
    _expressTrainsNumberLabelA.setText(_stats.getAverageExpressTimeAsString());
    _refreshNumberLabelA.setText(_stats.getAverageRefreshTimeAsString());
  }

  protected void runMagic() {
    Thread myThread = new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          if (_scanner.isOptimized()) {
            if (_sendInternational.isSelected()) {
              reload();
            }
            LOGGER.info("Let's get rolling...");
            Thread.sleep(200);
            doMagic();
          } else
            LOGGER.info("Scan first!");
        } catch (Exception e1) {
          LOGGER.severe(e1.getMessage());
          e1.printStackTrace();
        }
      }
    }, "MAGIC");

    myThread.start();
  }

  private void scan() {
    try {
      LOGGER.info("Scanning...");

      boolean found = _scanner.locateGameArea();
      if (found) {
        LOGGER.info("GAME FOUND! MICKEY READY.");
        // fixTheGame();
        setTitle(APP_TITLE + " READY");
      } else {
        LOGGER.info("CAN'T FIND THE GAME!");
      }
    } catch (Exception e1) {
      LOGGER.log(Level.WARNING, e1.getMessage());
      e1.printStackTrace();
    } catch (RobotInterruptedException e) {
      LOGGER.log(Level.SEVERE, "Interrupted by user", e);
      e.printStackTrace();
    }

  }

  private void refresh(boolean bookmark) throws RobotInterruptedException {
    _lastTime = System.currentTimeMillis();
    try {
      String dateStr = DateUtils.formatDateForFile2(System.currentTimeMillis());
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      MyImageIO.write(new Robot().createScreenCapture(new Rectangle(screenSize)), "PNG", new File("refresh " + dateStr + ".png"));
      deleteOlder("refresh", 5);
    } catch (HeadlessException e1) {
      e1.printStackTrace();
    } catch (IOException e1) {
      e1.printStackTrace();
      LOGGER.severe(e1.getMessage());
    } catch (AWTException e1) {
      e1.printStackTrace();
    }
    LOGGER.info("Time to refresh...");
    try {
      Pixel p;
      if (!bookmark) {
        if (_scanner.isOptimized()) {
          p = _scanner.getBottomRight();
          p.y = _scanner.getTopLeft().y + 100;
          p.x = _scanner.getBottomRight().x + 4;
        } else {
          p = new Pixel(0, 110);
        }
        _mouse.click(p.x, p.y);
        Robot robot = new Robot();
        robot.keyPress(KeyEvent.VK_F5);
        robot.keyRelease(KeyEvent.VK_F5);
      } else {
        try {
          p = _scanner.generateImageData("tsFavicon2.bmp", 8, 7).findImage(new Rectangle(0, 30, 400, 200));
          _mouse.click(p.x, p.y);
        } catch (IOException e) {
        }
      }

      LOGGER.fine("Wait 10 seconds...");
      _mouse.delay(10000);
      boolean done = false;
      try {
        for (int i = 0; i < 17 && !done; i++) {
          LOGGER.info("after refresh recovery try " + (i + 1));
          handleRarePopups(false);

          // OTHER POPUPS
          handlePopups();// hmm
          _mouse.delay(1000);

          // LOCATE THE GAME
          if (_scanner.locateGameArea()) {
            LOGGER.info("Game located successfully!");
            done = true;
          } else {
            processRequests();
          }
        }
      } catch (AWTException | IOException e) {
        LOGGER.info("whaaaat again?");
      } catch (InterruptedException e) {
        LOGGER.info("interrupted");
      } catch (SessionTimeOutException e) {
        LOGGER.info("Session time out. Stopping.");
      }
      if (done)
        LOGGER.info("Refresh done");
      else {
        LOGGER.info("Refresh failed");
        if (!bookmark) {
          LOGGER.info("Trying refresh through bookmark");
          refresh(true);
        }
      }
    } catch (AWTException e) {
      e.printStackTrace();
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

  private void handleRarePopups(boolean fast) throws InterruptedException, RobotInterruptedException {
    LOGGER.info("Checking for FB login and daily rewards...");
    _mouse.savePosition();
    _mouse.mouseMove(0, 0);
    int wait = 100;
    if (!fast) {
      wait = 1000;
      // FB LOGIN
      if (scanAndClick(_scanner.getLoginWIthFB(), null))
        _mouse.delay(2000);

      if (scanAndClick(_scanner.getLoginFB(), null))
        _mouse.delay(5000);
      else
        _mouse.delay(3000);
    }
    
    // INVITE
    if (scanAndClick(_scanner.getInvite(), null))
      _mouse.delay(wait);

    // DAILY
    if (scanAndClick(_scanner.getDailyRewards(), null))
      _mouse.delay(wait);

    // PROMO
    if (scanAndClick(_scanner.getPromoX(), null))
      _mouse.delay(wait);
    
    //SHOP X
    if (scanAndClick(_scanner.getShopX(), null))
      _mouse.delay(wait);
    
    _mouse.restorePosition();
  }

  private boolean scanAndClick(ImageData imageData, Rectangle area) {
    Pixel p = imageData.findImage(area);
    if (p != null) {
      _mouse.mouseMove(p);
      _mouse.click();
      LOGGER.info(imageData.getName() + " clicked.");
      return true;
    }
    return false;
  }

  private String getNow() {
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm  dd MMM");
    String date = sdf.format(Calendar.getInstance().getTime());
    return date;
  }

  public void doMagic() {
    setTitle(APP_TITLE + " RUNNING");
    _lastPingTime = System.currentTimeMillis();

    _stopThread = false;
    _homeClicked = false;
    if (_refreshClick.isSelected())
      updateLabels();

    long start = System.currentTimeMillis();
    long fstart = System.currentTimeMillis();
    NumberFormat nf = NumberFormat.getNumberInstance();
    nf.setMaximumFractionDigits(3);
    nf.setMinimumFractionDigits(0);
    int turn = 1;
    while (!_stopThread) {
      turn *= 2;
      if (turn > 8) turn = 1;
      LOGGER.info("T: " + turn);
      int timeForRefresh = (getShortestTime() * 60000 / 2);
      int mandatoryRefresh = _settings.getInt("mandatoryRefresh.time") * 60000;
      try {
        updateLabels();

        goHomeIfNeeded();

        // antistuck prevention
        checkIsStuck();

        // OTHER LOCATIONS
        boolean flag = scanOtherLocations(true, 1);

        captureContracts();

        if (_captureContractors.size() == 0) {
          if (_sendInternational.isSelected())
            sendInternational();

          //scanOtherLocations(true, 2);

          if (_pingClick.isSelected()) {
            if (ping())
              scanOtherLocations(true, 3);
          }

          // REFRESH
          if (_refreshClick.isSelected() && timeForRefresh > 3 * 60000) {
            // if "0" chosen no refresh
            long now = System.currentTimeMillis();
            LOGGER.info("time: " + DateUtils.fancyTime2(now - start));

            if (now - start >= timeForRefresh) {
              LOGGER.info("Warning: no trains for last " + DateUtils.fancyTime2(now - start));
              refresh(false);
              _stats.registerTimeOutRefresh();
              updateLabels();
              fstart = start = System.currentTimeMillis();
            }

            if (mandatoryRefresh > 0 && now - fstart >= mandatoryRefresh) {
              LOGGER.info("Mandatory refresh");
              refresh(false);
              _stats.registerMandatoryRefresh();
              updateLabels();

              fstart = start = System.currentTimeMillis();
            }

            //
            // No need since checkIsStuck works fine
            //
            // // check again has refresh gone well after 4 minutes
            // if (_lastTime != null && now - _lastTime >= 4 * 60 * 1000) {
            // handleRarePopups();
            // _lastTime = System.currentTimeMillis();
            // }
            //
          }

          // POPUPS
          handlePopups();
          goHomeIfNeeded();
          //scanOtherLocations(true, 4);

          // HOME
          boolean clickHomeFaster = "true".equalsIgnoreCase(_settings.getProperty("clickHomeFaster", "true"));
          if (clickHomeFaster) {
            flag = clickHomeFaster();
          } else {
            flag = clickHomeOneClick();
          }

          if (flag) {
            // true means train has been sent or other locations've been visited. Refresh postponed.
            start = System.currentTimeMillis();
          } else {
            //lookForPackages();
          }
          
          //LETTERS
          int h = _settings.getInt("huntLetters", 2);
          if (h > 0)
            huntLetters();
          
          //PACKAGES
          int hp = _settings.getInt("huntPackages", 1);
          int n = 16;
          for(int i = 0; i < hp; i++) {
            n = n / 2;
          }
          
          if ( turn % n == 0)
            lookForPackages2();

          _mouse.delay(200);
        } // _captureContractors == 0
      } catch (AWTException | IOException e) {
        LOGGER.severe(e.getMessage());
        e.printStackTrace();
        break;
      } catch (RobotInterruptedException e) {
        LOGGER.log(Level.SEVERE, "Interrupted by user", e);
        // LOGGER.info("Interrupted by user");
        setTitle(APP_TITLE + " READY");
        _stopThread = true;
        break;
      } catch (SessionTimeOutException e) {
        LOGGER.info("Session time out. Stopping...");
        setTitle(APP_TITLE + " READY");
        _stopThread = true;
        break;
      } catch (GameStuckException e) {
        _stopThread = true;
        new Thread(new Runnable() {
          public void run() {
            try {
              _stats.registerStuckRefresh();
              updateLabels();
              Thread.sleep(2000);
              refresh(false);
              runMagic();
            } catch (RobotInterruptedException e) {
            } catch (InterruptedException e) {
            }
          }
        }).start();
        break;
      } catch (Throwable e) {
        LOGGER.severe("SOMETHING WENT WRONG!");
        e.printStackTrace();
        LOGGER.log(Level.SEVERE, "UH OH " + e.getMessage() + " UH OH", e);
        setTitle("SOMETHING WENT WRONG!");
        // break; //please don't stop the music!
      }

    } // while
    if (_stopThread) {
      LOGGER.info("Mickey has being stopped");
    }
  }

  private void checkIsStuck() throws GameStuckException {
    int howMuch = 3;
    putNewImage(howMuch);

    if (_lastImageList.size() >= howMuch) {
      ImageComparator comparator = new SimilarityImageComparator(0.01, 1000);
      boolean uhoh = comparator.compare(_lastImageList.get(0), _lastImageList.get(1));
      for (int i = 1; i < howMuch - 1; ++i) {
        uhoh = uhoh && comparator.compare(_lastImageList.get(i), _lastImageList.get(i + 1));
      }
     
      if (uhoh) {
        try {
          handleRarePopups(true);
          _mouse.delay(200);
          howMuch = 4;
          putNewImage(howMuch);
        } catch (InterruptedException e) {
        } catch (RobotInterruptedException e) {
        }
      }
    
      //check again
      uhoh = comparator.compare(_lastImageList.get(0), _lastImageList.get(1));
      for (int i = 1; i < howMuch - 1; ++i) {
        uhoh = uhoh && comparator.compare(_lastImageList.get(i), _lastImageList.get(i + 1));
      }
        
      if (uhoh) {
        _lastImageList.clear();
        LOGGER.severe("THE GAME PROBABLY GOT STUCK");
        throw new GameStuckException();
      }
    }
  }

  private void putNewImage(int howMuch) {
    try {
      Pixel tl = _scanner.getTopLeft();
      int w = (_scanner.getGameWidth() - 214) / 2;
      // Rectangle rect = new Rectangle(tl.x + 33, tl.y + 7, 104, 15);
      Rectangle rect = new Rectangle(tl.x + w + 102, tl.y + 82, 65, 15);
      if (_lastImageList.size() < howMuch) {
        BufferedImage image = new Robot().createScreenCapture(rect);
        _lastImageList.add(image);
      } else if (_lastImageList.size() >= howMuch) {
        _lastImageList.remove(0);
        BufferedImage image = new Robot().createScreenCapture(rect);
        _lastImageList.add(image);
      }
    } catch (AWTException e) {
    }
  }

  private int                   _pingTurn = 1;

  private TrainManagementWindow _trainManagementWindow;

  private boolean ping() {
    boolean res = false;
    long now = System.currentTimeMillis();
    long time = _settings.getInt("ping.time") * 60000; // from minutes to millseconds
    String pingPrefix = "ping ";
    int xx = (_scanner.getGameWidth() - 780) / 2;
    int yy = (_scanner.getGameHeight() - 585) / 2;
    xx += _scanner.getTopLeft().x;
    yy += _scanner.getTopLeft().y;

    if (now - _lastPingTime >= time) {
      LOGGER.info("ping");
      if (_pingTurn == 2) {
        try {
          // capture trains
          pingPrefix += " trains ";
          _mouse.click(_scanner.getTopLeft().x + 56, _scanner.getTopLeft().y + 72);
          _mouse.delay(1300);
          _mouse.click(xx + 103, yy + 83);
          _mouse.delay(2300);
        } catch (RobotInterruptedException e) {
        }
      } else if (_pingTurn == 3) {
        try {
          // capture international trains
          pingPrefix += " int trains ";
          _mouse.click(_scanner.getTopLeft().x + 56, _scanner.getTopLeft().y + 72);
          _mouse.delay(1300);
          _mouse.click(xx + 209, yy + 83);
          _mouse.delay(2300);
        } catch (RobotInterruptedException e) {
        }
      }

      final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      _scanner.writeImage(new Rectangle(0, 0, screenSize.width, screenSize.height),
          pingPrefix + DateUtils.formatDateForFile2(System.currentTimeMillis()) + ".png");

      if (_pingTurn == 2 || _pingTurn == 3) {
        try {
          // need to close trains window
          _mouse.click(xx + 764, yy + 22);
          _mouse.delay(500);
          res = true;
        } catch (RobotInterruptedException e) {
        }
      }

      _pingTurn++;
      if (_pingTurn >= 4)
        _pingTurn = 1;

      deleteOlder("ping", 12);
      _lastPingTime = System.currentTimeMillis();
    }
    return res;
  }

  private void captureHome() throws RobotInterruptedException, AWTException, IOException, SessionTimeOutException {
    handlePopups();
    goHomeIfNeeded();
    // open contracts
    _mouse.click(_scanner.getBottomRight().x - 61, _scanner.getTopLeft().y + 73);
    _mouse.delay(1000);

    Pixel pm = null;
    int tries = 0;
    do {
      pm = _scanner.getMaterials().findImage();
      if (pm == null) {
        // try again after 1 second
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
      }
      tries++;
    } while (pm == null && tries < 3);

    if (pm != null) {
      pm = new Pixel(pm.x - 313, pm.y - 31);
      Rectangle rect = new Rectangle(pm.x, pm.y, 760, 550);
      _scanner.writeImage(rect, "data/Home_materials.bmp");
      _mouse.delay(300);
    }

    // click close
    _mouse.click(_scanner.getTopLeft().x + _scanner.getGameWidth() / 2, _scanner.getBottomRight().y - 75);
    _mouse.delay(600);
    _captureHome = false;
  }

  private void sendInternational() throws AWTException, IOException, RobotInterruptedException, SessionTimeOutException {
    long timeLeft = _trainManagementWindow != null ? _trainManagementWindow.getTimeLeft() - System.currentTimeMillis() : 10000000;
    LOGGER.info("INTERNATIONAL " + DateUtils.fancyTime2(timeLeft));
    if (_trainManagementWindow != null && timeLeft <= 0) {
      handlePopups();

      _trainManagementWindow.reloadNow();
      boolean atLeastOneSent = _trainManagementWindow.sendTrainsNow();// in this thread please
      if (atLeastOneSent)
        _trainManagementWindow.reschedule(_settings.getInt("IntTrains.rescheduleAgain", 30) * 1000);// 30 sec
      else
        _trainManagementWindow.reschedule(_settings.getInt("IntTrains.reschedule", 2 * 60) * 1000);
    }
  }

  private void captureContracts() throws AWTException, IOException, RobotInterruptedException, SessionTimeOutException {
    if (_captureHome) {
      captureHome();
    }

    if (_captureContractors.size() > 0) {
      String contractorName = _captureContractors.get(0);
      String fname = "data/" + contractorName;
      // String index = StringUtils.leftPad(ss[0], 2, "0");
      handlePopups();

      // open contracts
      _mouse.click(_scanner.getTopLeft().x + 234, _scanner.getBottomRight().y - 42);
      _mouse.delay(1000);

      Pixel p = _scanner.getContracts().findImage();
      Rectangle area;

      if (p != null) {
        // we're on the right track
        _commands.setProperty("contractor.scan", "running");
        _commands.saveSettingsSorted();

        Pixel scrollerTop = new Pixel(p.x + 106, p.y + 40);
        // _mouse.mouseMove(scrollerTop);
        // _mouse.click();
        // _mouse.delay(300);

        int track = 500;
        area = new Rectangle(p.x, p.y + 31, 100, 484);
        String cname = contractorName + ".bmp";
        boolean found = false;
        if (findAndClick(cname, area, 15, 7, true, false)) {
          found = true;
        } else {
          _mouse.mouseMove(scrollerTop);
          _mouse.click();
          _mouse.delay(300);
        }

        if (findAndClick(cname, area, 15, 7, true, false)) {
          found = true;
        } else {
          _mouse.mouseMove(scrollerTop);
          _mouse.click();
          _mouse.delay(300);

          // drag until get it
          int drag = 0;
          found = false;
          do {
            LOGGER.info("drag from " + drag + " to " + (drag + 75));
            _mouse.drag(scrollerTop.x, scrollerTop.y + drag, scrollerTop.x, scrollerTop.y + drag + 45);
            found = findAndClick(cname, area, 15, 7, true, false);
            drag += 50;
          } while (!found && drag < track);
        }
        if (found) {
          _mouse.delay(500);
          // _scanner.writeImage(new Rectangle(p.x - 38, p.y - 28, 780, 585), contractor + "_mission.bmp");
          _scanner.writeImage(new Rectangle(p.x - 38 + 376, p.y - 28 + 388, 71, 21), fname + "_missionNumber.bmp");
          _scanner.writeImage(new Rectangle(p.x - 38 + 456, p.y - 28 + 60 + 93, 291, 262), fname + "_objectives.bmp");

          // _scanner.captureGame("status " + index + "A " + contractor + ".bmp");
          _mouse.delay(100);
          if (true) {// _withMaterials
            // click visit
            _mouse.click(p.x + 267, p.y + 335);
            _mouse.delay(200);
            _mouse.click(p.x + 267, p.y + 335 + 154);
            _mouse.delay(2000);

            // click materials
            _mouse.click(_scanner.getTopLeft().x + 114, _scanner.getBottomRight().y - 42);
            _mouse.delay(1000);
            Pixel pm = null;
            int tries = 0;
            do {
              pm = _scanner.getMaterials().findImage();
              if (pm == null) {
                // try again after 1 second
                try {
                  Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
              }
              tries++;
            } while (pm == null && tries < 3);

            if (pm != null) {
              // _mouse.mouseMove(pm);
              // _mouse.delay(2000);
              pm = new Pixel(pm.x - 313, pm.y - 31);
              // _mouse.mouseMove(pm);
              Rectangle rect = new Rectangle(pm.x, pm.y, 760, 550);

              _scanner.writeImage(rect, fname + "_materials.bmp");
              // _scanner.captureGame("status " + index + "B " + contractor + " materials" + ".bmp");
              _mouse.delay(300);

              boolean scanMaterials2 = false;
              
              if (_scanMaterials2) {
                scanMaterials2 = new DataStore().getContractor(contractorName).isScanMaterials2();
              }
              if (scanMaterials2) {
                // click next page of materials
                _mouse.click(pm.x + 724, pm.y + 292);
                _mouse.delay(500);
                _scanner.writeImage(rect, fname + "_materials2.bmp");
                // _scanner.captureGame("status " + index + "C " + contractor + " materials" + ".bmp");
                _mouse.delay(300);

              }
            }

            // click close
            _mouse.click(_scanner.getTopLeft().x + _scanner.getGameWidth() / 2, _scanner.getBottomRight().y - 75);
            _mouse.delay(600);

            goHomeIfNeeded();
            _mouse.delay(300);
            handlePopups();
          }
        } else {
          LOGGER.info("Couldn't find " + contractorName);
        }
        _captureContractors.remove(0);
      }

      // _commands.setProperty("contractor.scan", "done");
      // _commands.saveSettingsSorted();
    } else {
      // all contractors captured
      // processContractors(_contractors);
    }
  }

  private void goHomeIfNeeded() throws AWTException, IOException, RobotInterruptedException {
    Rectangle area = new Rectangle(_scanner.getTopLeft().x, _scanner.getBottomRight().y - 50, 60, 50);
    Pixel p = _scanner.getHome().findImage(area);
    if (p != null && !_homeClicked) {
      _homeClicked = true;
      new Thread(new Runnable() {
        public void run() {
          try {
            Thread.sleep(2000);
          } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }  
          _homeClicked = false;
        }
      }).start();
      
      goHome();
      // _mouse.click(p.x, p.y);
    }
  }

  private int getShortestTime() {
    return _freightTime.getTime() < _expressTime.getTime() ? _freightTime.getTime() : _expressTime.getTime();
  }

  protected void massClick(final int railNumber, final boolean fromCursor) {
    massClick(railNumber, _scanner.getXOffset(), fromCursor);
  }

  protected void massClick(final int railNumber, final int xOff, final boolean fromCursor) {

    if (!isRunning("CLICK")) {
      Thread myThread = new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            try {
              LOGGER.info("Mass click " + railNumber);
              Point cp;
              if (fromCursor) {
                cp = _mouse.getCurrentPosition();
              } else {
                cp = new Point(_scanner.getBottomRight().x - 2, _scanner.getBottomRight().y - Locations.RAIL1 - 3);
              }
              int xleft = 0;
              if (_scanner.getTopLeft() != null) {
                xleft = _scanner.getTopLeft().x;
              }
              _mouse.saveCurrentPosition();

              int[] rails = _scanner.getRailsOut();
              for (int ix = cp.x; ix >= xleft; ix = ix - xOff) {
                for (int i = 0; i < railNumber; i++) {
                  int y = (int) (fromCursor ? cp.y - i * _scanner.getRailYOffset() : _scanner.getBottomRight().y - rails[i]
                      - _scanner.getRailYOffset());
                  if (railNumber < 4)
                    y -= (i * _scanner.getRailYOffset());
                  _mouse.click(ix, y);
                  _mouse.checkUserMovement();
                }
              }
            } catch (RobotInterruptedException e) {
              LOGGER.log(Level.FINE, "Interrupted by user2", e);
              _stopThread = true;
            }
          } catch (Exception e1) {
            LOGGER.log(Level.WARNING, e1.getMessage());
            e1.printStackTrace();
          }
        }

      }, "CLICK");
      myThread.start();
    }
  }

  private boolean findAndClick(String imageName, Rectangle area, int xOff, int yOff, boolean click) throws AWTException, IOException,
      RobotInterruptedException {
    return findAndClick(imageName, area, xOff, yOff, click, false);
  }

  private boolean findAndClick(String imageName, Rectangle area, int xOff, int yOff, boolean click, boolean capture) throws AWTException,
      IOException, RobotInterruptedException {
    Pixel p = _scanner.locateImageCoords(imageName, new Rectangle[] { area }, xOff, yOff);
    if (p != null) {
      LOGGER.fine("Found pointer " + p);
      LOGGER.fine("Found pointer " + imageName);
      _mouse.mouseMove(p);
      _mouse.delay(100);
      if (capture) {
        _scanner.captureGame();
        deleteOlder("popup", 5);
      }
      if (click) {
        _mouse.click();
        _mouse.delay(100);
      }
      return true;
    }
    return false;
  }

  private void goHome() throws AWTException, IOException, RobotInterruptedException {
    _mouse.saveCurrentPosition();
    _mouse.click(_scanner.getTopLeft().x + 26, _scanner.getBottomRight().y - 45);
    _mouse.delay(500, false);
    // int diff = 60;
    // if (!_lastDiffs.isEmpty()) {
    // diff = _lastDiffs.toArray(new Integer[0])[_lastDiffs.size() - 1];
    // }

    /*
     * no drag now int x1 = _scanner.getBottomRight().x - 50; int y = _scanner.getBottomRight().y - 160; LOGGER.fine("drag home: " + diff);
     * _mouse.drag(x1, y, x1 - diff, y);
     */
  }

  private void handlePopups() throws AWTException, IOException, RobotInterruptedException, SessionTimeOutException {
    long t1 = System.currentTimeMillis();
    long t2;
    //LOGGER.info("Scanning for popups...");

    _mouse.mouseMove(_scanner.getBottomRight());

    // first scan popups that need to be closed
    Rectangle area;

    //NO BUTTON
    boolean found = scanAndClick(_scanner.getNoButton(), null);
    ////long t2 = System.currentTimeMillis();
    ////LOGGER.info("> handle No: " + (t2 - t1));
    
    ////t1 = t2 = System.currentTimeMillis();
    //SESSION
    checkSession();
    ////t2 = System.currentTimeMillis();
    ////LOGGER.info("> handle Session: " + (t2 - t1));
    ////t1 = t2 = System.currentTimeMillis();
    
    
    //INVITE
    found = scanAndClick(_scanner.getInvite(), null);
    if (found) {
      LOGGER.info("found Invite popup...");
    }
    
    //DAILY REWARDS
    found = scanAndClick(_scanner.getDailyRewards(), null);
    if (found) {
      //check for FB Share popup
      try {
        LOGGER.info("found Daily Rewards. Sleep a while and look for FB share popup...");
        Thread.sleep(8000);
      } catch (InterruptedException e) {
      }
      found = scanAndClick(_scanner.getFBShare(), null);
    }
    
    //FB SHARE
    found = scanAndClick(_scanner.getFBShare(), null);

    //SHARE
    found = scanAndClick(_scanner.getShare(), null);
    ////t2 = System.currentTimeMillis();
    ////LOGGER.info("> handle Share: " + (t2 - t1));
    ////t1 = t2 = System.currentTimeMillis();
    
    //MOVED TO RARE POPUPS
    //found = scanAndClick(_scanner.getPromoX(), null);
    //t2 = System.currentTimeMillis();
    //LOGGER.info("> handle PromoX: " + (t2 - t1));
    //t1 = t2 = System.currentTimeMillis();

    //SHOP
    found = found || scanAndClick(_scanner.getShopX(), null);
    ////t2 = System.currentTimeMillis();
    ////LOGGER.info("> handle ShopX: " + (t2 - t1));
    ////t1 = t2 = System.currentTimeMillis();

    //PROMO
    found = found || scanAndClick(_scanner.getPromoX(), null);

    //CLOSE
    int xx = (_scanner.getGameWidth() - 544) / 2;
    area = new Rectangle(_scanner.getTopLeft().x + xx, _scanner.getBottomRight().y - 92, 64, 33);
    found = found || findAndClick(ScreenScanner.POINTER_CLOSE3_IMAGE, area, 23, 10, true, true);

    xx = (_scanner.getGameWidth() - 90) / 2;
    area = new Rectangle(_scanner.getTopLeft().x + xx, _scanner.getBottomRight().y - 92, 90, 33);
    found = found || findAndClick(ScreenScanner.POINTER_CLOSE3_IMAGE, area, 23, 10, true, true);
    
    ////t2 = System.currentTimeMillis();
    ////LOGGER.info("> handle Close: " + (t2 - t1));
    ////t1 = t2 = System.currentTimeMillis();

    
    xx = (_scanner.getGameWidth() - 200) / 2;
         
    // now check other popups that need to refresh the game
    area = new Rectangle(_scanner.getTopLeft().x + xx, _scanner.getTopLeft().y + 184, 200, 80);
    found = findAndClick("sync.bmp", area, 84, 263, true, true);
    // found = found || findAndClick(ScreenScanner.POINTER_CLOSE4_IMAGE, area, 23, 10, true, true);
    if (found) {
      LOGGER.info("Game out of sync! Refreshing...");
      //refresh(false);
      _stats.registerRefresh();
      updateLabels();
    }
    
    
    xx = (_scanner.getGameWidth() - 144) / 2;
    area = new Rectangle(_scanner.getTopLeft().x + xx, _scanner.getBottomRight().y - 239, 75, 40);
    findAndClick(ScreenScanner.DAILY_PUBLISH, area, 23, 10, true, true);

    t2 = System.currentTimeMillis();
    LOGGER.info("POPUPS " + (t2 - t1));
  }

  private boolean scanOtherLocations(boolean fast, int number) throws AWTException, IOException, RobotInterruptedException, SessionTimeOutException,
      DragFailureException {
    LOGGER.info("Locations... ");// + number
    Rectangle area = new Rectangle(_scanner.getTopLeft().x + 1, _scanner.getTopLeft().y + 50, 193 + 88, 50);
    if (findAndClick(ScreenScanner.POINTER_LOADING_IMAGE, area, 23, 13, true)) {
      _mouse.delay(200);
      LOGGER.fine("Going to location...");

      loadTrains(fast);
      return true;
    }
    return false;
  }

  private void checkSession() throws SessionTimeOutException, RobotInterruptedException {
    if (_scanner.isOptimized()) {
      Pixel p = _scanner.getSessionTimeOut().findImage();
      if (p != null) {
        if (_resumeClick.isSelected()) {
          LOGGER.info("Session expired, but resume is ON.");
          int time = _settings.getInt("resume.time", 10);
          LOGGER.info("Waiting " + time + " minutes...");
          try {
            Thread.sleep(time * 60000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          refresh(false);
        } else {
          throw new SessionTimeOutException();
        }
      }
    }
  }

  private boolean clickHome() throws AWTException, IOException, RobotInterruptedException {
    long t1 = System.currentTimeMillis();

    boolean trainHasBeenSent = false;
    int timeGiven = 3000; // 3 secs
    long start = System.currentTimeMillis();

    LOGGER.info("looking for pointer down...");

    Pixel p1 = null, p2 = null;
    boolean done = false;
    long curr = start;
    do {
      curr = System.currentTimeMillis();
      _mouse.saveCurrentPosition();
      moveIfNecessary();
      p2 = findPointerDown(_scanner.getTrainArea(), 4);
      if (p2 != null) {
        start = System.currentTimeMillis();
        if (clickCareful(p2, true, true)) {
          // ok it is a train and it is sent
          p1 = null;
        } else {
          if (p1 != null) {
            // we have p1 and p2
            LOGGER.finest("comparing >>> " + p1 + " and " + p2);
            if (p1.x - p2.x > 10 && Math.abs(p1.y - p2.y) < 6) {
              // it seems it's a chain
              Pixel p3 = p2;

              while (p3.x > _scanner.getTopLeft().x) {
                clickCareful(p3, false, true);
                _mouse.delay(20);
                p3.x = p3.x - _scanner.getXOffset();
              }
              p3.x = p1.x + _scanner.getXOffset() * 3;
              if (p3.x > _scanner.getBottomRight().x - 2) {
                p3.x = _scanner.getBottomRight().x - 2;
              }

              if (clickCareful(p3, true, true)) {
                _mouse.delay(2000);
                trainHasBeenSent = true;
              }
              done = true;
              p1 = null;
            }
          }
        }

        // doABitMore(p2);

        p1 = p2;
      } else {
        // // take a look at other locations
        // Rectangle area = new Rectangle(_scanner.getTopLeft().x + 89,
        // _scanner.getTopLeft().y + 50, 193, 50);
        // if (findAndClick(ScreenScanner.POINTER_LOADING_IMAGE, area, 23, 13,
        // true)) {
        // loadTrains();
        // return false;
        // } else {
        // moveIfNecessary();
        // }
      }
      _mouse.checkUserMovement();
    } while (!done && curr - start <= timeGiven);

    long t2 = System.currentTimeMillis();
    LOGGER.fine("time: " + (t2 - t1));

    return trainHasBeenSent;
  }

  private void lookForPackages() throws RobotInterruptedException, AWTException, IOException {
    LOGGER.info("Looking of mail packages...");
    Pixel p = null;
    for(int i = 0; i < 2 && p == null; i++) {
      p = _scanner.getPackage1().findImage();
      if (p == null)
        p = _scanner.getPackage2().findImage();
    }
    //////_scanner.writeImage(_scanner.getPackage1().getDefaultArea(), "packageArea.bmp");
    if (p != null) {
      _mouse.delay(300);
      LOGGER.info("FOUND PACKAGE!!!");
      _mouse.mouseMove(p);
      _mouse.click();
      _mouse.delay(300);
      
//      Rectangle zone = null;
//      boolean found = false;
//      for (int i = 0; i < _scanner.getDangerousZones().length && !found; ++i) {
//        zone = _scanner.getDangerousZones()[i];
//        if (p.x >= zone.x && p.x <= zone.x + zone.width) {
//          found = true;
//        }
//      }
//      if (!found) {
//        p.y = _scanner.getBottomRight().y - _scanner.getStreet1Y() - 3;
//        clickCareful(p, true, true);
//      } else {
//        LOGGER.info("Package is in zone. Avoiding clicking...");
//      }
    }
  }
  
  private void lookForPackages2() throws RobotInterruptedException, AWTException, IOException, DragFailureException, SessionTimeOutException {
    LOGGER.info("Looking of mail packages...");
    int timeGiven = 2000; // 2 secs
    long start = System.currentTimeMillis();

    LOGGER.info("looking for pointer down...");

    Pixel p = null;
    boolean done = false;
    long curr = start;
    boolean moved = false;
    do {
      curr = System.currentTimeMillis();
      _mouse.saveCurrentPosition();

      moved = moveIfNecessary();

      p = detectPointerDown(_scanner.getPackagesArea());
      if (p != null) {
        p.x = p.x + 2;
        p.y = _scanner.getBottomRight().y - _scanner.getStreet1Y() - 4 - 10;
        clickCareful(p, false, true);
        p.y = p.y + 10;
        clickCareful(p, false, true);
        done = true;
      }
    } while (!done && curr - start <= timeGiven && !_stopThread);
    
    _mouse.delay(500);
    
    if(moved) {
      if(!scanOtherLocations(true, 33)){
        //go to somewhere and go back just to reposition the game
        _mouse.click(_scanner.getBottomRight().x - 63, _scanner.getBottomRight().y - 48);
        _mouse.delay(1000);
        goHomeIfNeeded();
      };  
    }
  }

  private boolean clickHomeFaster() throws AWTException, IOException, RobotInterruptedException, SessionTimeOutException, DragFailureException {
    boolean trainHasBeenSent = false;
    boolean hadOtherLocations = false;
    int timeGiven = _settings.getInt("clickHomeFaster.time", 4000);
    long start = System.currentTimeMillis();

    Pixel p = null;
    long curr = start;

    int maxTurns = _settings.getInt("clickHomeFaster.turns", 4) + 1;
    int turn = 1;
    int h = _settings.getInt("huntLetters", 2);
    int nn = (int) Math.pow(2, maxTurns + 1);
    int n = nn;
    for(int i = 0; i < h; i++)
      n = n / 2;
    do {
      LOGGER.info("turn " + turn++);
      curr = System.currentTimeMillis();
      _mouse.saveCurrentPosition();
      int xOff = _settings.getInt("xOff", 150);

      p = new Pixel(_scanner.getBottomRight().x - xOff, _scanner.getBottomRight().y - 100);
      p = getOutOfZone3(p);

      int[] rails = _scanner.getRailsHome();

      for (int i = rails.length - 1; i >= 0; i--) {
        p.y = _scanner.getBottomRight().y - rails[i] - 4;
        clickCareful(p, false, false);
        _mouse.checkUserMovement();
      }
      for (int i = 0; i < rails.length; i++) {
        p.y = _scanner.getBottomRight().y - rails[i] - 4;
        clickCareful(p, false, false);
        _mouse.checkUserMovement();
      }

      _mouse.saveCurrentPosition();// ???

      _mouse.delay(250);
      trainHasBeenSent = checkTrainManagement();
      if (trainHasBeenSent) {
        //_mouse.delay(250);
        start = System.currentTimeMillis();
      }

      //LETTERS
      int t = (int) Math.pow(2, turn);
      if (t % n == 0) {
        huntLetters();
      }
      
      //LOCATIONS
      if (turn % _settings.getInt("checkLocations", 2) == 0)
        hadOtherLocations = scanOtherLocations(true, 11);
      
      //SHOP POPUP CHECK
      if ((turn + 1) % _settings.getInt("checkShop", 2) == 0)
        scanAndClick(_scanner.getShopX(), null);

    } while (curr - start <= timeGiven && !_stopThread && turn < maxTurns );

    return trainHasBeenSent || hadOtherLocations;
  }

  private void huntLetters() throws RobotInterruptedException {
    LOGGER.info("Scanning for letters...");
    Pixel p = detectLetter();
    int off = 2;
    int d = 10;
    if (p != null) {
      int y = _scanner.getBottomRight().y - p.y;
      if (y < 188) {
        // fast
        _mouse.click(p.x, p.y - off - 12);
        _mouse.delay(d);
        _mouse.click(p.x, p.y - off - 12);
        _mouse.delay(d);
        _mouse.click(p.x, p.y - off - 11);
        _mouse.delay(d);
        _mouse.checkUserMovement();
        _mouse.delay(d);
        _mouse.click(p.x, p.y - off - 11);
        _mouse.delay(d);
        _mouse.click(p.x, p.y - off - 11);
      } else if (y < 230) {
        // mid
        _mouse.click(p.x, p.y - off - 9);
        _mouse.delay(d);
        _mouse.click(p.x, p.y - off - 9);
        _mouse.delay(d);
        _mouse.checkUserMovement();
        _mouse.click(p.x, p.y - off - 8);
        _mouse.delay(d);
        _mouse.click(p.x, p.y - off - 8);
      } else {
        // slow
        _mouse.click(p.x, p.y - off - 6);
        _mouse.delay(d);
        _mouse.click(p.x, p.y - off - 5);
        _mouse.delay(d);
        _mouse.checkUserMovement();
        _mouse.click(p.x, p.y - off - 4);
        _mouse.delay(d);
        _mouse.click(p.x, p.y - off - 3);
      }
      _mouse.checkUserMovement();
      LOGGER.info("Whites: " + _stats.getWhiteLetter() + "   Reds: " + _stats.getRedLetter() + "   Browns: " + _stats.getBrownLetter());
      // LOGGER.info("found letter: " + p);
      _mouse.mouseMove(_scanner.getBottomRight());
    }
  }

  private void huntLettersOld() throws RobotInterruptedException {
    LOGGER.info("Scanning for letters...");
    Pixel[] ps = detectLetters();
    int i = 0;
    int off = 4;
    for (Pixel p : ps) {
      LOGGER.info("found letter: " + p);
      _mouse.click(p.x, p.y - 6 - (i * off));
      _mouse.click(p.x, p.y - 22 - (i * off));
      _mouse.checkUserMovement();
      _mouse.click(p.x, p.y - 17 - (i * off));
      _mouse.click(p.x, p.y - 13 - (i * off));
      _mouse.checkUserMovement();
      i++;
    }
    _mouse.mouseMove(_scanner.getBottomRight());
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

  private void loadTrains(boolean fast) throws AWTException, IOException, RobotInterruptedException, SessionTimeOutException, DragFailureException {
    // moveIfNecessary();

    if (isOneClick()) {
      if (fast) {
        _mouse.delay(300);
        loadTrainsFast();
        // that's it!
      } else {
        // TODO not working well. I will avoid it for the moment

        int startX = 0;
        LOGGER.info("[L1] looking for pointer down...");
        boolean found = false;
        Pixel p = null;
        for (int i = 0; i < 10 && !found; i++) {// usually 5
          _mouse.delay(400);
          p = _scanner.getPointerDown().findImage(_scanner.getTrainArea());
          if (p != null) {
            found = true;
            LOGGER.fine("[L1] FOUND PointerDown " + p);
            checkDangerousZones(p);
            startX = p.x;
          }
        }

        final int startXX = startX;

        if (!isRunning("LOADTRAINS")) {
          Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
              try {
                shootInTheDark(startXX);
              } catch (AWTException | IOException e) {
                LOGGER.info("whaaaat again?");
              } catch (RobotInterruptedException e) {
                LOGGER.log(Level.SEVERE, "Interrupted by user4", e);
                _stopThread = true;
              } catch (SessionTimeOutException e) {
                LOGGER.info("Session time out");
              } catch (DragFailureException e) {
                handleDragFailure();
              }
            }

          }, "LOADTRAINS");

          _stopThread = false;
          myThread.start();

          // IN PARALEL check for Home hint
          long start = System.currentTimeMillis();
          long now = start;
          boolean done = false;
          do {
            now = System.currentTimeMillis();

            Pixel p1 = _scanner.getPointerDown().findImage(_scanner.getHomeArea());
            if (p1 != null) {
              LOGGER.info("HOME is calling ...");
              done = true;
              _stopThread = true;
              goHome();
              return;// rude!!!
            } else {
              try {
                Thread.sleep(400);
                LOGGER.fine("NO HOME");
              } catch (InterruptedException e) {
                LOGGER.severe("whaaat?");
              }
            }

            if (!myThread.isAlive()) {
              LOGGER.fine("thread is DEAD!!!");
              done = true;
            }
          } while ((now - start < 15000) && !done);
        }
      }
    } else {// end of oneClick

      LOGGER.info("[L2] looking for pointer down...");
      boolean found = false;
      Pixel p = null;
      for (int i = 0; i < 3 && !found; i++) {// usually 5
        _mouse.delay(2000);
        p = findPointerDown(_scanner.getTrainArea(), 4);
        if (p != null) {
          found = true;
          LOGGER.fine("[L2] FOUND PointerDown " + p);
        }
      }

      if (p != null) {
        if (isOneClick()) {
          // try clicking on other (if any) trains without waiting for hint
          for (int i = 1; i <= 3; i++) {
            p.y = (int) (p.y - _scanner.getRailYOffset());
            clickCareful(p, false, true);
          }
        } else {
          // let's click all the wagons to the left
          clickCareful(p, false, true);

          while (p.x > _scanner.getTopLeft().x) {
            clickCareful(p, false, false);
            p.x = p.x - _scanner.getXOffset();
          }
        }
      } else {
        if (!isOneClick())
          shootInTheDark(0);
      }
    }
    if (!fast)
      handlePopups();
    goHomeIfNeeded();
  }

  private void shootInTheDark(int startX) throws AWTException, IOException, RobotInterruptedException, SessionTimeOutException, DragFailureException {
    int[] rails = _scanner.getRailsOut();
    int twoThirds = (int) (_scanner.getGameWidth() * 2 / 3);

    if (startX == 0) {
      startX = _scanner.getBottomRight().x - 50;

    }
    for (int ix = startX; ix >= _scanner.getTopLeft().x + twoThirds; ix = ix - _scanner.getXOffset()) {
      // System.err.println("ix:" + ix);
      Pixel p = new Pixel(ix, _scanner.getBottomRight().y - rails[0] - 4);
      checkDangerousZones(p);
      ix = p.x;

      for (int i = 0; i < rails.length; i++) {
        // System.err.println("click:" + ix);
        _mouse.click(p.x, _scanner.getBottomRight().y - rails[i] - 4);
        if (Thread.interrupted() || _stopThread) {
          return;
        }
        _mouse.checkUserMovement();
      }
    }
  }

  private void loadTrainsFast() throws RobotInterruptedException {
    int[] rails = _scanner.getRailsOut();
    int xx = _scanner.getBottomRight().x - 80; // safe zone
    for (int i = 0; i < rails.length; i++) {
      _mouse.click(xx, _scanner.getBottomRight().y - rails[i] - 4);
    }
    for (int i = 0; i < rails.length; i++) {
      _mouse.click(xx, _scanner.getBottomRight().y - rails[i] - 4);
    }

    _mouse.delay(300);
    scanAndClick(_scanner.getNoButton(), null);
    
    // int diff = 30;
    // int x1 = _scanner.getBottomRight().x - 57;
    // int y = _scanner.getBottomRight().y - 160;
    // _mouse.drag(x1, y, x1 - diff, y);

    // xx = _scanner.getBottomRight().x - 140; // safe zone
    // for (int i = 0; i < rails.length; i++) {
    // _mouse.click(xx, _scanner.getBottomRight().y - rails[i] - 4);
    // }
  }

  protected void handleDragFailure() {
    LOGGER.severe("Drag failure...");
    LOGGER.severe("NO IDEA WHAT TO DO!!!");
    try {
      refresh(false);
      _stats.registerRefresh();
      updateLabels();

      Thread.sleep(20000);
      runMagic();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (RobotInterruptedException e) {
      LOGGER.log(Level.SEVERE, "Interrupted by user5", e);
      e.printStackTrace();
    }
  }

  /**
   * @deprecated
   * @param withMaterialsStatus
   * @throws AWTException
   * @throws IOException
   * @throws RobotInterruptedException
   * @throws SessionTimeOutException
   */
  private void captureContractors(boolean withMaterialsStatus) throws AWTException, IOException, RobotInterruptedException, SessionTimeOutException {
    // _commands.setProperty("contractor.scan", "idle");
    // _commands.saveSettingsSorted();
    String s = _settings.getProperty("contractors");
    String[] contractorNames = s.split(",");// do not forget to trim before using

    handlePopups();
    List<Contractor> contractors = new ArrayList<Contractor>();
    // open contracts
    _mouse.click(_scanner.getTopLeft().x + 234, _scanner.getBottomRight().y - 42);
    _mouse.delay(1000);

    Pixel p = _scanner.getContracts().findImage();
    Rectangle area;
    // Rectangle area = _scanner.getContracts().getDefaultArea();
    // _scanner.writeImage(area, "bingo.png");

    if (p != null) {
      // we're on the right track
      // _commands.setProperty("contractor.scan", "running");
      // _commands.saveSettingsSorted();

      Pixel scrollerTop = new Pixel(p.x + 106, p.y + 40);
      _mouse.mouseMove(scrollerTop);
      _mouse.click();
      _mouse.delay(300);

      int track = 500;
      area = new Rectangle(p.x, p.y + 31, 100, 484);
      int n = 0;
      for (String cname : contractorNames) {
        n++;
        String cfilename = cname.trim() + ".bmp";
        boolean found = false;
        if (findAndClick(cfilename, area, 15, 7, true, false)) {
          found = true;
        } else {
          // drag until get it
          int drag = 0;
          found = false;
          do {
            LOGGER.info("drag from " + drag + " to " + (drag + 75));
            _mouse.drag(scrollerTop.x, scrollerTop.y + drag, scrollerTop.x, scrollerTop.y + drag + 45);
            found = findAndClick(cfilename, area, 15, 7, true, false);
            drag += 50;
          } while (!found && drag < track);
        }
        if (found) {
          _mouse.delay(500);
          _scanner.captureGame("status " + n + "A " + cname.trim() + ".bmp");

          // _scanner.writeImage(new Rectangle(p.x - 38, p.y - 28, 780, 585), "data/" + cname.trim() + "_mission.bmp");
          _scanner.writeImage(new Rectangle(p.x - 38 + 376, p.y - 28 + 288, 71, 21), "data/" + cname.trim() + "_missionNumber.bmp");
          _scanner.writeImage(new Rectangle(p.x - 38 + 456, p.y - 28 + 60 + 93, 291, 262), "data/" + cname.trim() + "_objectives.bmp");
          _mouse.delay(500);

          if (withMaterialsStatus) {
            // click visit
            _mouse.click(p.x + 267, p.y + 335);
            _mouse.delay(2000);

            // click materials
            _mouse.click(_scanner.getTopLeft().x + 114, _scanner.getBottomRight().y - 42);
            _mouse.delay(1000);
            Pixel pm = null;
            int tries = 0;
            do {
              pm = _scanner.getMaterials().findImage();
              if (pm == null) {
                // try again after 1 second
                try {
                  Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
              }
              tries++;
            } while (pm == null && tries < 3);

            if (pm != null) {
              // _mouse.mouseMove(pm);
              // _mouse.delay(2000);
              pm = new Pixel(pm.x - 313, pm.y - 31);
              // _mouse.mouseMove(pm);

              // //2
              try {
                Rectangle rect = new Rectangle(pm.x, pm.y, 760, 550);
                _scanner.writeImage(rect, "data/" + cname.trim() + "_materials.bmp");
              } catch (Exception e) {
                e.printStackTrace();
              }
              // //

              // _scanner.captureGame("status " + n + "B " + cname.trim() + " materials" + ".bmp");
              _mouse.delay(300);
            }

            // click close
            _mouse.click(_scanner.getTopLeft().x + _scanner.getGameWidth() / 2, _scanner.getBottomRight().y - 75);
            _mouse.delay(600);

            // click
            _mouse.click(_scanner.getTopLeft().x + 197, _scanner.getBottomRight().y - 42);
            _mouse.delay(600);
          }

          // _commands.setProperty("contractor.scan", "done");
          // _commands.saveSettingsSorted();

        } else {
          LOGGER.info("Couldn't find " + cname);
        }
      }
      _mouse.click(p.x + 722, p.y - 9);
      _mouse.delay(400);
      goHomeIfNeeded();
    }

    // processContractors(contractors);
  }

  /**
   * @deprecated
   * 
   * @param contractors
   */
  private void processContractors(List<Contractor> contractors) {
    // try {
    // DataStore dataStore = new DataStore();
    //
    // ////perhaps not necessary
    // Contractor[] readContractors = dataStore.readContractors();
    // for (Contractor contractorOld : readContractors) {
    // Contractor contractorNew = findContractor(contractorOld.getName(), contractors);
    // if (contractorNew.getCurrentMissionNumber() == 0) {
    // contractorNew.setCurrentMissionNumber(contractorOld.getCurrentMissionNumber());
    // }
    // }
    // ////
    //
    // dataStore.writeContractors(contractors.toArray(new Contractor[0]));
    // } catch (IOException e1) {
    // e1.printStackTrace();
    // }

    try {
      File file = new File("contractors.txt");
      FileOutputStream fos = new FileOutputStream(file);
      BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos, "8859_1"));

      File file2 = new File("contractors.csv");
      FileOutputStream fos2 = new FileOutputStream(file2);
      BufferedWriter bw2 = new BufferedWriter(new OutputStreamWriter(fos2, "8859_1"));

      try {
        for (Contractor contractor : contractors) {
          bw.write(contractor.getName());
          bw2.write(contractor.getName() + ",");
          bw.newLine();
          Material[] materials = contractor.getMaterials();
          for (int i = 0; i < materials.length; i++) {
            bw.write(materials[i].getName() + ": " + materials[i].getAmount());
            bw2.write(materials[i].getAmount() + ",");
            bw.newLine();
          }
          bw.newLine();
          bw2.newLine();
        }
      } finally {
        bw.flush();
        bw.close();
        bw2.flush();
        bw2.close();
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  private Contractor findContractor(String name, List<Contractor> contractors) {
    for (Contractor c : contractors) {
      if (c.getName().equals(name)) {
        return c;
      }
    }
    return null;
  }

  private void locate() throws RobotInterruptedException, AWTException, IOException {
    
    // Pixel p = new Pixel(_scanner.getBottomRight().x - 100, _scanner.getBottomRight().y - 140);
    // _mouse.mouseMove(p);
    //
    // _mouse.drag(p.x, p.y, p.x - 66, p.y);
    // _mouse.delay(2000);
    //
    // p.x = p.x - 66;
    // _mouse.mouseMove(p);

    try {
      
      // handleRarePopups();
      handlePopups();

      // captureContractors(true);

      // Pixel p = _scanner.generateImageData("trainStationBookmarkFirefox.bmp", 23, 8).findImage(new Rectangle(0, 0, 400, 200));
      //Pixel p = _scanner.generateImageData("expressTrain3.bmp", 0, 0).findImage(new Rectangle(0, 0, 910, 220));
      
//      Pixel p = _scanner.getXPTrain().findImage(new Rectangle(_scanner.getTopLeft().x, _scanner.getTopLeft().y, 910, 220));
//      if (p != null) {
//        _mouse.savePosition();
//        _mouse.mouseMove(p);
//        //_mouse.click();
//        _mouse.delay(2000);
//        _mouse.restorePosition();
//      }

      // fixTheGame();
    } catch (Throwable e) {
      e.printStackTrace();
    }

    // goHomeIfNeeded();

    // Pixel p = _scanner.getPointerDown().findImage(_scanner.getTrainArea());
    // if (p != null) {
    // _mouse.mouseMove(p);
    // }

    // p = _scanner.getPointerRight().findImage();
    // if (p != null) {
    // _mouse.mouseMove(p);
    // }

    // try {
    // handleRarePopups();
    // } catch (InterruptedException e1) {
    // // TODO Auto-generated catch block
    // e1.printStackTrace();
    // }

    /*
     * { Pixel p = _scanner.getLoginWIthFB().findImage(); if (p != null) _mouse.mouseMove(p); }
     */
    // {
    // Pixel p = _scanner.getDailyRewards().findImage();
    // if (p != null)
    // _mouse.mouseMove(p);
    // }
    // {
    // Pixel p = _scanner.getSessionTimeOut().findImage();
    // if (p != null)
    // _mouse.mouseMove(p);
    // }
    // {
    // Pixel tm = _scanner.getPointerTrainManagement().findImage();
    //
    // // (ScreenScanner.POINTER_TRAIN_MANAGEMENT_IMAGE, area,
    // // time.get_coordinates().x,
    // // time.get_coordinates().y, false);
    //
    // if (tm != null) {
    // // is it freight or express?
    // Rectangle area = new Rectangle(tm.x + 310, tm.y + 25, 43, 55);
    // Pixel exP = _scanner.getTopLeftImage().findImage(area);
    // _devMode = true;
    // drawImage(area);
    // if (exP != null) {
    // LOGGER.info("EXPRESSSSSSS");
    // }
    // _devMode = false;
    // }
    // }
    // moveIfNecessary();

    /*
     * try { loadTrains(); } catch (SessionTimeOutException e) { // TODO Auto-generated catch block e.printStackTrace(); }
     */

    // checkTrainManagement();
    /*
     * long start = System.currentTimeMillis(); Robot robot = new Robot(); long now = start; do { System.err.println("trying something");
     * robot.delay(200); Point position = MouseInfo.getPointerInfo().getLocation();
     * 
     * LOGGER.info("mouse: " + position);
     * 
     * now = System.currentTimeMillis(); } while (now - start <= 10000);
     */

    LOGGER.info("done");
  }

  private Pixel getOutOfZone3(Pixel p) {
    return getOutOfZone(p, _scanner.getDangerousZones()[0]); // first zone is zone3
  }

  private Pixel getOutOfZone(Pixel p, Rectangle zone) {
    int newX = p.x;
    if (p.x >= zone.x && p.x <= zone.x + zone.width / 2) {
      newX = p.x - 10;
    } else if (p.x > zone.x + zone.width / 2 && p.x <= zone.x + zone.width) {
      newX = zone.x + zone.width + 10;
    }
    return new Pixel(newX, p.y);
  }

  private boolean checkDangerousZones(Pixel p) throws RobotInterruptedException {

    // find which zone first
    Rectangle zone = null;
    boolean found = false;
    int i;
    for (i = 0; i < _scanner.getDangerousZones().length && !found; ++i) {
      zone = _scanner.getDangerousZones()[i];
      if (p.x >= zone.x && p.x <= zone.x + zone.width) {
        found = true;
      }
    }

    if (found) {
      int diff;
      int y = _scanner.getBottomRight().y - 160;
      int x1;
      if (i >= _scanner.getDangerousZones().length - 1) {
        // first n-1 zones move left, the rest move right
        diff = p.x - zone.x + 18;
        x1 = _scanner.getBottomRight().x - 50;
      } else {
        diff = (zone.x + zone.width - p.x) + 18;
        diff = -diff;
        x1 = _scanner.getTopLeft().x + 50;
      }

      LOGGER.fine("avoid zone [" + zone.x + " - " + (zone.x + zone.width) + "]");
      LOGGER.fine("drag " + diff);
      LOGGER.finest("[1] p.x = " + p.x);
      _mouse.drag(x1, y, x1 - diff, y);
      _mouse.saveCurrentPosition();
      p.x = p.x - diff;
      LOGGER.finest("[2] p.x = " + p.x);

      // Rectangle miniArea = new Rectangle(p.x - 44, p.y - 90, 88, 180);
      Pixel p2 = _scanner.getPointerDown().findImage(_scanner.getTrainArea());

      if (p2 != null) {
        found = false;
        for (i = 0; i < _scanner.getDangerousZones().length && !found; ++i) {
          zone = _scanner.getDangerousZones()[i];
          if (p.x >= zone.x && p.x <= zone.x + zone.width) {
            found = true;
          }
        }
        p.x = p2.x;
        return found;
      }
      LOGGER.finest("[3] p.x = " + p.x);
    }
    return false;
  }

  private boolean moveIfNecessary() throws RobotInterruptedException {
    boolean moved = false;
    Pixel p = _scanner.getPointerLeft().findImage();
    if (p != null) {
      LOGGER.info("Found left arrow. moving a bit...");
      // _mouse.mouseMove(p);
      int x1 = _scanner.getTopLeft().x + 5;
      int y = _scanner.getBottomRight().y - Locations.RAIL1;
      _mouse.drag(x1, y, x1 + 520, y);
      // _mouse.delay(500);
      moved = true;
    } else {
      p = _scanner.getPointerRight().findImage();
      if (p != null) {
        LOGGER.info("Found right arrow. moving a bit...");
        // _mouse.mouseMove(p);
        int x1 = _scanner.getBottomRight().x - 5;
        int y = _scanner.getBottomRight().y - Locations.RAIL1;
        _mouse.drag(x1, y, x1 - 520, y);
        // _mouse.delay(500);
        moved = true;
      } else {
        ImageData pointerDown = _scanner.getPointerDownL();
        Rectangle area = new Rectangle(_scanner.getBottomRight().x - 26, _scanner.getBottomRight().y - Locations.RAIL1 - 150, 26, 150);
        p = findPointerDownInt(area, pointerDown, 4);
        if (p != null) {
          int x1 = _scanner.getBottomRight().x - 5;
          int y = _scanner.getBottomRight().y - Locations.RAIL1;
          _mouse.drag(x1, y, x1 - 30, y);
          moved = true;
        }
        if (p == null) {
          pointerDown = _scanner.getPointerDownR();
          area = new Rectangle(_scanner.getTopLeft().x, _scanner.getBottomRight().y - Locations.RAIL1 - 150, 26, 150);
          p = findPointerDownInt(area, pointerDown, 4);
          if (p != null) {
            int x1 = _scanner.getTopLeft().x + 5;
            int y = _scanner.getBottomRight().y - Locations.RAIL1;
            _mouse.drag(x1, y, x1 + 30, y);
            moved = true;
          }
        }
      }
    }
    return moved;
  }

  private Pixel detectPointerDown(Rectangle area) throws RobotInterruptedException {
    ImageData pointerDown = _scanner.getPointerDown();
    Pixel p = pointerDown.findImage(area == null ? _scanner.getTrainArea() : area);
    if (p != null && p.x >= 2)
      p.x -= 2;// DOUBLE LOCO problem
    return p;
  }

  private Pixel[] detectLetters() throws RobotInterruptedException {
    //
    // ImageData red = _scanner._letterRed;
    // Pixel p = red.findImage(_scanner.getLetterArea());
    // if (p == null) {
    // _mouse.checkUserMovement();
    // ImageData white = _scanner._letterWhite;
    // p = white.findImage(_scanner.getLetterArea());
    // if (p == null) {
    // _mouse.checkUserMovement();
    // ImageData brown = _scanner._letterBrown;
    // p = brown.findImage(_scanner.getLetterArea());
    // if (p != null)
    // LOGGER.info("brown1");
    // } else {
    // LOGGER.info("white1");
    // }
    // } else {
    // LOGGER.info("red1");
    // }
    // _mouse.checkUserMovement();
    // if (p == null) {

    List<Pixel> res = new ArrayList<>();
    ImageData white = _scanner._letterWhite2;
    Pixel p = white.findImage(_scanner.getLetterArea());
    if (p != null) {
      res.add(p);
      LOGGER.info("white2");
    }
    _mouse.checkUserMovement();

    ImageData red = _scanner._letterRed2;
    p = red.findImage(_scanner.getLetterArea());
    if (p != null) {
      res.add(p);
      LOGGER.info("red2");
    }
    _mouse.checkUserMovement();

    ImageData brown = _scanner._letterBrown2;
    p = brown.findImage(_scanner.getLetterArea());
    if (p != null) {
      res.add(p);
      LOGGER.info("brown2");
    }
    // }

    return res.toArray(new Pixel[0]);
  }

  private Pixel detectLetter() throws RobotInterruptedException {
    ImageData white = _scanner._letterWhite3;
    Pixel p = white.findImage(_scanner.getLetterArea());
    if (p != null) {
      Rectangle tinyArea = new Rectangle(p.x - 6, p.y - 200, 13, 203);
      Pixel p2 = white.findImage(tinyArea);
      if (p2 != null)
        p = p2;
      _stats.registerWhiteLetter();
      return p;
    }
    _mouse.checkUserMovement();

    ImageData red = _scanner._letterRed3;
    p = red.findImage(_scanner.getLetterArea());
    if (p != null) {
      Rectangle tinyArea = new Rectangle(p.x - 6, p.y - 200, 13, 203);
      Pixel p2 = red.findImage(tinyArea);
      if (p2 != null)
        p = p2;
      _stats.registerRedLetter();
      return p;
    }
    _mouse.checkUserMovement();

    ImageData brown = _scanner._letterBrown3;
    p = brown.findImage(_scanner.getLetterArea());
    if (p != null) {
      Rectangle tinyArea = new Rectangle(p.x - 6, p.y - 200, 13, 203);
      Pixel p2 = brown.findImage(tinyArea);
      if (p2 != null)
        p = p2;

      _stats.registerBrownLetter();
      return p;
    }

//    white = _scanner._letterWhite4;
//    p = white.findImage(_scanner.getLetterArea());
//    if (p != null) {
//      Rectangle tinyArea = new Rectangle(p.x - 6, p.y - 200, 13, 203);
//      Pixel p2 = white.findImage(tinyArea);
//      if (p2 != null)
//        p = p2;
//      _stats.registerWhiteLetter();
//      return p;
//    }

//    ImageData pink = _scanner._letterPink3;
//    p = pink.findImage(_scanner.getLetterArea());
//    if (p != null) {
//      Rectangle tinyArea = new Rectangle(p.x - 6, p.y - 200, 13, 203);
//      Pixel p2 = pink.findImage(tinyArea);
//      if (p2 != null)
//        p = p2;
//
//      _stats.registerBrownLetter();
//      return p;
//    }
    return null;
  }

  private Pixel findPointerDown(Rectangle area, int railNumber) throws RobotInterruptedException {

    ImageData pointerDown = _scanner.getPointerDown();
    Pixel p = findPointerDownInt(area, pointerDown, railNumber);
    // if (p == null) {
    // pointerDown = _scanner.get_pointerDownL();
    // area = new Rectangle(_scanner.getBottomRight().x - 70,
    // _scanner.getBottomRight().y - Locations.RAIL1 - 150, 70,
    // 150);
    // p = findPointerDownInt(area, pointerDown, railNumber);
    // if (p == null) {
    // pointerDown = _scanner.get_pointerDownR();
    // area = new Rectangle(_scanner.getTopLeft().x, _scanner.getBottomRight().y
    // - Locations.RAIL1 - 150, 70, 150);
    // p = findPointerDownInt(area, pointerDown, railNumber);
    // }
    // }
    return p;
  }

  private Pixel findPointerDownInt(Rectangle area, ImageData pointerDown, int railNumber) throws RobotInterruptedException {
    Pixel p = pointerDown.findImage(area);
    if (p != null) {
      LOGGER.info("FOUND Down pointer" + p);
      int maxY = getMaxY(p);
      // LOGGER.info("Maxxed pointer is" + p);
      // LOGGER.info("maxY=" + maxY);
      p.x = p.x - 2;
      p.y = (int) (maxY + _scanner.getRailYOffset() * 2);
      // LOGGER.info("p.y=" + p.y);
      // LOGGER.info("railYOffset=" + _scanner.getRailYOffset());
      if (p.y > _scanner.getBottomRight().y - Locations.RAIL1) {
        p.y = _scanner.getBottomRight().y - Locations.RAIL1 - 3;
        LOGGER.warning("Point below rail 1! + " + p);
      }
    }
    return p;
  }

  private boolean clickCareful(Pixel p, boolean careful, boolean adjust) throws AWTException, IOException, RobotInterruptedException {
    if (careful) {
      if (checkTrainManagement())
        return true;
    }
    _mouse.mouseMove(p);
    _mouse.click();
    if (careful) {
      _mouse.delay(500);
      if (checkTrainManagement())
        return true;
    }

    if (adjust) {
      Rectangle miniArea = new Rectangle(p.x - 24, p.y - 90, 48, 180);
      Pixel p2 = _scanner.getPointerDown().findImage(miniArea);
      if (p2 != null) {
        // click didn't work
        int tries = 2;
        boolean worked = false;
        int newY = p.y;
        for (int i = 1; i <= tries && !worked; i++) {
          newY = p.y + i * 4;
          p2 = new Pixel(p.x, newY);
          _mouse.mouseMove(p2);
          _mouse.click();
          _mouse.delay(250);
          p2 = _scanner.getPointerDown().findImage(miniArea);
          worked = (p2 == null);
        }
        if (!worked) {
          for (int i = 1; i <= tries && !worked; i++) {
            newY = p.y - i * 4;
            p2 = new Pixel(p.x, newY);
            _mouse.mouseMove(p2);
            _mouse.click();
            _mouse.delay(250);
            p2 = _scanner.getPointerDown().findImage(miniArea);
            worked = (p2 == null);
          }
        }
        if (worked) {
          p.y = newY;
          if (careful) {
            _mouse.delay(500);
            if (checkTrainManagement())
              return true;
          } else
            return true;
        }
      }
    }

    return false;
  }

  private boolean checkTrainManagement() throws AWTException, IOException, RobotInterruptedException {
    // Rectangle area = new Rectangle(_scanner.getTopLeft().x + 335,
    // _scanner.getTopLeft().y + 47, 130, 90);
    // Location time = _times[getTimeIndex()];
    Location time = _freightTime;
    Pixel tm = _scanner.getTrainManagementAnchor().findImage();

    // (ScreenScanner.POINTER_TRAIN_MANAGEMENT_IMAGE, area,
    // time.get_coordinates().x,
    // time.get_coordinates().y, false);

    if (tm != null) {
      // is it freight or express?
      _mouse.delay(200);
      Rectangle area = new Rectangle(tm.x + 254, tm.y + 28, 169, 72);
      boolean isExpress = false;
      Pixel xpP = _scanner.getXPTrain().findImage(area);
      if (xpP != null) {
        time = _xpTime;
        LOGGER.info("XP " + time.getTime());
      } else {
        Pixel exP = _scanner.getExpressTrain().findImage(area);
        if (exP != null) {
          time = _expressTime;
          isExpress = true;
          LOGGER.info("EXPRESS " + time.getTime());
        } else {
          Pixel freeP = _scanner.getFreeTrain().findImage(area);
          if (freeP != null) {
            time = _freeTime;
            LOGGER.info("FREE " + time.getTime());
          } else {
            time = _freightTime;
            LOGGER.info("FREIGHT " + time.getTime());
          }
        }
      }
      
      // 1. ensure the page
      Pixel leftArrow = new Pixel(tm.x - 292, tm.y + 302);
      Pixel rightArrow = new Pixel(tm.x + 393, tm.y + 302);
      boolean firstPage = false;
      int tries = 3;
      do {
        Pixel sixMinutes = _scanner.getSixMinutes().findImage(new Rectangle(tm.x - 242, tm.y + 225, 77, 22));
        if (sixMinutes != null)
          firstPage = true;
        else {
          tries--;
          _mouse.mouseMove(leftArrow);
          _mouse.click();
          _mouse.delay(200);
        }
        
      } while (!firstPage && tries > 0);

//      _mouse.mouseMove(leftArrow);
//      for (int i = 0; i < 3; i++) {
//        _mouse.click();
//        _mouse.delay(200);
//      }

      // 2. now, go to desired page
      if (time.getPage() > 1) {
        _mouse.mouseMove(rightArrow);
        for (int i = 1; i < time.getPage(); i++) {
          _mouse.click();
          _mouse.delay(200);
        }
      }
      
      
      // 3. click the destination

      _mouse.mouseMove(tm.x + time.getCoordinates().x, tm.y + time.getCoordinates().y);
      if (_settings.getProperty("captureTrains", "false").equalsIgnoreCase("true")) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd  HH-mm-ss-SSS");
        String date = sdf.format(Calendar.getInstance().getTime());
        String filename = "train " + date + ".png";
        _scanner.captureGame(filename);
      }
      _mouse.delay(500, false); // give chance to change the location without
      _mouse.savePosition(); // stopping the magic.

      if (!_devMode) {
        _mouse.click();
        _stats.registerTrain(isExpress);
        String msg = "Trains: " + _stats.getTotalTrainCount();
        // String date = getNow();
        // _trainsNumberLabel.setText(msg + "  (" + date + ")");
        ////LOGGER.severe(msg);
        // _trainsNumberLabel.invalidate();
        updateLabels();
        boolean weredone = false;
        int turns = 0;
        do {
          turns++;
          LOGGER.fine("Check TM again " + turns);
          tm = _scanner.getTrainManagementAnchor().findImage();
          if (tm != null) {
            _mouse.delay(300);
          } else {
            // we're done
            weredone = true;
            LOGGER.fine("Check TM again DONE ");
          }
        } while (!weredone && turns < 6);
      }
      return true;
    }

    return false;
  }

  private int getMaxY(Pixel p) throws RobotInterruptedException {
    Rectangle miniArea = new Rectangle(p.x - 24, p.y - 90, 48, 180);
    Pixel[] ps = new Pixel[80];
    int maxY = p.y;
    for (int i = 0; i < ps.length; i++) {
      ps[i] = _scanner.getPointerDown().findImage(miniArea);
      _mouse.delay(5);
      if (ps[i] != null) {
        if (ps[i].y > maxY) {
          maxY = ps[i].y;
        }
      }
    }
    return maxY;
  }

  public void rescanContractor(String contractor) {
    _captureContractors.clear();
    _captureContractors.add(contractor);
  }

  private void scanTrains() {
    if (isRunning("MAGIC")) {
      stopMagic();
    }
    if (_trainManagementWindow == null) {
      TrainScanner tscanner = new TrainScanner(_scanner, LOGGER, _settings.getInt("IntTrains.takeABreakAfter", 3));
      _trainManagementWindow = new TrainManagementWindow(null, tscanner);
    }
    _trainManagementWindow.setVisible(true);
  }

  private boolean clickHomeOneClick() throws AWTException, IOException, RobotInterruptedException, SessionTimeOutException, DragFailureException {
    long t1 = System.currentTimeMillis();

    boolean trainHasBeenSent = false;
    boolean hadOtherLocations = false;
    int timeGiven = 2000; // 2 secs
    long start = System.currentTimeMillis();

    LOGGER.info("looking for pointer down...");

    Pixel p = null;
    boolean done = false;
    long curr = start;
    do {
      curr = System.currentTimeMillis();
      _mouse.saveCurrentPosition();

      moveIfNecessary();

      p = detectPointerDown(null);
      if (p != null) {
        boolean danger = checkDangerousZones(p);
        // Pixel p2 = detectPointerDown();
        // if (p2 != null)
        // p = p2;
        start = System.currentTimeMillis();
        _mouse.saveCurrentPosition();

        int[] rails = _scanner.getRailsHome();

        if (!danger) {
          // fast click all rails + street1 mainly for mail express trains
          p.y = _scanner.getBottomRight().y - _scanner.getStreet1Y() - 4;
          clickCareful(p, false, false);

          for (int i = rails.length - 1; i >= 0; i--) {
            p.y = _scanner.getBottomRight().y - rails[i] - 4;
            clickCareful(p, false, false);
          }
          for (int i = 0; i < rails.length; i++) {
            p.y = _scanner.getBottomRight().y - rails[i] - 4;
            clickCareful(p, false, false);
          }
        }

        // Try mail again. This time with adjusting
        p.y = _scanner.getBottomRight().y - _scanner.getStreet1Y() - 3;
        clickCareful(p, danger, true);

        _mouse.delay(250);
        trainHasBeenSent = checkTrainManagement() || trainHasBeenSent;
        _mouse.delay(250);
        hadOtherLocations = scanOtherLocations(true, 11);

        if (_captureContractors.size() == 0) {
          _mouse.delay(250);

          // again all rails one by one now more carefully
          boolean stop = false;
          for (int i = 1; i < rails.length && !stop && !_stopThread; i++) {
            try {
              LOGGER.info("trying rail " + (i + 1));
              p.y = _scanner.getBottomRight().y - rails[i] - 4;
              trainHasBeenSent = clickCareful(p, true, false) || trainHasBeenSent;
              _mouse.delay(200);
              trainHasBeenSent = clickCareful(p, true, false) || trainHasBeenSent;
              _mouse.checkUserMovement();

              Pixel pp = detectPointerDown(null);
              if (pp != null && Math.abs(pp.x - p.x) > 5) {
                stop = true;
                break;
              }

              if ((i + 1) % 2 == 0)
                huntLetters();

              if (scanOtherLocations(true, 22)) {
                hadOtherLocations = true;
                p.x = _scanner.getBottomRight().x - 80;
              }
            } catch (AWTException | IOException e) {
              LOGGER.log(Level.SEVERE, "Critical error occured", e);
            }
          } // for
        }

      } else {
        // p == null -> no trains detected yet
        huntLetters();
        hadOtherLocations = scanOtherLocations(true, 44);
      }

      _mouse.checkUserMovement();
    } while (!done && curr - start <= timeGiven && !_stopThread);

    long t2 = System.currentTimeMillis();
    LOGGER.fine("time: " + (t2 - t1));

    return trainHasBeenSent || hadOtherLocations;
  }

}
