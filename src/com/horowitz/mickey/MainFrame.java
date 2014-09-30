/**
 * 
 */
package com.horowitz.mickey;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Insets;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
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

/**
 * @author zhristov
 * 
 */
public final class MainFrame extends JFrame {

  private final static Logger LOGGER       = Logger.getLogger(MainFrame.class.getName());

  private static final String APP_TITLE    = "v0.625.b3";

  private boolean             _refresh     = true;
  private boolean             _devMode     = false;
  private boolean             _ping        = true;

  private ScreenScanner       _scanner;
  private MouseRobot          _mouse;
  private boolean             _stopThread  = false;
  private Statistics          _stats;
  private JLabel              _trainsNumberLabel;
  private JLabel              _trainsNumberLabelA;
  private JLabel              _freightTrainsNumberLabel;
  private JLabel              _freightTrainsNumberLabelA;
  private JLabel              _expressTrainsNumberLabel;
  private JLabel              _expressTrainsNumberLabelA;
  private JLabel              _refreshNumberLabel;
  private JLabel              _refreshNumberLabelA;
  private JLabel              _refreshTimeLabel;
  private JLabel              _lastActivityLabel;
  private JLabel              _startedLabel;

  private JButton             _locateAction;
  private JButton             _resetAction;
  private JButton             _doMagicAction;

  private Location            _freightTime = Locations.LOC_10MIN;
  private Location            _expressTime = Locations.LOC_30MIN;

  private Long                _lastTime;
  private Queue<Integer>      _lastDiffs   = new ArrayBlockingQueue<Integer>(3);

  private JToggleButton       _oneClick;

  private Settings            _settings;

  private JToggleButton       _refreshClick;

  private JToggleButton       _pingClick;
  private long                _lastPingTime;

  private boolean isOneClick() {
    return _oneClick.isSelected();
  }

  public MainFrame(Boolean refresh, Boolean ping) throws HeadlessException {
    super();
    _settings = new Settings();
    _stats = new Statistics();

    // _settings.setDefaults();
    // _settings.saveSettings();

    _settings.loadSettings();
    // addWindowListener(new WindowAdapter() {
    // @Override
    // public void windowClosing(WindowEvent e) {
    // super.windowClosing(e);
    // _settings.saveSettings();
    // }
    // });

    _refresh = refresh != null ? refresh : Boolean.parseBoolean(_settings.getProperty("refresh", "false"));
    _ping = ping != null ? ping : Boolean.parseBoolean(_settings.getProperty("ping", "false"));
    setupLogger();

    init();

    KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new MyKeyEventDispatcher());
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

    JToolBar mainToolbar1 = new JToolBar();
    JToolBar mainToolbar2 = new JToolBar();

    JToolBar frToolbar1 = new JToolBar();
    JToolBar frToolbar2 = new JToolBar();
    frToolbar1.add(new JLabel("Freight     "));
    JToolBar exToolbar1 = new JToolBar();
    JToolBar exToolbar2 = new JToolBar();
    exToolbar1.add(new JLabel("Express  "));

    JPanel toolbars = new JPanel(new GridLayout(6, 1));
    toolbars.add(mainToolbar1);
    toolbars.add(mainToolbar2);
    mainToolbar1.setFloatable(false);
    mainToolbar2.setFloatable(false);
    frToolbar1.setFloatable(false);
    frToolbar2.setFloatable(false);
    exToolbar1.setFloatable(false);
    exToolbar2.setFloatable(false);
    frToolbar1.setBackground(new Color(201, 177, 133));
    frToolbar2.setBackground(new Color(201, 177, 133));
    exToolbar1.setBackground(new Color(153, 173, 209));
    exToolbar2.setBackground(new Color(153, 173, 209));
    toolbars.add(frToolbar1);
    toolbars.add(frToolbar2);
    toolbars.add(exToolbar1);
    toolbars.add(exToolbar2);
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
    _refreshNumberLabel = new JLabel("" + (_refresh ? "88" : "off"));
    _refreshNumberLabelA = new JLabel("88");
    _refreshNumberLabelA.setForeground(Color.GRAY);
    _refreshNumberLabel.setFont(_refreshNumberLabel.getFont().deriveFont(14.0f));
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
              refresh();
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
      _locateAction = new JButton(new AbstractAction("Locate") {
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
      mainToolbar2.add(_oneClick);
    }
    // Refresh
    {
      _refreshClick = new JToggleButton("Auto refresh");
      _refreshClick.setSelected(_refresh);
      mainToolbar2.add(_refreshClick);
    }

    // Ping
    {
      _pingClick = new JToggleButton("Ping");
      _pingClick.setSelected(_ping);
      mainToolbar2.add(_pingClick);
    }

    ButtonGroup bgFr = new ButtonGroup();
    createButtons(frToolbar1, bgFr, Locations.LOC_PAGE1, true);
    createButtons(frToolbar2, bgFr, Locations.LOC_PAGE2, true);
    createButtons(frToolbar2, bgFr, Locations.LOC_PAGE3, true);

    ButtonGroup bgEx = new ButtonGroup();
    createButtons(exToolbar1, bgEx, Locations.LOC_PAGE1, false);
    createButtons(exToolbar2, bgEx, Locations.LOC_PAGE2, false);
    createButtons(exToolbar2, bgEx, Locations.LOC_PAGE3, false);
    ((JToggleButton) frToolbar1.getComponent(3)).setSelected(true);
    ((JToggleButton) exToolbar1.getComponent(4)).setSelected(true);

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

  private void updateLabels() {
    _stats.updateTime();
    _trainsNumberLabel.setText("" + _stats.getTotalTrainCount());
    _freightTrainsNumberLabel.setText("" + _stats.getFreightTrainCount());
    _expressTrainsNumberLabel.setText("" + _stats.getExpressTrainCount());
    _refreshNumberLabel.setText("" + _stats.getRefreshCount());
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
          LOGGER.info("Let's get rolling...");
          new Robot().delay(200);
          doMagic();
        } catch (Exception e1) {
          LOGGER.severe(e1.getMessage());
          e1.printStackTrace();
        }
      }
    });

    myThread.start();
  }

  private void createButtons(final JToolBar toolbar, final ButtonGroup bg, final Location[] locations, final boolean freight) {
    for (int i = 0; i < locations.length; i++) {
      final Location l = locations[i];
      JToggleButton button = new JToggleButton(new AbstractAction(l.getName()) {

        public void actionPerformed(ActionEvent e) {
          if (freight) {
            _freightTime = l;
            LOGGER.fine("selected freight: " + l.getName());
          } else {
            _expressTime = l;
            LOGGER.fine("selected express: " + l.getName());
          }
        }
      });
      bg.add(button);
      toolbar.add(button);

    }
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

  private void refresh() throws RobotInterruptedException {
    _lastTime = System.currentTimeMillis();
    try {
      String dateStr = DateUtils.formatDateForFile(System.currentTimeMillis());
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      ImageIO.write(new Robot().createScreenCapture(new Rectangle(screenSize)), "PNG", new File("refresh " + dateStr + ".png"));
      deleteOlder("refresh", 10);
    } catch (HeadlessException e1) {
      e1.printStackTrace();
    } catch (IOException e1) {
      e1.printStackTrace();
      LOGGER.severe(e1.getMessage());
    } catch (AWTException e1) {
      e1.printStackTrace();
    }
    LOGGER.info("Time to refresh...");
    Pixel p;
    if (_scanner.isOptimized()) {
      p = _scanner.getBottomRight();
      p.y = _scanner.getTopLeft().y + 100;
      p.x = _scanner.getBottomRight().x + 4;
    } else {
      p = new Pixel(1, 347);
    }
    _mouse.click(p.x, p.y);

    try {
      Robot robot = new Robot();
      robot.keyPress(KeyEvent.VK_F5);
      robot.keyRelease(KeyEvent.VK_F5);
      LOGGER.fine("Wait 10 seconds...");
      _mouse.delay(10000);
      boolean done = false;
      try {
        for (int i = 0; i < 17 && !done; i++) {
          LOGGER.info("after refresh recovery try " + (i + 1));
          handleRarePopups();

          // OTHER POPUPS
          handlePopups();// hmm
          _mouse.delay(1000);

          // LOCATE THE GAME
          if (_scanner.locateGameArea()) {
            LOGGER.info("Game located successfully!");
            done = true;
          }
        }
      } catch (AWTException | IOException e) {
        LOGGER.info("whaaaat again?");
      } catch (InterruptedException e) {
        LOGGER.info("interrupted");
      } catch (SessionTimeOutException e) {
        LOGGER.info("Session time out. Stopping.");
      }

      // fixTheGame();
      _stats.registerRefresh();
      updateLabels();

      LOGGER.info("Refresh done");
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

  private void handleRarePopups() throws InterruptedException, RobotInterruptedException {
    LOGGER.info("Checking for FB login and daily rewards...");
    _mouse.savePosition();
    _mouse.mouseMove(0, 0);
    // LOGIN
    Pixel pFB = _scanner.getLoginWIthFB().findImage();
    if (pFB != null) {
      _mouse.mouseMove(pFB);
      _mouse.saveCurrentPosition();
      _mouse.click();
      LOGGER.info("Logged through FB. Wait 5 seconds...");
      _mouse.delay(5000);
    }

    // DAILY
    Pixel pDaily = _scanner.getDailyRewards().findImage();
    if (pDaily != null) {
      _mouse.mouseMove(pDaily);
      _mouse.saveCurrentPosition();
      _mouse.click();
      LOGGER.info("Daily rewards clicked.");
      _mouse.delay(1000);
    }
    // PROMO
    Pixel promo = _scanner.getPromoX().findImage();
    if (promo != null) {
      _mouse.mouseMove(promo);
      _mouse.saveCurrentPosition();
      _mouse.click();
      LOGGER.info("Promo popup clicked.");
      _mouse.delay(1000);
    }
    _mouse.restorePosition();
  }

  private String getNow() {
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm  dd MMM");
    String date = sdf.format(Calendar.getInstance().getTime());
    return date;
  }

  private void fixTheGame() {
    Pixel p = _scanner.getZoomOutPixel();
    _mouse.mouseMove(p);
    try {
      for (int i = 0; i < 7; i++) {
        _mouse.click();
        _mouse.delay(200, false);
      }
      _mouse.delay(200, false);
      int x1 = _scanner.getBottomRight().x - 5;
      int y = _scanner.getBottomRight().y - Locations.RAIL1;
      _mouse.drag(x1, y, x1 - 640, y);
      _mouse.delay(200, false);
      p = _scanner.getTopPlayersPixel();
      _mouse.mouseMove(p);
      _mouse.click();
      _mouse.delay(200, false);
    } catch (RobotInterruptedException e) {
      LOGGER.log(Level.SEVERE, "Interrupted by user7", e);
      _stopThread = true;
    }
  }

  public void doMagic() {
    setTitle(APP_TITLE + " RUNNING");
    _lastPingTime = System.currentTimeMillis();

    if (_refreshClick.isSelected())
      updateLabels();

    int timeForRefresh = (getShortestTime() + 2) * 60000;
    int mandatoryRefresh = _settings.getInt("mandatoryRefresh.time") * 60000;

    long start = System.currentTimeMillis();
    long fstart = System.currentTimeMillis();
    NumberFormat nf = NumberFormat.getNumberInstance();
    nf.setMaximumFractionDigits(3);
    nf.setMinimumFractionDigits(0);
    while (true) {
      try {
        updateLabels();

        goHomeIfNeeded();

        if (_pingClick.isSelected()) {
          ping();
        }

        // REFRESH
        if (_refreshClick.isSelected() && timeForRefresh > 3 * 60000) {// if "0"
          // chosen no
          // refresh
          long now = System.currentTimeMillis();
          String t = nf.format(((double) (now - start) / 60000));
          LOGGER.info("time: " + DateUtils.fancyTime2(now - start));

          if (now - start >= timeForRefresh) {
            LOGGER.info("Warning: no trains for last " + t + " minutes");
            refresh();
            fstart = start = System.currentTimeMillis();
          }

          if (mandatoryRefresh > 0 && now - fstart >= mandatoryRefresh) {
            LOGGER.info("Mandatory refresh");
            refresh();
            fstart = start = System.currentTimeMillis();
          }

          // check again has refresh gone well after 3 minutes
          if (_lastTime != null && now - _lastTime >= 3 * 60 * 1000) {
            handleRarePopups();
            _lastTime = System.currentTimeMillis();
          }
        }

        // POPUPS

        handlePopups();

        // HOME
        boolean flag;
        if (_oneClick.isSelected())
          flag = clickHomeOneClick();
        else
          flag = clickHome();
        if (flag) {
          // true means train has been sent. refresh postponed
          start = System.currentTimeMillis();
        }

        // OTHER LOCATIONS
        scanOtherLocations(true);// TODO only fast scenario for the moment

        _mouse.delay(200);

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
        break;
      } catch (DragFailureException e) {
        handleDragFailure();
        break;
      } catch (Throwable e) {
        LOGGER.severe("SOMETHING WENT WRONG!");
        e.printStackTrace();
        setTitle("SOMETHING WENT WRONG!");
        break;
      }

    }

  }

  private void ping() {
    long now = System.currentTimeMillis();
    long time = _settings.getInt("ping.time") * 60000; // from minutes to millseconds
    if (now - _lastPingTime >= time) {
      LOGGER.info("ping");
      _scanner.captureGame("ping " + DateUtils.formatDateForFile(System.currentTimeMillis()) + ".png");
      deleteOlder("ping", 5);
      _lastPingTime = System.currentTimeMillis();
    }
  }

  private void goHomeIfNeeded() throws AWTException, IOException, RobotInterruptedException {
    Rectangle area = new Rectangle(_scanner.getTopLeft().x, _scanner.getBottomRight().y - 50, 60, 50);
    Pixel p = _scanner.getHome().findImage(area);
    if (p != null) {
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
      _mouse.mouseMove(p);
      _mouse.delay(100);
      if (capture) {
        _scanner.captureGame();
        deleteOlder("popup", 10);
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
    int diff = 60;
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

    LOGGER.info("Scanning for popups...");

    // _mouse.savePosition();
    // NO CLICK - it causes more trrouble than good
    // _mouse.click(_scanner.getBottomRight().x - 8, _scanner.getBottomRight().y - 8);
    // _mouse.delay(100);
    _mouse.mouseMove(_scanner.getBottomRight());

    // first scan popups that need to be closed
    Rectangle area;

    area = new Rectangle(_scanner.getBottomRight().x - 32 - 53, _scanner.getTopLeft().y, 32 + 53, 55 + 15);
    boolean found = findAndClick(ScreenScanner.POINTER_NIGHTX, area, 8, 8, true, true);
    found = found || findAndClick(ScreenScanner.POINTER_DAYLIGHTX, area, 8, 8, true, true);
    if (found)
      _mouse.delay(300);

    area = new Rectangle(_scanner.getTopLeft().x + 90, _scanner.getBottomRight().y - 100, _scanner.getGameWidth() - 180, 60);
    found = findAndClick(ScreenScanner.POINTER_CLOSE1_IMAGE, area, 23, 10, true, true);
    found = found || findAndClick(ScreenScanner.POINTER_CLOSE3_IMAGE, area, 23, 10, true, true);
    found = found || findAndClick(ScreenScanner.POINTER_CLOSE4_IMAGE, area, 23, 10, true, true);

    // area = new Rectangle(_scanner.getBottomRight().x - 156-53, _scanner.getBottomRight().y - 516-15, 55+53, 55+15);
    // found = found || findAndClick(ScreenScanner.POINTER_TIPSX, area, 15, 20, true, true);

    area = new Rectangle(_scanner.getTopLeft().x + _scanner.getGameWidth() / 2, _scanner.getTopLeft().y + 60, _scanner.getGameWidth() / 2, 62);
    found = found || findAndClick(ScreenScanner.POINTER_PROMOX, area, 14, 14, true, true);

    checkSession();

    // TODO check this
    // area = new Rectangle(_scanner.getTopLeft().x + 350, _scanner.getBottomRight().y - 211-15, 81, 42+15);
    // drawImage(area);

    // found = findAndClick(ScreenScanner.POINTER_CLOSE1_IMAGE, area, 23, 10, true, true);
    // found = found || findAndClick(ScreenScanner.POINTER_CLOSE3_IMAGE, area, 23, 10, true, true);
    // found = found || findAndClick(ScreenScanner.POINTER_CLOSE4_IMAGE, area, 23, 10, true, true);

    // now check other popups that need to refresh the game
    area = new Rectangle(_scanner.getTopLeft().x + 300, _scanner.getBottomRight().y - 240, _scanner.getGameWidth() - 600, 150);
    found = findAndClick(ScreenScanner.POINTER_CLOSE1_IMAGE, area, 23, 10, true, true);
    found = found || findAndClick(ScreenScanner.POINTER_CLOSE3_IMAGE, area, 23, 10, true, true);
    found = found || findAndClick(ScreenScanner.POINTER_CLOSE4_IMAGE, area, 23, 10, true, true);
    if (found) {
      LOGGER.info("Game probably crashed and needs refresh...");
      refresh();
      runMagic();
    }

    area = new Rectangle(_scanner.getBottomRight().x - 300, _scanner.getBottomRight().y - 125 - 30, _scanner.getGameWidth() - 600, 44 + 40);
    findAndClick(ScreenScanner.POINTER_PUBLISH_IMAGE, area, 23, 10, true, true);

    long t2 = System.currentTimeMillis();
    LOGGER.fine("time: " + (t2 - t1));
  }

  private boolean scanOtherLocations(boolean fast) throws AWTException, IOException, RobotInterruptedException, SessionTimeOutException,
      DragFailureException {
    LOGGER.info("Scanning for locations...");
    Rectangle area = new Rectangle(_scanner.getTopLeft().x + 1, _scanner.getTopLeft().y + 50, 193 + 88, 50);
    if (findAndClick(ScreenScanner.POINTER_LOADING_IMAGE, area, 23, 13, true)) {
      _mouse.delay(300);
      LOGGER.fine("Going to location...");

      loadTrains(fast);
      return true;
    }
    return false;
  }

  private void checkSession() throws SessionTimeOutException {
    if (_scanner.isOptimized()) {
      Pixel p = _scanner.getSessionTimeOut().findImage();
      if (p != null)
        throw new SessionTimeOutException();
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

  private boolean clickHomeOneClick() throws AWTException, IOException, RobotInterruptedException, SessionTimeOutException, DragFailureException {
    long t1 = System.currentTimeMillis();

    boolean trainHasBeenSent = false;
    int timeGiven = 2000; // 2 secs
    long start = System.currentTimeMillis();

    LOGGER.info("looking for pointer down...");

    Pixel p = null;
    boolean done = false;
    long curr = start;
    do {
      _stopThread = false;
      curr = System.currentTimeMillis();
      _mouse.saveCurrentPosition();

      moveIfNecessary();

      p = detectPointerDown();
      if (p != null) {
        checkDangerousZones(p);
        // Pixel p2 = detectPointerDown();
        // if (p2 != null)
        // p = p2;
        _stopThread = true;
        start = System.currentTimeMillis();
        _mouse.saveCurrentPosition();

        int[] rails = _scanner.getRailsHome();

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

        //Try mail again. This time with adjusting
        p.y = _scanner.getBottomRight().y - _scanner.getStreet1Y() - 3;
        clickCareful(p, false, true);
        
        _mouse.delay(250);
        trainHasBeenSent = checkTrainManagement() || trainHasBeenSent;
        _mouse.delay(250);
        scanOtherLocations(true);
        _mouse.delay(250);

        // again all rails one by one now more carefully
        boolean stop = false;
        for (int i = 0; i < rails.length && !stop; i++) {
          try {
            LOGGER.info("trying rail " + (i + 1));
            p.y = _scanner.getBottomRight().y - rails[i] - 4;
            trainHasBeenSent = clickCareful(p, true, false) || trainHasBeenSent;
            _mouse.delay(200);
            trainHasBeenSent = clickCareful(p, true, false) || trainHasBeenSent;
            _mouse.checkUserMovement();

            Pixel pp = detectPointerDown();
            if (pp != null && Math.abs(pp.x - p.x) > 5) {
              stop = true;
              break;
            }
            if (scanOtherLocations(true)) {
              // hmm
              p.x = _scanner.getBottomRight().x - 80;
            }
          } catch (AWTException | IOException e) {
            LOGGER.log(Level.SEVERE, "Critical error occured", e);
          }
        } // for

      } // p != null
      _mouse.checkUserMovement();
    } while (!done && curr - start <= timeGiven);

    long t2 = System.currentTimeMillis();
    LOGGER.fine("time: " + (t2 - t1));

    return trainHasBeenSent;
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
    goHome();
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

    // _mouse.delay(200);
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
      refresh();
      Thread.sleep(20000);
      runMagic();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (RobotInterruptedException e) {
      LOGGER.log(Level.SEVERE, "Interrupted by user5", e);
      e.printStackTrace();
    }
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
      handleRarePopups();
      handlePopups();

      // fixTheGame();
    } catch (SessionTimeOutException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
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

  private void checkDangerousZones(Pixel p) throws RobotInterruptedException, DragFailureException {

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
      if (i < 6) {
        // first 4 zones move left, the rest move right
        diff = p.x - zone.x + 18;
        x1 = _scanner.getBottomRight().x - 50;
      } else {
        diff = zone.x + zone.width - p.x - 20;
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

      Rectangle miniArea = new Rectangle(p.x - 44, p.y - 90, 88, 180);
      Pixel p2 = _scanner.getPointerDown().findImage(miniArea);
      if (p2 != null)
        p.x = p2.x;
      LOGGER.finest("[3] p.x = " + p.x);

      if (!_lastDiffs.offer(diff)) {
        // queue full
        Iterator<Integer> it = _lastDiffs.iterator();
        boolean same = true;
        while (it.hasNext()) {
          Integer d = (Integer) it.next();
          if (Math.abs(d - diff) > 2) {
            same = false;
          }
        }
        if (same) {
          // we have huge problem
          // TODO throw new DragFailureException();

          _lastDiffs.poll();// poll one
          _lastDiffs.offer(diff);// add one

        } else {
          _lastDiffs.poll();// poll one
          _lastDiffs.offer(diff);// add one
        }
      }
    }
  }

  private void moveIfNecessary() throws RobotInterruptedException {

    Pixel p = _scanner.getPointerLeft().findImage();
    if (p != null) {
      LOGGER.info("Found left arrow. moving a bit...");
      // _mouse.mouseMove(p);
      int x1 = _scanner.getTopLeft().x + 5;
      int y = _scanner.getBottomRight().y - Locations.RAIL1;
      _mouse.drag(x1, y, x1 + 640, y);
      // _mouse.delay(500);
    } else {
      p = _scanner.getPointerRight().findImage();
      if (p != null) {
        LOGGER.info("Found right arrow. moving a bit...");
        // _mouse.mouseMove(p);
        int x1 = _scanner.getBottomRight().x - 5;
        int y = _scanner.getBottomRight().y - Locations.RAIL1;
        _mouse.drag(x1, y, x1 - 640, y);
        // _mouse.delay(500);
      } else {
        ImageData pointerDown = _scanner.getPointerDownL();
        Rectangle area = new Rectangle(_scanner.getBottomRight().x - 70, _scanner.getBottomRight().y - Locations.RAIL1 - 150, 70, 150);
        p = findPointerDownInt(area, pointerDown, 4);
        if (p != null) {
          int x1 = _scanner.getBottomRight().x - 5;
          int y = _scanner.getBottomRight().y - Locations.RAIL1;
          _mouse.drag(x1, y, x1 - 240, y);
        }
        if (p == null) {
          pointerDown = _scanner.getPointerDownR();
          area = new Rectangle(_scanner.getTopLeft().x, _scanner.getBottomRight().y - Locations.RAIL1 - 150, 70, 150);
          p = findPointerDownInt(area, pointerDown, 4);
          if (p != null) {
            int x1 = _scanner.getTopLeft().x + 5;
            int y = _scanner.getBottomRight().y - Locations.RAIL1;
            _mouse.drag(x1, y, x1 + 240, y);
          }
        }
      }
    }
  }

  private Pixel detectPointerDown() throws RobotInterruptedException {
    ImageData pointerDown = _scanner.getPointerDown();
    Pixel p = pointerDown.findImage(_scanner.getTrainArea());
    return p;
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
    Pixel tm = _scanner.getPointerTrainManagement().findImage();

    // (ScreenScanner.POINTER_TRAIN_MANAGEMENT_IMAGE, area,
    // time.get_coordinates().x,
    // time.get_coordinates().y, false);

    if (tm != null) {
      // is it freight or express?
      _mouse.delay(200);
      Rectangle area = new Rectangle(tm.x + 287, tm.y + 8, 422 - 287, 113 - 8);
      Pixel exP = _scanner.getExpressTrain().findImage(area);
      boolean isExpress = exP != null;
      if (isExpress) {
        time = _expressTime;
        LOGGER.info("EXPRESS " + time.getTime());
      } else {
        time = _freightTime;
        LOGGER.info("FREIGHT " + time.getTime());
      }
      // 1. ensure the page
      Pixel leftArrow = new Pixel(tm.x - 292, tm.y + 302);
      Pixel rightArrow = new Pixel(tm.x + 393, tm.y + 302);

      _mouse.mouseMove(leftArrow);
      for (int i = 0; i < 3; i++) {
        _mouse.click();
        _mouse.delay(200);
      }
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

      _mouse.delay(200, false); // give chance to change the location without
      _mouse.savePosition(); // stopping the magic.

      if (!_devMode) {
        _mouse.click();
        _stats.registerTrain(isExpress);
        String msg = "Trains: " + _stats.getTotalTrainCount();
        // String date = getNow();
        // _trainsNumberLabel.setText(msg + "  (" + date + ")");
        LOGGER.severe(msg);
        // _trainsNumberLabel.invalidate();
        updateLabels();
        boolean weredone = false;
        int turns = 0;
        do {
          turns++;
          LOGGER.fine("Check TM again " + turns);
          tm = _scanner.getPointerTrainManagement().findImage();
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

}
