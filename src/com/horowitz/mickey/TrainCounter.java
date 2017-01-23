package com.horowitz.mickey;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

import com.horowitz.ocr.OCRe;

public class TrainCounter {
  private long   sumCoins = 0;
  private long   sumPass  = 0;
  private OCRe   ocrc;
  private OCRe   ocrp;
  private String folder;
  private String outputFile;

  public TrainCounter(String folder, String outputFile) throws IOException {
    super();
    this.folder = folder;
    this.outputFile = outputFile;
    ocrc = new OCRe("ocr/coin/coin");
    ocrp = new OCRe("ocr/pass/pass");
  }

  public String scanCoins(String filename) throws IOException {
    BufferedImage image = ImageIO.read(new File(filename));
    image = image.getSubimage(308, 308, 122, 17);
    return ocrc.scanImage(image);
  }

  public String scanCoins(Rectangle rect) throws AWTException {
    BufferedImage image = new Robot().createScreenCapture(rect);
    return ocrc.scanImage(image);
  }

  public String scanPassengers(Rectangle rect) throws AWTException {
    BufferedImage image = new Robot().createScreenCapture(rect);
    return ocrp.scanImage(image);
  }

  public String scanPassengers(String filename) throws IOException {
    BufferedImage image = ImageIO.read(new File(filename));
    image = image.getSubimage(495, 143, 113, 17);
    return ocrp.scanImage(image);
  }

  public void doReport() throws IOException {
    List<String> result = scanAll(folder);
    File file = new File(outputFile);

    FileUtils.writeLines(file, result);
  }

  public List<String> scanAll(String folderName) throws IOException {
    List<String> listCoins = new ArrayList<String>(100);
    File folder = new File(folderName);
    if (folder.exists() && folder.isDirectory()) {
      String[] list = folder.list(new FilenameFilter() {

        @Override
        public boolean accept(File file, String fn) {
          return fn.indexOf("train sent") >= 0;
        }
      });

      for (String fn : list) {
        fn = folder + "/" + fn;
        String s = scanCoins(fn);
        String err = "";
        try {
          long number = Integer.parseInt(s);
          sumCoins += number;
        } catch (NumberFormatException e) {
          System.err.println("Failed to scan coins from " + fn);
          err = "1";
        }

        String s2 = scanPassengers(fn);
        try {
          long number = Integer.parseInt(s2);
          sumPass += number;
        } catch (NumberFormatException e) {
          System.err.println("Failed to scan passengers from " + fn);
          err = "2";
        }

        listCoins.add(s + "," + s2 + "," + sumCoins + "," + sumPass + "," + fn + "," + err);
      }
      NumberFormat nf = NumberFormat.getNumberInstance();
      for (String line : listCoins) {
        System.out.println(line);
      }
      System.out.println("total coins: " + nf.format(sumCoins));
      System.out.println("total passengers: " + nf.format(sumPass));
    } else {
      System.err.println(folderName + " does not exists or is not a folder!");
    }
    return listCoins;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    String folder = "1701";
    String outputFile = "report.csv";
    if (args.length > 0) {
      folder = args[0];
      if (args.length > 1)
        outputFile = args[1];
    }
    try {
      TrainCounter trainCounter = new TrainCounter(folder, outputFile);
      trainCounter.doReport();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
