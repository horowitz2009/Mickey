package com.horowitz.mickey.trainScanner;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Train {
  private transient BufferedImage _fullImage;
  private String                  _fullImageFileName;

  private transient BufferedImage _scanImage;
  private String                  _scanImageFileName;

  private transient BufferedImage _additionalInfo;
  // private String _additionalInfoFileName;

  private transient BufferedImage _additionalInfoShort;
  private String                  _additionalInfoShortFileName;

  private List<String>            _contractorsBeenSent;
  private List<String>            _contractorsToSend = new ArrayList<String>();
  private boolean                 _idle;
  private long                    _sentTime;

  public BufferedImage getAdditionalInfo() {
    return _additionalInfo;
  }

  public void setAdditionalInfo(BufferedImage additionalInfo) {
    _additionalInfo = additionalInfo;
  }

  public Train(BufferedImage fullImage, BufferedImage scanImage) {
    super();

    _fullImage = fullImage;
    _scanImage = scanImage;
  }

  public BufferedImage getFullImage() {
    return _fullImage;
  }

  public void setFullImage(BufferedImage fullImage) {
    _fullImage = fullImage;
  }

  public BufferedImage getScanImage() {
    return _scanImage;
  }

  public void setScanImage(BufferedImage scanImage) {
    _scanImage = scanImage;
  }

  public void setContractorsBeenSent(List<String> contractorsBeenSent) {
    _contractorsBeenSent = contractorsBeenSent;
  }

  public List<String> getContractorsBeenSent() {
    return _contractorsBeenSent;
  }

  public void setSame() {
    _contractorsToSend.clear();
    _contractorsToSend.addAll(_contractorsBeenSent);
    // for (String cname : _contractorsBeenSent) {
    // _contractorsToSend.add(cname);
    // }
  }

  public List<String> getContractorsToSend() {
    return _contractorsToSend;
  }

  public BufferedImage getAdditionalInfoShort() {
    return _additionalInfoShort;
  }

  public void setAdditionalInfoShort(BufferedImage additionalInfoShort) {
    _additionalInfoShort = additionalInfoShort;
  }

  public void setIdle(boolean isIdle) {
    _idle = isIdle;
  }

  public boolean isIdle() {
    return _idle;
  }

  public void setSentTime(long time) {
    _sentTime = time;
  }

  public long getSentTime() {
    return _sentTime;
  }

  public String getFullImageFileName() {
    return _fullImageFileName;
  }

  public void setFullImageFileName(String fullImageFileName) {
    _fullImageFileName = fullImageFileName;
  }

  public String getAdditionalInfoShortFileName() {
    return _additionalInfoShortFileName;
  }

  public void setAdditionalInfoShortFileName(String additionalInfoShortFileName) {
    _additionalInfoShortFileName = additionalInfoShortFileName;
  }

  public String getScanImageFileName() {
    return _scanImageFileName;
  }

  public void setScanImageFileName(String scanImageFileName) {
    _scanImageFileName = scanImageFileName;
  }

}
