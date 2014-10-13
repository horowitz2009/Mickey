package com.horowitz.mickey;

import java.awt.Rectangle;

public class MaterialLocation {
  private String _name;
  private int    _page;

  public int getPage() {
    return _page;
  }

  public String getName() {
    return _name;
  }

  public Rectangle getArea() {
    return _area;
  }

  private Rectangle _area;

  public MaterialLocation(String name, Rectangle area, int page) {
    super();
    _name = name;
    _area = area;
    _page = page;
  }
}
