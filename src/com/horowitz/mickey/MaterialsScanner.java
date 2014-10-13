package com.horowitz.mickey;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.horowitz.mickey.data.Material;
import com.horowitz.mickey.ocr.OCR;

public class MaterialsScanner {

  private OCR _ocr;

  public MaterialsScanner() {
    super();
    _ocr = new OCR();
  }

  public Material[] scanMaterials(BufferedImage materialsImage, MaterialLocation[] materials) {
    List<Material> res = new ArrayList<Material>(17);
    for (int i = 0; i < materials.length; i++) {
      Rectangle area = materials[i].getArea();
      BufferedImage subimage = materialsImage.getSubimage(area.x, area.y, area.width, area.height);
      String resAmount = _ocr.scanImage(subimage);
      System.out.println(materials[i].getName() + ": " + resAmount);
      Integer amount = 0;
      if (resAmount.length() > 0)
        amount = Integer.parseInt(resAmount);
      res.add(new Material(materials[i].getName(), amount));
    }
    
    return res.toArray(new Material[0]);
  }
  
  public static void main(String[] args) {
    try {
      BufferedImage image = ImageIO.read(ImageManager.getImageURL("wolfgangmaterials.bmp"));
      MaterialsScanner scanner = new MaterialsScanner();
      Material[] scanMaterials = scanner.scanMaterials(image, Locations.MATERIALS_1);
      System.err.println(scanMaterials);
      
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

}
