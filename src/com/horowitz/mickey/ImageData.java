package com.horowitz.mickey;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;

import com.horowitz.mickey.common.MyImageIO;

public class ImageData {

  private String                _filename;

  private Map<Integer, Color[]> _colors;
  private Pixel[]               _mask;
  private Rectangle             _defaultArea;

  private Robot                 _robot;

  private BufferedImage         _image;
  private ImageComparator       _comparator;
  private int                   _xOff;

  private int                   _yOff;

  public ImageData(String filename, Rectangle defaultArea, ImageComparator comparator, int xOff, int yOff) throws IOException {
    super();
    this._filename = filename;
    this._defaultArea = defaultArea;
    this._comparator = comparator;

    _image = ImageIO.read(ImageManager.getImageURL(filename));
    ImageMask imageMask = new ImageMask(filename);
    _mask = imageMask.getMask();
    _colors = imageMask.getColors();

    _xOff = xOff;
    _yOff = yOff;
    try {
      _robot = new Robot();
    } catch (AWTException e) {
      e.printStackTrace();
    }
  }

  public Pixel findImage() {
    return findImage((Rectangle) null);
  }

  public Pixel findImage(BufferedImage screen) {
    for (int i = 0; i <= (screen.getWidth() - _image.getWidth()); i++) {
      for (int j = 0; j <= (screen.getHeight() - _image.getHeight()); j++) {
        final BufferedImage subimage = screen.getSubimage(i, j, _image.getWidth(), _image.getHeight());
        writeImage(subimage, 201);
        if (_comparator.compare(_image, subimage, _colors, _mask)) {
          Pixel p = new Pixel(i, j);
          Pixel resultPixel = new Pixel(p.x + _xOff, p.y + _yOff);
          return resultPixel;
        }
      }
    }
    return null;
  }
  
  private void writeImage(BufferedImage image, int n) {
    if (false)
      try {
        MyImageIO.write(image, "PNG", new File("subimage" + n + ".png"));
      } catch (IOException e) {
        e.printStackTrace();
      }
  }


  public Pixel findImage(Rectangle areaIn) {
    Rectangle area = areaIn != null ? areaIn : _defaultArea;
    BufferedImage screen = _robot.createScreenCapture(area);
    Pixel p = findImage(screen);
    if (p != null) {
      p.x = p.x + area.x;
      p.y = p.y + area.y;
    }
    return p;
  }

  public int get_xOff() {
    return _xOff;
  }

  public void set_xOff(int _xOff) {
    this._xOff = _xOff;
  }

  public int get_yOff() {
    return _yOff;
  }

  public void set_yOff(int _yOff) {
    this._yOff = _yOff;
  }

  public String getName() {
    return _filename.substring(0, _filename.length() - 5);
  }

  public Rectangle getDefaultArea() {
    return _defaultArea;
  }

}
