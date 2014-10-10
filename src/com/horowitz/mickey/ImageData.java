package com.horowitz.mickey;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;

public class ImageData {

	private String	              _filename;

	private Map<Integer, Color[]>	_colors;
	private Pixel[]	              _mask;
	private Rectangle	            _defaultArea;

	private Robot	                _robot;

	private BufferedImage	        _image;
	private ImageComparator	      _comparator;
	private int	                  _xOff;

	private int	                  _yOff;

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Pixel findImage() {
		return findImage(null);
	}

	public Pixel findImage(Rectangle areaIn) {
	  Rectangle area = areaIn != null ? areaIn : _defaultArea;
		BufferedImage screen = _robot.createScreenCapture(area);
		// try {
		// ImageIO.write(screen, "PNG", new File("area.png"));
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		for (int i = 0; i < (screen.getWidth() - _image.getWidth()); i++) {
			for (int j = 0; j < (screen.getHeight() - _image.getHeight()); j++) {
				final BufferedImage subimage = screen.getSubimage(i, j, _image.getWidth(), _image.getHeight());
				if (_comparator.compare(_image, subimage, _colors, _mask)) {
					// try {
					// ImageIO.write(_image, "PNG", new File("image.png"));
					// ImageIO.write(subimage, "PNG", new File("subimage.png"));
					// } catch (IOException e) {
					// e.printStackTrace();
					// }
					int x = area.x;
					int y = area.y;
					Pixel p = new Pixel(i, j);
					Pixel resultPixel = new Pixel(p.x + x + _xOff, p.y + y + _yOff);
					return resultPixel;
				}
			}
		}
		return null;
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
