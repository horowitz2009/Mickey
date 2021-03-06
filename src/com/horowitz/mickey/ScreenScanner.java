package com.horowitz.mickey;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

public class ScreenScanner {

  private final static Logger  LOGGER                         = Logger.getLogger(ScreenScanner.class.getName());

  private static final boolean DEBUG                          = false;

  public static final String   ANCHOR_IMAGE                   = "anchorBill.bmp";
  public static final String   ANCHOR_TOPLEFT_IMAGE           = "anchorTopLeft.bmp";
  public static final String   POINTER_DOWN_IMAGE             = "pointerDown3.bmp";
  public static final String   POINTER_DOWN_IMAGE_LEFT        = "pointerDown3l.bmp";
  public static final String   POINTER_DOWN_IMAGE_RIGHT       = "pointerDown3r.bmp";
  public static final String   POINTER_CLOSE1_IMAGE           = "close1.png";
  public static final String   POINTER_CLOSE2_IMAGE           = "close2.png";
  public static final String   POINTER_CLOSE3_IMAGE           = "close3.bmp";
  public static final String   POINTER_CLOSE4_IMAGE           = "close4.bmp";
  public static final String   POINTER_PUBLISH_IMAGE          = "publish.png";
  public static final String   POINTER_DAILY_IMAGE            = "daily.png";
  public static final String   POINTER_LOADING_IMAGE          = "loading.png";
  public static final String   POINTER_TRAIN_MANAGEMENT_IMAGE = "trainManagement.png";
  public static final String   POINTER_NIGHTX                 = "nightX.bmp";
  public static final String   POINTER_DAYLIGHTX              = "daylightX.bmp";
  public static final String   POINTER_TIPSX                  = "tipsX.bmp";
  public static final String   POINTER_PROMOX                 = "promoX.bmp";

  private ImageComparator      _comparator;

  private Pixel                _br                            = null;
  private Pixel                _tl                            = null;
  private boolean              _fullyOptimized                = false;

  private Pixel                _zoomOutPixel                  = null;
  private Pixel                _zoomInPixel                   = null;

  private ImageData            _home                          = null;
  private ImageData            _close1                        = null;
  private ImageData            _close3                        = null;
  private ImageData            _close4                        = null;
  private ImageData            _pointerDown                   = null;
  private ImageData            _pointerDownL                  = null;
  private ImageData            _pointerDownR                  = null;
  private ImageData            _nightX                        = null;
  private ImageData            _daylightY                     = null;
  private ImageData            _promoX                        = null;

  private ImageData            _trainManagement               = null;

  private ImageData            _topLeftImage                  = null;

  private ImageData            _pointerLeft                   = null;
  private ImageData            _pointerRight                  = null;

  private ImageData            _sessionTimeOut;

  private Rectangle            _homeArea                      = null;
  private Rectangle            _trainArea                     = null;

  private int[]                _railsOut;
  private int[]                _railsHome;

  private float                _railYOffset                   = 5;
  private int                  _xOffset                       = 22;

  private ImageData            _loginWIthFB;

  private ImageData            _dailyRewards;

  private Pixel                _topPlayersPixel;

  private Settings             _settings;

  private Rectangle[]          _dangerousZones;

  private ImageData            _expressTrain;

  private int                  _street1Y                      = 170;

  public ScreenScanner(Settings settings) {
    _settings = settings;
    _comparator = new SimilarityImageComparator(0.04, 20);

    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Rectangle area = new Rectangle(20, 340, screenSize.width - 20 - 404, screenSize.height - 340 - 110);
    try {
      _loginWIthFB = new ImageData("loginWithFB2.bmp", area, _comparator, 8, 8);

      area = new Rectangle(216, 10, screenSize.width - 216 - 286, screenSize.height - 10 - 222);
      _dailyRewards = new ImageData("dailyX.bmp", area, _comparator, 9, 9);
      
      area = new Rectangle(576, 88, screenSize.width - 576 - 250, screenSize.height/2 - 88);
      _promoX = new ImageData(POINTER_PROMOX, area, _comparator, 14, 14);

      _tl = new Pixel(0, 0);
      _br = new Pixel(screenSize.width - 3, screenSize.height - 3);
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  private void setKeyAreas() throws IOException {
    String zoom = _settings.getProperty("zoom");

    _zoomInPixel = new Pixel(_br.x - 29, _tl.y + 136);
    _zoomOutPixel = new Pixel(_br.x - 29, _tl.y + 161);
    _topPlayersPixel = new Pixel(_br.x - 24, _br.y - 23);

    // pointerDown
    _home = new ImageData("home.bmp", null, _comparator, 17, 0);
    _close1 = new ImageData(POINTER_CLOSE1_IMAGE, null, _comparator, 23, 10);
    _close3 = new ImageData(POINTER_CLOSE3_IMAGE, null, _comparator, 23, 10);
    _close4 = new ImageData(POINTER_CLOSE4_IMAGE, null, _comparator, 23, 10);

    _pointerDown = new ImageData("pointerDown5.bmp", null, _comparator, 15, 21);
    _pointerDownL = new ImageData("pointerDown5l.bmp", null, _comparator, 18, 21);
    _pointerDownR = new ImageData("pointerDown5r.bmp", null, _comparator, 4, 21);
    _expressTrain = new ImageData("expressTrain.bmp", null, _comparator, 0, 0);
    // _nightX = new ImageData("nightX.bmp", null, _comparator, 8, 8);
    // _daylightY = new ImageData("daylightX.bmp", null, _comparator, 8, 8);

    Rectangle area = new Rectangle(_tl.x + 305, _tl.y + 47, 450 + 130, 90);
    _trainManagement = new ImageData(POINTER_TRAIN_MANAGEMENT_IMAGE, area, _comparator, 0, 0);
    // top left image is used to determine whether the train is express
    _topLeftImage = new ImageData(ANCHOR_TOPLEFT_IMAGE, null, _comparator, 0, 0);

    _fullyOptimized = true;

    int xx = (getGameWidth() - 275) / 2;
    int yy = (getGameHeight() - 386) / 2;
    area = new Rectangle(_tl.x + xx, _tl.y + yy + 50, 275, 75); // TODO to be widen if not working
    _sessionTimeOut = new ImageData("session.bmp", area, _comparator, 0, 0);

    if (getGameWidth() > 900) {
      zoom = "" + (Integer.parseInt(zoom) + 2);
      LOGGER.info("ZOOM 4!!!!!");
    }
    _railsOut = _settings.getArray("railsOut" + zoom);// Locations.RAILS4_SMIX;
    _railsHome = _settings.getArray("railsHome" + zoom);// Locations.RAILS4A;
    _railYOffset = _settings.getInt("railYOffset" + zoom);// Locations.RAIL_Y_OFFSET;
    _xOffset = _settings.getInt("xOffset" + zoom);// 22;
    _street1Y = _settings.getInt("street1Y");

    _homeArea = new Rectangle(_tl.x, _br.y - 44 - 154, 70, 154);

    _trainArea = new Rectangle(_tl.x, _br.y - getRailsHome()[0] - 110, getGameWidth(), 110);

    int diff = getGameWidth() - 760;
    diff = diff / 2;
    int x = _br.x - diff + 1;
    diff = getGameHeight() - 550;
    diff = diff / 2;
    int y = _br.y - diff + 1;
    Pixel brTM = new Pixel(x - 74, y - 74);

    diff = getGameWidth() - 781;
    diff = diff / 2;
    x = _br.x - diff + 1;
    diff = getGameHeight() - 551;
    diff = diff / 2;
    y = _br.y - diff + 1;
    Pixel brShop = new Pixel(x - 74, y - 74);
    _dangerousZones = new Rectangle[] { _settings.getArea("zone3", brTM),    _settings.getArea("zone3b", brShop), 
                                        _settings.getArea("zone2a", brTM),   _settings.getArea("zone2", brTM), 
                                        _settings.getArea("zone2b", brShop), _settings.getArea("zone1a", brTM), 
                                        _settings.getArea("zone1", brTM),    _settings.getArea("zone1b", brShop) };

    area = new Rectangle(_tl.x, _br.y - getRailsHome()[0] - 195, 130, 195);// 130
    _pointerLeft = new ImageData("pointerLeft.bmp", area, _comparator, 0, 0);

    area = new Rectangle(_br.x - 130, _br.y - getRailsHome()[0] - 195, 130, 195);
    _pointerRight = new ImageData("pointerRight.bmp", area, _comparator, 0, 0);

  }

  public Pixel getTopPlayersPixel() {
    return _topPlayersPixel;
  }

  public boolean locateGameArea() throws AWTException, IOException, RobotInterruptedException {
    LOGGER.fine("Locating game area ... ");

    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int w = 200, h = 150;
    int wm = 20, hm = 20;

    List<Rectangle> rects = new ArrayList<Rectangle>(5);
    rects.add(new Rectangle(0, 170, 50, 50));
    rects.add(new Rectangle(0, 170, 50, 180));
    rects.add(new Rectangle(0, 0, 300, 350));
    int hturns = 1 + (screenSize.width - 600) / (w - wm);
    int vturns = 1 + (screenSize.height - 500) / (h - hm);
    int x = 0, y = 0;
    for (int row = 0; row < vturns; row++) {
      x = 0;
      for (int col = 0; col < hturns; col++) {
        Rectangle r = new Rectangle(x, y, w, h);
        rects.add(r);
        x += w - wm;
      }
      y += h - hm;
    }

    Rectangle[] areaTL = rects.toArray(new Rectangle[0]);

    // scroll a bit up
    boolean done = false;
    int turns = 0;
    do {
      turns++;
      _tl = locateImageCoords(ANCHOR_TOPLEFT_IMAGE, areaTL, -9, -5);
      if (_tl != null) {
        Rectangle[] areaBR = new Rectangle[] { new Rectangle(screenSize.width - 379, screenSize.height - 270, 113, 100),
            new Rectangle(_tl.x + 684, _tl.y + 543, 300, 100), new Rectangle(_tl.x + 684, _tl.y + 543, screenSize.width - 270 - 684, 100),
            new Rectangle(684, 607, screenSize.width - 270 - 684, screenSize.height - 607), new Rectangle(screenSize) };
        LOGGER.info("Found top left corner...");
        // good, we're still in the game
        _br = locateImageCoords(ANCHOR_IMAGE, areaBR, 79, 20);
        if (_br != null) {
          LOGGER.info("Found bottom right corner...");
          done = true;
        } else {
          LOGGER.info("Move a bit and try again...");
          Pixel p = new Pixel(_tl.x, _tl.y - 2);
          MouseRobot mouse = new MouseRobot();
          mouse.mouseMove(p.x, p.y);
          mouse.saveCurrentPosition();
          mouse.click();
          mouse.delay(200);
          Robot robot = new Robot();

          robot.keyPress(40);// arrow down
          mouse.delay(1000);
        }
      }

    } while (!done && turns < 3);

    if (_br != null && _tl != null) {
      setKeyAreas();
      return true;
    } else {
      _tl = new Pixel(0, 0);
      _br = new Pixel(1600, 1000);
    }
    return false;
  }

  public void writeImage(Rectangle rect, String filename) {
    try {
      ImageIO.write(new Robot().createScreenCapture(rect), "PNG", new File(filename));
    } catch (IOException e) {
      e.printStackTrace();
    } catch (AWTException e) {
      e.printStackTrace();
    }
  }

  public void captureGame() {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");
    String date = sdf.format(Calendar.getInstance().getTime());
    String filename = "popup_" + date + ".png";
    captureGame(filename);
  }
  
  public void captureGame(String filename) {
    writeImage(new Rectangle(new Point(_tl.x, _tl.y), new Dimension(getGameWidth(), getGameHeight())), filename);
  }

  public Pixel locateImageCoords(String imageName, Rectangle[] area, int xOff, int yOff) throws AWTException, IOException, RobotInterruptedException {

    final Robot robot = new Robot();
    final BufferedImage image = ImageIO.read(ImageManager.getImageURL(imageName));
    Pixel[] mask = new ImageMask(imageName).getMask();
    BufferedImage screen;
    int turn = 0;
    Pixel resultPixel = null;
    // MouseRobot mouse = new MouseRobot();
    // mouse.saveCurrentPosition();
    while (turn < area.length) {

      screen = robot.createScreenCapture(area[turn]);
      Map<Integer, Color[]> map = null;
      if (imageName.equals("pointerLeft.bmp")) {
        map = new Hashtable<Integer, Color[]>(3);
        map.put(1, new Color[] { new Color(243, 253, 254) });
        map.put(2, new Color[] { new Color(45, 143, 25) });
        map.put(3, new Color[] { new Color(255, 180, 61) });
      }

      List<Pixel> foundEdges = findEdge(image, screen, _comparator, map, mask);
      if (foundEdges.size() >= 1) {
        // found
        // AppConsole.print("found it! ");
        int y = area[turn].y;
        int x = area[turn].x;
        resultPixel = new Pixel(foundEdges.get(0).x + x + xOff, foundEdges.get(0).y + y + yOff);
        // System.err.println("AREA: [" + turn + "] " + area[turn]);
        break;
      }
      turn++;
    }
    // mouse.checkUserMovement();
    // AppConsole.println();
    return resultPixel;
  }

  public boolean isOptimized() {
    return _fullyOptimized && _br != null && _tl != null;
  }

  public Pixel getZoomOutPixel() {
    return _zoomOutPixel;
  }

  public Pixel getZoomInPixel() {
    return _zoomInPixel;
  }

  private List<Pixel> findEdge(final BufferedImage targetImage, final BufferedImage area, ImageComparator comparator, Map<Integer, Color[]> colors,
      Pixel[] indices) {
    if (DEBUG)
      try {
        ImageIO.write(area, "PNG", new File("C:/area.png"));
      } catch (IOException e) {
        e.printStackTrace();
      }
    List<Pixel> result = new ArrayList<Pixel>(8);
    for (int i = 0; i < (area.getWidth() - targetImage.getWidth()); i++) {
      for (int j = 0; j < (area.getHeight() - targetImage.getHeight()); j++) {
        final BufferedImage subimage = area.getSubimage(i, j, targetImage.getWidth(), targetImage.getHeight());
        if (DEBUG)
          try {
            ImageIO.write(subimage, "PNG", new File("C:/subimage.png"));
          } catch (IOException e) {
            e.printStackTrace();
          }
        if (comparator.compare(targetImage, subimage, colors, indices)) {
          // System.err.println("FOUND: " + i + ", " + j);
          result.add(new Pixel(i, j));
          if (result.size() > 0) {// increase in case of trouble
            break;
          }
        }
      }
    }
    return result;
  }

  public void scan() {
    try {
      Robot robot = new Robot();

      BufferedImage screenshot = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
      if (DEBUG)
        ImageIO.write(screenshot, "PNG", new File("screenshot.png"));
    } catch (HeadlessException | AWTException | IOException e) {

      e.printStackTrace();
    }

  }

  public void compare(String imageName1, String imageName2) throws IOException {
    final BufferedImage image1 = ImageIO.read(ImageManager.getImageURL(imageName1));
    Pixel[] mask1 = new ImageMask(imageName1).getMask();
    final BufferedImage image2 = ImageIO.read(ImageManager.getImageURL(imageName2));
    Pixel[] mask2 = new ImageMask(imageName2).getMask();

    List<Pixel> res = compareImages(image1, image2, _comparator, mask2);

    // System.err.println(res);
  }

  public List<Pixel> compareImages(final BufferedImage image1, final BufferedImage image2, ImageComparator comparator, Pixel[] indices) {
    if (DEBUG)
      try {
        ImageIO.write(image2, "PNG", new File("area.png"));
      } catch (IOException e) {
        e.printStackTrace();
      }
    List<Pixel> result = new ArrayList<Pixel>(8);
    for (int i = 0; i <= (image2.getWidth() - image1.getWidth()); i++) {
      for (int j = 0; j <= (image2.getHeight() - image1.getHeight()); j++) {
        final BufferedImage subimage = image2.getSubimage(i, j, image1.getWidth(), image1.getHeight());
        if (DEBUG)
          try {
            ImageIO.write(subimage, "PNG", new File("subimage.png"));
          } catch (IOException e) {
            e.printStackTrace();
          }

        boolean b = comparator.compare(image1, image2, null, indices);
        // System.err.println("equal: " + b);
        indices = null;
        b = comparator.compare(image1, image2, null, indices);
        // System.err.println("equal2: " + b);
        List<Pixel> list = comparator.findSimilarities(image1, subimage, indices);
        // System.err.println("FOUND: " + list);
      }
    }
    return result;
  }

  public Rectangle getTrainArea() {
    return _trainArea;
  }

  public int getXOffset() {
    return _xOffset;
  }

  public float getRailYOffset() {
    return _railYOffset;
  }

  public int[] getRailsOut() {
    return _railsOut;
  }

  public int[] getRailsHome() {
    return _railsHome;
  }

  public Pixel getBottomRight() {
    return _br;
  }

  public Pixel getTopLeft() {
    return _tl;
  }

  public int getGameWidth() {
    int width = isOptimized() ? _br.x - _tl.x : Toolkit.getDefaultToolkit().getScreenSize().width;
    return width != 0 ? width : Toolkit.getDefaultToolkit().getScreenSize().width;
  }

  public int getGameHeight() {
    if (isOptimized()) {
      return _br.y - _tl.y == 0 ? Toolkit.getDefaultToolkit().getScreenSize().height : _br.y - _tl.y;
    } else {
      return Toolkit.getDefaultToolkit().getScreenSize().height;
    }
  }

  public ImageData getPointerRight() {
    return _pointerRight;
  }

  public ImageData getPointerDown() {
    return _pointerDown;
  }

  public ImageData getPointerDownR() {
    return _pointerDownR;
  }

  public ImageData getPointerDownL() {
    return _pointerDownL;
  }

  public ImageData getPointerLeft() {
    return _pointerLeft;
  }

  public Rectangle getHomeArea() {
    return _homeArea;
  }

  public ImageData getLoginWIthFB() {
    return _loginWIthFB;
  }

  public ImageData getDailyRewards() {
    return _dailyRewards;
  }

  public ImageData getSessionTimeOut() {
    return _sessionTimeOut;
  }

  public void addHandler(Handler handler) {
    LOGGER.addHandler(handler);
  }

  public ImageData getTopLeftImage() {
    return _topLeftImage;
  }

  public ImageData getPointerTrainManagement() {
    return _trainManagement;
  }

  public Rectangle[] getDangerousZones() {
    return _dangerousZones;
  }

  public ImageData getExpressTrain() {
    return _expressTrain;
  }

  public ImageData getClose1() {
    return _close1;
  }

  public ImageData getClose3() {
    return _close3;
  }

  public ImageData getClose4() {
    return _close4;
  }

  public ImageData getHome() {
    return _home;
  }

  public int getStreet1Y() {
    return _street1Y;
  }

  public ImageData getNightX() {
    return _nightX;
  }

  public ImageData getDaylightY() {
    return _daylightY;
  }
  
  public ImageData getPromoX() {
    return _promoX;
  }

}
