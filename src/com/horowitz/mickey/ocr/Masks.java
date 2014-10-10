package com.horowitz.mickey.ocr;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.horowitz.mickey.Pixel;

public class Masks {

  private List<Mask> _masks;
  private int        _maxWidth;
  private int        _maxHeight;

  public List<Mask> getMasks() {
    return _masks;
  }

  public Masks() {
    super();
    _masks = new ArrayList<Mask>();
    loadMasks();
  }

  private void loadMasks() {
    _maxWidth = 0;
    _maxHeight = 0;
    final InputStream stream = OCR.class.getClassLoader().getResourceAsStream("masks.txt");

    // InputStreamReader reader = new InputStreamReader(stream);
    DataInputStream reader = new DataInputStream(stream);
    BufferedReader br = new BufferedReader(new InputStreamReader(reader));
    try {
      String line = null;
      line = br.readLine();
      while (line != null) {
        boolean readIt = true;
        if (line.trim().startsWith("[")) {
          String name = StringUtils.strip(line, " []");
          // this is beginning of new mask
          // String name = line.substring(1);
          // name = name.substring(0, name.length() - 1);
          if (name.length() > 0) {
            // we have a mask name

            List<String> rows = new ArrayList<String>(8);
            int length = 0;
            boolean end = false;
            do {
              line = br.readLine();
              if (line != null) {
                if (line.trim().startsWith("[")) {
                  // we've reached another mask beginning
                  readIt = false;
                  end = true;
                } else {
                  if (line.trim().length() > 0) {
                    line = StringUtils.stripEnd(line, null);
                    if (line.length() > length)
                      length = line.length();
                    rows.add(line);
                  }
                }
              }
            } while (line != null && !end);

            Mask mask = parseMask(name, rows, length);
            if (mask.getHeight() > _maxHeight)
              _maxHeight = mask.getHeight();
            if (mask.getWidth() > _maxWidth)
              _maxWidth = mask.getWidth();
            _masks.add(mask);

          } else {
            line = br.readLine();
          }
        }
        if (readIt) {
          line = br.readLine();
        } else {
          readIt = true;
        }
      }

    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        br.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

  }

  private Mask parseMask(String name, List<String> rows, int width) {
    List<Pixel> list = new ArrayList<Pixel>();
    int y = 0;
    for (String row : rows) {
      for (int x = 0; x < row.length(); ++x) {
        final char ch = row.charAt(x);
        int n = 0;
        if (ch != ' ') {
          n = Integer.parseInt("" + ch);
        }
        list.add(new Pixel(x, y, n));

      }
      for (int x = row.length(); x < width; ++x) {
        list.add(new Pixel(x, y, 0));
      }
      y++;
    }
    return new Mask(name, list, width, rows.size());
  }

  public int getMaxWidth() {
    return _maxWidth;
  }

  public int getMaxHeight() {
    return _maxHeight;
  }

  public void printAll() {
    Iterator<Mask> i = _masks.iterator();
    while (i.hasNext()) {
      Mask mask = (Mask) i.next();
      System.out.println(mask);
    }

    i = _masks.iterator();
    while (i.hasNext()) {
      Mask mask = (Mask) i.next();
      mask.printMask(System.err);
    }

  }

  public static void main(String[] args) {
    Masks masks = new Masks();
    masks.loadMasks();
    masks.printAll();
  }

}
