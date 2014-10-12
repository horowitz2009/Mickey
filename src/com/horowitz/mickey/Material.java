package com.horowitz.mickey;

import java.awt.Rectangle;

public class Material {
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

  private String    _imageFilename;
  private Rectangle _area;

  public Material(String name, Rectangle area, int page) {
    super();
    _name = name;
    _area = area;
    _page = page;
  }

}
