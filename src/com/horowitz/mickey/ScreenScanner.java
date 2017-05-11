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
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;

import javax.imageio.ImageIO;

import com.horowitz.commons.BaseScreenScanner;
import com.horowitz.commons.ImageComparator;
import com.horowitz.commons.ImageData;
import com.horowitz.commons.MouseRobot;
import com.horowitz.commons.Pixel;
import com.horowitz.commons.RobotInterruptedException;
import com.horowitz.commons.Settings;
import com.horowitz.commons.SimilarityImageComparator;
import com.horowitz.mickey.common.MyImageIO;
import com.horowitz.mickey.scanner.KeyboardRobot;

public class ScreenScanner extends BaseScreenScanner {

  public static final String  SELL_X                         = "sellX.bmp";
  public static final String  SHOP_X                         = "shopX.bmp";
  public static final String  CLOSE_X2                       = "closeX2.bmp";

  public static final String  LOGIN_WITH_FB                  = "FBLogin.bmp";
  private static final String INT_TRAINS                     = "int/Trains.bmp";
  private static final String SHARE                          = "share.bmp";
  private static final String MATERIALS                      = "materials.bmp";
  private static final String CONTRACTS                      = "contracts.bmp";
  private static final String POINTER_RIGHT_BLUE             = "pointerRightBlue.bmp";
  private static final String POINTER_LEFT_BLUE              = "pointerLeftBlue.bmp";
  public static final String  CLOSE_X                        = "closeX.bmp";
  // private static final String LETTERS_WHITE2 = "letters/white8x5.bmp";
  // private static final String LETTERS_PINK = "letters/pink8x5.bmp";
  private static final String LETTERS_BROWN                  = "letters/brown8x5.bmp";
  private static final String LETTERS_WHITE                  = "letters/white8x5.bmp";
  private static final String LETTERS_RED                    = "letters/red8x5.bmp";
  private static final String EXPRESS_TRAIN                  = "expressTrain3.bmp";
  private static final String FREE_TRAIN                     = "free2.bmp";
  private static final String XP_TRAIN                       = "xpTrain2.bmp";
  private static final String SESSION                        = "session2.bmp";

  public static final String  POINTER_DOWN_IMAGE             = "pointerDownBlue.bmp";
  public static final String  POINTER_DOWN_IMAGE_LEFT        = "pointerDownBlueL.bmp";
  public static final String  POINTER_DOWN_IMAGE_RIGHT       = "pointerDownBlueR.bmp";

  public static final String  ANCHOR_IMAGE                   = "anchorInvite2.bmp";

  public static final String  POINTER_CLOSE_IMAGE            = "Close.bmp";
  public static final String  POINTER_CLOSE1_IMAGE           = "close1.png";
  public static final String  POINTER_CLOSE2_IMAGE           = "close2.png";
  public static final String  POINTER_CLOSE3_IMAGE           = "close3.bmp";
  public static final String  POINTER_CLOSE4_IMAGE           = "close4.bmp";
  public static final String  POINTER_CANCEL_IMAGE           = "cancel.bmp";
  public static final String  POINTER_LOADING_IMAGE          = "loading.bmp";
  public static final String  POINTER_TRAIN_MANAGEMENT_IMAGE = "dispa.bmp";

  private ImageData           _home                          = null;
  private ImageData           _close1                        = null;
  private ImageData           _close3                        = null;
  private ImageData           _close4                        = null;
  private ImageData           _pointerDown                   = null;

  public ImageData            _letterRed                     = null;
  public ImageData            _letterWhite                   = null;
  public ImageData            _letterBrown                   = null;
  public ImageData            _letterRed2                    = null;
  public ImageData            _letterWhite2                  = null;
  public ImageData            _letterBrown2                  = null;
  public ImageData            _letterRed3                    = null;
  public ImageData            _letterWhite3                  = null;
  public ImageData            _letterWhite4                  = null;
  public ImageData            _letterBrown3                  = null;
  public ImageData            _letterPink3                   = null;
  public ImageData            _package1                      = null;
  public ImageData            _package2                      = null;

  private ImageData           _pointerDownL                  = null;
  private ImageData           _pointerDownR                  = null;
  private ImageData           _nightX                        = null;
  private ImageData           _daylightY                     = null;
  private ImageData           _promoX                        = null;
  private ImageData           _share                         = null;

  private ImageData           _trainManagementAnchor         = null;
  private ImageData           _sixMinutes                    = null;
  private ImageData           _maglevDest                    = null;
  private ImageData           _trainsAnchor                  = null;
  private ImageData           _materials                     = null;
  private ImageData           _materialsButton               = null;
  private ImageData           _goldIcon                      = null;

  // private ImageData _topLeftImage = null;

  private ImageData           _pointerLeft                   = null;
  private ImageData           _pointerRight                  = null;

  private ImageData           _sessionTimeOut                = null;
  private ImageData           _syncError                     = null;

  private ImageData           _contracts                     = null;

  private Rectangle           _homeArea                      = null;
  private Rectangle           _trainArea                     = null;
  private Rectangle           _letterArea                    = null;
  private Rectangle           _packagesArea                  = null;

  private ImageData           _loginWithFB;

  private ImageData           _invite;
  private ImageData           _dailyRewards;

  private Pixel               _topPlayersPixel;

  private ImageData           _expressTrain;
  private ImageData           _freeTrain;
  private ImageData           _xpTrain;
  private Rectangle           _passengersArea;

  private int                 _street1Y                      = 170;
  private Pixel               _whistlesPoint;
  public int                  _offset                        = 113;
  public Pixel                _resendP;
  private Rectangle           _resendArea;

  public ScreenScanner(Settings settings) {
    super(settings);

    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Rectangle area = new Rectangle(600, 200, screenSize.width - 20 - 600, 320);
    try {
      _loginWithFB = new ImageData(LOGIN_WITH_FB, area, _comparator, 8, 8);

      area = new Rectangle(187, 233, screenSize.width - 187 - 187, screenSize.height - 233 - 17);

      getImageData("Sure.bmp", area, 15, 5);

      area = new Rectangle(100, 122, screenSize.width - 200, 500);
      _invite = new ImageData("Invite.bmp", area, _comparator, 583, 5);

      area = new Rectangle(450, 270, screenSize.width - 450, screenSize.height - 270);
      getImageData("FBShare.bmp", area, 15, 5);

      area = new Rectangle(screenSize.width / 2, 10, screenSize.width / 2 - 120, screenSize.height / 2 - 121);
      getImageData(ScreenScanner.SHOP_X, area, 9, 9);
      area = new Rectangle(576, 88, screenSize.width - 576 - 250, screenSize.height / 2 - 88);
      getImageData(CLOSE_X, area, 14, 14);
      getImageData(CLOSE_X2, area, 14, 14);

      _tl = new Pixel(0, 0);
      _br = new Pixel(screenSize.width - 3, screenSize.height - 3);

      area = new Rectangle(0, 0, 250, 87);
      _contracts = new ImageData(CONTRACTS, area, _comparator, 1, 0);

      area = new Rectangle(_tl.x + 72, _tl.y + 423, screenSize.width - 72, screenSize.height - 423);

    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  private void setKeyAreas() throws IOException {

    _topPlayersPixel = new Pixel(_br.x - 24, _br.y - 23);

    // pointerDown
    _home = new ImageData("home.bmp", null, _comparator, 11, 12);
    _close1 = new ImageData(POINTER_CLOSE1_IMAGE, null, _comparator, 23, 10);
    _close3 = new ImageData(POINTER_CLOSE3_IMAGE, null, _comparator, 23, 10);
    _close4 = new ImageData(POINTER_CLOSE4_IMAGE, null, _comparator, 23, 10);

    _pointerDown = new ImageData(POINTER_DOWN_IMAGE, null, _comparator, 13, 19);
    _letterRed3 = new ImageData(LETTERS_RED, null, new SimilarityImageComparator(0.04, 2000), 4, 2);
    _letterWhite3 = new ImageData(LETTERS_WHITE, null, new SimilarityImageComparator(0.04, 2000), 4, 2);
    _letterBrown3 = new ImageData(LETTERS_BROWN, null, new SimilarityImageComparator(0.04, 2000), 4, 2);
    // _letterPink3 = new ImageData(LETTERS_PINK, null, new SimilarityImageComparator(0.04, 3000), 5, 3);
    // _letterWhite4 = new ImageData(LETTERS_WHITE2, null, new SimilarityImageComparator(0.04, 3000), 4, 3);

    _pointerDownL = new ImageData(POINTER_DOWN_IMAGE_LEFT, null, _comparator, 13, 19);
    _pointerDownR = new ImageData(POINTER_DOWN_IMAGE_RIGHT, null, _comparator, 1, 19);

    SimilarityImageComparator specialComparator = new SimilarityImageComparator(0.08, 4000);
    _expressTrain = new ImageData(EXPRESS_TRAIN, null, specialComparator, 0, 0);
    _freeTrain = new ImageData(FREE_TRAIN, null, specialComparator, 0, 0);
    _xpTrain = new ImageData(XP_TRAIN, null, specialComparator, 0, 0);
    // _nightX = new ImageData("nightX.bmp", null, _comparator, 8, 8);
    // _daylightY = new ImageData("daylightX.bmp", null, _comparator, 8, 8);

    _optimized = true;

    int xx = (getGameWidth() - 780) / 2;
    int yy = (getGameHeight() - 585) / 2;
    Rectangle area = new Rectangle(_tl.x + xx + 53, _tl.y + yy + 22, 171, 24);
    _trainManagementAnchor = new ImageData(POINTER_TRAIN_MANAGEMENT_IMAGE, area, _comparator, -53, 32);// was -46
    _sixMinutes = new ImageData("sixMinutes3.bmp", null, _comparator, 0, 0);
    _maglevDest = new ImageData("maglevDest.bmp", null, _comparator, 0, 0);
    // top left image is used to determine whether the train is express
    // _topLeftImage = new ImageData(ANCHOR_TOPLEFT_IMAGE1, null, _comparator, -24, 46);

    // SESSION
    xx = (getGameWidth() - 523) / 2;
    yy = (getGameHeight() - 281) / 2;
    area = new Rectangle(_tl.x + xx + 80, _tl.y + yy + 80, 120, 50);
    _sessionTimeOut = new ImageData(SESSION, area, _comparator, 0, 0);
    _syncError = new ImageData("sync.bmp", area, _comparator, 0, 0);
    xx = (getGameWidth() - 125) / 2;
    area = new Rectangle(_tl.x + xx, _tl.y + 162, 125, 50);
    getImageData("levelup.bmp", area, 0, 0);
    
    xx = (getGameWidth() - 523) / 2;
    area = new Rectangle(_tl.x + xx + 205, _tl.y + 172, 136, 50);
    getImageData("storageFull.bmp", area, 0, 0);

    // SHOP X
    xx = (getGameWidth() - 780) / 2;
    area = new Rectangle(_br.x - xx - 42, _tl.y + 24, xx + 42, 42);
    getImageData(ScreenScanner.SHOP_X, area, 9, 9);
    // PROMO
    xx = (getGameWidth() - 747) / 2;
    area = new Rectangle(_br.x - xx - 60, _tl.y + 24, 70, 180);
    getImageData(CLOSE_X, area, 7, 7);

    xx = (getGameWidth() - 520) / 2;
    area = new Rectangle(_br.x - xx - 60, _tl.y + 24, xx - 40, 240);
    getImageData(CLOSE_X2, area, 7, 7);

    xx = (getGameWidth() - 563) / 2;
    area = new Rectangle(_tl.x + xx + 522, _tl.y + 210, 50, 50);
    getImageData(SELL_X, area, 7, 7);

    _street1Y = _settings.getInt("street1Y", 204);

    _homeArea = new Rectangle(_tl.x, _br.y - 44 - 154, 70, 154);

    _trainArea = new Rectangle(_tl.x, _br.y - getRailsHome()[0] - 150, getGameWidth(), 150);
    _letterArea = new Rectangle(_tl.x + 70, _br.y - _street1Y - 120, getGameWidth() - 70 - 37, 120);
    _packagesArea = new Rectangle(_tl.x, _br.y - _street1Y - 38 - 88, getGameWidth(), 88);

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

    area = new Rectangle(_tl.x, _br.y - getRailsHome()[0] - 195, 130, 195);// 130
    _pointerLeft = new ImageData(POINTER_LEFT_BLUE, area, _comparator, 0, 0);

    area = new Rectangle(_br.x - 130, _br.y - getRailsHome()[0] - 195, 130, 195);
    _pointerRight = new ImageData(POINTER_RIGHT_BLUE, area, _comparator, 0, 0);

    xx = (getGameWidth() - 780) / 2;
    yy = (getGameHeight() - 585) / 2;
    xx += _tl.x;
    yy += _tl.y;

    area = new Rectangle(xx + 30, yy + 25, 65, 20);
    _contracts = new ImageData(CONTRACTS, area, _comparator, 1, 0);

    xx = (getGameWidth() - 780) / 2;
    yy = (getGameHeight() - 585) / 2;
    xx += _tl.x;
    yy += _tl.y;

    area = new Rectangle(xx + 17, yy + 17, 237, 37);
    _materials = new ImageData(MATERIALS, area, _comparator, 0, 0);
    area = new Rectangle(_br.x - 181, _tl.y, 181, 66);
    _materialsButton = new ImageData("Materials2.bmp", area, _comparator, 9, 6);
    xx = (getGameWidth() - 104) / 2;
    area = new Rectangle(_br.x - xx, _tl.y, xx, 36);
    _goldIcon = new ImageData("goldIcon.bmp", area, _comparator, 16, -1);

    // HOORAY
    xx = (getGameWidth() - 580) / 2;
    yy = (getGameHeight() - 453) / 2;
    xx += _tl.x;
    yy += _tl.y;
    area = new Rectangle(xx + 194, yy + 397, 192, 39);

    getImageData("Hooray.bmp", area, 23, 6);

    // SHARE
    xx = (getGameWidth() - 60) / 2;
    area = new Rectangle(_tl.x + xx, _br.y - 135, 60, 22);
    _share = new ImageData(SHARE, area, _comparator, 23, 6);

    xx = (getGameWidth() - 780) / 2;
    yy = (getGameHeight() - 585) / 2;
    xx += _tl.x;
    yy += _tl.y;
    area = new Rectangle(xx + 30, yy + 20, 100, 30);
    _trainsAnchor = new ImageData(INT_TRAINS, area, _comparator, 0, 0);

    xx = (getGameWidth() - 520) / 2;
    yy = (getGameHeight() - 280) / 2;
    xx += _tl.x;
    yy += _tl.y;

    area = new Rectangle(xx + 187, yy + 233, 150, 30);
    getImageData("Sure.bmp", area, 15, 5);

    // xx = (getGameWidth() + 250 - 595) / 2;
    // yy = 290 + _tl.y;
    // final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    // area = new Rectangle(0, yy, screenSize.width, screenSize.height - yy - 20);
    // _fbShare = new ImageData("FBShare.bmp", area, _comparator, 19, 5);

    _whistlesPoint = new Pixel(_tl.x + 36, _tl.y + 310);

    _resendP = new Pixel(_br.x - 85, _br.y - 178);
  }

  public Pixel getTopPlayersPixel() {
    return _topPlayersPixel;
  }

  public boolean locateGameArea() throws AWTException, IOException, RobotInterruptedException {
    // TS still needs a special way of locating game and passengers area
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
    Pixel tl = null;
    Pixel br = null;
    do {
      turns++;
      Pixel tslogo = locateImageCoords("anchorTopLeftTSLOGO.bmp", areaTL, -20, 46);
      if (tslogo != null) {
        if (tslogo.x < 0)
          tslogo.x = 0;
        tl = locateImageCoords("anchorTopLeftNEW.bmp", new Rectangle[] { new Rectangle(tslogo.x, tslogo.y, 260, 40) }, 0, -11);

        if (tl != null) {
          _passengersArea = new Rectangle(tl.x + 13, tl.y + 11, 104, 14);
          if (Math.abs(tslogo.x - tl.x) > 20) {
            tl.x = tslogo.x;
          } else {
            tl.x -= 21;
          }
          if (tl.x < 0)
            tl.x = 0;
          Rectangle[] areaBR = new Rectangle[] { new Rectangle(screenSize.width - 379, screenSize.height - 270, 113, 100),
              new Rectangle(tl.x + 684, tl.y + 543, 300, 100), new Rectangle(tl.x + 684, tl.y + 543, screenSize.width - 270 - 684, 100),
              new Rectangle(684, 607, screenSize.width - 270 - 684, screenSize.height - 607), new Rectangle(screenSize) };
          LOGGER.info("Found top left corner...");
          // good, we're still in the game
          br = locateImageCoords("anchorInvite2.bmp", areaBR, 61, 46);
          if (br != null) {
            LOGGER.info("Found bottom right corner...");
            done = true;
          } else {
            LOGGER.info("Move a bit and try again...");
            Pixel p = new Pixel(tl.x, tl.y - 2);
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
      }
    } while (!done && turns < 3);

    if (br != null && tl != null) {
      _tl = tl;
      _br = br;
      LOGGER.info("Top left    : " + _tl);
      LOGGER.info("Bottom Right: " + _br);
      setKeyAreas();
      return done;
    } else {
      // keep original coordinates
      // _tl = new Pixel(0, 0);
      // _br = new Pixel(1600, 1000);
      // setKeyAreas();
    }
    return false;
  }

  public void writeImage(Rectangle rect, String filename) {
    try {
      writeImage(new Robot().createScreenCapture(rect), filename);
    } catch (AWTException e) {
      e.printStackTrace();
    }
  }

  public void writeImage2(Rectangle rect, String filename) {
    try {
      SimpleDateFormat sdf = new SimpleDateFormat("MM-dd  HH-mm-ss-SSS");
      String date = sdf.format(Calendar.getInstance().getTime());
      String filename2 = filename + " " + date + ".png";

      writeImage(new Robot().createScreenCapture(rect), filename2);
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

  public void captureGame() {
    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd  HH-mm-ss-SSS");
    String date = sdf.format(Calendar.getInstance().getTime());
    String filename = "popup " + date + ".png";
    captureGame(filename);
  }

  public void captureGame(String filename) {
    writeImage(new Rectangle(new Point(_tl.x, _tl.y), new Dimension(getGameWidth(), getGameHeight())), filename);
  }

  public Pixel locateImageCoords(String imageName, Rectangle[] area, int xOff, int yOff) throws AWTException, IOException, RobotInterruptedException {
    Pixel p = null;

    int turn = 0;
    while (turn < area.length) {
      p = scanOneFast(imageName, area[turn], false);
      if (p != null) {
        p.x += xOff;
        p.y += yOff;
        break;
      }
      turn++;
    }

    return p;
  }

  public boolean isOptimized() {
    return _optimized && _br != null && _tl != null;
  }

  private List<Pixel> findEdge(final BufferedImage targetImage, final BufferedImage area, ImageComparator comparator, Map<Integer, Color[]> colors,
      Pixel[] indices) {
    if (_debugMode)
      try {
        MyImageIO.write(area, "PNG", new File("C:/area.png"));
      } catch (IOException e) {
        e.printStackTrace();
      }
    List<Pixel> result = new ArrayList<Pixel>(8);
    for (int i = 0; i < (area.getWidth() - targetImage.getWidth()); i++) {
      for (int j = 0; j < (area.getHeight() - targetImage.getHeight()); j++) {
        final BufferedImage subimage = area.getSubimage(i, j, targetImage.getWidth(), targetImage.getHeight());
        if (_debugMode)
          try {
            MyImageIO.write(subimage, "PNG", new File("C:/subimage.png"));
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
      if (_debugMode)
        MyImageIO.write(screenshot, "PNG", new File("screenshot.png"));
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
    if (_debugMode)
      try {
        ImageIO.write(image2, "PNG", new File("area.png"));
      } catch (IOException e) {
        e.printStackTrace();
      }
    List<Pixel> result = new ArrayList<Pixel>(8);
    for (int i = 0; i <= (image2.getWidth() - image1.getWidth()); i++) {
      for (int j = 0; j <= (image2.getHeight() - image1.getHeight()); j++) {
        final BufferedImage subimage = image2.getSubimage(i, j, image1.getWidth(), image1.getHeight());
        if (_debugMode)
          try {
            MyImageIO.write(subimage, "PNG", new File("subimage.png"));
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

  public Rectangle getLetterArea() {
    return _letterArea;
  }

  public Rectangle getPackagesArea() {
    return _packagesArea;
  }

  public int[] getRailsOut() {
    return _settings.getArray("railsOut");
  }

  public int[] getRailsHome() {
    return _settings.getArray("railsHome");
    // return _railsHome;
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

  public ImageData getLetterRed() {
    return _letterRed;
  }

  public ImageData getLetterWhite() {
    return _letterWhite;
  }

  public ImageData getLetterBrown() {
    return _letterBrown;
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
    return _loginWithFB;
  }

  public ImageData getInvite() {
    return _invite;
  }

  public ImageData getDailyRewards() {
    return _dailyRewards;
  }

  public ImageData getSessionTimeOut() {
    return _sessionTimeOut;
  }

  public ImageData getSyncError() {
    return _syncError;
  }

  public void addHandler(Handler handler) {
    LOGGER.addHandler(handler);
  }

  // public ImageData getTopLeftImage() {
  // return _topLeftImage;
  // }

  public ImageData getTrainManagementAnchor() {
    return _trainManagementAnchor;
  }

  public ImageData getSixMinutes() {
    return _sixMinutes;
  }

  public ImageData getTrainsAnchor() {
    return _trainsAnchor;
  }

  public ImageData getExpressTrain() {
    return _expressTrain;
  }

  public ImageData getFreeTrain() {
    return _freeTrain;
  }

  public ImageData getXPTrain() {
    return _xpTrain;
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
    _street1Y = _settings.getInt("street1Y", 204);
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

  public ImageData getShare() {
    return _share;
  }

  public ImageData getContracts() {
    return _contracts;
  }

  public ImageData getMaterials() {
    return _materials;
  }

  public ImageData getMaterialsButton() {
    return _materialsButton;
  }

  public ImageData getGoldIcon() {
    return _goldIcon;
  }

  public ImageData generateImageData(String imageFilename) throws IOException {
    return new ImageData(imageFilename, null, _comparator, 0, 0);
  }

  public ImageData generateImageData(String imageFilename, int xOff, int yOff) throws IOException {
    return new ImageData(imageFilename, null, _comparator, xOff, yOff);
  }

  public ImageComparator getComparator() {
    return _comparator;
  }

  public ImageData getPackage1() {
    return _package1;
  }

  public ImageData getPackage2() {
    return _package2;
  }

  public Rectangle getPassengersArea() {
    return _passengersArea;
  }

  public ImageData generateLetterImageData(int index) throws IOException {
    return new ImageData("letters/letter" + index + ".bmp", null, new SimilarityImageComparator(0.04, 2000), 4, 2);
  }

  public ImageData getMaglevDest() {
    return _maglevDest;
  }

  public Pixel getWhistlesPoint() {
    return _whistlesPoint;
  }

  public boolean isHyperloop(Pixel tm) throws AWTException, RobotInterruptedException, IOException {
    Rectangle area = new Rectangle(tm.x, tm.y + 288, 30, 102);
    Pixel p = scanOneFast("mat1.bmp", area, false);
    if (p != null)
      return true;
    p = scanOneFast("mat2.bmp", area, false);
    if (p != null)
      return true;
    p = scanOneFast("mat3.bmp", area, false);
    if (p != null)
      return true;
    p = scanOneFast("mat4.bmp", area, false);

    return p != null;
  }

  public void adjustHome(boolean resend) throws AWTException, RobotInterruptedException, IOException {
    _resendP = null;
    _offset = 112;

    Rectangle area = new Rectangle(_br.x - 170, _br.y - 136, 170, 24);
    _resendArea = new Rectangle(_br.x - 140, _br.y - 204, 140, 55);
    int y = _br.y - 104;
    int x1 = _br.x - 15;
    int x2 = _br.x - 15 - _settings.getInt("resendOffset", 105);

    if (resend) {
      _mouse.delay(500);
      // _scanner.writeArea(area, "polearea.bmp");
      // DEPRECATE THIS
      Pixel pp = scanOneFast("pole.bmp", area, false);
      if (pp == null) {
        _offset = 80;
        _mouse.drag4(x1, y, x2, y, false, false);
        _mouse.delay(250);
        pp = scanOneFast("pole.bmp", area, false);
        LOGGER.fine("pole found again:  " + (pp != null));
        // offset = getOffset(offset, area);
      } else {
        // it found but let's see is it far enough from edge
        if (_br.x - pp.x < 50) {
          _offset = 80;
          _mouse.drag4(x1, y, x2, y, false, false);
          _mouse.delay(250);
          pp = scanOneFast("pole.bmp", area, false);
          LOGGER.fine("pole found again2:  " + (pp != null));
        }
      }
      if (pp != null) {
        _offset = _br.x - pp.x;
        _resendP = new Pixel(pp.x + 28, pp.y - 42);
        if (_resendP.x > _br.x) {
          LOGGER.info("resend still outside...");
          _resendP.x = _br.x - 2;
        }
        _resendArea = new Rectangle(pp.x, _br.y - 204, _br.x - pp.x, 55);
        // _scanner.writeArea(resendArea, "resendArea.bmp");
      }
    } else {
      Pixel pp = scanOneFast("pole.bmp", area, false);
      if (pp != null) {
        _offset = _br.x - pp.x;
      }
    }

  }

  public Pixel scanResend() throws AWTException, RobotInterruptedException, IOException {
    _resendArea = new Rectangle(_br.x - 140, _br.y - 204, 140, 55);
    Pixel p = scanOneFast("resend.bmp", _resendArea, false);
    if (p != null) {
      _resendP = new Pixel(p.x + 17, p.y + 14);
      _offset = _br.x - p.x + 9;
    }
    return p;
  }

  public boolean isJourney(Pixel p) throws AWTException, RobotInterruptedException, IOException {
    Rectangle area;
    if (p != null) {
      area = new Rectangle(p.x - 482, p.y + 72, 156, 42);
    } else {
      int xx = (getGameWidth() - 520) / 2;
      area = new Rectangle(_tl.x + xx, _tl.y + 215, 235, 75);
    }
    Pixel pp = scanOneFast("journeyUnlocked.bmp", area, false);
    return pp != null;
  }

  public boolean sellWAGR() throws AWTException, RobotInterruptedException, IOException {
    boolean res = false;
    Rectangle area = new Rectangle(_tl.x + 105, _br.y - 70, 70, 70);
    Pixel p = scanOneFast("shopButton.bmp", area, false);
    if (p != null) {
      _mouse.click(p.x + 75, p.y);
      _mouse.delay(1000);

      area = new Rectangle(_br.x - 289, _tl.y + 93, 289, 78);
      int turn = 0;
      do {
        p = scanOneFast("searchButton.bmp", area, true);
        turn++;
        if (p == null) {
          try {
            Thread.sleep(2000);
          } catch (InterruptedException e) {
          }
        }
      } while (p == null && turn < 3);
      if (p != null) {
        _mouse.mouseMove(p.x - 158, p.y - 14);
        // 3. write wagr s
        new KeyboardRobot().enterText("wagr s");
        _mouse.delay(500);
        // 4 looking for wagr s loco
        area = new Rectangle(_tl.x + 40, _tl.y + 180, getGameWidth() - 80, 27);
        p = scanOneFast("wagrSClass.bmp", area, true);
        if (p != null) {
          _mouse.delay(700);
          _mouse.click(p.x + 44, p.y + 343);
          _mouse.delay(700);
          //TODO scrollbar eventurally
          area = generateWindowedArea(560, 210);
          _mouse.click(area.x + 469, area.y + 120);
          _mouse.delay(700);
          _mouse.click(area.x + 365, area.y + 178);
          _mouse.delay(700);
          res = true;
        }
      }

    }
    if (scanOneFast(ScreenScanner.SHOP_X, true) != null)
      _mouse.delay(2000);
    return res;
  }

  public boolean buyWAGR(int clicks) throws AWTException, RobotInterruptedException, IOException {
    boolean res = false;
    // 1. first ensure shop button is available. click it
    Rectangle area = new Rectangle(_tl.x + 105, _br.y - 70, 70, 70);
    if (scanOneFast("shopButton.bmp", area, true) != null) {
      _mouse.delay(1000);
      // 2. click on search button
      Pixel p = null;
      area = new Rectangle(_br.x - 289, _tl.y + 93, 289, 78);
      int turn = 0;
      do {
        p = scanOneFast("searchButton.bmp", area, true);
        turn++;
        if (p == null) {
          try {
            Thread.sleep(2000);
          } catch (InterruptedException e) {
          }
        }
      } while (p == null && turn < 3);
      
      if (p != null) {
        _mouse.mouseMove(p.x - 158, p.y - 14);
        _mouse.delay(500);
        // 3. write wagr s
        new KeyboardRobot().enterText("wagr s");
        _mouse.delay(500);
        // 4 looking for wagr s loco
        area = new Rectangle(_tl.x + 40, _tl.y + 180, getGameWidth() - 80, 27);
        p = scanOneFast("wagrSClass.bmp", area, true);
        if (p != null) {
          _mouse.mouseMove(p.x + 45, p.y + 354);
          for (int i = 0; i < clicks; i++) {
            _mouse.click();
            _mouse.delay(80);
          }
          res = true;
        }
      }
    }

    if (scanOneFast(ScreenScanner.SHOP_X, true) != null)
      _mouse.delay(2000);
    return res;
  }
}
