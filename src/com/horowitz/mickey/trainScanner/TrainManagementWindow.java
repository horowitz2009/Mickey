package com.horowitz.mickey.trainScanner;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class TrainManagementWindow extends JFrame {
  private List<Train> _trains;

  public TrainManagementWindow(List<Train> trains) {
    super();
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    _trains = trains;
    setTitle("Int. Train Manager");
    init();
  }

  private void init() {
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout());
    JTextArea area = new JTextArea(7, 40);
    mainPanel.add(new JScrollPane(area));
    for (Train t : _trains) {
      t.getFullImage();
      t.getAdditionalInfoShort();
      
      t.isIdle();
      
      List<ContractorView> contractorViews = t.getContractorViews();
      
    }
    getContentPane().add(mainPanel);

  }

}
