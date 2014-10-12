package com.horowitz.mickey;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import com.horowitz.mickey.ocr.OCR;

public class MaterialsScanner {

  private OCR _ocr;

  public MaterialsScanner() {
    super();
    _ocr = new OCR();
  }

  public void scanMaterials(BufferedImage materialsImage, Material[] materials) {
    for (int i = 0; i < materials.length; i++) {
      Rectangle area = materials[i].getArea();
      BufferedImage subimage = materialsImage.getSubimage(area.x, area.y, area.width, area.height);
      String res = _ocr.scanImage(subimage);
      System.out.println(materials[i].getName() + ": " + res);
    }
  }

}
