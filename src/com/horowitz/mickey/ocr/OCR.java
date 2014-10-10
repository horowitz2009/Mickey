package com.horowitz.mickey.ocr;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import com.horowitz.mickey.ImageManager;
import com.horowitz.mickey.Pixel;

public class OCR {
  private Color               _foreground = Color.BLACK;
  private Color               _background = Color.WHITE;
  private Map<Integer, Color> _colors;

  public OCR() {
    super();
    _colors = new HashMap<>(2);
    _colors.put(1, _foreground);
    _colors.put(0, _background);
  }

  public String scanImage(BufferedImage image) {
    BufferedImage subimage = image.getSubimage(0, 0, image.getWidth(), image.getHeight());// a copy actually
    BufferedImage subimage2 = image.getSubimage(0, 0, image.getWidth(), image.getHeight());// a copy actually
    //subimage = cutEdges(subimage, _foreground);
    String result = null;

    Masks masks = new Masks();
    int w = masks.getMaxWidth();
    int h = masks.getMaxHeight();

    while (subimage.getWidth() >= w) {
      // we have space to work
      subimage2 = subimage.getSubimage(0, 0, w, subimage.getHeight());

      Iterator<Mask> it = masks.getMasks().iterator();
      List<Mask> found = new ArrayList<Mask>();
      while (it.hasNext()) {
        Mask mask = (Mask) it.next();
        Pixel p = findMask(subimage2, mask);
        if (p != null) {
          found.add(mask);
          if (found.size() > 1) {
            // not good. either one or zero should be found
            break;
          }
        }
      }
      
      if (found.size() == 1) {
        // yahoooo
        Mask m = found.get(0);
        result += m.getName();
        // cut the chunk and move forward
        subimage = subimage.getSubimage(0 + m.getWidth(), 0, subimage.getWidth() - m.getWidth(), subimage.getHeight());
      } else if (found.isEmpty()) {
        int howMuchToTheRight = 1; // or w
        subimage = subimage.getSubimage(0 + howMuchToTheRight, 0, subimage.getWidth() - howMuchToTheRight, subimage.getHeight());
      } else {
        // size is 2 or more -> not good!!!
        // skip for now
      }

    }//while

    return result;
  }

  private BufferedImage cutEdges(BufferedImage image, Color foreground) {
    BufferedImage subimage;
    // cut north
    boolean lineClean = true;
    int yStart = 0;
    for (int y = 0; y < image.getHeight(); y++) {

      for (int x = 0; x < image.getWidth(); x++) {
        int diff = compareTwoColors(image.getRGB(x, y), foreground.getRGB());
        if (diff <= 1100) {
          // found one, line not clean
          lineClean = false;
          break;
        }
      }
      if (!lineClean) {
        yStart = y;
        // enough
        break;
      }
    }
    subimage = image.getSubimage(0, yStart, image.getWidth(), image.getHeight() - yStart);

    // cut south
    lineClean = true;
    yStart = image.getHeight() - 1;
    for (int y = image.getHeight() - 1; y >= 0; y--) {

      for (int x = 0; x < image.getWidth(); x++) {
        int diff = compareTwoColors(image.getRGB(x, y), foreground.getRGB());
        if (diff <= 1100) {
          // found one, line not clean
          lineClean = false;
          break;
        }
      }
      if (!lineClean) {
        yStart = y;
        // enough
        break;
      }
    }
    subimage = image.getSubimage(0, 0, image.getWidth(), image.getHeight() - yStart + 1);

    // cut west
    boolean colClean = true;
    int xStart = 0;
    for (int xx = 0; xx < image.getWidth(); xx++) {

      for (int y = 0; y < image.getHeight(); y++) {
        int diff = compareTwoColors(image.getRGB(xx, y), foreground.getRGB());
        if (diff <= 1100) {
          // found one, line not clean
          colClean = false;
          break;
        }
      }
      if (!colClean) {
        xStart = xx;
        // enough
        break;
      }
    }
    subimage = image.getSubimage(xStart, 0, image.getWidth() - xStart, image.getHeight());

    // cut east
    colClean = true;
    xStart = image.getWidth() - 1;
    for (int xx = image.getWidth() - 1; xx >= 0; xx--) {

      for (int y = 0; y < image.getHeight(); y++) {
        int diff = compareTwoColors(image.getRGB(xx, y), foreground.getRGB());
        if (diff <= 1100) {
          // found one, line not clean
          colClean = false;
          break;
        }
      }
      if (!colClean) {
        xStart = xx;
        // enough
        break;
      }
    }
    subimage = image.getSubimage(0, 0, image.getWidth() - xStart + 1, image.getHeight());

    return subimage;
  }

  public Pixel findMask(BufferedImage screen, Mask mask) {
    for (int i = 0; i < (screen.getWidth() - mask.getWidth()); i++) {
      for (int j = 0; j < (screen.getHeight() - mask.getHeight()); j++) {
        final BufferedImage subimage = screen.getSubimage(i, j, mask.getWidth(), mask.getHeight());
        // public boolean compare2(BufferedImage image, Map<Integer, Color> colors, Pixel[] indices, double percentage, int diffIndex) {
        if (compare2(subimage, _colors, mask.getPixelsAsArray(), 0.05, 1100)) {
          /*
           * try { ImageIO.write(subimage, "PNG", new File("subimage.png")); } catch (IOException e) { e.printStackTrace(); }
           */
          Pixel p = new Pixel(i, j);
          return p;
        }
      }
    }
    return null;
  }

  private int compareTwoColors(int rgb1, int rgb2) {
    final int diff = Math.abs(((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF)) * Math.abs(((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF))
        + Math.abs(((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF)) * Math.abs(((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF))
        + Math.abs(((rgb1 >> 0) & 0xFF) - ((rgb2 >> 0) & 0xFF)) * Math.abs(((rgb1 >> 0) & 0xFF) - ((rgb2 >> 0) & 0xFF));
    return diff;
  }

  public boolean compare(BufferedImage image, Map<Integer, Color> colors, Pixel[] indices) {
    int countErrors = 0;
    Set<Integer> keys = colors.keySet();
    for (Integer index : keys) {
      for (int i = 0; i < indices.length; i++) {
        if (indices[i].weight == index) {
          final int rgb1 = colors.get(index).getRGB();
          final int rgb2 = image.getRGB(indices[i].x, indices[i].y);
          int diff = compareTwoColors(rgb1, rgb2);
          if (diff > 1100)
            countErrors++;

          if (countErrors > 4) // this color is bad and that's enough
            return false;
        }
      }
    }
    return true;
  }

  public boolean compare2(BufferedImage image, Map<Integer, Color> colors, Pixel[] indices, double percentage, int diffIndex) {
    int countErrors = 0;
    int possibleErrors = (int) (indices.length * percentage);
    for (int i = 0; i < indices.length; i++) {
      final int rgb1 = colors.get(indices[i].weight).getRGB();
      final int rgb2 = image.getRGB(indices[i].x, indices[i].y);
      final int diff = Math.abs(((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF)) * Math.abs(((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF))
          + Math.abs(((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF)) * Math.abs(((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF))
          + Math.abs(((rgb1 >> 0) & 0xFF) - ((rgb2 >> 0) & 0xFF)) * Math.abs(((rgb1 >> 0) & 0xFF) - ((rgb2 >> 0) & 0xFF));
      if (diff > diffIndex)
        countErrors++;
      if (countErrors > possibleErrors)
        return false;
    }
    return true;
  }

  public boolean compare(BufferedImage image, Color color, Pixel[] indices) {
    int countErrors = 0;
    for (int i = 0; i < indices.length; i++) {
      if (indices[i].weight == 1) {
        final int rgb1 = color.getRGB();
        final int rgb2 = image.getRGB(indices[i].x, indices[i].y);
        final int diff = Math.abs(((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF)) * Math.abs(((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF))
            + Math.abs(((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF)) * Math.abs(((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF))
            + Math.abs(((rgb1 >> 0) & 0xFF) - ((rgb2 >> 0) & 0xFF)) * Math.abs(((rgb1 >> 0) & 0xFF) - ((rgb2 >> 0) & 0xFF));
        if (diff > 1100)
          countErrors++;

        if (countErrors > 4) {
          return false;
        }
      }
    }
    return true;
  }

  public static void main(String[] args) {
    try {
      final BufferedImage image = ImageIO.read(ImageManager.getImageURL("test.bmp"));

      OCR ocr = new OCR();
      String res = ocr.scanImage(image);
      System.out.println(res);

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
