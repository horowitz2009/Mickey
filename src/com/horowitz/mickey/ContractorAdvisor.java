package com.horowitz.mickey;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

public class ContractorAdvisor extends JPanel {

  private ContractorsPanel _contractorsPanel;

  public ContractorAdvisor() {
    super();
    setLayout(new BorderLayout());
    JTabbedPane pane = new JTabbedPane();
    _contractorsPanel = new ContractorsPanel();
    pane.addTab("Contractors", _contractorsPanel);
    pane.addTab("Table", new JPanel());
    add(pane);
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame("Contractor Assistant");
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
  }

}
