package com.horowitz.mickey.trainScanner;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Train {
  private BufferedImage        _fullImage;
  private BufferedImage        _scanImage;
  private BufferedImage        _additionalInfo;
  private BufferedImage        _additionalInfoShort;

  private List<ContractorView> _contractorViews;
  private List<String>         _contractorsToSend = new ArrayList<String>();
  private boolean              _idle;

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

  public void setContractorViews(List<ContractorView> contractorViews) {
    _contractorViews = contractorViews;
  }

  public List<ContractorView> getContractorViews() {
    return _contractorViews;
  }

  public void setSame() {
    _contractorsToSend.clear();
    for (ContractorView cv : _contractorViews) {
      _contractorsToSend.add(cv.getName());
    }
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

}
