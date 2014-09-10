/**
 * 
 */
package com.horowitz.mickey;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Image;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
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

	private final static Logger	LOGGER	      = Logger.getLogger(MainFrame.class.getName());

	private static final String	APP_TITLE	    = "Mickey v0.602";

	private boolean	            _refresh	    = true;
	private boolean	            _devMode	    = false;

	private ScreenScanner	      _scanner;
	private MouseRobot	        _mouse;
	private boolean	            _stopThread	  = false;
	private Pixel	              _lastPointer	= null;
	private boolean	            _foundPointer	= false;
	private int	                _trains;
	private int	                _refreshCount;
	private JLabel	            _trainsNumberLabel;
	private JLabel	            _refreshNumberLabel;
	private MyCanvas	          _myCanvas;
	private JButton	            _locateAction;
	private JButton	            _resetAction;
	private JButton	            _doMagicAction;

	private Location	          _freightTime	= Locations.LOC_10MIN;
	private Location	          _expressTime	= Locations.LOC_30MIN;

	private Long	              _lastTime;
	private Queue<Integer>	    _lastDiffs	  = new ArrayBlockingQueue<Integer>(3);

	private JToggleButton	      _oneClick;

	private Settings	          _settings;

	private JToggleButton	      _refreshClick;

	private boolean isOneClick() {
		return _oneClick.isSelected();
	}

	public MainFrame(Boolean refresh) throws HeadlessException {
		super();
		_settings = new Settings();

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

		Image	image	= null;

		public void paint(Graphics g) {
			super.paint(g);
			if (image != null) {
				g.drawImage(image, 0, 0, null);
			}
		}

	}

	private void init() {
		setTitle(APP_TITLE);

		_trainsNumberLabel = new JLabel("Trains: 0");
		_trainsNumberLabel.setFont(_trainsNumberLabel.getFont().deriveFont(18.0f));
		_refreshNumberLabel = new JLabel("Refresh: " + (_refresh ? "0" : "off"));

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setAlwaysOnTop(true);
		JPanel rootPanel = new JPanel(new BorderLayout());
		getContentPane().add(rootPanel, BorderLayout.CENTER);

		if (_devMode) {
			_myCanvas = new MyCanvas();
			// myCanvas.setMinimumSize(new Dimension(500, 200));
			rootPanel.add(_myCanvas, BorderLayout.EAST);
		}

		final JTextArea outputConsole = new JTextArea(6, 25);

		rootPanel.add(new JScrollPane(outputConsole), BorderLayout.CENTER);
		Box labelsBox = Box.createHorizontalBox();
		labelsBox.add(_trainsNumberLabel);
		labelsBox.add(Box.createHorizontalStrut(4));
		labelsBox.add(_refreshNumberLabel);
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

		JToolBar mainToolbar = new JToolBar();

		JToolBar frToolbar1 = new JToolBar();
		JToolBar frToolbar2 = new JToolBar();
		frToolbar1.add(new JLabel("Cargo      "));
		JToolBar exToolbar1 = new JToolBar();
		JToolBar exToolbar2 = new JToolBar();
		exToolbar1.add(new JLabel("Express  "));

		JPanel toolbars = new JPanel(new GridLayout(6, 1));
		toolbars.add(mainToolbar);
		mainToolbar.setFloatable(false);
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
		toolbars.add(labelsBox);
		rootPanel.add(toolbars, BorderLayout.NORTH);

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
		mainToolbar.add(scanAction);

		// DO MAGIC

		{
			_doMagicAction = new JButton(new AbstractAction("DO MAGIC") {
				public void actionPerformed(ActionEvent e) {
					runMagic();
				}
			});
			mainToolbar.add(_doMagicAction);
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
		mainToolbar.add(runAction);

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
			mainToolbar.add(_locateAction);
		}
		// RESET
		{
			_resetAction = new JButton(new AbstractAction("Reset") {
				public void actionPerformed(ActionEvent e) {
					Thread myThread = new Thread(new Runnable() {
						@Override
						public void run() {
							_trains = 0;
							_trainsNumberLabel.setText("Trains: 0");
							_trainsNumberLabel.invalidate();
							_refreshCount = 0;
							_refreshNumberLabel.setText("Refresh: 0");
							_refreshNumberLabel.invalidate();

						}
					});
					myThread.start();
				}
			});
			mainToolbar.add(_resetAction);
		}
		// OC
		{
			_oneClick = new JToggleButton("OC");
			_oneClick.setSelected(true);
			mainToolbar.add(_oneClick);
		}
		// OC
		{
			_refreshClick = new JToggleButton("R");
			_refreshClick.setSelected(_refresh);
			mainToolbar.add(_refreshClick);
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
						LOGGER.info("selected freight: " + l.getName());
					} else {
						_expressTime = l;
						LOGGER.info("selected express: " + l.getName());
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
			LOGGER.log(Level.SEVERE, "Interrupted by user1", e);
			e.printStackTrace();
		}

	}

	private void refresh() throws RobotInterruptedException {
		LOGGER.info("Time to refresh...");
		_lastTime = System.currentTimeMillis();
		Calendar now = Calendar.getInstance();
		try {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hhmmss");
			String dateStr = df.format(now.getTime());
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			ImageIO.write(new Robot().createScreenCapture(new Rectangle(screenSize)), "PNG", new File("refresh " + dateStr + ".png"));
		} catch (HeadlessException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
			LOGGER.severe(e1.getMessage());
		} catch (AWTException e1) {
			e1.printStackTrace();
		}
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
				LOGGER.info("session time out. Stopping.");
			}

			// fixTheGame();
			_refreshNumberLabel.setText("Refresh: " + (++_refreshCount) + "  (" + getNow() + ")");

			LOGGER.info("Refresh done");
		} catch (AWTException e) {
			e.printStackTrace();
		}

	}

	private void handleRarePopups() throws InterruptedException, RobotInterruptedException {
		LOGGER.info("checking for FB login and daily rewards...");
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
		setTitle(APP_TITLE + " READY AND RUNNING");
		if (_refreshClick.isSelected())
			_refreshNumberLabel.setText("Refresh: " + _refreshCount + "  (" + getNow() + ")");

		int timeForRefresh = (getLongestTime() + 1) * 60000;
		int mandatoryRefresh = _settings.getInt("mandatoryRefresh.time") * 60000;

		long start = System.currentTimeMillis();
		long fstart = System.currentTimeMillis();
		while (true) {
			try {
				goHomeIfNeeded();

				// REFRESH
				if (_refreshClick.isSelected() && timeForRefresh > 60000) {// if "0" chosen no refresh
					long now = System.currentTimeMillis();
					if (now - start >= timeForRefresh) {
						refresh();
						fstart = start = System.currentTimeMillis();
					}

					if (mandatoryRefresh > 0 && now - fstart >= mandatoryRefresh) {
						refresh();
						fstart = start = System.currentTimeMillis();
					}

					// check again has refresh gone well after 3 minutes
					if (_lastTime != null && now - _lastTime >= 3 * 60 * 1000) {
						handleRarePopups();
						_lastTime = null;
					}
				}

				// POPUPS

				handlePopups();

				// HOME
				if (_freightTime.getTime() == 0 && _expressTime.getTime() == 0) {
					// don't send trains
					LOGGER.info("DON'T SEND TRAINS. WAIT 2 seconds...");
					_mouse.saveCurrentPosition();
					_mouse.delay(2000);
				} else {
					boolean flag;
					if (_oneClick.isSelected())
						flag = clickHomeOneClick();
					else
						flag = clickHome();
					if (flag) {
						// true means train has been sent. refresh postponed
						start = System.currentTimeMillis();
					}
				}
				// OTHER LOCATIONS
				scanOtherLocations(false);

				_mouse.delay(200);

			} catch (AWTException | IOException e) {
				LOGGER.severe(e.getMessage());
				e.printStackTrace();
				break;
			} catch (RobotInterruptedException e) {
				LOGGER.log(Level.SEVERE, "Interrupted by user1", e);
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

	private void goHomeIfNeeded() {
		Rectangle area = new Rectangle(_scanner.getTopLeft().x, _scanner.getBottomRight().y - 50, 60, 50);
		Pixel p = _scanner.getHome().findImage(area);
		if (p != null) {
			_mouse.click(p.x, p.y);
		}
	}

	private int getLongestTime() {
		return _freightTime.getTime() > _expressTime.getTime() ? _freightTime.getTime() : _expressTime.getTime();
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
							LOGGER.log(Level.SEVERE, "Interrupted by user2", e);
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
		_mouse.click(_scanner.getTopLeft().x + 26, _scanner.getBottomRight().y - 45);
		_mouse.delay(1500, false);
	}

	private void handlePopups() throws AWTException, IOException, RobotInterruptedException, SessionTimeOutException {
		long t1 = System.currentTimeMillis();

		LOGGER.info("Scanning for popups...");

		// _mouse.savePosition();
		_mouse.mouseMove(_scanner.getBottomRight());
		Rectangle area = new Rectangle(_scanner.getTopLeft().x, _scanner.getTopLeft().y + 380, _scanner.getGameWidth(), 190);
		drawImage(area);

		findAndClick(ScreenScanner.POINTER_CLOSE1_IMAGE, area, 23, 10, true, true);
		findAndClick(ScreenScanner.POINTER_CLOSE3_IMAGE, area, 23, 10, true, true);
		findAndClick(ScreenScanner.POINTER_CLOSE4_IMAGE, area, 23, 10, true, true);

		// SAME area = new Rectangle(0, _scanner.getTopLeft().y + 420,

		findAndClick(ScreenScanner.POINTER_PUBLISH_IMAGE, area, 23, 10, true, true);

		checkSession();

		// _mouse.restorePosition();

		long t2 = System.currentTimeMillis();
		LOGGER.fine("time: " + (t2 - t1));
	}

	private boolean scanOtherLocations(boolean fast) throws AWTException, IOException, RobotInterruptedException, SessionTimeOutException,
	        DragFailureException {
		LOGGER.info("Scanning for locations...");
		Rectangle area = new Rectangle(_scanner.getTopLeft().x + 1, _scanner.getTopLeft().y + 50, 193 + 88, 50);
		drawImage(area);
		if (findAndClick(ScreenScanner.POINTER_LOADING_IMAGE, area, 23, 13, true)) {
			_mouse.delay(300);
			LOGGER.info("Going to location...");

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
		int timeGiven = 3000; // 3 secs
		long start = System.currentTimeMillis();

		LOGGER.info("looking for pointer down...");

		Pixel p = null;
		boolean done = false;
		long curr = start;
		do {
			_stopThread = false;
			curr = System.currentTimeMillis();
			_mouse.saveCurrentPosition();
			// moveIfNecessary();
			p = detectPointerDown();
			if (p != null) {
				checkDangerousZones(p);
				Pixel p2 = detectPointerDown();
				if (p2 != null)
					p = p2;
				_stopThread = true;
				start = System.currentTimeMillis();
				_mouse.saveCurrentPosition();

				// if (clickCareful(p, true, true)) {
				// trainHasBeenSent = true;
				// // ok it is a train and it is sent
				// } else {
				// // wait a bit and click again on the same location
				// _mouse.delay(200);
				// trainHasBeenSent = clickCareful(p, true, true);
				// _mouse.delay(200);
				// }

				// in case of mail hint
				_lastPointer = new Pixel(p.x, p.y);
				_foundPointer = false;
//				if (!isRunning("MAIL")) {
//					Thread myThread = new Thread(new Runnable() {
//						@Override
//						public void run() {
//							if (_lastPointer != null) {
//								try {
//									int maxY = getMaxY(_lastPointer);
//									LOGGER.info("Maxxed pointer is" + _lastPointer);
//									LOGGER.info("maxY=" + maxY);
//									_lastPointer.y = (int) (maxY + _scanner.getRailYOffset() * 2);// TODO
//									_foundPointer = true;
//								} catch (RobotInterruptedException e) {
//									LOGGER.log(Level.SEVERE, "Interrupted by user3", e);
//								}
//							}
//						}
//
//					}, "MAIL");
//
//					myThread.start();
//				}

				int[] rails = _scanner.getRailsHome();

				// fast click all rails + street1 mainly for mail express trains
				for (int i = 0; i < rails.length; i++) {
					p.y = _scanner.getBottomRight().y - rails[i] - 4;
					clickCareful(p, false, false);
				}
				p.y = _scanner.getBottomRight().y - _scanner.getStreet1Y() - 2;
				clickCareful(p, false, false);
				_mouse.delay(250);
				checkTrainManagement();
				_mouse.delay(250);
				scanOtherLocations(true);
				_mouse.delay(250);
				
				// again all rails one by one now more carefully
				boolean stop = false;
				for (int i = 0; i < rails.length && !stop; i++) {
					try {
						LOGGER.info("trying rail " + (i + 1));
						p.y = _scanner.getBottomRight().y - rails[i] - 4;
						clickCareful(p, false, false);
						_mouse.delay(200);
						clickCareful(p, true, false);
						_mouse.checkUserMovement();

						Pixel pp = detectPointerDown();
						if (pp != null && Math.abs(pp.x - p.x) > 5) {
							stop = true;
							break;
						}
//						if (_foundPointer) {
//							LOGGER.info("lastpointer = " + _lastPointer + ",  " + _lastPointer.y + " < "
//							        + (_scanner.getBottomRight().y - rails[rails.length - 1] - _scanner.getRailYOffset()));
//							if (_lastPointer.y < (_scanner.getBottomRight().y - rails[rails.length - 1] - _scanner.getRailYOffset())) {
//								// probably mail
//
//								clickCareful(_lastPointer, true, true);
//								_lastPointer = null;
//								_foundPointer = false;
//							}
//							// clickCareful(_lastPointer, true, true);
//						}
						if (scanOtherLocations(true)) {
							// break;
							_mouse.saveCurrentPosition();
							_mouse.delay(200);
							int diff = 60;
							if (!_lastDiffs.isEmpty()) {
								diff = _lastDiffs.toArray(new Integer[0])[_lastDiffs.size() - 1];
							}
							int x1 = _scanner.getBottomRight().x - 50;
							int y = _scanner.getBottomRight().y - 160;
							_mouse.drag(x1, y, x1 - diff, y);
						}
					} catch (AWTException | IOException e) {
						LOGGER.info("thread stopped");
					}
				} // for

			} // p != null
			_mouse.checkUserMovement();
		} while (!done && curr - start <= timeGiven);
		_foundPointer = false;
		_lastPointer = null;

		long t2 = System.currentTimeMillis();
		LOGGER.fine("time: " + (t2 - t1));

		return trainHasBeenSent;
	}

	private boolean isRunning(String threadName) {
		boolean isRunning = false;
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		for (Iterator<Thread> it = threadSet.iterator(); it.hasNext();) {
			Thread thread = it.next();
			if (thread.getName().equals("LOADTRAINS")) {
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
				_mouse.delay(200);
				int diff = 60;
				int x1 = _scanner.getBottomRight().x - 50;
				int y = _scanner.getBottomRight().y - 160;
				_mouse.drag(x1, y, x1 - diff, y);
				// click directly to a safe zone
				int[] rails = _scanner.getRailsOut();
				int xx = _scanner.getBottomRight().x - 131;
				for (int i = 0; i < rails.length; i++) {
					_mouse.click(xx, _scanner.getBottomRight().y - rails[i] - 4);
				}
				// that's it!
			} else {

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
								LOGGER.info("session time out");
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

						// System.err.println(Thread.currentThread().getName() + " " +
						// Thread.currentThread().isInterrupted() + " "
						// + Thread.interrupted());

						// if (now - start > 3000) {
						// LOGGER.info("STOOOOP");
						// // myThread.interrupt();
						// _stopThread = true;
						// goHome();
						// return;
						// }
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

	private void locate() throws RobotInterruptedException, AWTException, IOException {

		// try {
		// handlePopups();
		//
		//
		// // fixTheGame();
		// } catch (SessionTimeOutException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//

		goHomeIfNeeded();

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

		// LOGGER.info("p.x = " + p.x);

		// moving to left
		Rectangle zone = _scanner.getDangerousZones()[0];
		if (p.x >= zone.x && p.x <= zone.x + zone.width) {
			// uh oh!!!
			int diff = p.x - zone.x + 23;
			int x1 = _scanner.getBottomRight().x - 50;
			int y = _scanner.getBottomRight().y - 160;
			LOGGER.info("avoid zone [" + zone.x + " - " + (zone.x + zone.width));
			LOGGER.info("drag " + diff);
			_mouse.drag(x1, y, x1 - diff, y);
			_mouse.saveCurrentPosition();
			p.x = p.x - diff;
			Rectangle miniArea = new Rectangle(p.x - 44, p.y - 90, 88, 180);
			Pixel p2 = _scanner.getPointerDown().findImage(miniArea);
			if (p2 != null)
				p.x = p2.x;

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

		// moving to left
		zone = _scanner.getDangerousZones()[1];
		if (p.x >= zone.x && p.x <= zone.x + zone.width) {
			// uh oh!!!
			int diff = p.x - zone.x + 23;
			int x1 = _scanner.getBottomRight().x - 5;
			int y = _scanner.getBottomRight().y - 160;
			LOGGER.info("avoid zone2 [" + zone.x + " - " + (zone.x + zone.width));
			LOGGER.info("drag " + diff);
			_mouse.drag(x1, y, x1 - diff, y);
			p.x = p.x - diff;
		}

		// moving to right
		zone = _scanner.getDangerousZones()[2];
		if (p.x >= zone.x && p.x <= zone.x + zone.width) {
			// uh oh!!!
			int diff = zone.x + zone.width - p.x + 23;
			int x1 = _scanner.getTopLeft().x + 5;
			int y = _scanner.getBottomRight().y - 160;
			LOGGER.info("avoid zone1 [" + zone.x + " - " + (zone.x + zone.width));
			LOGGER.info("drag " + diff);
			_mouse.drag(x1, y, x1 + diff, y);
			p.x = p.x + diff;
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
		Rectangle area = new Rectangle(_scanner.getTopLeft().x + 305, _scanner.getTopLeft().y + 47, 450 + 130, 90);
		drawImage(area);
		// Location time = _times[getTimeIndex()];
		Location time = _freightTime;
		Pixel tm = _scanner.getPointerTrainManagement().findImage();

		// (ScreenScanner.POINTER_TRAIN_MANAGEMENT_IMAGE, area,
		// time.get_coordinates().x,
		// time.get_coordinates().y, false);

		if (tm != null) {
			// is it freight or express?
			_mouse.delay(200);
			area = new Rectangle(tm.x + 287, tm.y + 8, 422 - 287, 113 - 8);
			Pixel exP = _scanner.getExpressTrain().findImage(area);
			drawImage(area);

			if (exP != null) {
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
				String msg = "Trains: " + (++_trains);
				String date = getNow();
				_trainsNumberLabel.setText(msg + "  (" + date + ")");
				LOGGER.severe(msg);
				_trainsNumberLabel.invalidate();
				boolean weredone = false;
				int turns = 0;
				do {
					turns++;
					LOGGER.info("Check TM again " + turns);
					tm = _scanner.getPointerTrainManagement().findImage();
					if (tm != null) {
						_mouse.delay(300);
					} else {
						// we're done
						weredone = true;
						LOGGER.info("Check TM again DONE ");
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

	private void drawImage(Rectangle area) {
		if (_devMode)
			try {
				BufferedImage screen = new Robot().createScreenCapture(area);
				_myCanvas.image = screen;
				_myCanvas.repaint();
			} catch (AWTException e) {
				LOGGER.severe(e.getMessage());
				e.printStackTrace();
			}

	}

}
