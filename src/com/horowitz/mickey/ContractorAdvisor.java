package com.horowitz.mickey;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import com.horowitz.mickey.data.ContractAnalysis;
import com.horowitz.mickey.data.Need;

public class ContractorAdvisor extends JPanel {

  private ContractorsPanel _contractorsPanel;
  private ContractTablePanel _contractTablePanel;
  private ContractTablePanel _contractTablePanel2;

  public ContractorAdvisor() {
    super();
    setLayout(new BorderLayout());
    JTabbedPane pane = new JTabbedPane();
    _contractorsPanel = new ContractorsPanel();
    pane.addTab("Contractors", _contractorsPanel);
    ContractAnalysis ca = new ContractAnalysis();
    Map<String, Map<String, Need>> map = ca.collectCurrentNeeds();


    _contractTablePanel = new ContractTablePanel(new TableModel(map));
    _contractTablePanel2 = new ContractTablePanel(new TableModel(null));
    pane.addTab("Table", _contractTablePanel);
    //_contractTablePanel2.setMap(null);
    pane.addTab("Table2", _contractTablePanel2);
    add(pane);
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame("Contract Advisor v.103");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    final ContractorAdvisor advisor = new ContractorAdvisor();
    frame.getContentPane().add(advisor, BorderLayout.CENTER);

    frame.setSize(new Dimension(800, 550));

    frame.setLocationRelativeTo(null);

    frame.setVisible(true);
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        advisor.reloadAll();

      }
    });

  }

  private void reloadAll() {
    _contractorsPanel.reloadAll();
    _contractTablePanel.reload();
  }

}
